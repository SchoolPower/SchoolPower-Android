package com.carbonylgroup.schoolpower.fragments

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import com.carbonylgroup.schoolpower.R
import com.carbonylgroup.schoolpower.activities.MainActivity


class ChartFragment : Fragment() {

    private var mPager: ViewPager? = null
    private var mPagerAdapter: PagerAdapter? = null
    private val PagesNumber = 3

    private inner class ScreenSlidePagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {

        val titles = arrayOf(R.string.chart_line, R.string.chart_radar, R.string.chart_bar)

        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> LineChartFragment()
                1 -> RadarChartFragment()
                2 -> BarChartFragment()
                else -> LineChartFragment() // you shouldn't be here though
            }
        }

        override fun getPageTitle(position: Int) = activity!!.getString(titles[position])

        override fun getCount() = PagesNumber
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater!!.inflate(R.layout.fragment_viewpager, container, false)
        MainActivity.of(activity).expandToolBar(true, true)

        mPager = view.findViewById(R.id.chart_pager) as ViewPager
        mPagerAdapter = ScreenSlidePagerAdapter(fragmentManager!!)
        mPager!!.adapter = mPagerAdapter

        val tabLayout = view.findViewById(R.id.tab_layout) as TabLayout
        tabLayout.setupWithViewPager(mPager)

        return view
    }

}
