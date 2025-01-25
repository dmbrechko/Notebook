package com.example.notebook

import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.notebook.databinding.ActivityMainBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.goBTN.setOnClickListener {
            startActivity(Intent(this@MainActivity, NoteActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        binding.apply {
            val imageAnim = AnimationUtils.loadAnimation(this@MainActivity, R.anim.from_top)
            imageIV.startAnimation(imageAnim)
            val textAnim = AnimationUtils.loadAnimation(this@MainActivity, R.anim.from_right)
            textTV.startAnimation(textAnim)
            val buttonAnim = AnimationUtils.loadAnimation(this@MainActivity, R.anim.from_bottom)
            goBTN.startAnimation(buttonAnim)
        }
    }
}