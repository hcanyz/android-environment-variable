package com.hcanyz.environmentvariable.demo

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.hcanyz.environmentvariable.setting.ui.EvSwitchActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }

    fun setting(view: View) {
        EvSwitchActivity.skip(
            view.context,
            arrayListOf(EvHttpManager::class.java, Ev3rdPushInfoManager::class.java)
        )
    }
}