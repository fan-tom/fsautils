enablePlugins(JavaServerAppPackaging)
enablePlugins(GraalVMNativeImagePlugin)
addCompilerPlugin(scalafixSemanticdb)

libraryDependencies ++= Seq()

lazy val root = (project in file("."))
  .configs(IntegrationTest)
  .settings(
    Defaults.itSettings
    , inConfig(IntegrationTest)(org.scalafmt.sbt.ScalafmtPlugin.scalafmtConfigSettings)
    , inConfig(IntegrationTest)(scalafixConfigSettings(IntegrationTest))
    , inThisBuild(List(
      organization := "de.dominicscheurer.fsautils",
      scalaVersion := "2.12.8",
      crossScalaVersions := Seq("2.12.7", "2.12.8", "2.13.0-M5"),
      version      := "0.1.0-SNAPSHOT"
    ))
    , name := "FSAUtils"
    , libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % Test
    , libraryDependencies += "org.scala-lang.modules" %% "scala-xml" % "1.1.1"
//    , libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2"
//    , libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"
  )

logBuffered in Test := false
logBuffered := false
//mainClass in Compile := Some("com.example.Main")
scalacOptions ++= Seq("-deprecation", "-feature", "-language:existentials", "-target:jvm-1.8", "-opt:_",
    "-Yrangepos",          // required by SemanticDB compiler plugin
    "-Ywarn-unused-import" // required by `RemoveUnused` rule
)
javacOptions ++= Seq("-source", "1.8", "-target", "1.8")
fork := true

addCommandAlias(
  "fix",
  "all compile:scalafix test:scalafix"
)
addCommandAlias(
  "fixCheck",
  "; compile:scalafix --check ; test:scalafix --check"
)
