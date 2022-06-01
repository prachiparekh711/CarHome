package ro.westaco.carhome.presentation.screens.data.cars.details

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.request.RequestOptions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_car_details.*
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.requests.VehicleEvent
import ro.westaco.carhome.data.sources.remote.responses.models.*
import ro.westaco.carhome.di.ApiModule.Companion.BASE_URL_RESOURCES
import ro.westaco.carhome.presentation.base.BaseFragment
import ro.westaco.carhome.presentation.common.DeleteDialogFragment
import ro.westaco.carhome.presentation.screens.home.PdfActivity
import ro.westaco.carhome.utils.CatalogUtils
import ro.westaco.carhome.utils.DialogUtils.Companion.showErrorInfo
import ro.westaco.carhome.utils.FileUtil
import ro.westaco.carhome.utils.Progressbar
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

//C- Car Details Design
@AndroidEntryPoint
class CarDetailsFragment : BaseFragment<CarDetailsViewModel>() {
    private var vehicle: Vehicle? = null
    private var vehicleDetails: VehicleDetails? = null
    private lateinit var adapter: CarDetailsReminderAdapter
    private lateinit var otherAdapter: CarDetailsOtherAttachmentAdapter

    var cerAttachment: Attachments? = null
    var vehicleId: Int = 0
    private var progressbar: Progressbar? = null
    var mActivity: Activity? = null

    companion object {
        const val ARG_CAR = "arg_car"
        const val ARG_CAR_ID = "arg_car_id"
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Activity) {
            mActivity = context
        }
    }


    override fun getContentView() = R.layout.fragment_car_details

    override fun getStatusBarColor() = ContextCompat.getColor(requireContext(), R.color.white)

    override fun onResume() {
        super.onResume()
        arguments?.let {
            vehicle = it.getSerializable(ARG_CAR) as Vehicle?
            val vehicleId = it.getInt(ARG_CAR_ID)
            if (vehicle?.id != null)
                vehicle?.id?.let { it1 -> viewModel.onVehicle(it1) }
            else
                vehicleId.let { v -> viewModel.onVehicle(v) }

        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun initUi() {

        progressbar = Progressbar(requireContext())
        progressbar?.showPopup()

        back.setOnClickListener {
            viewModel.onBack()
        }

        home.setOnClickListener {
            viewModel.onMain()
        }

        rcyView.layoutManager = LinearLayoutManager(context)
        rcyViewOther.layoutManager = LinearLayoutManager(context)

    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun setObservers() {

        viewModel.vehicleDetailsLivedata.observe(viewLifecycleOwner) { vehicleDetails ->
            if (vehicleDetails != null) {
                this.vehicleDetails = vehicleDetails

                viewModel.fetchDefaultData()

                vehicleId = vehicleDetails.id.toInt()
                val options = RequestOptions()
                logo.clipToOutline = true
                Glide.with(requireContext())
                    .load(resources.getString(R.string.BASE_URL_RESOURCES) + vehicleDetails.brandLogo)
                    .apply(
                        options.fitCenter()
                            .skipMemoryCache(true)
                            .priority(Priority.HIGH)
                            .format(DecodeFormat.PREFER_ARGB_8888)
                    )
                    .error(R.drawable.logo_small)
                    .into(logo)

                addReminders.visibility = View.VISIBLE
                addReminders.setOnClickListener {
                    //                hasCalenderPermission(100)
                    viewModel.onAddReminders(vehicleDetails)
                }
                vehicleSubCategory.text = vehicleDetails.vehicleSubCategoryName ?: ""
                purpose.text = vehicleDetails.vehicleUsageTypeName ?: ""
                carNumber.text = vehicleDetails.licensePlate ?: ""
                policyExpiry.text = (vehicleDetails.policyExpirationDate ?: " ").toString()


                /**
                 * rca status
                 */
                if (!vehicleDetails.policyExpirationDate.isNullOrEmpty()) {
                    val serverDate =
                        viewModel.originalFormat.parse(vehicleDetails.policyExpirationDate)
                    val timeLeftMillis = serverDate.time - Date().time

                    if (timeLeftMillis > 0) {

                        status.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.greenActive
                            )
                        )
                        status.text = getString(R.string.status_active)
                    } else {

                        status.text = getString(R.string.purchases_exp_inactive)
                        status.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.redExpired
                            )
                        )
                    }


                } else {

                    status.text = "N/A"
                }

                /**
                 * rovinieta status
                 */
                if (!vehicleDetails.vignetteExpirationDate.isNullOrEmpty()) {

                    val serverDate =
                        viewModel.originalFormat.parse(vehicleDetails.vignetteExpirationDate)

                    val timeLeftMillis = serverDate.time - Date().time

                    if (timeLeftMillis > 0) {


                        policyExpiry.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.greenActive
                            )
                        )
                        policyExpiry.text = requireContext().getString(R.string.status_active)
                    } else {
                        policyExpiry.text =
                            requireContext().getString(R.string.purchases_exp_inactive)
                        policyExpiry.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.deleteRed
                            )
                        )
                        imgAlert.visibility = View.VISIBLE
                    }


                } else {

                    policyExpiry.text = "N/A"
                }

                /**
                 * document date
                 */
                ro_date.text = vehicleDetails.vignetteExpirationDate?.let { setdaymonthFormat(it) }
                rc_date.text = vehicleDetails.policyExpirationDate?.let { setdaymonthFormat(it) }

                /**
                 * rovinieta doc open
                 */

                if (vehicleDetails.vignetteAttachment.isNullOrEmpty()) {
                    roDocumentLL.visibility = View.GONE
                } else {
                    roDocumentLL.visibility = View.VISIBLE
                }

                tv_ro_btn.setOnClickListener {
                    val url = BASE_URL_RESOURCES + vehicleDetails.vignetteAttachment
                    val intent = Intent(requireContext(), PdfActivity::class.java)
                    intent.putExtra(PdfActivity.ARG_DATA, url)
                    intent.putExtra(PdfActivity.ARG_FROM, "DOCUMENT")
                    requireContext().startActivity(intent)
                }

                if (vehicleDetails.rcaAttachment.isNullOrEmpty()) {
                    rcaDocumentLL.visibility = View.GONE
                } else {
                    rcaDocumentLL.visibility = View.VISIBLE
                }

                docTitle.isVisible = (roDocumentLL.isVisible || rcaDocumentLL.isVisible)

                tv_rc_btn.setOnClickListener {
                    val url = BASE_URL_RESOURCES + vehicleDetails.rcaAttachment
                    val intent = Intent(requireContext(), PdfActivity::class.java)
                    intent.putExtra(PdfActivity.ARG_DATA, url)
                    intent.putExtra(PdfActivity.ARG_FROM, "DOCUMENT")
                    requireContext().startActivity(intent)
                }

                vin.text = vehicleDetails.vehicleIdentificationNumber ?: ""
                licensePlate.text = vehicleDetails.licensePlate ?: ""

                model.text = vehicleDetails.model ?: ""
                vehicleDetails.engineSize.let { engineSize.text = "$it cc" }
                vehicleDetails.enginePower.let { power.text = "$it HP" }

                noSeats.text = (vehicleDetails.noOfSeats ?: " ").toString()
                vehicleDetails.maxAllowableMass.let { maxAllowableMass.text = "$it kg" }

                year.text = (vehicleDetails.manufacturingYear ?: " ").toString()
                civ.text = vehicleDetails.vehicleIdentityCard ?: ""

                btnRegCertificate.setOnClickListener {
                    val result = FileUtil.checkPermission(requireContext())
                    if (result) {
                        callFileManagerForCertificate()
                    } else {
                        FileUtil.requestPermission(requireActivity())
                    }
                }

                cerAttachment = vehicleDetails.certificateAttachment
                if (cerAttachment != null) {
                    llCertificate.visibility = View.VISIBLE
                    llUploadCertificate.visibility = View.GONE
                    lblCertificate.text = cerAttachment?.name ?: ""
                }

                lblCertificate.setOnClickListener {
                    val url = BASE_URL_RESOURCES + cerAttachment?.href
                    val intent = Intent(requireContext(), PdfActivity::class.java)
                    intent.putExtra(PdfActivity.ARG_DATA, url)
                    intent.putExtra(PdfActivity.ARG_FROM, "DOCUMENT")
                    requireContext().startActivity(intent)
                }

                btnDeleteCertificate.setOnClickListener {

                    val certificateDialog = DeleteDialogFragment()
                    certificateDialog.layoutResId = R.layout.dialog_delete_car
                    certificateDialog.listener =
                        object : DeleteDialogFragment.OnDialogInteractionListener {
                            override fun onPosClicked() {

                                llCertificate.visibility = View.GONE
                                llUploadCertificate.visibility = View.VISIBLE
                                viewModel.onDeleteCertificateAttachment(
                                    vehicleDetails.id.toInt(),
                                    cerAttachment?.id
                                )
                                certificateDialog.dismiss()
                                btnRegCertificate.isEnabled = true
                            }
                        }

                    certificateDialog.show(childFragmentManager, DeleteDialogFragment.TAG)

                }

                llUploadOther.setOnClickListener {
                    val result = FileUtil.checkPermission(requireContext())
                    if (result) {
                        callFileManagerForOther()
                    } else {
                        FileUtil.requestPermission(requireActivity())
                    }
                }

                if (!vehicleDetails.vehicleEvents.isNullOrEmpty()) {
                    viewModel.getVehicleEventType()
                }

                otherAdapter = if (vehicleDetails.otherAttachments.isNullOrEmpty()) {
                    CarDetailsOtherAttachmentAdapter(requireContext(), arrayListOf())
                } else {
                    CarDetailsOtherAttachmentAdapter(
                        requireContext(),
                        vehicleDetails.otherAttachments,
                    )
                }
                rcyViewOther.adapter = otherAdapter

                otherAdapter.onItemClick = { attachments, from ->
                    if (from == "VIEW") {
                        val url = BASE_URL_RESOURCES + attachments.href
                        val intent = Intent(requireContext(), PdfActivity::class.java)
                        intent.putExtra(PdfActivity.ARG_DATA, url)
                        intent.putExtra(PdfActivity.ARG_FROM, "DOCUMENT")
                        requireContext().startActivity(intent)
                    } else {

                        val otherCertificateDialog = DeleteDialogFragment()
                        otherCertificateDialog.layoutResId = R.layout.dialog_delete_car
                        otherCertificateDialog.listener =
                            object : DeleteDialogFragment.OnDialogInteractionListener {
                                override fun onPosClicked() {

                                    viewModel.onDeleteCertificateAttachment(
                                        vehicleDetails.id.toInt(),
                                        attachments.id
                                    )

                                    otherCertificateDialog.dismiss()
                                }
                            }

                        otherCertificateDialog.show(childFragmentManager, DeleteDialogFragment.TAG)

                    }
                }

                cta.visibility = View.VISIBLE
                cta.setOnClickListener {
                    viewModel.onEdit(vehicleDetails)
                }

                delete.visibility = View.VISIBLE

                delete.setOnClickListener {
                    val deleterDialog = DeleteDialogFragment()
                    deleterDialog.layoutResId = R.layout.dialog_delete_car
                    deleterDialog.listener =
                        object : DeleteDialogFragment.OnDialogInteractionListener {
                            override fun onPosClicked() {
                                viewModel.onDelete(vehicleDetails.id)
                            }
                        }

                    deleterDialog.show(childFragmentManager, DeleteDialogFragment.TAG)
                }

            }
            progressbar?.dismissPopup()
        }

        viewModel.vehicleUsageData.observe(viewLifecycleOwner) { vehicleUsageData ->
            val usageId = vehicleDetails?.vehicleUsageType
            val uasgeStr =
                usageId?.let {
                    CatalogUtils.findById(
                        vehicleUsageData,
                        it
                    )?.name
                }

            purpose.text = uasgeStr ?: ""
            progressbar?.dismissPopup()
        }

        viewModel.vehicleCategoryData.observe(viewLifecycleOwner) { vehicleCategoryData ->
            val vehicleCategoryId = vehicleDetails?.vehicleCategory
            val vehicleCategoryStr =
                vehicleCategoryId?.let {
                    CatalogUtils.findById(
                        vehicleCategoryData,
                        it
                    )?.name
                }
            vehicleCategory.text = vehicleCategoryStr ?: ""

            vehicleDetails?.vehicleCategory?.toInt()
                ?.let { viewModel.fetchVehicleSubCategory(it) }
        }

        viewModel.vehicleSubCategoryData.observe(viewLifecycleOwner) { vehicleSubCategoryData ->
            val vehicleCategoryId = vehicleDetails?.vehicleSubCategoryId
            val vehicleSubCategoryStr =
                vehicleCategoryId?.let {
                    CatalogUtils.findById(
                        vehicleSubCategoryData,
                        it
                    )?.name
                }
            vehicleSubCategory.text = vehicleSubCategoryStr ?: ""
        }

        viewModel.vehicleBrandData.observe(viewLifecycleOwner) { vehicleBrandData ->

            val vehicleBrandId = vehicleDetails?.vehicleBrand
            val vehicleBrand =
                vehicleBrandId?.let {
                    CatalogUtils.findById(
                        vehicleBrandData,
                        it
                    )?.name
                }

            makeAndModel.text = "${vehicleDetails?.model ?: ""}"
            brand.text = "${vehicleBrand ?: ""} "
            manufacturer.text = vehicleBrand ?: ""
        }

        viewModel.fuelTypeData.observe(viewLifecycleOwner) { fuelTypeData ->

            val fuelTypeStr = vehicleDetails?.fuelTypeId?.let {
                CatalogUtils.findById(fuelTypeData, it)?.name
            } ?: ""

            fuelType2.text = fuelTypeStr
        }

        viewModel.countryData.observe(viewLifecycleOwner) { countryData ->
            if (countryData != null) {
                val pos = vehicleDetails?.registrationCountryCode?.let {
                    Country.findPositionForCode(
                        countryData,
                        it
                    )
                }
                val countryName = pos?.let { countryData[it].toString() }
                lblRegistrationCountryCode.text = countryName
            }
        }

        viewModel.actionStream.observe(viewLifecycleOwner) {
            when (it) {

                is CarDetailsViewModel.ACTION.OnEventsFetched -> setReminderAdapter(it.eventsTypelist)
            }
        }
    }

    private fun setReminderAdapter(eventTypeList: ArrayList<EventType>) {

        if (vehicleDetails?.vehicleEvents.isNullOrEmpty()) {

            rcyView.visibility = View.GONE

        } else {

            val list = ArrayList<VehicleEvent>()

            rcyView.visibility = View.VISIBLE

            vehicleDetails?.vehicleEvents?.forEach {


                if (it.reminder) {

                    list.add(it)

                    adapter = CarDetailsReminderAdapter(
                        requireContext(),
                        list,
                        eventTypeList
                    )
                    adapter.setItems(list)
                }
            }


        }

        rcyView.adapter = adapter

    }

    private fun callFileManagerForCertificate() {

        try {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/pdf"
            }
            startActivityForResult(intent, 101)
        } catch (e: ActivityNotFoundException) {
        }
    }

    private fun callFileManagerForOther() {
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
                    val dlFile = File(selectedFile)
                    viewModel.onAttach(vehicleId, "REG_CERTIFICATE", dlFile)
                    lblCertificate.text = "attachment"
                    llUploadCertificate.visibility = View.GONE
                    llCertificate.visibility = View.VISIBLE
                    btnRegCertificate.isEnabled = false
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
                    val dlFile = File(selectedFile)
                    viewModel.onAttach(vehicleId, "OTHER", dlFile)
                }
            }
        }

        if (requestCode == 2296) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    callFileManagerForCertificate()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            200 -> if (grantResults.size > 0) {
                val READ_EXTERNAL_STORAGE = grantResults[0] == PackageManager.PERMISSION_GRANTED
                val WRITE_EXTERNAL_STORAGE = grantResults[1] == PackageManager.PERMISSION_GRANTED
                if (READ_EXTERNAL_STORAGE && WRITE_EXTERNAL_STORAGE) {
                    callFileManagerForCertificate()
                } else {
                    showErrorInfo(requireContext(), getString(R.string.allow_permission))
                }
            }
        }
    }

    private fun setdaymonthFormat(unformattedDate: String): String? {
        return try {
            val dateFormat =
                SimpleDateFormat("yyyy-MM-dd\'T\'HH:mm:ss.SSS\'Z\'").parse(unformattedDate)
            SimpleDateFormat("dd MMM yyyy").format(dateFormat)
        } catch (e: Exception) {
            null
        }
    }


}