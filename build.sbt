val _scalaVersion = "2.11.7"

val jacksons = Seq(
  "com.fasterxml.jackson.core" % "jackson-core",
  "com.fasterxml.jackson.core" % "jackson-annotations",
  "com.fasterxml.jackson.core" % "jackson-databind",
  "com.fasterxml.jackson.datatype" % "jackson-datatype-jdk8",
  "com.fasterxml.jackson.datatype" % "jackson-datatype-jsr310"
).map(_ % "2.6.0")

val jello = crossProject
  .settings(
        name := "jello",
        //scalacOptions ++= Seq("-Ymacro-debug-lite"),
        scalaVersion := _scalaVersion,
        ivyScala := ivyScala.value map { _.copy(overrideScalaVersion = true) },
        // Sonatype
//        publishArtifact in Test := false,
        testFrameworks += TestFrameworks.ScalaTest,
//        scalacOptions ++= Seq("-Ymacro-debug-lite"),
        libraryDependencies ++= Seq(
          "org.scala-lang" % "scala-reflect" % scalaVersion.value,
          "org.scalatest" %% "scalatest" % "2.2.4" % Test
        )
    ).jsSettings(
        //scalacOptions ++= Seq("-Ymacro-debug-lite"),
        libraryDependencies ++= Seq(
            "org.monifu" %%% "minitest" % "0.14" % "test"
        ),
        testFrameworks += new TestFramework("minitest.runner.Framework"),
        scalaJSStage in Test := FullOptStage
    ).jvmSettings(
        libraryDependencies ++= Seq(
            "org.scalatest" %% "scalatest" % "2.2.4" % Test
        ) ++ jacksons.map(_ % "test,provided")
    )

// configure a specific directory for scalajs output
val scalajsOutputDir = Def.settingKey[File]("directory for javascript files output by scalajs")

// make all JS builds use the output dir defined later
//lazy val js2jvmSettings = Seq(packageScalaJSLauncher, fastOptJS, fullOptJS) map { packageJSKey =>
//    crossTarget in(jelloJS, Compile, packageJSKey) := scalajsOutputDir.value
//}


lazy val jelloJS = jello.js

lazy val jelloJVM = jello.jvm


