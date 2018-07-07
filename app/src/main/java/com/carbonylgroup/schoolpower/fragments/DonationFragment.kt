package com.carbonylgroup.schoolpower.fragments

import android.app.Fragment
import android.didikee.donate.AlipayDonate
import android.didikee.donate.WeiXinDonate
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import co.ceryle.segmentedbutton.SegmentedButtonGroup
import com.carbonylgroup.schoolpower.R
import com.carbonylgroup.schoolpower.utils.Utils
import net.glxn.qrgen.android.QRCode


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

        val qrCode = view.findViewById(R.id.qr_code) as ImageView

        val segmented: SegmentedButtonGroup = view.findViewById(R.id.donation_segmented)

        segmented.setPosition(0, 0)

        val callback = { position: Int ->
            when (position) {
                0 -> qrCode.setImageBitmap(generateQRCode(AlipayQR).bitmap())
                1 -> qrCode.setImageBitmap(generateQRCode(WeChatQR).bitmap())
            }
        }

        segmented.setOnClickedButtonPosition(callback)
        callback(segmented.position)

        view.findViewById<Button>(R.id.donate_button).setOnClickListener {
            if(segmented.position==0) {
                if (AlipayDonate.hasInstalledAlipayClient(activity)) {
                    AlipayDonate.startAlipayClient(activity, AlipayToken)
                } else {
                    utils.showSnackBar(view.findViewById(R.id.donation_fragment), getString(R.string.AlipayNotFound), true)
                }
            }else{
                if (WeiXinDonate.hasInstalledWeiXinClient(activity)) {
                    val qrPath = Environment.getExternalStorageDirectory().absolutePath + "tmp/sp_wechat.png"
                    generateQRCode(WeChatQR).file(qrPath)
                    WeiXinDonate.donateViaWeiXin(activity, qrPath)
                }else{
                    utils.showSnackBar(view.findViewById(R.id.donation_fragment), getString(R.string.WechatNotFound), true)
                }
            }
        }
        return view
    }
}
