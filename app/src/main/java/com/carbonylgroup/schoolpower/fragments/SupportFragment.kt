package com.carbonylgroup.schoolpower.fragments

import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.carbonylgroup.schoolpower.R
import com.carbonylgroup.schoolpower.activities.MainActivity


class SupportFragment : Fragment() {

    private var mPager: ViewPager? = null
    private var mPagerAdapter: PagerAdapter? = null
    private val PagesNumber = 2

    private inner class ScreenSlidePagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {

        val titles = arrayOf(R.string.promotion, R.string.donation)

        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> PromotionFragment()
                1 -> DonationFragment()
                else -> PromotionFragment() // you shouldn't be here though
            }
        }

        override fun getPageTitle(position: Int) = activity!!.getString(titles[position])

        override fun getCount() = PagesNumber
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_viewpager, container, false)
        (activity as? MainActivity)?.expandToolBar(true, true)

        // Instantiate a ViewPager and a PagerAdapter.
        mPager = view.findViewById(R.id.chart_pager) as ViewPager
        mPagerAdapter = ScreenSlidePagerAdapter(fragmentManager!!)
        mPager!!.adapter = mPagerAdapter

        if (arguments?.getBoolean("donation", false) == true) {
            mPager!!.setCurrentItem(2, true)
        }

        val tabLayout = view.findViewById(R.id.tab_layout) as TabLayout
        tabLayout.setupWithViewPager(mPager)

        return view
    }
}
