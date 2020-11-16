package com.kazakago.cacheflowable.sample.view

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.google.android.material.snackbar.Snackbar
import com.kazakago.cacheflowable.sample.R
import com.kazakago.cacheflowable.sample.databinding.ActivityGithubMetaBinding
import com.kazakago.cacheflowable.sample.viewmodel.GithubMetaViewModel

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
            githubMetaViewModel.request()
        }
        githubMetaViewModel.githubMeta.observe(this) {
            binding.md5RsaTextView.text = it?.sshKeyFingerprints?.md5Rsa?.let { "MD5_RSA\n${it}" }
            binding.md5DsaTextView.text = it?.sshKeyFingerprints?.md5Dsa?.let { "MD5_DSA\n${it}" }
            binding.sha256RsaTextView.text = it?.sshKeyFingerprints?.sha256Rsa?.let { "SHA256_RSA\n${it}" }
            binding.sha256DsaTextView.text = it?.sshKeyFingerprints?.sha256Dsa?.let { "SHA256_DSA\n${it}" }
        }
        githubMetaViewModel.isLoading.observe(this) {
            binding.progressBar.isVisible = it
        }
        githubMetaViewModel.error.observe(this) {
            binding.errorGroup.isVisible = (it != null)
            binding.errorTextView.text = it?.toString()
        }
        githubMetaViewModel.strongError.observe(this, "") {
            Snackbar.make(binding.root, it.toString(), Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_github_meta, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.refresh -> {
                githubMetaViewModel.request()
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
