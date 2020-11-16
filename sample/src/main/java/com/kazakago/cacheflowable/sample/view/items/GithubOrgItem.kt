package com.kazakago.cacheflowable.sample.view.items

import com.kazakago.cacheflowable.sample.R
import com.kazakago.cacheflowable.sample.model.GithubOrg
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.item_github_org.view.*

data class GithubOrgItem(private val githubOrg: GithubOrg) : Item(githubOrg.id) {

    override fun getLayout(): Int {
        return R.layout.item_github_org
    }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.idTextView.text = "ID: ${githubOrg.id}"
        viewHolder.itemView.titleTextView.text = githubOrg.name
    }

}

