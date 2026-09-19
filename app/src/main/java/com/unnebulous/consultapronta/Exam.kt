package com.unnebulous.consultapronta

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.firestore.ListenerRegistration
import com.unnebulous.consultapronta.database.DatabaseManager
import com.unnebulous.consultapronta.database.ExamModel
import com.unnebulous.consultapronta.databinding.FragmentExamBinding
import com.unnebulous.consultapronta.recyclerview.SpacingItemDecoration
import com.unnebulous.consultapronta.recyclerview.adapter.ExamListAdapter

class Exam : Fragment() {

	private var _binding: FragmentExamBinding? = null
	private val binding get() = _binding!!

	private lateinit var firestoreListener: ListenerRegistration

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
			setScreenTitle(getString(R.string.title_exam_page))
			setGoBackButtonOnClickListener {
				popBackStack()
			}
		}

		val adapter = ExamListAdapter()
		binding.examCards.adapter = adapter
		firestoreListener = ExamModel.collection
			.addSnapshotListener { snapshots, exception ->
				if (exception != null) {
					Log.e(ExamModel.COLLECTION_NAME, "getExams", exception)
					return@addSnapshotListener
				}

				if (snapshots == null) {
					return@addSnapshotListener
				}

				val exams = snapshots.documents.mapNotNull { ExamModel.fromDocument(it) }

				adapter.submitList(exams)
			}

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
		firestoreListener.remove()
	}
}