package com.unnebulous.consultapronta

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
				Toast.makeText(context, getString(R.string.succesfully_cache_deleted), Toast.LENGTH_SHORT).show()
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
				// TODO: INSTANCIAR TELA DE RELATÓRIOS
				Home()
			},
			MenuOption(R.drawable.ic_exams, R.string.my_exams_text) {
				// TODO: INSTANCIAR TELA DE MEUS EXAMES
				Home()
			},
			MenuOption(R.drawable.ic_pill, R.string.my_medicines_text) {
				// TODO: INSTANCIAR TELA DE MEUS MEDICAMENTOS
				Home()
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