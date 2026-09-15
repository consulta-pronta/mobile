package com.unnebulous.consultapronta

import android.os.Bundle
import android.content.res.ColorStateList
import android.text.InputType
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.autofill.AutofillValue
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.core.view.children
import com.unnebulous.consultapronta.databinding.FragmentCadastroFormaContatoBinding

class CadastroFormaContato : Fragment() {

	private var _binding: FragmentCadastroFormaContatoBinding? = null
	private val binding get() = _binding!!

	private var contactEmail: String = ""
	private var contactSMS: String = ""
	private var contactWhatsApp: String = ""
	private var contactTelegram: String = ""

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

		binding.verifyButton.setOnClickListener {
			binding.contentBody.visibility = View.GONE
			binding.contentBodyVerification.visibility = View.VISIBLE
		}

		binding.verifyCodeButton.setOnClickListener {

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
								contactEmail = text
							}

							dialog.dismiss()
						}
					}

					dialogBinding.negativeButton.setOnClickListener {
						dialog.dismiss()
					}

					dialog.setOnDismissListener {
						if (contactEmail.isBlank()) {
							button.isChecked = false
						}
					}
				}
			} else {
				binding.emailOption.setSubtitle("")
				contactEmail = ""
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
								contactSMS = text
							}

							dialog.dismiss()
						}
					}

					dialogBinding.negativeButton.setOnClickListener {
						dialog.dismiss()
					}

					dialog.setOnDismissListener {
						if (contactSMS.isBlank()) {
							button.isChecked = false
						}
					}
				}
			} else {
				binding.smsOption.setSubtitle("")
				contactSMS = ""
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
								contactWhatsApp = text
							}

							dialog.dismiss()
						}
					}

					dialogBinding.negativeButton.setOnClickListener {
						dialog.dismiss()
					}

					dialog.setOnDismissListener {
						if (contactWhatsApp.isBlank()) {
							button.isChecked = false
						}
					}
				}
			} else {
				binding.whatsappOption.setSubtitle("")
				contactWhatsApp = ""
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
								contactTelegram = text
							}

							dialog.dismiss()
						}
					}

					dialogBinding.negativeButton.setOnClickListener {
						dialog.dismiss()
					}

					dialog.setOnDismissListener {
						if (contactTelegram.isBlank()) {
							button.isChecked = false
						}
					}
				}
			} else {
				binding.telegramOption.setSubtitle("")
				contactTelegram = ""
			}
		}
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}
}