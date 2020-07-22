import $ivy.`com.lihaoyi::mill-contrib-bloop:$MILL_VERSION`
import $ivy.`com.goyeau::mill-scalafix:c71a533`
import $ivy.`io.github.davidgregory084::mill-tpolecat:0.1.3`

import mill._
import mill.scalalib._
import mill.scalajslib._
import publish._
import mill.scalalib.scalafmt._
import com.goyeau.mill.scalafix.ScalafixModule
import io.github.davidgregory084.TpolecatModule
object Deps {
  val scala211 = "2.11.12"
  val scala212 = "2.12.10"
  val scala213 = "2.13.3"
  val scalaJS06 = "0.6.32"
  val scalaJS1 = "1.0.0"

  val scalaVersion = scala211

  val decline = ivy"com.monovore::decline:1.2.0"
  val declineEnumeratum = ivy"com.monovore::decline-enumeratum:1.2.0"

  object quill extends Dep("io.getquill::quill-jdbc", "3.4.10")
  object postgres extends Dep("org.postgresql:postgresql", "42.2.8")
  object otjPgEmbedded
      extends Dep("com.opentable.components:otj-pg-embedded", "0.13.1")

  object fastparse extends Dep("com.lihaoyi::fastparse", "2.3.0")
  object framelesss extends Dep("org.typelevel::frameless", "0.8.0") {
    object core extends Dep(sub("core"), version)
    object cats extends Dep(sub("cats"), version)
    object dataset extends Dep(sub("dataset"), version)
  }
  
  object upickle extends Dep("com.lihaoyi::upickle", "1.1.0")
  object pprint extends Dep("com.lihaoyi::pprint", "0.5.4")

  object spark extends Dep("org.apache.spark::spark", "2.3.3") {

    object sql extends Dep(sub("sql"), version)
    object jdbc extends Dep(sub("jdbc"), version)
    object streaming extends Dep(sub("streaming"), version)

  }

  object zio extends Dep("dev.zio::zio", "1.0.0-RC21") {

    object test extends Dep(sub("test"), version) {
      object sbt extends Dep(sub("sbt"), version)
    }

    object config extends Dep(sub("config"), "1.0.0-RC23-1") {
      object derivation extends Dep(sub("magnolia"), version)
      object refined extends Dep(sub("refined"), version)
      object typesafe extends Dep(sub("typesafe"), version)
    }

    object logging extends Dep(sub("logging"), "0.3.2") {
      object sl4j extends Dep(sub("slf4j"), version)
    }

  }

  abstract class Dep(val notation: String, val version: String) {
    def apply() = ivy"$notation:$version"

    def sub(subArtifact: String): String = notation + s"-$subArtifact"
  }
}

trait DataflowzScalaModule extends ScalaModule with ScalafmtModule
with ScalafixModule with TpolecatModule {

  def scalaVersion = T { Deps.scalaVersion }
  def compileIvyDeps = Agg(ivy"com.lihaoyi::acyclic:0.1.7")
//  def scalacOptions = T {
//    super.scalacOptions() ++ Agg("-P:acyclic:force")
//  }
  def scalacPluginIvyDeps = Agg(ivy"com.lihaoyi::acyclic:0.1.7")

  trait Tests extends super.Tests with DataflowzScalaTestModule
}

trait DataflowzScalaTestModule extends ScalaModule with TestModule
with ScalafmtModule with ScalafixModule with TpolecatModule {

  def ivyDeps = Agg(
    Deps.pprint(),
    Deps.zio.test(),
    Deps.zio.test.sbt()
  )

  def scalacPluginIvyDeps = Agg(ivy"com.lihaoyi::acyclic:0.1.7")

  def testFrameworks =
    Seq("zio.test.sbt.ZTestFramework")
}

object dataflowz extends Module {
  object core extends DataflowzScalaModule {

    def ivyDeps = Agg(
      Deps.pprint(),
      Deps.zio(),
      Deps.zio.config(),
      Deps.zio.config.typesafe(),
      Deps.zio.logging(),
      Deps.zio.logging.sl4j(),
      Deps.otjPgEmbedded(),
      Deps.postgres(),
      Deps.quill()
    )

    object test extends Tests
  }

  object spark extends DataflowzScalaModule {
    def ivyDeps = Agg(
      Deps.spark.sql(),
      Deps.spark.streaming(),
      Deps.pprint(),
      Deps.zio(),
      Deps.zio.config(),
      Deps.zio.config.typesafe(),
      Deps.zio.logging(),
      Deps.zio.logging.sl4j(),
      Deps.otjPgEmbedded(),
      Deps.postgres(),
      Deps.frameless.dataset(),
      Deps.quill()
    )

    def moduleDeps = Seq(core)
  }

  object samples extends DataflowzScalaModule {
    def modeuleDeps = Seq(
      core,
      spark
    )

    def ivyDeps = Agg(
      Deps.spark.sql(),
      Deps.spark.jdbc()
    )
  }
}
