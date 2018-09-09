package com.carbonylgroup.schoolpower.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.carbonylgroup.schoolpower.R
import com.carbonylgroup.schoolpower.data.CategoryWeightData
import com.carbonylgroup.schoolpower.data.Subject
import com.carbonylgroup.schoolpower.utils.Utils
import kotlinx.android.synthetic.main.activity_category.*
import kotlinx.android.synthetic.main.content_category.*

class CategoryActivity : AppCompatActivity() {

    class CategoryItem(val weight: Double){
        var score: Double = 0.0
        var maxScore: Double = 0.0

        fun getPercentage() = score/maxScore
        fun getWeightedScore() = score*weight
        fun getWeightedMaxScore() = maxScore*weight
    }
    lateinit var categoriesWeights: CategoryWeightData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category)
        setSupportActionBar(toolbar)

        val utils = Utils(this)
        categoriesWeights = CategoryWeightData(utils)

        val subject = intent.getSerializableExtra("subject") as Subject

        val categoriesMap = HashMap<String, CategoryItem>()
        for(assignment in subject.assignments){
            if(!categoriesMap.contains(assignment.category))
                categoriesMap[assignment.category] =
                        CategoryItem(categoriesWeights.getWeight(assignment.category, subject)
                                ?: Double.NaN)
            if(assignment.score==null || assignment.weight==null || assignment.maximumScore==null)
                continue
            categoriesMap[assignment.category]!!.score += assignment.score * assignment.weight
            categoriesMap[assignment.category]!!.maxScore += assignment.maximumScore * assignment.weight
        }

        var sumScore = 0.0
        var sumMaxScore = 0.0
        for((name, category) in categoriesMap){
            cates.text = cates.text.toString() + "\n\n" + "name: "+name+" "+category.score+
                    "/"+category.maxScore+" ("+category.getPercentage()*100+"%) contributing "+
                    category.getWeightedScore() + "/" + category.getWeightedMaxScore()
            sumScore+=category.getWeightedScore()
            sumMaxScore+=category.getWeightedMaxScore()
        }

        cates.text = cates.text.toString() + "\n" + "total "+ sumScore+"/"+sumMaxScore+
                "("+(sumScore/sumMaxScore*100)+"%)"

        catset.setOnClickListener {
            categoriesWeights.setWeight(catname.text.toString(), subject, catweight.text.toString().toDouble())
            categoriesWeights.flush()
        }
    }
}
