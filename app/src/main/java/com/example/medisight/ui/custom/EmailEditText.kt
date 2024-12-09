package com.example.medisight.ui.custom

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.AppCompatEditText
import com.example.medisight.R

class EmailEditText : AppCompatEditText {

    private var isValidEmail = false

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    private fun init() {
        hint = context.getString(R.string.email_hint)
        textAlignment = View.TEXT_ALIGNMENT_VIEW_START

        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                validateEmail(s)
            }

            override fun afterTextChanged(s: Editable) {}
        })
    }

    private fun validateEmail(s: CharSequence) {
        if (s.isNotEmpty()) {
            if (!Patterns.EMAIL_ADDRESS.matcher(s).matches()) {
                if (isValidEmail) {
                    Toast.makeText(
                        context,
                        context.getString(R.string.email_invalid),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                isValidEmail = false
            } else {
                isValidEmail = true
            }
        }
    }
}