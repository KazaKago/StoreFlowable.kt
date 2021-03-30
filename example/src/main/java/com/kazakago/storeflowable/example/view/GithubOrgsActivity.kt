package com.kazakago.storeflowable.example.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.kazakago.storeflowable.example.databinding.ActivityGithubOrgsBinding
import com.kazakago.storeflowable.example.model.GithubOrg
import com.kazakago.storeflowable.example.view.items.ErrorItem
import com.kazakago.storeflowable.example.view.items.GithubOrgItem
import com.kazakago.storeflowable.example.view.items.LoadingItem
import com.kazakago.storeflowable.example.viewmodel.GithubOrgsViewModel
import com.xwray.groupie.Group
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine

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
            githubOrgsViewModel.requestAdditional()
        }
        binding.swipeRefreshLayout.setOnRefreshListener {
            githubOrgsViewModel.refresh()
        }
        binding.retryButton.setOnClickListener {
            githubOrgsViewModel.retry()
        }

        lifecycleScope.launchWhenStarted {
            combine(githubOrgsViewModel.githubOrgs, githubOrgsViewModel.isAdditionalLoading, githubOrgsViewModel.additionalError) { a, b, c -> Triple(a, b, c) }.collect {
                val items: List<Group> = mutableListOf<Group>().apply {
                    this += createGithubOrgItems(it.first)
                    if (it.second) this += createLoadingItem()
                    if (it.third != null) this += createErrorItem(it.third!!)
                }
                githubOrgsGroupAdapter.updateAsync(items)
            }
        }
        lifecycleScope.launchWhenStarted {
            githubOrgsViewModel.isMainLoading.collect {
                binding.progressBar.isVisible = it
            }
        }
        lifecycleScope.launchWhenStarted {
            githubOrgsViewModel.mainError.collect {
                binding.errorGroup.isVisible = (it != null)
                binding.errorTextView.text = it?.toString()
            }
        }
        lifecycleScope.launchWhenStarted {
            githubOrgsViewModel.isRefreshing.collect {
                binding.swipeRefreshLayout.isRefreshing = it
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
            onRetry = { githubOrgsViewModel.retryAdditional() }
        }
    }
}
