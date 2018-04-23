package com.carbonylgroup.schoolpower.fragments

import android.app.Fragment
import android.app.FragmentManager
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v13.app.FragmentStatePagerAdapter
import android.support.v4.view.PagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.carbonylgroup.schoolpower.R
import com.carbonylgroup.schoolpower.activities.MainActivity
import android.support.v4.view.ViewPager


class ChartFragment : Fragment() {

    private var mPager: ViewPager? = null
    private var mPagerAdapter: PagerAdapter? = null
    private val PagesNumber = 2

    private inner class ScreenSlidePagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {

        val titles = arrayOf(R.string.chart_line, R.string.chart_radar)

        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> LineChartFragment()
                1 -> RadarChartFragment()
                else -> LineChartFragment()
            }
        }

        override fun getPageTitle(position: Int) = activity.getString(titles[position])

        override fun getCount() = PagesNumber
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater!!.inflate(R.layout.fragment_chart, container, false)
        MainActivity.of(activity).expandToolBar(true, true)

        // Instantiate a ViewPager and a PagerAdapter.
        mPager = view.findViewById(R.id.chart_pager) as ViewPager
        mPagerAdapter = ScreenSlidePagerAdapter(fragmentManager)
        mPager!!.adapter = mPagerAdapter

        val tabLayout = view.findViewById(R.id.tab_layout) as TabLayout
        tabLayout.setupWithViewPager(mPager)
        return view
    }

}
