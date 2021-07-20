package com.kazakago.storeflowable.example.view

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.kazakago.storeflowable.example.databinding.ActivityGithubReposBinding
import com.kazakago.storeflowable.example.model.GithubRepo
import com.kazakago.storeflowable.example.view.items.ErrorItem
import com.kazakago.storeflowable.example.view.items.GithubRepoItem
import com.kazakago.storeflowable.example.view.items.LoadingItem
import com.kazakago.storeflowable.example.viewmodel.GithubReposViewModel
import com.xwray.groupie.Group
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.coroutines.flow.collect

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

    private val binding by lazy { ActivityGithubReposBinding.inflate(layoutInflater) }
    private val githubReposGroupAdapter = GroupAdapter<GroupieViewHolder>()
    private val githubReposViewModel by viewModels<GithubReposViewModel> {
        val githubUserName = intent.getStringExtra(ParameterName.UserName.name)!!
        GithubReposViewModel.Factory(githubUserName)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.githubReposRecyclerView.adapter = githubReposGroupAdapter
        binding.githubReposRecyclerView.addOnBottomReached {
            githubReposViewModel.requestNext()
        }
        binding.swipeRefreshLayout.setOnRefreshListener {
            githubReposViewModel.refresh()
        }
        binding.retryButton.setOnClickListener {
            githubReposViewModel.retry()
        }

        lifecycleScope.launchWhenStarted {
            githubReposViewModel.reposStatus.collect { reposStatus ->
                val items = mutableListOf<Group>().apply {
                    this += createGithubRepoItems(reposStatus.githubRepos)
                    if (reposStatus.isNextLoading) this += createLoadingItem()
                    reposStatus.nextError?.let { this += createErrorItem(it) }
                }
                githubReposGroupAdapter.updateAsync(items)
            }
        }
        lifecycleScope.launchWhenStarted {
            githubReposViewModel.isMainLoading.collect {
                binding.progressBar.isVisible = it
            }
        }
        lifecycleScope.launchWhenStarted {
            githubReposViewModel.mainError.collect {
                binding.errorGroup.isVisible = (it != null)
                binding.errorTextView.text = it?.toString()
            }
        }
        lifecycleScope.launchWhenStarted {
            githubReposViewModel.isRefreshing.collect {
                binding.swipeRefreshLayout.isRefreshing = it
            }
        }
    }

    private fun createGithubRepoItems(githubRepos: List<GithubRepo>): List<GithubRepoItem> {
        return githubRepos.map { githubRepo ->
            GithubRepoItem(githubRepo).apply {
                onClick = { githubRepo -> launch(githubRepo.htmlUrl) }
            }
        }
    }

    private fun createLoadingItem(): LoadingItem {
        return LoadingItem()
    }

    private fun createErrorItem(exception: Exception): ErrorItem {
        return ErrorItem(exception).apply {
            onRetry = { githubReposViewModel.retryNext() }
        }
    }

    private fun launch(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }
}
