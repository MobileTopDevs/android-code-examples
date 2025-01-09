package com.sample.app.base

import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModel

abstract class BaseFragmentViewModel<V : ViewDataBinding, VM : ViewModel> : BaseFragment<V>() {

    protected abstract val viewModel: VM
}