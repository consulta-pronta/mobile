package com.unnebulous.consultapronta.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.content.withStyledAttributes
import com.unnebulous.consultapronta.R
import com.unnebulous.consultapronta.databinding.ViewMapListBinding

class MapListView @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyleAttr: Int = 0,
	type: ViewType = ViewType.KEY_VALUE
): LinearLayout(context, attrs, defStyleAttr) {
	enum class ViewType { KEY_VALUE, ONLY_VALUE }

	private val binding: ViewMapListBinding

	private var map = mutableMapOf<String, String>()
		set(_) = update()

	init {
		binding = ViewMapListBinding.inflate(
			LayoutInflater.from(context),
			this,
			true
		)

		attrs?.let {
			applyAttibutes(it)
		}
	}

	fun addItem(key: String, value: String) {
		map[key] = value
	}

	fun setFullMap(map: Map<String, String>) {

	}

	private fun update() {
		map.forEach { key, value ->

		}
	}

	private fun applyAttibutes(attrs: AttributeSet) {
		context.withStyledAttributes(attrs, R.styleable.MapListView) {
			val title = getString(R.styleable.MapListView_title)
			val type = getInt(R.styleable.MapListView_type, 0)


		}
	}
}