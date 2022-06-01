package ro.westaco.carhome.presentation.screens.service.person.natural.addnatural

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_add_bill_natural.*
import kotlinx.android.synthetic.main.fragment_add_bill_natural.apartment
import kotlinx.android.synthetic.main.fragment_add_bill_natural.buildingNo
import kotlinx.android.synthetic.main.fragment_add_bill_natural.entrance
import kotlinx.android.synthetic.main.fragment_add_bill_natural.floor
import kotlinx.android.synthetic.main.fragment_add_bill_natural.streetName
import kotlinx.android.synthetic.main.fragment_add_bill_natural.zipCode
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.requests.Address
import ro.westaco.carhome.data.sources.remote.responses.models.CatalogItem
import ro.westaco.carhome.data.sources.remote.responses.models.Country
import ro.westaco.carhome.data.sources.remote.responses.models.NaturalPersonDetails
import ro.westaco.carhome.data.sources.remote.responses.models.Siruta
import ro.westaco.carhome.presentation.base.BaseFragment
import ro.westaco.carhome.presentation.interfaceitem.CountyListClick
import ro.westaco.carhome.presentation.screens.data.commen.CountryCodeDialog
import ro.westaco.carhome.presentation.screens.data.commen.CountyAdapter
import ro.westaco.carhome.presentation.screens.data.commen.LocalityAdapter
import ro.westaco.carhome.utils.CatalogUtils
import ro.westaco.carhome.utils.CountryCityUtils
import ro.westaco.carhome.utils.DialogUtils.Companion.showErrorInfo
import ro.westaco.carhome.utils.Progressbar
import ro.westaco.carhome.utils.SirutaUtil
import ro.westaco.carhome.utils.SirutaUtil.Companion.countyList
import java.util.*


@AndroidEntryPoint
class AddBillNaturalFragment : BaseFragment<AddBillNaturalViewModel>(),
    CountryCodeDialog.CountryCodePicker {

    private var naturalPersonDetails: NaturalPersonDetails? = null
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
        return R.layout.fragment_add_bill_natural
    }

    override fun initUi() {
        progressbar = Progressbar(requireContext())
        progressbar?.showPopup()

        toolbar.setNavigationOnClickListener {
            viewModel.onBack()
        }

        cancel.setOnClickListener {
            viewModel.onBack()
        }

        name_lay.setOnClickListener {

            if (li_name_h.visibility == View.VISIBLE) {

                li_name_h.visibility = View.GONE
                name_lay.setBackgroundColor(resources.getColor(R.color.white))
                name_arrow.setImageResource(R.drawable.ic_arrow_circle_down)

            } else {

                name_lay.setBackgroundColor(resources.getColor(R.color.expande_colore))
                li_name_h.visibility = View.VISIBLE
                name_arrow.setImageResource(R.drawable.ic_arrow_circle_up)
            }
        }

        fullAddress_lay.setOnClickListener {


            if (adds_hidden_view.visibility == View.VISIBLE) {

                adds_hidden_view.visibility = View.GONE
                fullAddress_lay.setBackgroundColor(resources.getColor(R.color.white))
                adds_arrow.setImageResource(R.drawable.ic_arrow_circle_down)

            } else {

                fullAddress_lay.setBackgroundColor(resources.getColor(R.color.expande_colore))
                adds_hidden_view.visibility = View.VISIBLE
                adds_arrow.setImageResource(R.drawable.ic_arrow_circle_up)

            }
        }

        changeCountryState("ROU")
        countySpinnerText.setText(SirutaUtil.defaultCounty?.name)
        localitySpinnerText.setText(SirutaUtil.defaultCity?.name)

        countyPosition = SirutaUtil.defaultCounty?.name?.let { SirutaUtil.fetchCountyPosition(it) }
        localityPosition = SirutaUtil.defaultCity?.name?.let { SirutaUtil.fetchCountyPosition(it) }

        countySpinnerText.setOnClickListener {
            openCountyDialog()
        }

        localitySpinnerText?.setOnClickListener {
            localityDialog?.show()
        }

        cta.setOnClickListener {

            val streetTypeItem =
                sp_quata?.selectedItemPosition?.let { it1 -> streetTypeList.get(it1).id }
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
                    regionStr = countyPosition?.let { countyList[it].name }
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

            if (firstName.text.toString().isNotEmpty()) {

                if (lastName.text.toString().isNotEmpty()) {

                    if (firstName.text.toString().length in 2..50) {

                        if (lastName.text.toString().length in 2..50) {

                            if (streetName.text.toString().isNotEmpty()) {

                                if (buildingNo.text.toString().isNotEmpty()) {

                                    if (!check.isChecked) {

                                        showErrorInfo(
                                            requireContext(),
                                            getString(R.string.confirm_details)
                                        )

                                    } else {

                                        viewModel.onSave(
                                            firstName.text.toString(),
                                            lastName.text.toString(),
                                            check.isChecked,
                                            addressItem
                                        )
                                    }

                                } else {
                                    showErrorInfo(
                                        requireContext(),
                                        getString(R.string.number_empty)
                                    )
                                }
                            } else {
                                showErrorInfo(requireContext(), getString(R.string.street_empty))
                            }

                        } else {
                            showErrorInfo(requireContext(), getString(R.string.last_name_len))
                        }

                    } else {
                        showErrorInfo(requireContext(), getString(R.string.first_name_len))

                    }

                } else {
                    showErrorInfo(requireContext(), getString(R.string.last_name_r))
                }

            } else {
                showErrorInfo(requireContext(), getString(R.string.first_name_r))
            }


        }


    }

    override fun setObservers() {
        viewModel.countryData.observe(viewLifecycleOwner) { countryData ->
            if (countryData != null) {
                this.countriesList = countryData
//                val countryCodeDialog = CountryCodeDialog(requireActivity(), countriesList, this)
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
                    countyCode = SirutaUtil.fetchCounty(countySpinnerText.text.toString())?.code
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