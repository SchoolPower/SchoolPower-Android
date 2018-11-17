package com.carbonylgroup.schoolpower.data

import java.io.Serializable
import java.util.*

class CalculatedGrade(subject: Subject, term: String, weightData: CategoryWeightData) : Serializable{
    class CategoryItem(val weight: Double) : Serializable {
        var score: Double = 0.0
        var maxScore: Double = 0.0

        fun getPercentage() = score/maxScore
        fun getWeightedPercentage() = score/maxScore * weight
    }
    var sumScorePercentage = 0.0
    var sumMaxScorePercentage = 0.0
    val categories = HashMap<String, CategoryItem>()
    fun getEstimatedPercentageGrade() : Double = sumScorePercentage/sumMaxScorePercentage

    init{
        for(assignment in subject.assignments){
            if(!assignment.includeInFinalGrade) continue
            if(!assignment.terms.contains(term)) continue
            if(!categories.contains(assignment.category))
                categories[assignment.category] =
                        CategoryItem(weightData.getWeight(assignment.category, subject)
                                ?: Double.NaN)
            if(assignment.score==null || assignment.weight==null || assignment.maximumScore==null)
                continue
            categories[assignment.category]!!.score += assignment.score * assignment.weight
            categories[assignment.category]!!.maxScore += assignment.maximumScore * assignment.weight
        }

        for((_, category) in categories){
            sumScorePercentage += category.getWeightedPercentage()
            sumMaxScorePercentage += category.weight
        }
    }
}

class Grade(_percentage: String, _letter: String,
                 val comment: String, val evaluation: String,
                 var calculatedGrade: CalculatedGrade) : Serializable {
    val letter: String = if (_letter != "null")  _letter else "--"
    val percentage : Int? = _percentage.toIntOrNull()

    fun getGrade() : Int? = if(letter!="--") percentage else null
    fun hasGrade() : Boolean = getGrade() != null
    fun getPercentageString() = percentage?.toString()?:"--"
}