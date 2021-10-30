package com.carbonylgroup.schoolpower.fragments

import android.content.Intent
import android.didikee.donate.AlipayDonate
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.cardview.widget.CardView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.carbonylgroup.schoolpower.R
import com.carbonylgroup.schoolpower.activities.WechatIntroActivity
import com.carbonylgroup.schoolpower.utils.CryptoDonationDialog
import com.carbonylgroup.schoolpower.utils.Utils


class DonationFragment : Fragment() {

    val WECHAT_INTRO = 1
    private lateinit var utils: Utils
    private val AlipayToken = "tsx09230fuwngogndwbkg3b"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_donation, container, false)
        utils = Utils(activity!!)
        view.findViewById<CardView>(R.id.alipay_card).setOnClickListener { gotoAlipay() }
        view.findViewById<CardView>(R.id.wechat_card).setOnClickListener { gotoWechatPay() }
        view.findViewById<CardView>(R.id.paypal_card).setOnClickListener { gotoPaypal() }
        view.findViewById<CardView>(R.id.bitcoin_card).setOnClickListener { gotoCrypto(CryptoDonationDialog.CRYPTO_TYPE.BITCOIN) }
        view.findViewById<CardView>(R.id.eth_card).setOnClickListener { gotoCrypto(CryptoDonationDialog.CRYPTO_TYPE.ETHER) }
        return view
    }

    fun gotoAlipay() {
        if (AlipayDonate.hasInstalledAlipayClient(activity)) {
            AlipayDonate.startAlipayClient(activity, AlipayToken)
            setIsDonated(true)
        } else {
            utils.showSnackBar(view!!.findViewById(R.id.donation_fragment), getString(R.string.AlipayNotFound), true)
        }
    }

    fun gotoWechatPay() {
        startActivityForResult(Intent(activity, WechatIntroActivity::class.java), WECHAT_INTRO)
    }

    fun gotoPaypal() {
        AlipayDonate.startIntentUrl(activity, getString(R.string.paypalDonationURL))
        setIsDonated(true)
    }

    fun gotoCrypto(crypto: CryptoDonationDialog.CRYPTO_TYPE) {
        CryptoDonationDialog(activity!!, crypto).show()
        setIsDonated(true)
    }

    private fun setIsDonated(donated: Boolean) {
        utils.setPreference("Donated", donated, Utils.TmpData)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == WECHAT_INTRO && resultCode == WechatIntroActivity().WECHAT_NOT_FOUND) {
            utils.showSnackBar(activity!!.findViewById(R.id.donation_fragment), getString(R.string.WechatNotFound), true)
        }
    }
}
