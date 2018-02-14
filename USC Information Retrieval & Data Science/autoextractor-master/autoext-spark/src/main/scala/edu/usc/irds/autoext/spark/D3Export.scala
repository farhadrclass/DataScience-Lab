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

import edu.usc.irds.autoext.utils.D3JsFormat
import org.kohsuke.args4j.Option

import scala.collection.JavaConverters._

/**
  * This CLI Tool exports clusters into most common format used by d3js charts.
  */
class D3Export extends IOSparkJob {
  @Option(name="-ids", usage = "Path to directory/file having index to id mapping. Optional.")
  var idsFile:String = null

  override def run(): Unit = {
    val rdd = sc.union(getInputPaths().map(sc.textFile(_)))

    val clusters = rdd.map(line => {
      val items = line.split(",").map(_.trim.toInt)
      (items(0), items.slice(2,  2 + items(1)).toSeq.asJava)
    }).collectAsMap().asJava.asInstanceOf[util.Map[Integer, util.List[Integer]]]

    var idsMap: util.Map[Integer, String] = null
    if (idsFile != null){
      idsMap = sc.textFile(idsFile)
        .map(line => {
          val parts = line.split(",")
          (parts(0).trim.toInt, parts(1).trim)})
        .collectAsMap().asJava.asInstanceOf[util.Map[Integer, String]]
    }
    LOG.info("Num Clusters : {} ",  clusters.size())
    D3JsFormat.storeClusters(outPath, "Clusters 1", clusters, idsMap, 10.0f)
    LOG.info("All done")
  }
}

object D3Export {

  def main(args: Array[String]) {
    new D3Export().run(args)
  }
}
