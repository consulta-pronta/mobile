package com.unnebulous.consultapronta

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.unnebulous.consultapronta.database.AuthManager
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

		binding.header.setGoBackButtonOnClickListener {
			popBackStack()
		}

		binding.signUpButton.setOnClickListener {
			changeFragmentWithBackStack(Cadastro())
		}

		binding.enterButton.setOnClickListener {
			val email = binding.emailInput.text.toString()
			val password = binding.passwordInput.text.toString()

			if (email.isBlank() || password.isBlank()) {
				showSnackbar(getString(R.string.error_blank_input), Utils.SnackBarType.WARNING)
				return@setOnClickListener
			}

			AuthManager.auth.signInWithEmailAndPassword(email, password)
				.addOnCompleteListener { task ->
					handlePostSignIn(task)
				}
		}
	}

	private fun handlePostSignIn(task: Task<AuthResult>) {
		if (task.isSuccessful) {
			Log.i("auth", "signInWithEmail:success")

			try {
				val activity = requireActivity()
				startActivity(Intent(activity, MainActivity::class.java))
				activity.finish()
			} catch (error: IllegalStateException) {
				Log.wtf("auth", "signInWithEmail:failure", error)
			}
		} else {
			Log.w("auth", "signInWithEmail:failure", task.exception)
			if (task.exception is FirebaseAuthInvalidCredentialsException) {
				showSnackbar(getString(R.string.error_invalid_input_data), Utils.SnackBarType.WARNING)
			}
		}

	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}
}