package ro.westaco.carhome.presentation.screens.service.person.legal.addlegal

import android.annotation.SuppressLint
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_add_bill_legal.*
import kotlinx.android.synthetic.main.fragment_add_new_legal_person.address_arrow
import kotlinx.android.synthetic.main.fragment_add_new_legal_person.address_in_lay
import kotlinx.android.synthetic.main.fragment_add_new_legal_person.adds_hidden_view
import kotlinx.android.synthetic.main.fragment_add_new_legal_person.apartment
import kotlinx.android.synthetic.main.fragment_add_new_legal_person.blockName
import kotlinx.android.synthetic.main.fragment_add_new_legal_person.buildingNo
import kotlinx.android.synthetic.main.fragment_add_new_legal_person.cd_hidden_view
import kotlinx.android.synthetic.main.fragment_add_new_legal_person.check
import kotlinx.android.synthetic.main.fragment_add_new_legal_person.cnmae
import kotlinx.android.synthetic.main.fragment_add_new_legal_person.companyName
import kotlinx.android.synthetic.main.fragment_add_new_legal_person.county
import kotlinx.android.synthetic.main.fragment_add_new_legal_person.cui
import kotlinx.android.synthetic.main.fragment_add_new_legal_person.entrance
import kotlinx.android.synthetic.main.fragment_add_new_legal_person.filled_county
import kotlinx.android.synthetic.main.fragment_add_new_legal_person.filled_locality
import kotlinx.android.synthetic.main.fragment_add_new_legal_person.flg
import kotlinx.android.synthetic.main.fragment_add_new_legal_person.floor
import kotlinx.android.synthetic.main.fragment_add_new_legal_person.in_filled_county
import kotlinx.android.synthetic.main.fragment_add_new_legal_person.in_filled_locality
import kotlinx.android.synthetic.main.fragment_add_new_legal_person.li_dialog
import kotlinx.android.synthetic.main.fragment_add_new_legal_person.localitys
import kotlinx.android.synthetic.main.fragment_add_new_legal_person.naceTV
import kotlinx.android.synthetic.main.fragment_add_new_legal_person.name_arrow
import kotlinx.android.synthetic.main.fragment_add_new_legal_person.name_in_lay
import kotlinx.android.synthetic.main.fragment_add_new_legal_person.noReg
import kotlinx.android.synthetic.main.fragment_add_new_legal_person.root
import kotlinx.android.synthetic.main.fragment_add_new_legal_person.sp_quata
import kotlinx.android.synthetic.main.fragment_add_new_legal_person.spinnerCity
import kotlinx.android.synthetic.main.fragment_add_new_legal_person.spinnerCounty
import kotlinx.android.synthetic.main.fragment_add_new_legal_person.streetName
import kotlinx.android.synthetic.main.fragment_add_new_legal_person.toolbar
import kotlinx.android.synthetic.main.fragment_add_new_legal_person.typeTV
import kotlinx.android.synthetic.main.fragment_add_new_legal_person.zipCode
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.requests.Address
import ro.westaco.carhome.data.sources.remote.responses.models.*
import ro.westaco.carhome.presentation.base.BaseFragment
import ro.westaco.carhome.presentation.screens.service.person.natural.addnatural.AddBillCountryCodeDialog
import ro.westaco.carhome.presentation.screens.settings.data.person_legal.add_new.ActivityTypeAdapter
import ro.westaco.carhome.presentation.screens.settings.data.person_legal.add_new.AddNewLegalPersonFragment
import ro.westaco.carhome.presentation.screens.settings.data.person_legal.add_new.caenAdapter
import ro.westaco.carhome.presentation.screens.settings.data.person_natural.add_new.CountyAdapter
import ro.westaco.carhome.presentation.screens.settings.data.person_natural.add_new.CountyListClick
import ro.westaco.carhome.presentation.screens.settings.data.person_natural.add_new.LocalityAdapter
import ro.westaco.carhome.utils.CatalogUtils
import ro.westaco.carhome.utils.CountryCityUtils
import ro.westaco.carhome.utils.Progressbar
import ro.westaco.carhome.utils.ViewUtils
import java.util.*


@AndroidEntryPoint
class AddBillLegalFragment : BaseFragment<AddBillLegalViewModel>() {

    private var legalPersonDetails: LegalPersonDetails? = null
    private var address: Address? = null
    private var caenItem: Caen? = null
    private var activityTypeItem: CatalogItem? = null
    var progressbar: Progressbar? = null
    var typePos = 0
    var caendialog: BottomSheetDialog? = null
    var activitydialog: BottomSheetDialog? = null
    var countydialog: BottomSheetDialog? = null
    var locality: BottomSheetDialog? = null
    var countyPosition = 0
    var localityPosition = 0

    var localityAdapter: LocalityAdapter? = null
    var countries: java.util.ArrayList<Country> = java.util.ArrayList()
    var sirutaDataList: java.util.ArrayList<Siruta> = java.util.ArrayList()
    var streetTypeList: java.util.ArrayList<CatalogItem> = java.util.ArrayList()


    companion object {
        const val ARG_IS_EDIT = "arg_is_edit"

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


        localitys?.setOnClickListener {
            locality?.show()
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



        cta_bill_legal.setOnClickListener {
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

                val cityList: java.util.ArrayList<Siruta> = java.util.ArrayList()
                for (i in sirutaDataList.indices) {
                    if (sirutaDataList[i].parentCode == null)
                        cityList.add(sirutaDataList[i])
                }
                sirutaCode = cityList[countyPosition].code
                val localityList: java.util.ArrayList<Siruta> = java.util.ArrayList()
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
                countryCode = AddNewLegalPersonFragment.countryItem?.code,
                block = blockName.text.toString(),
                region = region,
                entrance = entrance.text.toString(),
                floor = floor.text.toString(),
                apartment = apartment.text.toString()
            )



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
                                        address,
                                        check.isChecked,
                                        caenItem,
                                        activityTypeItem
                                    )

                                } else {

                                    Toast.makeText(
                                        requireContext(),
                                        requireContext().resources.getText(R.string.confirm_details),
                                        Toast.LENGTH_LONG
                                    ).show()
                                }

                            } else {

                                Toast.makeText(
                                    requireContext(),
                                    requireContext().resources.getText(R.string.Activity_type_empty),
                                    Toast.LENGTH_LONG
                                ).show()
                            }

                        } else {

                            Toast.makeText(
                                requireContext(),
                                requireContext().resources.getText(R.string.caen_empty),
                                Toast.LENGTH_LONG
                            ).show()
                        }


                    } else {

                        Toast.makeText(
                            requireContext(),
                            requireContext().resources.getText(R.string.invalid_cui),
                            Toast.LENGTH_LONG
                        ).show()
                    }

                } else {

                    Toast.makeText(
                        requireContext(),
                        requireContext().resources.getText(R.string.cui_empty),
                        Toast.LENGTH_LONG
                    ).show()
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    requireContext().resources.getText(R.string.company_empty),
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        spinnerCity.isVisible = true
        spinnerCounty.isVisible = true
        in_filled_county.visibility = View.GONE
        in_filled_locality.visibility = View.GONE

    }

    override fun setObservers() {

        viewModel.countryData.observe(viewLifecycleOwner) { countryData ->
            this.countries = countryData
            val countryCodeDialog = AddBillCountryCodeDialog(requireActivity(), countries, "Legal")

            li_dialog.setOnClickListener {
                countryCodeDialog.show()
            }

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

        viewModel.sirutaData.observe(viewLifecycleOwner) { sirutaData ->
            progressbar?.dismissPopup()
            this.sirutaDataList = sirutaData
            sirutaDataList.sortWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })
            var firstSirutaCode: Int? = null
            for (i in sirutaDataList.indices) {
                if (sirutaDataList[i].parentCode == null) {
                    firstSirutaCode = sirutaDataList[i].code
                    county.setText(sirutaDataList[i].name)
                    break
                }
            }

            val localityList: java.util.ArrayList<Siruta> = java.util.ArrayList()
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

        viewModel.streetTypeData.observe(viewLifecycleOwner) { streetTypeData ->
            this.streetTypeList = streetTypeData

            val arryadapter =
                ArrayAdapter(requireContext(), R.layout.drop_down_list, streetTypeList)
            sp_quata.adapter = arryadapter
        }

        viewModel.activityTypeData.observe(viewLifecycleOwner) { activityTypeList ->

            activitydialog = BottomSheetDialog(requireContext())

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


    }


    private fun setupFullHeight(bottomSheet: View) {
        val layoutParams = bottomSheet.layoutParams
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT
        bottomSheet.layoutParams = layoutParams
    }

    private fun opencountydialog() {

        var cityList = mutableListOf<Siruta>()
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