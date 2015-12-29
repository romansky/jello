val _scalaVersion = "2.11.7"

val jello = crossProject.settings(
        name := "jello",
        //scalacOptions ++= Seq("-Ymacro-debug-lite"),
        libraryDependencies ++= Seq(),
        scalaVersion := _scalaVersion,
        ivyScala := ivyScala.value map { _.copy(overrideScalaVersion = true) },
        // Sonatype
        publishArtifact in Test := false
    ).jsSettings(
        //scalacOptions ++= Seq("-Ymacro-debug-lite"),
        libraryDependencies ++= Seq(
            "org.monifu" %%% "minitest" % "0.14" % "test"
        ),
        testFrameworks += new TestFramework("minitest.runner.Framework"),
        scalaJSStage in Test := FullOptStage
    ).jvmSettings(
        libraryDependencies ++= Seq(
            "org.spire-math" %% "jawn-parser" % "0.7.0",
            "org.scalatest" %% "scalatest" % "2.2.4" % "test"
        )
    )

// configure a specific directory for scalajs output
val scalajsOutputDir = Def.settingKey[File]("directory for javascript files output by scalajs")

// make all JS builds use the output dir defined later
//lazy val js2jvmSettings = Seq(packageScalaJSLauncher, fastOptJS, fullOptJS) map { packageJSKey =>
//    crossTarget in(jelloJS, Compile, packageJSKey) := scalajsOutputDir.value
//}

lazy val jelloJS = jello.js.settings()

lazy val jelloJVM = jello.jvm.settings()


