package com.unnebulous.consultapronta

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.ListenerRegistration
import com.unnebulous.consultapronta.database.Symptom
import com.unnebulous.consultapronta.databinding.FragmentHistoricoSintomaBinding
import com.unnebulous.consultapronta.recyclerview.adapter.SymptomAdapter

class HistoricoSintoma : Fragment() {
	private var _binding: FragmentHistoricoSintomaBinding? = null
	private val binding get() = _binding!!

	private lateinit var symptomListener: ListenerRegistration

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = FragmentHistoricoSintomaBinding.inflate(layoutInflater, container, false)

		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		updateHeader {
			changeHeaderType(Utils.HeaderType.COMPACT)
		}

		val adapter = SymptomAdapter().apply {
			onClick = { symptom ->
				changeFragmentWithBackStack(
					HistoricoDoSintoma.newInstance(
						symptom.id
					))
			}
		}

		binding.historicoSintomaRecyclerview.adapter = adapter

		symptomListener = Symptom
			.collection
			.addSnapshotListener { snapshots, exception ->
				if (exception != null) {
					Log.e("firestore:getSymptoms", "Error getting documents: ", exception)
					return@addSnapshotListener
				}

				if (snapshots != null) {
					try {
						val symptoms = snapshots.documents.mapNotNull {
							Symptom.fromDocument(it)
						}

						adapter.submitList(symptoms)
					} catch (error: Exception) {
						Log.wtf("symptom", "getSymptoms:failure", error)
					}
				}
			}
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
		symptomListener?.remove()
	}

}