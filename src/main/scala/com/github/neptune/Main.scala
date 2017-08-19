package com.github.neptune

import mhtml.mount
import org.scalajs.dom
import scalacss.ProdDefaults._

object Main {

  def main(args: Array[String]): Unit = {
    NeptuneStyles.addToDocument()
    val editor = Neptune.editor()
    val mainDiv = dom.document.getElementById("my-editor")

    val mainView =
      <div>
        {editor.view}
        <h3>Editor output follows</h3>
        {editor.model}
      </div>

    mount(mainDiv, mainView)
  }
}
