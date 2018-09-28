# Contributing

Tickets and patches are very welcome!

Mbrowse follows [scalameta's contribution guidelines]. Please read them for
information about how to create good bug reports and submit pull requests.

For ideas of how to contribute, take a look at the list of tickets with the
[<kbd>help wanted</kbd> label][help-wanted].

 [help-wanted]: https://github.com/scalameta/mbrowse/issues?q=is%3Aissue+is%3Aopen+label%3A%22help+wanted%22
 [scalameta's contribution guidelines]: https://github.com/scalameta/scalameta/blob/master/CONTRIBUTING.md

Remember to run

```
./bin/scalafmt
```

Before raising a PR to ensure the code is formatted correctly (otherwise it will fail the checks)

## Running locally

To run mbrowse on corpus locally,

```
git clone https://github.com/scalameta/mbrowse.git
cd mbrowse
git submodule init
git submodule update

npm install -g yarn

sbt
  > mbrowse-site                        # generate static site under target/mbrowse.
  > js/fastOptJS::startWebpackDevServer # spin up local file server that listens for changes.
  > ~js/fastOptJS                       # compiles Scala.js application, browser refreshes on edit.
open http://localhost:8080
```

## Packaging CLI Locally

```
sbt cli/assembly
java -jar ./mbrowse-cli/target/scala-2.12/mbrowse.jar ...
```

## Upgrading the Monaco Editor

Mbrowse interfaces with the Monaco Editor using a Scala.js facade based on the
`monaco.d.ts` TypeScript type definition file provided as part of the
`monaco-editor` NPM package. The facade can be generated with
[scala-js-ts-importer], however, manual merging is necessary, since the facade
contains several custom tweaks.

The following instructions give a rough idea how to upgrade the monaco editor
facades:

 - Update the Monaco Editor version in `build.sbt`
   ```scala
   npmDependencies in Compile ++= Seq(
     "monaco-editor" -> "x.y.z",
     // ...
   )
   ```
 - Update the NPM packages after cleaning to force download of the new version
   ```
   $ sbt -batch clean js/compile:npmUpdate
   ```
 - Run script to update the `Monaco.scala` file
   ```
   $ bin/update-monaco-facade.sh
   ```
   This might require tweaking the curated list of edits (the `sed` command in
   the script.
 - Fix the `package importedjs {}` code inserted by [scala-js-ts-importer] by
   manually editing `Monaco.scala`
 - Reformat `Monaco.scala`
   ```
   $ bin/scalafmt mbrowse-js/src/main/scala/monaco/Monaco.scala
   ```

At this point merge the changes to `Monaco.scala` using `git add -i` or some
other tool, like `tig`. Stage any newly introduced types or methods and revert
changes that remove tweaks, such as use of `override` in front of `clone` and
`toString` methods.

 [scala-js-ts-importer]: https://github.com/sjrd/scala-js-ts-importer
