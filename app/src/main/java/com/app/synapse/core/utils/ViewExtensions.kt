package com.app.synapse.core.utils

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import androidx.compose.animation.with
import androidx.compose.ui.semantics.error
import androidx.wear.compose.material.placeholder
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.snackbar.Snackbar

/**
 * Makes the View visible.
 */
fun View.show() {
    visibility = View.VISIBLE
}

/**
 * Makes the View invisible.
 */
fun View.invisible() {
    visibility = View.INVISIBLE
}

/**
 * Makes the View gone.
 */
fun View.gone() {
    visibility = View.GONE
}

/**
 * Toggles the View's visibility between VISIBLE and GONE.
 * @param show True to make visible, false to make gone. If null, toggles based on current state.
 */
fun View.toggleVisibility(show: Boolean? = null) {
    visibility = when (show) {
        true -> View.VISIBLE
        false -> View.GONE
        null -> if (visibility == View.VISIBLE) View.GONE else View.VISIBLE
    }
}

/**
 * Shows a simple Snackbar.
 */
fun View.showSnackbar(message: String, duration: Int = Snackbar.LENGTH_SHORT) {
    Snackbar.make(this, message, duration).show()
}

/**
 * Shows a Snackbar with an action.
 */
fun View.showSnackbarWithAction(
    message: String,
    actionText: String,
    duration: Int = Snackbar.LENGTH_INDEFINITE,
    action: (View) -> Unit
) {
    Snackbar.make(this, message, duration)
        .setAction(actionText, action)
        .show()
}

/**
 * Loads an image into an ImageView using Glide.
 * Add Glide dependency: implementation "com.github.bumptech.glide:glide:4.16.0" (or latest)
 * @param imageUrl The URL of the image to load.
 * @param placeholderRes Optional placeholder drawable resource ID.
 * @param errorRes Optional error drawable resource ID.
 * @param circleCrop Optional: whether to crop the image to a circle.
 */
fun ImageView.loadImage(
    imageUrl: String?,
    placeholderRes: Int? = null,
    errorRes: Int? = null,
    circleCrop: Boolean = false
) {
    val request = Glide.with(this.context).load(imageUrl)

    placeholderRes?.let { request.placeholder(it) }
    errorRes?.let { request.error(it) }

    if (circleCrop) {
        request.circleCrop()
    }

    request.transition(DrawableTransitionOptions.withCrossFade())
        .into(this)
}

/**
 * Hides the soft keyboard from the screen.
 */
fun View.hideKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}

/**
 * Shows the soft keyboard and requests focus for the view.
 */
fun View.showKeyboard() {
    this.requestFocus()
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
}

