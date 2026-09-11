package com.unnebulous.consultapronta.recyclerview.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.unnebulous.consultapronta.databinding.CardExamBinding
import android.animation.ValueAnimator
import androidx.core.animation.doOnEnd

class ExamListAdapter :
	RecyclerView.Adapter<ExamListAdapter.ExamViewHolder>() {

	inner class ExamViewHolder(
		private val binding: CardExamBinding
	) : RecyclerView.ViewHolder(binding.root) {

		private var isExpanded = false

		fun bind() {

			binding.viewMore.setOnClickListener {

				isExpanded = !isExpanded

				if (isExpanded) {

					// Mostra a parte de baixo antes de medir
					binding.examDetails.visibility = View.VISIBLE

					binding.examDetails.measure(
						View.MeasureSpec.makeMeasureSpec(
							binding.root.width,
							View.MeasureSpec.EXACTLY
						),
						View.MeasureSpec.makeMeasureSpec(
							0,
							View.MeasureSpec.UNSPECIFIED
						)
					)

					val targetHeight = binding.examDetails.measuredHeight

					// Começa com altura 0
					binding.examDetails.layoutParams.height = 0

					ValueAnimator.ofInt(0, targetHeight).apply {

						duration = 300

						addUpdateListener { animator ->
							val height = animator.animatedValue as Int

							binding.examDetails.layoutParams.height = height
							binding.examDetails.requestLayout()
						}

						start()
					}

					// Gira a seta
					binding.viewMore.animate()
						.rotation(180f)
						.setDuration(300)
						.start()

				} else {

					val initialHeight = binding.examDetails.height

					// Anima a altura até 0
					ValueAnimator.ofInt(initialHeight, 0).apply {

						duration = 300

						addUpdateListener { animator ->
							val height = animator.animatedValue as Int

							binding.examDetails.layoutParams.height = height
							binding.examDetails.requestLayout()
						}

						doOnEnd {
							binding.examDetails.visibility = View.GONE
						}

						start()
					}

					// Volta a seta
					binding.viewMore.animate()
						.rotation(0f)
						.setDuration(300)
						.start()
				}
			}
		}
	}

	override fun onCreateViewHolder(
		parent: ViewGroup,
		viewType: Int
	): ExamViewHolder {

		val binding = CardExamBinding.inflate(
			LayoutInflater.from(parent.context),
			parent,
			false
		)

		return ExamViewHolder(binding)
	}

	override fun onBindViewHolder(
		holder: ExamViewHolder,
		position: Int
	) {
		holder.bind()
	}

	override fun getItemCount(): Int {
		return 10
	}
}