updateOptions := updateOptions.value.withCachedResolution(true)

val scalaSlack = "com.github.gilbertw1" %% "slack-scala-client" % "0.1.2"
val typesafeConfig = "com.typesafe" % "config" % "1.3.0"

lazy val commonSettings = Seq(
  organization := "com.thekidder",
  version := "0.0.1",
  scalaVersion := "2.11.7"
)

lazy val root = (project in file(".")).
  settings(commonSettings: _*).
  settings(
    name := "slackers-quest",
    libraryDependencies := Seq(scalaSlack, typesafeConfig)
  )

maintainer in Docker := "Adam Kidder <thekidder@gmail.com>"
dockerUpdateLatest in Docker := true

enablePlugins(JavaServerAppPackaging)
enablePlugins(DockerPlugin)