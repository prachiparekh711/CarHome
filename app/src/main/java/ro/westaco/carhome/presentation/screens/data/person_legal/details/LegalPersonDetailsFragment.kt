package ro.westaco.carhome.presentation.screens.data.person_legal.details

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_legal_person_details.*
import org.json.JSONObject
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.local.prefs.AppPreferencesDelegates
import ro.westaco.carhome.data.sources.remote.requests.PhoneCodeModel
import ro.westaco.carhome.data.sources.remote.responses.models.CatalogItem
import ro.westaco.carhome.data.sources.remote.responses.models.Country
import ro.westaco.carhome.data.sources.remote.responses.models.LegalPerson
import ro.westaco.carhome.data.sources.remote.responses.models.LegalPersonDetails
import ro.westaco.carhome.databinding.DialogDeleteLegalPersonBinding
import ro.westaco.carhome.databinding.LogoOptionLayoutBinding
import ro.westaco.carhome.di.ApiModule
import ro.westaco.carhome.presentation.base.BaseActivity
import ro.westaco.carhome.presentation.base.BaseFragment
import ro.westaco.carhome.utils.CountryCityUtils
import ro.westaco.carhome.utils.FileUtil
import ro.westaco.carhome.utils.ViewUtils
import java.io.File
import java.util.*

//C- Rebuilt
@AndroidEntryPoint
class LegalPersonDetailsFragment : BaseFragment<LegalPersonDetailsViewModel>(),
    BaseActivity.OnProfileLogoChangeListener {
    private var legalPerson: LegalPerson? = null
    private var legalPersonDetails: LegalPersonDetails? = null
    var lottieAnimationView: LottieAnimationView? = null
    private var menuOpen = false
    lateinit var bottomSheet: BottomSheetDialog
    var countriesList: ArrayList<Country> = ArrayList()
    var streetTypeList: ArrayList<CatalogItem> = ArrayList()
    private var menuOpen3 = false
    private var argMenuVisible = true

    companion object {
        const val ARG_LEGAL_PERSON = "arg_legal_person"
        const val ARG_MENU = "arg_menu"
    }

    override fun getContentView() = R.layout.fragment_legal_person_details

    override fun getStatusBarColor() = ContextCompat.getColor(requireContext(), R.color.white)

    override fun onResume() {
        super.onResume()
        arguments?.let {
            legalPerson = it.getSerializable(ARG_LEGAL_PERSON) as? LegalPerson?
            legalPerson?.id?.toLong()?.let { it1 -> viewModel.getLegalPersonDetails(it1) }
            argMenuVisible = it.getBoolean(ARG_MENU)
        }
    }

    override fun initUi() {

        bottomSheet = BottomSheetDialog(requireContext())
        lottieAnimationView = lottieAnimation
        lottieAnimationView?.visibility = View.VISIBLE

        mMenu.isVisible = argMenuVisible
        back.setOnClickListener {
            viewModel.onBack()
        }

        home.setOnClickListener {
            viewModel.onMain()
        }

        avatar.setOnClickListener {
            logoOperationDialog()
        }

        mMenu.setOnClickListener {
            menuVisible()
        }

        blankRl.setOnClickListener {
            menuVisible()
        }

        name_in_lay.setOnClickListener {
            if (cd_hidden_view.visibility == View.VISIBLE) {
                ViewUtils.collapse(cd_hidden_view)
                name_in_lay.setBackgroundResource(R.color.white)
                name_arrow.setImageResource(R.drawable.ic_arrow_circle_down)
            } else {
                ViewUtils.expand(cd_hidden_view)
                name_in_lay.setBackgroundResource(R.color.expande_colore)
                name_arrow.setImageResource(R.drawable.ic_arrow_circle_up)
            }
        }

        adds_fix_lay.setOnClickListener {
            if (menuOpen3) {
                menuOpen3 = !menuOpen3
                ViewUtils.expand(adds_hidden_view)
                adds_fix_lay.setBackgroundResource(R.color.expande_colore)
                adds_arrow.setImageResource(R.drawable.ic_arrow_circle_up)
            } else {
                menuOpen3 = !menuOpen3
                ViewUtils.collapse(adds_hidden_view)
                adds_fix_lay.setBackgroundResource(R.color.white)
                adds_arrow.setImageResource(R.drawable.ic_arrow_circle_down)
            }
        }

        personal_info_fixed_layout.setOnClickListener {

            if (p_hidden_view.visibility == View.VISIBLE) {
                ViewUtils.collapse(p_hidden_view)
                p_arrow.setImageResource(R.drawable.ic_arrow_circle_down)
                personal_info_lay.setBackgroundResource(R.color.white)
            } else {
                ViewUtils.expand(p_hidden_view)
                p_arrow.setImageResource(R.drawable.ic_arrow_circle_up)
                personal_info_lay.setBackgroundResource(R.color.expande_colore)
            }
        }

    }

    private fun menuVisible() {
        if (menuOpen) {
            menuOpen = !menuOpen
            ViewUtils.collapse(mLinear)
            blankRl.isVisible = false
            mMenu.setImageResource(R.drawable.ic_more_new)
        } else {
            menuOpen = !menuOpen
            ViewUtils.expand(mLinear)
            blankRl.isVisible = true
            mMenu.setImageResource(R.drawable.ic_more_new_done)
        }
    }

    private fun logoOperationDialog() {
        val bottomSheetDialog =
            BottomSheetDialog(
                requireActivity(),
                R.style.BottomSheetStyle
            )
        val selectorBinding =
            LogoOptionLayoutBinding.inflate(
                LayoutInflater.from(
                    requireActivity()
                )
            )
        bottomSheetDialog.setContentView(
            selectorBinding.root
        )
        bottomSheetDialog.show()
        selectorBinding.delete.isVisible = legalPersonDetails?.logoHref?.isNotEmpty() == true
        selectorBinding.upload.setOnClickListener {
            BaseActivity.profileLogoListner = this

            if (ActivityCompat.checkSelfPermission(
                    requireActivity(),
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                onCameraPermission()
            } else {

                if (permission()) {
                    openCamera()
                }
            }

            bottomSheetDialog.dismiss()
        }
        selectorBinding.delete.setOnClickListener {
            lottieAnimationView?.visibility = View.VISIBLE
            legalPersonDetails?.id.let {
                if (it != null) {
                    viewModel.deleteLogo(it.toInt())
                }
            }
            bottomSheetDialog.dismiss()
        }

        selectorBinding.Cancel.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

    }

    private fun openCamera() {
        CropImage.activity().setAllowFlipping(false).setAllowRotation(false).setAspectRatio(1, 1)
            .setCropShape(CropImageView.CropShape.OVAL).start(requireActivity())
    }

    override fun setObservers() {

        viewModel.legalPersDetailsLiveData?.observe(viewLifecycleOwner) { legalItem ->
            if (legalItem != null) {
                legalPersonDetails = legalItem

                if (legalPersonDetails?.logoHref?.isNotEmpty() == true) {
                    val url = "${ApiModule.BASE_URL_RESOURCES}${legalPersonDetails?.logoHref}"
                    val glideUrl = GlideUrl(
                        url,
                        LazyHeaders.Builder()
                            .addHeader(
                                "Authorization",
                                "Bearer ${AppPreferencesDelegates.get().token}"
                            )
                            .build()
                    )

                    val options = RequestOptions()
                    avatar.clipToOutline = true
                    Glide.with(requireContext())
                        .load(glideUrl)
                        .error(requireContext().resources.getDrawable(R.drawable.ic_user))
                        .apply(
                            options.centerCrop()
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .skipMemoryCache(true)
                                .priority(Priority.HIGH)
                                .format(DecodeFormat.PREFER_ARGB_8888)
                        )
                        .into(avatar)
                }
                lottieAnimationView?.visibility = View.GONE

                viewModel.fetchDefaultData()
                legalItem.let {
                    val address = it.address
                    companyTitle.text = it.companyName

                    noReg.setText(it.noRegistration)
                    emailId.setText(it.email)
                    cui.setText(it.cui)

                    caen.setText(it.caen?.name)
                    activityType.setText(it.activityType?.name)

                    street_type.setText(address?.streetType?.name)
                    streetName.setText(address?.streetName)
                    buildingNo.setText(address?.buildingNo)
                    blockName.setText(address?.block)
                    entrance.setText(address?.entrance)
                    apartment.setText(address?.apartment)
                    floor.setText(address?.floor)
                    zipCode.setText(address?.zipCode)

                    address?.countryCode?.let { changeCountryState(it) }
                    if (address?.countryCode == "ROU") {
                        countySpinnerText.setText(address.region)
                        localitySpinnerText.setText(address.locality)
                    } else {
                        stateProvinceText.setText(address?.region)
                        localityAreaText.setText(address?.locality)
                    }
                }

                cta.setOnClickListener {
                    viewModel.onEdit(legalItem)
                }


                delete.setOnClickListener {
                    menuVisible()

                    val bindingSheet = DataBindingUtil.inflate<DialogDeleteLegalPersonBinding>(
                        layoutInflater,
                        R.layout.dialog_delete_legal_person,
                        null,
                        false
                    )
                    bottomSheet.setContentView(bindingSheet.root)
                    bindingSheet.mDeleteText.text = requireContext().resources.getString(
                        R.string.delete_name,
                        legalPerson?.companyName
                    )

                    bindingSheet.cancel.setOnClickListener {
                        bottomSheet.dismiss()
                    }

                    bindingSheet.mDelete.setOnClickListener {
                        legalPerson?.id?.toLong()?.let { it1 -> viewModel.onDelete(it1) }
                        bottomSheet.dismiss()
                    }

                    bottomSheet.show()
                }
            }
        }

        viewModel.countryData.observe(viewLifecycleOwner) { countryData ->
            if (countryData != null) {
                this.countriesList = countryData
                val address = legalPersonDetails?.address
                val pos = address?.countryCode?.let {
                    Country.findPositionForCode(
                        countriesList,
                        it
                    )
                }
                val countryItem = pos?.let { countriesList[it] }
                countryNameTV.text = countryItem?.name
                countryItem?.twoLetterCode?.lowercase(
                    Locale.getDefault()
                )?.let {
                    CountryCityUtils.getFlagId(
                        it
                    )
                }?.let {
                    cuntryFlagIV.text =
                        it

                }
                setPhoneCountryData()
            }
        }

        viewModel.streetTypeData.observe(viewLifecycleOwner) { streetTypeData ->
            if (streetTypeData != null) {
                streetTypeList = streetTypeData
            }
        }

        viewModel.actionStream.observe(viewLifecycleOwner) {
            when (it) {
                is LegalPersonDetailsViewModel.ACTION.OnDeleteLogo -> {
                    lottieAnimationView?.visibility = View.GONE
                    avatar.setImageDrawable(requireContext().resources.getDrawable(R.drawable.default_avatar))
                }
            }
        }

    }

    private fun setPhoneCountryData() {
        val phoneModelList: ArrayList<PhoneCodeModel> = ArrayList()
        val obj = FileUtil.loadJSONFromAsset(requireContext())?.let { JSONObject(it) }
        var phoneCountryItem: Country? = null
        if (legalPersonDetails != null) {
            val pos = legalPersonDetails?.phoneCountryCode?.let { it1 ->
                Country.findPositionForCode(
                    countriesList,
                    it1
                )
            }
            if (pos != null)
                phoneCountryItem = countriesList[pos]
        }

        var romanCode: PhoneCodeModel? = null
        if (obj != null) {
            for (key in obj.keys()) {
                val keyStr = key as String
                val keyValue = obj[keyStr]
                val code = PhoneCodeModel(keyStr, keyValue as String?)
                phoneModelList.add(code)
                if (phoneCountryItem != null && phoneCountryItem.twoLetterCode == code.key) {
                    romanCode = code
                } else {
                    if (code.key == "RO" && romanCode == null) {
                        romanCode = code
                    }
                }
            }
        }

        if (legalPersonDetails?.phone.isNullOrEmpty()) {
            phoneItem.setText("")
        } else {
            phoneItem.setText("+${romanCode?.value}${legalPersonDetails?.phone}")
        }
    }

    private fun changeCountryState(code: String) {
        if (code == "ROU") {
            spinnerCounty.isVisible = true
            spinnerLocality.isVisible = true
            stateProvinceLabel.isVisible = false
            localityAreaLabel.isVisible = false
        } else {
            spinnerCounty.isVisible = false
            spinnerLocality.isVisible = false
            stateProvinceLabel.isVisible = true
            localityAreaLabel.isVisible = true
        }
    }


    override fun onChangeLogo(uri: Uri) {
        val selectedFile = uri.let {
            FileUtil.getPath(it, requireContext())
        }
        if (selectedFile != null) {
            legalPersonDetails?.id.let {
                if (it != null) {
                    viewModel.onAddLogo(it.toInt(), File(selectedFile))
                }
            }
        }
    }

    private fun onCameraPermission() {

        Dexter.withContext(requireContext())
            .withPermission(Manifest.permission.CAMERA)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                    openCamera()
                }

                override fun onPermissionDenied(p0: PermissionDeniedResponse?) {}

                override fun onPermissionRationaleShouldBeShown(
                    p0: PermissionRequest?,
                    p1: PermissionToken?,
                ) {
                    p1?.continuePermissionRequest()
                }
            }).withErrorListener {}

            .check()

    }

    private fun permission(): Boolean {

        return ActivityCompat.checkSelfPermission(
            requireActivity(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

    }

}