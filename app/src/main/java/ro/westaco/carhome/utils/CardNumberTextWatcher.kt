package ro.westaco.carhome.utils

import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher

/*
 ** Reference: https://stackoverflow.com/questions/11790102/format-credit-card-in-edit-text-in-android
 */
class CardNumberTextWatcher : TextWatcher {
    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
    override fun afterTextChanged(s: Editable) {
        // Remove spacing char
        if (s.isNotEmpty() && s.length % 5 == 0) {
            val c = s[s.length - 1]
            if (space == c) {
                s.delete(s.length - 1, s.length)
            }
        }
        // Insert char where needed.
        if (s.isNotEmpty() && s.length % 5 == 0) {
            val c = s[s.length - 1]
            // Only if its a digit where there should be a space we insert a space
            if (Character.isDigit(c) && TextUtils.split(s.toString(), space.toString()).size <= 3) {
                s.insert(s.length - 1, space.toString())
            }
        }
    }

    companion object {
        // Change this to what you want... ' ', '-' etc..
        private const val space = ' '
    }
}