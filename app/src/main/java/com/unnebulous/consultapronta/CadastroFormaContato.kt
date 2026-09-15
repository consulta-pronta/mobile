package com.unnebulous.consultapronta

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
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.unnebulous.consultapronta.database.User
import com.unnebulous.consultapronta.databinding.FragmentCadastroFormaContatoBinding
import com.unnebulous.consultapronta.views.SelectOptionItemView
import java.util.Date

class CadastroFormaContato : Fragment() {

	private var _binding: FragmentCadastroFormaContatoBinding? = null
	private val binding get() = _binding!!

	private lateinit var userTemp: UserTemp

	private var numberOfcontactFormsValidated = 0

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

		userTemp.contactForms.entries.forEach { entry ->
			when (entry.key) {
				"email" -> {
					binding.emailOption.setSubtitle(entry.value)
					binding.switchEmail.isChecked = true
				}

				"sms" -> {
					binding.smsOption.setSubtitle(entry.value)
					binding.switchSms.isChecked = true
				}
			}
		}

		binding.verifyButton.setOnClickListener {
			binding.contentBody.visibility = View.GONE
			binding.contentBodyVerification.visibility = View.VISIBLE
		}

		binding.verifyCodeButton.setOnClickListener {
			var isValid = false

			verify()

			if (numberOfcontactFormsValidated == userTemp.contactForms.size && isValid) {
				auth.createUserWithEmailAndPassword(userTemp.email, userTemp.password)
					.addOnCompleteListener { task -> handlePostSignUp(task) }
			} else if(isValid) {
				setupNextVerification("TODO: PEGAR O NOME DA FORMA DE CONTATO")
			} else {
				// TODO: deu ruim
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
		val creationTime = user.metadata?.creationTimestamp

		val userDocument = User(
			name = userTemp.name,
			email = user.email!!,
			phone = userTemp.phoneNumber,
			cpf = userTemp.cpf,
			user_type = userTemp.userType.toString().lowercase(),
			created_at = Timestamp(Date(creationTime!!))
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

	private fun verify() {
		val codeTyped = binding.verificationCode.getCode()

		// TODO: lógica de verificação...
	}

	private fun setupNextVerification(contactFormName: String) {
		binding.codeTitlesLayout.children.forEach { title ->
			title.alpha = 0.5f
		}

		numberOfcontactFormsValidated++

		val newTitle = TextView(context).apply {
			layoutParams = binding.codeTitle.layoutParams
			textSize = binding.codeTitle.textSize

			text = "$numberOfcontactFormsValidated. " + getString(R.string.type_code_send) + contactFormName

			setTextColor(binding.codeTitle.textColors)
		}

		binding.codeTitlesLayout.addView(newTitle)
		binding.verificationCode.clear()
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
								val text = input.text.toString()

								binding.emailOption.setSubtitle(text)
								userTemp.contactForms["email"] = text
							}

							dialog.dismiss()
						}
					}

					dialogBinding.negativeButton.setOnClickListener {
						dialog.dismiss()
					}

					dialog.setOnDismissListener {
						if (userTemp.contactForms["email"]?.isBlank() ?: true) {
							button.isChecked = false
						}
					}
				}
			} else {
				binding.emailOption.setSubtitle("")
				userTemp.contactForms["email"] = ""
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
								val text = input.text.toString()

								binding.smsOption.setSubtitle(text)
								userTemp.contactForms["sms"] = text
							}

							dialog.dismiss()
						}
					}

					dialogBinding.negativeButton.setOnClickListener {
						dialog.dismiss()
					}

					dialog.setOnDismissListener {
						if (userTemp.contactForms["sms"]?.isBlank() ?: true) {
							button.isChecked = false
						}
					}
				}
			} else {
				binding.smsOption.setSubtitle("")
				userTemp.contactForms["sms"] = ""
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
								val text = input.text.toString()

								binding.whatsappOption.setSubtitle(text)
								userTemp.contactForms["whatsapp"] = text
							}

							dialog.dismiss()
						}
					}

					dialogBinding.negativeButton.setOnClickListener {
						dialog.dismiss()
					}

					dialog.setOnDismissListener {
						if (userTemp.contactForms["whatsapp"]?.isBlank() ?: true) {
							button.isChecked = false
						}
					}
				}
			} else {
				binding.whatsappOption.setSubtitle("")
				userTemp.contactForms["whatsapp"] = ""
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
								val text = input.text.toString()

								binding.telegramOption.setSubtitle(text)
								userTemp.contactForms["telegram"] = text
							}

							dialog.dismiss()
						}
					}

					dialogBinding.negativeButton.setOnClickListener {
						dialog.dismiss()
					}

					dialog.setOnDismissListener {
						if (userTemp.contactForms["telegram"]?.isBlank() ?: true) {
							button.isChecked = false
						}
					}
				}
			} else {
				binding.telegramOption.setSubtitle("")
				userTemp.contactForms["telegram"] = ""
			}
		}
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}
}