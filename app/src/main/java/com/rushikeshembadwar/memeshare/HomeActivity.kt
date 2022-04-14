package com.rushikeshembadwar.memeshare

import android.content.Intent
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import com.rushikeshembadwar.memeshare.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    private var backPressedTime : Long = 0
    private var backToast: Toast?= null
    private var currentImageUrl: String? = null

    private lateinit var manager: ReviewManager
    private var reviewInfo: ReviewInfo? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initReviews()

        loadMeme()

        val button1: Button = findViewById(R.id.nextButton)

        button1.setOnClickListener {
            loadMeme()
        }

        val button2: Button = findViewById(R.id.shareButton)

        button2.setOnClickListener {
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_TEXT, "Check this cool meme i got $currentImageUrl")
            val chooser = Intent.createChooser(intent, "Share this meme using: ")
            startActivity(chooser)
        }
    }

    override fun onBackPressed() {
        askForReview()
        if(backPressedTime + 2000 > System.currentTimeMillis()){
            backToast?.cancel()
            super.onBackPressed()
            return
        }
        else{
            backToast = Toast.makeText(baseContext, "Press again to exit app", Toast.LENGTH_SHORT)
            backToast?.show()
        }
        backPressedTime = System.currentTimeMillis()
    }

    private fun loadMeme(){

        val progress: ProgressBar = findViewById(R.id.proBar)
        progress.visibility = View.VISIBLE

// Instantiate the RequestQueue.
        val image1: ImageView = findViewById(R.id.memeImage)
        val url = "https://meme-api.herokuapp.com/gimme"

// Request a string response from the provided URL.
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->

                currentImageUrl = response.getString("url")
                Glide.with(this).load(currentImageUrl).listener(object : RequestListener<Drawable> {

                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {

                        progress.visibility = View.GONE
                        return false

                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {

                        progress.visibility = View.GONE
                        return false

                    }

                }).into(image1)

            },
            {
                Toast.makeText(this, "no internet connection", Toast.LENGTH_LONG).show()
            })

// Access the RequestQueue through your singleton class.
        MySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest)
    }

    private fun initReviews() {
        manager = ReviewManagerFactory.create(this)
        manager.requestReviewFlow().addOnCompleteListener { request ->
            if (request.isSuccessful) {
                reviewInfo = request.result
            } else {
                // Log error
            }
        }
    }


    private fun askForReview() {
        if (reviewInfo != null) {
            manager.launchReviewFlow(this, reviewInfo!!).addOnFailureListener {
                // Log error and continue with the flow
            }.addOnCompleteListener {
                // Log success and continue with the flow
            }
        }
    }


}