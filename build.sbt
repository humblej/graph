lazy val graph = project
  .in(file("."))
  .enablePlugins(AutomateHeaderPlugin, GitVersioning)

libraryDependencies ++= Vector(
  Library.scalaTest % "test",
  Library.json4sNative
)

initialCommands := """|import com.me.jhumble.topology._
                      |""".stripMargin
