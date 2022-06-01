package ro.westaco.carhome.presentation.interfaceitem

import ro.westaco.carhome.data.sources.remote.responses.models.Siruta

interface CountyListClick {

    fun click(position: Int, code: Siruta)
}