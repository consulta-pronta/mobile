package com.unnebulous.consultapronta

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.unnebulous.consultapronta.databinding.FragmentListagemConsultaBinding
import com.unnebulous.consultapronta.recyclerview.adapter.ConsultationAdapter

class ListagemConsulta : Fragment() {
	private var _binding: FragmentListagemConsultaBinding? = null
	private val binding get() = _binding!!

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = FragmentListagemConsultaBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		updateHeader {
			changeHeaderType(Utils.HeaderType.TITLED, true)
			setScreenTitle(getString(R.string.my_consultations))
			setGoBackButtonOnClickListener {
				popBackStack()
			}
		}
		resetNavbarEntryActive()

		val adapter = ConsultationAdapter().apply {
			getProfessionalInfo = { consultationId ->
				/* todo
				* pega a consulta no Firestore
				* de lá pega o id do profissional
				* aí sim pega o nome e crm do profissional
				* */

				mapOf(
					"name" to "Firestore",
					"crm" to "CRM CRM/UF"
				)
			}

			acceptRequest = { consultationId ->
				// todo: ação para aceitar a proposta do profissional
			}

			denyRequest = { consultationId ->
				// todo: ação para negar a proposta do profissional
			}

			// todo: adiciona um campo na classe database.Consultation para diferenciar consultas marcadas, solicitadas e não aprovadas
		}

		binding.recyclerviewProposed.adapter = adapter
		binding.recyclerviewRequested.adapter = adapter
		binding.recyclerviewScheduled.adapter = adapter

		binding.requestConsultation.setOnClickListener {
			changeFragmentWithBackStack(SolicitarConsulta())
		}
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding
	}
}