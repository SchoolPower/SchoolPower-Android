package com.carbonylgroup.schoolpower.fragments

import android.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.carbonylgroup.schoolpower.R
import com.carbonylgroup.schoolpower.utils.Utils


class DonationFragment : Fragment() {

    private lateinit var utils: Utils


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater!!.inflate(R.layout.fragment_donation, container, false)

        utils = Utils(activity)

        return view
    }
}
