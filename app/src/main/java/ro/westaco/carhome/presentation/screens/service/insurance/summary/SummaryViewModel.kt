package ro.westaco.carhome.presentation.screens.service.insurance.summary

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import ro.westaco.carhome.data.sources.remote.apis.CarHomeApi
import ro.westaco.carhome.data.sources.remote.requests.PaymentRequest
import ro.westaco.carhome.data.sources.remote.requests.RcaInitRequest
import ro.westaco.carhome.data.sources.remote.responses.models.*
import ro.westaco.carhome.navigation.BundleProvider
import ro.westaco.carhome.navigation.Screen
import ro.westaco.carhome.navigation.UiEvent
import ro.westaco.carhome.navigation.events.NavAttribs
import ro.westaco.carhome.presentation.base.BaseViewModel
import ro.westaco.carhome.presentation.screens.service.support.transaction_details.TransactionDetailsFragment
import ro.westaco.carhome.utils.DateTimeUtils
import ro.westaco.carhome.utils.DeviceUtils
import ro.westaco.carhome.utils.FirebaseAnalyticsList
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import javax.inject.Inject

@HiltViewModel
class SummaryViewModel @Inject constructor(
    private val app: Application,
    private val api: CarHomeApi
) : BaseViewModel() {

    /*
   ** User Interaction
   */

    var durationData = MutableLiveData<ArrayList<RcaDurationItem>>()
    var rcaInitData = MutableLiveData<RcaInitResponse>()
    var initTransactionDataItems = MutableLiveData<PaymentResponse>()
    var mFirebaseAnalytics = FirebaseAnalytics.getInstance(app)
    var profileLogoData: MutableLiveData<Bitmap?>? = MutableLiveData()
    var userLiveData = MutableLiveData<FirebaseUser>()
    val naturalPersonDetailsLiveDataList: MutableLiveData<NaturalPersonDetails?> = MutableLiveData()
    var countryData = MutableLiveData<ArrayList<Country>>()
    var streetTypeData = MutableLiveData<ArrayList<CatalogItem>?>()

    init {
        userLiveData.value = FirebaseAuth.getInstance().currentUser
    }

    internal fun onBack() {
        uiEventStream.value = UiEvent.NavBack
    }

    internal fun onMain() {
        uiEventStream.value = UiEvent.GoToMain
    }

    internal fun onRootClicked() {
        uiEventStream.value = UiEvent.HideKeyboard
    }

    internal fun getProfileImage(context: Context, user: FirebaseUser?) =
        DeviceUtils.getProfileImage(context, user)

    override fun onFragmentCreated() {
        super.onFragmentCreated()
        fetchDefaultData()
        fetchProfileData()
    }

    internal fun convertFromServerDate(date: String?) =
        DateTimeUtils.convertFromServerDate(app, date)

    @SuppressLint("NullSafeMutableLiveData")
    fun fetchDefaultData() {
        api.getRcaDuration()
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { resp ->
                    durationData.value = resp?.data
                },
                {}
            )

        api.getCountries()
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { resp ->
                    countryData.value = resp?.data
                },
                {

                }
            )


        api.getSimpleCatalog("NOM_STREET_TYPE")
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { streetTypeData.value = it.data }, {

                })
    }

    private fun fetchProfileData() {
        api.getProfileLogo()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({

                try {
                    val byteArray = it.source().readByteArray()
                    val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                    profileLogoData?.value = bitmap
                } catch (e: Exception) {
                    profileLogoData?.value = null
                }
            }, {
                profileLogoData?.value = null
            })
    }

    fun getNaturalPerson(id: Long) {
        api.getNaturalPerson(id)
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                naturalPersonDetailsLiveDataList.value = it?.data
            }, {})
    }

    @SuppressLint("NullSafeMutableLiveData")
    internal fun onCtaItems(items: OffersItem, ds: Boolean) {
        val params = Bundle()
        mFirebaseAnalytics.logEvent(FirebaseAnalyticsList.PURCHASE_INSURANCE, params)

        val dataItems = RcaInitRequest(items, null, ds)
        api.initRcaTransactions(dataItems)
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                rcaInitData.value = it.data
            }, {})

    }

    @SuppressLint("NullSafeMutableLiveData")
    internal fun paymentStart(transactionGuidId: String, invoicePersonGuidId: String) {
        val request = PaymentRequest(
            transactionGuid = transactionGuidId,
            invoicePersonGuid = invoicePersonGuidId
        )
        api.initPayment(request)
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (it.success && it.data != null)
                    initTransactionDataItems.value = it.data
            }, {})

    }

    @SuppressLint("NullSafeMutableLiveData")
    internal fun paymentRetry(transactionGuidId: String) {
        api.retryPayment(transactionGuidId)
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (it.success && it.data != null)
                    initTransactionDataItems.value = it.data
            }, {})

    }

    internal fun onPaymentSuccessful(model: PaymentResponse) {

        uiEventStream.value =
            UiEvent.Navigation(NavAttribs(Screen.TransactionDetails, object : BundleProvider() {
                override fun onAddArgs(bundle: Bundle?): Bundle {
                    return Bundle().apply {
                        putString(TransactionDetailsFragment.ARG_TRANSACTION_GUID, model.guid)
                        putString(TransactionDetailsFragment.ARG_OF, "RO_RCA")
                    }
                }
            }))
    }


}