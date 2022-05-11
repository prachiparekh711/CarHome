package ro.westaco.carhome.utils

import ro.westaco.carhome.data.sources.remote.responses.models.Caen
import ro.westaco.carhome.data.sources.remote.responses.models.LeasingCompany

object LeasingCompanyUtils {

    fun findById(list: List<LeasingCompany>?, id: Int?): LeasingCompany? {
        if (list.isNullOrEmpty()) return null
        for (i in list) {
            if (i.id == id) {
                return i
            }
        }
        return null
    }

    fun findByName(list: List<Caen>?, name: String): String? {
        if (list.isNullOrEmpty()) return null
        for (i in list.withIndex()) {
            if (i.value.name == name) {
                return i.value.code
            }
        }
        return null
    }

    fun findPosByCode(list: List<Caen>?, code: String): Int {
        if (list.isNullOrEmpty()) return -1
        for (i in list.withIndex()) {
            if (i.value.code?.equals(code) == true) {
                return i.index
            }
        }
        return -1
    }
}