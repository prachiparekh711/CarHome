package ro.westaco.carhome.presentation.screens.onboarding

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class OnboardingItem(
    @StringRes val titleResId: Int,
    @DrawableRes val imageResId: Int
)