import sbt._
import sbt.Keys._
import play.Project._

name := """Taller 1: Crawler and parsers - Análisis de información sobre Big Data"""

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  cache,
  "org.webjars"   %% "webjars-play"  % "2.2.1" exclude("org.scala-lang", "scala-library"),
  // Downgrade to JQuery 1.8.3 so that integration tests with HtmlUnit work.
  "org.webjars" % "bootstrap" % "3.1.1" exclude("org.webjars", "jquery"),
  "org.webjars" % "jquery" % "1.11.0"
)     

play.Project.playJavaSettings

resolvers ++= Seq(
  "webjars" at "http://webjars.github.com/m2"
)
