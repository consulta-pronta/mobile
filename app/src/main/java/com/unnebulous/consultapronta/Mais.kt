package com.unnebulous.consultapronta

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.unnebulous.consultapronta.database.AuthManager
import com.unnebulous.consultapronta.databinding.FragmentMaisBinding
import com.unnebulous.consultapronta.views.OptionItemView

class Mais : Fragment() {
	
	private var _binding: FragmentMaisBinding? = null
	private val binding get() = _binding!!

	private data class MenuOption(
		val drawableResId: Int,
		val textResId: Int,
		val createFragment: () -> Fragment
	)

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = FragmentMaisBinding.inflate(layoutInflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		updateHeader {
			changeHeaderType(Utils.HeaderType.COMPACT)
		}

		// TODO: deve fazer verificação do tipo de usuário
		setPatientOptions()

		binding.apply {
			cleanCacheButton.setOnClickListener {
				requireContext().clearCache()
				showSnackbar(getString(R.string.succesfully_cache_deleted), Utils.SnackBarType.SUCCESS,
					Snackbar.LENGTH_SHORT)
			}

			exitAccountButton.setOnClickListener {
				val parentActivitiy = requireActivity()

				configBottomSheet { dialogBinding, dialog ->
					dialogBinding.apply {
						icon.setImageResource(R.drawable.ic_logout)
						icon.visibility = View.GONE
						title.text = getString(R.string.exit_account)
						body.apply {
							val subtitle = TextView(parentActivitiy).apply {
								text = getString(R.string.exit_account_subtitle)
							}
							val description = TextView(parentActivitiy).apply {
								text = getString(R.string.exit_account_description)
								textSize = 14f
							}

							listOf(subtitle, description).forEach {
								it.textAlignment = View.TEXT_ALIGNMENT_CENTER
								it.setTextColor(ContextCompat.getColor(context, R.color.textDark))
							}

							addView(subtitle)
							addView(description)
						}

						positiveButton.text = title.text
						positiveButton.setOnClickListener {
							AuthManager.auth.signOut()

							parentActivitiy.startActivity(Intent(
								parentActivitiy,
								AuthActivity::class.java)
							)
							parentActivitiy.finish()
						}
					}
				}
			}
		}
	}

	// TODO: incompleto
	private fun setPatientOptions() {
		// AVISO: DEVE ESTAR NA ORDEM QUE APARECE NO FIGMA
		val options = listOf(
			// opção de informações de saúde
			// opção de permissões médicas
			MenuOption(R.drawable.ic_reports, R.string.reports_text) {
				RelatoriosListagem()
			},
			MenuOption(R.drawable.ic_exams, R.string.my_exams_text) {
				Exam()
			},
			MenuOption(R.drawable.ic_pill, R.string.my_medicines_text) {
				MedicamentosListagem()
			},
			MenuOption(R.drawable.ic_appointment, R.string.appointments_text) {
				// TODO: INSTANCIAR TELA DE CONSULTAS
				Home()
			},
		)

		for (option in options) {
			val option = OptionItemView(requireContext()).apply {
				setIcon(option.drawableResId)
				setText(option.textResId)

				setOnClickListener {
					changeFragmentWithBackStack(option.createFragment())
				}
			}

			binding.userConfigList.addView(option)
		}
	}

	// TODO: incompleto
	private fun setProfessionalOptions() {
		// AVISO: DEVE ESTAR NA ORDEM QUE APARECE NO FIGMA
		val options = listOf(
			// opção de recursos
			// opção de exames
			MenuOption(R.drawable.ic_appointment, R.string.appointments_text) {
				// TODO: INSTANCIAR TELA DE CONSULTAS
				Home()
			},
			MenuOption(R.drawable.ic_reports, R.string.reports_text) {
				// TODO: INSTANCIAR TELA DE RELATÓRIOS
				Home()
			}
		)

		for (option in options) {
			val option = OptionItemView(requireContext()).apply {
				setIcon(option.drawableResId)
				setText(option.textResId)

				setOnClickListener {
					changeFragmentWithBackStack(option.createFragment())
				}
			}

			binding.userConfigList.addView(option)
		}
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}
}