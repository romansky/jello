import sbtcrossproject.CrossPlugin.autoImport.crossProject

val _organization = "com.uniformlyrandom"

version := CommonVersions.jello
scalaVersion := CommonVersions.scala
organization := _organization

publish / skip := true

credentials += Credentials(Path.userHome / ".sbt" / ".credentials")

val jacksons = Seq(
  "com.fasterxml.jackson.core" % "jackson-core",
  "com.fasterxml.jackson.core" % "jackson-annotations",
  "com.fasterxml.jackson.core" % "jackson-databind",
  "com.fasterxml.jackson.datatype" % "jackson-datatype-jdk8",
  "com.fasterxml.jackson.datatype" % "jackson-datatype-jsr310"
).map(_ % CommonVersions.jackson)

val jello = crossProject(JSPlatform, JVMPlatform)
  .settings(
    organization := _organization,
    name := "jello",
    version := CommonVersions.jello,
    scalacOptions += "-feature",
    homepage := Some(url("https://www.yielder.io")),
    licenses := Seq(("MIT", url("https://opensource.org/licenses/mit-license.php"))),
    //    scalacOptions ++= Seq("-Ymacro-debug-lite"),
    scalaVersion := CommonVersions.scala,
    //    ivyScala := ivyScala.value map { _.copy(overrideScalaVersion = true) },
    // Sonatype
    Test / publishArtifact := false,
    publishTo := sonatypePublishToBundle.value,
    publishConfiguration := publishConfiguration.value.withOverwrite(true),
    //    sonatypeBundleDirectory := (ThisBuild / baseDirectory).value / target.value.getName / "sonatype-staging" / s"${version.value}",
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
      "org.scala-lang" % "scala-reflect" % CommonVersions.scala % Provided,
      "org.scalatest" %% "scalatest" % CommonVersions.scalaTest % Test
    )
  )
  .settings(xerial.sbt.Sonatype.sonatypeSettings: _*)
  .jsSettings(
    libraryDependencies ++= Seq(
      "io.monix" %%% "minitest" % CommonVersions.minitest % "test",
      "org.scala-lang.modules" %%% "scala-collection-compat" % CommonVersions.scalaCollectionsCompat,
      "org.scala-js" %%% "scalajs-java-time" % CommonVersions.sclaJsJavaTime
    ),
    testFrameworks += new TestFramework("minitest.runner.Framework"),
    Test / scalaJSStage := FullOptStage
  )
  .jvmSettings(
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % CommonVersions.scalaTest % Test,
      "com.typesafe.play" %% "play-json" % CommonVersions.play,
      "org.scala-lang.modules" %% "scala-collection-compat" % CommonVersions.scalaCollectionsCompat
    ) ++ jacksons.map(_ % "test,provided")
  )

lazy val jelloJS = jello.js
lazy val jelloJVM = jello.jvm
