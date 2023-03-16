package com.umtualgames.squadmaster.ui.slide

import com.umtualgames.squadmaster.ui.base.BaseFragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import com.umtualgames.squadmaster.R
import com.umtualgames.squadmaster.data.models.SlideModel
import com.umtualgames.squadmaster.databinding.FragmentSlideBinding
import com.umtualgames.squadmaster.ui.main.MainActivity
import com.google.android.material.tabs.TabLayoutMediator

class SlideFragment : BaseFragment() {

    private val binding by lazy { FragmentSlideBinding.inflate(layoutInflater) }

    private val slidePagerAdapter by lazy { SlidePagerAdapter() }

    private val list: ArrayList<SlideModel> = arrayListOf()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewPager()
        binding.apply {
            btnStartGame.setOnClickListener {
                goToMain()
            }
        }

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (position == list.lastIndex) {
                    binding.btnStartGame.visibility = View.VISIBLE
                } else {
                    binding.btnStartGame.visibility = View.GONE
                }
            }
        })

        list.add(SlideModel(getString(R.string.slide_first_main_mode), R.drawable.screen_main_mode))
        list.add(SlideModel(getString(R.string.slide_two_league_mode), R.drawable.screen_league_mode))
        list.add(SlideModel(getString(R.string.slide_third_score), R.drawable.screen_score))
        slidePagerAdapter.updateAdapter(list)
    }

    private fun goToMain() { startActivity(MainActivity.createIntent(requireContext())) }

    private fun setupViewPager() {
        with(binding) {
            viewPager.apply {
                adapter = slidePagerAdapter
            }
            TabLayoutMediator(tabLayout, viewPager) { _, _ -> }.attach()
        }
    }
}