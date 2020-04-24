package com.kazakago.cachesample.presentation.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.observe
import coil.api.load
import com.google.android.material.snackbar.Snackbar
import com.kazakago.cachesample.R
import com.kazakago.cachesample.presentation.viewmodel.GithubUserViewModel
import com.kazakago.cachesample.presentation.viewmodel.livedata.observe
import kotlinx.android.synthetic.main.activity_github_user.*
import java.net.URL

class GithubUserActivity : AppCompatActivity() {

    private val githubUserViewModel by viewModels<GithubUserViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_github_user)

        linkTextView.setOnClickListener {
            githubUserViewModel.githubUser.value?.let { launch(it.url) }
        }
        retryButton.setOnClickListener {
            githubUserViewModel.request()
        }
        githubUserViewModel.githubUser.observe(this) {
            avatarImageView.load(it?.avatarUrl?.toString())
            idTextView.text = it?.id?.value?.let { id -> "ID: $id" }
            nameTextView.text = it?.name
            linkTextView.text = it?.url?.toString()
        }
        githubUserViewModel.isLoading.observe(this) {
            progressBar.isVisible = it
        }
        githubUserViewModel.error.observe(this) {
            errorGroup.isVisible = (it != null)
            errorTextView.text = it?.toString()
        }
        githubUserViewModel.strongError.observe(this, "") {
            Snackbar.make(rootView, it.toString(), Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_github_user, menu);
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.refresh -> {
                githubUserViewModel.request()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun launch(url: URL) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url.toString()))
        startActivity(intent)
    }
}
