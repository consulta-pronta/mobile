package com.unnebulous.consultapronta

import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.unnebulous.consultapronta.database.Symptom
import com.unnebulous.consultapronta.databinding.BottomSheetBinding
import com.unnebulous.consultapronta.databinding.SnackbarBinding
import com.unnebulous.consultapronta.views.HeaderView
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.Locale
import java.util.Locale.getDefault
import kotlin.enums.enumEntries
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

fun Fragment.showSnackbar(message: String, type: Utils.SnackBarType = Utils.SnackBarType.INFO, duration: Int = Snackbar.LENGTH_LONG) {
	val context = requireContext()
	
	val values = when (type) {
		Utils.SnackBarType.INFO -> R.drawable.ic_info to R.color.neutral
		Utils.SnackBarType.SUCCESS -> R.drawable.ic_check to R.color.success
		Utils.SnackBarType.WARNING -> R.drawable.ic_warning to R.color.warning
		Utils.SnackBarType.DANGER -> R.drawable.ic_danger to R.color.error
	}
	val drawable = ContextCompat.getDrawable(context, values.first)
	val color = ContextCompat.getColorStateList(context, values.second)


	val snackbar = Snackbar.make(requireView(), "", duration)
	val snackbarView = snackbar.view as ViewGroup
	val textView = snackbarView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
	val snackbarBinding = SnackbarBinding.inflate(layoutInflater)

	textView.visibility = View.INVISIBLE
	snackbarBinding.snackbarText.text = message
	snackbarBinding.snackbarIcon.setImageDrawable(drawable)
	snackbarBinding.snackbarIcon.backgroundTintList = color

	snackbarView.setPadding(0, 0, 0, 250)
	snackbarView.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
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

fun Double.remap(istart: Double, istop: Double, ostart: Double, ostop: Double) =
	ostart + (this - istart) * (ostop - ostart) / (istop - istart)

fun LocalDateTime.toFirestoreTimestamp() =
	Timestamp(atZone(ZoneId.systemDefault()).toInstant())

fun LocalDate.toLocalDateTime(): LocalDateTime = LocalDateTime.of(this, LocalTime.MIDNIGHT)

fun Timestamp.toBrazilianLocale(): String {
	val date = toDate()
	val locale = Locale.forLanguageTag("pt-BR")

	return SimpleDateFormat("d 'de' MMM 'de' yyyy", locale).format(date)
}

inline fun <reified T: Enum<T>> DocumentSnapshot.getEnum(field: String): T? {
	val value = get(field).toString()
	return enumEntries<T>().find { it.name.equals(value, ignoreCase = true) }
}

fun String.capitalizeFix(): String = lowercase().replaceFirstChar {
	if (it.isLowerCase()) it.titlecase(getDefault()) else it.toString()
}

fun Timestamp.toSimpleDate(): String {
	val date = toDate()
	val locale = Locale.forLanguageTag("pt-BR")

	return SimpleDateFormat("dd'/'MM", locale).format(date)
}

fun Timestamp.diffSeconds(other: Timestamp) = abs(seconds - other.seconds)

fun Timestamp.diffDays(other: Timestamp) = diffSeconds(other) / (24 * 3600)

suspend fun List<Symptom>.mergedHistoric() = this + flatMap { it.getHistoric() }

fun List<Symptom>.getIntensityAverage() =
	map { it.intensity }.average().let { value ->
		if (value.isNaN()) 0.0 else value
	}

fun List<Symptom>.getAreaMap() = groupBy { it.place }

fun List<Symptom>.getMostAffectArea() =
	getAreaMap().maxByOrNull { it.value.size }?.key
