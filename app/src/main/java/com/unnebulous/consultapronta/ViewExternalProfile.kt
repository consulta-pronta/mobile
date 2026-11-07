package com.unnebulous.consultapronta

import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.transition.TransitionManager
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.unnebulous.consultapronta.database.DatabaseManager
import com.unnebulous.consultapronta.database.User
import com.unnebulous.consultapronta.databinding.FragmentViewExternalProfileBinding
import com.unnebulous.consultapronta.views.OptionItemView
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ViewExternalProfile : Fragment() {

	private var _binding: FragmentViewExternalProfileBinding? = null
	private val binding get() = _binding!!

	private var isExternalDataMinimized = false
	private lateinit var userId: String

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		arguments?.let {
			userId = it.getString(ARG_USER_ID, "ERROR")
		}
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = FragmentViewExternalProfileBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		updateHeader {
			changeHeaderType(Utils.HeaderType.TITLED)
			setGoBackButtonOnClickListener {
				popBackStack()
			}
		}
		resetNavbarEntryActive()

		configUserType(Utils.UserType.PROFISSIONAL)

		lifecycleScope.launch {
			try {
				val user = DatabaseManager
					.db
					.collection(User.COLLECTION_NAME)
					.document(userId)
					.get()
					.await()

				val userType = user.getEnum<Utils.UserType>("user_type")
					?: throw FirebaseAuthInvalidCredentialsException("Invalid user", "User not have an valid user_type value")

				configUserType(userType)

				// TODO: formas de contato: opção de ligar, SMS e mandar email e listagem das formas
				// TODO: lógica para a tela de prontuário, caso tenha
				if (userType == Utils.UserType.PROFISSIONAL) {
					binding.apply {
						userName.text = user["name"].toString()
						userEmail.setText(user["email"].toString())
						userPhoneNumber.setText(user["phone"].toString())

						gotoUserSpecificScreenButton.setOnClickListener {
							changeFragmentWithBackStack(ListagemConsulta())
						}
					}
					addExternalData(getString(R.string.crm_profile), "CRM ${user["professional_data.crm"].toString()}/${user["professional_data.uf"].toString()}")
					addExternalData(getString(R.string.operation_area_profile), user["professional_data.operation_area"].toString())
					addExternalData(getString(R.string.speciality_profile), user["professional_data.speciality"].toString())
				} else {
					showSnackbar(getString(R.string.easter_fetching_patient))
					popBackStack()
				}
			} catch (e: Exception) {
				Log.wtf("ErroPronto", "getExternalUser:failure", e)
				showSnackbar(getString(R.string.error_not_fetching_external_user), Utils.SnackBarType.DANGER)
			}
		}

		binding.minimizeExternalUserDataLayoutButton.setOnClickListener {
			minimizeExternalUserDataLayout()
		}
	}

	private fun addContactForm(drawableResId: Int, name: String, onClickListener: View.OnClickListener) {
		val option = OptionItemView(requireContext()).apply {
			setText(name)
			setIcon(drawableResId)

			setOnClickListener(onClickListener)
		}

		binding.contactInfoLayout.addView(option)
	}

	private fun addExternalData(key: String, value: String) {
		val row = LinearLayout(context).apply {
			layoutParams = LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT
			)
			orientation = LinearLayout.HORIZONTAL
		}

		val keyTextView = TextView(context).apply {
			layoutParams = LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT
			)
			text = key
			setTextColor(ContextCompat.getColor(context, R.color.textDark))
			setTypeface(null, Typeface.BOLD)
		}

		val valueTextView = TextView(context).apply {
			val params = LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT
			)
			params.marginStart = resources.getDimensionPixelSize(R.dimen.list_items_spacing)
			layoutParams = params

			text = value
			setTextColor(ContextCompat.getColor(context, R.color.textDark))
		}

		row.addView(keyTextView)
		row.addView(valueTextView)
		binding.externalUserDataCard.addView(row)
	}

	private fun addExternalData(custom: () -> MutableList<View>) {
		val views = custom()

		for (view: View in views) {
			binding.externalUserDataCard.addView(view)
		}
	}

	private fun minimizeExternalUserDataLayout() {
		isExternalDataMinimized = !isExternalDataMinimized

		TransitionManager.beginDelayedTransition(binding.root as ViewGroup)

		binding.externalUserDataCard.visibility = if (isExternalDataMinimized) View.GONE else View.VISIBLE

		binding.minimizeExternalUserDataLayoutButton.animate()
			.rotation(if (isExternalDataMinimized) 270f else 90f)
			.setDuration(250)
			.start()
	}

	private fun configUserType(userType: Utils.UserType) {
		var title: String
		var specificScreenTitle: String

		when (userType) {
			Utils.UserType.PACIENTE -> {
				title = getString(R.string.health_information)
				specificScreenTitle = getString(R.string.reports_text)
			}
			Utils.UserType.PROFISSIONAL -> {
				title = getString(R.string.professional_data)
				specificScreenTitle = getString(R.string.appointments_text)
			}
		}

		binding.externalUserDataTitle.text = title
		binding.gotoUserSpecificScreenButton.text = specificScreenTitle
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	companion object {
		private const val ARG_USER_ID = "user_id"

		@JvmStatic
		fun newInstance(userId: String) =
			ViewExternalProfile().apply {
				arguments = Bundle().apply {
					putString(ARG_USER_ID, userId)
				}
			}
	}
}