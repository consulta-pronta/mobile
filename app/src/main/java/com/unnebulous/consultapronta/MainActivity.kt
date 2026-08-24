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

		if (savedInstanceState == null) {
			supportFragmentManager
				.beginTransaction()
				.replace(R.id.main_fragment_container, Home())
				.commit()
		}
	}
}