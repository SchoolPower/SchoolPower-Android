package com.carbonylgroup.schoolpower.data

import org.json.JSONArray
import org.json.JSONObject

class ILDNotification(jsonStr: String) {

    var show: Boolean = false
    var uuid: String = ""
    var headerImageURL: String = ""
    var titles: ArrayList<String> = arrayListOf("", "", "")
    var messages: ArrayList<String> = arrayListOf("", "", "")
    var primaryTexts: ArrayList<String> = arrayListOf("", "", "")
    var secondaryTexts: ArrayList<String> = arrayListOf("", "", "")
    var dismissTexts: ArrayList<String> = arrayListOf("", "", "")
    var hideDismiss: Boolean = true
    var hideSecondary: Boolean = true
    var onlyOnce: Boolean = true
    // Experimental
    var primaryOnClickListenerIndex: Int = -1

    init {
        val notification = JSONObject(jsonStr).getJSONObject("ildNotification")
        show = notification["show"].toString().toBoolean()
        uuid = notification["uuid"].toString()
        headerImageURL = notification["image"].toString()
        titles = notification.getJSONArray("titles").toArrayList()
        messages = notification.getJSONArray("messages").toArrayList()
        primaryTexts = notification.getJSONArray("primaryTexts").toArrayList()
        secondaryTexts = notification.getJSONArray("secondaryTexts").toArrayList()
        dismissTexts = notification.getJSONArray("dismissTexts").toArrayList()
        hideDismiss = notification["hideDismiss"].toString().toBoolean()
        hideSecondary = notification["hideSecondary"].toString().toBoolean()
        onlyOnce = notification["onlyOnce"].toString().toBoolean()
        primaryOnClickListenerIndex = notification["primaryOnClick"].toString().toInt()
    }

    private fun JSONArray.toArrayList(): ArrayList<String> {
        val list = ArrayList<String>()
        val jArray = this
        for (i in 0 until jArray.length() - 1) {
            list.add(jArray.getString(i))
        }
        return list
    }
}
