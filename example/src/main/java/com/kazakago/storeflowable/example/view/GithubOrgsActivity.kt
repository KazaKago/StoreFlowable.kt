package com.kazakago.storeflowable.example.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.kazakago.storeflowable.example.databinding.ActivityGithubOrgsBinding
import com.kazakago.storeflowable.example.model.GithubOrg
import com.kazakago.storeflowable.example.view.items.ErrorItem
import com.kazakago.storeflowable.example.view.items.GithubOrgItem
import com.kazakago.storeflowable.example.view.items.LoadingItem
import com.kazakago.storeflowable.example.viewmodel.GithubOrgsViewModel
import com.xwray.groupie.Group
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.coroutines.launch

class GithubOrgsActivity : AppCompatActivity() {

    companion object {
        fun createIntent(context: Context): Intent {
            return Intent(context, GithubOrgsActivity::class.java)
        }
    }

    private val binding by lazy { ActivityGithubOrgsBinding.inflate(layoutInflater) }
    private val githubOrgsGroupAdapter = GroupAdapter<GroupieViewHolder>()
    private val githubOrgsViewModel by viewModels<GithubOrgsViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.githubOrgsRecyclerView.adapter = githubOrgsGroupAdapter
        binding.githubOrgsRecyclerView.addOnBottomReached {
            githubOrgsViewModel.requestNext()
        }
        binding.swipeRefreshLayout.setOnRefreshListener {
            githubOrgsViewModel.refresh()
        }
        binding.retryButton.setOnClickListener {
            githubOrgsViewModel.retry()
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    githubOrgsViewModel.orgsStatus.collect { reposStatus ->
                        val items = mutableListOf<Group>().apply {
                            this += createGithubOrgItems(reposStatus.githubOrgs)
                            if (reposStatus.isNextLoading) this += createLoadingItem()
                            reposStatus.nextError?.let { this += createErrorItem(it) }
                        }
                        githubOrgsGroupAdapter.updateAsync(items)
                    }
                }
                launch {
                    githubOrgsViewModel.isMainLoading.collect {
                        binding.progressBar.isVisible = it
                    }
                }
                launch {
                    githubOrgsViewModel.mainError.collect {
                        binding.errorGroup.isVisible = (it != null)
                        binding.errorTextView.text = it?.toString()
                    }
                }
                launch {
                    githubOrgsViewModel.isRefreshing.collect {
                        binding.swipeRefreshLayout.isRefreshing = it
                    }
                }
            }
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
            onRetry = { githubOrgsViewModel.retryNext() }
        }
    }
}
