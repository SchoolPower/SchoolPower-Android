package com.carbonylgroup.schoolpower.fragments

import android.content.Context
import android.os.Bundle
import android.os.Handler
import com.google.android.material.tabs.TabLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import android.util.Log
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
    private val titles = arrayOf(R.string.chart_line, R.string.chart_radar, R.string.chart_bar)
    private val icons = arrayOf(
            R.drawable.ic_line_chart_black_24dp,
            R.drawable.ic_radar_chart_24px,
            R.drawable.ic_bar_chart_24px
    )

    private inner class ScreenSlidePagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {


        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> LineChartFragment()
                1 -> RadarChartFragment()
                2 -> BarChartFragment()
                else -> LineChartFragment() // you shouldn't be here though
            }
        }

        override fun getPageTitle(position: Int) = ""

        override fun getCount() = PagesNumber
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_viewpager, container, false)
        MainActivity.of(activity).expandToolBar(true, true)

        mPager = view.findViewById(R.id.chart_pager) as ViewPager
        mPagerAdapter = ScreenSlidePagerAdapter(fragmentManager!!)
        mPager!!.adapter = mPagerAdapter

        val tabLayout = view.findViewById(R.id.tab_layout) as TabLayout
        tabLayout.setupWithViewPager(mPager)

        for (i in 0 until tabLayout.tabCount) {
            tabLayout.getTabAt(i)!!.setIcon(icons[i])
        }

        return view
    }

}
