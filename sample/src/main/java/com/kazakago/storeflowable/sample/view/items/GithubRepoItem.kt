package com.kazakago.storeflowable.sample.view.items

import android.view.View
import com.kazakago.storeflowable.sample.R
import com.kazakago.storeflowable.sample.databinding.ItemGithubRepoBinding
import com.kazakago.storeflowable.sample.model.GithubRepo
import com.xwray.groupie.viewbinding.BindableItem

data class GithubRepoItem(private val githubRepo: GithubRepo) : BindableItem<ItemGithubRepoBinding>(githubRepo.id) {

    var onClick: ((githubRepo: GithubRepo) -> Unit) = {}

    override fun getLayout(): Int {
        return R.layout.item_github_repo
    }

    override fun initializeViewBinding(view: View): ItemGithubRepoBinding {
        return ItemGithubRepoBinding.bind(view)
    }

    override fun bind(viewBinding: ItemGithubRepoBinding, position: Int) {
        viewBinding.idTextView.text = "ID: ${githubRepo.id}"
        viewBinding.titleTextView.text = githubRepo.fullName
        viewBinding.linkTextView.text = githubRepo.htmlUrl
        viewBinding.root.setOnClickListener {
            onClick(githubRepo)
        }
    }
}
