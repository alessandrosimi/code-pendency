package code.pendency

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FeatureSpec, Matchers}

import scala.collection.JavaConverters._

@RunWith(classOf[JUnitRunner])
class TestGroupBy extends FeatureSpec with Matchers {

  feature("group by package") {

    scenario("group three packages + java lang") {
      val file = ClassFile("code", "pendency", "group")
      val depend = Codependency.create()
          .withDirectory(file.getAbsolutePath)
      val analysis = depend.analyze()
      analysis.getClasses should have size 5
      val groupBy = analysis.group(Analysis.BY_PACKAGE).asScala
      groupBy should have size 4
      groupBy.map(_.getName) should contain ("code.pendency.group")
      groupBy.map(_.getName) should contain ("code.pendency.group.sub1")
      groupBy.map(_.getName) should contain ("code.pendency.group.sub2")
      groupBy.map(_.getName) should contain ("java.lang")
      val parent = groupBy.find(_.getName == "code.pendency.group").get
      parent.getEfferents should have size 3
      parent.getAfferents should have size 0
      parent.getCount shouldBe 2
      val sub1 = groupBy.find(_.getName == "code.pendency.group.sub1").get
      sub1.getEfferents should have size 1
      sub1.getAfferents.asScala.map(_.getName) should have size 2
      sub1.getCount shouldBe 1
      val sub2 = groupBy.find(_.getName == "code.pendency.group.sub2").get
      sub2.efferentCoupling() shouldBe 2
      sub2.afferentCoupling() shouldBe 1
      sub2.getCount shouldBe 1
    }

  }

}
