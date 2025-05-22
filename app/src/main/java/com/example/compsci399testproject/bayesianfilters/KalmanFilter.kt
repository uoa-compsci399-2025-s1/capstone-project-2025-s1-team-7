package com.example.compsci399testproject.bayesianfilters

import org.nd4j.linalg.factory.Nd4j


// Generic Kalman filter implementation
// To be ripped from here: https://github.com/zziz/kalman-filter?tab=readme-ov-file

class KalmanFilter {

    private companion object {
        var n = Nd4j.create(TODO())
        var m = Nd4j.create(TODO())

        var F = TODO()
        var H = TODO()
        var B = TODO()
        var Q = TODO()
        var R = TODO()
        var P = TODO()
        var x = TODO()
    }

    init {
        // TODO
    }

    suspend fun predict() {
       // TODO
    }

    suspend fun update() {
        // TODO
    }
}