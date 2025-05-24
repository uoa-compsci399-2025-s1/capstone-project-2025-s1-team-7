package com.example.compsci399testproject.machinelearning

/**
 *  A model that predicts floor level, x coordinate and y coordinate.
 *  These predictions are based on wifi signal strength readings.
 * */

import com.example.compsci399testproject.machinelearning.models.ClassifierModel
import com.example.compsci399testproject.machinelearning.models.RegressionModel
import com.example.compsci399testproject.machinelearning.models.FloorRandomForest
import com.example.compsci399testproject.machinelearning.models.XRandomForest1
import com.example.compsci399testproject.machinelearning.models.XRandomForest2
import com.example.compsci399testproject.machinelearning.models.XRandomForest3
import com.example.compsci399testproject.machinelearning.models.XRandomForest4
//import com.example.compsci399testproject.machinelearning.models.XRandomForest10
import com.example.compsci399testproject.machinelearning.models.XRandomForestCurrent
import com.example.compsci399testproject.machinelearning.models.XRandomForestOriginal
import com.example.compsci399testproject.machinelearning.models.YRandomForest1
import com.example.compsci399testproject.machinelearning.models.YRandomForest2
import com.example.compsci399testproject.machinelearning.models.YRandomForest3
import com.example.compsci399testproject.machinelearning.models.YRandomForest4
//import com.example.compsci399testproject.machinelearning.models.YRandomForest10
import com.example.compsci399testproject.machinelearning.models.YRandomForestCurrent
import com.example.compsci399testproject.machinelearning.models.YRandomForestOriginal

class LocationPredictor() {
    companion object{
        private var xModel: RegressionModel = XRandomForestOriginal.INSTANCE
        private var yModel: RegressionModel = YRandomForestOriginal.INSTANCE
        private var floorModel: ClassifierModel = FloorRandomForest.INSTANCE
        private val modelsMap: Map<String, RegressionModel> = mapOf(
            "XOriginal" to XRandomForestOriginal.INSTANCE,
            "XCurrent" to XRandomForestCurrent.INSTANCE,
            "X1" to XRandomForest1.INSTANCE,
            "X2" to XRandomForest2.INSTANCE,
            "X3" to XRandomForest3.INSTANCE,
            "X4" to XRandomForest4.INSTANCE,
//            "X10" to XRandomForest10.INSTANCE,
            "YOriginal" to YRandomForestOriginal.INSTANCE,
            "YCurrent" to YRandomForestCurrent.INSTANCE,
            "Y1" to YRandomForest1.INSTANCE,
            "Y2" to YRandomForest2.INSTANCE,
            "Y3" to YRandomForest3.INSTANCE,
            "Y4" to YRandomForest4.INSTANCE,
//            "Y10" to YRandomForest10.INSTANCE,
            )

        fun  predictFloor(input: FloatArray) : Int {
            val predictionScores: DoubleArray = floorModel.score(input)
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

        fun predictX(input: FloatArray) : Float {
            val floor = predictFloor(input).toFloat()
            val newInput = floatArrayOf(floor) + input
            val prediction : Double = xModel.score(newInput)
            return prediction.toFloat()
        }

        fun predictY(input: FloatArray) : Float {
            val floor = predictFloor(input).toFloat()
            val newInput = floatArrayOf(floor) + input
            val prediction : Double = yModel.score(newInput)
            return prediction.toFloat()
        }

        fun changeXModel(model: String) {
            xModel = modelsMap[model]!!
        }

        fun changeYModel(model: String) {
            yModel = modelsMap[model]!!
        }

    }


}