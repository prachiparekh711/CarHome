package ro.westaco.carhome.presentation.screens.settings.data.person_natural.details

import android.app.Activity
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_natural_person_details.*
import okhttp3.ResponseBody
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.responses.models.*
import ro.westaco.carhome.databinding.DialogDeleteNaturalPersonBinding
import ro.westaco.carhome.di.ApiModule
import ro.westaco.carhome.presentation.base.BaseFragment
import ro.westaco.carhome.presentation.common.DeleteDialogFragment
import ro.westaco.carhome.presentation.screens.main.MainActivity
import ro.westaco.carhome.utils.*
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

//C- Rebuilt Section
@AndroidEntryPoint
class NaturalPersonDetailsFragment : BaseFragment<NaturalPersonDetailsViewModel>() {
    private var naturalPerson: NaturalPerson? = null
    lateinit var bottomSheet: BottomSheetDialog
    private var menuOpen = false
    private var menuOpen1 = false
    private var menuOpen2 = false
    private var menuOpen3 = false
    private var menuOpen4 = false
    private var menuOpen5 = false
    var countriesList: ArrayList<Country> = ArrayList()
    var sirutaList: ArrayList<Siruta> = ArrayList()
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
        mContext = requireContext()
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

            /*naturalItem?.drivingLicenseAttachment?.id?.let { it1 ->
                naturalItem?.id?.let { it2 ->
                    viewModel.onDeleteAttachment(
                        it2,
                        it1,
                        requireContext().getString(R.string.attchment_dl)
                    )
                }
            }*/

            val DrivingLicenseDialog = DeleteDialogFragment()
            DrivingLicenseDialog.layoutResId = R.layout.n_person_delete_doc
            DrivingLicenseDialog.listener =
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

                        DrivingLicenseDialog.dismiss()
                    }
                }

            DrivingLicenseDialog.show(childFragmentManager, DeleteDialogFragment.TAG)
        }

        btnDeleteId.setOnClickListener {

            /*naturalItem?.identityDocumentAttachment?.id?.let { it1 ->
                naturalItem?.id?.let { it2 ->
                    viewModel.onDeleteAttachment(
                        it2,
                        it1,
                        requireContext().getString(R.string.attchment_id)
                    )
                }
            }*/

            val CNPDialog = DeleteDialogFragment()
            CNPDialog.layoutResId = R.layout.n_person_delete_doc_id
            CNPDialog.listener = object : DeleteDialogFragment.OnDialogInteractionListener {
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
                    CNPDialog.dismiss()
                }
            }

            CNPDialog.show(childFragmentManager, DeleteDialogFragment.TAG)

        }

        avatar.setOnClickListener {
            val result = FileUtil.checkPermission(requireContext())
            if (result) {
                openGallery()
            } else {
                FileUtil.requestPermission(requireActivity())
            }
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
            if (naturalItem != null) {
                this.naturalItem = naturalItem
                viewModel.fetchDefaultData()

                firstName.text = naturalItem.firstName
                lastName.text = naturalItem.lastName
                email.setText(naturalItem.email)
                if (naturalItem.phone.isNullOrEmpty()) {
                    phone.setText("")
                } else {
                    phone.setText("+" + naturalItem.phone)
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
                headerFullname.text =
                    "${naturalItem.firstName ?: ""}  ${naturalItem.lastName ?: ""}"
                headerEmail.text = naturalItem.email ?: ""

                //  (address) Object
                street_locality.setText(naturalItem.address?.locality)
                street_type.setText(naturalItem.address?.streetType?.name)
                street_name.setText(naturalItem.address?.streetName)
                street_number.setText(naturalItem.address?.buildingNo)
                street_block.setText(naturalItem.address?.block)
                street_entrance.setText(naturalItem.address?.entrance)
                street_apartment.setText(naturalItem.address?.apartment)
                street_floor.setText(naturalItem.address?.floor)
                street_zipcode.setText(naturalItem.address?.zipCode)

                //  (identityDocument) Object
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

                //  (drivingLicenseAttachment) Object
                dlAttachment = naturalItem.drivingLicenseAttachment
                if (dlAttachment?.href.isNullOrEmpty()) {
                    lblCertificate.visibility = View.GONE
                    btnDeleteCertificate.visibility = View.GONE
                } else {
                    lblCertificate.visibility = View.VISIBLE
                    btnDeleteCertificate.visibility = View.VISIBLE
                    lblCertificate.text = dlAttachment?.name
                }

                //  (identityDocumentAttachment) Object
                idAttachment = naturalItem.identityDocumentAttachment
                if (idAttachment?.href.isNullOrEmpty()) {
                    lblId.visibility = View.GONE
                    btnDeleteId.visibility = View.GONE
                } else {
                    lblId.visibility = View.VISIBLE
                    btnDeleteId.visibility = View.VISIBLE
                    lblId.text = idAttachment?.name
                }

                lblCertificate.setOnClickListener {
                    dlAttachment?.href?.let { it ->
                        progressbar?.showPopup()
                        val baseUrl = ApiModule.BASE_URL_RESOURCES + it
                        viewModel.fetchData(baseUrl)
                    }
                }

                lblId.setOnClickListener {
                    idAttachment?.href?.let { it ->
                        progressbar?.showPopup()
                        val baseUrl = ApiModule.BASE_URL_RESOURCES + it
                        viewModel.fetchData(baseUrl)
                    }
                }
            }
            progressbar?.dismissPopup()
        }

        viewModel.naturalLogoData?.observe(viewLifecycleOwner) { naturalLogo ->
            if (naturalLogo != null) {
                val options = RequestOptions()
                avatar.clipToOutline = true
                Glide.with(requireContext())
                    .load(naturalLogo)
                    .apply(
                        options.centerCrop()
                            .skipMemoryCache(true)
                            .priority(Priority.HIGH)
                            .format(DecodeFormat.PREFER_ARGB_8888)
                    )
                    .into(avatar)
            }
            lottieAnimationView?.visibility = View.GONE
        }

        viewModel.attachmentData.observe(viewLifecycleOwner) { attachmentData ->

            progressbar?.dismissPopup()


            /*     if (attachmentData != null) {
                     val dir: File
                     val root = Environment.getExternalStorageDirectory().absolutePath.toString()
                     val myDir = File(root, "DCIM")
                     myDir.mkdirs()

                     dir = File(myDir, context?.resources?.getString(R.string.app_name))
                     if (!dir.exists()) {
                         dir.mkdirs()
                     }
                     saveFile(
                         attachmentData,
                         dir.absolutePath + "/Attachment_" + System.currentTimeMillis() + ".pdf"
                     )
                 }*/
        }

        viewModel.countryData.observe(viewLifecycleOwner) { countryData ->
            this.countriesList = countryData
            val address = naturalItem?.address
            for (i in countriesList.indices) {
                if (countriesList[i].code == address?.countryCode) {
                    mCountry.text = countriesList[i].name
                    flg.setImageResource(
                        CountryCityUtils.getFlagDrawableResId(
                            countriesList[i].twoLetterCode.lowercase(
                                Locale.getDefault()
                            )
                        )
                    )
                }
            }
        }

        viewModel.sirutaData.observe(viewLifecycleOwner) { sirutaData ->
            this.sirutaList = sirutaData
            val address = naturalItem?.address
            if (address?.countryCode == "ROU") {
                locality.isVisible = true
                spinnerCounty.isVisible = true
                in_filled_county.visibility = View.GONE
                in_filled_locality.visibility = View.GONE

                for (i in sirutaList.indices) {
                    if (sirutaList[i].code == address.sirutaCode) {
                        county.setText(sirutaList[i].name)
                    }
                }

                for (i in sirutaList.indices) {
                    if (address.locality == sirutaList[i].name) {
                        street_locality.setText(sirutaList[i].name)
                    }
                }
            } else {
                locality.isVisible = false
                spinnerCounty.isVisible = false
                in_filled_county.visibility = View.VISIBLE
                in_filled_locality.visibility = View.VISIBLE
                filled_county.setText(address?.region)
                filled_locality.setText(address?.locality)
            }
        }

        viewModel.streetTypeData.observe(viewLifecycleOwner) { streetTypeData ->
            this.streetTypeList = streetTypeData
        }

        viewModel.licenseCategoryData.observe(viewLifecycleOwner) { licenseCategoryData ->
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

        viewModel.actionStream.observe(viewLifecycleOwner) {
            when (it) {
                is NaturalPersonDetailsViewModel.ACTION.onDeleteSuccess -> onDeleteSuccess(it.attachType)
                is NaturalPersonDetailsViewModel.ACTION.onUploadSuccess -> onUploadSuccess(
                    it.attachType,
                    it.attachment
                )
            }
        }
    }

    fun saveFile(body: ResponseBody?, pathWhereYouWantToSaveFile: String): String {
        if (body == null)
            return ""
        var input: InputStream? = null
        try {
            input = body.byteStream()
            //val file = File(getCacheDir(), "cacheFileAppeal.srl")
            val fos = FileOutputStream(pathWhereYouWantToSaveFile)
            fos.use { output ->
                val buffer = ByteArray(4 * 1024) // or other buffer size
                var read: Int
                while (input.read(buffer).also { read = it } != -1) {
                    output.write(buffer, 0, read)
                }
                output.flush()
            }
            Toast.makeText(
                requireContext(),
                resources.getString(R.string.dwld_success),
                Toast.LENGTH_SHORT
            ).show()
            return pathWhereYouWantToSaveFile
        } catch (e: Exception) {
            Toast.makeText(
                requireContext(),
                resources.getString(R.string.dwld_error),
                Toast.LENGTH_SHORT
            ).show()
        } finally {
            input?.close()

        }
        return ""
    }

    var mContext: Context? = null

    private fun openGallery() {
        val imageUri: Uri? = null
        val intent = Intent()
        intent.action = Intent.ACTION_PICK
        intent.type = "image/png"
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, imageUri)
        startActivityForResult(intent, 111)
    }

    fun callFileManagerForLicense() {
        try {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/pdf"
            }
            startActivityForResult(intent, 101)
        } catch (e: ActivityNotFoundException) {
        }
    }

    fun callFileManagerForID() {
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

        if (requestCode == 111 && resultCode == Activity.RESULT_OK) {
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
                    naturalItem?.id.let {
                        if (it != null) {
                            viewModel.onAddLogo(it, File(selectedFile))
                        }
                    }
                }
            }
        }

        if (requestCode == 1000 && resultCode == Activity.RESULT_OK) {
            openGallery()
        }


    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray,
    ) {
        when (requestCode) {
            200 -> if (grantResults.size > 0) {
                val READ_EXTERNAL_STORAGE = grantResults[0] == PackageManager.PERMISSION_GRANTED
                val WRITE_EXTERNAL_STORAGE = grantResults[1] == PackageManager.PERMISSION_GRANTED
                if (READ_EXTERNAL_STORAGE && WRITE_EXTERNAL_STORAGE) {
                    // perform action when allow permission success
                } else {
                    Toast.makeText(
                        requireContext(),
                        requireContext().resources.getString(R.string.allow_permission),
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }
        }
    }

    fun getFilename(filePath: String): String {
        val file = File(filePath)
        return file.name
    }

    fun onDeleteSuccess(attachType: String) {
        if (attachType.equals("DRIVING_LICENSE")) {
            lblCertificate.visibility = View.GONE
            btnDeleteCertificate.visibility = View.GONE
        } else {
            lblId.visibility = View.GONE
            btnDeleteId.visibility = View.GONE
        }
    }

    fun onUploadSuccess(attachType: String, attachments: Attachments) {
        if (attachType == "DRIVING_LICENSE") {
            lblCertificate.visibility = View.VISIBLE
            btnDeleteCertificate.visibility = View.VISIBLE
            lblCertificate.text = attachments.name
        } else {
            lblId.visibility = View.VISIBLE
            btnDeleteId.visibility = View.VISIBLE
            lblId.text = attachments.name
        }
    }

    override fun onResume() {
        super.onResume()
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
}