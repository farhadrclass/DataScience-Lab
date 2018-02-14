package edu.usc.irds.autoext.hdfs

import java.io.File
import java.nio.file
import java.nio.file.Paths
import java.util.concurrent.atomic.AtomicInteger

import edu.usc.irds.autoext.hdfs.RawToSeq.LOG
import edu.usc.irds.autoext.spark.CliTool
import edu.usc.irds.autoext.utils.Timer
import org.apache.commons.io.FileUtils
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.hadoop.io.{SequenceFile, Text}
import org.apache.nutch.metadata.Metadata
import org.apache.nutch.protocol.Content
import org.kohsuke.args4j.Option
import org.slf4j.LoggerFactory

/**
  * This tool creates sequence file from Raw HTML files
  */
class RawToSeq extends CliTool {

  @Option(name = "-in", required = true, usage = "path to directory having html pages")
  var in: String = null

  @Option(name = "-out", required = true, usage = "path to output Sequence File")
  var output: String = null

  def run(): Unit ={
    val config = new Configuration()
    val fs = FileSystem.get(config)
    val inDir = new File(in)

    val files = FileUtils.listFiles(inDir, null, true).iterator()
    val outPath = new Path(output)
    //SequenceFile.createWriter(fs, config, outPath, , classOf[Content], )
    val writer = SequenceFile.createWriter(config, SequenceFile.Writer.keyClass(classOf[Text]),
      SequenceFile.Writer.valueClass(classOf[Content]), SequenceFile.Writer.file(outPath))

    val timer = new Timer
    val delay = 2000
    val count = new AtomicInteger()
    while (files.hasNext) {
      val nextFile = files.next()
      if (nextFile.isDirectory || nextFile.getName.startsWith(".")){
        //that's fine, skip it
      } else if (nextFile.isFile) {
        val id = nextFile.getPath
        val allBytes: Array[Byte] = file.Files.readAllBytes(Paths.get(nextFile.getAbsolutePath))
        val content = new Content(id, id, allBytes, "text/html", new Metadata(), config)
        writer.append(new Text(id), content)
        count.incrementAndGet()
      } else {
        LOG.warn(s"Skip : $nextFile" )
      }
      if (timer.read() >= delay ){
        LOG.info(s"Count = $count, Last=$nextFile")
        timer.reset()
      }
    }
    writer.close()
    LOG.info(s"Done.. $count")
  }
}

object RawToSeq {

  val LOG = LoggerFactory.getLogger(RawToSeq.getClass)
  def main(args: Array[String]) {
    val i = new RawToSeq()
    i.parseArgs(args)
    i.run()
  }
}
