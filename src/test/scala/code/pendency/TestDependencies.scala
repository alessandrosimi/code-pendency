package code.pendency

import code.pendency.dependencies.{DependOnSimpleClass, SimpleClass}
import code.pendency.dependencies.SimpleClass
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, FeatureSpec}
import scala.collection.JavaConverters._

@RunWith(classOf[JUnitRunner])
class TestDependencies extends FeatureSpec with Matchers {

  feature("analyze dependencies from package with 3 classes and 1 inner class") {

    scenario("with inner class") {
      val file = ClassFile("code", "pendency", "manager")
      val depend = Codependency
          .create()
          .includesInnerClasses()
          .withDirectory(file.getAbsolutePath)
      val analysis = depend.analyze()
      val classes = analysis.getClasses.asScala
      analysis.numberOfClasses() shouldBe 5
      classes.map(_.getClassName) should contain ("java.lang.Object")
      val packages = analysis.group(Analysis.BY_PACKAGE).asScala
      packages should have size 3
      packages.map(_.getName) should contain ("java.lang")
    }

    scenario("without inner class") {
      val file = ClassFile("code", "pendency", "manager")
      val depend = Codependency
          .create()
          .excludesInnerClasses()
          .withDirectory(file.getAbsolutePath)
      val analysis = depend.analyze()
      val classes = analysis.getClasses
      classes should have size 4
    }

  }

  feature("filter the analysis") {

    scenario("all classes") {
      val file = ClassFile("code", "pendency", "manager")
      val depend = Codependency
        .create()
        .excludes("code.pendency.manager")
        .withDirectory(file.getAbsolutePath)
      val analysis = depend.analyze()
      val classes = analysis.getClasses
      classes should have size 0
    }

    scenario("one package") {
      val file = ClassFile("code", "pendency", "manager")
      val depend = Codependency
        .create()
        .excludes("code.pendency.manager.sub")
        .withDirectory(file.getAbsolutePath)
      val analysis = depend.analyze()
      val classes = analysis.getClasses
      classes should have size 4
    }

    scenario("one class") {
      val file = ClassFile("code", "pendency", "manager")
      val depend = Codependency
        .create()
        .excludes("code.pendency.manager.SimpleClass")
        .withDirectory(file.getAbsolutePath)
      val analysis = depend.analyze()
      val classes = analysis.getClasses
      classes should have size 4
    }

    scenario("imported package") {
      val file = ClassFile("code", "pendency", "manager")
      val depend = Codependency
        .create()
        .excludes("java.lang")
        .withDirectory(file.getAbsolutePath)
      val analysis = depend.analyze()
      val packages = analysis.group(Analysis.BY_PACKAGE)
      packages should have size 2
    }

  }

  scenario("check the dependencies value between two classes") {
    val file = ClassFile("code", "pendency", "dependencies")
    val depend = Codependency
      .create()
        .excludes("java.lang")
      .withDirectory(file.getAbsolutePath)
    val analysis = depend.analyze()
    val classes = analysis.getClasses.asScala
    classes should have size 2
    val simpleClass = classes.find(_.getClassName == classOf[SimpleClass].getName)
    simpleClass shouldBe defined
    simpleClass.get.getEfferents should have size 0
    simpleClass.get.getAfferents should have size 1
    val dependOnSimpleClass = classes.find(_.getClassName == classOf[DependOnSimpleClass].getName)
    dependOnSimpleClass shouldBe defined
    dependOnSimpleClass.get.getEfferents should have size 1
    dependOnSimpleClass.get.getAfferents should have size 0
  }

  scenario("include and exclude classes") {
    val file = ClassFile("code", "pendency", "manager")
    val depend = Codependency
      .create()
      .includes("code.pendency.manager")
      .excludes("code.pendency.manager.sub")
      .withDirectory(file.getAbsolutePath)
    val analysis = depend.analyze()
    val classes = analysis.getClasses.asScala
    classes should have size 3
    val packages = analysis.group(Analysis.BY_PACKAGE).asScala
    packages should have size 1
  }

  scenario("listen the class parsing") {
    val file = ClassFile("code", "pendency", "manager", "sub")
    var listenClass: Option[JavaClass] = None
    val listener = new ParserListener {
      def onParsedJavaClass(parsedClass: JavaClass): Unit = listenClass = Some(parsedClass)
    }
    val depend = Codependency.create()
        .withListener(listener)
        .excludes("java.lang")
        .withDirectory(file.getAbsolutePath)
    val analysis = depend.analyze()
    analysis.getClasses.size shouldBe 1
    listenClass.map(_.getClassName) shouldBe Some("code.pendency.manager.sub.SubFolderClass")
  }

  scenario("check the java object into the entire classpath") {
    val depend = Codependency.create()
        .withEntireClassPath()
        .includes("java.lang.Object")
    val analysis = depend.analyze()
    val classes = analysis.getClasses.asScala
    classes should have size 1
    val javaObject = classes.head
    javaObject.getClassName shouldBe "java.lang.Object"
    javaObject.getEfferents should have size 0
  }

  feature("test the equality of a dependency") {

    scenario("same object") {
      val javaClass = new JavaClass("Class")
      javaClass.equals(javaClass) shouldBe true
    }

    scenario("null object") {
      val javaClass = new JavaClass("Class")
      javaClass.equals(null) shouldBe false
    }

    scenario("different type") {
      val javaClass = new JavaClass("Class")
      javaClass.equals("Class") shouldBe false
    }

    scenario("identical") {
      val javaClass = new JavaClass("Class")
      val identical = new JavaClass("Class")
      javaClass.equals(identical) shouldBe true
    }

  }

}
