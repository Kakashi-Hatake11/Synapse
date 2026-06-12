package com.app.synapse.core.base

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.viewbinding.ViewBinding
// Make sure you have a progress_dialog.xml or remove/comment out its usage if not.
// For simplicity, this version uses a programmatic ProgressBar for the dialog.
// import com.app.synapse.R

// Type alias for the inflater function for ViewBinding
typealias ActivityInflater<VB> = (inflater: LayoutInflater) -> VB

abstract class BaseActivity<VB : ViewBinding>(
    private val bindingInflater: ActivityInflater<VB>
) : AppCompatActivity() {

    private var _binding: VB? = null
    protected val binding: VB
        get() = _binding ?: throw IllegalStateException("Binding not initialized or already released.")

    private var progressDialog: Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = bindingInflater.invoke(layoutInflater)
        setContentView(binding.root)
        setupProgressDialog()
    }

    protected fun setupToolbar(toolbar: Toolbar, titleResId: Int? = null, showUpButton: Boolean = false) {
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            titleResId?.let { setTitle(it) }
            setDisplayHomeAsUpEnabled(showUpButton)
            setDisplayShowHomeEnabled(showUpButton)
        }
    }

    private fun setupProgressDialog() {
        progressDialog = Dialog(this).apply {
            // Using a simple ProgressBar programmatically
            val progressBar = ProgressBar(this@BaseActivity)
            setContentView(progressBar)
            // Example if using a layout: setContentView(R.layout.progress_dialog)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setCancelable(false)
            setCanceledOnTouchOutside(false)
        }
    }

    fun showProgress() {
        if (progressDialog?.isShowing == false) {
            progressDialog?.show()
        }
    }

    fun hideProgress() {
        if (progressDialog?.isShowing == true) {
            progressDialog?.dismiss()
        }
    }

    protected fun showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(this, message, duration).show()
    }

    protected fun showToast(messageResId: Int, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(this, getString(messageResId), duration).show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        // Dismiss dialog to prevent window leaks
        progressDialog?.dismiss()
        progressDialog = null
        // Clear binding reference
        _binding = null
        super.onDestroy()
    }
}
