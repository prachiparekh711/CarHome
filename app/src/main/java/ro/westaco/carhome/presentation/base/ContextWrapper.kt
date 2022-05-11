package ro.westaco.carhome.presentation.base

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.LocaleList
import java.util.*


class ContextWrapper(base: Context) : android.content.ContextWrapper(base) {

    companion object {
        fun wrap(context: Context, newLocale: Locale): ContextWrapper {
            var context: Context = context
            val res: Resources = context.resources
            val configuration: Configuration = res.configuration
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                configuration.setLocale(newLocale)
                val localeList = LocaleList(newLocale)
                LocaleList.setDefault(localeList)
                configuration.setLocales(localeList)
                context = context.createConfigurationContext(configuration)
            } else setAppLanguage(context, newLocale)
            return ContextWrapper(context)
        }


        fun setAppLanguage(context: Context, newLocale: Locale) {
            val res = context.resources
            val dm = res.displayMetrics
            val conf = res.configuration
            conf.locale = newLocale
            res.updateConfiguration(conf, dm)
        }
    }
}