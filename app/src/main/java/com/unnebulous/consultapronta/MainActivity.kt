package com.unnebulous.consultapronta

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.unnebulous.consultapronta.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

	private lateinit var binding: ActivityMainBinding

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		binding = ActivityMainBinding.inflate(layoutInflater)
		setContentView(binding.root)

		if (savedInstanceState == null) {
			changeFragment(Home(), R.id.main_fragment_container)
		}

		binding.navbar.setOnClickListener(Utils.NavbarButton.FIRST) { view ->
			//if (binding.mainFragmentContainer.getFragment<>())
			changeFragment(Home(), R.id.main_fragment_container)
		}
		binding.navbar.setOnClickListener(Utils.NavbarButton.SECOND) { view ->
			changeFragment(HistoricoSintoma(), R.id.main_fragment_container)
		}
	}
}