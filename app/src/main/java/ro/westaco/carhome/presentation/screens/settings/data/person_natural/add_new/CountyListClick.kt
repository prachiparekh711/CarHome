package ro.westaco.carhome.presentation.screens.settings.data.person_natural.add_new

import ro.westaco.carhome.data.sources.remote.responses.models.Siruta

interface CountyListClick {

    fun click(position: Int, code: Siruta)
}