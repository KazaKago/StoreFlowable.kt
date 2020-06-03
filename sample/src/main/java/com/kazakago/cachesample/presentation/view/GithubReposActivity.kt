package com.kazakago.cachesample.presentation.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.observe
import com.google.android.material.snackbar.Snackbar
import com.kazakago.cachesample.R
import com.kazakago.cachesample.domain.model.GithubRepo
import com.kazakago.cachesample.presentation.view.global.addOnBottomReached
import com.kazakago.cachesample.presentation.viewmodel.GithubReposViewModel
import com.kazakago.cachesample.presentation.viewmodel.livedata.compositeLiveDataOf
import com.kazakago.cachesample.presentation.viewmodel.livedata.observe
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.activity_github_repos.*
import java.net.URL

class GithubReposActivity : AppCompatActivity() {

    private val githubReposGroupAdapter = GroupAdapter<GroupieViewHolder>()
    private val mainViewModel by viewModels<GithubReposViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_github_repos)

        githubReposRecyclerView.adapter = githubReposGroupAdapter
        githubReposRecyclerView.addOnBottomReached {
            mainViewModel.requestAdditional(false)
        }
        swipeRefreshLayout.setOnRefreshListener {
            mainViewModel.request()
        }
        retryButton.setOnClickListener {
            mainViewModel.request()
        }
        compositeLiveDataOf(mainViewModel.githubRepos, mainViewModel.isAdditionalLoading, mainViewModel.additionalError).observe(this) {
            val items = mutableListOf<Item>().apply {
                this += createGithubRepoItems(it.first)
                it.second.let { if (it) this += createLoadingItem() }
                it.third.let { if (it != null) this += createErrorItem(it) }
            }
            githubReposGroupAdapter.updateAsync(items)
        }
        mainViewModel.isMainLoading.observe(this) {
            progressBar.isVisible = it
        }
        mainViewModel.mainError.observe(this) {
            errorGroup.isVisible = (it != null)
            errorTextView.text = it?.toString()
        }
        mainViewModel.hideSwipeRefresh.observe(this, "") {
            swipeRefreshLayout.isRefreshing = false
        }
        mainViewModel.strongError.observe(this, "") {
            Snackbar.make(rootView, it.toString(), Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun createGithubRepoItems(githubRepos: List<GithubRepo>): List<GithubRepoItem> {
        return githubRepos.map { githubRepo ->
            GithubRepoItem(githubRepo).apply {
                onClick = { githubRepo -> launch(githubRepo.url) }
            }
        }
    }

    private fun createLoadingItem(): GithubRepoLoadingItem {
        return GithubRepoLoadingItem()
    }

    private fun createErrorItem(exception: Exception): GithubRepoErrorItem {
        return GithubRepoErrorItem(exception).apply {
            onRetry = { mainViewModel.retryAdditional() }
        }
    }

    private fun launch(url: URL) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url.toString()))
        startActivity(intent)
    }

}
