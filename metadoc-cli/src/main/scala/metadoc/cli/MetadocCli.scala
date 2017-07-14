package metadoc.cli

import java.io.File
import java.net.URLEncoder
import java.nio.file.Files
import java.nio.file.StandardOpenOption
import java.util.Base64
import scala.collection.mutable
import scala.meta._
import scala.meta.internal.io.FileIO
import scala.meta.internal.io.PathIO
import caseapp.{Name => _, _}
import metadoc.{schema => d}
import better.files._
import scala.meta.internal.trees.XtensionTreesName

@AppName("metadoc")
@AppVersion("0.1.0-SNAPSHOT")
@ProgName("metadoc")
case class MetadocOptions(
    @HelpMessage("The output directory to generate the metadoc site.")
    target: Option[String] = None,
    @HelpMessage(
      "Clean the target directory before generating new site. " +
        "All files will be deleted so be careful."
    )
    cleanTargetFirst: Boolean = false
)

case class MetadocSite(
    semanticdb: Seq[AbsolutePath],
    symbols: Seq[d.Symbol],
    index: d.Index
)

object MetadocCli extends CaseApp[MetadocOptions] {
  def filename(input: Input): String = input match {
    case Input.VirtualFile(path, _) => path
    case Input.File(path, _) =>
      path.toRelative(PathIO.workingDirectory).toString()
  }

  def metadocPosition(position: Position): d.Position =
    d.Position(
      filename(position.input),
      start = position.start,
      end = position.end
    )

  def getAbsolutePath(path: String): AbsolutePath =
    if (PathIO.isAbsolutePath(path)) AbsolutePath(path)
    else PathIO.workingDirectory.resolve(path)

  def getSymbols(implicit db: Database): Seq[d.Symbol] = {
    val symbols = mutable.Map
      .empty[String, d.Symbol]
      .withDefault(sym => d.Symbol(symbol = sym))

    def add(name: Name): Unit = db.names.get(name.pos).foreach { symbol =>
      // add globally relevant symbols to index.
      val syntax = symbol.syntax
      if (name.isBinder) {
        symbols(syntax) =
          symbols(syntax).copy(definition = Some(metadocPosition(name.pos)))
      } else {
        val old = symbols(syntax)
        symbols(syntax) =
          old.copy(references = old.references :+ metadocPosition(name.pos))
      }
    }

    db.sources.foreach { source =>
      source.traverse { case name: Name => add(name) }
    }
    symbols.values.iterator.filter(_.definition.isDefined).toSeq
  }

  def createMetadocSite(site: MetadocSite, options: MetadocOptions): Unit = {
    val target = getAbsolutePath(
      options.target.getOrElse(sys.error("--target is required!"))
    )
    if (options.cleanTargetFirst && Files.exists(target.toNIO)) {
      target.toFile.toScala.delete()
    }
    def semanticdb(): Unit = {
      site.semanticdb.foreach { root =>
        val from = root.resolve("META-INF").resolve("semanticdb")
        from.toFile.toScala.copyTo(target.resolve("semanticdb").toFile.toScala)
      }
    }

    def symbol(): Unit = {
      val root = target.resolve("symbol")
      root.toFile.mkdirs()
      site.symbols.foreach { symbol =>
        val url = new String(Base64.getEncoder.encode(symbol.symbol.getBytes))
        val out = root.resolve(url)
        Files.createDirectories(out.toNIO.getParent)
        Files.write(
          out.toNIO,
          symbol.toByteArray,
          StandardOpenOption.CREATE
        )
      }
    }
    def index(): Unit = {
      Files.write(
        target.resolve("metadoc.index").toNIO,
        site.index.toByteArray
      )
    }
    semanticdb()
    symbol()
    index()
  }

  def run(options: MetadocOptions, remainingArgs: RemainingArgs): Unit = {
    val classpath = Classpath(
      remainingArgs.remainingArgs
        .flatMap(cp => cp.split(File.pathSeparator).map(getAbsolutePath))
        .toList
    )
    val db = Database.load(classpath)
    val symbols = getSymbols(db)
    val files = db.entries.map(x => filename(x.input))
    val index = d.Index(files, symbols.map(_.copy(references = Nil)))
    val site = MetadocSite(classpath.shallow, symbols, index)
    createMetadocSite(site, options)
    println(options.target.get)
  }
}
