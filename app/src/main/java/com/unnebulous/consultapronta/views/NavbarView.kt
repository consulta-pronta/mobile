package com.unnebulous.consultapronta.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.unnebulous.consultapronta.R
import com.unnebulous.consultapronta.databinding.NavbarViewBinding
import androidx.core.content.withStyledAttributes

class NavbarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding: NavbarViewBinding

    init {
        clipChildren = false
        clipToPadding = false

        binding = NavbarViewBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )

        attrs?.let {
            applyAttributes(it)
        }
    }

    private fun applyAttributes(attrs: AttributeSet) {
        context.withStyledAttributes(attrs, R.styleable.NavbarView) {

            val isProfessional = getBoolean(R.styleable.NavbarView_professional, false)

            if (isProfessional) {
                binding.secondElementIcon.setImageResource(R.drawable.ic_group)
                binding.secondElementText.text =
                    context.getString(R.string.navbar_second_element_professional_text)

                binding.mainButtonIcon.setImageResource(R.drawable.ic_graphic)

                binding.fourthElementIcon.setImageResource(R.drawable.ic_pill)
                binding.fourthElementText.text =
                    context.getString(R.string.navbar_fourth_element_professional_text)
            } else {
                binding.secondElementIcon.setImageResource(R.drawable.ic_history)
                binding.secondElementText.text =
                    context.getString(R.string.navbar_second_element_patient_text)

                binding.mainButtonIcon.setImageResource(R.drawable.ic_add_circle)

                binding.fourthElementIcon.setImageResource(R.drawable.ic_hospital)
                binding.fourthElementText.text =
                    context.getString(R.string.navbar_fourth_element_patient_text)
            }

        }
    }
}