package com.unnebulous.consultapronta.recyclerview

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.unnebulous.consultapronta.R

class SpacingItemDecoration(
	private val space: Int = R.dimen.default_inner_elements_spacing
) : RecyclerView.ItemDecoration() {

	override fun getItemOffsets(
		outRect: Rect,
		view: View,
		parent: RecyclerView,
		state: RecyclerView.State
	) {
		outRect.bottom = space
	}
}