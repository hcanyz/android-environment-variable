package com.hcanyz.environmentvariable.setting.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import com.hcanyz.environmentvariable.IEvManager
import com.hcanyz.environmentvariable.setting.R
import kotlinx.android.synthetic.main.activity_ev_main.*

class EvSwitchActivity : AppCompatActivity() {

    companion object {
        fun skip(
            context: Context,
            evGroupClassList: ArrayList<Class<out IEvManager>> = arrayListOf(),
            evGroupClassNameList: ArrayList<String> = arrayListOf()
        ) {
            context.startActivity(Intent(context, EvSwitchActivity::class.java).apply {
                putExtra("evGroupClassList", evGroupClassList)
                putStringArrayListExtra("evGroupClassNameList", evGroupClassNameList)
            })
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ev_main)

        val lists: ArrayList<Class<IEvManager>> =
            intent.getSerializableExtra("evGroupClassList") as ArrayList<Class<IEvManager>>

        val lists2: ArrayList<String> =
            intent.getStringArrayListExtra("evGroupClassNameList") as ArrayList<String>

        val data = lists.apply {
            addAll(lists2.map { Class.forName(it) as Class<IEvManager> })
        }

        ev_viewpager.adapter = object : FragmentStatePagerAdapter(
            supportFragmentManager,
            BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
        ) {
            override fun getCount(): Int {
                return data.size
            }

            override fun getItem(position: Int): Fragment {
                return EvSwitchFragment.newInstance(data[position])
            }

            override fun getPageTitle(position: Int): CharSequence? {
                return data[position].simpleName
            }
        }
    }
}