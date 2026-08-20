package com.unnebulous.consultapronta

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import com.unnebulous.consultapronta.databinding.FragmentCadastroBinding

class Cadastro : Fragment() {

	private var _binding: FragmentCadastroBinding? = null
	private val binding get() = _binding!!

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = FragmentCadastroBinding.inflate(layoutInflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		binding.userTypeSwitch.setOnClickListener {
			/*
			* Retorna 0 se usuário for Paciente
			* Retorna 1 se usuário for Profissional
			* */
			val userType = binding.userTypeSwitch.changeUser()

			binding.sendCrmButton.visibility = if (userType == 0) View.GONE else View.VISIBLE
		}

		binding.signInButton.setOnClickListener {
			parentFragmentManager.beginTransaction()
				.setReorderingAllowed(true)
				.replace(
					R.id.fragment_container,
					Login()
				)
				.addToBackStack(null)
				.commit()
		}

		binding.createAccountButton.setOnClickListener {
			startActivity(Intent(requireActivity(), MainActivity::class.java))
			requireActivity().finish()
		}

		binding.passwordInput.setOnFocusChangeListener { _, hasFocus ->
			if (hasFocus) {
				binding.passwordErrorsLayout.visibility = View.VISIBLE
			} else {
				binding.passwordErrorsLayout.visibility = View.GONE
			}
		}
		
		binding.passwordInput.doOnTextChanged { text, start, before, count ->

		}
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}
}