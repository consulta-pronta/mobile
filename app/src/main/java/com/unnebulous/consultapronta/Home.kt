package com.unnebulous.consultapronta

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.unnebulous.consultapronta.database.Symptom
import com.unnebulous.consultapronta.database.Symptom.Companion.fromDocument
import com.unnebulous.consultapronta.databinding.FragmentHomeBinding
import com.unnebulous.consultapronta.recyclerview.adapter.SymptomAdapter
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

const val NUMBER_OF_SYMPTOMS = 5

class Home : Fragment() {
	private var _binding: FragmentHomeBinding? = null
	private val binding get() = _binding!!

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = FragmentHomeBinding.inflate(layoutInflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		updateHeader {
			changeHeaderType(Utils.HeaderType.COMPACT)
		}

		val adapter = SymptomAdapter().apply {
			onClick = { symptom ->
				changeFragmentWithBackStack(HistoricoDoSintoma.newInstance(symptom.id))
			}
		}

		binding.apply {
			firstButton.setOnClickListener {
				changeFragmentWithBackStack(RelatoriosListagem())
			}
			secondButton.setOnClickListener {
				changeFragmentWithBackStack(Exam())
			}
			thirdButton.setOnClickListener {
				changeFragmentWithBackStack(MedicamentosListagem())
			}
			fourthButton.setOnClickListener {
				changeFragmentWithBackStack(TODO())
			}
			seeAllButton.setOnClickListener {
				changeFragmentWithBackStack(HistoricoSintoma())
			}

			recentsRecycler.adapter = adapter
		}

		lifecycleScope.launch {
			val queryResult = Symptom.collection.get().await()
			val symptoms = queryResult.documents.mapNotNull { fromDocument(it) }
				.take(NUMBER_OF_SYMPTOMS)

			adapter.submitList(symptoms)
		}
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}
}