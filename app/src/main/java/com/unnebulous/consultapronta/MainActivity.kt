package com.unnebulous.consultapronta

import android.os.Bundle
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.unnebulous.consultapronta.database.AuthManager
import com.unnebulous.consultapronta.databinding.ActivityMainBinding
import kotlinx.coroutines.launch
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

	private lateinit var binding: ActivityMainBinding

	private val notificationPermissionLauncher =
		registerForActivityResult(
			ActivityResultContracts.RequestPermission()
		) { isGranted ->

			if (isGranted) {
				// todo: quando for permitido
			} else {
				// todo: quando for negado
			}
		}

	private fun checkNotificationPermission() {

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
			val permission = Manifest.permission.POST_NOTIFICATIONS

			if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
				notificationPermissionLauncher.launch(permission)
			}
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		binding = ActivityMainBinding.inflate(layoutInflater)
		setContentView(binding.root)

		checkNotificationPermission()

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

		lifecycleScope.launch {
			AuthManager.userInFirestore = AuthManager.getUserData()!!
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

	fun setFragmentPadding(horizontalPadding: Int = resources.getDimensionPixelSize(R.dimen.default_screen_padding)) {
		binding.mainFragmentContainer.setPadding(horizontalPadding, 0, horizontalPadding, 0)
	}
}