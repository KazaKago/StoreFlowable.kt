package com.kazakago.storeflowable.example.view.items

import android.view.View
import com.kazakago.storeflowable.example.R
import com.kazakago.storeflowable.example.databinding.ItemGithubOrgBinding
import com.kazakago.storeflowable.example.model.GithubOrg
import com.xwray.groupie.viewbinding.BindableItem

data class GithubOrgItem(private val githubOrg: GithubOrg) : BindableItem<ItemGithubOrgBinding>(githubOrg.id) {

    override fun getLayout(): Int {
        return R.layout.item_github_org
    }

    override fun initializeViewBinding(view: View): ItemGithubOrgBinding {
        return ItemGithubOrgBinding.bind(view)
    }

    override fun bind(viewBinding: ItemGithubOrgBinding, position: Int) {
        viewBinding.idTextView.text = "ID: ${githubOrg.id}"
        viewBinding.titleTextView.text = githubOrg.name
    }
}
