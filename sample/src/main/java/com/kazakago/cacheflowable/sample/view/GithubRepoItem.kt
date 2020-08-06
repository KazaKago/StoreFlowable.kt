package com.kazakago.cacheflowable.sample.view

import com.kazakago.cacheflowable.sample.R
import com.kazakago.cacheflowable.sample.model.GithubRepo
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.item_github_repo.view.*

data class GithubRepoItem(private val githubRepo: GithubRepo) : Item(githubRepo.id) {

    var onClick: ((githubRepo: GithubRepo) -> Unit) = {}

    override fun getLayout(): Int {
        return R.layout.item_github_repo
    }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.idTextView.text = "ID: ${githubRepo.id}"
        viewHolder.itemView.titleTextView.text = githubRepo.fullName
        viewHolder.itemView.linkTextView.text = githubRepo.htmlUrl
        viewHolder.itemView.setOnClickListener {
            onClick(githubRepo)
        }
    }

}

