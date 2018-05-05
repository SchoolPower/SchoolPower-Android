package com.carbonylgroup.schoolpower.fragments

import android.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import co.ceryle.segmentedbutton.SegmentedButtonGroup
import com.carbonylgroup.schoolpower.R
import com.carbonylgroup.schoolpower.utils.Utils
import net.glxn.qrgen.android.QRCode


class PromotionFragment : Fragment() {

    private lateinit var utils: Utils

    private fun generateQRCode(url: String) = QRCode.from(url)
            .withSize(utils.dpToPx(250), utils.dpToPx(250))
            .withColor(0xff09314b.toInt(), 0xfffffffff.toInt())
            .bitmap()!!

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater!!.inflate(R.layout.fragment_promotion, container, false)

        utils = Utils(activity)

        val androidDownloadUrl = utils.getSharedPreference(Utils.OtherData).getString("app_download_url", getString(R.string.default_android_download_address))

        val qrCode = view.findViewById(R.id.qr_code) as ImageView

        val segmented: SegmentedButtonGroup = view.findViewById(R.id.promotion_segmented)

        segmented.setPosition(0, 0)

        val callback = { position: Int ->
            when (position) {
                0 -> qrCode.setImageBitmap(generateQRCode(androidDownloadUrl))
                1 -> qrCode.setImageBitmap(generateQRCode(getString(R.string.ios_download_address)))
            }
        }

        segmented.setOnClickedButtonPosition(callback)
        callback(segmented.position)

        return view
    }
}
