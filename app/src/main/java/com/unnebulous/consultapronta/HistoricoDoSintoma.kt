package com.unnebulous.consultapronta

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.unnebulous.consultapronta.database.Symptom
import com.unnebulous.consultapronta.databinding.FragmentHistoricoDoSintomaBinding
import com.unnebulous.consultapronta.recyclerview.adapter.SymptomHistoryAdapter
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class HistoricoDoSintoma : Fragment() {

	private lateinit var symptomId: String

	private var _binding: FragmentHistoricoDoSintomaBinding? = null
	private val binding get() = _binding!!

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		arguments?.let {
			symptomId = it.getString(ARG_SYMPTOM_ID, "ERROR")
		}
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = FragmentHistoricoDoSintomaBinding.inflate(layoutInflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		updateHeader {
			changeHeaderType(Utils.HeaderType.TITLED, true)
			setScreenTitle(getString(R.string.symptom_history_title))
			setGoBackButtonOnClickListener {
				popBackStack()
			}
		}

		val adapter = SymptomHistoryAdapter()

		binding.apply {
			recyclerview.adapter = adapter
			editSymptom.setOnClickListener {
				changeFragmentWithBackStack(EditSymptom.newInstance(symptomId))
			}
		}

		lifecycleScope.launch {
			try {
				val topLevelSymptom = Symptom.collection.document(symptomId).get().await()
				val list = listOf(Symptom.fromDocument(topLevelSymptom))
					.mergedHistoric()
					.sortedByDescending { it.date_time }

				adapter.submitList(list)

				Log.i(Symptom.HISTORIC_COLLECTION_NAME, "getHistoric:success")
			} catch (e: Exception) {
				Log.e(Symptom.HISTORIC_COLLECTION_NAME, "getHistoric:failure", e)
			}
		}
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	companion object {
		private const val ARG_SYMPTOM_ID = "symptom_id"

		@JvmStatic
		fun newInstance(id: String) =
			HistoricoDoSintoma().apply {
				arguments = Bundle().apply {
					putString(ARG_SYMPTOM_ID, id)
				}
			}
	}
}