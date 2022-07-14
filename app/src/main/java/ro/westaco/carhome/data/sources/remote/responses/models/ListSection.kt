package ro.westaco.carhome.data.sources.remote.responses.models

data class ListSection(
    val title: String,
    val code: String,
    val isToday: Boolean,
    val isPastSection: Boolean
) : ListItem(null, null) {
    override fun toString(): String {
        return "ListSection(title='$title', code='$code', isToday=$isToday, isPastSection=$isPastSection)"
    }
}
