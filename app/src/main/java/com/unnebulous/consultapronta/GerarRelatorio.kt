package com.unnebulous.consultapronta

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.unnebulous.consultapronta.databinding.FragmentGerarRelatorioBinding
import java.time.LocalDate

class GerarRelatorio : Fragment() {
	private var _binding: FragmentGerarRelatorioBinding? = null
	private val binding get() = _binding!!

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = FragmentGerarRelatorioBinding.inflate(layoutInflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		updateHeader {
			changeHeaderType(Utils.HeaderType.TITLED, true)
			setScreenTitle(getString(R.string.gen_reports_screen_title))
			setGoBackButtonOnClickListener {
				popBackStack()
			}
		}

		var reportPeriodEndDate = LocalDate.now()
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}
}