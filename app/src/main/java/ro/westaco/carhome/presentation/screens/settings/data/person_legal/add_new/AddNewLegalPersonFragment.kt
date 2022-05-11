package ro.westaco.carhome.presentation.screens.settings.data.person_legal.add_new

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textfield.TextInputLayout
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
import ro.westaco.carhome.data.sources.remote.responses.models.*
import ro.westaco.carhome.presentation.base.BaseFragment
import ro.westaco.carhome.presentation.screens.settings.data.person_natural.add_new.*
import ro.westaco.carhome.utils.*
import java.util.*


//C-    Add & edit Legal Person
@AndroidEntryPoint
class AddNewLegalPersonFragment : BaseFragment<AddNewLegalPersonViewModel>() {
    private var isEdit = false
    private var legalPersonDetails: LegalPersonDetails? = null
    private var address: Address? = null
    private var caenItem: Caen? = null
    private var activityTypeItem: CatalogItem? = null

    var countries: ArrayList<Country> = ArrayList()
    var sirutaDataList: ArrayList<Siruta> = ArrayList()
    var streetTypeList: ArrayList<CatalogItem> = ArrayList()
    var typePos = 0
    var caendialog: BottomSheetDialog? = null
    var activitydialog: BottomSheetDialog? = null
    var phoneCountryDialog: BottomSheetDialog? = null
    var countydialog: BottomSheetDialog? = null
    var locality: BottomSheetDialog? = null

    var countyPosition = 0
    var localityPosition = 0
    var phoneCountryPosition = 0
    var progressbar: Progressbar? = null
    var localityAdapter: LocalityAdapter? = null

    companion object {

        var selectPhoneCode: String? = null
        const val ARG_IS_EDIT = "arg_is_edit"
        const val ARG_LEGAL_PERSON = "arg_legal_person"

        @SuppressLint("StaticFieldLeak")
        var cna: TextView? = null

        @SuppressLint("StaticFieldLeak")
        var flgs: ImageView? = null

        @SuppressLint("StaticFieldLeak")
        private var in_filleds_county: TextInputLayout? = null

        @SuppressLint("StaticFieldLeak")
        private var in_filleds_locality: TextInputLayout? = null

        @SuppressLint("StaticFieldLeak")
        private var spinnerCity1: TextInputLayout? = null

        @SuppressLint("StaticFieldLeak")
        private var spinnerCounty1: TextInputLayout? = null


        @SuppressLint("StaticFieldLeak")
        lateinit var contexts: Context

        var countryItem: Country? = null
        fun getFlg(item: Country, flagDrawableResId: Int) {
            this.countryItem = item


            cna?.text = item.name
            flgs?.setImageResource(flagDrawableResId)

            if (item.code == "ROU") {
                spinnerCity1?.isVisible = true
                spinnerCounty1?.isVisible = true
                in_filleds_county?.visibility = View.GONE
                in_filleds_locality?.visibility = View.GONE

            } else {
                spinnerCity1?.isVisible = false
                spinnerCounty1?.isVisible = false
                in_filleds_county?.visibility = View.VISIBLE
                in_filleds_locality?.visibility = View.VISIBLE

            }

        }

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

        county.setOnClickListener {

            opencountydialog()
        }



        contexts = requireContext()
        cna = cnmae
        flgs = flg


        in_filleds_county = in_filled_county
        in_filleds_locality = in_filled_locality
        spinnerCity1 = spinnerCity
        spinnerCounty1 = spinnerCounty

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
            selectPhoneCode = romanCode?.key
            phoneFlag.text = CountryCityUtils.getFlagId(
                CountryCityUtils.firstTwo(
                    romanCode?.key?.lowercase(Locale.getDefault()).toString()
                ).toString()
            )

//            phoneFlag.setImageResource(
//                CountryCityUtils.getFlagDrawableResId(CountryCityUtils.firstTwo(
//                    romanCode?.key?.lowercase(Locale.getDefault()).toString()).toString())
//            )

        }

        localitys?.setOnClickListener {
            locality?.show()
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

        cc_dialog.setOnClickListener {
            openPhoneCountryCode()
        }

        cta.setOnClickListener {
            val streetTypeItem =
                sp_quata?.selectedItemPosition?.let { it1 -> streetTypeList[it1].id }
                    ?.let { it2 ->
                        CatalogUtils.findById(
                            streetTypeList,
                            it2
                        )
                    }

            var sirutaCode: Int? = null
            var locality: String? = null
            var region: String? = null

            if (countryItem == null || countryItem?.code == "ROU") {
                for (i in countries.indices) {
                    if (countries[i].code == "ROU") {
                        countryItem = countries[i]
                    }
                }

                val cityList: ArrayList<Siruta> = ArrayList()
                for (i in sirutaDataList.indices) {
                    if (sirutaDataList[i].parentCode == null)
                        cityList.add(sirutaDataList[i])
                }
                sirutaCode = cityList[countyPosition].code
                val localityList: ArrayList<Siruta> = ArrayList()
                for (i in sirutaDataList.indices) {
                    if (sirutaCode == sirutaDataList[i].parentCode) {
                        localityList.add(sirutaDataList[i])
                    }
                }
                region = null
                locality = localityList[localityPosition].name
            } else {
                sirutaCode = null
                region = filled_county.text.toString()
                locality = filled_locality.text.toString()
            }

            address = Address(
                zipCode = zipCode.text.toString(),
                streetType = streetTypeItem,
                sirutaCode = sirutaCode,
                locality = locality,
                streetName = streetName.text.toString(),
                addressDetail = null,
                buildingNo = buildingNo.text.toString(),
                countryCode = countryItem?.code,
                block = blockName.text.toString(),
                region = region,
                entrance = entrance.text.toString(),
                floor = floor.text.toString(),
                apartment = apartment.text.toString()
            )

            val phonePos =
                selectPhoneCode?.let { it1 -> Country.findPositionForTwoLetterCode(countries, it1) }

            var phoneCodeForCountry: String? = null

            if (legalPersonDetails != null) {
                if (phonePos != null)
                    phoneCodeForCountry = countries[phonePos].code

            } else if (legalPersonDetails == null) {

                phoneCodeForCountry = legalPersonDetails?.phoneCountryCode

            } else {
                val phonePosItem = selectPhoneCode?.let { it1 ->
                    Country.findPositionForTwoLetterCode(
                        countries,
                        it1
                    )
                }
                if (phonePosItem != null)
                    phoneCodeForCountry = countries[phonePosItem].code
            }

            viewModel.onSave(
                legalPersonDetails?.id,
                companyName.text.toString(),
                cui.text.toString(),
                noReg.text.toString(),
                address,
                check.isChecked,
                caenItem,
                activityTypeItem,
                isEdit,
                phoneLegal.text.toString(),
                phoneCodeForCountry,
                emailLegal.text.toString(),
                typeTV.text.toString()
            )

        }

        if (isEdit && legalPersonDetails != null) {

            title.text = getString(R.string.edit_legal_pers)
            cta.text = getString(R.string.save_changes)
            companyName.setText(legalPersonDetails?.companyName)
            cui.setText(legalPersonDetails?.cui)
            noReg.setText(legalPersonDetails?.noRegistration)
            phoneLegal.setText(legalPersonDetails?.phone)
            phoneCode.text = legalPersonDetails?.phoneCountryCode
            phoneFlag.text = CountryCityUtils.getFlagId(
                CountryCityUtils.firstTwo(
                    legalPersonDetails?.phoneCountryCode?.lowercase(Locale.getDefault()).toString()
                ).toString()
            )

//            phoneFlag.setImageResource(
//                CountryCityUtils.getFlagDrawableResId(CountryCityUtils.firstTwo(
//                    legalPersonDetails?.phoneCountryCode?.lowercase(Locale.getDefault()).toString())
//                    .toString())
//            )
            emailLegal.setText(legalPersonDetails?.email)

            caenItem = legalPersonDetails?.caen
            activityTypeItem = legalPersonDetails?.activityType
            //  (address) Object
            address = legalPersonDetails?.address


            filled_county.setText(address?.region)
            filled_locality.setText(address?.locality)

            localitys.setText(address?.locality)

            address?.streetType?.id?.let {
                CatalogUtils.findPosById(
                    streetTypeList,
                    it.toLong()
                )
            }?.let {
                sp_quata?.setSelection(
                    it
                )
            }

            streetName.setText(address?.streetName)
            buildingNo.setText(address?.buildingNo)
            blockName.setText(address?.block)
            entrance.setText(address?.entrance)
            apartment.setText(address?.apartment)
            floor.setText(address?.floor)
            zipCode.setText(address?.zipCode)

            if (address?.countryCode == "ROU") {
                spinnerCity.isVisible = true
                spinnerCounty.isVisible = true
                in_filled_county.visibility = View.GONE
                in_filled_locality.visibility = View.GONE
            } else {
                spinnerCity.isVisible = false
                spinnerCounty.isVisible = false
                in_filled_county.visibility = View.VISIBLE
                in_filled_locality.visibility = View.VISIBLE
            }
        } else {
            spinnerCity.isVisible = true
            spinnerCounty.isVisible = true
            in_filled_county.visibility = View.GONE
            in_filled_locality.visibility = View.GONE

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
                override fun OnSelection(pos: Int, model: CatalogItem) {
                    typePos = pos
                    activityTypeItem = model
                }
            }
            val adapter = ActivityTypeAdapter(requireContext(), typeInterface, typePos)
            adapter.arrayList.clear()
            val view = layoutInflater.inflate(R.layout.activity_type_layout, null)
            val mRecycler = view.findViewById<RecyclerView>(R.id.rv_caen)
            val mainRL = view.findViewById<RelativeLayout>(R.id.mainRL)
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

            val typeInterface = object : caenAdapter.TypeInterface {
                override fun OnSelection(pos: Int, model: Caen) {
                    typePos = pos
                    caenItem = model
                }
            }
            val adapter = caenAdapter(requireContext(), typeInterface, typePos)
            adapter.arrayList.clear()
            val view = layoutInflater.inflate(R.layout.nace_items_layout, null)
            val etSearch = view.findViewById<androidx.appcompat.widget.SearchView>(R.id.etSearch)
            val mRecycler = view.findViewById<RecyclerView>(R.id.rv_caen)
            val cancel = view.findViewById<TextView>(R.id.cancel)
            val mClose = view.findViewById<ImageView>(R.id.mClose)
            val mImage = view.findViewById<ImageView>(R.id.mImage)
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

        viewModel.countryData.observe(viewLifecycleOwner) { countryData ->
            this.countries = countryData
            val countryCodeDialog =
                CountryCodeDialog(requireActivity(), countryData, "Legal")
            li_dialog.setOnClickListener {
                countryCodeDialog.show()
            }

            if (isEdit && legalPersonDetails != null) {
                address = legalPersonDetails?.address
                for (i in countries.indices) {
                    if (countries[i].code == address?.countryCode) {
                        cnmae.text = countries[i].name
                        flg.setImageResource(
                            CountryCityUtils.getFlagDrawableResId(
                                countries[i].twoLetterCode.lowercase(
                                    Locale.getDefault()
                                )
                            )
                        )
                    }
                }
            } else {
                for (i in countries.indices) {
                    if (countries[i].code == "ROU") {
                        cnmae.text = countries[i].name
                        flg.setImageResource(
                            CountryCityUtils.getFlagDrawableResId(
                                countries[i].twoLetterCode.lowercase(
                                    Locale.getDefault()
                                )
                            )
                        )
                    }
                }
            }
        }

        viewModel.sirutaData.observe(viewLifecycleOwner) { sirutaData ->
            this.sirutaDataList = sirutaData

            if (isEdit && legalPersonDetails != null) {
                address = legalPersonDetails?.address
                for (i in sirutaDataList.indices) {
                    if (sirutaDataList[i].code == address?.sirutaCode) {
                        county.setText(sirutaDataList[i].name)
                    }
                }
            } else {
                sirutaDataList.sortWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })
                var firstSirutaCode: Int? = null
                for (i in sirutaDataList.indices) {
                    if (sirutaDataList[i].parentCode == null) {
                        firstSirutaCode = sirutaDataList[i].code
                        county.setText(sirutaDataList[i].name)
                        break
                    }
                }

                val localityList: ArrayList<Siruta> = ArrayList()
                for (i in sirutaDataList.indices) {
                    if (firstSirutaCode == sirutaDataList[i].parentCode) {
                        localityList.add(sirutaDataList[i])
                    }
                }
                if (localityList.isNotEmpty()) {
                    localityList.sortWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })
                    localitys.setText(localityList[0].name)
                }
            }

            progressbar?.dismissPopup()

        }

        viewModel.streetTypeData.observe(viewLifecycleOwner) { streetTypeList ->
            this.streetTypeList = streetTypeList
            val arryadapter =
                ArrayAdapter(requireContext(), R.layout.drop_down_list, streetTypeList)
            sp_quata.adapter = arryadapter
        }

    }

    private fun setupFullHeight(bottomSheet: View) {
        val layoutParams = bottomSheet.layoutParams
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT
        bottomSheet.layoutParams = layoutParams
    }

    private fun openPhoneCountryCode() {

        phoneCountryDialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.phone_country_code, null)

        val mRecycler: RecyclerView? = view.findViewById(R.id.mRecycler)
//        val mClose: TextView? = view.findViewById(R.id.mClose)
//        val mDone: TextView? = view.findViewById(R.id.mDone)

        phoneCountryDialog?.setCancelable(true)
        phoneCountryDialog?.setContentView(view)

        val phoneModelList: ArrayList<PhoneCodeModel> = ArrayList()
        val obj = FileUtil.loadJSONFromAsset(requireContext())?.let { JSONObject(it) }

        if (obj != null) {
            for (key in obj.keys()) {
                val keyStr = key as String
                val keyValue = obj.get(keyStr)
                val code = PhoneCodeModel(keyStr, keyValue as String?)
                phoneModelList.add(code)
            }
        }

        mRecycler?.layoutManager = LinearLayoutManager(context)
        mRecycler?.adapter = PhoneCountryCode(requireContext(),
            phoneModelList, object : PhoneCountryCode.countryCodePhone {
                @SuppressLint("SetTextI18n")
                override fun phoneCountry(countries: PhoneCodeModel, position: Int) {
                    phoneCode?.text = "+ ${countries.value}"
                    selectPhoneCode = countries.key
                    //
                    phoneFlag.text = CountryCityUtils.getFlagId(
                        CountryCityUtils.firstTwo(
                            countries.key?.lowercase(Locale.getDefault()).toString()
                        ).toString()
                    )
//                    phoneFlag.setImageResource(
//
//                    )
                    phoneCountryDialog?.dismiss()
                }
            })

//        mClose?.setOnClickListener {
//            phoneCountryDialog?.dismiss()
//        }

//        mDone?.setOnClickListener {
//
//            if (countryList != null){
//
//            }else{
//                phoneCode?.text = "+ ${romanCode?.value}"
//                phoneFlag.setImageResource(
//                    CountryCityUtils.getFlagDrawableResId(CountryCityUtils.firstTwo(
//                        romanCode?.key?.lowercase(Locale.getDefault()).toString()).toString()))
//            }
//
//            phoneCountryDialog?.dismiss()
//        }

        phoneCountryDialog?.show()

    }

    private fun opencountydialog() {

        val cityList = mutableListOf<Siruta>()
        val view = layoutInflater.inflate(R.layout.county_layout, null)
        countydialog = BottomSheetDialog(requireContext())
        countydialog?.setCancelable(true)
        countydialog?.setContentView(view)
        countydialog!!.show()

        val rv_county: RecyclerView? = view.findViewById(R.id.rv_county)
        val etSearchTrain: EditText? = view.findViewById(R.id.etSearchTrain)
        val mClose: ImageView? = view.findViewById(R.id.mClose)
        rv_county?.layoutManager = LinearLayoutManager(requireContext())

        if (sirutaDataList.isNotEmpty()) {

            for (i in sirutaDataList.indices) {

                if (sirutaDataList[i].parentCode == null)

                    cityList.add(sirutaDataList[i])
            }

            cityList.sortWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })

            val adapter = CountyAdapter(
                requireContext(),
                cityList,
                countyPosition = countyPosition,
                countyListClick = object :
                    CountyListClick {
                    override fun click(position: Int, code: Siruta) {
                        countyPosition = position
                        county.setText(code.name)
                        locality(code.code.toString())
                        countydialog?.dismiss()

                    }
                })
            rv_county?.adapter = adapter

            etSearchTrain?.addTextChangedListener(object : TextWatcher {

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {

                }

                @SuppressLint("NotifyDataSetChanged")
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                    if (s.toString().isNotEmpty()) {

                        adapter.filter.filter(s.toString())

                    } else {


                    }

                }

                override fun afterTextChanged(s: Editable?) {

                }


            })

        }

        mClose?.setOnClickListener {

            countydialog?.dismiss()
        }

    }

    private fun locality(code: String) {

        var city = mutableListOf<Siruta>()

        val view = layoutInflater.inflate(R.layout.locality_layout, null)
        locality = BottomSheetDialog(requireContext())
        locality?.setCancelable(true)
        locality?.setContentView(view)

        val rv_locality: RecyclerView? = view.findViewById(R.id.rv_locality)
        val etSearchTrain: EditText? = view.findViewById(R.id.etSearchTrain)
        val mClose: ImageView? = view.findViewById(R.id.mClose)
        rv_locality?.layoutManager = LinearLayoutManager(requireContext())

        if (sirutaDataList.isNotEmpty()) {

            for (i in sirutaDataList.indices) {
                if (code == sirutaDataList[i].parentCode.toString()) {
                    city.add(sirutaDataList[i])
                }
            }

            city.sortWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })
            localityAdapter = LocalityAdapter(
                requireContext(),
                city,
                localityListClick = object : LocalityAdapter.LocalityListClick {
                    override fun localityclick(position: Int, siruta: Siruta) {
                        localityPosition = position
                        localitys.setText(siruta.name)
                        locality?.dismiss()
                    }
                })

            rv_locality?.adapter = localityAdapter

            etSearchTrain?.addTextChangedListener(object : TextWatcher {

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {

                }

                @SuppressLint("NotifyDataSetChanged")
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                    if (s.toString().isNotEmpty()) {

                        localityAdapter?.filter?.filter(s.toString())

                    } else {


                    }

                }

                override fun afterTextChanged(s: Editable?) {

                }


            })
        }


        mClose?.setOnClickListener {

            locality?.dismiss()
        }

    }


}