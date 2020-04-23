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
import com.kazakago.cachesample.presentation.viewmodel.GithubRepoState
import com.kazakago.cachesample.presentation.viewmodel.MainViewModel
import com.kazakago.cachesample.presentation.viewmodel.livedata.observe
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.activity_main.*
import java.net.URL

class MainActivity : AppCompatActivity() {

    private val githubReposGroupAdapter = GroupAdapter<GroupieViewHolder>()
    private val mainViewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
        mainViewModel.githubReposState.observe(this) {
            when (it) {
                is GithubRepoState.Loading -> {
                    progressBar.isVisible = true
                    errorGroup.isVisible = false
                    githubReposGroupAdapter.clear()
                }
                is GithubRepoState.LoadingWithValue -> {
                    progressBar.isVisible = false
                    errorGroup.isVisible = false
                    githubReposGroupAdapter.updateAsync(createGithubRepoItems(it.githubRepos) + createLoadingItem())
                }
                is GithubRepoState.Completed -> {
                    progressBar.isVisible = false
                    errorGroup.isVisible = false
                    githubReposGroupAdapter.updateAsync(createGithubRepoItems(it.githubRepos))
                }
                is GithubRepoState.Error -> {
                    progressBar.isVisible = false
                    errorGroup.isVisible = true
                    errorTextView.text = it.exception.toString()
                    githubReposGroupAdapter.clear()
                }
                is GithubRepoState.ErrorWithValue -> {
                    progressBar.isVisible = false
                    errorGroup.isVisible = false
                    githubReposGroupAdapter.updateAsync(createGithubRepoItems(it.githubRepos) + createErrorItem(it.exception))
                }
            }
        }
        mainViewModel.hideSwipeRefresh.observe(this, "") {
            swipeRefreshLayout.isRefreshing = false
        }
        mainViewModel.exception.observe(this, "") {
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

    private fun createLoadingItem(): LoadingItem {
        return LoadingItem()
    }

    private fun createErrorItem(exception: Exception): ErrorItem {
        return ErrorItem(exception).apply {
            onRetry = { mainViewModel.retryAdditional() }
        }
    }

    private fun launch(url: URL) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url.toString()))
        startActivity(intent)
    }

}
