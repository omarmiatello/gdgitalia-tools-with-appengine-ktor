package template.mdc


// Utils

interface MaterialStyle {
    val cssClass: String
}

val Array<out MaterialStyle>.cssClasses get() = Array(size) { this[it].cssClass }


// Components

/**
 * ## Style: Buttons
 * Buttons allow users to take actions, and make choices, with a single tap.
 * [https://material.io/develop/web/components/buttons/]
 */
enum class ButtonStyle(override val cssClass: String) : MaterialStyle {
    /**
     * Styles a contained button that is elevated above the surface.
     */
    raised("mdc-button--raised"),
    /**
     * Styles a contained button that is flush with the surface.
     */
    unelevated("mdc-button--unelevated"),
    /**
     * Styles an outlined button that is flush with the surface.
     */
    outlined("mdc-button--outlined"),
    /**
     * Makes the button text and container slightly smaller.
     */
    dense("mdc-button--dense"),
    /**
     * Indicates an icon element.
     */
    icon("mdc-button--icon"),
}


/**
 * ## Style: Top App Bar
 * MDC Top App Bar acts as a container for items such as application title, navigation icon, and action items.
 * [https://material.io/develop/web/components/top-app-bar/]
 */
enum class TopAppBarStyle(override val cssClass: String) : MaterialStyle {
    /**
     * Class used to style the top app bar as a fixed top app bar.
     */
    fixed("mdc-top-app-bar--fixed"),
    /**
     * Class used to style the top app bar as a prominent top app bar.
     */
    prominent("mdc-top-app-bar--prominent"),
    /**
     * Class used to style the top app bar as a dense top app bar.
     */
    dense("mdc-top-app-bar--dense"),
    /**
     * Class used to style the top app bar as a short top app bar.
     */
    short("mdc-top-app-bar--short"),
    /**
     * Class used to indicate the short top app bar is collapsed.
     */
    short_collapsed("mdc-top-app-bar--short-collapsed"),
}

enum class TopAppBarContentBelowStyle(override val cssClass: String) : MaterialStyle {
    /**
     * Class used to style the content below the standard and fixed top app bar to prevent the top app bar from covering it.
     */
    standard("mdc-top-app-bar--fixed-adjust"),
    /**
     * Class used to style the content below the standard and fixed top app bar to prevent the top app bar from covering it.
     */
    fixed_adjust("mdc-top-app-bar--fixed-adjust"),
    /**
     * Class used to style the content below the prominent top app bar to prevent the top app bar from covering it.
     */
    prominent_fixed_adjust("mdc-top-app-bar--prominent-fixed-adjust"),
    /**
     * Class used to style the content below the dense top app bar to prevent the top app bar from covering it.
     */
    dense_fixed_adjust("mdc-top-app-bar--dense-fixed-adjust"),
    /**
     * Class used to style the content below the top app bar when styled as both prominent and dense, to prevent the top app bar from covering it.
     */
    dense_prominent_fixed_adjust("mdc-top-app-bar--dense-prominent-fixed-adjust"),
    /**
     * Class used to style the content below the short top app bar to prevent the top app bar from covering it.
     */
    short_fixed_adjust("mdc-top-app-bar--short-fixed-adjust"),
}