package com.unnebulous.consultapronta.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.withStyledAttributes
import com.unnebulous.consultapronta.R
import com.unnebulous.consultapronta.Utils
import com.unnebulous.consultapronta.databinding.HeaderAltViewBinding
import com.unnebulous.consultapronta.databinding.HeaderViewBinding

class HeaderView @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

	private var _binding: HeaderViewBinding? = null
	private var _bindingAlt: HeaderAltViewBinding? = null

	private val binding get() = _binding!!
	private val bindingAlt get() = _bindingAlt!!

	var headerType = Utils.HeaderType.COMPACT
	var isScreenTitled = false

	init {
		attrs?.let {
			context.withStyledAttributes(attrs, R.styleable.HeaderView) {

				headerType = if (getColor(R.styleable.HeaderView_headerType, 0) == 0) {
					Utils.HeaderType.COMPACT
				} else {
					Utils.HeaderType.TITLED
				}

				isScreenTitled = getBoolean(R.styleable.HeaderView_isScreenTitled, false)
			}
		}

		setupLayout()
	}

	fun changeHeaderType(newType: Utils.HeaderType, screenTitled: Boolean = false) {
		if (headerType == newType && isScreenTitled == screenTitled) {
			return
		}

		headerType = newType
		isScreenTitled = screenTitled
		setupLayout()
	}

	fun setScreenTitle(title: String) {
		if (headerType == Utils.HeaderType.COMPACT && !isScreenTitled) {
			return
		}

		bindingAlt.headerMainTitle.text = title
	}

	fun setGoBackButtonOnClickListener(l: OnClickListener?) {
		if (headerType == Utils.HeaderType.COMPACT) {
			return
		}

		bindingAlt.goBack.setOnClickListener(l)
	}

	fun setProfileViewOnClickListener(l: OnClickListener?) {
		if (headerType == Utils.HeaderType.TITLED) {
			return
		}

		binding.headerProfileView.setOnClickListener(l)
	}

	fun setNotificationButtonOnClickListener(l: OnClickListener?) {
		if (headerType == Utils.HeaderType.TITLED) {
			return
		}

		binding.notificationButton.setOnClickListener(l)
	}
	private fun setupLayout() {
		removeAllViews()

		when (headerType) {
			Utils.HeaderType.COMPACT -> {
				_binding = HeaderViewBinding.inflate(
					LayoutInflater.from(context),
					this,
					true
				)
				_bindingAlt = null
			}

			Utils.HeaderType.TITLED -> {
				_bindingAlt = HeaderAltViewBinding.inflate(
					LayoutInflater.from(context),
					this,
					true
				)
				_binding = null

				if (isScreenTitled) {
					bindingAlt.headerTitle.visibility = GONE
					bindingAlt.headerMainTitle.visibility = VISIBLE
				}
			}
		}
	}
}