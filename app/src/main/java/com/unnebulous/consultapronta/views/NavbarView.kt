package com.unnebulous.consultapronta.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.unnebulous.consultapronta.R
import com.unnebulous.consultapronta.databinding.NavbarViewBinding

class NavbarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding: NavbarViewBinding

    init {
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
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.NavbarView)

        val isProfessional = typedArray.getBoolean(R.styleable.NavbarView_professional, false)

        // TODO: definir texto dos elementos
        if (isProfessional) {
            binding.secondElementIcon.setImageResource(R.drawable.ic_group)
            binding.thirdElementIcon.setImageResource(R.drawable.ic_graphic)
            binding.fourthElementIcon.setImageResource(R.drawable.ic_pill)
        } else {
            binding.secondElementIcon.setImageResource(R.drawable.ic_history)
            binding.thirdElementIcon.setImageResource(R.drawable.ic_add_circle)
            binding.fourthElementIcon.setImageResource(R.drawable.ic_hospital)
        }

        typedArray.recycle()
    }
}