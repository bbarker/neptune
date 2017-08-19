package com.github.neptune

import scala.scalajs.js
import org.scalajs.dom
import dom.{document, window}
import dom.raw.{MouseEvent, MutationObserver, MutationObserverInit, MutationRecord}
import mhtml.{Rx, Var}
import org.scalajs.dom.html.Div

import scala.concurrent.Future
import scala.scalajs.js.annotation.JSExportTopLevel
import scala.xml.Node

import scala.concurrent.ExecutionContext.Implicits.global

/**

Adapted from https://github.com/amirkarimi/neptune

  MIT license follows:

  Copyright (c) 2017 Amir Karimi, Brandon Barker

  Permission is hereby granted, free of charge, to any person obtaining a copy
  of this software and associated documentation files (the "Software"), to deal
  in the Software without restriction, including without limitation the rights
  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  copies of the Software, and to permit persons to whom the Software is
  furnished to do so, subject to the following conditions:

  The above copyright notice and this permission notice shall be included in all
  copies or substantial portions of the Software.

  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  SOFTWARE.
  */


@JSExportTopLevel("Neptune")
object Neptune {

  case class Component[D](view: Node, model: Rx[D])

  case class Action(icon: Node, title: String, command: () => Unit)

  object Act {
    def apply(icon: Node, title: String)(command: => Unit): Action = {
      Action.apply(icon, title, () => command)
    }
  }

  case class Settings(actions: Seq[Action] = actions, styleWithCss: Boolean = false)

  val actions = Seq(
    Act(<b>B</b>, "Bold") {
      exec("bold")
    },
    Act(<i>I</i>, "Italic") {
      exec("italic")
    },
    Act(<u>U</u>, "Underline") {
      exec("underline")
    },
    Act(<strike>S</strike>, "strikeThrough") {
      exec("strikeThrough")
    },
    Act(<b>H<sub>1</sub></b>, "Heading 1") {
      exec("formatBlock", "<H1>")
    },
    Act(<b>H<sub>2</sub></b>, "Heading 2") {
      exec("formatBlock", "<H2>")
    },
    Act(<div>¶</div>, "Paragraph") {
      exec("formatBlock", "<P>")
    },
    Act(<div>“”</div>, "Quote") {
      exec("formatBlock", "<BLOCKQUOTE>")
    },
    Act(<div>#</div>, "Ordered List") {
      exec("insertOrderedList")
    },
    Act(<div>•</div>, "Unordered List") {
      exec("insertUnorderedList")
    },
    Act(<div>&lt;/&gt;</div>, "Code") {
      exec("formatBlock", "<PRE>")
    },
    Act(<div>―</div>, "Horizontal Line") {
      exec("insertHorizontalRule")
    },
    Act(<div>🔗</div>, "Link") {
      val url = window.prompt("Enter the link URL")
      if (url.nonEmpty) exec("createLink", url)
    },
    Act(<div>📷</div>, "Image") {
      val url = window.prompt("Enter the image URL")
      if (url.nonEmpty) exec("insertImage", url)
    }
  )

  private def exec(command: String) = {
    document.execCommand(command, false, null)
  }

  private def exec(command: String, value: scalajs.js.Any) = {
    document.execCommand(command, false, value)
  }

  def editor(settings: Settings = Settings()): Component[String] = {
    // TODO: settings.classes.actionbar
    val actionBar = <div class={NeptuneStyles.neptuneActionbar.htmlClass}>{
      actions.map { action =>
        <button class={ NeptuneStyles.neptuneButton.htmlClass }
                title={ action.title }
                onclick={ (ev: MouseEvent) => action.command() }
        >{ action.icon }</button>
      }
      }</div>


    val content: Var[String] = Var("")

    def updateContent(domNode: Div): Unit = {
      Future[Unit] { //onmount workaround requires wrapping in Future
        val observerCallback = new Function2[js.Array[MutationRecord], MutationObserver, Unit] {
          def apply(muts: js.Array[MutationRecord], obs: MutationObserver) = {
            content := domNode.innerHTML
          }
        }

        val contentObserver: MutationObserver = new MutationObserver(observerCallback)
        val contentObserverParams = new js.Object{
          val subtree = true
          val attributes = true
          val childList =true
          val characterData = true
          val characterDataOldValue =true
        }.asInstanceOf[MutationObserverInit]
        contentObserver.observe(domNode, contentObserverParams)
      }
      ()
    }

    val contentStore = <div
      class={NeptuneStyles.neptuneContent.htmlClass}
      contentEditable="true" onkeydown={preventTab _}
      mhtml-onmount={ updateContent _ }
    />

    val view = <div>{actionBar}{contentStore}</div>

    if (settings.styleWithCss) exec("styleWithCSS")
    Component(view, content)
  }


  def preventTab(kev: dom.KeyboardEvent): Unit =
    if (kev.keyCode == 9) kev.preventDefault()

}

