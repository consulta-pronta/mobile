package com.unnebulous.consultapronta

import android.content.Context
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
import com.unnebulous.consultapronta.databinding.ToastBinding
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

fun Fragment.showSnackbar(message: String, duration: Int = Snackbar.LENGTH_LONG) {
	Log.i("TestePronto", "snackbar")

	val snackbar = Snackbar.make(requireView(), "", duration)
	val snackbarView = snackbar.view as ViewGroup

	// Esconde o texto padrão do Snackbar
	val textView = snackbarView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
	textView.visibility = View.INVISIBLE

	// Infla o seu layout customizado (toast.xml)
	val customBinding = ToastBinding.inflate(layoutInflater)
	customBinding.toastText.text = message

	// Remove paddings e o fundo padrão para usar o seu drawable arredondado
	snackbarView.setPadding(0, 0, 0, 0)
	snackbarView.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.transparent))
	snackbarView.background = null
	
	snackbarView.addView(customBinding.root, 0)
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