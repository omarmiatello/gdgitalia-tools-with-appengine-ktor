package template.mdc

import io.ktor.html.Placeholder
import io.ktor.html.Template
import io.ktor.html.insert
import kotlinx.html.*


/**
 * ## Buttons
 * Buttons allow users to take actions, and make choices, with a single tap.
 * [https://material.io/develop/web/components/buttons/]
 * TODO: Disabled, icons
 */
class ButtonMaterialTemplate(
    private val text: String? = null,
    private vararg val buttonStyles: ButtonStyle = emptyArray(),
    private val useJSAutoInit: Boolean = true
) : Template<FlowContent> {
    val content = Placeholder<FlowContent>()
    override fun FlowContent.apply() {
        button {
            classes = setOf("mdc-button", *buttonStyles.cssClasses)
            if (useJSAutoInit) attributes["data-mdc-auto-init"] = "MDCRipple"
            if (text != null) +text
            insert(content)
        }
    }
}


/**
 * ## Top App Bar
 * MDC Top App Bar acts as a container for items such as application title, navigation icon, and action items.
 * [https://material.io/develop/web/components/top-app-bar/]
 */
class TopAppBarMaterialTemplate(
    private val barTitle: String? = null,
    private vararg val topAppBarStyles: TopAppBarStyle = emptyArray(),
    private val useJSAutoInit: Boolean = true
) : Template<FlowContent> {
    val content = Placeholder<FlowContent>()
    override fun FlowContent.apply() {
        header {
            classes = setOf("mdc-top-app-bar", *topAppBarStyles.cssClasses)
            if (useJSAutoInit) attributes["data-mdc-auto-init"] = "MDCTopAppBar"
            div("mdc-top-app-bar__row") {
                section("mdc-top-app-bar__section mdc-top-app-bar__section--align-start") {
                    a("#", classes = "material-icons mdc-top-app-bar__navigation-icon") { +"menu" }
                    if (barTitle != null) span("mdc-top-app-bar__title") { +barTitle }
                    insert(content)
                }

            }
        }
    }
}


