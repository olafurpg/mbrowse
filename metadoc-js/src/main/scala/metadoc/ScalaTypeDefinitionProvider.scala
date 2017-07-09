package metadoc

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined
import metadoc.schema.Index
import monaco.CancellationToken
import monaco.Position
import monaco.Thenable
import monaco.editor.IReadOnlyModel
import monaco.languages.Languages.Definition
import monaco.languages.TypeDefinitionProvider
import org.scalameta.logger

@ScalaJSDefined
class ScalaTypeDefinitionProvider(index: Index) extends TypeDefinitionProvider {
  override def provideTypeDefinition(
      model: IReadOnlyModel,
      position: Position,
      token: CancellationToken
  ): Thenable[Definition] = {
    logger.elem(js.JSON.stringify(position))
    ???
  }
}
