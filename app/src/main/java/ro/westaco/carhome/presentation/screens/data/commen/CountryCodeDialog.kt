package ro.westaco.carhome.presentation.screens.data.commen

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.responses.models.Country
import ro.westaco.carhome.utils.CountryCityUtils
import java.util.*

class CountryCodeDialog(
    val context: Activity,
    val countries: ArrayList<Country>,
    val listner: CountryCodePicker
) :

    BottomSheetDialogFragment() {

    private lateinit var bottomSheet: ViewGroup
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<*>

    interface CountryCodePicker {
        fun OnCountryPick(item: Country, flagDrawableResId: String)
    }

    override fun onStart() {
        super.onStart()
        bottomSheet =
            dialog?.findViewById(com.google.android.material.R.id.design_bottom_sheet) as ViewGroup // notice the R root package
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehavior.peekHeight = BottomSheetBehavior.PEEK_HEIGHT_AUTO
        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(view: View, i: Int) {
                if (BottomSheetBehavior.STATE_HIDDEN == i) {
                    dismiss()
                }
            }

            override fun onSlide(view: View, v: Float) {}
        })

//        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val viewItems: View =
            inflater.inflate(R.layout.country_code_picker_layout_picker_dialog, container, false)

        val recyclerView = viewItems.findViewById<RecyclerView>(R.id.country_dialog_lv)
        val close = viewItems.findViewById<ImageView>(R.id.mClose)

        recyclerView?.layoutManager =
            LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)
        recyclerView.adapter = CountryPickerAdapter(context, countries,
            countryPick = object : CountryPickerAdapter.CountryPick {
                override fun pick(position: Int) {

                    listner.OnCountryPick(
                        countries[position],
                        CountryCityUtils.getFlagId(
                            countries[position].twoLetterCode.lowercase(
                                Locale.getDefault()
                            )
                        )
                    )

                    dismiss()
                }

            })

        close.setOnClickListener { dismiss() }

        return viewItems

    }

}