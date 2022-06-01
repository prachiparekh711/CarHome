package ro.westaco.carhome.presentation.screens.service.person.legal.addlegal

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_add_bill_legal.*
import kotlinx.android.synthetic.main.fragment_add_bill_legal.apartment
import kotlinx.android.synthetic.main.fragment_add_bill_legal.buildingNo
import kotlinx.android.synthetic.main.fragment_add_bill_legal.entrance
import kotlinx.android.synthetic.main.fragment_add_bill_legal.floor
import kotlinx.android.synthetic.main.fragment_add_bill_legal.streetName
import kotlinx.android.synthetic.main.fragment_add_bill_legal.zipCode
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.requests.Address
import ro.westaco.carhome.data.sources.remote.responses.models.*
import ro.westaco.carhome.presentation.base.BaseFragment
import ro.westaco.carhome.presentation.interfaceitem.CountyListClick
import ro.westaco.carhome.presentation.screens.data.commen.CountryCodeDialog
import ro.westaco.carhome.presentation.screens.data.commen.CountyAdapter
import ro.westaco.carhome.presentation.screens.data.commen.LocalityAdapter
import ro.westaco.carhome.presentation.screens.data.person_legal.add_new.ActivityTypeAdapter
import ro.westaco.carhome.presentation.screens.data.person_legal.add_new.CaenAdapter
import ro.westaco.carhome.utils.*
import ro.westaco.carhome.utils.DialogUtils.Companion.showErrorInfo
import java.util.*


@AndroidEntryPoint
class AddBillLegalFragment : BaseFragment<AddBillLegalViewModel>(),
    CountryCodeDialog.CountryCodePicker {

    private var legalPersonDetails: LegalPersonDetails? = null
    private var caenItem: Caen? = null
    private var activityTypeItem: CatalogItem? = null
    var typePos = 0
    var caendialog: BottomSheetDialog? = null
    var activitydialog: BottomSheetDialog? = null

    var address: Address? = null
    var countriesList: ArrayList<Country> = ArrayList()
    var streetTypeList: ArrayList<CatalogItem> = ArrayList()
    var progressbar: Progressbar? = null
    var countyPosition: Int? = null
    var localityPosition: Int? = null
    var countyDialog: BottomSheetDialog? = null
    var localityDialog: BottomSheetDialog? = null
    var countryItem: Country? = null
    var cityList: ArrayList<Siruta> = ArrayList()


    override fun getContentView(): Int {
        return R.layout.fragment_add_bill_legal
    }

    override fun initUi() {

        progressbar = Progressbar(requireContext())
        progressbar?.showPopup()


        name_in_lay.setOnClickListener {

            if (cd_hidden_view.visibility == View.VISIBLE) {
                ViewUtils.collapse(cd_hidden_view)
                name_arrow.setImageResource(R.drawable.ic_arrow_circle_down)
                name_in_lay.setBackgroundResource(R.color.white)
            } else {
                ViewUtils.expand(cd_hidden_view)
                name_arrow.setImageResource(R.drawable.ic_arrow_circle_up)
                name_in_lay.setBackgroundResource(R.color.expande_colore)
            }
        }

        address_in_lay.setOnClickListener {

            if (adds_hidden_view.visibility == View.VISIBLE) {
                ViewUtils.collapse(adds_hidden_view)
                address_arrow.setImageResource(R.drawable.ic_arrow_circle_down)
                address_in_lay.setBackgroundResource(R.color.white)
            } else {
                ViewUtils.expand(adds_hidden_view)
                address_arrow.setImageResource(R.drawable.ic_arrow_circle_up)
                address_in_lay.setBackgroundResource(R.color.expande_colore)
            }
        }

        countySpinnerText.setOnClickListener {
            openCountyDialog()
        }

        localitySpinnerText?.setOnClickListener {
            localityDialog?.show()
        }

        toolbar.setNavigationOnClickListener {

            viewModel.onBack()
        }

        cancel.setOnClickListener {
            viewModel.onBack()
        }

        root.setOnClickListener {
            viewModel.onRootClicked()
        }

        changeCountryState("ROU")
        countySpinnerText.setText(SirutaUtil.defaultCounty?.name)
        localitySpinnerText.setText(SirutaUtil.defaultCity?.name)

        countyPosition = SirutaUtil.defaultCounty?.name?.let { SirutaUtil.fetchCountyPosition(it) }
        localityPosition = SirutaUtil.defaultCity?.name?.let { SirutaUtil.fetchCountyPosition(it) }

        cta_bill_legal.setOnClickListener {
            val streetTypeItem =
                sp_quata?.selectedItemPosition?.let { it1 -> streetTypeList[it1].id }
                    ?.let { it2 ->
                        CatalogUtils.findById(
                            streetTypeList,
                            it2
                        )
                    }

            var regionStr: String? = null
            var sirutaCode: Int? = null
            var localityStr: String? = null


            if (countryItem?.code == "ROU") {
                if (countyPosition != -1 && localityPosition != -1) {
                    regionStr = countyPosition?.let { SirutaUtil.countyList[it].name }
                    sirutaCode = localityPosition?.let { cityList[it].code }
                    localityStr = localityPosition?.let { cityList[it].name }
                } else {
                    regionStr = SirutaUtil.defaultCounty?.name
                    sirutaCode = SirutaUtil.defaultCity?.code
                    localityStr = SirutaUtil.defaultCity?.name
                }
            } else {
                regionStr = stateProvinceText.text.toString()
                sirutaCode = null
                localityStr = localityAreaText.text.toString()
            }

            var addressItem: Address? = null

            if (regionStr != null && localityStr != null) {
                addressItem = Address(
                    zipCode = zipCode.text.toString(),
                    streetType = streetTypeItem,
                    sirutaCode = sirutaCode,
                    locality = localityStr,
                    streetName = streetName.text.toString(),
                    addressDetail = null,
                    buildingNo = buildingNo.text.toString(),
                    countryCode = countryItem?.code,
                    block = blockName.text.toString(),
                    region = regionStr,
                    entrance = entrance.text.toString(),
                    floor = floor.text.toString(),
                    apartment = apartment.text.toString()
                )
            } else {
                showErrorInfo(requireContext(), getString(R.string.address_require))
            }

            if (companyName.text?.isNotEmpty() == true) {

                if (cui.text?.isNotEmpty() == true) {

                    if (cui.length() >= 2) {

                        if (naceTV.text?.isNotEmpty() == true) {

                            if (typeTV.text?.isNotEmpty() == true) {

                                if (check.isChecked) {

                                    viewModel.onSave(
                                        legalPersonDetails?.id,
                                        companyName.text.toString(),
                                        cui.text.toString(),
                                        noReg.text.toString(),
                                        addressItem,
                                        check.isChecked,
                                        caenItem,
                                        activityTypeItem
                                    )

                                } else {
                                    showErrorInfo(
                                        requireContext(),
                                        getString(R.string.confirm_details)
                                    )
                                }
                            } else {
                                showErrorInfo(
                                    requireContext(),
                                    getString(R.string.Activity_type_empty)
                                )
                            }
                        } else {
                            showErrorInfo(requireContext(), getString(R.string.caen_empty))
                        }
                    } else {
                        showErrorInfo(requireContext(), getString(R.string.invalid_cui))
                    }

                } else {
                    showErrorInfo(requireContext(), getString(R.string.cui_empty))
                }
            } else {
                showErrorInfo(requireContext(), getString(R.string.company_empty))
            }

        }

    }

    override fun setObservers() {

        viewModel.countryData.observe(viewLifecycleOwner) { countryData ->
            if (countryData != null) {
                this.countriesList = countryData
                li_dialog.setOnClickListener {
                    val countryCodeDialog =
                        CountryCodeDialog(requireActivity(), countriesList, this)
                    countryCodeDialog.show(requireActivity().supportFragmentManager, null)
                }

                val pos = Country.findPositionForCode(countriesList, "ROU")
                this.countryItem = pos.let { countriesList[it] }
                countryNameTV.text = countryItem?.name
                countryItem?.twoLetterCode?.lowercase(
                    Locale.getDefault()
                )?.let { CountryCityUtils.getFlagId(it) }?.let { cuntryFlagIV.text = it }
            }

            progressbar?.dismissPopup()

        }

        viewModel.streetTypeData.observe(viewLifecycleOwner) { streetTypeData ->
            if (streetTypeData != null) {
                this.streetTypeList = streetTypeData

                val arrayAdapter =
                    ArrayAdapter(requireContext(), R.layout.drop_down_list, streetTypeList)
                sp_quata.adapter = arrayAdapter
            }
        }

        viewModel.activityTypeData.observe(viewLifecycleOwner) { activityTypeList ->

            activitydialog = BottomSheetDialog(requireContext())

            val typeInterface = object : ActivityTypeAdapter.TypeInterface {
                override fun onSelection(pos: Int, model: CatalogItem) {
                    typePos = pos
                    activityTypeItem = model
                }
            }
            val adapter = ActivityTypeAdapter(requireContext(), typeInterface, typePos)
            adapter.arrayList.clear()
            val view = layoutInflater.inflate(R.layout.activity_type_layout, null)
            val mRecycler = view.findViewById<RecyclerView>(R.id.rv_caen)
            val cancel = view.findViewById<TextView>(R.id.cancel)
            val mClose = view.findViewById<ImageView>(R.id.mClose)
            val cta = view.findViewById<TextView>(R.id.cta)

            mRecycler.layoutManager =
                LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            mRecycler.adapter = adapter
            if (activityTypeList != null) {
                adapter.addAll(activityTypeList)
            }
            activitydialog?.setCancelable(false)
            activitydialog?.setContentView(view)

            typeTV?.setOnClickListener {
                activitydialog?.show()
            }

            cancel.setOnClickListener { activitydialog?.dismiss() }
            mClose.setOnClickListener { activitydialog?.dismiss() }
            cta.setOnClickListener {
                typeTV.setText(activityTypeList?.get(typePos)?.name)
                activitydialog?.dismiss()
            }
        }

        viewModel.caenData.observe(viewLifecycleOwner) { caenList ->

            caendialog = BottomSheetDialog(requireContext())


            val typeInterface = object : CaenAdapter.TypeInterface {
                override fun onSelection(pos: Int, model: Caen) {
                    typePos = pos
                    caenItem = model
                }
            }
            val adapter = CaenAdapter(requireContext(), typeInterface, typePos)
            adapter.arrayList.clear()
            val view = layoutInflater.inflate(R.layout.nace_items_layout, null)
            val etSearch = view.findViewById<androidx.appcompat.widget.SearchView>(R.id.etSearch)
            val mRecycler = view.findViewById<RecyclerView>(R.id.rv_caen)
            val cancel = view.findViewById<TextView>(R.id.cancel)
            val mClose = view.findViewById<ImageView>(R.id.mClose)
            val cta = view.findViewById<TextView>(R.id.cta)

            mRecycler.layoutManager =
                LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            mRecycler.layoutAnimation = null
            mRecycler.adapter = adapter
            if (caenList != null) {
                adapter.addAll(caenList)
            }
            adapter.filter.filter("")
            caendialog?.setCancelable(false)
            caendialog?.setContentView(view)
            caendialog?.setOnShowListener {
                val bottomSheetDialog = it as BottomSheetDialog
                val parentLayout =
                    bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
                parentLayout?.let { it ->
                    val behaviour = BottomSheetBehavior.from(it)
                    setupFullHeight(it)
                    behaviour.state = BottomSheetBehavior.STATE_EXPANDED
                }
            }


            etSearch.setOnQueryTextListener(object :
                androidx.appcompat.widget.SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String): Boolean {
                    adapter.filter.filter(newText)
                    return true
                }
            })

            naceTV?.setOnClickListener {
                caendialog?.show()
            }

            cancel.setOnClickListener { caendialog?.dismiss() }
            mClose.setOnClickListener { caendialog?.dismiss() }
            cta.setOnClickListener {
                naceTV.setText(caenItem?.name)
                caendialog?.dismiss()
            }
        }


    }


    private fun setupFullHeight(bottomSheet: View) {
        val layoutParams = bottomSheet.layoutParams
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT
        bottomSheet.layoutParams = layoutParams
    }

    private fun openCountyDialog() {

        val view = layoutInflater.inflate(R.layout.county_layout, null)
        countyDialog = BottomSheetDialog(requireContext())
        countyDialog?.setCancelable(true)
        countyDialog?.setContentView(view)
        countyDialog?.show()

        val rvCounty: RecyclerView? = view.findViewById(R.id.rv_county)
        val etSearchTrain: EditText? = view.findViewById(R.id.etSearchTrain)
        val mClose: ImageView? = view.findViewById(R.id.mClose)
        rvCounty?.layoutManager = LinearLayoutManager(requireContext())

        if (SirutaUtil.countyList.isNotEmpty()) {

            val adapter =

                CountyAdapter(
                    requireContext(),
                    SirutaUtil.countyList,
                    countyCode = SirutaUtil.fetchCounty(countySpinnerText.text.toString())?.code
                        ?: SirutaUtil.countyList[0].code,
                    countyListClick = object :
                        CountyListClick {
                        override fun click(position: Int, code: Siruta) {
                            countyPosition = position
                            countySpinnerText.setText(code.name)
                            localityBlock(code)
                            countyDialog?.dismiss()
                        }
                    })


            rvCounty?.adapter = adapter
            adapter.filter.filter("")

            etSearchTrain?.addTextChangedListener(object : TextWatcher {

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                    if (s.toString().isNotEmpty()) {
                        adapter.filter.filter(s.toString())
                    } else {
                        adapter.filter.filter("")
                    }
                }

                override fun afterTextChanged(s: Editable?) {

                }


            })

        }

        mClose?.setOnClickListener {
            countyDialog?.dismiss()
        }


    }

    private fun localityBlock(county: Siruta) {

        val view = layoutInflater.inflate(R.layout.locality_layout, null)
        localityDialog = BottomSheetDialog(requireContext())
        localityDialog?.setCancelable(true)
        localityDialog?.setContentView(view)

        val rvLocality: RecyclerView? = view.findViewById(R.id.rv_locality)
        val etSearchTrain: EditText? = view.findViewById(R.id.etSearchTrain)
        val mClose: ImageView? = view.findViewById(R.id.mClose)
        rvLocality?.layoutManager = LinearLayoutManager(requireContext())

        cityList = SirutaUtil.fetchCity(county)


        localitySpinnerText.setText(cityList[0].name)
        val adapter = LocalityAdapter(
            requireContext(),
            cityList,
            localityListClick = object : LocalityAdapter.LocalityListClick {
                override fun localityclick(position: Int, siruta: Siruta) {
                    localityPosition = position
                    localitySpinnerText.setText(siruta.name)
                    localityDialog?.dismiss()
                }
            })
        rvLocality?.adapter = adapter

        etSearchTrain?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString().isNotEmpty()) {
                    adapter.filter.filter(s.toString())
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })

        mClose?.setOnClickListener {
            localityDialog?.dismiss()
        }

    }

    override fun OnCountryPick(item: Country, flagDrawableResId: String) {
        this.countryItem = item
        countryNameTV.text = item.name
        cuntryFlagIV.text = flagDrawableResId
        changeCountryState(item.code)
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


}