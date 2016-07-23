import sbtdocker.BuildOptions.Remove.Always

lazy val graph = project.
  in(file(".")).
  enablePlugins(AutomateHeaderPlugin, DockerPlugin).
  settings(
    name := "space",
    version := "0.1",
    exportJars := true
  )

libraryDependencies ++= Vector(
  Library.scalaTest % "test",
  Library.json4sNative
)

mainClass in Compile := Some("com.me.jhumble.space.Boot")


dockerfile in docker := {
  val jarFile: File = sbt.Keys.`package`.in(Compile, packageBin).value
  val classpath = (managedClasspath in Compile).value
  val mainclass = mainClass.in(Compile, packageBin).value.getOrElse(sys.error("Expected exactly one main class"))
  val jarTarget = s"/app/${jarFile.getName}"
  // Make a colon separated classpath with the JAR file
  val classpathString = classpath.files.map("/app/" + _.getName)
    .mkString(":") + ":" + jarTarget
  new Dockerfile {
    // Base image
    from("java")
    // Add all files on the classpath
    add(classpath.files, "/app/")
    // Add the JAR file
    add(jarFile, jarTarget)
    // On launch run Java with the classpath and the main class
    entryPoint("java", "-cp", classpathString, mainclass)
  }
}

buildOptions in docker := BuildOptions(removeIntermediateContainers = Always)

imageNames in docker := Seq(
  ImageName(s"10.210.201.187:5000/fred/${name.value}:latest"),
  ImageName(s"10.210.201.187:5000/fred/${name.value}:${(version in ThisBuild).value}")
)

initialCommands := """|import com.me.jhumble.topology._
|""".stripMargin
