package com.carbonylgroup.schoolpower.classes.Data

import org.json.JSONObject
import java.io.Serializable

/**
 * Created by null on 17-9-13.
 */
class StudentInformation(json: JSONObject) : Serializable {
    /*
        Sample:
            {
                "currentGPA": null,
                "currentMealBalance": "0.0",
                "currentTerm": null,
                "dcid": "10000",
                "dob": "2001-01-01T16:00:00.000Z",
                "ethnicity": null,
                "firstName": "John",
                "gender": "M",
                "gradeLevel": "10",
                "id": "10000",
                "lastName": "Doe",
                "middleName": "English Name",
                "photoDate": "2016-01-01T16:10:05.699Z",
                "startingMealBalance": "0.0"
            }
    */

    enum class Gender{
        Male, Female
    }

    val GPA: Double? = json.optDouble("currentGPA")
    val id : Int            = json.getInt("id")
    val gender : Gender     = if(json.getString("gender") == "M") Gender.Male else Gender.Female
    val dob : String        = json.getString("dob")
    val firstName : String  = json.getString("middleName")
    val middleName : String = json.getString("gender")
    val lastName : String   = json.getString("lastName")
    val photoDate : String  = json.getString("photoDate")

    fun getFullName() = "$middleName $firstName, $lastName"
}
