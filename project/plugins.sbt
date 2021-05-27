addSbtPlugin("ch.epfl.scala" % "sbt-scalajs-bundler" % "0.17.0")
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.9")
addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.9.0")
addSbtPlugin("com.thesamet" % "sbt-protoc" % "0.99.31")
addSbtPlugin("org.scala-js" % "sbt-scalajs" % "1.0.1")
addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "1.0.0")
addSbtPlugin("com.geirsson" % "sbt-ci-release" % "1.5.3")

libraryDependencies ++= List(
  "io.github.bonigarcia" % "webdrivermanager" % "3.6.1",
  "com.thesamet.scalapb" %% "compilerplugin-shaded" % "0.10.3",
  "org.scala-sbt" %% "scripted-plugin" % sbtVersion.value
)
