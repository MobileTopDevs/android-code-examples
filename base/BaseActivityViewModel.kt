package com.sample.app.base

import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModel

abstract class BaseActivityViewModel<V : ViewDataBinding, VM : ViewModel> : BaseActivity<V>() {

    protected abstract val viewModel: VM
}