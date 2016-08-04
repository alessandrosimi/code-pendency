package code.pendency

import java.io.File

object ClassFile {

  private val targetTestClassDir = {
    val homeDir: String = System.getProperty("user.dir") + File.separator
    val targetDir: String = homeDir + "target" + File.separator
    targetDir + "test-classes"
  }

  def apply(paths: String*): File = {
    val builder: StringBuilder = new StringBuilder(targetTestClassDir)
    for (path <- paths) {
      builder.append(File.separator).append(path)
    }
    new File(builder.toString)
  }

}
