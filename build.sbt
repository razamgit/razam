val JgitVersion = "5.7.0.202003110725-r"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(
    name := "rzm",
    version := "2.8.x",
    maintainer := "eugenebosyakov@gmail.com",
    scalaVersion := "2.13.1",
    libraryDependencies ++= Seq(
      guice,
      "org.scalatestplus.play"           %% "scalatestplus-play"          % "5.0.0" % Test,
      "org.eclipse.jgit"                 % "org.eclipse.jgit.http.server" % JgitVersion,
      "org.eclipse.jgit"                 % "org.eclipse.jgit.archive"     % JgitVersion,
      "commons-io"                       % "commons-io"                   % "2.6",
      "org.mindrot"                      % "jbcrypt"                      % "0.3m",
      "org.apache.tika"                  % "tika-core"                    % "1.22",
      "com.googlecode.juniversalchardet" % "juniversalchardet"            % "1.0.3",
      "net.debasishg"                    %% "redisclient"                 % "3.30",
      "org.abstractj.kalium"             % "kalium"                       % "0.8.0"
    ),
    scalacOptions ++= List("-encoding", "utf8", "-deprecation", "-feature", "-unchecked", "-Xfatal-warnings"),
    javacOptions ++= List("-Xlint:unchecked", "-Xlint:deprecation", "-Werror")
  )
