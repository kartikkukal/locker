package com.kevlar.locker.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.view.size
import androidx.fragment.app.Fragment
import com.kevlar.locker.IntroductionActivity
import com.kevlar.locker.R

class IntroductionFragmentWelcome: Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val layout = inflater.inflate(R.layout.introduction_fragment_welcome, container, false)
        val activity = (requireActivity() as IntroductionActivity)

        layout.findViewById<Button>(R.id.to_setup).setOnClickListener {
            if(activity.introductionPager.size >= (activity.introductionPager.currentItem + 1)) {
                activity.introductionPager.setCurrentItem(activity.introductionPager.currentItem + 1, true)
            }
        }

        return layout
    }
}