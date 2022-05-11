package ro.westaco.carhome.presentation.screens.dashboard.profile

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.request.RequestOptions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_profile.*
import okhttp3.ResponseBody
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.responses.models.Attachments
import ro.westaco.carhome.data.sources.remote.responses.models.CatalogItem
import ro.westaco.carhome.data.sources.remote.responses.models.Country
import ro.westaco.carhome.data.sources.remote.responses.models.ProfileItem
import ro.westaco.carhome.di.ApiModule
import ro.westaco.carhome.presentation.base.BaseFragment
import ro.westaco.carhome.presentation.common.DeleteDialogFragment
import ro.westaco.carhome.utils.CatalogUtils
import ro.westaco.carhome.utils.FileUtil
import ro.westaco.carhome.utils.FileUtil.Companion.checkPermission
import ro.westaco.carhome.utils.FileUtil.Companion.requestPermission
import ro.westaco.carhome.utils.Progressbar
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream


//C- Profile Section
@AndroidEntryPoint
class ProfileFragment : BaseFragment<ProfiledetailsViewModel>() {

    //    ProfiledetailsViewModel (S2)

    override fun getContentView() = R.layout.fragment_profile

    var dlAttachment: Attachments? = null
    var idAttachment: Attachments? = null
    var profileItem: ProfileItem? = null
    var progressbar: Progressbar? = null
    var lottieAnimationView: LottieAnimationView? = null
    var countries: java.util.ArrayList<Country> = java.util.ArrayList()
    var licenseCatList: java.util.ArrayList<CatalogItem> = java.util.ArrayList()

    companion object {
        var profileId: Int? = null
    }

    override fun getStatusBarColor() = ContextCompat.getColor(requireContext(), R.color.white)

    override fun initUi() {
        mContext = requireContext()
        progressbar = Progressbar(requireContext())
        progressbar?.showPopup()
        lottieAnimationView = lottieAnimation
        lottieAnimationView?.visibility = View.VISIBLE
        lblStepCount.text = requireContext().resources.getString(R.string.step_profile, 0, 0)
        back.setOnClickListener {
            viewModel.onBack()
        }

        home.setOnClickListener {
            viewModel.onMain()
        }

        llUploadCertificate.setOnClickListener {
            val result = checkPermission(requireContext())
            if (result) {
                callFileManagerForLicense()
            } else {
                requestPermission(requireActivity())
            }
        }


        llUploadId.setOnClickListener {
            val result = checkPermission(requireContext())
            if (result) {
                callFileManagerForID()
            } else {
                requestPermission(requireActivity())
            }
        }

        btnDeleteCertificate.setOnClickListener {

            /*profileItem?.drivingLicenseAttachment?.id?.let { it1 ->
                viewModel.onDeleteAttachment(
                    it1,
                    requireContext().getString(R.string.attchment_dl)
                )
            }*/

            val DrivingLicenseDialog = DeleteDialogFragment()
            DrivingLicenseDialog.layoutResId = R.layout.n_person_delete_doc
            DrivingLicenseDialog.listener =
                object : DeleteDialogFragment.OnDialogInteractionListener {
                    override fun onPosClicked() {

                        profileItem?.drivingLicenseAttachment?.id?.let { it1 ->
                            viewModel.onDeleteAttachment(
                                it1,
                                requireContext().getString(R.string.attchment_dl)
                            )
                        }

                        DrivingLicenseDialog.dismiss()
                    }
                }

            DrivingLicenseDialog.show(childFragmentManager, DeleteDialogFragment.TAG)
        }

        btnDeleteId.setOnClickListener {

            val CNPDialog = DeleteDialogFragment()
            CNPDialog.layoutResId = R.layout.n_person_delete_doc_id
            CNPDialog.listener = object : DeleteDialogFragment.OnDialogInteractionListener {
                override fun onPosClicked() {

                    profileItem?.identityDocumentAttachment?.id?.let { it1 ->
                        viewModel.onDeleteAttachment(
                            it1,
                            requireContext().getString(R.string.attchment_id)
                        )
                    }

                    CNPDialog.dismiss()
                }
            }

            CNPDialog.show(childFragmentManager, DeleteDialogFragment.TAG)
        }

        avatar.setOnClickListener {
            val result = checkPermission(requireContext())
            if (result) {
                openGallery()
            } else {
                requestPermission(requireActivity())
            }
        }

        delete.setOnClickListener {
            viewModel.onCloseAccount()
        }
    }

    override fun setObservers() {
//*        *** Do not remove this code(S2) ***
//        Profile data fetch by FirebaseUser via  authentication

//        viewModel.userLiveData.observe(viewLifecycleOwner, { user ->
//            if (user != null) {
//                headerFullname.text = user.displayName
//                headerEmail.text = user.email
//
//                email.text = user.email
//                phone.text = user.phoneNumber
//
//                val imageUrl = viewModel.getProfileImage(requireContext(), user)
//                Picasso.with(context)
//                    .load(imageUrl)
//                    .transform(CropCircleTransformation())
//                    .placeholder(R.drawable.default_avatar)
//                    .into(avatar)
//            }
//        })

//*        *** Do not remove this code(S2) ***


        viewModel.profileLiveData?.observe(viewLifecycleOwner) { profileItem ->
            if (profileItem != null) {
                this.profileItem = profileItem

                profileId = profileItem.id
                if (profileItem.stepDone != profileItem.stepTotal) {
                    lblStepCount.text = requireContext().resources.getString(
                        R.string.step_profile,
                        profileItem.stepDone,
                        profileItem.stepTotal
                    )
                    progressBar.max = profileItem.stepTotal ?: 0
                    progressBar.progress = profileItem.stepDone ?: 0
                    progressBar.isVisible = true
                    stepLL.isVisible = true
                } else {
                    progressBar.isVisible = false
                    stepLL.isVisible = false
                }
                firstName.text = profileItem.firstName
                lastName.text = profileItem.lastName
                phone.text = profileItem.phone
                dob.text = profileItem.dateOfBirth
                headerFullname.text =
                    "${profileItem.firstName ?: ""}  ${profileItem.lastName ?: ""}"
                headerEmail.text = profileItem.email ?: ""

                //  (address) Object
                address1.text = profileItem.address?.addressDetail

                //  (identityDocument) Object
                val identityDocument = profileItem.identityDocument
                type.text = identityDocument?.documentType?.name
                series.text = identityDocument?.series
                number.text = identityDocument?.number
                exp.text = identityDocument?.expirationDate
                cnp.text = profileItem.cnp

                val drivingLicense = profileItem.drivingLicense
                drivingLicense?.licenseId?.let { lid.text = it }
                drivingLicense?.issueDate?.let {
                    lissue_date.text = viewModel.convertFromServerDate(it)
                }
                drivingLicense?.expirationDate?.let {
                    exp_date.text = viewModel.convertFromServerDate(it)
                }

                //  (drivingLicenseAttachment) Object
                dlAttachment = profileItem.drivingLicenseAttachment
                if (dlAttachment?.href.isNullOrEmpty()) {
                    llUploadCertificate.visibility = View.VISIBLE
                    llCertificate.visibility = View.GONE
                } else {
                    llUploadCertificate.visibility = View.GONE
                    btnDeleteCertificate.visibility = View.VISIBLE
                    llCertificate.visibility = View.VISIBLE
                    lblCertificate.text = dlAttachment?.name
                }

                //  (identityDocumentAttachment) Object
                idAttachment = profileItem.identityDocumentAttachment
                if (idAttachment?.href.isNullOrEmpty()) {
                    llUploadId.visibility = View.VISIBLE
                    llId.visibility = View.GONE
                } else {
                    llUploadId.visibility = View.GONE
                    btnDeleteId.visibility = View.VISIBLE
                    llId.visibility = View.VISIBLE
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

                cta.setOnClickListener {
                    viewModel.onEditAccount(profileItem)
                }

            }
            progressbar?.dismissPopup()
        }

        viewModel.profileLogoData?.observe(viewLifecycleOwner) { profileLogo ->
            if (profileLogo != null) {
                val options = RequestOptions()
                avatar.clipToOutline = true
                Glide.with(requireContext())
                    .load(profileLogo)
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

//            if (attachmentData != null) {
//                val dir: File
//                val root = Environment.getExternalStorageDirectory().absolutePath.toString()
//                val myDir = File(root, "DCIM")
//                myDir.mkdirs()
//
//                dir = File(myDir, context?.resources?.getString(R.string.app_name))
//                if (!dir.exists()) {
//                    dir.mkdirs()
//                }
//                saveFile(
//                    attachmentData,
//                    dir.absolutePath + "/Attachment_" + System.currentTimeMillis() + ".pdf"
//                )
//            }
        }

        viewModel.countryData.observe(viewLifecycleOwner) { countryData ->
            this.countries = countryData
        }

        viewModel.licenseCategoryData.observe(viewLifecycleOwner) { licenseCategoryData ->
            this.licenseCatList = licenseCategoryData
            val drivingLicense = profileItem?.drivingLicense
            val categorylist = ArrayList<String>()
            if (drivingLicense?.vehicleCategories?.isNotEmpty() == true) {
                for (i in drivingLicense.vehicleCategories.indices) {
                    val catelog = CatalogUtils.findById(
                        licenseCategoryData,
                        drivingLicense.vehicleCategories[i].toLong()
                    )
                    catelog?.name?.let { categorylist.add(it) }
                }
                categorylist.let { lcat.text = it.joinToString(", ") }
            }

        }

        viewModel.actionStream.observe(viewLifecycleOwner) {
            when (it) {
                is ProfiledetailsViewModel.ACTION.onDeleteSuccess -> onDeleteSuccess(it.attachType)
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


    fun openGallery() {
        var imageUri: Uri? = null
        val intent = Intent()
        intent.action = Intent.ACTION_PICK
        intent.type = "image/png"
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, imageUri)
        startActivityForResult(intent, 111)
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
                    llUploadCertificate.visibility = View.GONE
                    btnDeleteCertificate.visibility = View.INVISIBLE
                    llCertificate.visibility = View.VISIBLE
                    val dlFile = File(selectedFile)
                    lblCertificate.text = requireContext().resources.getString(R.string.uploading_)
                    viewModel.onAttach("DRIVING_LICENSE", dlFile)
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
                    llUploadId.visibility = View.GONE
                    btnDeleteId.visibility = View.INVISIBLE
                    llId.visibility = View.VISIBLE
                    val idFile = File(selectedFile)
                    lblId.text = requireContext().resources.getString(R.string.uploading_)
                    viewModel.onAttach("IDENTITY_DOCUMENT", idFile)
                }
            }
        }

        if (requestCode == 1001 && resultCode == Activity.RESULT_OK) {
            callFileManagerForLicense()
        }

        if (requestCode == 1002 && resultCode == Activity.RESULT_OK) {
            callFileManagerForID()
        }


        if (requestCode == 111 && resultCode == Activity.RESULT_OK) {
            if (data != null && data.data != null) {

                val selectedUri: Uri? = data.data
                val selectedFile = selectedUri?.let {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        FileUtil.getFilePathFor11(requireContext(), it)
                    } else {
                        FileUtil.getPath(it, requireContext())
                    }
                }
                if (selectedFile != null) {
                    viewModel.onAddLogo(File(selectedFile))
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
        grantResults: IntArray
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


    fun onDeleteSuccess(attachType: String) {
        if (attachType.equals("DRIVING_LICENSE")) {
            /*drivingLicenseTV?.isVisible = false
            mDeleteDL?.isVisible = false
            drivingLicenseUploadImg?.isVisible = true*/
            llUploadCertificate.visibility = View.VISIBLE
            llCertificate.visibility = View.GONE
        } else {
            llUploadId.visibility = View.VISIBLE
            llId.visibility = View.GONE
        }
    }


}