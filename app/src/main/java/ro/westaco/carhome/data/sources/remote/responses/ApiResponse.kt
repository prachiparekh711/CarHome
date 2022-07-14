package ro.westaco.carhome.data.sources.remote.responses

import ro.westaco.carhome.data.sources.remote.responses.models.WarningsItem
import java.io.Serializable

class ApiResponse<T>(
    var success: Boolean = false,
    var errorCode: String? = null,
    var errorMessage: String? = null,
    var errorDetails: String? = null,
    var validationResult: ArrayList<WarningsItem?>? = null,
    var data: T? = null

) : Serializable {
    override fun toString(): String {
        return "ApiResponse(success=$success, errorCode=$errorCode, errorMessage=$errorMessage, errorDetails=$errorDetails, validationResult=$validationResult, data=$data)"
    }
}
