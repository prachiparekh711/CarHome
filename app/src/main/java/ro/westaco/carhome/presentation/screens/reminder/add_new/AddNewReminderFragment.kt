package ro.westaco.carhome.presentation.screens.reminder.add_new

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_add_new_reminder.*
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.requests.ReminderNotification
import ro.westaco.carhome.data.sources.remote.responses.models.CatalogItem
import ro.westaco.carhome.data.sources.remote.responses.models.LocationV2Item
import ro.westaco.carhome.data.sources.remote.responses.models.Reminder
import ro.westaco.carhome.databinding.DirectionPopupBinding
import ro.westaco.carhome.databinding.LocationSelectorBinding
import ro.westaco.carhome.presentation.base.BaseFragment
import java.text.SimpleDateFormat
import java.util.*


//C-     Repeat option dropdown
//C-    Location option
//C-    selecting tags color
@AndroidEntryPoint
class AddNewReminderFragment : BaseFragment<AddNewReminderViewModel>() {

    private var isEdit = false
    private var reminder: Reminder? = null
    var repeatPos = 0
    var tagPos = 0
    var repeatList: ArrayList<CatalogItem> = ArrayList()
    var durationList: ArrayList<CatalogItem> = ArrayList()
    private var allFilterList: ArrayList<CatalogItem> = ArrayList()
    private var tagsAdapter: ReminderTagsAdapter? = null
    protected val REQUEST_CHECK_SETTINGS = 0x1
    var duration: Int = 0
    var durationUnit: Int = 0

    private var locationReceiver: LocationReceiver? = null
    override fun getContentView() = R.layout.fragment_add_new_reminder

    override fun getStatusBarColor() = ContextCompat.getColor(requireContext(), R.color.white)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            isEdit = it.getBoolean(ARG_IS_EDIT)
            reminder = it.getSerializable(ARG_REMINDER) as Reminder
            if (reminder?.id != null) {
                viewModel.fetchRemoteData(reminder?.id!!)
            }
        }
    }

    override fun initUi() {
        mName = mText
        mGarage1 = mGarage
        locationswitch = mSwitch
        fullAddress = mLocation
        distance = mKm
        mRelative1 = mRelative
        mContext = requireContext()



        toolbar.setNavigationOnClickListener {
            viewModel.onBack()
        }

        root.setOnClickListener {
            viewModel.onRootClicked()
        }

        dueDate.setOnClickListener {
            viewModel.onDueDateClicked()
        }
        dueDateCal.setOnClickListener {
            viewModel.onDueDateClicked()
        }
        dueTime.setOnClickListener {
            viewModel.onDueTimeClicked()
        }
        dueTimeCal.setOnClickListener {
            viewModel.onDueTimeClicked()
        }

        mNumberPicker.minValue = 0
        mNumberPicker.maxValue = 60
        mNumberPicker.wrapSelectorWheel = true
        mNumberPicker.setOnValueChangedListener { picker, oldVal, newVal ->
            duration = newVal
        }

        notificationCheck.setOnCheckedChangeListener { buttonView, isChecked ->
            notificationRL.isVisible = isChecked
        }

        if (isEdit && reminder != null) {
            notificationRL.isVisible = reminder?.notifications?.isEmpty() == false
        }

//


//      *Location in BottomSheet(R6)

        locationReceiver = LocationReceiver()
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            locationReceiver!!,
            IntentFilter("LOCATION")
        )
        val view = layoutInflater.inflate(R.layout.location_bottomsheet, null)

        sheetDialog?.setCancelable(false)
        sheetDialog?.setContentView(view)
        /*mRelative.isVisible = mSwitch.isChecked*/

        mSwitch.setOnCheckedChangeListener { _, isChecked ->

            if (isChecked) {
                displayLocationSettingsRequest(requireActivity())
                openBottomMapFragment(fragment)
            } else {
                mRelative.isVisible = false
            }
        }

        image.setOnClickListener {
            sheetDialog?.show()
        }
//      *Location in BottomSheet(R6)


        cta.setOnClickListener {
            val notifications = arrayListOf<ReminderNotification>()
            if (notificationCheck.isChecked) {
                notifications.add(
                    ReminderNotification(
                        duration.toLong(),
                        durationList[durationUnit].id
                    )
                )
            }


            viewModel.onSave(
                title.text.toString(),
                notes.text.toString(),
                dueDate.text.toString(),
                dueTime.text.toString(),
                notifications,
                tagsAdapter?.getSelected(),
                repeatPos,
                locationItem,
                isEdit,
                reminder
            )
        }
    }


    private var dpd: DatePickerDialog? = null
    private fun showDatePicker(dateInMillis: Long) {
        val c = Calendar.getInstance().apply {
            if (isEdit && reminder != null)
                timeInMillis = reminder?.dueDate?.let { dateToMilis(it) }!!
            else
                timeInMillis = dateInMillis
        }

        dpd?.cancel()
        dpd = DatePickerDialog(
            requireContext(), R.style.DialogTheme, { _, year, monthOfYear, dayOfMonth ->
                viewModel.onDueDatePicked(
                    Calendar.getInstance().apply {
                        set(Calendar.YEAR, year)
                        set(Calendar.MONTH, monthOfYear)
                        set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    }.timeInMillis
                )
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)
        )
        dpd?.datePicker?.minDate = System.currentTimeMillis() - 1000
        dpd?.show()
    }

    private var tpd: TimePickerDialog? = null
    private fun showTimePicker(dateTimeInMillis: Long) {
        val c = Calendar.getInstance().apply {
            if (isEdit && reminder != null)
                timeInMillis = timeToMilis(reminder?.dueTime!!)
            else
                timeInMillis = dateTimeInMillis
        }

        tpd?.cancel()
        tpd = TimePickerDialog(
            requireContext(), R.style.DialogTheme, { _, hour, min ->
                viewModel.onDueTimePicked(
                    Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, hour)
                        set(Calendar.MINUTE, min)
                    }.timeInMillis
                )
            }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true
        )
        tpd?.show()
    }

    @SuppressLint("SimpleDateFormat")
    override fun setObservers() {
        viewModel.dueDateLiveData.observe(viewLifecycleOwner) { dateMillis ->
            dueDate.visibility = View.VISIBLE
            dueDate.text = SimpleDateFormat(
                getString(R.string.date_format_template), Locale.getDefault()
            ).format(
                Date(dateMillis)
            )
        }

        viewModel.dueTimeLiveData.observe(viewLifecycleOwner) { timeDateMillis ->
            dueTime.visibility = View.VISIBLE
            dueTime.text = SimpleDateFormat(
                getString(R.string.time_format_template), Locale.getDefault()
            ).format(
                Date(timeDateMillis)
            )
        }

        viewModel.actionStream.observe(viewLifecycleOwner) {
            when (it) {
                is AddNewReminderViewModel.ACTION.ShowDatePicker -> showDatePicker(it.dateInMillis)
                is AddNewReminderViewModel.ACTION.ShowTimePicker -> showTimePicker(it.dateTimeInMillis)
            }
        }

        viewModel.remindersTabData.observe(viewLifecycleOwner) { tagsList ->
            allFilterList = tagsList
            tags.layoutManager = GridLayoutManager(context, ReminderTagsAdapter.COLUMNS)

            if (isEdit && reminder != null) {
                for (i in allFilterList.indices) {
                    if (reminder?.tags?.contains(allFilterList[i].id) == true)
                        tagPos = i
                }
            } else {
                tagPos = allFilterList.size - 1
            }
            tagsAdapter = ReminderTagsAdapter(requireContext(), tagPos)
            tagsAdapter?.setItems(allFilterList)
            tags.adapter = tagsAdapter
        }

        viewModel.remindersLiveData.observe(viewLifecycleOwner) { reminderItem ->
            //* Edit Reminder(R11)
            if (isEdit && reminderItem != null) {
                reminder = reminderItem
                toolbar.title = getString(R.string.edit_reminder)
                cta.text = getString(R.string.save_changes)

                title.setText(reminderItem.title)
                notes.setText(reminderItem.notes)
                val sdf = SimpleDateFormat(getString(R.string.server_date_format_template))
                val mDate = sdf.parse(reminderItem.dueDate)

                reminderItem.dueDate.let { mDate.time }.let { viewModel.onDueDatePicked(it) }
                reminderItem.dueTime.let {
                    if (it != null) {
                        viewModel.onDueTimePicked(timeToMilis(it))
                    }
                }
                if (reminderItem.notifications != null) {
                    notificationCheck.isChecked = reminderItem.notifications.isNotEmpty()
                }


            } else {

                repeatPos = 0
            }

            //*Edit Reminder(R11)
        }

        viewModel.repeatLiveData.observe(viewLifecycleOwner) { repeatList ->
            this.repeatList = repeatList
            if (repeatList.isNotEmpty())
                repeat.text = repeatList[0].toString()

            if (isEdit && reminder != null) {
                if (reminder?.repeat != null) {
                    for (i in repeatList.indices) {
                        if (repeatList[i].id.toInt() == reminder?.repeat)
                            repeatPos = i
                    }

                    try {
                        repeat.text = repeatList[repeatPos].toString()
                    } catch (e: Exception) {
                        repeat.text = repeatList[0].toString()
                    }
                }
            }


//        *Repeat Recycler in Bottomsheet(R5)
            val dialog = BottomSheetDialog(requireContext())

            val repeatInterface = object : Repeatadapter.RepeatInterface {
                override fun OnSelection(model: Int) {
                    repeatPos = model
                }
            }

            repeat.setOnClickListener {
                if (repeatList.isNotEmpty()) {
                    val adapter = Repeatadapter(requireContext(), repeatInterface, repeatPos)
                    adapter.arrayList.clear()
                    val view = layoutInflater.inflate(R.layout.repeat_bottomsheet, null)
                    val mRecycler = view.findViewById<RecyclerView>(R.id.mRepeatRec)
                    val mBack = view.findViewById<ImageView>(R.id.mBack)
                    val mContinue = view.findViewById<TextView>(R.id.mContinue)
                    mRecycler.layoutManager =
                        LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
                    mRecycler.layoutAnimation = null
                    mRecycler.adapter = adapter
                    adapter.addAll(repeatList)

                    mContinue.setOnClickListener {
                        repeat.text = repeatList[repeatPos].toString()
                        dialog.dismiss()
                    }
                    dialog.setCancelable(false)
                    dialog.setContentView(view)
                    dialog.setOnShowListener {
                        val bottomSheetDialog = it as BottomSheetDialog
                        val parentLayout =
                            bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
                        parentLayout?.let { it ->
                            val behaviour = BottomSheetBehavior.from(it)
                            behaviour.state = BottomSheetBehavior.STATE_EXPANDED
                        }
                    }
                    dialog.show()
                    mBack.setOnClickListener { dialog.dismiss() }
                }
            }
//      *Repeat Recycler in Bottomsheet(R5)
        }

        viewModel.durationData.observe(viewLifecycleOwner) { durationList ->
            this.durationList = durationList
            var typeList: Array<String> = arrayOf()
            durationList.forEach {
                typeList += it.name
            }

            mDurationPicker.minValue = 0
            mDurationPicker.maxValue = durationList.size - 1
            mDurationPicker.displayedValues = typeList
            mDurationPicker.wrapSelectorWheel = false
            mDurationPicker.setOnValueChangedListener { picker, oldVal, newVal ->
                durationUnit = newVal
            }
        }
    }

    private fun openBottomMapFragment(fragment: MapBottomsheetDialog) {
        fragment.show(childFragmentManager, "map")
    }

    private class LocationReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            locationItem = intent?.getSerializableExtra("locationData") as LocationV2Item
            currentLocation = intent.getSerializableExtra("currentLocation") as LocationV2Item
            setLocationData(locationItem!!)
            setLocationDialog(locationItem!!, currentLocation!!)
        }
    }

    companion object {

        @SuppressLint("StaticFieldLeak")
        var mName: TextView? = null

        @SuppressLint("StaticFieldLeak")
        var mGarage1: TextView? = null

        @SuppressLint("StaticFieldLeak")
        var fullAddress: TextView? = null

        @SuppressLint("StaticFieldLeak")
        var distance: TextView? = null

        @SuppressLint("StaticFieldLeak")
        var mRelative1: RelativeLayout? = null
        var locationswitch: SwitchCompat? = null
        val fragment = MapBottomsheetDialog()

        @SuppressLint("StaticFieldLeak")
        var mContext: Context? = null
        private var latitude = 0.0
        private var longitude: Double = 0.0

        @SuppressLint("StaticFieldLeak")
        var sheetDialog: BottomSheetDialog? = null
        var locationItem: LocationV2Item? = null
        var currentLocation: LocationV2Item? = null
        const val ARG_IS_EDIT = "arg_is_edit"
        const val ARG_REMINDER = "arg_reminder"


        @SuppressLint("SetTextI18n")
        fun setLocationData(locationItem: LocationV2Item) {

            mName?.text = locationItem.name
            mGarage1?.text = locationItem.services
            fullAddress?.text = locationItem.fullAddress
            distance?.text = "${(locationItem.distance)?.toInt()} km"
            mRelative1?.isVisible = true
            fragment.dismiss()

        }

        @SuppressLint("SetTextI18n")
        fun setLocationDialog(locationItem: LocationV2Item, currentLocation: LocationV2Item) {
            val isAppInstalled = appInstalledOrNot("com.waze")
            sheetDialog =
                mContext?.let { BottomSheetDialog(it, R.style.BottomSheetStyle) }
            val dialogBinding = DirectionPopupBinding.inflate(
                LayoutInflater.from(mContext)
            )
            sheetDialog?.setContentView(dialogBinding.root)

            if (currentLocation.email != null && currentLocation.email.endsWith("petrom.com")) {
                dialogBinding.mImg.setImageResource(R.drawable.petrom)
            } else {
                dialogBinding.mImg.setImageResource(R.drawable.omv)
            }
            dialogBinding.name.text = currentLocation.name
            dialogBinding.status.text = currentLocation.timetable?.locationStatus
            dialogBinding.services.text = currentLocation.services
            dialogBinding.mAddress.text = currentLocation.fullAddress
            dialogBinding.mkm.text =
                "• " + ((locationItem.distance)?.toInt()) + " km away"
            if (currentLocation.timetable != null && currentLocation.timetable?.locationStatus.equals(
                    "CLOSE"
                )
            ) {
                mContext?.resources?.getColor(R.color.closed)
                    ?.let { dialogBinding.status.setTextColor(it) }
            } else {
                mContext?.resources?.getColor(R.color.list_time)
                    ?.let { dialogBinding.status.setTextColor(it) }
            }
            if (currentLocation.latitude != null && currentLocation.longitude != null) {
                latitude = currentLocation.latitude
                longitude = currentLocation.longitude
            }

            dialogBinding.mapButton.setOnClickListener {
                sheetDialog?.dismiss()
                val bottomSheetDialog =
                    mContext?.let { it1 -> BottomSheetDialog(it1, R.style.BottomSheetStyle) }
                val selectorBinding = LocationSelectorBinding.inflate(
                    LayoutInflater.from(mContext)
                )
                bottomSheetDialog?.setContentView(selectorBinding.root)
                bottomSheetDialog?.show()
                selectorBinding.google.setOnClickListener {
                    bottomSheetDialog?.dismiss()
                    val gmmIntentUri =
                        Uri.parse("google.navigation:q=$latitude,$longitude")
                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                    mapIntent.setPackage("com.google.android.apps.maps")
                    mContext?.startActivity(mapIntent)
                }
                selectorBinding.Waze.setOnClickListener {
                    bottomSheetDialog?.dismiss()
                    if (isAppInstalled) {
                        val p = String.format(
                            Locale.ENGLISH,
                            "geo:%f,%f",
                            latitude,
                            longitude
                        )
                        val i =
                            Intent(Intent.ACTION_VIEW, Uri.parse(p))
                        i.setPackage("com.waze")
                        mContext?.startActivity(i)
                    } else {
                        openPlayStoreApplication("https://play.google.com/store/apps/details?id=com.waze")
                    }
                }
                selectorBinding.Cancel.setOnClickListener { bottomSheetDialog?.dismiss() }
            }
        }

        private fun appInstalledOrNot(uri: String): Boolean {
            val pm = mContext?.packageManager
            try {
                pm?.getPackageInfo(uri, PackageManager.GET_ACTIVITIES)
                return true
            } catch (e: PackageManager.NameNotFoundException) {
            }
            return false
        }

        private fun openPlayStoreApplication(appPackageName: String) {
            try {
                mContext?.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=$appPackageName")
                    )
                )
            } catch (anfe: ActivityNotFoundException) {
                mContext?.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")
                    )
                )
            }
        }
    }

    fun dateToMilis(str: String): Long {
        val sdf = SimpleDateFormat(getString(R.string.server_date_format_template))

        val mDate = sdf.parse(str)
        return mDate.time

    }

    @SuppressLint("SimpleDateFormat")
    fun timeToMilis(str: String): Long {
        val sdf = SimpleDateFormat(getString(R.string.time_format_template))

        val mDate = sdf.parse(str)
        return mDate.time
    }

    private fun displayLocationSettingsRequest(context: Context) {
        val googleApiClient = GoogleApiClient.Builder(context)
            .addApi(LocationServices.API).build()
        googleApiClient.connect()
        val locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 10000
        locationRequest.fastestInterval = (10000 / 2).toLong()
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        builder.setAlwaysShow(true)
        val result =
            LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build())
        result.setResultCallback { result ->
            val status = result.status

            when (status.statusCode) {
                LocationSettingsStatusCodes.SUCCESS ->
                    Log.e(ContentValues.TAG, "All location settings are satisfied.")

                LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {

                    Log.e(
                        ContentValues.TAG,
                        "Location settings are not satisfied. Show the user a dialog to upgrade location settings "
                    )
                    try {
                        // Show the dialog by calling startResolutionForResult(), and check the result
                        // in onActivityResult().
                        status.startResolutionForResult(
                            requireActivity(),
                            REQUEST_CHECK_SETTINGS
                        )
//                        Toast.makeText(mActivity, "All location settings are satisfied.", Toast.LENGTH_SHORT).show()
                    } catch (e: IntentSender.SendIntentException) {
                        Log.e(ContentValues.TAG, "PendingIntent unable to execute request.")
                    }
                }
                LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> Log.i(
                    ContentValues.TAG,
                    "Location settings are inadequate, and cannot be fixed here. Dialog not created."
                )
            }
        }
    }

}