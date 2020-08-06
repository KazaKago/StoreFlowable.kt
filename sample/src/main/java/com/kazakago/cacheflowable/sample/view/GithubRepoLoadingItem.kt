package com.kazakago.cacheflowable.sample.view

import com.kazakago.cacheflowable.sample.R
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item

class GithubRepoLoadingItem : Item(0) {

    override fun getLayout(): Int {
        return R.layout.item_github_repo_loading
    }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
    }

}

