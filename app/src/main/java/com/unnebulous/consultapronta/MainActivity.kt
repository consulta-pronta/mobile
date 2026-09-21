package com.unnebulous.consultapronta

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.unnebulous.consultapronta.databinding.ActivityMainBinding

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
				changeFragment(SymptomRegister(), fragmentId)
			}
			setOnClickListener(Utils.NavbarButton.FOURTH) {
				changeFragment(Hospital(), fragmentId)
			}
			setOnClickListener(Utils.NavbarButton.FIFTH) {
				changeFragment(Mais(), fragmentId)
			}
		}
	}
}