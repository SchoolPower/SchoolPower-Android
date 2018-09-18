package com.carbonylgroup.schoolpower.activities

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.didikee.donate.WeiXinDonate
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.support.annotation.Nullable
import android.support.v13.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.WindowManager
import android.widget.Toast
import com.carbonylgroup.schoolpower.R
import com.carbonylgroup.schoolpower.utils.ContextWrapper
import com.carbonylgroup.schoolpower.utils.Utils
import com.github.paolorotolo.appintro.AppIntro
import com.github.paolorotolo.appintro.AppIntroFragment
import com.github.paolorotolo.appintro.model.SliderPage
import java.io.File


class WechatIntroActivity : AppIntro() {

    val MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE = 233
    val WECHAT_NOT_FOUND = 1

    override fun attachBaseContext(newBase: Context?) {
        val utils = Utils(newBase!!)
        val newLocale = utils.getSharedPreference(Utils.SettingsPreference).getString("lang", "0")!!.toInt()
        val context = ContextWrapper.wrap(newBase, Utils.localeSet[newLocale])
        super.attachBaseContext(context)
    }

    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val page1 = SliderPage()
        val page2 = SliderPage()
        val page3 = SliderPage()
        val page4 = SliderPage()
        val page5 = SliderPage()

        page1.title = getString(R.string.wechat_instruction_page1_title)
        page1.description = getString(R.string.wechat_instruction_page1_des)
        page2.description = getString(R.string.wechat_instruction_page2_des)
        page3.description = getString(R.string.wechat_instruction_page3_des)
        page4.description = getString(R.string.wechat_instruction_page4_des)
        page5.description = getString(R.string.wechat_instruction_page5_des)

        page1.bgColor = ContextCompat.getColor(this, R.color.B_score_green)
        page2.bgColor = ContextCompat.getColor(this, R.color.B_score_green)
        page3.bgColor = ContextCompat.getColor(this, R.color.B_score_green)
        page4.bgColor = ContextCompat.getColor(this, R.color.B_score_green)
        page5.bgColor = ContextCompat.getColor(this, R.color.B_score_green)

        page1.imageDrawable = R.drawable.ic_wechat_pay
        page2.imageDrawable = R.drawable.page2
        page3.imageDrawable = R.drawable.page3
        page4.imageDrawable = R.drawable.page4
        page5.imageDrawable = R.drawable.page5

        addSlide(AppIntroFragment.newInstance(page1))
        addSlide(AppIntroFragment.newInstance(page2))
        addSlide(AppIntroFragment.newInstance(page3))
        addSlide(AppIntroFragment.newInstance(page4))
        addSlide(AppIntroFragment.newInstance(page5))

        val window = this.window
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(this, R.color.B_score_green_dark)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.B_score_green)

        setBarColor(ContextCompat.getColor(this, android.R.color.transparent))
        setSeparatorColor(ContextCompat.getColor(this, R.color.white_0_10))

        showSkipButton(true)
        isProgressButtonEnabled = true
    }

    override fun onSkipPressed(currentFragment: Fragment) {
        super.onSkipPressed(currentFragment)
        checkStoragePermission()
    }

    override fun onDonePressed(currentFragment: Fragment) {
        super.onDonePressed(currentFragment)
        checkStoragePermission()
    }

    private fun checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                val explanation = AlertDialog.Builder(this)
                val listener = DialogInterface.OnClickListener { _, _ ->
                    ActivityCompat.requestPermissions(this,
                            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                            MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE) }

                explanation.setTitle(R.string.storage_permission_exp_title)
                explanation.setMessage(R.string.wechat_instruction_page1_des)
                explanation.setPositiveButton(R.string.alright, listener)
                explanation.show()
            } else {
                // No explanation needed
                ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE)
            }
        } else {
            // Permission has already been granted
            gotoWechatScan()
        }
    }

    private fun gotoWechatScan() {
        if (WeiXinDonate.hasInstalledWeiXinClient(this)) {
            val weixinQrIs = resources.openRawResource(R.raw.sp_wechat)
            val qrPath = Environment.getExternalStorageDirectory().absolutePath + File.separator + "SchoolPowerDonate" + File.separator +
                    "sp_wechat.png"
            WeiXinDonate.saveDonateQrImage2SDCard(qrPath, BitmapFactory.decodeStream(weixinQrIs))
            WeiXinDonate.donateViaWeiXin(this, qrPath)
            setIsDonated(true)
            this.finish()
        } else {
            setResult(WECHAT_NOT_FOUND)
            this.finish()
        }
    }

    private fun setIsDonated(donated: Boolean) {
        Utils(this).setSharedPreference(Utils.TmpData, "Donated", donated)
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                   permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted
                    gotoWechatScan()
                } else {
                    // permission denied
                    Toast.makeText(this, R.string.storage_permission_denied, Toast.LENGTH_LONG).show()
                    this.finish()
                }
                return
            }
            else -> {
                // Ignore all other requests.
            }
        }
    }
}
