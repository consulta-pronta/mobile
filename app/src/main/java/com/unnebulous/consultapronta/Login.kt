package com.unnebulous.consultapronta

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.unnebulous.consultapronta.databinding.FragmentLoginBinding

class Login : Fragment() {

	private var _binding: FragmentLoginBinding? = null
	private val binding get() = _binding!!

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = FragmentLoginBinding.inflate(layoutInflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		binding.userTypeSwitch.setOnClickListener {
			/*
			* Retorna 0 se usuário for Paciente
			* Retorna 1 se usuário for Profissional
			* */
			binding.userTypeSwitch.changeUser()
		}

		binding.signUpButton.setOnClickListener {
			parentFragmentManager.beginTransaction()
				.setReorderingAllowed(true)
				.replace(
					R.id.fragment_container,
					Cadastro()
				)
				.addToBackStack(null)
				.commit()
		}
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}
}