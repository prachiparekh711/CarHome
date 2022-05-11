package ro.westaco.carhome.presentation.screens.reminder.add_new

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import ro.westaco.carhome.R
import ro.westaco.carhome.databinding.LocationBottomsheetBinding

class MapBottomsheetDialog : BottomSheetDialogFragment() {
    var binding: LocationBottomsheetBinding? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.location_bottomsheet, container, false)
        val transaction = childFragmentManager.beginTransaction()
        transaction.replace(R.id.mapContainer, BottomMapFragment()).commit()
        return binding?.root
    }
}

class TouchEventInterceptorLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        val handled = super.dispatchTouchEvent(ev)
        requestDisallowInterceptTouchEvent(true)
        return handled
    }
}