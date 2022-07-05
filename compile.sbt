import org.clulab.sbt.BuildUtils

Compile / packageBin / mappings := {
  val packageDir = BuildUtils.pkgToDir("org.clulab.pdf2txt")

  def mkMapping(filename: String): (File, String) = {
    // package;format="packaged" results in backlashes and
    // syntax errors on Windows, so this is converted manually.
    // file(filename) -> s"$packageDir/$filename")
    file(filename) -> s"$filename"
  }

  // Remove placeholder files (.gitempty).
  val filtered = (mappings in (Compile, packageBin)).value.filter {
    case (_, name) => !name.endsWith(".gitempty")
  }

  // Put these files next to the model, in part so they don't conflict with other dependencies.
  filtered ++ Seq(
    // Do this if the files should be moved to the package directory.
    mkMapping("README.md"),
    mkMapping("CHANGES.md"),
    mkMapping("LICENSE")
  )
}

// See https://github.com/earldouglas/xsbt-web-plugin/issues/115.
Compile / packageBin := {
  //  val log = streams.value.log
  //  val log = sLog.value

  ((Compile / packageBin).map { file: File =>
    // This is inside the map because otherwise there is an error message
    // [error] java.lang.IllegalArgumentException: Could not find proxy for val compress: Boolean
    val useCompression = BuildUtils.compression

    if (useCompression)
      file
    else {
      import java.io.{FileInputStream, FileOutputStream, ByteArrayOutputStream}
      import java.util.zip.{CRC32, ZipEntry, ZipInputStream, ZipOutputStream}

      println(s" Start (re)packaging ${file.getName} with zero compression...")
      val zipInputStream = new ZipInputStream(new FileInputStream(file))
      val tmpFile = new File(file.getAbsolutePath + "_decompressed")
      val zipOutputStream = new ZipOutputStream(new FileOutputStream(tmpFile))
      zipOutputStream.setMethod(ZipOutputStream.STORED)
      Iterator
        .continually(zipInputStream.getNextEntry)
        .takeWhile(zipEntry => zipEntry != null)
        .foreach { zipEntry =>
          val byteArrayOutputStream = new ByteArrayOutputStream
          Iterator
            .continually(zipInputStream.read())
            .takeWhile(-1 !=)
            .foreach(byteArrayOutputStream.write)
          val bytes = byteArrayOutputStream.toByteArray
          zipEntry.setMethod(ZipEntry.STORED)
          zipEntry.setSize(byteArrayOutputStream.size)
          zipEntry.setCompressedSize(byteArrayOutputStream.size)
          val crc = new CRC32
          crc.update(bytes)
          zipEntry.setCrc(crc.getValue)
          zipOutputStream.putNextEntry(zipEntry)
          zipOutputStream.write(bytes)
          zipOutputStream.closeEntry
          zipInputStream.closeEntry
        }
      zipOutputStream.close
      zipInputStream.close
      if (!file.delete())
        println(s"The file ${file.getName} could not be deleted!")
      else if (!tmpFile.renameTo(file))
        println(s"The file ${file.getName} could not be renamed!")
      else
        println(s"Finish (re)packaging ${file.getName} with zero compression...")
      file
    }
  }).value
}

// This setting adversely affects logLevel during tests, so needs counterweight.
// ThisBuild / Compile / logLevel := Level.Error
// ThisBuild / Runtime / logLevel := Level.Info
// Static methods in interface require -target:jvm-1.8
ThisBuild / Compile / scalacOptions ++= Seq("-feature", "-unchecked", "-deprecation", "-target:jvm-1.8")
