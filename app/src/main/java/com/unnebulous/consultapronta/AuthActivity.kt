package com.unnebulous.consultapronta

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.unnebulous.consultapronta.databinding.ActivityAuthBinding

class AuthActivity : AppCompatActivity() {

	private lateinit var binding: ActivityAuthBinding

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		binding = ActivityAuthBinding.inflate(layoutInflater)
		setContentView(binding.root)

		// se a ActivityAuth está rodando pela primeira vez
		// (isso não mostra que o usuário está usando o aplicativo pela primeira vez!)
		if (savedInstanceState == null) {
			// variável separada para permitir a troca de qual o fragmento será iniciado
			var fragment: Fragment

			fragment = Cadastro()

			supportFragmentManager
				.beginTransaction()
				.replace(R.id.fragment_container, fragment)
				.commit()
		}
	}
}