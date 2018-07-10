package com.carbonylgroup.schoolpower.fragments

import android.app.Fragment
import android.content.Context
import android.content.Intent
import android.didikee.donate.AlipayDonate
import android.didikee.donate.WeiXinDonate
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.carbonylgroup.schoolpower.R
import com.carbonylgroup.schoolpower.utils.Utils
import net.glxn.qrgen.android.QRCode
import java.io.File
import android.graphics.BitmapFactory
import android.support.transition.TransitionManager
import android.support.v7.widget.CardView
import android.widget.Button
import android.widget.LinearLayout
import com.carbonylgroup.schoolpower.activities.LoginActivity
import com.carbonylgroup.schoolpower.activities.WechatIntroActivity


class DonationFragment : Fragment() {

    private lateinit var utils: Utils

    private fun generateQRCode(url: String) = QRCode.from(url)
            .withSize(utils.dpToPx(250), utils.dpToPx(250))
            .withColor(utils.getPrimaryColor(), 0xFFFFFFFF.toInt())

    private val AlipayToken = "tsx09230fuwngogndwbkg3b"
    private val AlipayQR = "https://qr.alipay.com/$AlipayToken"
    private val WeChatQR = "wxp://f2f0cTQAZ3_D0IW8psZSCq0EhuJcUIJcr0hE"

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater!!.inflate(R.layout.fragment_donation, container, false)
        utils = Utils(activity)

//                0 -> qrCode.setImageBitmap(generateQRCode(AlipayQR).bitmap())
//                1 -> qrCode.setImageBitmap(generateQRCode(WeChatQR).bitmap())

        view.findViewById<CardView>(R.id.alipay_card).setOnClickListener { gotoAlipay() }
        view.findViewById<CardView>(R.id.wechat_card).setOnClickListener { gotoWechatPay() }
        return view
    }

    fun gotoAlipay() {
        if (AlipayDonate.hasInstalledAlipayClient(activity)) {
            AlipayDonate.startAlipayClient(activity, AlipayToken)
        } else {
            utils.showSnackBar(view.findViewById(R.id.donation_fragment), getString(R.string.AlipayNotFound), true)
        }
    }

    fun gotoWechatPay() {
        startActivity(Intent(activity, WechatIntroActivity::class.java))

    }
}
