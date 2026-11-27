package com.unnebulous.consultapronta

import android.content.Context
import android.content.Intent
import android.text.SpannableStringBuilder
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.unnebulous.consultapronta.Utils.stringToEnum
import com.unnebulous.consultapronta.database.Symptom
import com.unnebulous.consultapronta.databinding.BottomSheetBinding
import com.unnebulous.consultapronta.databinding.SnackbarBinding
import com.unnebulous.consultapronta.recyclerview.SpacingItemDecoration
import com.unnebulous.consultapronta.views.HeaderView
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Month
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Locale
import java.util.Locale.getDefault
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

fun Fragment.showSnackbar(
	message: String,
	type: Utils.SnackBarType = Utils.SnackBarType.INFO,
	duration: Int = Snackbar.LENGTH_LONG
) {
	if (!isAdded || view == null) return

	val context = requireContext()

	val values = when (type) {
		Utils.SnackBarType.INFO ->
			R.drawable.ic_info to R.color.neutral

		Utils.SnackBarType.SUCCESS ->
			R.drawable.ic_check to R.color.success

		Utils.SnackBarType.WARNING ->
			R.drawable.ic_warning to R.color.warning

		Utils.SnackBarType.DANGER ->
			R.drawable.ic_danger to R.color.error
	}

	val drawable = ContextCompat.getDrawable(
		context,
		values.first
	)

	val color = ContextCompat.getColorStateList(
		context,
		values.second
	)

	val rootContainer = requireActivity()
		.findViewById<View>(android.R.id.content)

	val snackbar = Snackbar.make(
		rootContainer,
		"",
		duration
	)

	val snackbarView = snackbar.view as ViewGroup

	val textView = snackbarView.findViewById<TextView>(
		com.google.android.material.R.id.snackbar_text
	)

	val snackbarBinding = SnackbarBinding.inflate(
		layoutInflater
	)

	textView.visibility = View.INVISIBLE

	snackbarBinding.snackbarText.text = message

	snackbarBinding.snackbarIcon.setImageDrawable(drawable)

	snackbarBinding.snackbarIcon.backgroundTintList = color

	snackbarView.setPadding(0, 0, 0, 250)

	snackbarView.setBackgroundColor(
		ContextCompat.getColor(
			context,
			android.R.color.transparent
		)
	)

	snackbarView.removeAllViews()

	snackbarView.addView(
		snackbarBinding.root,
		ViewGroup.LayoutParams(
			ViewGroup.LayoutParams.MATCH_PARENT,
			ViewGroup.LayoutParams.WRAP_CONTENT
		)
	)

	snackbar.show()
}

fun Fragment.setNavbarEntryActive(entry: Utils.NavbarButton) {
	(activity as? MainActivity)?.setNavbarEntryActive(entry)
}

fun Fragment.resetNavbarEntryActive() {
	(activity as? MainActivity)?.resetNavbarEntryActive()
}

val Fragment.recyclerviewSpacing get() = SpacingItemDecoration(resources.getDimensionPixelSize(
	R.dimen.default_inner_elements_spacing
))

fun Fragment.changeActivityWithBackStackCleared(activityClass: Class<*>) {
	val activity = requireActivity()
	val intent = Intent(activity, activityClass)
	intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
	startActivity(intent)
	activity.finish()
}

fun Fragment.setupSelect(select: MaterialAutoCompleteTextView, array: Array<String>) {
	select.apply {
		setAdapter(createAdapter(array))
		setOnClickListener {
			showDropDown()
		}
	}
}

fun Fragment.createAdapter(array: Array<String>) =
	ArrayAdapter(requireContext(), R.layout.item_spinner, array).apply {
		setDropDownViewResource(R.layout.item_spinner)
	}

fun Context.clearCache() {
	try {
		cacheDir.deleteRecursively()
	} catch (e: Exception) {
		e.printStackTrace()
		Log.e("ErroPronto", getString(R.string.failed_to_delete_cache), e)
	}
}

fun Double.remap(istart: Double, istop: Double, ostart: Double, ostop: Double) =
	ostart + (this - istart) * (ostop - ostart) / (istop - istart)

fun LocalDateTime.toBrazillianFullDateAndTime(context: Context): String {
	val month = when (this.month) {
		Month.JANUARY -> context.getString(R.string.jan)
		Month.FEBRUARY -> context.getString(R.string.fev)
		Month.MARCH -> context.getString(R.string.mar)
		Month.APRIL -> context.getString(R.string.abr)
		Month.MAY -> context.getString(R.string.ma)
		Month.JUNE -> context.getString(R.string.jun)
		Month.JULY -> context.getString(R.string.jul)
		Month.AUGUST -> context.getString(R.string.ago)
		Month.SEPTEMBER -> context.getString(R.string.set)
		Month.OCTOBER -> context.getString(R.string.out)
		Month.NOVEMBER -> context.getString(R.string.nov)
		Month.DECEMBER -> context.getString(R.string.dez)
	}

	val hour = if (hour >= 10) {
		hour.toString()
	} else {
		context.getString(R.string.hour_format, hour)
	}

	val minute = if (minute >= 10) {
		minute.toString()
	} else {
		context.getString(R.string.minute_format, minute)
	}

	return context.getString(R.string.date_format, dayOfMonth, month, year, hour, minute)
}

fun LocalDateTime.toFirestoreTimestamp() =
	Timestamp(atZone(ZoneId.systemDefault()).toInstant())

fun LocalDate.toLocalDateTime(): LocalDateTime = LocalDateTime.of(this, LocalTime.MIDNIGHT)

fun LocalDate.toEpochMilli(): Long =
	ZonedDateTime
		.of(this.toLocalDateTime(), ZoneId.systemDefault())
		.toInstant()
		.toEpochMilli()

fun Timestamp.toBrazilianLocale(context: Context): String {
	val date = toDate()
	val locale = Locale.forLanguageTag(context.getString(R.string.language_tag))

	return SimpleDateFormat(context.getString(R.string.pattern1), locale).format(date)
}

fun Timestamp.toLocalDateTime(): LocalDateTime =
	toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()

inline fun <reified T: Enum<T>> DocumentSnapshot.getEnum(field: String): T? {
	return stringToEnum<T>(get(field).toString())
}

fun String.capitalizeFix(): String = lowercase().replaceFirstChar {
	if (it.isLowerCase()) it.titlecase(getDefault()) else it.toString()
}

fun Timestamp.toSimpleDate(context: Context): String {
	val date = toDate()
	val locale = Locale.forLanguageTag(context.getString(R.string.language_tag))

	return SimpleDateFormat(context.getString(R.string.pattern2), locale).format(date)
}

fun Timestamp.toISODate(context: Context): String {
	val date = toDate()
	val locale = Locale.forLanguageTag(context.getString(R.string.language_tag))

	return SimpleDateFormat(context.getString(R.string.pattern3), locale).format(date)
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

fun String.toEditable() = SpannableStringBuilder(this)