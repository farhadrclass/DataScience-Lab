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

import org.apache.spark.mllib.linalg.distributed.MatrixEntry
import org.kohsuke.args4j.Option

/**
  * Combines two similarities on a linear scale with given weight value
  */
class SimilarityCombiner extends SparkJob {

  @Option(name = "-in1", required = true, usage = "Path to similarity Matrix 1 (Expected : saved MatrixEntry RDD).")
  var in1Path: String = null

  @Option(name = "-in2", required = true, usage = "Path to Similarity Matrix 2 (Expected : saved MatrixEntry RDD)")
  var in2Path: String = null

  @Option(name = "-out", required = true, usage = "Path to output file/folder where the result similarity matrix shall be stored.")
  var outPath: String = null

  @Option(name = "-weight", required = true,
    usage = "Weight/Scale for combining the similarities. The expected is [0.0, 1.0]. " +
    "The combining step is \n out = in1 * weight + (1.0 - weight) * in2")
  var weight: Double = -1.0

  /**
    * method which has actual job description
    */
  override def run(): Unit ={
      val weight = this.weight //local variable
      if (weight < 0 || weight > 1){
        throw new IllegalArgumentException(s"Weight $weight is out of bound. expected in range [0.0, 1.0]")
      }
      LOG.info(s"Combining $in1Path with $in2Path with scale $weight")
      val first = sc.objectFile[MatrixEntry](in1Path).map(e => ((e.i, e.j), e.value))
      val second = sc.objectFile[MatrixEntry](in2Path).map(e => ((e.i, e.j), e.value))

      val result = first.join(second).map({case ((i,j),(v1, v2)) => MatrixEntry(i, j, weight * v1 + (1 - weight) * v2)})
      result.saveAsObjectFile(outPath)
      LOG.info(s"Saved output at $outPath")
  }
}

object SimilarityCombiner{
  def main(args: Array[String]) {
    new SimilarityCombiner().run(args)
  }
}


