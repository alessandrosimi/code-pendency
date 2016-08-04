package code.pendency

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FeatureSpec, Matchers}
import scala.collection.JavaConversions._

@RunWith(classOf[JUnitRunner])
class TestFileManagerAndBuilder extends FeatureSpec with Matchers {

  feature("extract classes from directory with 4 classes, one in a sub folder and one abtract") {

    scenario("with inner class") {
      val file = ClassFile("code", "pendency", "manager").getAbsolutePath
      val filter = buildFilter(includeInnerClass = true)
      val fileManager = new FileManager(filter, List(file))
      val files = fileManager.extractFiles()
      files should have size 4
    }

    scenario("without inner class") {
      val file = ClassFile("code", "pendency", "manager").getAbsolutePath
      val filter = buildFilter(includeInnerClass = false)
      val fileManager = new FileManager(filter, List(file))
      val files = fileManager.extractFiles()
      files should have size 3
    }

    scenario("not a directory") {
      val file = ClassFile("code", "pendency", "manager", "SimpleClass.class").getAbsolutePath
      val fileManager = new FileManager(buildFilter(), List(file))
      val files = fileManager.extractFiles()
      files should have size 0
    }

  }

  feature("extract archives") {

    scenario("jar file") {
      val file = ClassFile("data", "test.jar").getAbsolutePath
      val fileManager = new FileManager(buildFilter(), List(file))
      val files = fileManager.extractFiles()
      files should have size 1
    }

    scenario("zip file") {
      val file = ClassFile("data", "test.zip").getAbsolutePath
      val fileManager = new FileManager(buildFilter(), List(file))
      val files = fileManager.extractFiles()
      files should have size 1
    }

  }

  feature("build java classes extracted from a package and archives") {

    scenario("with inner class") {
      val file = ClassFile("code", "pendency", "manager").getAbsolutePath
      val filter = buildFilter(includeInnerClass = true)
      val parser = buildClassFileParser
      val fileManager = new FileManager(filter, List(file))
      val builder = new JavaClassBuilder(parser, fileManager, filter)
      val javaClasses = builder.build()
      javaClasses should have size 4
    }

    scenario("without inner class") {
      val file = ClassFile("code", "pendency", "manager").getAbsolutePath
      val filter = buildFilter(includeInnerClass = false)
      val parser = buildClassFileParser
      val fileManager = new FileManager(filter,  List(file))
      val builder = new JavaClassBuilder(parser, fileManager, filter)
      val javaClasses = builder.build()
      javaClasses should have size 3
    }

    scenario("jar file with inner classes") {
      val file = ClassFile("data", "test.jar").getAbsolutePath
      val filter = buildFilter(includeInnerClass = true)
      val parser = buildClassFileParser
      val fileManager = new FileManager(filter, List(file))
      val builder = new JavaClassBuilder(parser, fileManager, filter)
      val javaClasses = builder.build()
      javaClasses should have size 5
    }

    scenario("jar file without inner classes") {
      val file = ClassFile("data", "test.jar").getAbsolutePath
      val filter = buildFilter(includeInnerClass = false)
      val parser = buildClassFileParser
      val fileManager = new FileManager(filter, List(file))
      val builder = new JavaClassBuilder(parser, fileManager, filter)
      val javaClasses = builder.build()
      javaClasses should have size 4
      javaClasses.map(_.getJarName).head shouldBe "test.jar"
    }

  }

  def buildFilter(includeInnerClass: Boolean = true): Filter =
    new Filter(includeInnerClass, List[String](), List[String]())

  def buildClassFileParser: ClassFileParser =
    new ClassFileParser(buildFilter(), List[ParserListener]())

}
