package com.sample.app.base

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding

abstract class BaseActivity<V : ViewDataBinding> : AppCompatActivity() {

    lateinit var binding: V

    private var toast: Toast? = null

    abstract fun getActivityLayout(): Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, getActivityLayout())
    }

    fun showToast(
        text: String,
        length: Int = Toast.LENGTH_LONG
    ) {
        toast?.cancel() // to cancel previous toast before showing a new one
        toast = Toast.makeText(this, text, length).apply {
            show()
        }
    }
}