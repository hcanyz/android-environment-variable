package com.hcanyz.environmentvariable.demo

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.hcanyz.environmentvariable.setting.ui.EvSwitchActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }

    fun setting(view: View) {
        Toast.makeText(
            this,
            EvHttpManager.getSingleton().getEvItemCurrentValue(EvHttpManager.EV_ITEM_SERVERURL),
            Toast.LENGTH_LONG
        ).show()
        EvSwitchActivity.skip(
            view.context,
            arrayListOf(EvHttpManager::class.java),
            arrayListOf(Ev3rdPushInfoManager::class.java.name)
        )
    }
}