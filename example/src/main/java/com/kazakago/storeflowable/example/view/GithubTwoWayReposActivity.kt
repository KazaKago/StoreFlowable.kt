package com.kazakago.storeflowable.example.view

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.kazakago.storeflowable.example.databinding.ActivityGithubTwoWayReposBinding
import com.kazakago.storeflowable.example.model.GithubRepo
import com.kazakago.storeflowable.example.view.items.ErrorItem
import com.kazakago.storeflowable.example.view.items.GithubRepoItem
import com.kazakago.storeflowable.example.view.items.LoadingItem
import com.kazakago.storeflowable.example.viewmodel.GithubTwoWayReposViewModel
import com.xwray.groupie.Group
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.coroutines.flow.collect

class GithubTwoWayReposActivity : AppCompatActivity() {

    companion object {
        fun createIntent(context: Context): Intent {
            return Intent(context, GithubTwoWayReposActivity::class.java)
        }
    }

    private val binding by lazy { ActivityGithubTwoWayReposBinding.inflate(layoutInflater) }
    private val githubReposGroupAdapter = GroupAdapter<GroupieViewHolder>()
    private val githubReposViewModel by viewModels<GithubTwoWayReposViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.githubReposRecyclerView.adapter = githubReposGroupAdapter
        binding.githubReposRecyclerView.addOnTopReached {
            githubReposViewModel.requestPrepending()
        }
        binding.githubReposRecyclerView.addOnBottomReached {
            githubReposViewModel.requestAppending()
        }
        binding.retryButton.setOnClickListener {
            githubReposViewModel.retry()
        }

        lifecycleScope.launchWhenStarted {
            githubReposViewModel.reposStatus.collect { reposStatus ->
                val items: List<Group> = mutableListOf<Group>().apply {
                    this += createGithubRepoItems(reposStatus.githubRepos)
                    if (reposStatus.isAppendingLoading) add(createLoadingItem())
                    if (reposStatus.isPrependingLoading) add(0, createLoadingItem())
                    reposStatus.appendingError?.let { add(createAppendingErrorItem(it)) }
                    reposStatus.prependingError?.let { add(0, createPrependingErrorItem(it)) }
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

    private fun createAppendingErrorItem(exception: Exception): ErrorItem {
        return ErrorItem(exception).apply {
            onRetry = { githubReposViewModel.retryAppending() }
        }
    }

    private fun createPrependingErrorItem(exception: Exception): ErrorItem {
        return ErrorItem(exception).apply {
            onRetry = { githubReposViewModel.retryPrepending() }
        }
    }

    private fun launch(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }
}
