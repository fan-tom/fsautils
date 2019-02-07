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
scalacOptions ++= Seq(
  "-encoding", "utf8",      // Option and arguments on same line
  "-Xfatal-warnings",       // New lines for each options
  "-deprecation",
  "-unchecked",
  "-language:implicitConversions",
  "-language:higherKinds",
  "-language:existentials",
  "-language:postfixOps",
  "-feature",
  "-target:jvm-1.8",
  "-Xfuture",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard",
  "-Ywarn-unused",
  "-Ypartial-unification",
  "-Yrangepos",             // required by SemanticDB compiler plugin
  "-Ywarn-unused-import",   // required by `RemoveUnused` rule
  "-opt:unreachable-code",  // Eliminate unreachable code, exception handlers guarding no instructions, redundant metadata (debug information, line numbers).
  "-opt:simplify-jumps",    // Simplify branching instructions, eliminate unnecessary ones.
  "-opt:compact-locals",    // Eliminate empty slots in the sequence of local variables.
  "-opt:copy-propagation", // Eliminate redundant local variables and unused values (including closures). Enables unreachable-code.
  "-opt:redundant-casts",   // Eliminate redundant casts using a type propagation analysis.
  "-opt:box-unbox",         // Eliminate box-unbox pairs within the same method (also tuples, xRefs, value class instances). Enables unreachable-code.
  "-opt:nullness-tracking", // Track nullness / non-nullness of local variables and apply optimizations.
  "-opt:closure-invocations", // Rewrite closure invocations to the implementation method.
  "-Yopt-inline-heuristics:default",
//  "-opt:inline",            // Inline method invocations according to -Yopt-inline-heuristics and -opt-inline-from.
//  "-opt-inline-from PATTERNS1,PATTERNS2",
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
