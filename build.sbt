import sbt._
import Keys._
import sbt.Keys._
import sbtassembly.Plugin._
import AssemblyKeys._
import com.typesafe.sbt.SbtStartScript

name := """upd-hole-punching-scala-akka"""

version := "1.0"

scalaVersion := "2.10.4"

scalacOptions ++= Seq("-unchecked", "-deprecation","-feature")

resolvers ++= Seq(
  "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
  "mandubian maven bintray" at "http://dl.bintray.com/mandubian/maven",
  "spray" at "http://repo.spray.io/"
)

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.3.0"
)

seq(SbtStartScript.startScriptForClassesSettings: _*)

assemblySettings

test in assembly := {}