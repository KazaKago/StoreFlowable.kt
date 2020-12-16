package com.kazakago.storeflowable.sample.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.kazakago.storeflowable.sample.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.metaButton.setOnClickListener {
            val intent = GithubMetaActivity.createIntent(this)
            startActivity(intent)
        }
        binding.orgsButton.setOnClickListener {
            val intent = GithubOrgsActivity.createIntent(this)
            startActivity(intent)
        }
        binding.reposButton.setOnClickListener {
            val intent = GithubReposActivity.createIntent(this, binding.userNameInputEditText.text.toString())
            startActivity(intent)
        }
        binding.userButton.setOnClickListener {
            val intent = GithubUserActivity.createIntent(this, binding.userNameInputEditText.text.toString())
            startActivity(intent)
        }
    }
}
