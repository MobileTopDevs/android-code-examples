package com.example.hashtex.ui.login

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import com.example.hashtex.R
import com.example.hashtex.base.BaseBottomSheetDialogFragment
import com.example.hashtex.databinding.FragmentDialogSignUpBinding
import com.example.hashtex.util.TAG
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BottomSheetSignUpFragment : BaseBottomSheetDialogFragment<FragmentDialogSignUpBinding>() {

    override fun getFragmentLayoutResId() = R.layout.fragment_dialog_sign_up

    override fun isExpanded() = false

    override fun isTransparentBackground() = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
    }

    private fun initListeners() {
        binding.run {
            signUpBtn.setOnClickListener {
                activity?.supportFragmentManager?.setFragmentResult(
                    SIGN_UP_BOTTOM_SHEET_FRAGMENT_RESULT_REQUEST_KEY, bundleOf(
                        SIGN_UP_BOTTOM_SHEET_SHOULD_OPEN_ARG to true
                    )
                )

                dismiss()
            }

            skipBtn.setOnClickListener {
                dismiss()
            }
        }
    }

    companion object {
        val SIGN_UP_BOTTOM_SHEET_FRAGMENT_RESULT_REQUEST_KEY =
            "${TAG}.SIGN_UP_BOTTOM_SHEET_FRAGMENT_RESULT_REQUEST_KEY"
        val SIGN_UP_BOTTOM_SHEET_SHOULD_OPEN_ARG = "${TAG}.SIGN_UP_BOTTOM_SHEET_ARG"
    }
}