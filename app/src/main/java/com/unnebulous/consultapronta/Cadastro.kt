package com.unnebulous.consultapronta

import android.app.ActivityManager
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.widget.TextViewCompat
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
		
		binding.passwordInput.doOnTextChanged { text, _, _, _ ->
			text?.let {
				val drawables = Array(3) {0}
				val colors = Array(3) {0}

				var haveLowercase = false
				var haveUppercase = false
				var haveNumber = false

				it.forEach { char ->
					haveLowercase = (char.isLowerCase() || haveLowercase)
					haveUppercase = (char.isUpperCase() || haveUppercase)
					haveNumber = (char.isDigit() || haveNumber)
				}

				val isGreaterOrEqualThan8 = it.length > 8
				val haveLowerAndUppercase = haveLowercase && haveUppercase

				colors[0] = if (isGreaterOrEqualThan8) R.color.success else R.color.error
				drawables[0] = if (isGreaterOrEqualThan8) R.drawable.ic_check_circle else R.drawable.ic_error_circle

				colors[1] = if (haveLowerAndUppercase) R.color.success else R.color.error
				drawables[1] = if (haveLowerAndUppercase) R.drawable.ic_check_circle else R.drawable.ic_error_circle

				colors[2] = if (haveNumber) R.color.success else R.color.error
				drawables[2] = if (haveNumber) R.drawable.ic_check_circle else R.drawable.ic_error_circle

				binding.passwordErrorLength.setCompoundDrawablesRelativeWithIntrinsicBounds(drawables[0], 0, 0, 0)
				binding.passwordErrorLength.setTextColor(colors[0])

				binding.passwordErrorCaps.setCompoundDrawablesRelativeWithIntrinsicBounds(drawables[1], 0, 0, 0)
				binding.passwordErrorCaps.setTextColor(colors[1])

				binding.passwordErrorNumbers.setCompoundDrawablesRelativeWithIntrinsicBounds(drawables[2], 0, 0, 0)
				binding.passwordErrorNumbers.setTextColor(colors[2])
			}
		}
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}
}