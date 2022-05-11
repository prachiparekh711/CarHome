package ro.westaco.carhome.navigation.events

import android.os.Bundle
import ro.westaco.carhome.navigation.BundleProvider
import ro.westaco.carhome.navigation.Screen

class NavAttribs(screen: Screen, bp: BundleProvider? = null, addToBackStack: Boolean = true) {

    var screen: Screen? = screen
        private set
    var args: Bundle? = null
        private set
    var addToBackStack: Boolean = addToBackStack
        private set

    init {
        bp?.let { this.args = it.get() }
    }
}