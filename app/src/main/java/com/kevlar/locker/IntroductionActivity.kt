package com.kevlar.locker

import android.os.Bundle
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.kevlar.locker.adapters.IntroductionSplashAdapter
import com.kevlar.locker.fragments.IntroductionFragmentSetup
import com.kevlar.locker.fragments.IntroductionFragmentWelcome

class IntroductionActivity : FragmentActivity() {

    lateinit var introductionPager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.introduction_activity)

        this.introductionPager = findViewById(R.id.introduction_pager)
        val tabLayout = findViewById<TabLayout>(R.id.dot_layout)

        val fragmentList: ArrayList<Fragment> = ArrayList()

        fragmentList.add(IntroductionFragmentWelcome())
        fragmentList.add(IntroductionFragmentSetup())

        val adapter = IntroductionSplashAdapter(fragmentList, supportFragmentManager, lifecycle)

        introductionPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        introductionPager.adapter = adapter

        TabLayoutMediator(tabLayout, introductionPager){ _, _ -> }.attach()
    }
}