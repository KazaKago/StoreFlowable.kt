package com.kazakago.cacheflowable.sample.view

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.observe
import com.google.android.material.snackbar.Snackbar
import com.kazakago.cacheflowable.sample.R
import com.kazakago.cacheflowable.sample.model.GithubRepo
import com.kazakago.cacheflowable.sample.viewmodel.GithubReposViewModel
import com.kazakago.cacheflowable.sample.viewmodel.livedata.compositeLiveDataOf
import com.kazakago.cacheflowable.sample.viewmodel.livedata.observe
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.activity_github_repos.*

class GithubReposActivity : AppCompatActivity() {

    companion object {
        fun createIntent(context: Context, userName: String): Intent {
            return Intent(context, GithubReposActivity::class.java).apply {
                putExtra(ParameterName.UserName.name, userName)
            }
        }
    }

    private enum class ParameterName {
        UserName
    }

    private val githubReposGroupAdapter = GroupAdapter<GroupieViewHolder>()
    private val githubReposViewModel by viewModels<GithubReposViewModel> {
        val githubUserName = intent.getStringExtra(ParameterName.UserName.name)!!
        GithubReposViewModel.Factory(application, githubUserName)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_github_repos)

        githubReposRecyclerView.adapter = githubReposGroupAdapter
        githubReposRecyclerView.addOnBottomReached {
            githubReposViewModel.requestAdditional(false)
        }
        swipeRefreshLayout.setOnRefreshListener {
            githubReposViewModel.request()
        }
        retryButton.setOnClickListener {
            githubReposViewModel.request()
        }
        compositeLiveDataOf(githubReposViewModel.githubRepos, githubReposViewModel.isAdditionalLoading, githubReposViewModel.additionalError).observe(this) {
            val items = mutableListOf<Item>().apply {
                this += createGithubRepoItems(it.first)
                it.second.let { if (it) this += createLoadingItem() }
                it.third.let { if (it != null) this += createErrorItem(it) }
            }
            githubReposGroupAdapter.updateAsync(items)
        }
        githubReposViewModel.isMainLoading.observe(this) {
            progressBar.isVisible = it
        }
        githubReposViewModel.mainError.observe(this) {
            errorGroup.isVisible = (it != null)
            errorTextView.text = it?.toString()
        }
        githubReposViewModel.hideSwipeRefresh.observe(this, "") {
            swipeRefreshLayout.isRefreshing = false
        }
        githubReposViewModel.strongError.observe(this, "") {
            Snackbar.make(rootView, it.toString(), Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun createGithubRepoItems(githubRepos: List<GithubRepo>): List<GithubRepoItem> {
        return githubRepos.map { githubRepo ->
            GithubRepoItem(githubRepo).apply {
                onClick = { githubRepo -> launch(githubRepo.htmlUrl) }
            }
        }
    }

    private fun createLoadingItem(): GithubRepoLoadingItem {
        return GithubRepoLoadingItem()
    }

    private fun createErrorItem(exception: Exception): GithubRepoErrorItem {
        return GithubRepoErrorItem(exception).apply {
            onRetry = { githubReposViewModel.retryAdditional() }
        }
    }

    private fun launch(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

}
