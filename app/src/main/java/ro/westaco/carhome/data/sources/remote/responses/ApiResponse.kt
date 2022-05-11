package ro.westaco.carhome.data.sources.remote.responses

class ApiResponse<T> {
    var success: Boolean = false
    var errorCode: String? = null
    var errorMessage: String? = null
    var data: T? = null
    override fun toString(): String {
        return "ApiResponse(success=$success, errorCode=$errorCode, errorMessage=$errorMessage, data=$data)"
    }
}
