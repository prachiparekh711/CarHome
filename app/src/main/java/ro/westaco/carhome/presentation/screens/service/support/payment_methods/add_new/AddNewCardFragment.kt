package ro.westaco.carhome.presentation.screens.service.support.payment_methods.add_new

import android.app.DatePickerDialog
import androidx.core.content.ContextCompat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_add_new_card.*
import ro.westaco.carhome.R
import ro.westaco.carhome.presentation.base.BaseFragment
import ro.westaco.carhome.utils.CardNumberTextWatcher
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class AddNewCardFragment : BaseFragment<AddNewCardViewModel>() {
    override fun getContentView() = R.layout.fragment_add_new_card

    override fun getStatusBarColor() = ContextCompat.getColor(requireContext(), R.color.white)

    override fun initUi() {
        back.setOnClickListener {
            viewModel.onBack()
        }

        home.setOnClickListener {
            viewModel.onMain()
        }

        root.setOnClickListener {
            viewModel.onRootClicked()
        }

        cardNumber.addTextChangedListener(CardNumberTextWatcher())

        expirationDate.setOnClickListener {
            viewModel.onExpirationDateClicked()
        }

        cta.setOnClickListener {
            viewModel.onSave()
        }
    }

    override fun setObservers() {
        viewModel.cardExpirationDateLiveData.observe(viewLifecycleOwner) { dateMillis ->
            expirationDate.text = SimpleDateFormat(
                getString(R.string.date_format_template), Locale.getDefault()
            ).format(
                Date(dateMillis)
            )
        }

        viewModel.actionStream.observe(viewLifecycleOwner) {
            when (it) {
                is AddNewCardViewModel.ACTION.ShowDatePicker -> showDatePicker(it.dateInMillis)
            }
        }
    }

    private var dpd: DatePickerDialog? = null
    private fun showDatePicker(dateInMillis: Long) {
        val c = Calendar.getInstance().apply {
            timeInMillis = dateInMillis
        }

        dpd?.cancel()
        dpd = DatePickerDialog(
            requireContext(), R.style.DialogTheme, { _, year, monthOfYear, dayOfMonth ->
                viewModel.onDatePicked(
                    Calendar.getInstance().apply {
                        set(Calendar.YEAR, year)
                        set(Calendar.MONTH, monthOfYear)
                        set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    }.timeInMillis
                )
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)
        )
        dpd?.show()
    }
}