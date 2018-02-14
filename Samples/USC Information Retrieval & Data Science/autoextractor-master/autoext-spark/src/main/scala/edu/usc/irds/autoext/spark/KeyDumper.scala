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

import org.apache.hadoop.io.Writable

/**
  * Dumps all the keys of sequence files
  */
class KeyDumper extends IOSparkJob {

  def run(): Unit ={
    sc.union(getInputPaths().map(sc.sequenceFile(_,
      classOf[Writable], classOf[Writable])))
      .map(rec => rec._1.toString) //keys only
      .saveAsTextFile(outPath) //write it to a file
    LOG.info(s"Stored the output at $outPath")
  }
}

object KeyDumper{

  def main(args: Array[String]) {
    new KeyDumper().run(args)
  }
}