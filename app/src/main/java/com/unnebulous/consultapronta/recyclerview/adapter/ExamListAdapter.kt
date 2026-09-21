package com.unnebulous.consultapronta.recyclerview.adapter

import android.animation.ValueAnimator
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.unnebulous.consultapronta.R
import com.unnebulous.consultapronta.Utils
import com.unnebulous.consultapronta.capitalizeFix
import com.unnebulous.consultapronta.database.ExamModel
import com.unnebulous.consultapronta.databinding.CardExamBinding
import com.unnebulous.consultapronta.toBrazilianLocale

class ExamListAdapter: ListAdapter<ExamModel, ExamListAdapter.ExamViewHolder>(ExamComparator()) {

	class ExamViewHolder(
		private val binding: CardExamBinding
	) : RecyclerView.ViewHolder(binding.root) {

		private var isExpanded = false

		fun bind(exam: ExamModel) {
			binding.apply {
				examInfo.text = exam.name
				examIcon.setImageResource(when (exam.type) {
					"hemograma" -> R.drawable.ic_bloodtype
					"radiografia" -> R.drawable.ic_radiology
					"urina" -> R.drawable.ic_water_drop
				        else -> R.drawable.ic_broken_image
				})
				examState.text = when (exam.status) {
					Utils.ExamStatus.SOLICITADO -> "Agendado"
					Utils.ExamStatus.TRIAGEM -> "Em andamento"
					Utils.ExamStatus.LIBERADO -> "Resultado liberado"
					Utils.ExamStatus.PENDENTE -> "Ação pendente"
					null -> "Desconhecio"
				}
				examStateIcon.setImageResource(when (exam.status) {
					Utils.ExamStatus.SOLICITADO -> R.drawable.ic_more_three_dots
					Utils.ExamStatus.TRIAGEM -> R.drawable.ic_clock
					Utils.ExamStatus.LIBERADO -> R.drawable.ic_check_circle
					Utils.ExamStatus.PENDENTE -> R.drawable.ic_pending_actions
					null -> R.drawable.ic_question_mark
				})
				examStateIcon.imageTintList = ColorStateList.valueOf(
					ContextCompat.getColor(root.context, when (exam.status) {
						Utils.ExamStatus.SOLICITADO -> R.color.primary
						Utils.ExamStatus.TRIAGEM -> R.color.primary
						Utils.ExamStatus.LIBERADO -> R.color.success
						Utils.ExamStatus.PENDENTE -> R.color.primary
						null -> R.color.error
					}
					)
				)
				examCategory.text = exam.category.toString().capitalizeFix()
				examDate.text = exam.date!!.toBrazilianLocale()
				examHospital.text = exam.place
			}

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
			false)

		return ExamViewHolder(binding)
	}

	override fun onBindViewHolder(holder: ExamViewHolder, position: Int) {
		holder.bind(getItem(position))
	}

	class ExamComparator : DiffUtil.ItemCallback<ExamModel>() {
		override fun areItemsTheSame(old: ExamModel, new: ExamModel) = old.id == new.id
		override fun areContentsTheSame(old: ExamModel, new: ExamModel) = old == new
	}
}