package com.unnebulous.consultapronta

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.unnebulous.consultapronta.database.AuthManager
import com.unnebulous.consultapronta.databinding.FragmentPasswordRecoveryBinding
import kotlinx.coroutines.tasks.await

class PasswordRecovery : Fragment() {
	private var _binding: FragmentPasswordRecoveryBinding? = null
	private val binding get() = _binding!!

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = FragmentPasswordRecoveryBinding.inflate(
			inflater,
			container,
			false
		)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		binding.apply {
			header.setGoBackButtonOnClickListener {
				popBackStack()
			}

			recoverEmailButton.setOnClickListener {
				val email = emailInput.text.toString()

				AuthManager.auth.sendPasswordResetEmail(email)
					.addOnCompleteListener { task ->
						if (task.isSuccessful) {
							typeEmailLabel.isVisible = false
							emailInput.isVisible = false

							openEmailLabel.isVisible = true
							gotoLoginLabel.isVisible = true

							recoverEmailButton.apply {
								this.text = getString(R.string.go_back)
								this.setOnClickListener {
									popBackStack()
								}
							}
						} else {
							showSnackbar(getString(R.string.error_email_not_sent), Utils.SnackBarType.DANGER)
						}
					}
			}
		}
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}
}