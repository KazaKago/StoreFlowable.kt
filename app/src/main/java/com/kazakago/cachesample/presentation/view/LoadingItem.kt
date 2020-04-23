package com.kazakago.cachesample.presentation.view

import com.kazakago.cachesample.R
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item

class LoadingItem() : Item(0) {

    override fun getLayout(): Int {
        return R.layout.item_loading
    }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
    }

}

