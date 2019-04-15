package com.github.omarmiatello.gdgtools.template

import io.ktor.html.*
import kotlinx.html.*
import template.mdc.ButtonMaterialTemplate
import template.mdc.TopAppBarContentBelowStyle
import template.mdc.TopAppBarMaterialTemplate

class MulticolumnTemplate(val main: MaterialTemplate = MaterialTemplate()) : Template<HTML> {
    val column1 = Placeholder<FlowContent>()
    val column2 = Placeholder<FlowContent>()
    override fun HTML.apply() {
        insert(main) {
            menu {
                item { +"One" }
                item { +"Two" }
            }
            content {
                div("column") {
                    insert(column1)
                }
                div("column") {
                    insert(column2)
                }
            }
        }
    }
}

class MaterialTemplate : Template<HTML> {
    val content = Placeholder<HtmlBlockTag>()
    val menu = TemplatePlaceholder<MenuTemplate>()
    override fun HTML.apply() {
        head {
            meta("viewport", "width=device-width,initial-scale=1,shrink-to-fit=no")
            link(
                href = "https://unpkg.com/material-components-web@latest/dist/material-components-web.min.css",
                rel = "stylesheet"
            )
            link(href = "https://fonts.googleapis.com/icon?family=Material+Icons", rel = "stylesheet")
            style { unsafe { +"body{margin:0;} :root{--mdc-theme-primary:#1565c0;--mdc-theme-secondary:#ffc400;}" } }
            title { +"Template" }
        }
        body {
            insert(TopAppBarMaterialTemplate("My title")) {}

            div(TopAppBarContentBelowStyle.standard.cssClass) {

                insert(ButtonMaterialTemplate("My button")) {}


                h1 {
                    insert(content)
                }
                insert(MenuTemplate(), menu)
            }

            script(src = "https://unpkg.com/material-components-web@latest/dist/material-components-web.min.js") {}
            script { unsafe { +"mdc.autoInit();" } }
        }
    }
}

class MenuTemplate : Template<FlowContent> {
    val item = PlaceholderList<UL, FlowContent>()
    override fun FlowContent.apply() {
        if (!item.isEmpty()) {
            ul {
                each(item) {
                    li {
                        if (it.first) b {
                            insert(it)
                        } else {
                            insert(it)
                        }
                    }
                }
            }
        }
    }
}