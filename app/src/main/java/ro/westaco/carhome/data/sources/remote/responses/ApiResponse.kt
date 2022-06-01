package ro.westaco.carhome.data.sources.remote.responses

import java.io.Serializable

class ApiResponse<T>(
    var success: Boolean = false,
    var errorCode: String? = null,
    var errorMessage: String? = null,
    var errorDetails: String? = null,
    var data: T? = null

) : Serializable {
    override fun toString(): String {
        return "ApiResponse(success=$success, errorCode=$errorCode, errorMessage=$errorMessage, errorDetails=$errorDetails, data=$data)"
    }

}
