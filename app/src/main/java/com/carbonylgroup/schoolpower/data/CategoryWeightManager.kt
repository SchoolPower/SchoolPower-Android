package com.carbonylgroup.schoolpower.data

import com.carbonylgroup.schoolpower.utils.Utils
import org.json.JSONObject

/**
 * Example of JSON:
 * {
 *   "Science 10": {
 *     "Homework": 0.2,
 *     "Quiz": 0.3,
 *     "Test": 0.5
 *   }
 * }
 */
class CategoryWeightData(val utils: Utils){
    var json = JSONObject(utils.getSharedPreference(Utils.CategoryWeightData)
            .getString("category_data", "{}"))

    fun getWeight(category: String, subject: Subject): Double?{
        if(!json.has(subject.name)) return null
        val sub = json.getJSONObject(subject.name)
        if(!sub.has(category)) return null
        return sub.getDouble(category)
    }

    fun setWeight(category: String, subject: Subject, weight: Double) {
        if(!json.has(subject.name)) json.put(subject.name, JSONObject())
        json.getJSONObject(subject.name).put(category, weight)
    }

    fun flush() {
        utils.setSharedPreference(Utils.CategoryWeightData, "category_data", json.toString())
    }
}