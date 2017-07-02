package metadoc

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.meta.internal.semantic.schema.Message
import scala.meta.internal.semantic.schema.Message.Severity
import scala.meta.internal.semantic.{schema => s}
import scala.scalajs.js
import scala.scalajs.js.JSON
import scala.scalajs.js.annotation.ScalaJSDefined
import monaco.Promise
import monaco.Uri
import monaco.common.IReference
import monaco.editor.Editor
import monaco.editor.IMarkerData
import monaco.editor.IModel
import monaco.services.ITextEditorModel
import monaco.services.ITextModelResolverService
import org.scalameta.logger

@ScalaJSDefined
object MetadocTextModelService extends ITextModelResolverService {
  // NOTE: we have a private cache here to avoid "duplicate model" errors.
  // It's possible to hit those errors for example in the reference provider, where
  // we call `Future.sequence(Seq.map(modelReference))`. Since js is single threaded,
  // we are safe from race conditions.
  private val cache = mutable.Map.empty[String, IModel]
  def createModel(value: String, filename: String): IModel =
    createModel(value, createUri(filename))
  def createModel(value: String, uri: Uri): IModel =
    cache.getOrElseUpdate(
      uri.path,
      monaco.editor.Editor.createModel(value, "scala", uri)
    )

  def modelReference(
      filename: String
  ): Future[IReference[ITextEditorModel]] =
    modelReference(createUri(filename))

  def modelReference(
      resource: Uri
  ): Future[IReference[ITextEditorModel]] = {
    val existingModel = Editor.getModel(resource)
    if (existingModel != null) {
      Future.successful(IReference(ITextEditorModel(existingModel)))
    } else {
      for {
        attrs <- MetadocAttributeService.fetchsAttributes(resource.path)
      } yield {
        val model = createModel(attrs.contents, resource)
        // NOTE(olafurpg): It's not documented what `owner` does, but I suspect
        // it's supposed to be a unique string for the application reporting that
        // marker. For example, each linter/compiler application will use a
        // custom owner.
        val markers = attrs.messages.collect {
          case message @ Message(Some(range), severity, msg) =>
            val marker = jsObject[IMarkerData]
            val start = model.getPositionAt(range.start)
            val end = model.getPositionAt(range.end)
            marker.message = msg
            marker.startColumn = start.column
            marker.startLineNumber = start.lineNumber
            marker.endColumn = end.column
            marker.endLineNumber = end.lineNumber
            marker.severity = severity match {
              case Severity.ERROR => monaco.Severity.Error
              case Severity.WARNING => monaco.Severity.Warning
              case Severity.INFO => monaco.Severity.Info
              case _ => monaco.Severity.Ignore
            }
            logger.elem(message, JSON.stringify(marker))
            marker
        }
        // PS: We might want to delay reporting markers. createModel is called
        // in batch by services like reference provider, where we care less
        // about seeing markers immediately.
        Editor.setModelMarkers(model, "metadoc", js.Array(markers: _*))
        IReference(ITextEditorModel(model))
      }
    }
  }
  override def createModelReference(
      resource: Uri
  ): Promise[IReference[ITextEditorModel]] =
    modelReference(resource).toMonacoPromise
}
