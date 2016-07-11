import sbt._

object Version {
  final val Scala     = "2.11.8"
  final val ScalaTest = "3.0.0-RC2"
}

object Library {
  val scalaTest = "org.scalatest" %% "scalatest" % Version.ScalaTest
  val json4sNative = "org.json4s" %% "json4s-native" % "3.4.0"
}
