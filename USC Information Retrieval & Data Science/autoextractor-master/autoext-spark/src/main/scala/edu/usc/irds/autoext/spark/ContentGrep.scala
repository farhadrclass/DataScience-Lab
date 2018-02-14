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

/**
  * Greps the content for specific url sub strings and content type sub strings
  */
class ContentGrep extends IOSparkJob {

  @Option(name = "-urlfilter", usage = "Url filter substring", required = true)
  var urlFilter: String = null

  @Option(name = "-contentfilter", usage = "Content type filter substring")
  var contentFilter:String = null

  def run(): Unit ={
    println("Initializing spark context")
    val paths = getInputPaths()
    println(s"Found ${paths.length} paths")

    val rdds = new Array[RDD[(Text, Content)]](paths.length)
    for( i <- paths.indices){
      rdds(i) = sc.sequenceFile(paths(i), classOf[Text], classOf[Content])
    }
    var rdd = sc.union(rdds)
    val contentFilter = this.contentFilter
    val urlFilter = this.urlFilter
    rdd = rdd.filter(rec => ((urlFilter == null || rec._2.getUrl.contains(urlFilter))
                            && (contentFilter == null || rec._2.getContentType.contains(contentFilter))))
    LOG.info("Saving output at {}", outPath)
    rdd.saveAsHadoopFile(outPath, classOf[Text], classOf[Content], classOf[SequenceFileOutputFormat[_,_]])
    LOG.info("Done. Stopping spark context")
    sc.stop()
  }
}

object ContentGrep {

  def main(args: Array[String]) {
    new ContentGrep().run(args)
  }
}