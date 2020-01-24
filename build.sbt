import sbtcrossproject.CrossPlugin.autoImport.crossProject

val _scalaVersion = "2.12.10"
val _organization = "com.uniformlyrandom"
val _playVersion = "2.8.0-M7"

scalaVersion := _scalaVersion
organization := _organization

skip in publish := true

val jacksons = Seq(
  "com.fasterxml.jackson.core" % "jackson-core",
  "com.fasterxml.jackson.core" % "jackson-annotations",
  "com.fasterxml.jackson.core" % "jackson-databind",
  "com.fasterxml.jackson.datatype" % "jackson-datatype-jdk8",
  "com.fasterxml.jackson.datatype" % "jackson-datatype-jsr310"
).map(_ % "2.8.10")

val jello = crossProject(JSPlatform, JVMPlatform)
  .settings(
    organization := _organization,
    name := "jello",
    version := "0.6.1",
    scalacOptions += "-feature",
    homepage := Some(url("http://www.uniformlyrandom.com")),
    licenses := Seq(("MIT", url("http://opensource.org/licenses/mit-license.php"))),
    //    scalacOptions ++= Seq("-Ymacro-debug-lite"),
    scalaVersion := _scalaVersion,
    //    ivyScala := ivyScala.value map { _.copy(overrideScalaVersion = true) },
    // Sonatype
    publishArtifact in Test := false,
    publishTo := sonatypePublishTo.value,
    publishConfiguration := publishConfiguration.value.withOverwrite(true),
    pomExtra :=
      <scm>
        <url>git@github.com:uniformlyrandom/jello.git</url>
        <connection>scm:git:git@github.com:uniformlyrandom/jello.git</connection>
      </scm>
        <developers>
          <developer>
            <id>romansky</id>
            <name>Roman Landenband</name>
            <url>http://www.uniformlyrandom.com</url>
          </developer>
        </developers>,
    // publish Github sources
    testFrameworks += TestFrameworks.ScalaTest,
    libraryDependencies ++= Seq(
      "org.scala-lang" % "scala-reflect" % scalaVersion.value,
      "org.scalatest" %% "scalatest" % "3.2.0-M2" % Test
    )
  )
  .settings(xerial.sbt.Sonatype.sonatypeSettings: _*)
  .jsSettings(
    emitSourceMaps := true,
    libraryDependencies ++= Seq(
      "io.monix" %%% "minitest" % "2.7.0" % "test",
      "org.scala-lang.modules" %%% "scala-collection-compat" % "2.1.2",
      "org.scala-js" %%% "scalajs-java-time" % "0.2.6"
    ),
    testFrameworks += new TestFramework("minitest.runner.Framework"),
    scalaJSStage in Test := FullOptStage,
    scalacOptions ++= (if (isSnapshot.value) Seq.empty
    else
      Seq({
        val a = baseDirectory.value.toURI.toString
          .replaceFirst("[^/]+/?$", "")
        val g =
          "https://raw.githubusercontent.com/japgolly/scalajs-react"
        s"-P:scalajs:mapSourceURI:$a->$g/v${version.value}/"
      }))
  )
  .jvmSettings(
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "3.2.0-M2" % Test,
      "com.typesafe.play" %% "play-json" % _playVersion,
      "org.scala-lang.modules" %% "scala-collection-compat" % "2.1.2"
    ) ++ jacksons.map(_ % "test,provided")
  )

lazy val jelloJS = jello.js
lazy val jelloJVM = jello.jvm
