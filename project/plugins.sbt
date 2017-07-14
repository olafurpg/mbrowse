resolvers += Resolver.bintrayIvyRepo("scalameta", "maven")
addSbtPlugin("org.scala-js" % "sbt-scalajs" % "0.6.18")
addSbtPlugin("org.scalameta" % "sbt-scalahost" % "1.9.0-1035-1bd51115")
addSbtPlugin("ch.epfl.scala" % "sbt-scalajs-bundler" % "0.7.0")
addSbtPlugin("io.get-coursier" % "sbt-coursier" % "1.0.0-RC6")
addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.7.0")
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.5")
addSbtPlugin(
  "com.thesamet" % "sbt-protoc" % "0.99.6" exclude ("com.trueaccord.scalapb", "protoc-bridge_2.10")
)
libraryDependencies += "com.trueaccord.scalapb" %% "compilerplugin-shaded" % "0.6.0-pre5"
resolvers += Resolver.bintrayIvyRepo("scalameta", "sbt-plugins")
