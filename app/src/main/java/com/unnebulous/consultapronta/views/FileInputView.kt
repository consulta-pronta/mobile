package com.unnebulous.consultapronta.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.content.withStyledAttributes
import com.unnebulous.consultapronta.R
import com.unnebulous.consultapronta.databinding.FileInputViewBinding

class FileInputView @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

	enum class InputMode { SEND, GET }

	private val binding: FileInputViewBinding
	private var inputMode = InputMode.SEND

	init {
		isClickable = true
		isFocusable = true

		binding = FileInputViewBinding.inflate(
			LayoutInflater.from(context),
			this,
			true
		)

		attrs?.let {
			applyAttributes(it)
		}
	}

	// O arquivo será usado em operações exclusivamente backend
	// Ex.: envio do CRM
	fun sendFile(action: (Int) -> Unit) {
		if (inputMode != InputMode.SEND) {
			return
		}

		setOnClickListener {
			action(0)
		}
	}

	// O arquivo será usado em operações que envolvem frontend
	// Ex.: anexo de sintoma
	fun getFile(action: (Int) -> Unit) {
		if (inputMode != InputMode.GET) {
			return
		}

		setOnClickListener {
			action(0)
		}
	}

	fun setMainText(textResId: Int) {
		setMainText(ContextCompat.getString(context, textResId))
	}

	fun setMainText(text: String) {
		binding.mainText.text = text
	}

	fun setFileTypeText(textResId: Int) {
		setFileTypeText(ContextCompat.getString(context, textResId))
	}

	fun setFileTypeText(text: String) {
		binding.fileTypeText.text = text
	}

	fun setFileTypeTextVisibility(isVisible: Boolean) {
		binding.fileTypeText.visibility = if (isVisible) VISIBLE else GONE
	}

	fun setIcon(drawableResId: Int) {
		if (drawableResId != 0) {
			binding.icon.setImageResource(drawableResId)
		}
	}

	private fun applyAttributes(attrs: AttributeSet) {
		context.withStyledAttributes(attrs, R.styleable.FileInputView) {
			val mainText = getString(R.styleable.FileInputView_textMain) ?: ""
			val fileTypeText = getString(R.styleable.FileInputView_textFileType) ?: ""
			val icon = getDrawable(R.styleable.FileInputView_iconSrc)
			val inputMode = getColor(R.styleable.FileInputView_inputMode, 0)

			setMainText(mainText)

			if (fileTypeText.isNotBlank()) {
				setFileTypeTextVisibility(true)
				setFileTypeText(fileTypeText)
			} else {
				setFileTypeTextVisibility(false)
			}

			binding.icon.setImageDrawable(icon)

			this@FileInputView.inputMode = if (inputMode == 0) InputMode.SEND else InputMode.GET
		}
	}
}