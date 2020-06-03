package com.kazakago.cacheflowable.sample.presentation.view

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.kazakago.cacheflowable.sample.R
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        reposButton.setOnClickListener {
            startActivity(Intent(this, GithubReposActivity::class.java))
        }
        userButton.setOnClickListener {
            startActivity(Intent(this, GithubUserActivity::class.java))
        }
    }

}
