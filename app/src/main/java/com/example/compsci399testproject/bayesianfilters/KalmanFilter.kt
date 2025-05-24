package com.example.compsci399testproject.bayesianfilters

import org.nd4j.linalg.factory.Nd4j
import space.kscience.kmath.nd.StructureND
import space.kscience.kmath.nd.StructureNDOfDouble
import space.kscience.kmath.nd4j.Nd4jArrayStructure
import space.kscience.kmath.structures.Float64


// Generic Kalman filter implementation
// To be ripped from here: https://github.com/zziz/kalman-filter?tab=readme-ov-file

class KalmanFilter(F: StructureND<Float64>, B?, H, Q?, R?, P?, x0?) {

    private companion object {

        var n = F.shape[1]
        var m = H.shape[1]

        var F = F
        var H = H
        var B = if (B == null) 0 else B
        var Q = TODO()
        var R = TODO()
        var P = TODO()
        var x = TODO()
    }

    init {
        // TODO
    }

    suspend fun predict(mean: Float64 = 0.0) {
       // TODO
    }

    // z is observation
    suspend fun update(z: ArrayList<Float64>) {
        // TODO
    }
}