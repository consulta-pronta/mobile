package com.unnebulous.consultapronta

import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.tasks.Task
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.firebase.Timestamp
import com.google.firebase.auth.AuthResult
import com.unnebulous.consultapronta.database.AuthManager
import com.unnebulous.consultapronta.database.DatabaseManager
import com.unnebulous.consultapronta.database.User
import com.unnebulous.consultapronta.databinding.FragmentCadastroBinding
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import kotlin.properties.Delegates

class Cadastro : Fragment() {

	/* TODO
	* - Especialidade do profissional
	* - Remover profile view
	* */

	private var _binding: FragmentCadastroBinding? = null
	private val binding get() = _binding!!

	private val dateFormatter by lazy { DateTimeFormatter.ofPattern(getString(R.string.DATE_FORMAT)) }

	private var part by Delegates.observable(1) { _, old, new ->
		binding.apply {
			when (old) {
				1 -> firstPart.isVisible = false
				2 -> secondPart.isVisible = false
				3 -> thirdPart.isVisible = false
			}

			when (new) {
				1 -> {
					firstPart.isVisible = true
					previousButton.isVisible = false
				}
				2 -> {
					secondPart.isVisible = true
					previousButton.isVisible = true
					createAccountButton.text = getString(R.string.next_label)
				}
				3 -> {
					thirdPart.isVisible = true
					createAccountButton.text = getString(R.string.create_account)
				}
			}
		}
	}

	var userType = Utils.UserType.PACIENTE
	var email = ""
	var phoneNumber = ""
	var password = ""
	var confirmPassword = ""
	var isPasswordValid = false
	var isPasswordCorrect = false
	var name = ""
	var cpf = ""
	var address = ""
	var birthDate: LocalDate? = null
	var biologicalSex = ""
	var genderIdentity = ""
	var weight: Float? = null
	var height: Float? = null
	var bloodType = ""
	var crm = ""
	var uf = ""
	var operationArea = ""

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

		binding.userTypeSwitch.option = Utils.UserType.PACIENTE

		configSelects()

		binding.userTypeSwitch.setOnClickListener {
			userType = binding.userTypeSwitch.switch()

			if (userType == Utils.UserType.PACIENTE) {
				binding.apply {
					professionalCrmInputs.visibility = View.GONE
					operationAreaInput.visibility = View.GONE

					patientDataInputs.visibility = View.VISIBLE
					bloodtypeInputlayout.visibility = View.VISIBLE
				}
			} else {
				binding.apply {
					professionalCrmInputs.visibility = View.VISIBLE
					operationAreaInput.visibility = View.VISIBLE

					patientDataInputs.visibility = View.GONE
					bloodtypeInputlayout.visibility = View.GONE
				}
			}
		}

		binding.signInButton.setOnClickListener {
			changeFragmentWithBackStack(Login())
		}

		binding.createAccountButton.setOnClickListener {
			if (part == 3) {
				if (verifyInputs()) {
//				    (activity as AuthActivity).userTemp.apply {
//				    	this.name = name
//					    this.userType = userType
//					    this.cpf = cpf
//					    this.email = email
//					    this.phoneNumber = phoneNumber
//					    this.password = password
//
//					    this.contactForms["email"] = this.email
//					    this.contactForms["sms"] = this.phoneNumber
//				    }

					AuthManager.auth.createUserWithEmailAndPassword(email, password)
						.addOnCompleteListener { task -> handlePostSignUp(task) }
				}
			} else if (verifyInputs()) {
				part++
			}
		}

		binding.previousButton.setOnClickListener {
			part--
		}

		binding.birthDateInput.setOnClickListener {
			Utils.showDatePicker(this, defaultDate = birthDate ?: LocalDate.now()) { date ->
				birthDate = date

				binding.birthDateInput.text = date.format(dateFormatter)
				binding.birthDateInput.setTextColor(ContextCompat.getColor(requireContext(), R.color.textLight))
			}
		}

		configPasswordInputs()

		binding.cpfInput.addTextChangedListener(Utils.buildCpfMask())
		binding.phoneNumberInput.addTextChangedListener(Utils.buildPhoneMask())
	}

	private fun setupSelect(select: MaterialAutoCompleteTextView, array: Array<String>) {
		select.apply {
			setAdapter(createAdapter(array))
			setOnClickListener {
				showDropDown()
			}
		}
	}

	private fun createAdapter(array: Array<String>) =
		ArrayAdapter(requireContext(), R.layout.item_spinner, array).apply {
			setDropDownViewResource(R.layout.item_spinner)
		}

	private fun verifyInputs(): Boolean {
		var isValid = true
		var oneOrMoreInputBlank = false

		val verifyInputsList: (List<String>) -> Unit = { inputs ->
			oneOrMoreInputBlank = false
			for (item in inputs) {
				if (item.trim().isBlank()) {
					oneOrMoreInputBlank = true
					break
				}
			}

		}

		binding.apply {
			when (part) {
				1 -> {
					email = emailInput.text.toString().trim()
					phoneNumber = phoneNumberInput.text.toString().trim()

					verifyInputsList(listOf(email, phoneNumber))

					isValid = !oneOrMoreInputBlank && isPasswordCorrect
				}

				2 -> {
					name = nameInput.text.toString().trim()
					cpf = cpfInput.text.toString().trim()
					address = addressInput.toString().trim()

					verifyInputsList(listOf(name, cpf, address))

					isValid = !oneOrMoreInputBlank && birthDate != null
				}

				3 -> {
					biologicalSex = biologicalSexInput.text.toString()
					genderIdentity = genderIdentityInput.text.toString()

					val isUserSpecificInputValid = if (userType == Utils.UserType.PACIENTE) {
						weight = weightInput.text.toString().trim().toFloatOrNull()
						height = heightInput.text.toString().trim().toFloatOrNull()
						bloodType = bloodtypeInput.text.toString().trim()

						verifyInputsList(listOf(bloodType))

						!oneOrMoreInputBlank && weight != null && height != null
					} else {
						crm = crmInput.text.toString().trim()
						uf = crmUfInput.text.toString().trim()
						operationArea = operationAreaInput.text.toString().trim()

						verifyInputsList(listOf(crm, uf, operationArea))

						!oneOrMoreInputBlank
					}

					verifyInputsList(listOf(biologicalSex, genderIdentity))

					isValid = isUserSpecificInputValid && !oneOrMoreInputBlank
				}
			}
		}

		if (!isValid) {
			val error = if (oneOrMoreInputBlank || weight == null || height == null || birthDate == null) {
				getString(R.string.error_blank_input)
			} else if (!isPasswordValid) {
				getString(R.string.error_password_not_valid)
			} else {
				getString(R.string.error_password_not_the_same)
			}

			showSnackbar(error, Utils.SnackBarType.WARNING)
		}

		return isValid
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	private fun configPasswordInputs() {
		val drawables = Array(4) {0}
		val colors = Array(4) {0}

		val successColor = ContextCompat.getColor(requireContext(), R.color.success)
		val errorColor = ContextCompat.getColor(requireContext(), R.color.error)

		val successDrawable = R.drawable.ic_check_circle
		val errorDrawable = R.drawable.ic_error_circle

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

		binding.passwordInput.doOnTextChanged { text, _, _, _ ->
			text?.let {
				password = text.toString()

				var haveLowercase = false
				var haveUppercase = false
				var haveNumber = false

				it.forEach { char ->
					haveLowercase = (char.isLowerCase() || haveLowercase)
					haveUppercase = (char.isUpperCase() || haveUppercase)
					haveNumber = (char.isDigit() || haveNumber)
				}

				val isGreaterOrEqualThan8 = it.length >= 8
				val haveLowerAndUppercase = haveLowercase && haveUppercase
				val isEqualConfirmPassword = password.isNotBlank() && confirmPassword.isNotBlank() && password == confirmPassword

				colors[0] = if (isGreaterOrEqualThan8) successColor else errorColor
				drawables[0] = if (isGreaterOrEqualThan8) successDrawable else errorDrawable

				colors[1] = if (haveLowerAndUppercase) successColor else errorColor
				drawables[1] = if (haveLowerAndUppercase) successDrawable else errorDrawable

				colors[2] = if (haveNumber) successColor else errorColor
				drawables[2] = if (haveNumber) successDrawable else errorDrawable

				colors[3] = if (isEqualConfirmPassword) successColor else errorColor
				drawables[3] = if (isEqualConfirmPassword) successDrawable else errorDrawable

				binding.passwordErrorLength.setCompoundDrawablesRelativeWithIntrinsicBounds(drawables[0], 0, 0, 0)
				changeTextViewDrawableColors(binding.passwordErrorLength, colors[0])
				binding.passwordErrorLength.setTextColor(colors[0])

				binding.passwordErrorCaps.setCompoundDrawablesRelativeWithIntrinsicBounds(drawables[1], 0, 0, 0)
				changeTextViewDrawableColors(binding.passwordErrorCaps, colors[1])
				binding.passwordErrorCaps.setTextColor(colors[1])

				binding.passwordErrorNumbers.setCompoundDrawablesRelativeWithIntrinsicBounds(drawables[2], 0, 0, 0)
				changeTextViewDrawableColors(binding.passwordErrorNumbers, colors[2])
				binding.passwordErrorNumbers.setTextColor(colors[2])

				binding.passwordErrorEquals.setCompoundDrawablesRelativeWithIntrinsicBounds(drawables[3], 0, 0, 0)
				changeTextViewDrawableColors(binding.passwordErrorEquals, colors[3])
				binding.passwordErrorEquals.setTextColor(colors[3])

				isPasswordValid = isGreaterOrEqualThan8 && haveLowerAndUppercase && haveNumber
				isPasswordCorrect = isPasswordValid && isEqualConfirmPassword
			}
		}

		binding.confirmPasswordInput.doOnTextChanged { text, _, _, _ ->
			text?.let {
				confirmPassword = text.toString()

				val isEqualConfirmPassword = password.isNotBlank() && confirmPassword.isNotBlank() && password == confirmPassword

				colors[3] = if (isEqualConfirmPassword) successColor else errorColor
				drawables[3] = if (isEqualConfirmPassword) successDrawable else errorDrawable

				binding.passwordErrorEquals.setCompoundDrawablesRelativeWithIntrinsicBounds(drawables[3], 0, 0, 0)
				changeTextViewDrawableColors(binding.passwordErrorEquals, colors[3])
				binding.passwordErrorEquals.setTextColor(colors[3])

				isPasswordCorrect = isPasswordValid && isEqualConfirmPassword
			}
		}

		binding.passwordInput.setOnFocusChangeListener { _, hasFocus ->
			if (hasFocus) {
				binding.passwordErrorsLayout.visibility = View.VISIBLE
			} else {
				binding.passwordErrorsLayout.visibility = View.GONE
			}
		}

		binding.confirmPasswordInput.setOnFocusChangeListener { _, hasFocus ->
			if (hasFocus) {
				binding.passwordErrorsLayout.visibility = View.VISIBLE
			} else {
				binding.passwordErrorsLayout.visibility = View.GONE
			}
		}
	}

	private fun configSelects() {
		val ufs = Utils.UF.entries.map { it.name }.toTypedArray()
		setupSelect(binding.crmUfInput, ufs)

		val bloodTypes = Utils.BLOODTYPE.entries.map { it.display }.toTypedArray()
		setupSelect(binding.bloodtypeInput, bloodTypes)

		val biologicalSexes = arrayOf(
			"Masculino",
			"Feminino",
			"Intersexo",
			"Prefiro não informar"
		)
		setupSelect(binding.biologicalSexInput, biologicalSexes)

		val genderIdentityArray = arrayOf(
			"Homem cisgênero",
			"Mulher cisgênero",
			"Homem transexual",
			"Mulher transexual",
			"Pessoa não binária",
			"Prefiro não informar"
		)
		setupSelect(binding.genderIdentityInput, genderIdentityArray)
	}

	private fun handlePostSignUp(task: Task<AuthResult>) {
		if (!task.isSuccessful) {
			Log.w("auth", "signUpWithEmail:failure", task.exception)
			return
		}
		Log.i("auth", "signUpWithEmail:success")

		val user = task.result.user!!
		val creationTime = user.metadata?.creationTimestamp!!

		val userFormData = User.Companion.FormData(
			name = name,
			email = user.email!!,
			phone = phoneNumber,
			cpf = cpf,
			address = address,
			biological_sex = biologicalSex,
			gender = genderIdentity,
			user_type = userType,
			birth_date = birthDate?.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))!!,
			created_at = Timestamp(Date(creationTime))
		)

		lifecycleScope.launch {
			try {
				DatabaseManager.userDocument
					.set(userFormData.toMap())
					.await()

				Log.i("auth", "setUserDocument:success")

				val userData = mutableMapOf<String, Any>().apply {
					if (userType == Utils.UserType.PACIENTE) {
						this["weight"] = weight!!
						this["height"] = height!!
						this["blood_type"] = bloodType
					} else {
						this["crm"] = crm
						this["operation_area"] = operationArea
						this["uf"] = uf
					}
				}

				if (userType == Utils.UserType.PROFISSIONAL) {
					DatabaseManager.db.collection("signupRequests")
						.document(user.uid)
						.set(userData)
						.await()

					Log.i("auth", "requestProfessional:success")
				} else {
					DatabaseManager.userDocument
						.update(DatabaseManager.PATIENT_USER_DATA, userData)
						.await()

					Log.i("auth", "updatePatientData:success")
				}

				val activity = requireActivity()
				startActivity(Intent(activity, MainActivity::class.java))
				activity.finish()
			} catch (e: Exception) {
				Log.w("auth", "setUserDocument:failure", e)
			}
		}
	}
}