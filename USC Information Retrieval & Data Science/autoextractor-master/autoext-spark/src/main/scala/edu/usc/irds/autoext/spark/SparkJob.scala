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
import org.apache.nutch.protocol.Content
import org.apache.spark.{SparkConf, SparkContext}
import org.kohsuke.args4j.Option
import org.slf4j.LoggerFactory

/**
  * Base class for all spark jobs
  */
trait SparkJob extends CliTool {

  val LOG = LoggerFactory.getLogger(getClass)

  @Option(name = "-master", aliases = Array("--master"),
  usage = "Spark master. This is not required when job is started with spark-submit")
  var sparkMaster: String = null

  @Option(name = "-app", aliases= Array("--app-name"),
  usage = "Name for spark context.")
  var appName: String = getClass.getSimpleName

  var sc: SparkContext = null

  /**
    * initializes spark context if not already initialized
    */
  def initSpark(): Unit ={
    if (sc == null) {
      LOG.info("Initializing Spark Context ")
      val conf = new SparkConf().setAppName(appName)
        .registerKryoClasses(Array(classOf[Text], classOf[Content]))
      if (sparkMaster != null) {
        LOG.info("Spark Master {}", sparkMaster)
        conf.setMaster(sparkMaster)
      }
      sc = new SparkContext(conf)
    }
  }

  def stopSpark(): Unit ={
    if (sc != null){
      LOG.info("Stopping spark.")
      sc.stop()
    }
  }

  /**
    * Abstract method which has actual job description
    */
  def run()

  def run(args:Array[String]): Unit ={
    parseArgs(args)
    initSpark()
    run()
    stopSpark()
  }
}
