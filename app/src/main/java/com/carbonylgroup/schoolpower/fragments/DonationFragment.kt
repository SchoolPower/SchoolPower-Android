package com.carbonylgroup.schoolpower.fragments

import android.app.Fragment
import android.content.Context
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

//        val segmented: SegmentedButtonGroup = view.findViewById(R.id.donation_segmented)
//
//        segmented.setPosition(0, 0)
//
//        val callback = { position: Int ->
//            when (position) {
//                0 -> qrCode.setImageBitmap(generateQRCode(AlipayQR).bitmap())
//                1 -> qrCode.setImageBitmap(generateQRCode(WeChatQR).bitmap())
//            }
//        }

        fun gotoAlipay() {
            if (AlipayDonate.hasInstalledAlipayClient(activity)) {
                AlipayDonate.startAlipayClient(activity, AlipayToken)
            } else {
                utils.showSnackBar(view.findViewById(R.id.donation_fragment), getString(R.string.AlipayNotFound), true)
            }
        }

        fun gotoWechatPay() {
            if (WeiXinDonate.hasInstalledWeiXinClient(activity)) {
                val weixinQrIs = resources.openRawResource(R.raw.sp_wechat)
                val qrPath = Environment.getExternalStorageDirectory().absolutePath + File.separator +
                        "SchoolPowerDonate" + File.separator + "sp_wechat.png"
                WeiXinDonate.saveDonateQrImage2SDCard(qrPath, BitmapFactory.decodeStream(weixinQrIs))
                WeiXinDonate.donateViaWeiXin(activity, qrPath)
            }else{
                utils.showSnackBar(view.findViewById(R.id.donation_fragment), getString(R.string.WechatNotFound), true)
            }
        }
        return view
    }
}
