package com.kazakago.storeflowable.example.view.items

import android.view.View
import com.kazakago.storeflowable.example.R
import com.kazakago.storeflowable.example.databinding.ItemLoadingBinding
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
