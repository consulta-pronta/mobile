package com.unnebulous.consultapronta.views

import android.content.Context
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.withStyledAttributes
import com.unnebulous.consultapronta.R

class VerificationCodeView @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

	private var codeLength = 6
	private val digits = ArrayList<TextView>()
	private var hiddenEditText: EditText

	init {
		orientation = HORIZONTAL
		gravity = Gravity.CENTER
		
		attrs?.let {
			context.withStyledAttributes(it, R.styleable.VerificationCodeView) {
				codeLength = getInt(R.styleable.VerificationCodeView_codeLength, 6)
			}
		}

		removeAllViews()
		digits.clear()

		val digitParams = LayoutParams(
			resources.getDimensionPixelSize(R.dimen.verification_digit_width),
			resources.getDimensionPixelSize(R.dimen.verification_digit_height)
		).apply {
			setMargins(8, 0, 8, 0)
		}

		for (_i in 0 until codeLength) {
			val textView = TextView(context).apply {
				layoutParams = digitParams
				gravity = Gravity.CENTER
				textSize = 24f
				setTextColor(ContextCompat.getColor(context, R.color.textLight))
				background = ContextCompat.getDrawable(context, R.drawable.shape_verification_digit)
				isFocusable = false
				isClickable = false
			}
			digits.add(textView)
			addView(textView)
		}

		hiddenEditText = EditText(context).apply {
			layoutParams = LayoutParams(1, 1)
			alpha = 0f
			inputType = InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
			isFocusable = true
			isFocusableInTouchMode = true
			filters = arrayOf(android.text.InputFilter.LengthFilter(codeLength))
		}
		addView(hiddenEditText)

		setOnClickListener {
			hiddenEditText.requestFocus()
			val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
			inputMethodManager.showSoftInput(hiddenEditText, InputMethodManager.SHOW_IMPLICIT)
			// deprecado no Baklava (Android 16). O nosso mínimo não é API 36, logo, não há por que remover.
		}

		hiddenEditText.addTextChangedListener(object : TextWatcher {
			override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
			override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
				val text = s?.toString() ?: ""
				for (i in 0 until codeLength) {
					if (i < text.length) {
						digits[i].text = text[i].toString().uppercase()
						digits[i].alpha = 1f
					} else {
						digits[i].text = ""
						digits[i].alpha = 0.5f
					}
				}
			}
			override fun afterTextChanged(s: Editable?) {}
		})
	}

	fun getCode(): String = hiddenEditText.text.toString()

	fun clear() {
		hiddenEditText.text.clear()
	}
}