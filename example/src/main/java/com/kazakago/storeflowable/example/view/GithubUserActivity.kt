package com.kazakago.storeflowable.example.view

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import coil.load
import com.kazakago.storeflowable.example.R
import com.kazakago.storeflowable.example.databinding.ActivityGithubUserBinding
import com.kazakago.storeflowable.example.viewmodel.GithubUserViewModel

class GithubUserActivity : AppCompatActivity() {

    companion object {
        fun createIntent(context: Context, userName: String): Intent {
            return Intent(context, GithubUserActivity::class.java).apply {
                putExtra(ParameterName.UserName.name, userName)
            }
        }
    }

    private enum class ParameterName {
        UserName
    }

    private val binding by lazy { ActivityGithubUserBinding.inflate(layoutInflater) }
    private val githubUserViewModel by viewModels<GithubUserViewModel> {
        val githubUserName = intent.getStringExtra(ParameterName.UserName.name)!!
        GithubUserViewModel.Factory(application, githubUserName)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.linkTextView.setOnClickListener {
            githubUserViewModel.githubUser.value?.let { launch(it.htmlUrl) }
        }
        binding.retryButton.setOnClickListener {
            githubUserViewModel.retry()
        }
        githubUserViewModel.githubUser.observe(this) {
            binding.avatarImageView.load(it?.avatarUrl)
            binding.idTextView.text = it?.id?.let { id -> "ID: $id" }
            binding.nameTextView.text = it?.name
            binding.linkTextView.text = it?.htmlUrl
        }
        githubUserViewModel.isLoading.observe(this) {
            binding.progressBar.isVisible = it
        }
        githubUserViewModel.error.observe(this) {
            binding.errorGroup.isVisible = (it != null)
            binding.errorTextView.text = it?.toString()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_github_user, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.refresh -> {
                githubUserViewModel.refresh()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun launch(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }
}
