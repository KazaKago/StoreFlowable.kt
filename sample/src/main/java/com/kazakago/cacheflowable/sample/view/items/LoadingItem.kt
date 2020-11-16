package com.kazakago.cacheflowable.sample.view.items

import android.view.View
import com.kazakago.cacheflowable.sample.R
import com.kazakago.cacheflowable.sample.databinding.ItemLoadingBinding
import com.xwray.groupie.viewbinding.BindableItem

class LoadingItem : BindableItem<ItemLoadingBinding>(0) {

    override fun getLayout(): Int {
        return R.layout.item_loading
    }

    override fun initializeViewBinding(view: View): ItemLoadingBinding {
        return ItemLoadingBinding.bind(view)
    }

    override fun bind(viewBinding: ItemLoadingBinding, position: Int) {
        // do nothing.
    }

}

