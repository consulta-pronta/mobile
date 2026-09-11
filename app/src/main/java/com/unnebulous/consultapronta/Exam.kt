package com.unnebulous.consultapronta

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.unnebulous.consultapronta.databinding.FragmentExamBinding
import com.unnebulous.consultapronta.recyclerview.SpacingItemDecoration
import com.unnebulous.consultapronta.recyclerview.adapter.ExamListAdapter

class Exam : Fragment() {

	private var _binding: FragmentExamBinding? = null
	private val binding get() = _binding!!

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {

		_binding = FragmentExamBinding.inflate(
			inflater,
			container,
			false
		)

		return binding.root
	}

	override fun onViewCreated(
		view: View,
		savedInstanceState: Bundle?
	) {
		super.onViewCreated(view, savedInstanceState)

		updateHeader {
			changeHeaderType(Utils.HeaderType.TITLED, true)
			setScreenTitle(getString(R.string.title_register_page))
			setGoBackButtonOnClickListener {
				popBackStack()
			}
		}

		binding.examCards.adapter = ExamListAdapter()

		val spacing = resources.getDimensionPixelSize(
			R.dimen.default_card_padding
		)

		binding.examCards.addItemDecoration(
			SpacingItemDecoration(spacing)
		)
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}
}