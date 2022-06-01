package ro.westaco.carhome.utils

import ro.westaco.carhome.data.sources.remote.responses.models.Siruta

class SirutaUtil {

    companion object {
        var sirutaList: ArrayList<Siruta> = ArrayList()
        var countyList: ArrayList<Siruta> = ArrayList()
        var defaultCounty: Siruta? = null
        var defaultCity: Siruta? = null

        fun fetchCity(siruta: Siruta): ArrayList<Siruta> {

            val cityList: ArrayList<Siruta> = ArrayList()
            for (i in sirutaList.indices) {
                if (siruta.code == sirutaList[i].parentCode) {
                    cityList.add(sirutaList[i])
                }
            }
            cityList.sortWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })
            return cityList
        }

        fun fetchCountyPosition(situtaName: String): Int {
            if (countyList.isEmpty()) return -1
            for (i in countyList.withIndex()) {
                if (i.value.name == situtaName) {
                    return i.index
                }
            }
            return -1
        }

        fun fetchCounty(situtaName: String): Siruta? {
            for (i in countyList) {
                if (i.name == situtaName) {
                    return i
                }
            }
            return null
        }

    }
}