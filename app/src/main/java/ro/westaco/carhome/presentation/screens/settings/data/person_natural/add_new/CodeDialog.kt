package ro.westaco.carhome.presentation.screens.settings.data.person_natural.add_new

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.Window
import androidx.recyclerview.widget.LinearLayoutManager
import com.rilixtech.widget.countrycodepicker.R
import kotlinx.android.synthetic.main.country_code_picker_layout_picker_dialog.*

class CodeDialog(val context: Activity, val phoneModelList: ArrayList<PhoneCodeModel>) :
    Dialog(context) {

    private val addNewNaturalPersonFragment = AddNewNaturalPersonFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.country_code_picker_layout_picker_dialog)

        country_dialog_lv?.layoutManager = LinearLayoutManager(context)

        country_dialog_lv.adapter = CountryCodePickerAdapter(context, phoneModelList,
            countyrPick = object : CountryCodePickerAdapter.CountyrPick {
                override fun pick(countries: PhoneCodeModel) {

                    addNewNaturalPersonFragment.getcountrycode(countries)
                    dismiss()
                }

            })

    }


}