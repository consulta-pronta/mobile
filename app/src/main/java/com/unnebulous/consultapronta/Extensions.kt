package com.unnebulous.consultapronta

import android.app.ActionBar
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import androidx.core.view.children
import com.bumptech.glide.util.Util
import com.unnebulous.consultapronta.databinding.SnackbarBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.unnebulous.consultapronta.databinding.BottomSheetBinding
import com.unnebulous.consultapronta.views.HeaderView

fun AppCompatActivity.changeFragment(fragment: Fragment, containerId: Int) {
	supportFragmentManager
		.beginTransaction()
		.replace(containerId, fragment)
		.commit()
}

fun AppCompatActivity.changeFragmentWithBackStack(fragment: Fragment, containerId: Int) {
	supportFragmentManager
		.beginTransaction()
		.setReorderingAllowed(true)
		.replace(containerId, fragment)
		.addToBackStack(null)
		.commit()
}

fun Fragment.changeFragment(fragment: Fragment) {
	(requireActivity() as AppCompatActivity).changeFragment(fragment, id)
}

fun Fragment.changeFragmentWithBackStack(fragment: Fragment) {
	(requireActivity() as AppCompatActivity).changeFragmentWithBackStack(fragment, id)
}

fun Fragment.updateHeader(updateBlock: HeaderView.() -> Unit) {
	(activity as? MainActivity)?.findViewById<HeaderView>(R.id.header)?.apply(updateBlock)
}

fun Fragment.popBackStack() {
	parentFragmentManager.popBackStack()
}

fun Fragment.configBottomSheet(configBlock: (BottomSheetBinding, BottomSheetDialog) -> Unit) {
	val dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialog)
	val dialogBinding = BottomSheetBinding.inflate(layoutInflater, null, false)

	dialogBinding.negativeButton.setOnClickListener {
		dialog.dismiss()
	}

	configBlock(dialogBinding, dialog)

	dialog.setContentView(dialogBinding.root)
	dialog.show()
}

fun Fragment.showSnackbar(message: String, type: Utils.SnackBarType = Utils.SnackBarType.INFO, duration: Int = Snackbar.LENGTH_LONG) {
	var drawable: Drawable?
	var color: ColorStateList?

	when (type) {
		Utils.SnackBarType.INFO -> {
			drawable = ContextCompat.getDrawable(requireContext(), R.drawable.ic_info)
			color = ContextCompat.getColorStateList(requireContext(), R.color.neutral)
		}

		Utils.SnackBarType.SUCCESS -> {
			drawable = ContextCompat.getDrawable(requireContext(), R.drawable.ic_check)
			color = ContextCompat.getColorStateList(requireContext(), R.color.success)
		}

		Utils.SnackBarType.WARNING -> {
			drawable = ContextCompat.getDrawable(requireContext(), R.drawable.ic_warning)
			color = ContextCompat.getColorStateList(requireContext(), R.color.warning)
		}

		Utils.SnackBarType.DANGER -> {
			drawable = ContextCompat.getDrawable(requireContext(), R.drawable.ic_danger)
			color = ContextCompat.getColorStateList(requireContext(), R.color.error)
		}
	}

	val snackbar = Snackbar.make(requireView(), "", duration)
	val snackbarView = snackbar.view as ViewGroup
	val textView = snackbarView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
	val snackbarBinding = SnackbarBinding.inflate(layoutInflater)

	textView.visibility = View.INVISIBLE
	snackbarBinding.snackbarText.text = message
	snackbarBinding.snackbarIcon.setImageDrawable(drawable)
	snackbarBinding.snackbarIcon.backgroundTintList = color

	snackbarView.setPadding(0, 0, 0, 250)
	snackbarView.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.transparent))
	snackbarView.removeAllViews()
	snackbarView.addView(snackbarBinding.root, 0)

	snackbar.show()
}

fun Context.clearCache() {
	try {
		cacheDir.deleteRecursively()
	} catch (e: Exception) {
		e.printStackTrace()
		Log.e("ErroPronto", "Erro ao apagar cache", e)
	}
}