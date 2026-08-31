package com.unnebulous.consultapronta

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.unnebulous.consultapronta.databinding.FragmentLoginBinding

class Login : Fragment() {

	private var _binding: FragmentLoginBinding? = null
	private val binding get() = _binding!!
	private lateinit var auth: FirebaseAuth

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		auth = Firebase.auth
	}

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

			auth.signInWithEmailAndPassword(email, password)
				.addOnCompleteListener { task ->
					handlePostSignIn(task)
				}
		}
	}

	private fun handlePostSignIn(task: Task<AuthResult>) {
		if (task.isSuccessful) {
			Log.i("auth", "signInWithEmail:success")

			val activity = requireActivity()
			startActivity(Intent(activity, MainActivity::class.java))
			activity.finish()
		} else {
			Log.w("auth", "signInWithEmail:failure", task.exception)
		}

	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}
}