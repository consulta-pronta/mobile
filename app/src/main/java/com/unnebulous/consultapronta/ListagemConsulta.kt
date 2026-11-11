package com.unnebulous.consultapronta

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ListAdapter
import com.unnebulous.consultapronta.database.Consultation
import com.unnebulous.consultapronta.database.Consultation.Companion.ConsultationStatus
import com.unnebulous.consultapronta.database.DatabaseManager
import com.unnebulous.consultapronta.database.User
import com.unnebulous.consultapronta.databinding.FragmentListagemConsultaBinding
import com.unnebulous.consultapronta.recyclerview.adapter.ConsultationAdapter
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

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

		binding.recyclerviewProposed.adapter = setAdapter()
		binding.recyclerviewRequested.adapter = setAdapter()
		binding.recyclerviewScheduled.adapter = setAdapter()

		binding.requestConsultation.setOnClickListener {
			changeFragmentWithBackStack(SolicitarConsulta())
		}

		lifecycleScope.launch {
			val list = mutableListOf<Pair<Consultation, Map<String, String>>>()

			val consultationRef = Consultation
				.collection
				.get()
				.await()

			consultationRef.forEach { doc ->
				val professional = DatabaseManager
					.db
					.collection(User.COLLECTION_NAME)
					.document(doc["professional_id"].toString())
					.get()
					.await()

				list.add(
					Consultation.fromDocument(doc) to mapOf(
						"name" to professional["name"].toString(),
						"crm" to "CRM ${professional.get("professional_data.crm").toString()}/${professional.get("professional_data.uf").toString()}",
						"id" to professional.id
					)
				)

				@Suppress("UNCHECKED_CAST")
				binding.apply {
					(recyclerviewProposed.adapter as ListAdapter<Pair<Consultation, Map<String, String>>, ConsultationAdapter.ConsultationViewHolder>).submitList(list.filter {
						it.first.status == ConsultationStatus.SOLICITADO_PROFISSIONAL
					})
					(recyclerviewScheduled.adapter as ListAdapter<Pair<Consultation, Map<String, String>>, ConsultationAdapter.ConsultationViewHolder>).submitList(list.filter {
						it.first.status == ConsultationStatus.MARCADA
					})
					(recyclerviewRequested.adapter as ListAdapter<Pair<Consultation, Map<String, String>>, ConsultationAdapter.ConsultationViewHolder>).submitList(list.filter {
						it.first.status == ConsultationStatus.SOLICITADO_PACIENTE
					})
				}
			}
		}
	}

	private fun setAdapter() = ConsultationAdapter().apply {
		acceptRequest = { consultationId ->
			lifecycleScope.launch {
				val consultationRef = Consultation
					.collection
					.document(consultationId)

				val data = consultationRef.get().await()

				val consultationUpdated = Consultation.Companion.FormData(
					professional_id = data["professional_id"].toString(),
					patient_id = data["patient_id"].toString(),
					motivation = data["motivation"].toString(),
					details = data["details"].toString(),
					date_time = data.getTimestamp("date_time"),
					hospital = data["hospital"].toString(),
					status = ConsultationStatus.MARCADA
				)

				consultationRef.set(consultationUpdated.toMap())
			}
		}

		denyRequest = { consultationId ->
			lifecycleScope.launch {
				val consultationRef = Consultation
					.collection
					.document(consultationId)

				val data = consultationRef.get().await()

				val consultationUpdated = Consultation.Companion.FormData(
					professional_id = data["professional_id"].toString(),
					patient_id = data["patient_id"].toString(),
					motivation = data["motivation"].toString(),
					details = data["details"].toString(),
					date_time = data.getTimestamp("date_time"),
					hospital = data["hospital"].toString(),
					status = ConsultationStatus.NEGADA
				)

				consultationRef.set(consultationUpdated.toMap())
			}
		}

		onCardClick = { userId ->
			changeFragmentWithBackStack(ViewExternalProfile.newInstance(userId))
		}
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding
	}
}