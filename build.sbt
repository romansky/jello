val _scalaVersion = "2.11.7"

crossScalaVersions := Seq(_scalaVersion)
scalaVersion := _scalaVersion
organization := "com.uniformlyrandom"

val jacksons = Seq(
  "com.fasterxml.jackson.core" % "jackson-core",
  "com.fasterxml.jackson.core" % "jackson-annotations",
  "com.fasterxml.jackson.core" % "jackson-databind",
  "com.fasterxml.jackson.datatype" % "jackson-datatype-jdk8",
  "com.fasterxml.jackson.datatype" % "jackson-datatype-jsr310"
).map(_ % "2.6.0")


val jello = crossProject
  .settings(
        organization := "com.uniformlyrandom",
        name := "jello",
        version := "0.2.0",
        crossVersion := CrossVersion.Disabled,
        crossScalaVersions := Seq(_scalaVersion),
        homepage := Some(url("http://www.uniformlyrandom.com")),
        licenses := Seq(("MIT", url("http://opensource.org/licenses/mit-license.php"))),
        //scalacOptions ++= Seq("-Ymacro-debug-lite"),
        scalaVersion := _scalaVersion,
        ivyScala := ivyScala.value map { _.copy(overrideScalaVersion = true) },
        // Sonatype
        publishArtifact in Test := false,
        publishTo <<= version { (v: String) =>
          val nexus = "https://oss.sonatype.org/"
          if (v.trim.endsWith("SNAPSHOT"))
            Some("snapshots" at nexus + "content/repositories/snapshots")
          else
            Some("releases"  at nexus + "service/local/staging/deploy/maven2")
        },
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
          "org.scalatest" %% "scalatest" % "2.2.4" % Test
        )
    )
  .settings(xerial.sbt.Sonatype.sonatypeSettings:_*)
  .jsSettings(
      //scalacOptions ++= Seq("-Ymacro-debug-lite"),
      libraryDependencies ++= Seq(
          "org.monifu" %%% "minitest" % "0.14" % "test"
      ),
      testFrameworks += new TestFramework("minitest.runner.Framework"),
      scalaJSStage in Test := FullOptStage,
      scalacOptions ++= (if (isSnapshot.value) Seq.empty else Seq({
        val a = baseDirectory.value.toURI.toString.replaceFirst("[^/]+/?$", "")
        val g = "https://raw.githubusercontent.com/japgolly/scalajs-react"
        s"-P:scalajs:mapSourceURI:$a->$g/v${version.value}/"
      }))
  ).jvmSettings(
      libraryDependencies ++= Seq(
          "org.scalatest" %% "scalatest" % "2.2.4" % Test
      ) ++ jacksons.map(_ % "test,provided")
  )

import com.typesafe.sbt.pgp.PgpKeys._

lazy val preventPublication = Seq[Def.Setting[_]](
    publish :=(),
    publishLocal :=(),
    publishSigned :=(),
    publishLocalSigned :=(),
    publishArtifact := false,
    publishTo := Some(Resolver.file("Unused transient repository", target.value / "fakepublish")),
    packagedArtifacts := Map.empty
  )


preventPublication

// configure a specific directory for scalajs output
//val scalajsOutputDir = Def.settingKey[File]("directory for javascript files output by scalajs")

// make all JS builds use the output dir defined later
//lazy val js2jvmSettings = Seq(packageScalaJSLauncher, fastOptJS, fullOptJS) map { packageJSKey =>
//    crossTarget in(jelloJS, Compile, packageJSKey) := scalajsOutputDir.value
//}


lazy val jelloJS = jello.js

lazy val jelloJVM = jello.jvm

