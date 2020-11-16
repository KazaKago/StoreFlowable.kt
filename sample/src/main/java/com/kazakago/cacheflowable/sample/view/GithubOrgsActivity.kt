package com.kazakago.cacheflowable.sample.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.google.android.material.snackbar.Snackbar
import com.kazakago.cacheflowable.sample.R
import com.kazakago.cacheflowable.sample.model.GithubOrg
import com.kazakago.cacheflowable.sample.view.items.ErrorItem
import com.kazakago.cacheflowable.sample.view.items.GithubOrgItem
import com.kazakago.cacheflowable.sample.view.items.LoadingItem
import com.kazakago.cacheflowable.sample.viewmodel.GithubOrgsViewModel
import com.kazakago.cacheflowable.sample.viewmodel.livedata.compositeLiveDataOf
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.activity_github_orgs.*

class GithubOrgsActivity : AppCompatActivity() {

    companion object {
        fun createIntent(context: Context): Intent {
            return Intent(context, GithubOrgsActivity::class.java)
        }
    }

    private val githubOrgsGroupAdapter = GroupAdapter<GroupieViewHolder>()
    private val githubOrgsViewModel by viewModels<GithubOrgsViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_github_orgs)

        githubOrgsRecyclerView.adapter = githubOrgsGroupAdapter
        githubOrgsRecyclerView.addOnBottomReached {
            githubOrgsViewModel.requestAdditional()
        }
        swipeRefreshLayout.setOnRefreshListener {
            githubOrgsViewModel.request()
        }
        retryButton.setOnClickListener {
            githubOrgsViewModel.request()
        }
        compositeLiveDataOf(githubOrgsViewModel.githubOrgs, githubOrgsViewModel.isAdditionalLoading, githubOrgsViewModel.additionalError).observe(this) {
            val items = mutableListOf<Item>().apply {
                this += createGithubOrgItems(it.first)
                it.second.let { if (it) this += createLoadingItem() }
                it.third.let { if (it != null) this += createErrorItem(it) }
            }
            githubOrgsGroupAdapter.updateAsync(items)
        }
        githubOrgsViewModel.isMainLoading.observe(this) {
            progressBar.isVisible = it
        }
        githubOrgsViewModel.mainError.observe(this) {
            errorGroup.isVisible = (it != null)
            errorTextView.text = it?.toString()
        }
        githubOrgsViewModel.hideSwipeRefresh.observe(this, "") {
            swipeRefreshLayout.isRefreshing = false
        }
        githubOrgsViewModel.strongError.observe(this, "") {
            Snackbar.make(rootView, it.toString(), Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun createGithubOrgItems(githubOrgs: List<GithubOrg>): List<GithubOrgItem> {
        return githubOrgs.map { GithubOrgItem(it) }
    }

    private fun createLoadingItem(): LoadingItem {
        return LoadingItem()
    }

    private fun createErrorItem(exception: Exception): ErrorItem {
        return ErrorItem(exception).apply {
            onRetry = { githubOrgsViewModel.retryAdditional() }
        }
    }

}
