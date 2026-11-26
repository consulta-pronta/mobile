package com.unnebulous.consultapronta.views

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.view.menu.ListMenuItemView
import androidx.core.content.ContextCompat
import androidx.core.content.withStyledAttributes
import androidx.core.view.forEach
import androidx.core.view.isVisible
import com.google.api.Distribution
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

	lateinit var onDeleteClick: (Pair<String, String>) -> Unit
	lateinit var onAddClick: () -> Unit

	private var map = mutableMapOf<String, String>()
		set(value) {
			field = value
			update()
		}
	private val displayedItemsKey = mutableListOf<String>()
	private val displayedItemsId = mutableMapOf<String, Int>()

	private var itemColor: Map<String, Int>

	var hasOptionToUpdate = true

	var title: String?
		get() = binding.title.text.toString()
		set(value) {
			binding.title.text = value
		}

	var darkMode: Boolean = false
		set(value) {
			field = value

			var textColor: Int

			if (field) {
				textColor = ContextCompat.getColor(context, R.color.textDark)
				itemColor = mapOf(
					"text" to ContextCompat.getColor(context, R.color.textLight),
					"background" to ContextCompat.getColor(context, R.color.primaryDark)
				)
			} else {
				textColor = ContextCompat.getColor(context, R.color.textLight)
			}

			binding.title.setTextColor(textColor)
		}

	init {
		binding = ViewMapListBinding.inflate(
			LayoutInflater.from(context),
			this,
			true
		)
		this.type = type

		itemColor = mapOf(
			"text" to ContextCompat.getColor(context, R.color.textDark),
			"background" to ContextCompat.getColor(context, R.color.surface)
		)

		binding.addItem.setOnClickListener {
			onAddClick()
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
		update()
		displayedItemsKey.add(key)
	}

	fun setFullMap(map: Map<String, String>) {
		displayedItemsKey.clear()
		this.map = map.toMutableMap()
		displayedItemsKey.addAll(map.keys)
	}

	fun removeItem(itemKey: String) {
		displayedItemsKey.remove(itemKey)
		map.remove(itemKey)
		binding.itemList.removeView(binding.itemList.findViewById<LinearLayout>(displayedItemsId[itemKey]!!))
		displayedItemsId.remove(itemKey)
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
					this.root.id = generateViewId()
					displayedItemsId[mapKey] = this.root.id
					keyFrame.isVisible = hasKeyFrame
					key.text = mapKey
					value.text = mapValue
					this.root.layoutParams = MarginLayoutParams(
						LayoutParams.MATCH_PARENT,
						LayoutParams.WRAP_CONTENT
					).apply {
						setMargins(
							0,
							0,
							0,
							resources.getDimensionPixelSize(
								R.dimen.list_items_spacing
							)
						)
					}

					keyFrame.backgroundTintList = ColorStateList.valueOf(itemColor["background"]!!)
					valueLayout.backgroundTintList = ColorStateList.valueOf(itemColor["background"]!!)
					key.setTextColor(itemColor["text"]!!)
					value.setTextColor(itemColor["text"]!!)

					if (hasOptionToUpdate) {
						deleteButton.setOnClickListener {
							onDeleteClick(mapKey to mapValue)
							displayedItemsKey.remove(mapKey)
							update()
						}
					} else {
						deleteButton.isVisible = false
						binding.addItem.isVisible = false
					}
				}.root)
			}
		}
	}

	private fun applyAttibutes(attrs: AttributeSet) {
		context.withStyledAttributes(attrs, R.styleable.MapListView) {
			val title = getString(R.styleable.MapListView_title)
			val type = getInt(R.styleable.MapListView_type, 0)

			this@MapListView.title = title

			this@MapListView.type = when (type) {
				0 -> ViewType.KEY_VALUE
				1 -> ViewType.ONLY_VALUE
				else -> ViewType.KEY_VALUE
			}
		}
	}
}