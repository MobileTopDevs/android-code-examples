package com.example.hashtex.ui.login.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.navArgs
import com.example.hashtex.R
import com.example.hashtex.models.auth.TwoFactorAuthWay
import com.example.hashtex.util.TAG
import com.example.hashtex.util.serializable


class TwoFactorWaySelectDialogFragment : DialogFragment() {

    private val args by navArgs<TwoFactorWaySelectDialogFragmentArgs>()

    private var selectedTwoFactorAuthWay: TwoFactorAuthWay? = null

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        selectedTwoFactorAuthWay =
            savedInstanceState?.serializable(ARG_SAVEABLE_SELECTED_TWO_FACTOR_AUTH_WAY)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable(
            ARG_SAVEABLE_SELECTED_TWO_FACTOR_AUTH_WAY,
            selectedTwoFactorAuthWay
        )
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireContext()).apply {
            setTitle(getString(R.string.two_factor_way_select_dialog_title))
            val twoFactorAuthWays = args.twoFactorAuthWays
            val checkedIdx = selectedTwoFactorAuthWay?.let { twoFactorAuthWays.indexOf(it) } ?: -1
            setSingleChoiceItems(
                twoFactorAuthWays.map {
                    when (it) {
                        TwoFactorAuthWay.EMAIL -> getString(R.string.two_factor_way_email)
                        TwoFactorAuthWay.SMS -> getString(R.string.two_factor_way_sms)
                        TwoFactorAuthWay.GOOGLE_AUTHENTICATOR -> getString(R.string.two_factor_way_google_auth_app)
                    }
                }.toTypedArray(),
                checkedIdx
            ) { _, i ->
                selectedTwoFactorAuthWay = twoFactorAuthWays[i]
            }
            setPositiveButton(getString(R.string.accept_text)) { _, _ ->
                selectedTwoFactorAuthWay?.let {
                    setMethod(it)
                }
            }
            setNegativeButton(getString(R.string.cancel_text), null)
        }.create()
    }

    private fun setMethod(twoFactorAuthWay: TwoFactorAuthWay) {
        setFragmentResult(
            DIALOG_TWO_FACTOR_METHOD_SELECT_REQUEST_CODE,
            bundleOf(
                ARG_SELECTED_TWO_FACTOR_METHOD to twoFactorAuthWay,
                ARG_USER_SESSION to args.userSession
            )
        )
    }

    companion object {
        val DIALOG_TWO_FACTOR_METHOD_SELECT_REQUEST_CODE =
            "$TAG.DIALOG_TWO_FACTOR_METHOD_SELECT_REQUEST_CODE"
        val ARG_SELECTED_TWO_FACTOR_METHOD = "$TAG.ARG_SELECTED_TWO_FACTOR_METHOD"
        val ARG_USER_SESSION = "$TAG.ARG_USER_SESSION"

        private val ARG_SAVEABLE_SELECTED_TWO_FACTOR_AUTH_WAY =
            "$TAG.ARG_SAVEABLE_SELECTED_TWO_FACTOR_AUTH_WAY"
    }
}