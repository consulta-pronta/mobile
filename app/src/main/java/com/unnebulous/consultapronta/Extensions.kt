package com.unnebulous.consultapronta

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
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