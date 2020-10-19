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
import coil.api.load
import com.google.android.material.snackbar.Snackbar
import com.kazakago.cacheflowable.sample.R
import com.kazakago.cacheflowable.sample.viewmodel.GithubUserViewModel
import kotlinx.android.synthetic.main.activity_github_user.*

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

    private val githubUserViewModel by viewModels<GithubUserViewModel> {
        val githubUserName = intent.getStringExtra(ParameterName.UserName.name)!!
        GithubUserViewModel.Factory(application, githubUserName)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_github_user)

        linkTextView.setOnClickListener {
            githubUserViewModel.githubUser.value?.let { launch(it.htmlUrl) }
        }
        retryButton.setOnClickListener {
            githubUserViewModel.request()
        }
        githubUserViewModel.githubUser.observe(this) {
            avatarImageView.load(it?.avatarUrl)
            idTextView.text = it?.id?.let { id -> "ID: $id" }
            nameTextView.text = it?.name
            linkTextView.text = it?.htmlUrl
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

    private fun launch(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }
}
