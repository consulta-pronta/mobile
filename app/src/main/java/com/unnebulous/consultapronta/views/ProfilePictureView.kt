package com.unnebulous.consultapronta.views

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.unnebulous.consultapronta.R
import com.unnebulous.consultapronta.databinding.ProfilePictureViewBinding
import androidx.core.content.withStyledAttributes

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
		context.withStyledAttributes(attrs, R.styleable.ProfilePictureView) {

			val profileImageRes = getResourceId(R.styleable.ProfilePictureView_profileImageSrc, 0)

			if (profileImageRes != 0) {
				binding.profileImageView.setImageResource(profileImageRes)
			}
		}
	}

	fun setProfileImage(uri: Uri) {
		Glide
			.with(context)
			.load(uri)
			.circleCrop()
			.into(binding.profileImageView)
	}
}