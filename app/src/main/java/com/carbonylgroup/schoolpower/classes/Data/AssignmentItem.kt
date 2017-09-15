/**
 * Copyright (C) 2017 Gustav Wang
 */

package com.carbonylgroup.schoolpower.classes.Data

import org.json.JSONObject
import java.io.Serializable
/*
Sample:
{
    "category": "Assignment Category",
    "description": null,
    "name": "Assignment Name",
    "percent": "80",
    "score": "8"
}
*/
class AssignmentItem(assignmentData: JSONObject) : Serializable {
    val title: String = assignmentData.getString("name")
    val date: String
    val percentage: String
    val dividedScore: String
    val grade: String
    val category: String = assignmentData.getString("category")
    val term: String

    var isNew = false

    init{
        // TODO: other data
    }
}
