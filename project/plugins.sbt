addSbtPlugin("com.github.sbt"    % "sbt-release"         % "1.1.0")
addSbtPlugin("com.github.sbt"    % "sbt-pgp"             % "2.1.2")
addSbtPlugin("org.xerial.sbt"    % "sbt-sonatype"        % "3.9.13")
addSbtPlugin("com.timushev.sbt"  % "sbt-updates"         % "0.6.3")
//addSbtPlugin("com.github.sbt"    % "sbt-native-packager" % "1.9.11")
addSbtPlugin("com.github.sbt"    % "sbt-native-packager" % "1.9.9")

addSbtPlugin("ch.epfl.scala"      % "sbt-scalafix"             % "0.10.4")
addSbtPlugin("org.scala-js"       % "sbt-scalajs"              % "1.11.0")
addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "1.2.0")

addSbtPlugin("io.spray"           % "sbt-revolver"             % "0.9.1") // for the sbt reStart feature