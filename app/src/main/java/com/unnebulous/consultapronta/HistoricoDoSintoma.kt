package com.unnebulous.consultapronta

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.unnebulous.consultapronta.database.SymptomHistory
import com.unnebulous.consultapronta.databinding.FragmentHistoricoDoSintomaBinding
import com.unnebulous.consultapronta.recyclerview.adapter.SymptomHistoryAdapter

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

		binding.recyclerview.adapter = adapter
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