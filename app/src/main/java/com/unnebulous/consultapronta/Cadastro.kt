package com.unnebulous.consultapronta

import android.app.ActivityManager
import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.TextViewCompat
import androidx.core.widget.doOnTextChanged
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

				val successColor = ContextCompat.getColor(requireContext(), R.color.success)
				val errorColor = ContextCompat.getColor(requireContext(), R.color.error)

				// para os leigos, isso é uma função lambda
				val changeTextViewDrawableColors: (TextView, Int) -> Unit = { view, color ->
					// loop for para acessar todos os drawables (top, bottom, start, end)
					for (drawable in view.compoundDrawables) {
						drawable?.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
						/*
						* Isso aplica um filtro de cor do tipo "PorterDuff"
						* (Segundo pesquisas, PorterDuff se refere a dois bros da informática: Thomas Porter e Tom Duff, criadores dessa
						* técnica de composição de imagens)
						* O modo SRC_IN dita que só será aplicado o filtro aonde tem pixel. Se for transparente, não será aplicado
						* */
					}
				}

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

				colors[0] = if (isGreaterOrEqualThan8) successColor else errorColor
				drawables[0] = if (isGreaterOrEqualThan8) R.drawable.ic_check_circle else R.drawable.ic_error_circle

				colors[1] = if (haveLowerAndUppercase) successColor else errorColor
				drawables[1] = if (haveLowerAndUppercase) R.drawable.ic_check_circle else R.drawable.ic_error_circle

				colors[2] = if (haveNumber) successColor else errorColor
				drawables[2] = if (haveNumber) R.drawable.ic_check_circle else R.drawable.ic_error_circle

				binding.passwordErrorLength.setCompoundDrawablesRelativeWithIntrinsicBounds(drawables[0], 0, 0, 0)
				changeTextViewDrawableColors(binding.passwordErrorLength, colors[0])
				binding.passwordErrorLength.setTextColor(colors[0])

				binding.passwordErrorCaps.setCompoundDrawablesRelativeWithIntrinsicBounds(drawables[1], 0, 0, 0)
				changeTextViewDrawableColors(binding.passwordErrorCaps, colors[1])
				binding.passwordErrorCaps.setTextColor(colors[1])

				binding.passwordErrorNumbers.setCompoundDrawablesRelativeWithIntrinsicBounds(drawables[2], 0, 0, 0)
				changeTextViewDrawableColors(binding.passwordErrorNumbers, colors[2])
				binding.passwordErrorNumbers.setTextColor(colors[2])
			}
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