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
import com.unnebulous.consultapronta.recyclerview.SpacingItemDecoration
import com.unnebulous.consultapronta.recyclerview.adapter.ConsultationAdapter
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.unnebulous.consultapronta.database.AuthManager

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

		binding.apply {
			requestConsultation.setOnClickListener {
				changeFragmentWithBackStack(SolicitarConsulta())
			}
		}

		// Verifica a autenticação antes de acessar o Firestore.
		val uid = AuthManager.uid

		if (uid == null) {
			Log.e("AUTH_DEBUG", getString(R.string.non_authenticated_on_opening))
			return
		}

		binding.apply {
			recyclerviewProposed.apply {
				adapter = setAdapter(uid)
				addItemDecoration(recyclerviewSpacing)
			}

			recyclerviewRequested.apply {
				adapter = setAdapter(uid)
				addItemDecoration(recyclerviewSpacing)
			}

			recyclerviewScheduled.apply {
				adapter = setAdapter(uid)
				addItemDecoration(recyclerviewSpacing)
			}
		}

		viewLifecycleOwner.lifecycleScope.launch {
			try {
				val list = mutableListOf<Pair<Consultation, Map<String, String>>>()

				val consultationRef = DatabaseManager
					.userCollection(Consultation.COLLECTION_NAME, uid)
					.get()
					.await()

				consultationRef.forEach { doc ->

					val professionalId = doc.getString("professional_id")
						?: return@forEach

					val professional = DatabaseManager.db
						.collection(User.COLLECTION_NAME)
						.document(professionalId)
						.get()
						.await()

					list.add(
						Consultation.fromDocument(doc) to mapOf(
							"name" to professional.getString("name").orEmpty(),
							"crm" to "CRM ${
								professional.get("professional_data.crm").toString()
							}/${
								professional.get("professional_data.uf").toString()
							}",
							"id" to professional.id
						)
					)
				}

				binding.apply {

					@Suppress("UNCHECKED_CAST")
					(recyclerviewProposed.adapter as ListAdapter<
						Pair<Consultation, Map<String, String>>,
						ConsultationAdapter.ConsultationViewHolder
						>).submitList(
						list.filter {
							it.first.status == ConsultationStatus.SOLICITADO_PROFISSIONAL
						}
					)

					@Suppress("UNCHECKED_CAST")
					(recyclerviewScheduled.adapter as ListAdapter<
						Pair<Consultation, Map<String, String>>,
						ConsultationAdapter.ConsultationViewHolder
						>).submitList(
						list.filter {
							it.first.status == ConsultationStatus.MARCADA
						}
					)

					@Suppress("UNCHECKED_CAST")
					(recyclerviewRequested.adapter as ListAdapter<
						Pair<Consultation, Map<String, String>>,
						ConsultationAdapter.ConsultationViewHolder
						>).submitList(
						list.filter {
							it.first.status == ConsultationStatus.SOLICITADO_PACIENTE
						}
					)
				}

			} catch (e: Exception) {
				Log.e("CONSULTATION_DEBUG", getString(R.string.failed_to_load_consultations), e)
			}
		}
	}

	private fun setAdapter(uid: String) = ConsultationAdapter().apply {

		val consultationCollection = DatabaseManager
			.userCollection(Consultation.COLLECTION_NAME, uid)

		acceptRequest = { consultationId ->

			viewLifecycleOwner.lifecycleScope.launch {
				try {
					val consultationRef = consultationCollection
						.document(consultationId)

					val data = consultationRef.get().await()

					val consultationUpdated = Consultation.Companion.FormData(
						professional_id = data["professional_id"].toString(),
						patient_id = data["patient_id"].toString(),
						motivation = data["motivation"].toString(),
						details = data["details"]?.toString(),
						date_time = data.getTimestamp("date_time"),
						hospital = data["hospital"].toString(),
						status = ConsultationStatus.MARCADA
					)

					consultationRef.set(consultationUpdated.toMap()).await()

				} catch (e: Exception) {
					Log.e("CONSULTATION_DEBUG", getString(R.string.failed_to_accept), e)
				}
			}
		}

		denyRequest = { consultationId ->

			viewLifecycleOwner.lifecycleScope.launch {
				try {
					val consultationRef = consultationCollection
						.document(consultationId)

					val data = consultationRef.get().await()

					val consultationUpdated = Consultation.Companion.FormData(
						professional_id = data["professional_id"].toString(),
						patient_id = data["patient_id"].toString(),
						motivation = data["motivation"].toString(),
						details = data["details"]?.toString(),
						date_time = data.getTimestamp("date_time"),
						hospital = data["hospital"].toString(),
						status = ConsultationStatus.NEGADA
					)

					consultationRef.set(consultationUpdated.toMap()).await()

				} catch (e: Exception) {
					Log.e("CONSULTATION_DEBUG", getString(R.string.failed_to_deny), e)
				}
			}
		}

		onCardClick = { userId ->
			changeFragmentWithBackStack(
				ViewExternalProfile.newInstance(userId)
			)
		}
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}
}