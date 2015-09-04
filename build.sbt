val scalaSlack = "com.github.gilbertw1" %% "slack-scala-client" % "0.1.2"
val typesafeConfig = "com.typesafe" % "config" % "1.3.0"
val pickling = "org.scala-lang.modules" %% "scala-pickling" % "0.10.1"

lazy val commonSettings = Seq(
  organization := "com.thekidder",
  version := "0.0.1",
  scalaVersion := "2.11.7"
)

lazy val root = (project in file(".")).
  settings(commonSettings: _*).
  settings(
    name := "slackers-quest",
    libraryDependencies := Seq(scalaSlack, typesafeConfig, pickling)
  ).
  enablePlugins(DockerPlugin).
  enablePlugins(JavaServerAppPackaging)

maintainer in Docker := "Adam Kidder <thekidder@gmail.com>"

dockerUpdateLatest in Docker := true

dockerExposedVolumes in Docker := Seq("/data")
