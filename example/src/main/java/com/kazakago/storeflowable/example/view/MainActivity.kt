package com.kazakago.storeflowable.example.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.kazakago.storeflowable.example.databinding.ActivityMainBinding

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
        binding.userButton.setOnClickListener {
            val intent = GithubUserActivity.createIntent(this, binding.userName1InputEditText.text.toString())
            startActivity(intent)
        }
        binding.reposButton.setOnClickListener {
            val intent = GithubReposActivity.createIntent(this, binding.userName2InputEditText.text.toString())
            startActivity(intent)
        }
        binding.repos2Button.setOnClickListener {
            val intent = GithubTwoWayReposActivity.createIntent(this)
            startActivity(intent)
        }
    }
}
