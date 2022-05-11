package ro.westaco.carhome.presentation.screens.settings.data.cars.details

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.view.View
import android.widget.Toast
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
import okhttp3.ResponseBody
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.requests.VehicleEvent
import ro.westaco.carhome.data.sources.remote.responses.models.*
import ro.westaco.carhome.di.ApiModule.Companion.BASE_URL_RESOURCES
import ro.westaco.carhome.presentation.base.BaseFragment
import ro.westaco.carhome.presentation.common.DeleteDialogFragment
import ro.westaco.carhome.utils.CatalogUtils
import ro.westaco.carhome.utils.FileUtil
import ro.westaco.carhome.utils.Progressbar
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

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
        progressbar!!.showPopup()

        back.setOnClickListener {
            viewModel.onBack()
        }

        home.setOnClickListener {
            viewModel.onMain()
        }

        rcyView.layoutManager = LinearLayoutManager(context)
        rcyViewOther.layoutManager = LinearLayoutManager(context)

    }

    @SuppressLint("SetTextI18n")
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
                    val timeLeftMillisPos =
                        if (timeLeftMillis < 0) -timeLeftMillis else timeLeftMillis
                    val daysLeft = TimeUnit.MILLISECONDS.toDays(timeLeftMillisPos)
                    val hoursLeft = TimeUnit.MILLISECONDS.toHours(timeLeftMillisPos)
                    val minutesLeft = TimeUnit.MILLISECONDS.toMinutes(timeLeftMillisPos)
                    val formattedTimeLeft = when {
                        daysLeft > 0 -> context?.getString(R.string.days_template, daysLeft)
                        hoursLeft > 0 -> context?.getString(R.string.hours_template, hoursLeft)
                        minutesLeft > 0 -> context?.getString(
                            R.string.minutes_template,
                            minutesLeft
                        )
                        else -> ""
                    }

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
                    val timeLeftMillisPos =
                        if (timeLeftMillis < 0) -timeLeftMillis else timeLeftMillis
                    val daysLeft = TimeUnit.MILLISECONDS.toDays(timeLeftMillisPos)
                    val hoursLeft = TimeUnit.MILLISECONDS.toHours(timeLeftMillisPos)
                    val minutesLeft = TimeUnit.MILLISECONDS.toMinutes(timeLeftMillisPos)
                    val formattedTimeLeft = when {
                        daysLeft > 0 -> requireContext().getString(R.string.days_template, daysLeft)
                        hoursLeft > 0 -> requireContext().getString(
                            R.string.hours_template,
                            hoursLeft
                        )
                        minutesLeft > 0 -> requireContext().getString(
                            R.string.minutes_template,
                            minutesLeft
                        )
                        else -> ""
                    }

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
                                R.color.redExpired
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
                    vehicleDetails.vignetteAttachment.let {
                        if (it != null) {
                            viewModel.fetchData(it)
                        }
                    }
                }

                if (vehicleDetails.rcaAttachment.isNullOrEmpty()) {
                    rcaDocumentLL.visibility = View.GONE
                } else {
                    rcaDocumentLL.visibility = View.VISIBLE
                }

                docTitle.isVisible = (roDocumentLL.isVisible || rcaDocumentLL.isVisible)

                tv_rc_btn.setOnClickListener {
                    vehicleDetails.rcaAttachment.let {
                        if (it != null) {
                            viewModel.fetchData(it)
                        }
                    }
                }

                vin.text = vehicleDetails.vehicleIdentificationNumber ?: ""
                licensePlate.text = vehicleDetails.licensePlate ?: ""

                model.text = vehicleDetails.model ?: ""
                engineSize.text = (vehicleDetails.engineSize ?: " ").toString()
                power.text = (vehicleDetails.enginePower ?: " ").toString()

                noSeats.text = (vehicleDetails.noOfSeats ?: " ").toString()
                maxAllowableMass.text = (vehicleDetails.maxAllowableMass ?: " ").toString()
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
                    cerAttachment?.href?.let { it ->
                        progressbar?.showPopup()
                        val baseUrl = BASE_URL_RESOURCES + it
                        viewModel.fetchData(baseUrl)
                    }
                }

                btnDeleteCertificate.setOnClickListener {

                    /*llCertificate.visibility = View.GONE
                    llUploadCertificate.visibility = View.VISIBLE
                    viewModel.onDeleteCertificateAttachment(
                        vehicleDetails.id.toInt(),
                        cerAttachment?.id
                    )
                    btnRegCertificate.isEnabled = true*/

                    val CertificateDialog = DeleteDialogFragment()
                    CertificateDialog.layoutResId = R.layout.dialog_delete_car
                    CertificateDialog.listener =
                        object : DeleteDialogFragment.OnDialogInteractionListener {
                            override fun onPosClicked() {

                                llCertificate.visibility = View.GONE
                                llUploadCertificate.visibility = View.VISIBLE
                                viewModel.onDeleteCertificateAttachment(
                                    vehicleDetails.id.toInt(),
                                    cerAttachment?.id
                                )
                                CertificateDialog.dismiss()
                                btnRegCertificate.isEnabled = true
                            }
                        }

                    CertificateDialog.show(childFragmentManager, DeleteDialogFragment.TAG)

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
                    vehicleDetails.otherAttachments.let {
                        CarDetailsOtherAttachmentAdapter(
                            requireContext(),
                            it,
                        )
                    }
                }
                rcyViewOther.adapter = otherAdapter

                otherAdapter.onItemClick = { attachments, from ->
                    if (from == "VIEW") {
                        attachments.href?.let { it ->
                            progressbar?.showPopup()
                            val baseUrl = BASE_URL_RESOURCES + it
                            viewModel.fetchData(baseUrl)
                        }
                    } else {

                        /* viewModel.onDeleteCertificateAttachment(
                             vehicleDetails.id.toInt(),
                             attachments.id
                         )*/

                        val othercertificateDialog = DeleteDialogFragment()
                        othercertificateDialog.layoutResId = R.layout.dialog_delete_car
                        othercertificateDialog.listener =
                            object : DeleteDialogFragment.OnDialogInteractionListener {
                                override fun onPosClicked() {

                                    viewModel.onDeleteCertificateAttachment(
                                        vehicleDetails.id.toInt(),
                                        attachments.id
                                    )

                                    othercertificateDialog.dismiss()
                                }
                            }

                        othercertificateDialog.show(childFragmentManager, DeleteDialogFragment.TAG)

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

        viewModel.attachmentData.observe(viewLifecycleOwner) { attachmentData ->
            progressbar?.dismissPopup()


/*            if (attachmentData != null) {
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

            makeAndModel.text = "${vehicleBrand ?: ""} ${vehicleDetails?.model ?: ""}"
            manufacturer.text = vehicleBrand ?: ""
        }

        viewModel.fuelTypeData.observe(viewLifecycleOwner) { fuelTypeData ->

            val fuelTypeStr = vehicleDetails?.fuelTypeId?.let {
                CatalogUtils.findById(fuelTypeData, it)?.name
            } ?: ""
            fuelType.text = fuelTypeStr

            fuelType2.text = fuelTypeStr
        }

        viewModel.countryData.observe(viewLifecycleOwner) { countryData ->

            val pos = vehicleDetails?.registrationCountryCode?.let {
                Country.findPositionForCode(
                    countryData,
                    it
                )
            }
            val countryName = pos?.let { countryData[it].toString() }
            lblRegistrationCountryCode.text = countryName

        }

        viewModel.actionStream.observe(viewLifecycleOwner) {
            when (it) {

                is CarDetailsViewModel.ACTION.onEventsFetched -> setReminderAdapter(it.eventsTypelist)
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

    fun setReminderAdapter(eventTypeList: ArrayList<EventType>) {

        if (vehicleDetails?.vehicleEvents.isNullOrEmpty()) {

            rcyView.visibility = View.GONE

        } else {

            val list = ArrayList<VehicleEvent>()

            rcyView.visibility = View.VISIBLE

            vehicleDetails?.vehicleEvents?.forEach {


                if (it.reminder) {

                    list.add(it)

                    adapter = vehicleDetails?.vehicleEvents.let {

                        CarDetailsReminderAdapter(
                            requireContext(),
                            list,
                            eventTypeList
                        )
                    }
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
                    Toast.makeText(
                        requireContext(),
                        requireContext().resources.getString(R.string.allow_permission),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    fun setdaymonthFormat(unformattedDate: String): String? {
        try {
            val dateformat =
                SimpleDateFormat("yyyy-MM-dd\'T\'HH:mm:ss.SSS\'Z\'").parse(unformattedDate)
            return SimpleDateFormat("dd MMM yyyy").format(dateformat)
        } catch (e: Exception) {
            return null
        }
    }


}