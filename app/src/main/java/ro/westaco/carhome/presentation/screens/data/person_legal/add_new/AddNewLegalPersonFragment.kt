package ro.westaco.carhome.presentation.screens.data.person_legal.add_new

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_add_new_legal_person.*
import kotlinx.android.synthetic.main.fragment_add_new_legal_person.apartment
import kotlinx.android.synthetic.main.fragment_add_new_legal_person.buildingNo
import kotlinx.android.synthetic.main.fragment_add_new_legal_person.entrance
import kotlinx.android.synthetic.main.fragment_add_new_legal_person.floor
import kotlinx.android.synthetic.main.fragment_add_new_legal_person.streetName
import kotlinx.android.synthetic.main.fragment_add_new_legal_person.zipCode
import org.json.JSONObject
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.requests.Address
import ro.westaco.carhome.data.sources.remote.requests.PhoneCodeModel
import ro.westaco.carhome.data.sources.remote.responses.models.*
import ro.westaco.carhome.presentation.base.BaseFragment
import ro.westaco.carhome.presentation.interfaceitem.CountyListClick
import ro.westaco.carhome.presentation.screens.data.commen.CodeDialog
import ro.westaco.carhome.presentation.screens.data.commen.CountryCodeDialog
import ro.westaco.carhome.presentation.screens.data.commen.CountyAdapter
import ro.westaco.carhome.presentation.screens.data.commen.LocalityAdapter
import ro.westaco.carhome.utils.*
import ro.westaco.carhome.utils.DialogUtils.Companion.showErrorInfo
import ro.westaco.carhome.utils.SirutaUtil.Companion.countyList
import ro.westaco.carhome.utils.SirutaUtil.Companion.defaultCity
import ro.westaco.carhome.utils.SirutaUtil.Companion.defaultCounty
import ro.westaco.carhome.utils.SirutaUtil.Companion.fetchCity
import ro.westaco.carhome.utils.SirutaUtil.Companion.fetchCounty
import ro.westaco.carhome.utils.SirutaUtil.Companion.fetchCountyPosition
import java.util.*

@AndroidEntryPoint
class AddNewLegalPersonFragment : BaseFragment<AddNewLegalPersonViewModel>(),
    CountryCodeDialog.CountryCodePicker,
    CodeDialog.CountyPickerItems {
    private var isEdit = false
    private var legalPersonDetails: LegalPersonDetails? = null

    private var caenItem: Caen? = null
    private var activityTypeItem: CatalogItem? = null

    var streetTypeList: ArrayList<CatalogItem> = ArrayList()
    var typePos = 0
    var caendialog: BottomSheetDialog? = null
    var activitydialog: BottomSheetDialog? = null
    var phoneCountryDialog: BottomSheetDialog? = null
    var progressbar: Progressbar? = null

    private var address: Address? = null
    var selectedPhoneCode: String? = null
    var countriesList: ArrayList<Country> = ArrayList()
    var cityList: ArrayList<Siruta> = ArrayList()
    var countryItem: Country? = null
    var countyPosition: Int? = null
    var localityPosition: Int? = null
    var countyDialog: BottomSheetDialog? = null
    var localityDialog: BottomSheetDialog? = null

    companion object {
        const val ARG_IS_EDIT = "arg_is_edit"
        const val ARG_LEGAL_PERSON = "arg_legal_person"
    }

    override fun getContentView() = R.layout.fragment_add_new_legal_person

    override fun getStatusBarColor() = ContextCompat.getColor(requireContext(), R.color.white)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            isEdit = it.getBoolean(ARG_IS_EDIT)
            legalPersonDetails = it.getSerializable(ARG_LEGAL_PERSON) as? LegalPersonDetails?

        }
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

        if (legalPersonDetails == null) {
            val phoneModelList: ArrayList<PhoneCodeModel> = ArrayList()
            val obj = FileUtil.loadJSONFromAsset(requireContext())?.let { JSONObject(it) }
            var romanCode: PhoneCodeModel? = null

            if (obj != null) {
                for (key in obj.keys()) {
                    val keyStr = key as String
                    val keyValue = obj.get(keyStr)
                    val code = PhoneCodeModel(keyStr, keyValue as String?)
                    phoneModelList.add(code)
                    if (code.key == "RO") {
                        romanCode = code
                    }
                }
            }

            phoneCode?.text = "+ ${romanCode?.value}"
            selectedPhoneCode = romanCode?.key
            phoneFlag.text = CountryCityUtils.getFlagId(
                CountryCityUtils.firstTwo(
                    romanCode?.key?.lowercase(Locale.getDefault()).toString()
                ).toString()
            )

        }

        back.setOnClickListener {
            viewModel.onBack()
        }

        cancel.setOnClickListener {
            viewModel.onBack()

        }

        root.setOnClickListener {
            viewModel.onRootClicked()
        }


        cta.setOnClickListener {

            val streetTypeItem =
                sp_quata?.selectedItemPosition?.let { it1 -> streetTypeList[it1].id }?.let { it2 ->
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
                    regionStr = countyPosition?.let { countyList[it].name }
                    sirutaCode = localityPosition?.let { cityList[it].code }
                    localityStr = localityPosition?.let { cityList[it].name }
                } else {
                    regionStr = defaultCounty?.name
                    sirutaCode = defaultCity?.code
                    localityStr = defaultCity?.name
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

            val phonePos = selectedPhoneCode?.let { it1 ->
                Country.findPositionForTwoLetterCode(
                    countriesList,
                    it1
                )
            }

            var phoneCodeForCountry: String? = null
            if (legalPersonDetails != null) {
                if (phonePos != null)
                    phoneCodeForCountry = countriesList[phonePos].code
            } else if (legalPersonDetails == null) {
                phoneCodeForCountry = legalPersonDetails?.phoneCountryCode
            } else {
                val phonePosItem = selectedPhoneCode?.let { it1 ->
                    Country.findPositionForTwoLetterCode(
                        countriesList,
                        it1
                    )
                }
                if (phonePosItem != null)
                    phoneCodeForCountry = countriesList[phonePosItem].code
            }

            if (RegexData.checkRegCompanyRegex(noReg.text.toString())) {

                if (companyName.text?.isNotEmpty() == true) {

                    if (cui.text?.isNotEmpty() == true) {

                        if (cui.text?.length == 9) {

                            if (typeTV.text?.isNotEmpty() == true) {

                                if (check.isChecked) {

                                    if (DeviceUtils.isOnline(requireActivity().application)) {

                                        if (RegexData.checkCUIRegex(cui.text.toString())) {

                                            viewModel.onSave(
                                                legalPersonDetails?.id,
                                                companyName.text.toString(),
                                                cui.text.toString(),
                                                noReg.text.toString(),
                                                addressItem,
                                                check.isChecked,
                                                caenItem,
                                                activityTypeItem,
                                                isEdit,
                                                phoneLegal.text.toString(),
                                                phoneCodeForCountry,
                                                emailLegal.text.toString(),
                                                typeTV.text.toString()
                                            )

                                        } else {
                                            showErrorInfo(
                                                requireContext(),
                                                getString(R.string.reg_invalid_cui)
                                            )
                                        }

                                    } else {
                                        showErrorInfo(
                                            requireContext(),
                                            getString(R.string.int_not_connect)
                                        )
                                    }
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
                            showErrorInfo(requireContext(), getString(R.string.invalid_cui))
                        }
                    } else {
                        showErrorInfo(requireContext(), getString(R.string.cui_empty))
                    }

                } else {
                    showErrorInfo(requireContext(), getString(R.string.company_empty))
                }

            } else {
                showErrorInfo(requireContext(), getString(R.string.reg_invalid_reg_company))
            }

        }

        if (isEdit && legalPersonDetails != null) {
            address = legalPersonDetails?.address
            title.text = getString(R.string.edit_legal_pers)
            cta.text = getString(R.string.save_changes)
            companyName.setText(legalPersonDetails?.companyName)
            cui.setText(legalPersonDetails?.cui)
            noReg.setText(legalPersonDetails?.noRegistration)
            phoneLegal.setText(legalPersonDetails?.phone)
            emailLegal.setText(legalPersonDetails?.email)

            caenItem = legalPersonDetails?.caen
            activityTypeItem = legalPersonDetails?.activityType

            address?.streetType?.id?.let { CatalogUtils.findPosById(streetTypeList, it) }
                ?.let { sp_quata?.setSelection(it) }

            streetName.setText(address?.streetName)
            buildingNo.setText(address?.buildingNo)
            blockName.setText(address?.block)
            entrance.setText(address?.entrance)
            apartment.setText(address?.apartment)
            floor.setText(address?.floor)
            zipCode.setText(address?.zipCode)

            address?.countryCode?.let { changeCountryState(it) }
            if (address?.countryCode == "ROU") {
                countySpinnerText.setText(address?.region)
                localitySpinnerText.setText(address?.locality)
            } else {
                stateProvinceText.setText(address?.region)
                localityAreaText.setText(address?.locality)
            }

            countyPosition = address?.region?.let { fetchCountyPosition(it) }

            localityPosition = address?.locality?.let { fetchCountyPosition(it) }

        } else {
            changeCountryState("ROU")
            countySpinnerText.setText(defaultCounty?.name)
            localitySpinnerText.setText(defaultCity?.name)

            countyPosition = defaultCounty?.name?.let { fetchCountyPosition(it) }
            localityPosition = defaultCity?.name?.let { fetchCountyPosition(it) }

        }
    }

    override fun setObservers() {

        viewModel.activityTypeData.observe(viewLifecycleOwner) { activityTypeList ->

            activitydialog = BottomSheetDialog(requireContext())
            if (isEdit && legalPersonDetails != null) {
                for (i in activityTypeList.indices) {
                    if (activityTypeList[i].id == legalPersonDetails?.activityType?.id) {
                        typePos = i
                        activityTypeItem = activityTypeList[typePos]
                        typeTV.setText(activityTypeList[i].toString())
                    }
                }
            }

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
            adapter.addAll(activityTypeList)
            activitydialog?.setCancelable(false)
            activitydialog?.setContentView(view)

            typeTV?.setOnClickListener {
                activitydialog?.show()
            }

            cancel.setOnClickListener { activitydialog?.dismiss() }
            mClose.setOnClickListener { activitydialog?.dismiss() }
            cta.setOnClickListener {
                typeTV.setText(activityTypeList[typePos].name)
                activitydialog?.dismiss()
            }
        }

        viewModel.caenData.observe(viewLifecycleOwner) { caenList ->

            caendialog = BottomSheetDialog(requireContext())

            if (isEdit && legalPersonDetails != null) {
                for (i in caenList.indices) {
                    if (caenList[i].code == legalPersonDetails?.caen?.code) {
                        typePos = i
                        caenItem = caenList[typePos]
                        naceTV.setText(caenList[i].toString())
                    }
                }
            }

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
            adapter.addAll(caenList)
            adapter.filter.filter("")
            caendialog?.setCancelable(false)
            caendialog?.setContentView(view)
            caendialog?.setOnShowListener {
                val bottomSheetDialog = it as BottomSheetDialog
                val parentLayout =
                    bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
                parentLayout?.let { it1 ->
                    val behaviour = BottomSheetBehavior.from(it1)
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

        viewModel.countryData.observe(viewLifecycleOwner) { countryData ->
            if (countryData != null) {
                this.countriesList = countryData
                val countryCodeDialog = CountryCodeDialog(requireActivity(), countriesList, this)
                li_dialog.setOnClickListener {
                    countryCodeDialog.show(requireActivity().supportFragmentManager, null)
                }

                if (isEdit && legalPersonDetails != null) {
                    val pos = address?.countryCode?.let {
                        Country.findPositionForCode(
                            countriesList,
                            it
                        )
                    }
                    this.countryItem = pos?.let { countriesList[it] }
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
                } else {
                    val pos = Country.findPositionForCode(countriesList, "ROU")
                    this.countryItem = pos.let { countriesList[it] }
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
                }
            }
            setPhoneCountryData()
            progressbar?.dismissPopup()
        }


        viewModel.streetTypeData.observe(viewLifecycleOwner) { streetTypeList ->
            this.streetTypeList = streetTypeList
            val arryadapter =
                ArrayAdapter(requireContext(), R.layout.drop_down_list, streetTypeList)
            sp_quata.adapter = arryadapter
        }

    }

    private fun setPhoneCountryData() {
        val phoneModelList: ArrayList<PhoneCodeModel> = ArrayList()
        val obj = FileUtil.loadJSONFromAsset(requireContext())?.let { JSONObject(it) }
        var phoneCountryItem: Country? = null
        if (isEdit && legalPersonDetails != null) {
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
        obj?.keys()?.forEach { key ->
            val keyStr = key as String
            val keyvalue = obj.get(keyStr)
            val code = PhoneCodeModel(keyStr, keyvalue as String?)
            phoneModelList.add(code)
            if (phoneCountryItem != null && phoneCountryItem.twoLetterCode == code.key) {
                romanCode = code
            } else {
                if (code.key == "RO" && romanCode == null) {
                    romanCode = code
                }
            }
        }

        phoneCode.text = "+ ${romanCode?.value}"
        selectedPhoneCode = romanCode?.key
        phoneFlag.text =
            CountryCityUtils.getFlagId(
                CountryCityUtils.firstTwo(
                    romanCode?.key?.lowercase(Locale.getDefault()).toString()
                ).toString()
            )

        val phoneCodeDialog = CodeDialog(requireActivity(), phoneModelList, this)

        cc_dialog.setOnClickListener {
            phoneCodeDialog.show(requireActivity().supportFragmentManager, null)
        }
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

        if (countyList.isNotEmpty()) {

            val adapter =
                CountyAdapter(
                    requireContext(),
                    countyList,
                    countyCode = fetchCounty(countySpinnerText.text.toString())?.code
                        ?: countyList[0].code,
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
                    after: Int,
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

        cityList = fetchCity(county)


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
                after: Int,
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

    @SuppressLint("SetTextI18n")
    override fun pickCountry(countries: PhoneCodeModel) {
        phoneCode.text = "+ ${countries.value}"
        selectedPhoneCode = countries.key
        phoneFlag.text =
            CountryCityUtils.getFlagId(
                CountryCityUtils.firstTwo(
                    countries.key?.lowercase(Locale.getDefault()).toString()
                ).toString()
            )
    }

}