package com.example.compsci399testproject.bayesianfilters

import space.kscience.kmath.nd.ShapeND
import space.kscience.kmath.nd4j.DoubleNd4jTensorAlgebra.ndArray
import space.kscience.kmath.structures.Float64
import space.kscience.kmath.tensors.api.TensorAlgebra
import space.kscience.kmath.tensors.core.DoubleTensor
import space.kscience.kmath.tensors.core.DoubleTensorAlgebra
import space.kscience.kmath.tensors.core.asDoubleTensor
import space.kscience.kmath.tensors.core.zero


// Generic Kalman filter implementation
// To be ripped from here: https://github.com/zziz/kalman-filter?tab=readme-ov-file

class KalmanFilter(
    fF: DoubleTensor,
    bB: DoubleTensor?,
    hH: DoubleTensor,
    qQ: DoubleTensor?,
    rR: DoubleTensor?,
    pP: DoubleTensor?,
    x0: DoubleTensor?
) {

    var n = fF.shape[1]
    var m = hH.shape[1]

    var fF = fF
    var hH = hH
    var bB = bB ?: 0
    var qQ = qQ ?: DoubleTensorAlgebra.eye(n)
    var rR = rR ?: DoubleTensorAlgebra.eye(n)
    var pP = pP ?: DoubleTensorAlgebra.eye(n)
    var x = x0?: DoubleTensorAlgebra.zero(shape= ShapeND(n, 1))

    fun predict(mean: Float64 = 0.0): DoubleTensor {
        // Idk why I can't use either dot product or matmul methods
        x = TODO()
        return x
    }

    // z is observation
    fun update(z: DoubleTensor) {
        val y = z
        val sS = rR
        val kK = TODO()
        x = x
        val iI = DoubleTensorAlgebra.eye(n)
        pP = TODO()
    }
}