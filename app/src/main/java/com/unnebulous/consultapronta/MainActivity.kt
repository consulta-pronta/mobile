package com.unnebulous.consultapronta

import android.graphics.Rect
import android.os.Bundle
import android.view.MotionEvent
import android.view.TouchDelegate
import androidx.appcompat.app.AppCompatActivity
import com.unnebulous.consultapronta.databinding.ActivityMainBinding
import com.unnebulous.consultapronta.views.NavbarView

class MainActivity : AppCompatActivity() {

	private lateinit var binding: ActivityMainBinding

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		binding = ActivityMainBinding.inflate(layoutInflater)
		setContentView(binding.root)

		binding.navbar.apply {
			val fragmentId = R.id.main_fragment_container

			setOnClickListener(Utils.NavbarButton.FIRST) {
				changeFragment(Home(), fragmentId)
			}
			setOnClickListener(Utils.NavbarButton.SECOND) {
				changeFragment(HistoricoSintoma(), fragmentId)
			}
			setOnClickListener(Utils.NavbarButton.MAIN) {
				changeFragmentWithBackStack(SymptomRegister(), fragmentId)
			}
			setOnClickListener(Utils.NavbarButton.FOURTH) {
				changeFragment(Hospital(), fragmentId)
			}
			setOnClickListener(Utils.NavbarButton.FIFTH) {
				changeFragment(Mais(), fragmentId)
			}
		}
	}

	fun setNavbarEntryActive(entry: Utils.NavbarButton){
		resetNavbarEntryActive()
		binding.navbar.setActive(entry)
	}

	fun resetNavbarEntryActive() {
		binding.navbar.resetStyle()
	}

	override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
		val mainButton = binding.navbar.getButtonByEnum(Utils.NavbarButton.MAIN)

		if (mainButton.isShown) {
			val location = IntArray(2)
			mainButton.getLocationOnScreen(location)

			val x = ev.rawX
			val y = ev.rawY

			val left = location[0].toFloat()
			val top = location[1].toFloat()
			val right = left + mainButton.width
			val bottom = top + mainButton.height

			// check if the touch was in the main button area
			if (x in left..right && y in top..bottom) {
				val clonedEvent = MotionEvent.obtain(ev)
				clonedEvent.setLocation(x - left, y - top)

				val handled = mainButton.dispatchTouchEvent(clonedEvent)
				clonedEvent.recycle()

				return handled
			}
		}

		return super.dispatchTouchEvent(ev)
	}
}