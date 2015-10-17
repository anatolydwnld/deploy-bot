name := "GitDeploy Health Monitor"

version := "0.0.1"

scalaVersion := "2.11.7"

resolvers += "jgit-repo" at "http://download.eclipse.org/jgit/maven"

libraryDependencies += "org.eclipse.jgit" % "org.eclipse.jgit" % "[2.1,)"

libraryDependencies += "com.typesafe" % "config" % "1.3.0"

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.1.3"

libraryDependencies += "com.github.gilbertw1" %% "slack-scala-client" % "0.1.3"

scalacOptions ++= Seq("-unchecked",
  "-deprecation",
  "-feature",
  "-Xlint",
  "-Xfatal-warnings",
  "-Yrangepos",
  "-Ywarn-inaccessible",
  "-Ywarn-infer-any",
  "-Ywarn-nullary-override",
  "-Ywarn-nullary-unit",
  "-Ywarn-numeric-widen",
  "-Ywarn-unused",
  "-Ywarn-unused-import",
  "-Ywarn-value-discard")
