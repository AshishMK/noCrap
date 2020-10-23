package com.x.nocrap.scenes.homeScene

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.x.nocrap.R
import com.x.nocrap.databinding.WalkThroughActivityBinding

class WalkThroughActivity : AppCompatActivity() {
    /**
     * I am using Data binding
     * */
    private lateinit var binding: WalkThroughActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_walk_through)

    }

    fun onFinishClick(view: View) {
        finish()
    }

}