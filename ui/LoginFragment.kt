package com.example.hashtex.ui.login

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.hashtex.R
import com.example.hashtex.base.BaseViewModelFragment
import com.example.hashtex.base.validator.ui.TextInputEditTextValidator
import com.example.hashtex.databinding.FragmentLoginBinding
import com.example.hashtex.models.Result
import com.example.hashtex.models.auth.SocialProvider
import com.example.hashtex.models.auth.TwoFactorAuthWay
import com.example.hashtex.models.errors.AccessForbiddenException
import com.example.hashtex.models.errors.NotFoundException
import com.example.hashtex.models.errors.UnauthorizedException
import com.example.hashtex.ui.general.MessageDialogBtnClicked
import com.example.hashtex.ui.general.MessageDialogFragment
import com.example.hashtex.ui.login.dialogs.TwoFactorWaySelectDialogFragment
import com.example.hashtex.ui.login.validator.UIInputHelper
import com.example.hashtex.util.*
import com.example.hashtex.workers.FetchUserProfileWorker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : BaseViewModelFragment<FragmentLoginBinding, LoginViewModel>() {

    override val viewModel: LoginViewModel by viewModels()

    override fun getFragmentLayoutResId(): Int = R.layout.fragment_login

    private val googleSignInActivityResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        SocialSignInGoogleUtility.handleGoogleSignInActivityResult(
            it,
            onSignIn = { authCode, idToken ->
                val clientId = getString(R.string.google_client_id)
                val clientSecret = getString(R.string.google_secret)
                viewModel.getGoogleToken(
                    clientId = clientId,
                    clientSecret = clientSecret,
                    authCode = authCode,
                    idToken = idToken,
                )
            },
            onError = {
                showMessageDialog(
                    message = getString(R.string.error_social_sign_in)
                )
            }
        )
    }

    private lateinit var emailPhoneValidator: TextInputEditTextValidator<CharSequence>
    private lateinit var passwordValidator: TextInputEditTextValidator<CharSequence>

    private var selectedTwoFactorAuthWay: TwoFactorAuthWay? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        findNavController().graph.setStartDestination(R.id.loginFragment)

        binding.viewModel = viewModel

        initInputValidators()
        initListeners()
        initObservers()
    }

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

    private fun initListeners() {
        binding.loginEditTextPassword.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                login()
                true
            } else {
                false
            }
        }
        binding.loginSignInBtn.setOnClickListener {
            login()
        }
        binding.loginSignUpBtn.setOnClickListener {
            register()
        }
        binding.loginForgotPasswordBtn.setOnClickListener {
            viewModel.clearPasswordInputText()
            navigate(LoginFragmentDirections.openForgotPasswordScreen())
        }
        binding.loginTermsOfServiceBtn.setOnClickListener {
            // TODO: open terms screen
        }
        binding.loginSocialGoogleBtn.setOnClickListener {
            val intent = SocialSignInGoogleUtility.getSignInIntent(requireActivity())
            googleSignInActivityResult.launch(intent)
        }

        binding.loginSocialFacebookBtn.setOnClickListener {
            viewModel.getFacebookLoginToken(this)
        }

        setFragmentResultListener(ERROR_DIALOG_ACCOUNT_DISABLED_REQUEST_CODE) { _, b ->
            val dialogBtnClicked =
                b.serializable<MessageDialogBtnClicked>(MessageDialogFragment.RESULT_BTN_CLICKED)
            if (dialogBtnClicked == MessageDialogBtnClicked.POSITIVE) {
                navigate(LoginFragmentDirections.openContactSupportScreen())
            }
        }

        setFragmentResultListener(TwoFactorWaySelectDialogFragment.DIALOG_TWO_FACTOR_METHOD_SELECT_REQUEST_CODE) { _, b ->
            val userSession = b.getString(TwoFactorWaySelectDialogFragment.ARG_USER_SESSION)!!
            val twoFactorAuthWay = b.serializable<TwoFactorAuthWay>(
                TwoFactorWaySelectDialogFragment.ARG_SELECTED_TWO_FACTOR_METHOD
            )!!
            selectTwoFactorAuthWay(userSession, twoFactorAuthWay)
        }
    }

    private fun selectTwoFactorAuthWay(userSession: String, twoFactorAuthWay: TwoFactorAuthWay) {
        selectedTwoFactorAuthWay = twoFactorAuthWay
        viewModel.chooseTwoFactorAuthWay(userSession, twoFactorAuthWay)
    }

    private fun login() {
        hideSoftKeyboard()

        emailPhoneValidator.clearError()
        passwordValidator.clearError()

        if (emailPhoneValidator.validateToFirstError(viewModel.emailOrPhoneText.value.trim()).second
            && passwordValidator.validateToFirstError(viewModel.passwordText.value).second
        ) {
            viewModel.login()
        }
    }

    private fun register() {
        hideSoftKeyboard()

        emailPhoneValidator.clearError()
        passwordValidator.clearError()

        if (emailPhoneValidator.validateToFirstError(viewModel.emailOrPhoneText.value.trim()).second
            && passwordValidator.validateToFirstError(viewModel.passwordText.value).second
        ) {
            viewModel.register()
        }
    }

    private fun initInputValidators() {
        val emailPhoneMinLength = resources.getInteger(R.integer.input_length_min_email_phone)
        val emailPhoneMaxLength = resources.getInteger(R.integer.input_length_max_email_phone)
        emailPhoneValidator = UIInputHelper.buildEmailPhoneValidator(
            binding.loginInputLayoutEmailPhone,
            minLength = emailPhoneMinLength,
            maxLength = emailPhoneMaxLength,
            emptyMessage = getString(
                R.string.login_empty_email_phone_empty_error_text
            ),
            invalidLengthMessage = getString(
                R.string.login_empty_email_phone_length_error_text,
                emailPhoneMinLength
            )
        )
        binding.loginEditTextEmailPhone.addTextChangedListener {
            emailPhoneValidator.clearError()
        }

        val passwordMinLength = resources.getInteger(R.integer.input_length_min_password)
        val passwordMaxLength = resources.getInteger(R.integer.input_length_max_password)
        passwordValidator = UIInputHelper.buildPasswordValidator(
            binding.loginInputLayoutPassword,
            minLength = passwordMinLength,
            maxLength = passwordMaxLength,
            emptyMessage = getString(
                R.string.empty_password_empty_error_text
            ),
            invalidLengthMessage = getString(
                R.string.login_empty_password_length_error_text,
                passwordMinLength,
                passwordMaxLength
            )
        )
        binding.loginEditTextPassword.addTextChangedListener {
            passwordValidator.clearError()
        }
    }

    private fun initObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.loginResponse.collect { result ->
                        when (result) {
                            is Result.Loading -> {
                                hideSoftKeyboard()
                            }
                            is Result.Success -> {
                                FetchUserProfileWorker.beginWork(activityContext)

                                val loginResponseUI = result.data
                                if (loginResponseUI.hasTwoFactorAuth) {
                                    loginResponseUI.userSession?.let {
                                        val twoFactorAuthWays = loginResponseUI.twoFactorAuthWays
                                        if (twoFactorAuthWays.size == 1) {
                                            val selectedWay = twoFactorAuthWays.first()
                                            selectTwoFactorAuthWay(userSession = it, selectedWay)
                                        } else {
                                            navigate(
                                                LoginFragmentDirections.openTwoFactorWaySelectDialog(
                                                    twoFactorAuthWays = twoFactorAuthWays.toTypedArray(),
                                                    userSession = it
                                                )
                                            )
                                        }
                                    }
                                } else {
                                    navigate(LoginFragmentDirections.openDashboardScreen())
                                }
                                viewModel.resetLoginResponse()
                            }
                            is Result.Error -> {
                                if (result.exception is AccessForbiddenException
                                    || result.exception is UnauthorizedException
                                ) {
                                    showMessageDialog(
                                        requestCode = ERROR_DIALOG_ACCOUNT_DISABLED_REQUEST_CODE,
                                        title = getString(R.string.login_error_dlg_account_disabled),
                                        message = result.message,
                                        buttonPositiveTitle = getString(R.string.login_error_dlg_account_disabled_positive_btn_title)
                                    )
                                } else {
                                    showMessageDialog(
                                        title = if (result.exception is NotFoundException) {
                                            getString(R.string.error_incorrect_data_text)
                                        } else {
                                            getString(R.string.error_dialog_title_text)
                                        },
                                        message = result.message
                                    )
                                }
                                viewModel.resetLoginResponse()
                            }
                            else -> {
                                /* ignored */
                            }
                        }
                    }
                }
                launch {
                    viewModel.registerResponse.collect { result ->
                        when (result) {
                            is Result.Loading -> {
                                hideSoftKeyboard()
                            }
                            is Result.Success -> {
                                navigate(LoginFragmentDirections.openDashboardScreen())
                                viewModel.resetRegisterResponse()
                            }
                            is Result.Error -> {
                                showMessageDialog(
                                    title = getString(R.string.error_dialog_title_text),
                                    message = result.message
                                )
                                viewModel.resetRegisterResponse()
                            }
                            else -> Unit
                        }
                    }
                }
                launch {
                    viewModel.googleTokenResponse.collect { result ->
                        when (result) {
                            is Result.Success -> {
                                val token = result.data
                                viewModel.resetGoogleTokenResponse()
                                viewModel.socialLogin(
                                    provider = SocialProvider.GOOGLE,
                                    accessToken = token
                                )
                            }
                            is Result.Error -> {
                                showMessageDialog(message = getString(R.string.error_social_sign_in))
                                viewModel.resetGoogleTokenResponse()
                            }
                            else -> {
                                /* ignored */
                            }
                        }
                    }
                }
                launch {
                    viewModel.facebookLoginTokenResponse.collect { result ->
                        when (result) {
                            is Result.Success -> {
                                val token = result.data
                                viewModel.resetFacebookLoginTokenResponse()
                                viewModel.socialLogin(
                                    provider = SocialProvider.FACEBOOK,
                                    accessToken = token
                                )
                            }
                            is Result.Error -> {
                                showMessageDialog(message = getString(R.string.error_social_sign_in))
                                viewModel.resetFacebookLoginTokenResponse()
                            }
                            else -> {
                                /* ignored */
                            }
                        }
                    }
                }
                launch {
                    viewModel.twoFactorAuthWayChooseResponse.collect { result ->
                        when (result) {
                            is Result.Success -> {
                                val twoFactorAuthWayResponse = result.data
                                selectedTwoFactorAuthWay?.let {
                                    navigate(
                                        LoginFragmentDirections.openTwoFactorAuthScreen(
                                            twoAuthWayResponse = twoFactorAuthWayResponse,
                                            selectedTwoAuthWay = it
                                        )
                                    )
                                }
                                viewModel.resetTwoFactorAuthWayChooseResponse()
                            }
                            is Result.Error -> {
                                showMessageDialog(message = result.message)
                                viewModel.resetTwoFactorAuthWayChooseResponse()
                            }
                            else -> {
                                /* ignored */
                            }
                        }
                    }
                }
            }
        }
    }

    companion object {
        private val ERROR_DIALOG_ACCOUNT_DISABLED_REQUEST_CODE =
            "$TAG.ERROR_DIALOG_ACCOUNT_DISABLED_REQUEST_CODE"

        private val ARG_SAVEABLE_SELECTED_TWO_FACTOR_AUTH_WAY =
            "$TAG.ARG_SAVEABLE_SELECTED_TWO_FACTOR_AUTH_WAY"
    }
}