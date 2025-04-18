package com.example.compsci399testproject.machinelearning

/**
 *  A model that predicts floor level, x coordinate and y coordinate.
 *  These predictions are based on wifi signal strength readings.
 * */

import com.example.compsci399testproject.machinelearning.models.FloorRandomForest
import com.example.compsci399testproject.machinelearning.models.XRandomForest
import com.example.compsci399testproject.machinelearning.models.YRandomForest

class LocationPredictor() {
    companion object{
        fun  predictFloor(input: DoubleArray) : Int {
            val predictionScores: DoubleArray = FloorRandomForest.score(input)
            var predictedClassIndex = -1
            var maxScore = Double.NEGATIVE_INFINITY

            predictionScores.forEachIndexed { index, score ->
                if (score > maxScore) {
                    maxScore = score
                    predictedClassIndex = index
                }
            }

            return predictedClassIndex
        }

        fun predictX(input: DoubleArray) : Int {
            val floor = predictFloor(input).toDouble()
            val newInput = doubleArrayOf(floor) + input
            val prediction : Double = XRandomForest.score(newInput)
            return prediction.toInt()
        }

        fun predictY(input: DoubleArray) : Int {
            val floor = predictFloor(input).toDouble()
            val newInput = doubleArrayOf(floor) + input
            val prediction : Double = YRandomForest.score(newInput)
            return prediction.toInt()
        }
    }


}