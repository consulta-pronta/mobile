package com.unnebulous.consultapronta.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.withStyledAttributes
import com.unnebulous.consultapronta.R
import com.unnebulous.consultapronta.databinding.HeaderAltViewBinding
import com.unnebulous.consultapronta.databinding.HeaderViewBinding

class HeaderView @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

	private lateinit var binding: HeaderViewBinding
	private lateinit var binding_alt: HeaderAltViewBinding

	var headerType = 0
	var isScreenTitled = false

	init {
		attrs?.let {
			context.withStyledAttributes(attrs, R.styleable.HeaderView) {
				headerType = getColor(R.styleable.HeaderView_headerType, 0)
				isScreenTitled = getBoolean(R.styleable.HeaderView_isScreenTitled, false)
			}
		}

		when (headerType) {
			// compact header
			0 -> {
				binding = HeaderViewBinding.inflate(
					LayoutInflater.from(context),
					this,
					true
				)
			}

			// titled header
			1 -> {
				binding_alt = HeaderAltViewBinding.inflate(
					LayoutInflater.from(context),
					this,
					true
				)

				if (isScreenTitled) {
					binding_alt.headerTitle.visibility = GONE
					binding_alt.headerMainTitle.visibility = VISIBLE
				}
			}
		}
	}

	fun setScreenTitle(title: String) {
		if (headerType == 0 && !isScreenTitled) {
			return
		}

		binding_alt.headerMainTitle.text = title
	}

	fun setGoBackButtonOnClickListener(l: OnClickListener?) {
		if (headerType == 0) {
			return
		}

		binding_alt.goBack.setOnClickListener(l)
	}

	fun setProfileViewOnClickListener(l: OnClickListener?) {
		if (headerType == 1) {
			return
		}

		binding.headerProfileView.setOnClickListener(l)
	}

	fun setNotificationButtonOnClickListener(l: OnClickListener?) {
		if (headerType == 1) {
			return
		}

		binding.notificationButton.setOnClickListener(l)
	}
}