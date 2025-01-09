package com.sample.app.base

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import com.sample.app.R
import com.sample.app.base.view.SnackBarNoSwipeBehavior
import com.google.android.material.snackbar.Snackbar

abstract class BaseFragment<V : ViewDataBinding> : Fragment() {

    protected lateinit var binding: V
    protected lateinit var activityContext: Context

    private var toast: Toast? = null

    private var snackBar: Snackbar? = null

    protected var interactionListener: InteractionListener? = null

    abstract fun getFragmentLayout(): Int

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, getFragmentLayout(), container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        interactionListener = context as InteractionListener
        activityContext = context
    }

    override fun onDetach() {
        super.onDetach()
        interactionListener = null
    }

    fun showToast(
        text: String,
        length: Int = Toast.LENGTH_LONG
    ) {
        toast?.cancel() // to cancel previous toast before showing a new one
        toast = Toast.makeText(activityContext, text, length).apply {
            show()
        }
    }

    fun showSnackBar(
        text: String,
        length: Int = Snackbar.LENGTH_LONG
    ) {
        snackBar?.dismiss()
        snackBar = Snackbar.make(binding.root, text, length)
        snackBar?.show()
    }

    fun showSnackBarWithAction(
        text: String,
        length: Int = Snackbar.LENGTH_LONG,
        actionText: String = getString(R.string.retry),
        action: View.OnClickListener
    ) {
        snackBar?.dismiss()
        snackBar = Snackbar.make(binding.root, text, length).setAction(actionText, action).apply {
            if (length == Snackbar.LENGTH_INDEFINITE) {
                behavior = SnackBarNoSwipeBehavior()
            }
        }
        snackBar?.show()
    }

    fun dismissSnackBar() {
        snackBar?.dismiss()
        snackBar = null
    }

    interface InteractionListener {
        fun logout()
    }
}