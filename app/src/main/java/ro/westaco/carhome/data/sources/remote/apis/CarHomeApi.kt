package ro.westaco.carhome.data.sources.remote.apis

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*
import ro.westaco.carhome.data.sources.remote.requests.*
import ro.westaco.carhome.data.sources.remote.responses.ApiResponse
import ro.westaco.carhome.data.sources.remote.responses.models.*
import rx.Observable


interface CarHomeApi {

    /*
    ** Catalogs
    */
    @GET("carhome/rest/catalogs/{code}")
    fun getSimpleCatalog(@Path("code") code: String): Observable<ApiResponse<ArrayList<CatalogItem>>>

    @GET("carhome/rest/catalogs/NOM_COUNTRIES")
    fun getCountries(): Observable<ApiResponse<ArrayList<Country>>>

    @GET("carhome/rest/siruta")
    fun getSiruta(): Observable<ApiResponse<ArrayList<Siruta>>>

    @GET("transactions/rest/catalogs/NOM_ROVIGNETTE_CATEGORIES")
    fun getRovignetteCategories(): Observable<ApiResponse<ArrayList<ServiceCategory>>>

    @GET("transactions/rest/catalogs/NOM_PASSTAX_CATEGORIES")
    fun getBridgeTaxCategories(): Observable<ApiResponse<ArrayList<ServiceCategory>>>

    @GET("transactions/rest/catalogs/NOM_ROVIGNETTE_DURATIONS")
    fun getRovignetteDurations(): Observable<ApiResponse<ArrayList<RovignetteDuration>>>

    @GET("carhome/rest/catalogs/NOM_COR_ISCO_08")
    fun getOccupation(): Observable<ApiResponse<ArrayList<CatalogItem>>>

    @GET("carhome/rest/catalogs/NOM_CAEN")
    fun getCaen(): Observable<ApiResponse<ArrayList<Caen>>>

    @GET("carhome/rest/catalogs/NOM_COMPANY_ACTIVITY_TYPE")
    fun getActivityType(): Observable<ApiResponse<ArrayList<CatalogItem>>>

    @GET("carhome/rest/catalogs/NOM_VEHICLE_EVENT_TYPES")
    fun getVehicleEventType(): Observable<ApiResponse<ArrayList<EventType>>>

    /*
    ** Account
    */
    @POST("carhome/rest/login/firebase")
    fun login(): Observable<ApiResponse<Nothing>>

    @POST("carhome/rest/account/close")
    fun closeAccount(): Observable<ApiResponse<Nothing>>


    /*
    ** Purchases
    */
    @GET("carhome/rest/transactions/relevant-purchases")
    fun getPurchases(): Observable<ApiResponse<ArrayList<PurchaseCategory>?>>

//test


    /*
    ** Vehicles
    */
    @POST("carhome/rest/vehicles/query-vehicle-info")
    fun queryVehicleInfo(@Body req: QueryVehicleInfoRequest): Observable<ApiResponse<VehicleDetails?>>

    @GET("carhome/rest/vehicles")
    fun getVehicles(): Observable<ApiResponse<ArrayList<Vehicle>?>>

    @GET("carhome/rest/vehicles/{id}")
    fun getVehicle(@Path("id") id: Int): Observable<ApiResponse<VehicleDetails?>>


    @POST("carhome/rest/vehicles")
    fun createVehicle(@Body req: AddVehicleRequest): Call<ApiResponse<Long>>

    @PUT("carhome/rest/vehicles/{id}")
    fun editVehicle(
        @Path("id") id: Long,
        @Body req: AddVehicleRequest
    ): Call<ApiResponse<Nothing>>

    @DELETE("carhome/rest/vehicles/{id}")
    fun deleteVehicle(@Path("id") id: Long): Observable<ApiResponse<Nothing>>

    @GET("carhome/rest/catalogs/NOM_VEHICLE_SUBCATEGORIES")
    fun getVehicleSubCategory(
        @Query("category") category: Int?
    ): Observable<ApiResponse<ArrayList<CatalogItem>>>

    @GET("carhome/rest/catalogs/NOM_VEHICLE_USAGE_TYPE")
    fun getVehicleUsage(): Observable<ApiResponse<ArrayList<CatalogItem>>>

    @GET("carhome/rest/catalogs/NOM_LEASING_COMPANIES")
    fun getLeasingCompanies(
        @Query("countryCode") countryCode: String?
    ): Observable<ApiResponse<ArrayList<LeasingCompany>>>

    @Multipart
    @POST("carhome/rest/vehicles/{id}/attachments")
    fun attachDocumentToVehicles(
        @Path("id") id: Int,
        @Part("attachType") attachType: RequestBody,
        @Part attachment: MultipartBody.Part
    ): Observable<ApiResponse<ArrayList<Attachments>>>

    @DELETE("carhome/rest/vehicles/{id}/attachments/{attachmentId}")
    fun deleteCertificateAttachment(
        @Path("id") id: Int,
        @Path("attachmentId") attachmentId: Int
    ): Observable<ApiResponse<Nothing>>

    /*
    ** Natural Persons
    */
    @GET("carhome/rest/person/natural")
    fun getNaturalPersons(): Observable<ApiResponse<ArrayList<NaturalPerson>?>>

    @GET("carhome/rest/person/natural/{id}")
    fun getNaturalPerson(@Path("id") id: Long): Observable<ApiResponse<NaturalPersonDetails?>>

    @GET("carhome/rest/person/natural/{id}")
    fun getNaturalPersonOffer(@Path("id") id: Long): Observable<ApiResponse<NaturalPersonForOffer?>>

    @GET("carhome/rest/vehicles/{id}")
    fun getVehicleForOffer(@Path("id") id: Int): Observable<ApiResponse<VehicleDetailsForOffer?>>

    @POST("carhome/rest/person/natural")
    fun createNaturalPerson(@Body req: AddNaturalPersonRequest): Observable<ApiResponse<Nothing>>

    @PUT("carhome/rest/person/natural/{id}")
    fun editNaturalPerson(
        @Path("id") id: Long,
        @Body req: AddNaturalPersonRequest
    ): Observable<ApiResponse<Nothing>>

    @DELETE("carhome/rest/person/natural/{id}")
    fun deleteNaturalPerson(@Path("id") id: Long): Observable<ApiResponse<Nothing>>

    @Multipart
    @POST("carhome/rest/person/natural/{id}/attachments")
    fun attachDocumentToNaturalPerson(
        @Path("id") id: Int,
        @Part("attachType") attachType: RequestBody,
        @Part attachment: MultipartBody.Part
    ): Observable<ApiResponse<Attachments>>

    @DELETE("carhome/rest/person/natural/{id}/attachments/{attachmentId}")
    fun deleteAttachmentToNaturalPerson(
        @Path("id") id: Long,
        @Path("attachmentId") attachmentId: Long
    ): Observable<ApiResponse<Nothing>>

    @Multipart
    @POST("carhome/rest/person/{id}/logo")
    fun addPersonLogo(
        @Path("id") id: Int,
        @Part attachment: MultipartBody.Part
    ): Observable<ApiResponse<Attachments>>

    @GET("carhome/rest/person/{id}/logo")
    fun getPersonLogo(
        @Path("id") id: Long
    ): Observable<ResponseBody>

    @DELETE("carhome/rest/person/{id}/logo")
    fun deletePersonLogo(@Path("id") id: Int): Observable<ApiResponse<Nothing>>

    /*
    ** Legal Persons
    */
    @GET("carhome/rest/person/legal")
    fun getLegalPersons(): Observable<ApiResponse<ArrayList<LegalPerson>?>>

    @GET("carhome/rest/person/legal/{id}")
    fun getLegalPersonDetails(@Path("id") id: Long): Observable<ApiResponse<LegalPersonDetails>>

    @POST("carhome/rest/person/legal")
    fun createLegalPerson(@Body req: AddLegalPersonRequest): Observable<ApiResponse<Nothing>>

    @PUT("carhome/rest/person/legal/{id}")
    fun editLegalPerson(
        @Path("id") id: Long,
        @Body req: AddLegalPersonRequest
    ): Observable<ApiResponse<Nothing>>

    @DELETE("carhome/rest/person/legal/{id}")
    fun deleteLegalPerson(@Path("id") id: Long): Observable<ApiResponse<Nothing>>


    /*
    ** Reminders
    */
    @GET("carhome/rest/reminders")
    fun getReminders(): Observable<ApiResponse<ArrayList<Reminder>?>>

    @GET("carhome/rest/reminders/{id}")
    fun getRemindersDetail(
        @Path("id") id: Long
    ): Observable<ApiResponse<Reminder>>

    @POST("carhome/rest/reminders")
    fun createReminder(@Body req: AddReminderRequest): Observable<ApiResponse<Nothing>>

    //    C- Edit Reminder
    @PUT("carhome/rest/reminders/{id}")
    fun editReminder(
        @Path("id") id: Long,
        @Body req: AddReminderRequest
    ): Observable<ApiResponse<Nothing>>

    @DELETE("carhome/rest/reminders/{id}")
    fun deleteReminder(@Path("id") id: Long): Observable<ApiResponse<Nothing>>


    /*
    ** Locations
    */
    @GET("locations/rest/filters")
    fun locationFilterMaps(
    ): Call<ApiResponse<List<SectionModel>>>

    @GET("locations/rest/getLocationFilter")
    fun locationFilterReminder(
    ): Call<ApiResponse<List<LocationFilterItem>>>

    @GET("locations/rest/getLocationsV2")
    fun getLocation(
        @Query("currentLat") currentLat: String,
        @Query("currentLong") currentLong: String
    ): Call<LocationV2Data>

    @GET("locations/rest/getLocationsV2")
    fun getLocationsFiltered(
        @Query("currentLat") currentLat: String,
        @Query("currentLong") currentLong: String,
        @Query("text") searchText: String?,
        @Query("services") services: ArrayList<Int>
    ): Call<LocationV2Data>

    @GET("locations/rest/getLocation")
    fun getCurrentLocation(
        @Query("id") id: Int
    ): Call<ApiResponse<LocationV2Item>>

    // C- Profile screen

    @GET("carhome/rest/profile/person")
    fun getProfile(): Observable<ApiResponse<ProfileItem?>>

    @PUT("carhome/rest/profile/person")
    fun editProfile(@Body req: AddProfileDataRequest): Observable<ApiResponse<Nothing>>

    @Multipart
    @POST("carhome/rest/profile/person/attachments")
    fun attachDocumentToProfile(
        @Part("attachType") attachType: RequestBody,
        @Part attachment: MultipartBody.Part
    ): Observable<ApiResponse<Attachments>>

    @DELETE("carhome/rest/profile/person/attachments/{id}")
    fun deleteAttachment(@Path("id") id: Int): Observable<ApiResponse<Nothing>>

    @Multipart
    @POST("carhome/rest/profile/person/logo")
    fun addProfileLogo(
        @Part attachment: MultipartBody.Part
    ): Observable<ApiResponse<Attachments>>

    @DELETE("carhome/rest/profile/person/logo")
    fun deleteProfileLogo(): Observable<ApiResponse<Nothing>>

    @GET("carhome/rest/profile/person/logo")
    fun getProfileLogo(): Observable<ResponseBody>


    //C - Contact us screen
    /*
    ** Reasons
    */
    @GET("carhome/rest/catalogs/NOM_CONTACT_REASONS")
    fun getContactReasons(): Observable<ApiResponse<ArrayList<Categories>>>

    @Multipart
    @POST("carhome/rest/contact")
    fun submitForm(
        @Part("reasonId") reasonId: RequestBody,
        @Part("message") message: RequestBody,
        @Part attachment: List<MultipartBody.Part>?
    ): Observable<ApiResponse<Nothing>>

    //C - All Notifications
    @GET("carhome/rest/notifications")
    fun getNotifications(): Observable<ApiResponse<ArrayList<Notification>?>>

    @POST("carhome/rest/notifications/mark-as-seen")
    fun markAsSeen(
        @Body ids: MarkSeenRequest
    ): Observable<ApiResponse<Nothing>>

    @GET("carhome/rest/notifications/preferences")
    fun getNotificationPrefrences(): Observable<ApiResponse<ArrayList<NotificationPrefrences>?>>

    @POST("carhome/rest/notifications/preferences")
    fun postNotificationPrefrences(
        @Query("type") type: String,
        @Query("channel") channel: String,
        @Query("active") active: Boolean
    ): Observable<ApiResponse<Nothing>>


    //C- Register Device

    @POST("carhome/rest/notifications/devices")
    fun registerDevice(
        @Body deviceToken: DeviceTokenRequest
    ): Observable<ApiResponse<Nothing>>


    //C- Get Progress for profile and user’s vehicles.
    @GET("carhome/rest/progress")
    fun getProgress(): Observable<ApiResponse<ProgressItem?>>

    //C - History
    @GET("transactions/rest/history")
    fun getHistory(): Observable<ApiResponse<ArrayList<HistoryItem>>>

    //    C-Document
    @GET("documents/rest/categories")
    fun getDocumentCategories(
        @Query("parentId") type: Int?
    ): Observable<ApiResponse<ArrayList<Categories>>>

    @GET("documents/rest/documents")
    fun getDocuments(
        @Query("categoryId") type: Int
    ): Observable<ApiResponse<Documents>>

    @GET("documents/rest/documents/recent")
    fun getRecentDocuments(
    ): Observable<ApiResponse<ArrayList<RowsItem>>>

    @POST("documents/rest/categories")
    fun addCategory(
        @Body req: Categories
    ): Observable<ApiResponse<Int>>

    @GET
    fun getDocumentData(@Url url: String): Observable<ResponseBody>

    @Multipart
    @POST("documents/rest/documents")
    fun addDocument(
        @Part("categoryId") categoryId: RequestBody,
        @Part("fileName") fileName: RequestBody,
        @Part("mergeAs") mergeAs: RequestBody?,
        @Part attachment: List<MultipartBody.Part>?
    ): Observable<ApiResponse<Nothing>>

    @Multipart
    @PUT("documents/rest/documents/{id}")
    fun editDocument(
        @Path("id") id: String,
        @Part("fileName") fileName: RequestBody
    ): Observable<ApiResponse<Nothing>>

    @DELETE("documents/rest/documents/{id}")
    fun deleteDocument(
        @Path("id") id: String
    ): Observable<ApiResponse<Nothing>>

    @DELETE("documents/rest/categories/{id}")
    fun deleteCategory(
        @Path("id") id: String
    ): Observable<ApiResponse<Nothing>>


    @HTTP(
        method = "DELETE",
        hasBody = true,
        path = "documents/rest/delete"
    )
    fun deleteDocumentandCategory(
        @Body model: DocumentCategoryRequest
    ): Observable<ApiResponse<Nothing>>

    @POST("documents/rest/copy")
    fun copyDocumentandCategory(
        @Body model: DocumentCategoryRequest
    ): Observable<ApiResponse<Nothing>>

    @POST("documents/rest/move")
    fun moveDocumentandCategory(
        @Body model: DocumentCategoryRequest
    ): Observable<ApiResponse<Nothing>>

    @GET("documents/rest/categories/{id}")
    fun getCategoryDetail(
        @Path("id") id: String
    ): Observable<ApiResponse<Categories>>

    @GET("documents/rest/documents/{id}")
    fun getDocumentDetail(
        @Path("id") id: String
    ): Observable<ApiResponse<RowsItem>>

    @PUT("documents/rest/categories/{id}")
    fun editCategory(
        @Path("id") id: String,
        @Body req: Categories
    ): Observable<ApiResponse<Nothing>>


    /*
       Insurance
           */
    /*
    * //  personType
//  10010 for natural person, 10020 for legal person.
* */
    @POST("transactions/rest/rca/validate-vehicle/{vehicleGUID}")
    fun identifyVehicle(@Path("vehicleGUID") vehicleGUID: String): Observable<ApiResponse<RcaResponse>>

    @GET("transactions/rest/rca/persons/natural")
    fun verifyNaturalPersonForRCA(
        @Query("personRole") personRole: String
    ): Observable<ApiResponse<ArrayList<VerifyRcaPerson>>>

    @GET("transactions/rest/rca/persons/legal")
    fun verifyLegalPersonForRCA(
        @Query("personRole") personRole: String
    ): Observable<ApiResponse<ArrayList<VerifyRcaPerson>>>

    @POST("transactions/rest/rca/get-offers")
    fun getRcaOffers(
        @Body req: RcaOfferRequest
    ): Observable<ApiResponse<RcaOfferResponse>>

    @GET("transactions/rest/rca/get-offers/{offerCode}")
    fun getRcaOfferDetails(
        @Path("offerCode") offerCode: String,
        @Query("insurerCode") insurerCode: String
    ): Observable<ApiResponse<RcaOfferDetails>>

    // Insurance type (eg. RCA).
    @GET("transactions/rest/rca/pid")
    fun getInsurancePID(
        @Query("insurer") insurer: String,
        @Query("type") type: String
    ): Observable<ResponseBody>

    @POST("transactions/rest/rca/init")
    fun initRcaTransactions(
        @Body req: RcaInitRequest
    ): Observable<ApiResponse<RcaInitResponse>>

    @GET("transactions/rest/rca/{guid}")
    fun getRcaTransaction(@Path("guid") guid: String): Observable<ApiResponse<TransactionData?>>

    @GET("transactions/rest/catalogs/NOM_RCA_DURATIONS")
    fun getRcaDuration(): Observable<ApiResponse<ArrayList<RcaDurationItem>>>

    /*
    ** Vignette
    */
    @GET("transactions/rest/vignettes/prices")
    fun getVignettePrices(): Observable<ApiResponse<ArrayList<VignettePrice>?>>

    /*
    Old URL
       @POST("carhome/rest/transactions/vignettes/init")
      */
    @POST("transactions/rest/vignettes/init")
    fun initVignettePurchaseModel(@Body req: InitVignettePurchaseRequest): Call<ApiResponse<PaymentResponse>>

    /*
    Old URL
      @GET("carhome/rest/transactions/vignettes/{guid}")
      */

    @GET("transactions/rest/vignettes/{guid}")
    fun getVignetteTransaction(@Path("guid") guid: String): Observable<ApiResponse<TransactionData?>>

    /*
     ** Bridge Tax
     */
    @POST("transactions/rest/passtax/prices")
    fun getPasstaxPrices(@Body req: BridgeTaxPrices): Observable<ApiResponse<ArrayList<BridgeTaxPrices>?>>


    @GET("transactions/rest/catalogs/NOM_PASSTAX_OBJECTIVES")
    fun getObjectives(): Observable<ApiResponse<ArrayList<ObjectiveItem>>>

    // Old URL
    //    @POST("carhome/rest/transactions/passtax/init")
    @POST("transactions/rest/passtax/init")
    fun initPassTax(@Body req: PassTaxInitRequest): Call<ApiResponse<PaymentResponse>>

    //  Old URL
    //    @GET("carhome/rest/transactions/passtax/{guid}")
    @GET("transactions/rest/passtax/{guid}")
    fun getPassTaxTransaction(@Path("guid") guid: String): Observable<ApiResponse<TransactionData?>>

    /*
    * Commen API
    * Old URL
    *    @POST("carhome/rest/transactions/payment")
    * */
    @POST("transactions/rest/payment")
    fun initPayment(@Body req: PaymentRequest): Observable<ApiResponse<PaymentResponse>>


}