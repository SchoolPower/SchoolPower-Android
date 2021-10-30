package com.carbonylgroup.schoolpower.utils

import android.app.Activity
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.cardview.widget.CardView
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.carbonylgroup.schoolpower.R
import net.glxn.qrgen.android.QRCode


class CryptoDonationDialog(private val activity: Activity, private val crypto: CRYPTO_TYPE) {

    enum class CRYPTO_TYPE {
        BITCOIN, ETHER
    }

    fun show() {
        val utils = Utils(activity)
        val cryptoDonationDialog = LayoutInflater.from(activity).inflate(R.layout.crypto_donation_dialog, null)
        val cryptoDonationDialogView = cryptoDonationDialog.findViewById<View>(R.id.crypto_dialog_root_view)
        val cryptoDonationDialogBuilder = AlertDialog.Builder(activity)
        val address = when (crypto) {
            CRYPTO_TYPE.BITCOIN -> activity.getString(R.string.bitcoin_address)
            CRYPTO_TYPE.ETHER -> activity.getString(R.string.ether_address)
        }
        cryptoDonationDialogView.findViewById<ImageView>(R.id.crypto_qr_view).setImageBitmap(QRCode.from(address)
                .withSize(utils.dpToPx(250), utils.dpToPx(250))
                .withColor(utils.getPrimaryColor(), 0xFFFFFFFF.toInt()).bitmap())
        cryptoDonationDialogView.findViewById<TextView>(R.id.crypto_address).text = address
        cryptoDonationDialogView.findViewById<CardView>(R.id.crypto_address_wrapper).setOnClickListener {
            val clipboard = activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("crypto address", address))
            Toast.makeText(activity, activity.getString(R.string.copied), Toast.LENGTH_SHORT).show()
        }
        cryptoDonationDialogBuilder.setView(cryptoDonationDialogView)
        cryptoDonationDialogBuilder.setTitle(
                when (crypto) {
                    CRYPTO_TYPE.BITCOIN -> activity.getString(R.string.donate_via_bitcoin)
                    CRYPTO_TYPE.ETHER -> activity.getString(R.string.donate_via_eth)
                }
        )
        cryptoDonationDialogBuilder.setPositiveButton(activity.getString(R.string.sweet), null)
        cryptoDonationDialogBuilder.create().setCanceledOnTouchOutside(true)
        cryptoDonationDialogBuilder.create().show()
    }
}
