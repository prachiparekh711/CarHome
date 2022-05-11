package ro.westaco.carhome.presentation.screens.service.person.natural.addnatural

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.Window
import androidx.recyclerview.widget.LinearLayoutManager
import com.rilixtech.widget.countrycodepicker.R
import kotlinx.android.synthetic.main.country_code_picker_layout_picker_dialog.*
import ro.westaco.carhome.data.sources.remote.responses.models.Country
import ro.westaco.carhome.presentation.screens.service.person.legal.addlegal.AddBillLegalFragment
import ro.westaco.carhome.presentation.screens.service.person.legal.addlegal.AddBillLegalFragment.Companion.getFlg
import ro.westaco.carhome.presentation.screens.settings.data.person_natural.add_new.CountryPickerAdapter
import ro.westaco.carhome.utils.CountryCityUtils
import java.util.*

class AddBillCountryCodeDialog(
    val context: Activity,
    val countries: ArrayList<Country>,
    val from: String
) :
    Dialog(context) {

    private val addbillNaturalFragment = AddBillNaturalFragment
    private val addNewLegalPersonFragment = AddBillLegalFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.country_code_picker_layout_picker_dialog)

        country_dialog_lv?.layoutManager = LinearLayoutManager(context)

        country_dialog_lv.adapter = CountryPickerAdapter(
            context,
            countries,
            countyrPick = object : CountryPickerAdapter.CountyrPick {
                override fun pick(position: Int) {

                    when (from) {
                        "Natural" -> {
                            addbillNaturalFragment.getFlg(
                                countries[position],
                                CountryCityUtils.getFlagDrawableResId(
                                    countries[position].twoLetterCode.lowercase(
                                        Locale.getDefault()
                                    )
                                )
                            )
                        }
                        "Legal" -> {
                            getFlg(
                                countries[position],
                                CountryCityUtils.getFlagDrawableResId(
                                    countries[position].twoLetterCode.lowercase(
                                        Locale.getDefault()
                                    )
                                )
                            )
                        }
                    }

                    dismiss()
                }

            })


    }


}