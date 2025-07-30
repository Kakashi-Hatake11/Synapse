package com.app.synapse.core.base

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

// Type alias for the inflater function for ViewBinding in Fragments
typealias FragmentInflater<VB> = (inflater: LayoutInflater, container: ViewGroup?, attachToRoot: Boolean) -> VB

abstract class BaseFragment<VB : ViewBinding>(
    private val bindingInflater: FragmentInflater<VB>
) : Fragment() {

    private var _binding: VB? = null
    protected val binding: VB
        get() = _binding ?: throw IllegalStateException("Binding not initialized or already released.")

    private var baseActivity: BaseActivity<*>? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is BaseActivity<*>) {
            baseActivity = context
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = bindingInflater.invoke(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        observeViewModel()
    }

    /**
     * Abstract method for subclasses to setup their views.
     * Called in onViewCreated.
     */
    protected abstract fun setupViews()

    /**
     * Abstract method for subclasses to observe ViewModel LiveData.
     * Called in onViewCreated.
     */
    protected abstract fun observeViewModel()

    protected fun showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
        context?.let { Toast.makeText(it, message, duration).show() }
    }

    protected fun showToast(messageResId: Int, duration: Int = Toast.LENGTH_SHORT) {
        context?.let { Toast.makeText(it, getString(messageResId), duration).show() }
    }

    protected fun showProgress() {
        baseActivity?.showProgress()
    }

    private fun hideProgress() {
        baseActivity?.hideProgress()
    }

    override fun onDetach() {
        super.onDetach()
        baseActivity = null // Clean up reference
    }

    override fun onDestroyView() {
        // It's good practice to hide progress if the fragment view is destroyed
        // to avoid leaks if an operation was ongoing.
        hideProgress()
        _binding = null // Important to prevent memory leaks
        super.onDestroyView()
    }
}
