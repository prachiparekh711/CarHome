package ro.westaco.carhome.presentation.screens.dashboard.profile

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_profile.*
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.responses.models.Attachments
import ro.westaco.carhome.data.sources.remote.responses.models.CatalogItem
import ro.westaco.carhome.data.sources.remote.responses.models.Country
import ro.westaco.carhome.data.sources.remote.responses.models.ProfileItem
import ro.westaco.carhome.databinding.LogoOptionLayoutBinding
import ro.westaco.carhome.di.ApiModule
import ro.westaco.carhome.presentation.base.BaseActivity
import ro.westaco.carhome.presentation.base.BaseActivity.Companion.profileLogoListner
import ro.westaco.carhome.presentation.base.BaseFragment
import ro.westaco.carhome.presentation.common.DeleteDialogFragment
import ro.westaco.carhome.presentation.screens.home.PdfActivity
import ro.westaco.carhome.presentation.screens.main.MainActivity
import ro.westaco.carhome.utils.CatalogUtils
import ro.westaco.carhome.utils.DialogUtils.Companion.showErrorInfo
import ro.westaco.carhome.utils.FileUtil
import ro.westaco.carhome.utils.FileUtil.Companion.checkCameraPermission
import ro.westaco.carhome.utils.FileUtil.Companion.checkPermission
import ro.westaco.carhome.utils.FileUtil.Companion.requestCamerasPermission
import ro.westaco.carhome.utils.FileUtil.Companion.requestPermission
import ro.westaco.carhome.utils.Progressbar
import java.io.File


//C- Profile Section
@AndroidEntryPoint
class ProfileFragment : BaseFragment<ProfileDetailsViewModel>(),
    BaseActivity.OnProfileLogoChangeListner {

    override fun getContentView() = R.layout.fragment_profile

    var dlAttachment: Attachments? = null
    var idAttachment: Attachments? = null
    var profileItem: ProfileItem? = null
    var progressbar: Progressbar? = null
    var lottieAnimationView: LottieAnimationView? = null
    var countries: java.util.ArrayList<Country> = java.util.ArrayList()
    var licenseCatList: java.util.ArrayList<CatalogItem> = java.util.ArrayList()
    var profileLogoExist = false

    override fun getStatusBarColor() = ContextCompat.getColor(requireContext(), R.color.white)

    override fun onResume() {
        super.onResume()
        progressbar?.dismissPopup()
    }

    override fun initUi() {

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

            val drivingLicenseDialog = DeleteDialogFragment()
            drivingLicenseDialog.layoutResId = R.layout.n_person_delete_doc
            drivingLicenseDialog.listener =
                object : DeleteDialogFragment.OnDialogInteractionListener {
                    override fun onPosClicked() {

                        profileItem?.drivingLicenseAttachment?.id?.let { it1 ->
                            viewModel.onDeleteAttachment(
                                it1,
                                requireContext().getString(R.string.attchment_dl)
                            )
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

                    profileItem?.identityDocumentAttachment?.id?.let { it1 ->
                        viewModel.onDeleteAttachment(
                            it1,
                            requireContext().getString(R.string.attchment_id)
                        )
                    }

                    cnpDialog.dismiss()
                }
            }

            cnpDialog.show(childFragmentManager, DeleteDialogFragment.TAG)
        }

        avatar.setOnClickListener {
            logoOperationDialog()
        }

        delete.setOnClickListener {
            viewModel.onCloseAccount()
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

        selectorBinding.delete.isVisible = profileLogoExist

        selectorBinding.upload.setOnClickListener {
            profileLogoListner = this
            val result = checkCameraPermission(requireContext())
            if (result) {
                openCamera()
            } else {
                requestCamerasPermission(requireActivity())
            }
            bottomSheetDialog.dismiss()
        }
        selectorBinding.delete.setOnClickListener {
            lottieAnimationView?.visibility = View.VISIBLE
            viewModel.deleteLogo()
            bottomSheetDialog.dismiss()
        }
        selectorBinding.Cancel.setOnClickListener {
            bottomSheetDialog.dismiss()
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
                MainActivity.profileItem = profileItem
                this.profileItem = profileItem

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
                lissue_date.text = drivingLicense?.issueDate
                exp_date.text = drivingLicense?.expirationDate

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

                editProfile.setOnClickListener {
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
                profileLogoExist = true
            } else {
                profileLogoExist = false
            }
            lottieAnimationView?.visibility = View.GONE

        }

        viewModel.countryData.observe(viewLifecycleOwner) { countryData ->
            if (countryData != null) {
                this.countries = countryData
            }
        }

        viewModel.licenseCategoryData.observe(viewLifecycleOwner) { licenseCategoryData ->
            if (licenseCategoryData != null) {
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
                    categorylist.sort()
                    categorylist.let { lcat.text = it.joinToString(", ") }
                }
            }
        }

        viewModel.actionStream.observe(viewLifecycleOwner) {
            when (it) {
                is ProfileDetailsViewModel.ACTION.OnDeleteSuccess -> onDeleteSuccess(it.attachType)
                is ProfileDetailsViewModel.ACTION.OnDeleteLogo -> {
                    lottieAnimationView?.visibility = View.GONE
                    avatar.setImageDrawable(requireContext().resources.getDrawable(R.drawable.default_avatar))
                }
            }
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


    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        when (requestCode) {

            201 -> if (grantResults.isNotEmpty()) {
                val camera = grantResults[0] == PackageManager.PERMISSION_GRANTED
                if (camera) {
                    openCamera()
                } else {
                    showErrorInfo(
                        requireContext(),
                        getString(R.string.crop_image_activity_no_permissions)
                    )
                }
            }
        }
    }


    private fun onDeleteSuccess(attachType: String) {
        if (attachType == "DRIVING_LICENSE") {
            llUploadCertificate.visibility = View.VISIBLE
            llCertificate.visibility = View.GONE
        } else {
            llUploadId.visibility = View.VISIBLE
            llId.visibility = View.GONE
        }
    }

    override fun onChangeLogo(uri: Uri) {
        val selectedFile = uri.let {
            FileUtil.getPath(it, requireContext())
        }
        if (selectedFile != null) {
            lottieAnimationView?.visibility = View.VISIBLE
            viewModel.onAddLogo(File(selectedFile))
        }
    }

}