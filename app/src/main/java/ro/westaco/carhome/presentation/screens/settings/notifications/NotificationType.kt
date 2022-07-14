package ro.westaco.carhome.presentation.screens.settings.notifications

import android.content.Context
import ro.westaco.carhome.data.sources.remote.responses.models.CatalogItem

enum class NotificationType(val type: String) {
    ALL("all_notifications"),
    READ("read_notifications"),
    UNREAD("unread_notifications");

    companion object {
        fun toCatalogItemArray(context: Context): ArrayList<CatalogItem> {
            val enumValues = enumValues<NotificationType>()
            val catalogItems = arrayListOf<CatalogItem>()
            enumValues.forEach {
                catalogItems.add(
                    CatalogItem(
                        0,
                        context.getString(getIdForStringResource(it.type, context)),
                    )
                )
            }
            return catalogItems
        }

        private fun getIdForStringResource(name: String, context: Context): Int {
            val packageName = context.packageName
            val resId = context.resources.getIdentifier(name, "string", packageName)
            return resId
        }
    }
}



