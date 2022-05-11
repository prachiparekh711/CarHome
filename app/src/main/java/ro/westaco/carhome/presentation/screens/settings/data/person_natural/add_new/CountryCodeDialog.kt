package ro.westaco.carhome.presentation.screens.settings.data.person_natural.add_new

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.Window
import androidx.recyclerview.widget.LinearLayoutManager
import com.rilixtech.widget.countrycodepicker.R
import kotlinx.android.synthetic.main.country_code_picker_layout_picker_dialog.*
import ro.westaco.carhome.data.sources.remote.responses.models.Country
import ro.westaco.carhome.presentation.screens.settings.data.person_legal.add_new.AddNewLegalPersonFragment
import ro.westaco.carhome.utils.CountryCityUtils
import java.util.*

class CountryCodeDialog(
    val context: Activity,
    val countries: ArrayList<Country>,
    val from: String
) :
    Dialog(context) {

    private val addNewNaturalPersonFragment = AddNewNaturalPersonFragment
    private val addNewLegalPersonFragment = AddNewLegalPersonFragment

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
                            addNewNaturalPersonFragment.getFlg(
                                countries[position],
                                CountryCityUtils.getFlagDrawableResId(
                                    countries[position].twoLetterCode.lowercase(
                                        Locale.getDefault()
                                    )
                                )
                            )
                        }
                        "Legal" -> {
                            addNewLegalPersonFragment.getFlg(
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