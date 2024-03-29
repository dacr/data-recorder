ThisBuild / organization := "fr.janalyse"
ThisBuild / name         := "data-recorder"
ThisBuild / homepage     := Some(new URL("https://github.com/dacr/data-recorder"))
ThisBuild / scalaVersion := "3.2.1"

ThisBuild / licenses += "Apache 2" -> url(s"https://www.apache.org/licenses/LICENSE-2.0.txt")

ThisBuild / scmInfo := Some(
  ScmInfo(
    url(s"https://github.com/dacr/data-recorder.git"),
    s"git@github.com:dacr/data-recorder.git"
  )
)

ThisBuild / testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")

// WARNING TAKE CARE OF GLOBAL COHERENCY IN PARTICULAR WITH SCALA.JS WITH ZIO SUB-DEPENDENCIES
// TO AVOID SUCH ERRORS : Referring to non-existent method zio.VersionSpecific$$anon$1.apply(java.lang.Object)java.lang.Object
// https://www.scala-js.org/doc/project/linking-errors.html
val versions = new {
  val zio        = "2.0.6"
  val zioJson    = "0.4.2"
  val ziologging = "2.1.8"
  val logback    = "1.4.5"

  val tapir       = "1.2.6"
  val sttp        = "1.3.12"
  val sttpClient3 = "3.8.8"
  val http4s      = "0.23.13"

  val laminar = "0.14.5"
  val fetch   = "0.14.4"

  // val sapui5 = ""
}

lazy val backend =
  project
    .in(file("modules/backend"))
    .dependsOn(sharedDomain.jvm)
    .settings(
      libraryDependencies ++= Seq(
        "dev.zio"                       %% "zio"                     % versions.zio,
        "dev.zio"                       %% "zio-streams"             % versions.zio,
        "dev.zio"                       %% "zio-json"                % versions.zioJson,
        "dev.zio"                       %% "zio-logging"             % versions.ziologging,
        "dev.zio"                       %% "zio-logging-slf4j"       % versions.ziologging,
        "com.softwaremill.sttp.tapir"   %% "tapir-zio"               % versions.tapir,
        "com.softwaremill.sttp.tapir"   %% "tapir-json-zio"          % versions.tapir,
        "com.softwaremill.sttp.tapir"   %% "tapir-http4s-server-zio" % versions.tapir,
        "com.softwaremill.sttp.tapir"   %% "tapir-swagger-ui-bundle" % versions.tapir,
        "com.softwaremill.sttp.tapir"   %% "tapir-asyncapi-docs"     % versions.tapir,
        "com.softwaremill.sttp.apispec" %% "asyncapi-circe-yaml"     % "0.3.2",
        "org.http4s"                    %% "http4s-blaze-server"     % versions.http4s, // ideally in sync with http4s from tapir-http4s-server-zio dependencies
        "ch.qos.logback"                 % "logback-classic"         % versions.logback,
        "dev.zio"                       %% "zio-test"                % versions.zio % Test,
        "dev.zio"                       %% "zio-test-junit"          % versions.zio % Test,
        "dev.zio"                       %% "zio-test-sbt"            % versions.zio % Test,
        "dev.zio"                       %% "zio-test-scalacheck"     % versions.zio % Test
      )
    )

lazy val frontend =
  project
    .in(file("modules/frontend"))
    .enablePlugins(ScalaJSPlugin)
    // .enablePlugins(ScalablyTypedConverterPlugin)
    .dependsOn(sharedDomain.js)
    .settings(
      scalaJSLinkerConfig ~= {
        _.withModuleKind(ModuleKind.ESModule)
          .withModuleSplitStyle(org.scalajs.linker.interface.ModuleSplitStyle.SmallModulesFor(List("frontend", "shared")))
      },
      scalaJSLinkerConfig ~= {
        _.withSourceMap(false)
      },
      scalaJSUseMainModuleInitializer := true,
      libraryDependencies ++= Seq(
        "io.github.cquiroz"             %%% "scala-java-time"           % "2.5.0",
        "io.github.cquiroz"             %%% "scala-java-time-tzdb"      % "2.5.0",
        "org.scala-js"                  %%% "scalajs-java-securerandom" % "1.0.0" cross CrossVersion.for3Use2_13,
        // zio
        "dev.zio"                       %%% "zio"                       % versions.zio,
        "dev.zio"                       %%% "zio-streams"               % versions.zio,
        "dev.zio"                       %%% "zio-json"                  % versions.zioJson,
        // sttp
        "com.softwaremill.sttp.client3" %%% "core"                       % versions.sttpClient3,
        "com.softwaremill.sttp.tapir"   %%% "tapir-sttp-client"         % versions.tapir,
        "com.softwaremill.sttp.tapir"   %%% "tapir-json-zio"            % versions.tapir,
        "com.softwaremill.sttp.client3" %%% "zio"                       % versions.sttpClient3,
        // laminar
        "com.raquo"                     %%% "laminar"                   % versions.laminar,
        "io.laminext"                   %%% "fetch"                     % versions.fetch
      )
    )

lazy val sharedDomain =
  crossProject(JSPlatform, JVMPlatform)
    .in(file("modules/common"))
    .settings(
      libraryDependencies ++= Seq(
        "dev.zio"                      %%% "zio-json"          % versions.zioJson,
        "com.softwaremill.sttp.tapir"  %%% "tapir-sttp-client" % versions.tapir,
        "com.softwaremill.sttp.tapir"  %%% "tapir-json-zio"    % versions.tapir,
        "com.softwaremill.sttp.shared" %%% "zio"               % versions.sttp
      )
    )
//    .jvmSettings(
//      libraryDependencies ++= Seq(
//        "com.softwaremill.sttp.tapir" %%% "tapir-json-zio" % versions.tapir
//      )
//    )
//    .jsSettings(
//      libraryDependencies ++= Seq(
//        "com.softwaremill.sttp.tapir" %%% "tapir-json-zio" % versions.tapir
//      )
//    )

//lazy val root =
//  project.in(file(".")).aggregate(frontend, backend, shared.js, shared.jvm)
