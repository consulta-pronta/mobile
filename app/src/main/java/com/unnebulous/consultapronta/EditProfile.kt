package com.unnebulous.consultapronta

import android.content.res.ColorStateList
import android.os.Bundle
import android.text.InputType
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.core.view.children
import com.unnebulous.consultapronta.database.AuthManager
import com.unnebulous.consultapronta.database.User
import com.unnebulous.consultapronta.databinding.FragmentEditProfileBinding
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.sequences.forEach
import kotlin.text.isBlank

class EditProfile : Fragment() {
	private var _binding: FragmentEditProfileBinding? = null
	private val binding get() = _binding!!

	private val updatedContactForms = mutableMapOf<String, String>()

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = FragmentEditProfileBinding.inflate(
			inflater,
			container,
			false
		)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		updateHeader {
			changeHeaderType(Utils.HeaderType.TITLED, true)
			setScreenTitle(getString(R.string.edit_profile))
			setGoBackButtonOnClickListener {
				popBackStack()
			}
		}
		resetNavbarEntryActive()

		try {
			val userData = AuthManager.userInFirestore

			binding.apply {
				setupSelect(genderIdentityInput, resources.getStringArray(R.array.genderIdentityArray))

				userData.apply {
					val birthDate = LocalDate.parse(userData.birth_date)

					userName.text = name
					getContactForms(contact_forms)

					birthDateSelect.text = birthDate.format(DateTimeFormatter.ofPattern(getString(R.string.DATE_FORMAT)))
					genderIdentityInput.text = gender.toEditable()
					locationInput.text = address.toEditable()
				}
			}
		} catch (e: Exception) {
			Log.wtf(User.COLLECTION_NAME, getString(R.string.getuserdata_failure), e)
			showSnackbar(getString(R.string.error_generic), Utils.SnackBarType.DANGER)
		}
	}

	private fun getContactForms(contactForms: Map<String, String>) {
		val email = contactForms["email"]
		val sms = contactForms["sms"]
		val whatsapp = contactForms["whatsapp"]
		val telegram = contactForms["telegram"]

		binding.apply {
			email?.let {
				emailOption.apply {
					setSubtitle(email)
				}
				switchEmail.isChecked = true
			}

			sms?.let {
				smsOption.apply {
					setSubtitle(Utils.applyMask(Utils.PHONE_MASK, sms))
				}
				switchSms.isChecked = true
			}

			whatsapp?.let {
				whatsappOption.apply {
					setSubtitle(Utils.applyMask(Utils.PHONE_MASK, whatsapp))
				}
				switchWhatsapp.isChecked = true
			}

			telegram?.let {
				telegramOption.apply {
					checked = true
					setSubtitle(Utils.applyMask(Utils.PHONE_MASK, telegram))
				}
				switchTelegram.isChecked = true
			}
		}

		configEmailSwitch(email)
		configSmsSwitch(sms)
		configWhatsappSwitch(whatsapp)
		configTelegramSwitch(telegram)
	}

	private fun configEmailSwitch(email: String?) {
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
						text = email?.toEditable()

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
								updatedContactForms["email"] = text
							}

							dialog.dismiss()
						}
					}

					dialogBinding.negativeButton.setOnClickListener {
						dialog.dismiss()
					}

					dialog.setOnDismissListener {
						if (updatedContactForms["email"]?.isBlank() ?: true) {
							button.isChecked = false
						}
					}
				}
			} else {
				binding.emailOption.setSubtitle("")
				updatedContactForms.remove("email")
			}
		}
	}

	private fun configSmsSwitch(sms: String?) {
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
						text = sms?.toEditable()

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
								updatedContactForms["sms"] = text
							}

							dialog.dismiss()
						}
					}

					dialogBinding.negativeButton.setOnClickListener {
						dialog.dismiss()
					}

					dialog.setOnDismissListener {
						if (updatedContactForms["sms"]?.isBlank() ?: true) {
							button.isChecked = false
						}
					}
				}
			} else {
				binding.smsOption.setSubtitle("")
				updatedContactForms.remove("sms")
			}
		}
	}

	private fun configWhatsappSwitch(whatsapp: String?) {
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
						text = whatsapp?.toEditable()

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
								updatedContactForms["whatsapp"] = text
							}

							dialog.dismiss()
						}
					}

					dialogBinding.negativeButton.setOnClickListener {
						dialog.dismiss()
					}

					dialog.setOnDismissListener {
						if (updatedContactForms["whatsapp"]?.isBlank() ?: true) {
							button.isChecked = false
						}
					}
				}
			} else {
				binding.whatsappOption.setSubtitle("")
				updatedContactForms.remove("whatsapp")
			}
		}
	}

	private fun configTelegramSwitch(telegram: String?) {
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
						text = telegram?.toEditable()

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
								updatedContactForms["telegram"] = text
							}

							dialog.dismiss()
						}
					}

					dialogBinding.negativeButton.setOnClickListener {
						dialog.dismiss()
					}

					dialog.setOnDismissListener {
						if (updatedContactForms["telegram"]?.isBlank() ?: true) {
							button.isChecked = false
						}
					}
				}
			} else {
				binding.telegramOption.setSubtitle("")
				updatedContactForms.remove("telegram")
			}
		}
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}
}