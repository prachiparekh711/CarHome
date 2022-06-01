package ro.westaco.carhome.presentation.screens.data.person_natural.details

import android.app.Activity
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.RequiresApi
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
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_natural_person_details.*
import org.json.JSONObject
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.local.prefs.AppPreferencesDelegates
import ro.westaco.carhome.data.sources.remote.requests.PhoneCodeModel
import ro.westaco.carhome.data.sources.remote.responses.models.*
import ro.westaco.carhome.databinding.DialogDeleteNaturalPersonBinding
import ro.westaco.carhome.databinding.LogoOptionLayoutBinding
import ro.westaco.carhome.di.ApiModule
import ro.westaco.carhome.presentation.base.BaseActivity
import ro.westaco.carhome.presentation.base.BaseFragment
import ro.westaco.carhome.presentation.common.DeleteDialogFragment
import ro.westaco.carhome.presentation.screens.home.PdfActivity
import ro.westaco.carhome.presentation.screens.main.MainActivity
import ro.westaco.carhome.utils.*
import java.io.File
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

//C- Rebuilt Section
@AndroidEntryPoint
class NaturalPersonDetailsFragment : BaseFragment<NaturalPersonDetailsViewModel>(),
    BaseActivity.OnProfileLogoChangeListner {
    private var naturalPerson: NaturalPerson? = null
    lateinit var bottomSheet: BottomSheetDialog
    private var menuOpen = false
    private var menuOpen1 = false
    private var menuOpen2 = false
    private var menuOpen3 = false
    private var menuOpen4 = false
    private var menuOpen5 = false
    var countriesList: ArrayList<Country> = ArrayList()
    var streetTypeList: ArrayList<CatalogItem> = ArrayList()
    var licenseCatList: ArrayList<CatalogItem> = ArrayList()
    lateinit var dialog: Dialog


    companion object {
        const val ARG_NATURAL_PERSON = "arg_natural_person"
    }

    override fun getContentView() = R.layout.fragment_natural_person_details
    var dlAttachment: Attachments? = null
    var idAttachment: Attachments? = null
    var naturalItem: NaturalPersonDetails? = null
    var lottieAnimationView: LottieAnimationView? = null
    var progressbar: Progressbar? = null


    override fun getStatusBarColor() = ContextCompat.getColor(requireContext(), R.color.white)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            naturalPerson = it.getSerializable(ARG_NATURAL_PERSON) as? NaturalPerson?
            viewModel.onReceivedPerson(naturalPerson?.id?.toInt())
        }
    }

    override fun initUi() {
        bottomSheet = BottomSheetDialog(requireContext())
        progressbar = Progressbar(requireContext())

        progressbar?.showPopup()

        lottieAnimationView = lottieAnimation
        lottieAnimationView?.visibility = View.VISIBLE
        back.setOnClickListener {
            viewModel.onBack()
        }

        home.setOnClickListener {
            viewModel.onMain()
        }

        cta.setOnClickListener {
            menuVisible()
            viewModel.onEdit()
        }

        delete.setOnClickListener {

            menuVisible()

            val bindingSheet = DataBindingUtil.inflate<DialogDeleteNaturalPersonBinding>(
                layoutInflater,
                R.layout.dialog_delete_natural_person,
                null,
                false
            )
            bottomSheet.setContentView(bindingSheet.root)
            bindingSheet.mDeleteText.text =
                requireContext().resources.getString(R.string.delete_name, naturalPerson?.firstName)

            bindingSheet.cancel.setOnClickListener {
                bottomSheet.dismiss()
            }

            bindingSheet.mDelete.setOnClickListener {
                naturalPerson?.id?.let { it1 -> viewModel.onDelete(it1) }
                bottomSheet.dismiss()
            }

            bottomSheet.show()
        }

        llUploadCertificate.setOnClickListener {
            val result = FileUtil.checkPermission(requireContext())
            if (result) {
                callFileManagerForLicense()
            } else {
                FileUtil.requestPermission(requireActivity())
            }

        }

        llUploadId.setOnClickListener {
            val result = FileUtil.checkPermission(requireContext())
            if (result) {
                callFileManagerForID()
            } else {
                FileUtil.requestPermission(requireActivity())
            }
        }

        btnDeleteCertificate.setOnClickListener {

            val drivingLicenseDialog = DeleteDialogFragment()
            drivingLicenseDialog.layoutResId = R.layout.n_person_delete_doc
            drivingLicenseDialog.listener =
                object : DeleteDialogFragment.OnDialogInteractionListener {
                    override fun onPosClicked() {

                        naturalItem?.drivingLicenseAttachment?.id?.let { it1 ->
                            naturalItem?.id?.let { it2 ->
                                viewModel.onDeleteAttachment(
                                    it2,
                                    it1,
                                    requireContext().getString(R.string.attchment_dl)
                                )
                            }
                        }

                        drivingLicenseDialog.dismiss()
                    }
                }

            drivingLicenseDialog.show(childFragmentManager, DeleteDialogFragment.TAG)
        }

        btnDeleteId.setOnClickListener {


            val cnpDialog = DeleteDialogFragment()
            cnpDialog.layoutResId = R.layout.n_person_delete_doc_id
            cnpDialog.listener = object : DeleteDialogFragment.OnDialogInteractionListener {
                override fun onPosClicked() {

                    naturalItem?.identityDocumentAttachment?.id?.let { it1 ->
                        naturalItem?.id?.let { it2 ->
                            viewModel.onDeleteAttachment(
                                it2,
                                it1,
                                requireContext().getString(R.string.attchment_id)
                            )
                        }
                    }
                    cnpDialog.dismiss()
                }
            }

            cnpDialog.show(childFragmentManager, DeleteDialogFragment.TAG)

        }

        avatar.setOnClickListener {
            logoOperationDialog()
        }

        personal_info_fixed_layout.setOnClickListener {
            if (menuOpen) {
                menuOpen = !menuOpen
                ViewUtils.expand(p_hidden_view)
                personal_info_fixed_layout.setBackgroundResource(R.color.expande_colore)
                p_arrow.setImageResource(R.drawable.ic_arrow_circle_up)
            } else {
                menuOpen = !menuOpen
                ViewUtils.collapse(p_hidden_view)
                personal_info_fixed_layout.setBackgroundResource(R.color.white)
                p_arrow.setImageResource(R.drawable.ic_arrow_circle_down)
            }
        }

        identity_fix_lay.setOnClickListener {
            if (menuOpen1) {
                menuOpen1 = !menuOpen1
                ViewUtils.expand(identity_hidden_view)
                identity_fix_lay.setBackgroundResource(R.color.expande_colore)
                id_arrow.setImageResource(R.drawable.ic_arrow_circle_up)
            } else {
                menuOpen1 = !menuOpen1
                ViewUtils.collapse(identity_hidden_view)
                identity_fix_lay.setBackgroundResource(R.color.white)
                id_arrow.setImageResource(R.drawable.ic_arrow_circle_down)
            }
        }

        license_fix_lay.setOnClickListener {
            if (menuOpen2) {
                menuOpen2 = !menuOpen2
                ViewUtils.expand(license_hidden_view)
                license_fix_lay.setBackgroundResource(R.color.expande_colore)
                license_arrow.setImageResource(R.drawable.ic_arrow_circle_up)
            } else {
                menuOpen2 = !menuOpen2
                ViewUtils.collapse(license_hidden_view)
                license_fix_lay.setBackgroundResource(R.color.white)
                license_arrow.setImageResource(R.drawable.ic_arrow_circle_down)

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

        doc_fix_lay.setOnClickListener {
            if (menuOpen4) {
                menuOpen4 = !menuOpen4
                ViewUtils.expand(doc_hidden_view)
                doc_fix_lay.setBackgroundResource(R.color.expande_colore)
                doc_arrow.setImageResource(R.drawable.ic_arrow_circle_up)
            } else {
                menuOpen4 = !menuOpen4
                ViewUtils.collapse(doc_hidden_view)
                doc_fix_lay.setBackgroundResource(R.color.white)
                doc_arrow.setImageResource(R.drawable.ic_arrow_circle_down)
            }
        }

        mMenu.setOnClickListener {
            menuVisible()
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

        selectorBinding.delete.isVisible = naturalItem?.logoHref?.isNotEmpty() == true
        selectorBinding.upload.setOnClickListener {
            BaseActivity.profileLogoListner = this
            val result = FileUtil.checkCameraPermission(requireContext())
            if (result) {
                openCamera()
            } else {
                FileUtil.requestCamerasPermission(requireActivity())
            }
            bottomSheetDialog.dismiss()
        }
        selectorBinding.delete.setOnClickListener {
            lottieAnimationView?.visibility = View.VISIBLE
            naturalItem?.id.let {
                if (it != null) {
                    viewModel.deleteLogo(it)
                }
            }
            bottomSheetDialog.dismiss()
        }
        selectorBinding.Cancel.setOnClickListener {
            bottomSheetDialog.dismiss()
        }
    }

    private fun menuVisible() {

        if (menuOpen5) {
            menuOpen5 = !menuOpen5
            ViewUtils.collapse(mLinear)
            mMenu.setImageResource(R.drawable.ic_more_new)
        } else {
            menuOpen5 = !menuOpen5
            ViewUtils.expand(mLinear)
            mMenu.setImageResource(R.drawable.ic_more_new_done)
        }
    }

    override fun setObservers() {
        viewModel.naturalPersDetailsLiveData.observe(viewLifecycleOwner) { naturalItem ->
            progressbar?.dismissPopup()
            if (naturalItem != null) {
                this.naturalItem = naturalItem
                val address = naturalItem.address

                if (naturalItem.logoHref?.isNotEmpty() == true) {

                    val url = "${ApiModule.BASE_URL_RESOURCES}${naturalItem.logoHref}"
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

                firstName.text = naturalItem.firstName
                lastName.text = naturalItem.lastName
                email.setText(naturalItem.email)

                mCall.setOnClickListener {
                    viewModel.openDialPad(naturalItem)
                }

                mEmail.setOnClickListener {
                    viewModel.openComposedMail(naturalItem)
                }

                var dateStr: String? = null
                if (naturalItem.dateOfBirth?.isNotEmpty() == true) {
                    val dateFormat: DateFormat =
                        SimpleDateFormat("yyyy-MM-dd")
                    val date: Date? =
                        dateFormat.parse(naturalItem.dateOfBirth)
                    val formatter: DateFormat =
                        SimpleDateFormat("dd/MM/yyyy")
                    dateStr =
                        formatter.format(date)
                }

                dob.setText(dateStr)

                val identityDocument = naturalItem.identityDocument
                type.setText(identityDocument?.documentType?.name)
                series.setText(identityDocument?.series)
                number.setText(identityDocument?.number)
                if (identityDocument?.expirationDate.isNullOrEmpty()) {
                    exp.setText("")
                } else {
                    exp.setText(identityDocument?.expirationDate.toString())
                }
                cnp.setText(naturalItem.cnp)

                val drivingLicense = naturalItem.drivingLicense
                drivingLicense?.licenseId?.let { lid.setText(it) }
                drivingLicense?.issueDate?.let {
                    lissue_date.setText(viewModel.convertFromServerDate(it).toString())
                }
                drivingLicense?.expirationDate?.let {
                    exp_date.setText(viewModel.convertFromServerDate(it).toString())
                }

                dlAttachment = naturalItem.drivingLicenseAttachment

                if (dlAttachment?.href.isNullOrEmpty()) {
                    lblCertificate.visibility = View.GONE
                    btnDeleteCertificate.visibility = View.GONE
                    iv_doc.setImageDrawable(requireContext().resources.getDrawable(R.drawable.ic_document_upload))
                } else {
                    lblCertificate.visibility = View.VISIBLE
                    btnDeleteCertificate.visibility = View.VISIBLE
                    lblCertificate.text = dlAttachment?.name
                    iv_doc.setImageDrawable(requireContext().resources.getDrawable(R.drawable.ic_pdf_selected))
                }

                idAttachment = naturalItem.identityDocumentAttachment
                if (idAttachment?.href.isNullOrEmpty()) {
                    lblId.visibility = View.GONE
                    btnDeleteId.visibility = View.GONE
                    iv_cnp.setImageDrawable(requireContext().resources.getDrawable(R.drawable.ic_document_upload))
                } else {
                    lblId.visibility = View.VISIBLE
                    btnDeleteId.visibility = View.VISIBLE
                    lblId.text = idAttachment?.name
                    iv_cnp.setImageDrawable(requireContext().resources.getDrawable(R.drawable.ic_pdf_selected))
                }

                lblCertificate.setOnClickListener {
                    dlAttachment?.href?.let { it1 ->
                        val url = ApiModule.BASE_URL_RESOURCES + it1
                        val intent = Intent(requireContext(), PdfActivity::class.java)
                        intent.putExtra(PdfActivity.ARG_DATA, url)
                        intent.putExtra(PdfActivity.ARG_FROM, "DOCUMENT")
                        requireContext().startActivity(intent)
                    }
                }

                lblId.setOnClickListener {
                    idAttachment?.href?.let { it1 ->
                        val url = ApiModule.BASE_URL_RESOURCES + it1
                        val intent = Intent(requireContext(), PdfActivity::class.java)
                        intent.putExtra(PdfActivity.ARG_DATA, url)
                        intent.putExtra(PdfActivity.ARG_FROM, "DOCUMENT")
                        requireContext().startActivity(intent)
                    }
                }

                //  (address) Object
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

        }


        viewModel.countryData.observe(viewLifecycleOwner) { countryData ->
            if (countryData != null) {
                this.countriesList = countryData
                val address = naturalItem?.address
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
                this.streetTypeList = streetTypeData
            }
        }

        viewModel.licenseCategoryData.observe(viewLifecycleOwner) { licenseCategoryData ->
            if (licenseCategoryData != null) {
                this.licenseCatList = licenseCategoryData

                val categorylist = ArrayList<String>()
                val drivingLicense = naturalItem?.drivingLicense
                if (drivingLicense?.vehicleCategories?.isNotEmpty() == true) {
                    for (i in drivingLicense.vehicleCategories.indices) {
                        val catelog = drivingLicense.vehicleCategories[i].let {
                            CatalogUtils.findById(
                                licenseCategoryData,
                                it.toLong()
                            )
                        }
                        catelog?.name?.let { categorylist.add(it) }
                    }
                    categorylist.let { lcat.setText(it.joinToString(", ")) }
                }
            }
        }

        viewModel.actionStream.observe(viewLifecycleOwner) {
            when (it) {
                is NaturalPersonDetailsViewModel.ACTION.OnDeleteSuccess -> onDeleteSuccess(it.attachType)
                is NaturalPersonDetailsViewModel.ACTION.OnUploadSuccess -> onUploadSuccess(
                    it.attachType,
                    it.attachment
                )
                is NaturalPersonDetailsViewModel.ACTION.OnDeleteLogo -> {
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
        if (naturalItem != null) {
            val pos = naturalItem?.phoneCountryCode?.let { it1 ->
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

        if (naturalItem?.phone.isNullOrEmpty()) {
            phone.setText("")
        } else {
            phone.setText("+${romanCode?.value}${naturalItem?.phone}")
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

    private fun openCamera() {
        CropImage.activity().setAllowFlipping(false).setAllowRotation(false).setAspectRatio(1, 1)
            .setCropShape(CropImageView.CropShape.OVAL).start(requireActivity())
    }


    private fun callFileManagerForLicense() {
        try {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/pdf"
            }
            startActivityForResult(intent, 101)
        } catch (e: ActivityNotFoundException) {
        }
    }

    private fun callFileManagerForID() {
        try {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/pdf"
            }
            startActivityForResult(intent, 102)
        } catch (e: ActivityNotFoundException) {
        }
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 101 && resultCode == Activity.RESULT_OK) {
            if (data != null && data.data != null) {
                val selectedUri: Uri? = data.data
                var selectedFile: String? = null
                if (selectedUri != null) {
                    selectedFile = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        FileUtil.getFilePathFor11(requireContext(), selectedUri)
                    } else {
                        FileUtil.getPath(selectedUri, requireContext())
                    }
                }
                if (selectedFile != null) {
                    lblCertificate.visibility = View.VISIBLE
                    btnDeleteCertificate.visibility = View.VISIBLE
                    val dlFile = File(selectedFile)
                    naturalItem?.id?.let { viewModel.onAttach(it, "DRIVING_LICENSE", dlFile) }
                }
            }
        }

        if (requestCode == 102 && resultCode == Activity.RESULT_OK) {
            if (data != null && data.data != null) {
                val selectedUri: Uri? = data.data
                var selectedFile: String? = null
                if (selectedUri != null) {
                    selectedFile = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        FileUtil.getFilePathFor11(requireContext(), selectedUri)
                    } else {
                        FileUtil.getPath(selectedUri, requireContext())
                    }
                }
                if (selectedFile != null) {
                    lblId.visibility = View.VISIBLE
                    btnDeleteId.visibility = View.VISIBLE
                    val idFile = File(selectedFile)
                    naturalItem?.id?.let { viewModel.onAttach(it, "IDENTITY_DOCUMENT", idFile) }
                }
            }
        }

        if (requestCode == 1001 && resultCode == Activity.RESULT_OK) {
            callFileManagerForLicense()
        }

        if (requestCode == 1002 && resultCode == Activity.RESULT_OK) {
            callFileManagerForID()
        }

        if (requestCode == 2296) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    callFileManagerForID()
                }
            }
        }
        if (requestCode == 2297) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    callFileManagerForLicense()
                }
            }
        }

    }

    private fun onDeleteSuccess(attachType: String) {
        if (attachType == "DRIVING_LICENSE") {
            lblCertificate.visibility = View.GONE
            btnDeleteCertificate.visibility = View.GONE
            iv_doc.setImageDrawable(requireContext().resources.getDrawable(R.drawable.ic_document_upload))
        } else {
            lblId.visibility = View.GONE
            btnDeleteId.visibility = View.GONE
            iv_cnp.setImageDrawable(requireContext().resources.getDrawable(R.drawable.ic_document_upload))
        }
    }

    private fun onUploadSuccess(attachType: String, attachments: Attachments) {
        if (attachType == "DRIVING_LICENSE") {
            lblCertificate.visibility = View.VISIBLE
            btnDeleteCertificate.visibility = View.VISIBLE
            lblCertificate.text = attachments.name
            iv_doc.setImageDrawable(requireContext().resources.getDrawable(R.drawable.ic_pdf_selected))
        } else {
            lblId.visibility = View.VISIBLE
            btnDeleteId.visibility = View.VISIBLE
            lblId.text = attachments.name
            iv_cnp.setImageDrawable(requireContext().resources.getDrawable(R.drawable.ic_pdf_selected))
        }
    }

    override fun onResume() {
        super.onResume()
        progressbar?.dismissPopup()
        arguments?.let {
            naturalPerson = it.getSerializable(ARG_NATURAL_PERSON) as? NaturalPerson?
            viewModel.onReceivedPerson(naturalPerson?.id?.toInt())
        }

        if (naturalPerson?.id?.toInt() == MainActivity.activeId) {
            delete.visibility = View.GONE
        } else {
            delete.visibility = View.VISIBLE
        }

    }

    override fun onChangeLogo(uri: Uri) {
        val selectedFile = uri.let {
            FileUtil.getPath(it, requireContext())
        }
        if (selectedFile != null) {
            naturalItem?.id.let {
                if (it != null) {
                    viewModel.onAddLogo(it, File(selectedFile))
                }
            }
        }
    }
}