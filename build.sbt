import java.util.jar.Attributes

val githubRepo = "scalajs-jsdocgen"
val osgiVersion = "5.0.0"

lazy val commonSettings = Seq(
  organization := "com.github.maprohu",
  version := "0.2.8",
  publishMavenStyle := true,
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some(sbtglobal.SbtGlobals.devops)
//      Some("releases"  at nexus + "service/local/staging/deploy/maven2")
  },
  credentials += sbtglobal.SbtGlobals.devopsCredentials,
  pomIncludeRepository := { _ => false },
  licenses := Seq("BSD-style" -> url("http://www.opensource.org/licenses/bsd-license.php")),
  homepage := Some(url(s"https://github.com/maprohu/${githubRepo}")),
  pomExtra := (
    <scm>
      <url>git@github.com:maprohu/{githubRepo}.git</url>
      <connection>scm:git:git@github.com:maprohu/{githubRepo}.git</connection>
    </scm>
      <developers>
        <developer>
          <id>maprohu</id>
          <name>maprohu</name>
          <url>https://github.com/maprohu</url>
        </developer>
      </developers>
    ),

  crossPaths := false,
  scalaVersion := "2.11.7",
  OsgiKeys.additionalHeaders ++= Map(
    "-noee" -> "true",
    Attributes.Name.IMPLEMENTATION_VERSION.toString -> version.value
  ),
  publishArtifact in packageDoc := false,
  OsgiKeys.exportPackage := Seq(name.value.replaceAll("-", ".")),
  OsgiKeys.privatePackage := OsgiKeys.exportPackage.value.map(_ + ".impl"),
  OsgiKeys.bundleActivator := Some(OsgiKeys.privatePackage.value(0) + ".Activator"),
  libraryDependencies ++= Seq(
    "org.osgi" % "org.osgi.core" % osgiVersion % Provided
  )
)

lazy val root = (project in file("."))
  .aggregate(scalarxJVM)
  .settings(
    publishArtifact := false,
    publishTo := Some(Resolver.file("Unused transient repository", file("target/unusedrepo")))
  )

val scalarx = crossProject.enablePlugins(SbtOsgi).settings(
    name := "scalarx",
    libraryDependencies ++= Seq(
      "com.lihaoyi" %%% "utest" % "0.3.1" % "test",
      "com.lihaoyi" %% "acyclic" % "0.1.2" % "provided"
    ),
    addCompilerPlugin("com.lihaoyi" %% "acyclic" % "0.1.2"),
    testFrameworks += new TestFramework("utest.runner.Framework"),
    autoCompilerPlugins := true
  )
  .jvmSettings(
    commonSettings:_*
  )
  .jvmSettings(
    osgiSettings:_*
  )
  .jvmSettings(
    OsgiKeys.bundleActivator := None,
    OsgiKeys.privatePackage := Seq(),
    OsgiKeys.exportPackage := Seq(
      "rx.*"
    ),
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor" % "2.3.2" % "provided"
    )
  )

lazy val scalarxJVM = scalarx.jvm
