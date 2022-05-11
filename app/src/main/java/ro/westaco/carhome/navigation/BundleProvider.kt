package ro.westaco.carhome.navigation

import android.os.Bundle

abstract class BundleProvider {
    private val bundle = Bundle()

    abstract fun onAddArgs(bundle: Bundle?): Bundle

    fun get(): Bundle {
        return onAddArgs(bundle)
    }
}