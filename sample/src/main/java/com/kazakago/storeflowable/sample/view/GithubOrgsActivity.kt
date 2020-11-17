package com.kazakago.storeflowable.sample.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.google.android.material.snackbar.Snackbar
import com.kazakago.storeflowable.sample.databinding.ActivityGithubOrgsBinding
import com.kazakago.storeflowable.sample.model.GithubOrg
import com.kazakago.storeflowable.sample.view.items.ErrorItem
import com.kazakago.storeflowable.sample.view.items.GithubOrgItem
import com.kazakago.storeflowable.sample.view.items.LoadingItem
import com.kazakago.storeflowable.sample.viewmodel.GithubOrgsViewModel
import com.kazakago.storeflowable.sample.viewmodel.livedata.compositeLiveDataOf
import com.xwray.groupie.Group
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder

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
            githubOrgsViewModel.request()
        }
        binding.retryButton.setOnClickListener {
            githubOrgsViewModel.request()
        }
        compositeLiveDataOf(githubOrgsViewModel.githubOrgs, githubOrgsViewModel.isAdditionalLoading, githubOrgsViewModel.additionalError).observe(this) {
            val items: List<Group> = mutableListOf<Group>().apply {
                this += createGithubOrgItems(it.first)
                if (it.second) this += createLoadingItem()
                if (it.third != null) this += createErrorItem(it.third!!)
            }
            githubOrgsGroupAdapter.updateAsync(items)
        }
        githubOrgsViewModel.isMainLoading.observe(this) {
            binding.progressBar.isVisible = it
        }
        githubOrgsViewModel.mainError.observe(this) {
            binding.errorGroup.isVisible = (it != null)
            binding.errorTextView.text = it?.toString()
        }
        githubOrgsViewModel.hideSwipeRefresh.observe(this, "") {
            binding.swipeRefreshLayout.isRefreshing = false
        }
        githubOrgsViewModel.strongError.observe(this, "") {
            Snackbar.make(binding.root, it.toString(), Snackbar.LENGTH_SHORT).show()
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
