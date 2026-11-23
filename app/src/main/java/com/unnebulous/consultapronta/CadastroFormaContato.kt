package com.unnebulous.consultapronta

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.content.res.ColorStateList
import android.text.InputType
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.unnebulous.consultapronta.database.AuthManager
import com.unnebulous.consultapronta.database.DatabaseManager
import com.unnebulous.consultapronta.database.User
import com.unnebulous.consultapronta.databinding.FragmentCadastroFormaContatoBinding
import com.unnebulous.consultapronta.views.SelectOptionItemView
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.format.DateTimeFormatter
import java.util.Date

class CadastroFormaContato : Fragment() {

	private var _binding: FragmentCadastroFormaContatoBinding? = null
	private val binding get() = _binding!!

	private lateinit var userTemp: UserTemp

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = FragmentCadastroFormaContatoBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		binding.header.setGoBackButtonOnClickListener {
			popBackStack()
		}

		configEmailSwitch()
		configSmsSwitch()
		configWhatsappSwitch()
		configTelegramSwitch()

		userTemp = (activity as AuthActivity).userTemp

		userTemp.apply {
			binding.emailOption.setSubtitle(email)
			contact_forms["email"] = email

			binding.smsOption.setSubtitle(phone)
			contact_forms["sms"] = phone
		}

		binding.createAccountButton.setOnClickListener {
			if (userTemp.contact_forms.keys.isNotEmpty()) {
				AuthManager.auth.createUserWithEmailAndPassword(userTemp.email, userTemp.password)
					.addOnCompleteListener { task -> handlePostSignUp(task) }
			} else {
				showSnackbar(getString(R.string.error_no_contact_form_defined), Utils.SnackBarType.WARNING)
			}
		}
	}

	private fun handlePostSignUp(task: Task<AuthResult>) {
		if (!task.isSuccessful) {
			Log.w("auth", "signUpWithEmail:failure", task.exception)
			return
		}
		Log.i("auth", "signUpWithEmail:success")

		val user = task.result.user!!
		val creationTime = user.metadata?.creationTimestamp!!

		var userFormData: User.Companion.FormData

		userTemp.apply {
			userFormData = User.Companion.FormData(
				name = name,
				email = user.email!!,
				phone = phone,
				cpf = cpf,
				address = address,
				biological_sex = biological_sex,
				gender = gender,
				user_type = user_type!!,
				birth_date = birth_date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
				contact_forms = contact_forms,
				created_at = Timestamp(Date(creationTime))
			)
		}

		lifecycleScope.launch {
			try {
				DatabaseManager
					.userDocument
					.set(userFormData.toMap())
					.await()

				Log.i("auth", "setUserDocument:success")

				val userData = mutableMapOf<String, Any>().apply {
					if (userTemp.user_type == Utils.UserType.PACIENTE) {
						this["weight"] = userTemp.user_data["weight"]!!
						this["height"] = userTemp.user_data["height"]!!
						this["blood_type"] = userTemp.user_data["blood_type"]!!
					} else {
						this["crm"] = userTemp.user_data["crm"]!!
						this["operation_area"] = userTemp.user_data["operation_area"]!!
						this["speciality"] = userTemp.user_data["speciality"]!!
						this["uf"] = userTemp.user_data["uf"]!!
					}
				}

				if (userTemp.user_type == Utils.UserType.PROFISSIONAL) {
					DatabaseManager
						.db
						.collection("signupRequests")
						.document(user.uid)
						.set(userData)
						.await()

					Log.i("auth", "requestProfessional:success")
				} else {
					DatabaseManager
						.userDocument
						.update("patient_data", userData)
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

	private fun configEmailSwitch() {
		binding.switchEmail.setOnCheckedChangeListener { button, isChecked ->
			if (isChecked) {
				configBottomSheet { dialogBinding, dialog ->
					dialogBinding.icon.visibility = View.GONE
					dialogBinding.title.text = getString(R.string.register_contact_form)

					val input = EditText(context).apply {
						layoutParams = ViewGroup.LayoutParams(
							ViewGroup.LayoutParams.MATCH_PARENT,
							ViewGroup.LayoutParams.WRAP_CONTENT
						)

						if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
							setAutofillHints(View.AUTOFILL_HINT_EMAIL_ADDRESS)
						}

						inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
						background = ContextCompat.getDrawable(context, R.drawable.shape_input)
						setCompoundDrawablesRelativeWithIntrinsicBounds(
							ContextCompat.getDrawable(context, R.drawable.ic_email),
							null,
							null,
							null
						)
						compoundDrawablePadding = 20
						hint = getString(R.string.type_contact_form)
						text = userTemp.contact_forms["email"]?.toEditable()

						setTextColor(ContextCompat.getColor(context, R.color.textLight))
						setHintTextColor(ContextCompat.getColor(context, R.color.textLight60))
						compoundDrawableTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.textLight))
					}

					dialogBinding.body.addView(input)

					dialogBinding.positiveButton.apply {
						text = getString(R.string.save)
						setOnClickListener {
							dialogBinding.body.children.forEach { view ->
								val input = view as EditText
								val text = input.text.toString().trim()

								if (text.isEmpty()) return@forEach

								binding.emailOption.setSubtitle(text)
								userTemp.contact_forms["email"] = text
							}

							dialog.dismiss()
						}
					}

					dialogBinding.negativeButton.setOnClickListener {
						dialog.dismiss()
					}

					dialog.setOnDismissListener {
						if (userTemp.contact_forms["email"]?.isBlank() ?: true) {
							button.isChecked = false
						}
					}
				}
			} else {
				binding.emailOption.setSubtitle("")
				userTemp.contact_forms.remove("email")
			}
		}
	}

	private fun configSmsSwitch() {
		binding.switchSms.setOnCheckedChangeListener { button, isChecked ->
			if (isChecked) {
				configBottomSheet { dialogBinding, dialog ->
					dialogBinding.icon.visibility = View.GONE
					dialogBinding.title.text = getString(R.string.register_contact_form)

					val input = EditText(context).apply {
						layoutParams = ViewGroup.LayoutParams(
							ViewGroup.LayoutParams.MATCH_PARENT,
							ViewGroup.LayoutParams.WRAP_CONTENT
						)

						if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
							setAutofillHints(View.AUTOFILL_HINT_PHONE)
						}

						inputType = InputType.TYPE_CLASS_PHONE
						background = ContextCompat.getDrawable(context, R.drawable.shape_input)
						setCompoundDrawablesRelativeWithIntrinsicBounds(
							ContextCompat.getDrawable(context, R.drawable.ic_sms),
							null,
							null,
							null
						)
						compoundDrawablePadding = 20
						hint = getString(R.string.type_contact_form)
						text = userTemp.contact_forms["sms"]?.toEditable()

						setTextColor(ContextCompat.getColor(context, R.color.textLight))
						setHintTextColor(ContextCompat.getColor(context, R.color.textLight60))
						compoundDrawableTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.textLight))
						addTextChangedListener(Utils.buildPhoneMask())
					}

					dialogBinding.body.addView(input)

					dialogBinding.positiveButton.apply {
						text = getString(R.string.save)
						setOnClickListener {
							dialogBinding.body.children.forEach { view ->
								val input = view as EditText
								val text = input.text.toString().trim()

								if (text.isEmpty()) return@forEach

								binding.smsOption.setSubtitle(text)
								userTemp.contact_forms["sms"] = text
							}

							dialog.dismiss()
						}
					}

					dialogBinding.negativeButton.setOnClickListener {
						dialog.dismiss()
					}

					dialog.setOnDismissListener {
						if (userTemp.contact_forms["sms"]?.isBlank() ?: true) {
							button.isChecked = false
						}
					}
				}
			} else {
				binding.smsOption.setSubtitle("")
				userTemp.contact_forms.remove("sms")
			}
		}
	}

	private fun configWhatsappSwitch() {
		binding.switchWhatsapp.setOnCheckedChangeListener { button, isChecked ->
			if (isChecked) {
				configBottomSheet { dialogBinding, dialog ->
					dialogBinding.icon.visibility = View.GONE
					dialogBinding.title.text = getString(R.string.register_contact_form)

					val input = EditText(context).apply {
						layoutParams = ViewGroup.LayoutParams(
							ViewGroup.LayoutParams.MATCH_PARENT,
							ViewGroup.LayoutParams.WRAP_CONTENT
						)

						if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
							setAutofillHints(View.AUTOFILL_HINT_PHONE)
						}

						inputType = InputType.TYPE_CLASS_PHONE
						background = ContextCompat.getDrawable(context, R.drawable.shape_input)
						setCompoundDrawablesRelativeWithIntrinsicBounds(
							ContextCompat.getDrawable(context, R.drawable.ic_whatsapp_app),
							null,
							null,
							null
						)
						compoundDrawablePadding = 20
						hint = getString(R.string.type_contact_form)
						text = userTemp.contact_forms["whatsapp"]?.toEditable()

						setTextColor(ContextCompat.getColor(context, R.color.textLight))
						setHintTextColor(ContextCompat.getColor(context, R.color.textLight60))
						compoundDrawableTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.textLight))
						addTextChangedListener(Utils.buildPhoneMask())
					}

					dialogBinding.body.addView(input)

					dialogBinding.positiveButton.apply {
						text = getString(R.string.save)
						setOnClickListener {
							dialogBinding.body.children.forEach { view ->
								val input = view as EditText
								val text = input.text.toString().trim()

								if (text.isEmpty()) return@forEach

								binding.whatsappOption.setSubtitle(text)
								userTemp.contact_forms["whatsapp"] = text
							}

							dialog.dismiss()
						}
					}

					dialogBinding.negativeButton.setOnClickListener {
						dialog.dismiss()
					}

					dialog.setOnDismissListener {
						if (userTemp.contact_forms["whatsapp"]?.isBlank() ?: true) {
							button.isChecked = false
						}
					}
				}
			} else {
				binding.whatsappOption.setSubtitle("")
				userTemp.contact_forms.remove("whatsapp")
			}
		}
	}

	private fun configTelegramSwitch() {
		binding.switchTelegram.setOnCheckedChangeListener { button, isChecked ->
			if (isChecked) {
				configBottomSheet { dialogBinding, dialog ->
					dialogBinding.icon.visibility = View.GONE
					dialogBinding.title.text = getString(R.string.register_contact_form)

					val input = EditText(context).apply {
						layoutParams = ViewGroup.LayoutParams(
							ViewGroup.LayoutParams.MATCH_PARENT,
							ViewGroup.LayoutParams.WRAP_CONTENT
						)

						if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
							setAutofillHints(View.AUTOFILL_HINT_PHONE)
						}

						inputType = InputType.TYPE_CLASS_PHONE
						background = ContextCompat.getDrawable(context, R.drawable.shape_input)
						setCompoundDrawablesRelativeWithIntrinsicBounds(
							ContextCompat.getDrawable(context, R.drawable.ic_telegram_app),
							null,
							null,
							null
						)
						compoundDrawablePadding = 20
						hint = getString(R.string.type_contact_form)
						text = userTemp.contact_forms["telegram"]?.toEditable()

						setTextColor(ContextCompat.getColor(context, R.color.textLight))
						setHintTextColor(ContextCompat.getColor(context, R.color.textLight60))
						compoundDrawableTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.textLight))
						addTextChangedListener(Utils.buildPhoneMask())
					}

					dialogBinding.body.addView(input)

					dialogBinding.positiveButton.apply {
						text = getString(R.string.save)
						setOnClickListener {
							dialogBinding.body.children.forEach { view ->
								val input = view as EditText
								val text = input.text.toString().trim()

								if (text.isEmpty()) return@forEach

								binding.telegramOption.setSubtitle(text)
								userTemp.contact_forms["telegram"] = text
							}

							dialog.dismiss()
						}
					}

					dialogBinding.negativeButton.setOnClickListener {
						dialog.dismiss()
					}

					dialog.setOnDismissListener {
						if (userTemp.contact_forms["telegram"]?.isBlank() ?: true) {
							button.isChecked = false
						}
					}
				}
			} else {
				binding.telegramOption.setSubtitle("")
				userTemp.contact_forms.remove("telegram")
			}
		}
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}
}