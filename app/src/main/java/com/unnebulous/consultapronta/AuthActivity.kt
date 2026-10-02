package com.unnebulous.consultapronta

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.unnebulous.consultapronta.database.AuthManager
import com.unnebulous.consultapronta.databinding.ActivityAuthBinding

class AuthActivity : AppCompatActivity() {

	private lateinit var binding: ActivityAuthBinding

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		binding = ActivityAuthBinding.inflate(layoutInflater)
		setContentView(binding.root)

		if (AuthManager.user != null) {
			startActivity(Intent(this, MainActivity::class.java))
			finish()
		} else {
			changeFragment(Cadastro(), R.id.fragment_container)
		}
	}
}