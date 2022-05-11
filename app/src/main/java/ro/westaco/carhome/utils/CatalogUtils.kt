package ro.westaco.carhome.utils

import ro.westaco.carhome.data.sources.remote.responses.models.CatalogItem

object CatalogUtils {

    fun findById(list: List<CatalogItem>?, id: Long): CatalogItem? {
        if (list.isNullOrEmpty()) return null
        for (i in list) {
            if (i.id == id) {
                return i
            }
        }
        return null
    }

    fun findPosById(list: List<CatalogItem>?, id: Long): Int {
        if (list.isNullOrEmpty()) return -1
        for (i in list.withIndex()) {
            if (i.value.id == id) {
                return i.index
            }
        }
        return -1
    }

    fun findByName(list: List<CatalogItem>?, name: String): CatalogItem? {
        if (list.isNullOrEmpty()) return null
        for (i in list) {
            if (i.name == name) {
                return i
            }
        }
        return null
    }
}