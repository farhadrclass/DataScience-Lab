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

import java.net.URL

import org.apache.hadoop.io.Text
import org.apache.hadoop.mapred.lib.MultipleSequenceFileOutputFormat
import org.apache.nutch.protocol.Content
import org.apache.spark.rdd.RDD

/**
  * This tool partitions data based on host name and content type
  */
class ContentPartitioner extends IOSparkJob {

  def run(): Unit ={
    val paths = getInputPaths()
    var rdd: RDD[(Text, Content)] = sc.sequenceFile(paths(0), classOf[Text], classOf[Content])
    for (i <- 1 to paths.length - 1){
      rdd = sc.union(rdd, sc.sequenceFile(paths(i), classOf[Text], classOf[Content]))
    }
    rdd.map({case (k,v) =>
      val newK = new URL(k.toString).getHost + "/" + v.getContentType.replaceAll("[^a-zA-Z]", "").toLowerCase
      (new Text(newK), v)}) // key is host name + content type
      .saveAsHadoopFile(outPath, classOf[Text], classOf[Content],
        classOf[SplitOutputFormat])
  }
}

/**
  * Splits output based on key name and content type
  */
class SplitOutputFormat extends MultipleSequenceFileOutputFormat[Text, Content]{
  override def generateActualKey(key: Text, value: Content): Text = new Text(value.getUrl)

  override def generateFileNameForKeyValue(key: Text, value: Content, name: String): String =
    key.toString + "/" + name
}

object ContentPartitioner{
  def main(args: Array[String]) {
    new ContentPartitioner().run(args)
  }
}
