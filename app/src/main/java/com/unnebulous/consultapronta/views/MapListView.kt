package com.unnebulous.consultapronta.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.view.menu.ListMenuItemView
import androidx.core.content.withStyledAttributes
import androidx.core.view.isVisible
import com.unnebulous.consultapronta.R
import com.unnebulous.consultapronta.Utils
import com.unnebulous.consultapronta.databinding.ItemMapListViewBinding
import com.unnebulous.consultapronta.databinding.ViewMapListBinding

class MapListView @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyleAttr: Int = 0,
	type: ViewType = ViewType.KEY_VALUE
): LinearLayout(context, attrs, defStyleAttr) {
	enum class ViewType { KEY_VALUE, ONLY_VALUE }

	private val binding: ViewMapListBinding
	private var type: ViewType

	var onDeleteClick: (() -> Unit)? = null
	lateinit var onAddClick: () -> Pair<String, String>

	private var map = mutableMapOf<String, String>()
		set(_) = update()
	private val displayedItemsKey = mutableListOf<String>()

	init {
		binding = ViewMapListBinding.inflate(
			LayoutInflater.from(context),
			this,
			true
		)
		this.type = type

		binding.addItem.setOnClickListener {
			addItem(onAddClick())
		}

		attrs?.let {
			applyAttibutes(it)
		}
	}

	fun addItem(item: Pair<String, String>) {
		addItem(item.first, item.second)
	}

	fun addItem(key: String, value: String) {
		map[key] = value
		displayedItemsKey.add(key)
		update()
	}

	fun setFullMap(map: Map<String, String>) {
		displayedItemsKey.clear()
		this.map = map.toMutableMap()
		displayedItemsKey.addAll(map.keys)
	}

	private fun update() {
		val hasKeyFrame = type == ViewType.KEY_VALUE
		map.forEach { (mapKey, mapValue) ->
			if (mapKey !in displayedItemsKey) {
				binding.itemList.addView(ItemMapListViewBinding.inflate(
					LayoutInflater.from(context),
					binding.itemList,
					false
				).apply {
					keyFrame.isVisible = hasKeyFrame
					key.text = mapKey
					value.text = mapValue

					deleteButton.setOnClickListener {
						onDeleteClick?.invoke()
					}
				}.root)
			}
		}
	}

	private fun applyAttibutes(attrs: AttributeSet) {
		context.withStyledAttributes(attrs, R.styleable.MapListView) {
			val title = getString(R.styleable.MapListView_title)
			val type = getInt(R.styleable.MapListView_type, 0)

			binding.title.text = title

			this@MapListView.type = when (type) {
				0 -> ViewType.KEY_VALUE
				1 -> ViewType.ONLY_VALUE
				else -> ViewType.KEY_VALUE
			}
		}
	}
}