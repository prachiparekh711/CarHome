package ro.westaco.carhome.presentation.screens.home

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_dashboard.*
import kotlinx.android.synthetic.main.fragment_home.*
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.local.prefs.AppPreferencesDelegates
import ro.westaco.carhome.data.sources.remote.responses.models.*
import ro.westaco.carhome.di.ApiModule
import ro.westaco.carhome.presentation.base.BaseFragment
import ro.westaco.carhome.presentation.screens.dashboard.DashboardFragment
import ro.westaco.carhome.presentation.screens.data.cars.DataCarAdapter
import ro.westaco.carhome.presentation.screens.home.adapter.CarProgressAdapter
import ro.westaco.carhome.presentation.screens.home.adapter.RecentDocumentAdapter
import ro.westaco.carhome.presentation.screens.home.adapter.ReminderAdapter
import ro.westaco.carhome.presentation.screens.main.MainActivity
import ro.westaco.carhome.presentation.screens.main.MainActivity.Companion.profileItem
import ro.westaco.carhome.presentation.screens.maps.MapActivity
import ro.westaco.carhome.presentation.screens.pdf_viewer.PdfActivity
import ro.westaco.carhome.presentation.screens.settings.history.HistoryAdapter
import ro.westaco.carhome.utils.DateTimeUtils
import ro.westaco.carhome.utils.FirebaseAnalyticsList
import ro.westaco.carhome.views.MarginItemDecoration
import ro.westaco.carhome.views.Progressbar
import java.util.*


//C- Home tab section
@AndroidEntryPoint
class HomeFragment : BaseFragment<HomeViewModel>(),
    CarProgressAdapter.OnCarItemInteractionListener,
    DataCarAdapter.OnItemInteractionListener,
    RecentDocumentAdapter.OnItemInteractionListener,
    HistoryAdapter.OnItemInteractionListener,
    ReminderAdapter.OnItemInteractionListener {

    companion object {
        const val TAG = "HomeFragment"
    }

    private lateinit var carAdapter: CarProgressAdapter
    private lateinit var carDetailAdapter: DataCarAdapter
    private lateinit var recentDocAdapter: RecentDocumentAdapter
    private lateinit var reminderAdapter: ReminderAdapter
    var progressbar: Progressbar? = null
    lateinit var adapter: HistoryAdapter
    var allFilterList: ArrayList<CatalogItem> = ArrayList()
    var vehicleProgressList: ArrayList<VehicleProgressItem> = ArrayList()

    override fun getContentView() = R.layout.fragment_home

    override fun getStatusBarColor() = ContextCompat.getColor(requireContext(), R.color.white)

    override fun initUi() {
        MainActivity.activeService = ""
        progressbar = Progressbar(requireContext())
        progressbar?.showPopup()

        val parentFrag: DashboardFragment = this@HomeFragment.parentFragment as DashboardFragment
        val menu: Menu = parentFrag.bottomNavigationView.menu


        avatar.setOnClickListener {
            viewModel.onAvatarClicked()
        }

        mProfileRL.setOnClickListener {
            viewModel.onAvatarClicked()
        }

        addCar.setOnClickListener {
            viewModel.onAddNewCar()
        }

        noCarRL.setOnClickListener {
            viewModel.onAddNewCar()
        }

        offerOpinion.clipToOutline = true
        offerMap.clipToOutline = true
        offerReminder.clipToOutline = true

        offerOpinion.setOnClickListener {
            viewModel.onContactUsClicked()
        }

        offerMap.setOnClickListener {
            val params = Bundle()
            viewModel.mFirebaseAnalytics.logEvent(FirebaseAnalyticsList.ACCESS_OFFER_HOME, params)
            val i = Intent(requireActivity(), MapActivity::class.java)
            i.putExtra(MapActivity.PARENT, MapActivity.HOME_FRAGMENT)
            requireActivity().startActivity(i)
        }

        offerReminder.setOnClickListener {
            val params = Bundle()
            viewModel.mFirebaseAnalytics.logEvent(FirebaseAnalyticsList.ACCESS_OFFER_HOME, params)
            parentFrag.bottomNavigationView.menu.findItem(R.id.reminder).isChecked = true
            parentFrag.viewModel.onItemSelected(menu.findItem(R.id.reminder))
        }

        insurance.setOnClickListener {
            viewModel.onInsurance()
        }

        rovinieta.setOnClickListener {
            viewModel.onServiceClicked("RO_VIGNETTE")
        }

        bridge_tax.setOnClickListener {
            viewModel.onServiceClicked("RO_PASS_TAX")
        }

        carProgressRV.addItemDecoration(
            MarginItemDecoration(20, orientation = LinearLayoutManager.HORIZONTAL)
        )
        carRv.addItemDecoration(
            MarginItemDecoration(20, orientation = LinearLayoutManager.HORIZONTAL)
        )

        cars.setOnClickListener {
            val params = Bundle()
            viewModel.mFirebaseAnalytics.logEvent(FirebaseAnalyticsList.ACCESS_CAR_HOME, params)
            viewModel.onDataClicked(0)
        }

        person.setOnClickListener {
            val params = Bundle()
            viewModel.mFirebaseAnalytics.logEvent(FirebaseAnalyticsList.ACCESS_PERSON_HOME, params)
            viewModel.onDataClicked(1)
        }

        companies.setOnClickListener {
            viewModel.onDataClicked(2)
        }

        viewCar.setOnClickListener {
            viewModel.onDataClicked(0)
        }

        documentLL.setOnClickListener {
            viewModel.onNewDocument()
        }

        reminderLL.setOnClickListener {
            viewModel.onNewReminder()
        }

        viewDocument.setOnClickListener {
            viewModel.onNewDocument()
        }

        viewReminder.setOnClickListener {
            parentFrag.bottomNavigationView.menu.findItem(R.id.reminder).isChecked = true
            parentFrag.viewModel.onItemSelected(menu.findItem(R.id.reminder))
        }

        viewHistory.setOnClickListener {
            viewModel.onHistoryClicked()
        }

        mServices.setOnClickListener {
            parentFrag.bottomNavigationView.menu.findItem(R.id.services).isChecked = true
            parentFrag.viewModel.onItemSelected(menu.findItem(R.id.services))
        }
    }

    @SuppressLint("NotifyDataSetChanged", "SimpleDateFormat")
    override fun setObservers() {

        viewModel.remindersTabData.observe(viewLifecycleOwner) { tags ->
            if (tags != null)
                allFilterList = tags
        }

        viewModel.profileLogoData?.observe(viewLifecycleOwner) { profileLogo ->
            if (profileLogo != null) {

                val options = RequestOptions()
                avatar.clipToOutline = true
                Glide.with(requireContext())
                    .load(profileLogo)
                    .apply(
                        options.fitCenter()
                            .skipMemoryCache(true)
                            .priority(Priority.HIGH)
                            .format(DecodeFormat.PREFER_ARGB_8888)
                    )
                    .into(avatar)

            } else {
                viewModel.getUserLivedata()
            }
        }

        viewModel.userLiveData.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                val imageUrl = viewModel.getProfileImage(requireContext(), user)
                val options = RequestOptions()
                avatar.clipToOutline = true
                Glide.with(requireContext())
                    .load(imageUrl)
                    .diskCacheStrategy(DiskCacheStrategy.DATA)
                    .apply(
                        options.fitCenter()
                            .priority(Priority.HIGH)
                            .format(DecodeFormat.PREFER_ARGB_8888)
                    )
                    .into(avatar)
            }
        }

        viewModel.progressData.observe(viewLifecycleOwner) { progressItem ->
            if (progressItem != null) {
                val profileProgressItem = progressItem.profileProgress
                if (profileProgressItem != null) {
                    firstNameHint.text = profileProgressItem.description.toString()
                        .ifBlank { requireContext().resources.getString(R.string.no_name) }

                    if (profileProgressItem.completionPercent != null) {
                        if (profileProgressItem.completionPercent == 100) {
                            mProfileRL.isVisible = false
                        } else {
                            mProfileRL.isVisible = true
                            profileProgress.max = 100
                            profileProgress.progress = profileProgressItem.completionPercent
                            profileProgressLbl.text =
                                requireContext().resources.getString(
                                    R.string.progress,
                                    profileProgressItem.completionPercent
                                )
                        }

                    } else {
                        profileProgressLbl.text =
                            "${requireContext().resources.getString(R.string.progress, 0)}"
                    }

                } else {
                    firstNameHint.isInvisible = true
                    profileProgressLbl.text =
                        "${requireContext().resources.getString(R.string.progress, 0)}"
                }

                cta_profile.setOnClickListener {
                    profileItem?.let { it1 -> viewModel.onEditProfile(it1) }
                }

                vehicleProgressList = progressItem.vehicleProgress as ArrayList<VehicleProgressItem>
                viewModel.fetchVehicles()
            }
        }

        viewModel.carsLivedata.observe(viewLifecycleOwner) { carList ->
            if (viewLifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {
                if (carList.isNullOrEmpty()) {
                    carLL.isVisible = false
                    noCarRL.isVisible = true
                    carProgressRV.isVisible = false
                } else {
                    noCarRL.isVisible = false
                    carProgressRV.isVisible = true
                    carProgressRV.layoutManager =
                        LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)

                    val car100: ArrayList<VehicleProgressItem> = ArrayList()

                    for (i in vehicleProgressList.indices) {
                        if (vehicleProgressList[i].completionPercent != 100) {
                            car100.add(vehicleProgressList[i])
                        }
                    }
                    carAdapter = CarProgressAdapter(requireContext(), car100, this)
                    carProgressRV.adapter = carAdapter

                    carAdapter.setItems(car100)

                    carLL.isVisible = true
                    carRv.layoutManager =
                        LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
                    carDetailAdapter =
                        DataCarAdapter(requireContext(), cars = arrayListOf(), this, from = "HOME")
                    carRv.adapter = carDetailAdapter

                    carDetailAdapter.setItems(carList)
                }
            }
            progressbar?.dismissPopup()
        }

        viewModel.documentLivedata.observe(viewLifecycleOwner) { docList ->
            if (docList.isNullOrEmpty()) {
                documentLL.isVisible = true
                documentRv.isVisible = false
                viewDocument.isVisible = false
            } else {
                documentLL.isVisible = false
                documentRv.isVisible = true
                viewDocument.isVisible = true
                documentRv.layoutManager =
                    LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
                recentDocAdapter = RecentDocumentAdapter(requireContext(), arrayListOf(), this)
                documentRv.adapter = recentDocAdapter
                recentDocAdapter.setItems(docList)
            }
        }


        viewModel.remindersLiveData.observe(viewLifecycleOwner) { reminderList ->
            if (reminderList.isNullOrEmpty()) {
                reminderLL.isVisible = true
                reminderRv.isVisible = false
                viewReminder.isVisible = false
            } else {

                reminderLL.isVisible = false
                reminderRv.isVisible = true
                viewReminder.isVisible = true

                reminderAdapter = ReminderAdapter(
                    arrayListOf(),
                    this
                )
                reminderRv.adapter = reminderAdapter
                reminderAdapter.clearAll()

                val currentReminderList: ArrayList<Reminder> = ArrayList()
                for (i in reminderList.indices) {

                    val currentDate = Date()
                    if (reminderList[i].dueDate?.after(currentDate) == true) {
                        currentReminderList.add(reminderList[i])
                    }
                }

                currentReminderList.sortWith { o1, o2 ->
                    if (o1.dueTime != null && o2.dueTime != null && o1.dueDate != null && o2.dueDate != null) {
                        val date1 = DateTimeUtils.addStringTimeToDate(o1.dueDate, o1.dueTime)
                        val date2 = DateTimeUtils.addStringTimeToDate(o2.dueDate, o2.dueTime)
                        date1.compareTo(date2)
                    } else {
                        o1.dueDate!!.compareTo(o2.dueDate)
                    }
                }

                val listItems: ArrayList<Reminder> = ArrayList()
                if (currentReminderList.size < 3) {
                    for (i in currentReminderList.indices)
                        listItems.add(currentReminderList[i])
                } else {
                    for (i in 0..2)
                        listItems.add(currentReminderList[i])
                }

                reminderAdapter.setItems(listItems, allFilterList)
            }

        }

        viewModel.historyLiveData.observe(viewLifecycleOwner) { historyList ->

            if (historyList.isNullOrEmpty()) {
                historyLL.isVisible = true
                historyRv.isVisible = false
                viewHistory.isVisible = false
            } else {
                historyLL.isVisible = false
                historyRv.isVisible = true
                viewHistory.isVisible = true
                historyRv.layoutManager =
                    LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
                adapter = HistoryAdapter(requireContext(), arrayListOf(), this, "HOME")
                historyRv.adapter = adapter
                val list: ArrayList<HistoryItem> = ArrayList()
                if (historyList.size < 3) {
                    for (i in historyList.indices)
                        list.add(historyList[i])
                } else {
                    for (i in 0..2)
                        list.add(historyList[i])
                }
                adapter.setItems(list)
            }

        }

    }

    override fun onItemClick(item: VehicleProgressItem) {

        val params = Bundle()
        viewModel.mFirebaseAnalytics.logEvent(
            FirebaseAnalyticsList.ACCESS_CAR_COMPLETE_CARD,
            params
        )

        item.id?.let { viewModel.onEditCar(it) }
    }

    override fun onItemClick(item: Vehicle) {

        val params = Bundle()
        viewModel.mFirebaseAnalytics.logEvent(FirebaseAnalyticsList.ACCESS_CAR_HOME, params)

        item.id.let {
            if (it != null) {
                viewModel.onEditCar(it)
            }
        }
    }

    override fun onBuyClick(item: Vehicle, service: String) {
        when (service) {
            "RO_PASS_TAX" -> {
                viewModel.onBuyPassTax(item)
            }
            "RO_VIGNETTE" -> {
                viewModel.onBuyVignette(item)
            }
            "RO_RCA" -> {
                viewModel.onBuyInsurance(item)
            }
        }
    }

    override fun onDocClick(item: Vehicle, service: String) {
        var url: String? = null
        when (service) {
            "RO_PASS_TAX" -> {
                url = ApiModule.BASE_URL_RESOURCES + item.passTaxDocumentHref
            }
            "RO_VIGNETTE" -> {
                url = ApiModule.BASE_URL_RESOURCES + item.vignetteTicketHref
            }
            "RO_RCA" -> {
                url = ApiModule.BASE_URL_RESOURCES + item.rcaDocumentHref
            }
        }
        if (url != null) {
            val intent = Intent(requireContext(), PdfActivity::class.java)
            intent.putExtra(PdfActivity.ARG_DATA, url)
            intent.putExtra(PdfActivity.ARG_FROM, "DOCUMENT")
            requireContext().startActivity(intent)
        }
    }

    override fun onNotificationClick(item: Vehicle) {
        viewModel.onNotificationClick(item)
    }

    override fun onItemClick(item: HistoryItem) {
        val params = Bundle()
        viewModel.mFirebaseAnalytics.logEvent(FirebaseAnalyticsList.ACCESS_HISTORY_HOME, params)
        viewModel.onHistoryDetail(item)
    }

    override fun onItemClick(item: RowsItem) {

        val params = Bundle()
        viewModel.mFirebaseAnalytics.logEvent(
            FirebaseAnalyticsList.ACCESS_DOCUMENT_HOME,
            params
        )

        val intent = Intent(requireContext(), PdfActivity::class.java)
        intent.putExtra(PdfActivity.ARG_ITEM, item)
        intent.putExtra(PdfActivity.ARG_FROM, "DOCUMENT")
        requireContext().startActivity(intent)
    }

    override fun onResume() {
        super.onResume()

        viewModel.fetchProfileData()

        if (AppPreferencesDelegates.get().language == "en-US") {
            offerOpinion.setImageResource(R.drawable.offer_opinion)
            offerMap.setImageResource(R.drawable.offer_map)
            offerReminder.setImageResource(R.drawable.offer_reminder)
        } else if (AppPreferencesDelegates.get().language == "en-RO") {
            offerOpinion.setImageResource(R.drawable.offer_opinion_ro)
            offerMap.setImageResource(R.drawable.offer_map_ro)
            offerReminder.setImageResource(R.drawable.offer_reminder_ro)
        }

    }

    override fun onItemClick(item: Reminder) {
        viewModel.onUpdate(item)
    }

}