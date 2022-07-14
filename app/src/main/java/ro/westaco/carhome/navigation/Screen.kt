package ro.westaco.carhome.navigation

import androidx.fragment.app.Fragment
import ro.westaco.carhome.presentation.screens.auth.ForgotPasswordFragment
import ro.westaco.carhome.presentation.screens.dashboard.DashboardFragment
import ro.westaco.carhome.presentation.screens.dashboard.profile.ProfileFragment
import ro.westaco.carhome.presentation.screens.dashboard.profile.close.CloseAccountFragment
import ro.westaco.carhome.presentation.screens.dashboard.profile.edit.EditProfileFragment
import ro.westaco.carhome.presentation.screens.data.DataFragment
import ro.westaco.carhome.presentation.screens.data.cars.add_new.AddNewCar2Fragment
import ro.westaco.carhome.presentation.screens.data.cars.add_new.AddNewCarFragment
import ro.westaco.carhome.presentation.screens.data.cars.details.CarDetailsFragment
import ro.westaco.carhome.presentation.screens.data.cars.query_details.QueryCarDetailsFragment
import ro.westaco.carhome.presentation.screens.data.person_legal.add_new.AddNewLegalPersonFragment
import ro.westaco.carhome.presentation.screens.data.person_legal.details.LegalPersonDetailsFragment
import ro.westaco.carhome.presentation.screens.data.person_natural.add_new.AddNewNaturalPersonFragment
import ro.westaco.carhome.presentation.screens.data.person_natural.details.NaturalPersonDetailsFragment
import ro.westaco.carhome.presentation.screens.documents.DocumentsFragment
import ro.westaco.carhome.presentation.screens.reminder.add_new.AddNewReminderFragment
import ro.westaco.carhome.presentation.screens.service.bridgetax_rovignette.bridge_tax_init.PassTaxInitFragment
import ro.westaco.carhome.presentation.screens.service.bridgetax_rovignette.rovignette_init.BuyVignetteFragment
import ro.westaco.carhome.presentation.screens.service.bridgetax_rovignette.select_car.SelectCarFragment
import ro.westaco.carhome.presentation.screens.service.bridgetax_rovignette.summary.BridgeTaxSummaryFragment
import ro.westaco.carhome.presentation.screens.service.insurance.car_selection.InsAddCarFragment
import ro.westaco.carhome.presentation.screens.service.insurance.car_selection.InsCarEditFragment
import ro.westaco.carhome.presentation.screens.service.insurance.car_selection.SelectCarsFragment
import ro.westaco.carhome.presentation.screens.service.insurance.init.DirectClaimInfoFragment
import ro.westaco.carhome.presentation.screens.service.insurance.init.InsuranceFragment
import ro.westaco.carhome.presentation.screens.service.insurance.init.InsuranceStep2Fragment
import ro.westaco.carhome.presentation.screens.service.insurance.offers.FetchScreenFragment
import ro.westaco.carhome.presentation.screens.service.insurance.offers.InsOfferDetailsFragment
import ro.westaco.carhome.presentation.screens.service.insurance.offers.InsOffersFragment
import ro.westaco.carhome.presentation.screens.service.insurance.request.InsAcceptanceRequestFragment
import ro.westaco.carhome.presentation.screens.service.insurance.summary.SummaryFragment
import ro.westaco.carhome.presentation.screens.service.support.help.TransactionHelpFragment
import ro.westaco.carhome.presentation.screens.service.support.payment_methods.SavedCardsFragment
import ro.westaco.carhome.presentation.screens.service.support.payment_methods.add_new.AddNewCardFragment
import ro.westaco.carhome.presentation.screens.service.support.person_add_view.add_legal.AddBillLegalFragment
import ro.westaco.carhome.presentation.screens.service.support.person_add_view.add_natural.AddBillNaturalFragment
import ro.westaco.carhome.presentation.screens.service.support.transaction_details.TransactionDetailsFragment
import ro.westaco.carhome.presentation.screens.settings.contact_us.ContactUsFragment
import ro.westaco.carhome.presentation.screens.settings.extras.*
import ro.westaco.carhome.presentation.screens.settings.history.HistoryFragment
import ro.westaco.carhome.presentation.screens.settings.notifications.AllNotificationSettingFragment
import ro.westaco.carhome.presentation.screens.settings.notifications.NotificationFragment
import ro.westaco.carhome.presentation.screens.settings.notifications.NotificationSettingFragment
import ro.westaco.carhome.presentation.screens.settings.security.ChangePasswordFragment
import ro.westaco.carhome.presentation.screens.settings.security.PasswordChangeSuccessFragment
import ro.westaco.carhome.presentation.screens.settings.security.SecurityFragment


enum class Screen(fragmentClass: Class<out Fragment?>) {
    /*
    ** App Screens
    */

    ForgotPassword(
        ForgotPasswordFragment::class.java
    ),
    Dashboard(
        DashboardFragment::class.java
    ),
    Document(
        DocumentsFragment::class.java
    ),
    SelectCarForService(
        SelectCarFragment::class.java
    ),
    BuyVignette(
        BuyVignetteFragment::class.java
    ),
    TransactionDetails(
        TransactionDetailsFragment::class.java
    ),
    AddReminder(
        AddNewReminderFragment::class.java
    ),
    Profile(
        ProfileFragment::class.java
    ),
    EditAccount(
        EditProfileFragment::class.java
    ),
    ChangePassword(
        ChangePasswordFragment::class.java
    ),
    PasswordChangeSuccess(
        PasswordChangeSuccessFragment::class.java
    ),
    CloseAccount(
        CloseAccountFragment::class.java
    ),
    PaymentMethods(
        SavedCardsFragment::class.java
    ),
    AddPaymentMethod(
        AddNewCardFragment::class.java
    ),
    Security(
        SecurityFragment::class.java
    ),
    History(
        HistoryFragment::class.java
    ),
    Data(
        DataFragment::class.java
    ),
    Notifications(
        NotificationFragment::class.java
    ),
    NotificationSetting(
        NotificationSettingFragment::class.java
    ),
    AllNotificationSetting(
        AllNotificationSettingFragment::class.java
    ),
    QueryCar(
        QueryCarDetailsFragment::class.java
    ),
    AddCar(
        AddNewCarFragment::class.java
    ),
    AddCar2(
        AddNewCar2Fragment::class.java
    ),
    CarDetails(
        CarDetailsFragment::class.java
    ),
    AddNaturalPerson(
        AddNewNaturalPersonFragment::class.java
    ),
    NaturalPersonDetails(
        NaturalPersonDetailsFragment::class.java
    ),
    AddLegalPerson(
        AddNewLegalPersonFragment::class.java
    ),
    LegalPersonDetails(
        LegalPersonDetailsFragment::class.java
    ),

    //  C-  Setting Screen
    AboutUs(
        AboutUsFragment::class.java
    ),
    TermsAndCond(
        TermsAndConFragment::class.java
    ),
    Consents(
        ConsentsFragment::class.java
    ),
    Share(
        ShareTheAppFragment::class.java
    ),
    Faq(
        FAQFragment::class.java
    ),
    ContactUs(
        ContactUsFragment::class.java
    ),
    Social(
        SocialFragment::class.java
    ),
    Language(
        LanguageFragment::class.java
    ),
    BridgeTaxInit(
        PassTaxInitFragment::class.java
    ),


    AddBillNaturalPerson(
        AddBillNaturalFragment::class.java
    ),

    AddBillLegalPerson(
        AddBillLegalFragment::class.java
    ),

    InsuranceRequest(
        InsAcceptanceRequestFragment::class.java
    ),

    SelectInsuranceCar(
        SelectCarsFragment::class.java
    ),

    InsuranceEditCar(
        InsCarEditFragment::class.java
    ),

    InsuranceAddCar(
        InsAddCarFragment::class.java
    ),

    Insurance(
        InsuranceFragment::class.java
    ),

    InsuranceStep2(
        InsuranceStep2Fragment::class.java
    ),

    FetchOfferScreen(
        FetchScreenFragment::class.java
    ),

    DirectClaimInfoScreen(
        DirectClaimInfoFragment::class.java
    ),

    InsuranceOffers(
        InsOffersFragment::class.java
    ),

    InsuranceOfferDetail(
        InsOfferDetailsFragment::class.java
    ),

    InsuranceSummary(
        SummaryFragment::class.java
    ),

    BridgeTaxSummary(
        BridgeTaxSummaryFragment::class.java
    ),

    TransactionHelp(
        TransactionHelpFragment::class.java
    );


    /*
    ** Implementation
    */
    val fragmentClass: Class<*>

    init {
        this.fragmentClass = fragmentClass
    }

    companion object {

        var screenLayerHashMap: HashMap<Class<*>, Screen>? = null

        init {
            screenLayerHashMap = HashMap()
            for (screen in values()) {
                screenLayerHashMap?.let { it[screen.fragmentClass] = screen }
            }
        }

        fun getScreenForFragment(fragment: Fragment?): Screen? {
            return try {
                if (fragment == null) null else screenLayerHashMap?.get(fragment.javaClass)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }


}