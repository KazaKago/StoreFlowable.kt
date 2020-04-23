package com.kazakago.cachesample.presentation.view

import com.kazakago.cachesample.R
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.item_error.view.*

data class ErrorItem(private val exception: Exception) : Item(exception.hashCode().toLong()) {

    var onRetry: (() -> Unit) = {}

    override fun getLayout(): Int {
        return R.layout.item_error
    }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.errorTextView.text = exception.toString()
        viewHolder.itemView.retryButton.setOnClickListener {
            onRetry()
        }
    }

}

