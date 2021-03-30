package com.kazakago.storeflowable.example.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.kazakago.storeflowable.example.R
import com.kazakago.storeflowable.example.databinding.ActivityGithubMetaBinding
import com.kazakago.storeflowable.example.viewmodel.GithubMetaViewModel
import kotlinx.coroutines.flow.collect

class GithubMetaActivity : AppCompatActivity() {

    companion object {
        fun createIntent(context: Context): Intent {
            return Intent(context, GithubMetaActivity::class.java)
        }
    }

    private val binding by lazy { ActivityGithubMetaBinding.inflate(layoutInflater) }
    private val githubMetaViewModel by viewModels<GithubMetaViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.retryButton.setOnClickListener {
            githubMetaViewModel.retry()
        }

        lifecycleScope.launchWhenStarted {
            githubMetaViewModel.githubMeta.collect { meta ->
                binding.sha256RsaTextView.text = meta?.sshKeyFingerprints?.sha256Rsa?.let { "SHA256_RSA\n$it" }
                binding.sha256DsaTextView.text = meta?.sshKeyFingerprints?.sha256Dsa?.let { "SHA256_DSA\n$it" }
            }
        }
        lifecycleScope.launchWhenStarted {
            githubMetaViewModel.isLoading.collect {
                binding.progressBar.isVisible = it
            }
        }
        lifecycleScope.launchWhenStarted {
            githubMetaViewModel.error.collect {
                binding.errorGroup.isVisible = (it != null)
                binding.errorTextView.text = it?.toString()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_github_meta, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.refresh -> {
                githubMetaViewModel.refresh()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
