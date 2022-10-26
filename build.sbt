ThisBuild / organization := "fr.janalyse"
ThisBuild / name         := "data-recorder"
ThisBuild / homepage     := Some(new URL("https://github.com/dacr/data-recorder"))
ThisBuild / scalaVersion := "3.2.0"

ThisBuild / licenses += "Apache 2" -> url(s"https://www.apache.org/licenses/LICENSE-2.0.txt")

ThisBuild / scmInfo := Some(
  ScmInfo(
    url(s"https://github.com/dacr/data-recorder.git"),
    s"git@github.com:dacr/data-recorder.git"
  )
)

ThisBuild / testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")

val versions = new {
  val zio     = "2.0.2"
  val zioJson = "0.3.0"
  val laminar = "0.14.5"
  val fetch   = "0.14.4"
}

lazy val shared =
  crossProject(JSPlatform, JVMPlatform)
    .in(file("modules/shared"))
    .settings(
      libraryDependencies ++= Seq(
        "dev.zio" %%% "zio-json" % versions.zioJson
      )
    )

lazy val backend =
  project
    .in(file("modules/backend"))
    .dependsOn(shared.jvm)
    .settings(
      libraryDependencies ++= Seq(
        "dev.zio" %% "zio"                 % versions.zio,
        "dev.zio" %% "zio-json"            % versions.zioJson,
        "dev.zio" %% "zio-test"            % versions.zio % Test,
        "dev.zio" %% "zio-test-junit"      % versions.zio % Test,
        "dev.zio" %% "zio-test-sbt"        % versions.zio % Test,
        "dev.zio" %% "zio-test-scalacheck" % versions.zio % Test
      )
    )

lazy val frontend =
  project
    .in(file("modules/frontend"))
    .enablePlugins(ScalaJSPlugin)
    .dependsOn(shared.js)
    .settings(
      scalaJSLinkerConfig ~= {
        _.withModuleKind(ModuleKind.ESModule)
      },
      scalaJSLinkerConfig ~= {
        _.withSourceMap(false)
      },
      scalaJSUseMainModuleInitializer := true,
      libraryDependencies ++= Seq(
        "io.github.cquiroz" %%% "scala-java-time"           % "2.3.0",
        "io.github.cquiroz" %%% "scala-java-time-tzdb"      % "2.3.0",
        "org.scala-js"      %%% "scalajs-java-securerandom" % "1.0.0" cross CrossVersion.for3Use2_13,
        // zio
        "dev.zio"           %%% "zio"                       % versions.zio,
        "dev.zio"           %%% "zio-json"                  % versions.zioJson,
        // laminar
        "com.raquo"         %%% "laminar"                   % versions.laminar,
        "io.laminext"       %%% "fetch"                     % versions.fetch
      )
    )

lazy val root =
  project.in(file(".")).aggregate(frontend, backend, shared.js, shared.jvm)
