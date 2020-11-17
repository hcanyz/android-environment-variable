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
        fun skip(context: Context, evGroupClassList: ArrayList<Class<out IEvManager>>) {
            context.startActivity(Intent(context, EvSwitchActivity::class.java).apply {
                putExtra("evGroupClassList", evGroupClassList)
            })
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ev_main)

        val lists: ArrayList<Class<IEvManager>> =
            intent.getSerializableExtra("evGroupClassList") as ArrayList<Class<IEvManager>>

        ev_viewpager.adapter = object : FragmentStatePagerAdapter(
            supportFragmentManager,
            BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
        ) {
            override fun getCount(): Int {
                return lists.size
            }

            override fun getItem(position: Int): Fragment {
                return EvSwitchFragment.newInstance(lists[position])
            }

            override fun getPageTitle(position: Int): CharSequence? {
                return lists[position].simpleName
            }
        }
    }
}