package code.pendency

import java.io._

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FeatureSpec, Matchers}

import scala.collection.JavaConverters._

@RunWith(classOf[JUnitRunner])
class TestClassFileParser extends FeatureSpec with Matchers {

  scenario("not a java class") {
    val classFile = ClassFile("data", "NotAJavaClass.txt")
    val parser = buildClassFileParser
    intercept[IOException](parser.parse(asInputStream(classFile)))
  }

  feature("parse isolated class") {

    scenario("properties") {
      val classFile = ClassFile("code", "pendency", "sample", "IsolatedClass.class")
      val parser = buildClassFileParser
      val javaClass = parser.parse(asInputStream(classFile))
      javaClass.getClassName shouldBe "code.pendency.sample.IsolatedClass"
      javaClass.getPackageName shouldBe "code.pendency.sample"
      javaClass.getSourceFile shouldBe "IsolatedClass.java"
      javaClass.isAbstract shouldBe false
      javaClass.isInterface shouldBe false
      javaClass.toString shouldBe javaClass.getClassName
      javaClass.getMinorVersion shouldBe 0
      javaClass.getMajorVersion > 0 shouldBe true
      javaClass.getJarName shouldBe "<sourcecode>"
    }

    scenario("metrics") {
      val classFile = ClassFile("code", "pendency", "sample", "IsolatedClass.class")
      val parser = buildClassFileParser
      val javaClass = parser.parse(asInputStream(classFile))
      javaClass.afferentCoupling() shouldBe 0
      javaClass.getAfferents shouldBe empty
      javaClass.efferentCoupling() shouldBe 0
      javaClass.getEfferents should have size 0
      javaClass.getEfferentIds should have size 1
      val efferent = javaClass.getEfferentIds.asScala.head
      efferent shouldBe "java.lang.Object"
    }

    scenario("attributes") {
      val classFile = ClassFile("code", "pendency", "sample", "IsolatedClass.class")
      val parser = buildClassFileParser
      val javaClass = parser.parse(asInputStream(classFile))
      javaClass.getClassName shouldBe "code.pendency.sample.IsolatedClass"
      javaClass.getPackageName shouldBe "code.pendency.sample"
      javaClass.getSourceFile shouldBe "IsolatedClass.java"
      javaClass.isAbstract shouldBe false
      javaClass.isInterface shouldBe false
      javaClass.toString shouldBe javaClass.getClassName
    }
  }

  feature("class with method and field") {

    scenario("String and Integer") {
      val classFile = ClassFile("code", "pendency", "sample", "ClassWithMethodAndField.class")
      val parser = buildClassFileParser
      val javaClass = parser.parse(asInputStream(classFile))
      javaClass.getEfferentIds should have size 3
      val efferentNames = javaClass.getEfferentIds.asScala
      efferentNames should contain ("java.lang.Object")
      efferentNames should contain ("java.lang.String")
      efferentNames should contain ("java.lang.Integer")
    }

    scenario("Primitive and Boxing") {
      val classFile = ClassFile("code", "pendency", "sample", "ClassWithPrimitivesAndBoxingType.class")
      val parser = buildClassFileParser
      val javaClass = parser.parse(asInputStream(classFile))
      javaClass.getEfferentIds should have size 5
      val efferentNames = javaClass.getEfferentIds.asScala
      efferentNames should contain ("java.lang.Object")
      efferentNames should contain ("java.lang.Integer")
      efferentNames should contain ("java.lang.Double")
      efferentNames should contain ("java.lang.Long")
      efferentNames should contain ("java.lang.Boolean")
    }

  }

  feature("annotations") {

    scenario("Annotation class") {
      val classFile = ClassFile("code", "pendency", "sample", "Annotation.class")
      val parser = buildClassFileParser
      val javaClass = parser.parse(asInputStream(classFile))
      javaClass.getEfferentIds should have size 8
      val efferentNames = javaClass.getEfferentIds
      efferentNames should contain ("java.lang.Object")
      efferentNames should contain ("java.lang.Class")
      efferentNames should contain ("java.lang.annotation.Annotation")
      efferentNames should contain ("java.lang.annotation.ElementType")
      efferentNames should contain ("java.lang.annotation.Retention")
      efferentNames should contain ("java.lang.annotation.RetentionPolicy")
      efferentNames should contain ("java.lang.annotation.Target")
      efferentNames should contain ("java.lang.annotation.Inherited")
    }

    scenario("Annotation skips primitives") {
      val classFile = ClassFile("code", "pendency", "sample", "AnnotationWithPrimitives.class")
      val parser = buildClassFileParser
      val javaClass = parser.parse(asInputStream(classFile))
      javaClass.getEfferentIds should have size 6
      val efferentNames = javaClass.getEfferentIds
      efferentNames should contain ("java.lang.Object")
      efferentNames should contain ("java.lang.annotation.Annotation")
      efferentNames should contain ("java.lang.annotation.ElementType")
      efferentNames should contain ("java.lang.annotation.Retention")
      efferentNames should contain ("java.lang.annotation.RetentionPolicy")
      efferentNames should contain ("java.lang.annotation.Target")
    }

    scenario("Annotated class") {
      val classFile = ClassFile("code", "pendency", "sample", "ClassWithAnnotation.class")
      val parser = buildClassFileParser
      val javaClass = parser.parse(asInputStream(classFile))
      javaClass.getEfferentIds should have size 3
      val efferentNames = javaClass.getEfferentIds.asScala
      efferentNames should contain ("java.lang.Object")
      efferentNames should contain ("java.lang.String")
      efferentNames should contain ("code.pendency.sample.Annotation")
    }

  }

  feature("interface") {

    scenario("properties") {
      val classFile = ClassFile("code", "pendency", "sample", "InterfaceClass.class")
      val parser = buildClassFileParser
      val javaClass = parser.parse(asInputStream(classFile))
      javaClass.getClassName shouldBe "code.pendency.sample.InterfaceClass"
      javaClass.isAbstract shouldBe false
      javaClass.isInterface shouldBe true
    }

    scenario("metrics") {
      val classFile = ClassFile("code", "pendency", "sample", "InterfaceClass.class")
      val parser = buildClassFileParser
      val javaClass = parser.parse(asInputStream(classFile))
      javaClass.getEfferentIds should have size 1
      val efferentNames = javaClass.getEfferentIds.asScala
      efferentNames should contain ("java.lang.Object")
    }

  }

  feature("class with interface") {

    scenario("properties") {
      val classFile = ClassFile("code", "pendency", "sample", "ClassWithInterface.class")
      val parser = buildClassFileParser
      val javaClass = parser.parse(asInputStream(classFile))
      javaClass.getClassName shouldBe "code.pendency.sample.ClassWithInterface"
      javaClass.isAbstract shouldBe false
    }

    scenario("metrics") {
      val classFile = ClassFile("code", "pendency", "sample", "ClassWithInterface.class")
      val parser = buildClassFileParser
      val javaClass = parser.parse(asInputStream(classFile))
      javaClass.getEfferentIds should have size 2
      val efferentNames = javaClass.getEfferentIds.asScala
      efferentNames should contain ("java.lang.Object")
      efferentNames should contain ("code.pendency.sample.InterfaceClass")
    }

  }

  feature("abstract class") {

    scenario("properties") {
      val classFile = ClassFile("code", "pendency", "sample", "AbstractClass.class")
      val parser = buildClassFileParser
      val javaClass = parser.parse(asInputStream(classFile))
      javaClass.getClassName shouldBe "code.pendency.sample.AbstractClass"
      javaClass.isAbstract shouldBe true
      javaClass.isInterface shouldBe false
    }

  }

  def buildClassFileParser: ClassFileParser = {
    val filter = new Filter(true, List[String]().asJava, List[String]().asJava)
    new ClassFileParser(filter, List[ParserListener]().asJava)
  }

  def asInputStream(file: File): InputStream =
    new BufferedInputStream(new FileInputStream(file))

}
