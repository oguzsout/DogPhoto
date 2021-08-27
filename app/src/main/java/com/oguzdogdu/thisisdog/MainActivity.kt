package com.oguzdogdu.thisisdog

import android.graphics.drawable.AnimationDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import com.bumptech.glide.Glide
import com.oguzdogdu.thisisdog.databinding.ActivityMainBinding
import com.oguzdogdu.thisisdog.service.ApiRequest
import com.oguzdogdu.thisisdog.service.BASE_URL
import kotlinx.coroutines.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    @DelicateCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Keeps the phone in light mode

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        // Starts up the background transitions
        backgroundAnimation()

        // Makes an API request as soon as the app starts
        makeApiRequest()


        binding.floatingActionButton.setOnClickListener {

            // FAB rotate animation
            binding.floatingActionButton.animate().apply {
                rotationBy(360f)
                duration = 1000
            }.start()

            makeApiRequest()
            binding.ivRandomDog.visibility = View.GONE

        }
    }
        private fun backgroundAnimation() {
            val animationDrawable: AnimationDrawable = binding.rlLayout.background as AnimationDrawable
            animationDrawable.apply {
                setEnterFadeDuration(1000)
                setExitFadeDuration(3000)
                start()
            }
        }

        @DelicateCoroutinesApi
        private fun makeApiRequest() {
            val api = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiRequest::class.java)

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = api.getRandomDog()
                    Log.d("Main", "Size: ${response.fileSizeBytes}")

                    //If the image is less than about 0.4mb, then we try to load it into our app, else we try again.
                    if (response.fileSizeBytes < 400_000) {
                        withContext(Dispatchers.Main) {
                            Glide.with(applicationContext).load(response.url).into(binding.ivRandomDog)
                            binding.ivRandomDog.visibility = View.VISIBLE
                        }
                    } else {
                        makeApiRequest()
                    }

                } catch (e: Exception) {
                    Log.e("Main", "Error: ${e.message}")
                }
            }
        }

    }


