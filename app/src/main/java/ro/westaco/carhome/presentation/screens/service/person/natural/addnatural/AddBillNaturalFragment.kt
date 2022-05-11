package ro.westaco.carhome.presentation.screens.service.person.natural.addnatural

import android.annotation.SuppressLint
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_add_new_natural_person.*
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.requests.Address
import ro.westaco.carhome.data.sources.remote.responses.models.CatalogItem
import ro.westaco.carhome.data.sources.remote.responses.models.Country
import ro.westaco.carhome.data.sources.remote.responses.models.NaturalPersonDetails
import ro.westaco.carhome.data.sources.remote.responses.models.Siruta
import ro.westaco.carhome.presentation.base.BaseFragment
import ro.westaco.carhome.presentation.screens.settings.data.person_natural.add_new.CountyAdapter
import ro.westaco.carhome.presentation.screens.settings.data.person_natural.add_new.CountyListClick
import ro.westaco.carhome.presentation.screens.settings.data.person_natural.add_new.LocalityAdapter
import ro.westaco.carhome.utils.CatalogUtils
import ro.westaco.carhome.utils.Progressbar


@AndroidEntryPoint
class AddBillNaturalFragment : BaseFragment<AddBillNaturalViewModel>() {

    private var naturalPersonDetails: NaturalPersonDetails? = null
    var address: Address? = null
    var countriesList: ArrayList<Country> = ArrayList()
    var sirutaList: ArrayList<Siruta> = ArrayList()
    var streetTypeList: ArrayList<CatalogItem> = ArrayList()
    var progressbar: Progressbar? = null
    var countyPosition = 0
    var localityPosition = 0
    var countydialog: BottomSheetDialog? = null
    var locality: BottomSheetDialog? = null
    private lateinit var runnable: Runnable

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

        contexts = requireContext()
        cna = cnmae
        flgs = flg

        in_filleds_county = in_filled_county
        in_filleds_locality = in_filled_locality
        spinnerCity1 = spinnerCity
        spinnerCounty1 = spinnerCounty

        name_lay.setOnClickListener {

            if (li_name_h.visibility == View.VISIBLE) {

                /*TransitionManager.beginDelayedTransition(base_cardview)*/
                li_name_h.visibility = View.GONE
                name_lay.setBackgroundColor(resources.getColor(R.color.white))
                name_arrow.setImageResource(R.drawable.ic_arrow_circle_down)

            } else {

                /*TransitionManager.beginDelayedTransition(
                    base_cardview)*/
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

        cta.setOnClickListener {

            val streetTypeItem =
                sp_quata?.selectedItemPosition?.let { it1 -> streetTypeList.get(it1).id }
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
                for (i in countriesList.indices) {
                    if (countriesList[i].code == "ROU") {
                        countryItem = countriesList[i]
                    }
                }

                val cityList: ArrayList<Siruta> = ArrayList()
                for (i in sirutaList.indices) {
                    if (sirutaList[i].parentCode == null)
                        cityList.add(sirutaList[i])
                }
                sirutaCode = cityList[countyPosition].code
                val localityList: ArrayList<Siruta> = ArrayList()
                for (i in sirutaList.indices) {
                    if (sirutaCode == sirutaList[i].parentCode) {
                        localityList.add(sirutaList[i])
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

            if (firstName.text.toString().isNotEmpty()) {

                if (lastName.text.toString().isNotEmpty()) {

                    if (firstName.text.toString().length in 2..50) {

                        if (lastName.text.toString().length in 2..50) {


                            if (streetName.text.toString().isNotEmpty()) {

                                if (buildingNo.text.toString().isNotEmpty()) {

                                    if (!check.isChecked) {

                                        Toast.makeText(
                                            requireContext(),
                                            R.string.confirm_details,
                                            Toast.LENGTH_SHORT
                                        ).show()

                                    } else {

                                        viewModel.onSave(
                                            firstName.text.toString(),
                                            lastName.text.toString(),
                                            check.isChecked,
                                            naturalPersonDetails?.id?.toLong(),
                                            address

                                        )
                                    }

                                } else {

                                    Toast.makeText(
                                        requireContext(),
                                        R.string.number_empty,
                                        Toast.LENGTH_SHORT
                                    ).show()

                                }

                            } else {

                                Toast.makeText(
                                    requireContext(),
                                    R.string.street_empty,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }


                        } else {

                            Toast.makeText(
                                requireContext(),
                                R.string.last_name_len,
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                    } else {

                        Toast.makeText(
                            requireContext(),
                            R.string.first_name_len,
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                } else {

                    Toast.makeText(requireContext(), R.string.last_name_r, Toast.LENGTH_SHORT)
                        .show()
                }


            } else {

                Toast.makeText(requireContext(), R.string.first_name_r, Toast.LENGTH_SHORT).show()
            }


        }

        localitys.setOnClickListener {

            locality?.show()
        }

    }

    override fun setObservers() {
        viewModel.countryData.observe(viewLifecycleOwner) { countryData ->
            this.countriesList = countryData
            val countryCodeDialog =
                AddBillCountryCodeDialog(requireActivity(), countriesList, "Natural")
            li_dialog.setOnClickListener {
                countryCodeDialog.show()
            }
        }

        viewModel.sirutaData.observe(viewLifecycleOwner) { sirutaData ->
            this.sirutaList = sirutaData
            county.setOnClickListener {
                opencountydialog()
            }

            progressbar?.dismissPopup()
        }

        viewModel.streetTypeData.observe(viewLifecycleOwner) { streetTypeData ->
            this.streetTypeList = streetTypeData
            val arryadapter =
                ArrayAdapter(requireContext(), R.layout.drop_down_list, streetTypeList)
            sp_quata.adapter = arryadapter
        }
    }

    private fun opencountydialog() {

        val cityList: ArrayList<Siruta> = ArrayList()

        val view = layoutInflater.inflate(R.layout.county_layout, null)
        countydialog = BottomSheetDialog(requireContext())
        countydialog?.setCancelable(true)
        countydialog?.setContentView(view)
        countydialog!!.show()

        val rv_county: RecyclerView? = view.findViewById(R.id.rv_county)
        val etSearchTrain: EditText? = view.findViewById(R.id.etSearchTrain)
        val mClose: ImageView? = view.findViewById(R.id.mClose)
        rv_county?.layoutManager = LinearLayoutManager(requireContext())

        if (sirutaList.isNotEmpty()) {

            for (i in sirutaList.indices) {

                if (sirutaList[i].parentCode == null)

                    cityList.add(sirutaList[i])
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
                    after: Int,
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

            countydialog!!.dismiss()
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

        if (sirutaList.isNotEmpty()) {
            for (i in sirutaList.indices) {
                if (code == sirutaList[i].parentCode.toString()) {
                    city.add(sirutaList[i])
                }
            }


            city.sortWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })

            val adapter = LocalityAdapter(
                requireContext(),
                city,
                localityListClick = object : LocalityAdapter.LocalityListClick {
                    override fun localityclick(position: Int, siruta: Siruta) {
                        localityPosition = position
                        localitys.setText(siruta.name)
                        locality?.dismiss()

                    }


                })
            rv_locality?.adapter = adapter

            etSearchTrain?.addTextChangedListener(object : TextWatcher {

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int,
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

            locality?.dismiss()
        }

    }

    companion object {

        var countryItem: Country? = null
        var selectedPhoneCode: String? = null

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

        var bottomOpen: String? = null

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


}