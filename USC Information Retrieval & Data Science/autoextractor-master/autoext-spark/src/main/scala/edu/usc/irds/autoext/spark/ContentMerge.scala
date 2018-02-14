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

import org.apache.hadoop.io.Text
import org.apache.hadoop.mapred.SequenceFileOutputFormat
import org.apache.nutch.protocol.Content
import org.apache.spark.rdd.RDD
import org.kohsuke.args4j.Option
import org.slf4j.LoggerFactory

/**
  * Merges sequence parts into one sequence file with configurable number of parts
  */
class ContentMerge extends IOSparkJob {

  @Option(name = "-numparts", usage = "Number of parts in the output. Ex: 1, 2, 3.... Optional => default")
  var partitions:Integer = null

  def run(): Unit = {

    val paths = getInputPaths()
    LOG.info(s"Found ${paths.length} input paths")
    val rdds = new Array[RDD[(Text, Content)]](paths.length)
    for( i <- paths.indices){
      rdds(i) = sc.sequenceFile(paths(i), classOf[Text], classOf[Content])
    }
    var rdd = sc.union(rdds) // club all parts
    if (partitions != null) {
      rdd = rdd.coalesce(partitions)
    }
    rdd.saveAsHadoopFile(outPath, classOf[Text], classOf[Content],
      classOf[SequenceFileOutputFormat[_,_]]) // save it
    LOG.info(s"Done. Saved output at $outPath")
  }
}

object ContentMerge {
  val LOG = LoggerFactory.getLogger(DeDuplicator.getClass)
  def main(args: Array[String]) {
    new ContentMerge().run(args)
  }
}
