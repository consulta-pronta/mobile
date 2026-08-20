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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import com.google.firebase.firestore.firestore
import com.unnebulous.consultapronta.data.User
import com.unnebulous.consultapronta.databinding.FragmentCadastroBinding
import java.util.Date

class Cadastro : Fragment() {

	private var _binding: FragmentCadastroBinding? = null
	private val binding get() = _binding!!
	private lateinit var auth: FirebaseAuth
	private lateinit var db: FirebaseFirestore

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		db = Firebase.firestore
		auth = Firebase.auth
	}

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
			val email = binding.emailInput.text.toString()
			val password = binding.passwordInput.text.toString()

			auth.createUserWithEmailAndPassword(email, password)
				.addOnCompleteListener { task -> handlePostSignUp(task) }
		}
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}


	private fun handlePostSignUp(task: Task<AuthResult>) {
		if (!task.isSuccessful) {
			Log.w("auth", "signUpWithEmail:failure", task.exception)
			return
		}
		Log.i("auth", "signUpWithEmail:success")

		val user = task.result.user!!
		val creationTime = user.metadata?.creationTimestamp

		val userDocument = User(
			name = binding.nameInput.text.toString(),
			email = user.email!!,
			phone = binding.phoneNumberInput.text.toString(),
			cpf = binding.cpfInput.text.toString(),
			createdAt = Timestamp(Date(creationTime!!))
		)

		db.collection("users")
			.document(user.uid)
			.set(userDocument)
			.addOnSuccessListener {
				Log.i("auth", "setUserDocument:success")

				val activity = requireActivity()
				startActivity(Intent(activity, MainActivity::class.java))
				activity.finish()
			}
			.addOnFailureListener { e ->
				Log.w("auth", "setUserDocument:failure", e)
			}
	}
}