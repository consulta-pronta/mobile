package com.unnebulous.consultapronta

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.unnebulous.consultapronta.views.Symptom
import com.unnebulous.consultapronta.views.SymptomAdapter

class Home : Fragment() {

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {

		val view = inflater.inflate(
			R.layout.fragment_home,
			container,
			false
		)

		val recyclerView =
			view.findViewById<RecyclerView>(
				R.id.recents_recycler
			)

		recyclerView.layoutManager =
			LinearLayoutManager(requireContext())

		val symptoms = mutableListOf<Symptom>()

		val adapter = SymptomAdapter(symptoms)

		recyclerView.adapter = adapter

		return view
	}
}