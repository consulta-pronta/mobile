package com.unnebulous.consultapronta

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.firestore
import com.unnebulous.consultapronta.database.Symptom
import com.unnebulous.consultapronta.databinding.FragmentHistoricoSintomaBinding
import com.unnebulous.consultapronta.recyclerview.adapter.SymptomAdapter

class HistoricoSintoma : Fragment() {
	private var _binding: FragmentHistoricoSintomaBinding? = null
	private val binding get() = _binding!!

	private lateinit var auth: FirebaseAuth
	private lateinit var db: FirebaseFirestore
	private lateinit var firestoreListener: ListenerRegistration

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = FragmentHistoricoSintomaBinding.inflate(layoutInflater, container, false)

		auth = Firebase.auth
		db = Firebase.firestore

		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		val adapter = SymptomAdapter().apply {
			onClick = { symptom ->
				// TODO: abrir página de histórico do sintoma
			}
		}

		binding.historicoSintomaRecyclerview.adapter = adapter

		firestoreListener = db.collection("users")
			.document(auth.uid!!)
			.collection("symptom")
			.addSnapshotListener { snapshots, exception ->
				if (exception != null) {
					Log.e("firestore:getSymptoms", "Error getting documents: ", exception)
					return@addSnapshotListener
				}

				if (snapshots != null) {
					val symptoms = snapshots.documents.mapNotNull { document ->
						val data = document.data!!
						Symptom(
							id = document.id,
							title = data["title"] as String,
							description = data["description"] as String,
							dateTime = data["date_time"] as Timestamp,
							place = data["place"] as String,
							intensity = (data["intensity"] as Long).toInt(),
							createdAt = data["created_at"] as Timestamp,
						)
					}

					adapter.submitList(symptoms)
				}
			}
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
		firestoreListener?.remove()
	}

}