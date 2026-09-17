package com.unnebulous.consultapronta

import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.Timestamp
import com.unnebulous.consultapronta.databinding.BottomSheetBinding
import com.unnebulous.consultapronta.views.HeaderView
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.Locale
import kotlin.math.abs

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

fun Context.clearCache() {
	try {
		cacheDir.deleteRecursively()
	} catch (e: Exception) {
		e.printStackTrace()
		Log.e("ErroPronto", "Erro ao apagar cache", e)
	}
}

fun LocalDateTime.toFirestoreTimestamp() =
	Timestamp(atZone(ZoneId.systemDefault()).toInstant())

fun LocalDate.toLocalDateTime() =
	LocalDateTime.of(this, LocalTime.MIDNIGHT)

fun Timestamp.toBrazilianLocale(): String {
	val date = toDate()
	val locale = Locale.forLanguageTag("pt-BR")

	return SimpleDateFormat("d 'de' MMM 'de' yyyy", locale).format(date)
}

fun Timestamp.diffSeconds(other: Timestamp) = abs(seconds - other.seconds)

fun Timestamp.diffDays(other: Timestamp) = diffSeconds(other) / (24 * 3600)
