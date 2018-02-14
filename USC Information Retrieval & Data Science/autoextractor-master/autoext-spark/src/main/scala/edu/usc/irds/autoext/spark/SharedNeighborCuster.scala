/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.usc.irds.autoext.spark

import java.util

import edu.usc.irds.autoext.cluster.SharedNeighborClusterer._
import org.apache.spark.graphx.{Edge, Graph}
import org.apache.spark.mllib.linalg.distributed.MatrixEntry
import org.apache.spark.rdd.RDD
import org.kohsuke.args4j.Option

import scala.collection.JavaConversions._
import scala.collection.mutable

/**
  * Shared Near Neighbor Clustering implemented using GraphX on spark
  *
  * @author Thamme Gowda N.
  */
class SharedNeighborCuster extends IOSparkJob {

  @Option(name = "-sim", aliases = Array("--similarityThreshold"),
    usage = "if two items have similarity above this value," +
      " then they will be treated as neighbors. Range[0.0, 1.0]")
  var similarityThreshold: Double = 0.7

  @Option(name = "-share", aliases = Array("--sharingThreshold"),
    usage = "if the percent of similar neighbors in clusters exceeds this value," +
      " then those clusters will be collapsed/merged into same cluster. Range:[0.0, 1.0]")
  var sharedNeighborThreshold: Double = 0.8

  @Option(name = "-d3export", usage = "Exports data to d3 JSON format")
  var d3Export = false


  /**
    * Clusters the items based on similarity using shared near neighbors
    * @param simMatrix An RDD of Matrix Entries
    * @param simThreshold threshold for treating entries as neighbors
    * @param snThreshold threshold for clubbing the cluster (Shared Neighbors)
    * @return graph after running clustering algorithm
    */
  def cluster(simMatrix:RDD[MatrixEntry], simThreshold:Double,
              snThreshold:Double): Graph[VertexData, Double] ={

    var entryRDD = simMatrix
    //STEP : Initial set of neighbors
    entryRDD = entryRDD.filter(e => e.value >= simThreshold).cache()

    //Step : Initial edges
    var edges = entryRDD.filter(e => e.i < e.j)  // only one edge out of (1,2) and (2, 1)
      .map(e => {Edge(e.i, e.j, e.value)})

    //Step :  Initial set of Vertices
    var vertices = entryRDD
      .map(t => (t.i, (t.j, t.value)))
      .groupByKey()
      .map({case (vId, ns) =>
        val assignments = new mutable.HashSet[Long]()
        assignments.add(vId)                // item belong to its own cluster
      val neighbors = new util.BitSet()
        neighbors.set(vId.toInt, true)
        ns.foreach({ case(nId, v) => neighbors.set(nId.toInt, true)}) //FIXME: converting long to int here, possible overflow

        (vId, new VertexData(neighbors, assignments))
      })

    var graph:Graph[VertexData, Double] = null
    var hasMoreIteration = true
    var iterations:Int = 0

    while (hasMoreIteration) {
      iterations += 1
      //Step : Initial Graph
      graph =  Graph(vertices.distinct(), edges.distinct()).cache()
      println(s" Before Iteration $iterations :: Num vertices=${graph.vertices.count()}," +
        s" Num Edges=${graph.edges.count()}")

      //Step : collapse similar clusters
      val replacementRdd = graph.triplets
        //filter clusters which are mutually neighbours => source in dest and dest in source
        .filter(et => et.srcAttr.neighbors.get(et.srcId.toInt) && et.srcAttr.neighbors.get(et.dstId.toInt))
        //transform to a convenient form (srcCluster, (similarity, destCluster))
        .map(et => (Math.max(et.srcId, et.dstId),
        (findOverlap(et.srcAttr.neighbors, et.dstAttr.neighbors), Math.min(et.srcId, et.dstId))))
        // Filter clusters which exceeds threshold shared neighbors
        .filter(_._2._1 >= snThreshold)
        // when there are multiple target assignments, reduction to choose one out
        .reduceByKey({
        case ((sim1, cluster1),(sim2, cluster2)) =>
          if (Math.abs(sim1 - sim2) < 1e-6) {  // if the similarity is same, pick smaller numeric index
            (sim1, Math.min(cluster1, cluster2))
          } else if (sim1 > sim2) {           // highest similar cluster
            (sim1, cluster1)
          } else {
            (sim2, cluster2)
          }})
        .mapValues(_._2) // Dropped -> TargetAssignment, Similarity Measure not required

      //tree map to keep the keys sorted in ascending order
      val replacements = new util.TreeMap[Long, Long](replacementRdd.collectAsMap())
      // resolve the transitive replacements {2=1, 3=2} => {2=1, 3=1}
      for (k <- replacements.keySet()) {   //TODO: if possible, do distributed computation
      var key = replacements.get(k)
        while (replacements.containsKey(key)){
          key = replacements.get(key)
        }
        replacements.put(k, key)
      }

      println(s"Number of clusters collapsed :: ${replacements.size()}")
      //Step  : Decision : Can there be next Iteration?
      hasMoreIteration = !replacements.isEmpty
      if (hasMoreIteration){
        //Step : Update : Finding vertices for next iteration
        vertices = graph.vertices
          .map({case (vId, data) =>
            if (replacements.containsKey(vId)){
              (replacements.get(vId), data)   // pass data of this vertex to the assigned vertex
            } else {
              // this vertex remains, but the neighbors should be updated
              for ((k,v) <- replacements if v != vId && data.neighbors.get(k.toInt)) {
                //for all replacements which were its neighbors
                data.neighbors.set(k.toInt, false)  //unset k'th bit and set v'th bit
                data.neighbors.set(v.toInt, true)
              }
              (vId, data)
            }
          })
          .reduceByKey({case (data1, data2) =>
            data1.items ++= data2.items; data1  // items are joined
          })

        //Step 2 : Update : Finding updated edges for the next iteration
        edges = graph.edges.map( e => {
          if (replacements.containsKey(e.srcId) || replacements.contains(e.dstId)) {
            //affected edge
            Edge(replacements.getOrElse(e.srcId, e.srcId),
              replacements.getOrElse(e.dstId, e.dstId), e.attr)
          } else { // un affected
            e
          }
        }).filter(e => e.srcId != e.dstId) // drop looping edges
      }
    }
    graph
  }

  def run(): Unit ={

    //STEP : Input set of similarity matrix
    val matrixEntries:RDD[MatrixEntry] = sc.union(
      getInputPaths().map(sc.objectFile[MatrixEntry](_)))

    //STEP : Cluster
    val graph = cluster(matrixEntries, similarityThreshold, sharedNeighborThreshold)

    //STEP : Format output
    val clusters = graph.vertices.map({case (id, data) =>
      //To iterate over the true bits in a BitSet, use the following loop:
      s"$id,${data.items.size}," + data.items.mkString(",")
    }).cache()

    //STEP save output
    clusters.saveAsTextFile(outPath)
    println(s"Total Clusters = ${clusters.count()}");

    //Optional STEP : Export
    if (d3Export){
      val d3exp = new D3Export
      d3exp.sc = sc
      d3exp.inputPath = outPath
      d3exp.outPath = s"$outPath.json"
      LOG.info(s"Exporting D3 file at ${d3exp.outPath}")
      d3exp.run()
    }
    LOG.info("All Done")
  }
}


class VertexData (val neighbors:util.BitSet, val items:mutable.HashSet[Long])
  extends Serializable {}

object SharedNeighborCuster {

  def main(args: Array[String]) {
    new SharedNeighborCuster().run(args)
  }
}
