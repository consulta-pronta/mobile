package com.unnebulous.consultapronta.views

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.unnebulous.consultapronta.R
import com.unnebulous.consultapronta.databinding.ProfilePictureViewBinding

class ProfilePictureView @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

	private val binding: ProfilePictureViewBinding

	init {
		binding = ProfilePictureViewBinding.inflate(
			LayoutInflater.from(context),
			this,
			true
		)

		attrs?.let {
			applyAttributes(it)
		}
	}

	private fun applyAttributes(attrs: AttributeSet) {
		val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ProfilePictureView)

		val profileImageRes = typedArray.getResourceId(R.styleable.ProfilePictureView_profileImageSrc, 0)

		if (profileImageRes != 0) {
			binding.profileImageView.setImageResource(profileImageRes)
		}

		typedArray.recycle()
	}

	fun setProfileImage(uri: Uri) {
		Glide
			.with(context)
			.load(uri)
			.circleCrop()
			.into(binding.profileImageView)
	}
}