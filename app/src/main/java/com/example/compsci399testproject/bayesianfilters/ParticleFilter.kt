package com.example.compsci399testproject.bayesianfilters

import kotlinx.coroutines.*
import space.kscience.kmath.distributions.NormalDistribution
import space.kscience.kmath.distributions.UniformDistribution
import space.kscience.kmath.misc.cumulativeSum
import space.kscience.kmath.operations.asIterable
import space.kscience.kmath.structures.Float64
import space.kscience.kmath.random.RandomGenerator
import space.kscience.kmath.stat.next
import space.kscience.kmath.stat.nextBuffer
import space.kscience.kmath.structures.Float64Buffer
import space.kscience.kmath.structures.asList
import space.kscience.kmath.structures.toDoubleArray
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

private typealias XY = Pair<Float64, Float64>
private typealias Weight = Float64

data class Particle(var x: Float64, var y: Float64, var h: Float64)


// TODO: change to multivariate distributions? NVM not implemented

// TODO -> Add noise

// I went from Float64Buffer to DoubleTensor to StructureND<Float64> to Nd4jArrayDoubleStructure back to Float64Buffer
class ParticleFilter(initialX: Float64, initialY: Float64, initialHeading: Float64) {

    private companion object {
        const val N = 1000
        const val SENSOR_STD_ERROR = 0.5 // Adjustable - 0.5 is just arbitrary value

        val RNG = RandomGenerator.default
    }

    //  TODO -> check
    private var xParticles: Float64Buffer
    private var yParticles: Float64Buffer
    private var hParticles: Float64Buffer

    //  Assume equal weightage for now
    private var weights: Float64Buffer = Float64Buffer(DoubleArray(N) { 1.0 / N } )

    init {

        // TODO -> check
        // since we have initial position through ML, get mean and std and generate a normal distribution
        val xND = NormalDistribution(mean=initialX, standardDeviation=SENSOR_STD_ERROR)
        val yND = NormalDistribution(mean=initialY, standardDeviation=SENSOR_STD_ERROR)
        val hND = NormalDistribution(mean=initialHeading, standardDeviation=SENSOR_STD_ERROR)

        val xParticlesTemp: DoubleArray
        val yParticlesTemp: DoubleArray
        val hParticlesTemp: DoubleArray

        // TODO -> check
        runBlocking {
            xParticlesTemp = xND.nextBuffer(generator=RNG, size=N).toDoubleArray()
            yParticlesTemp = yND.nextBuffer(generator=RNG, size=N).toDoubleArray()

            // 0.00 <= h < 360.00
            hParticlesTemp = hND.nextBuffer(generator=RNG, size=N).asList().map { h -> h.mod(360.00) }.toDoubleArray()
        }

        xParticles = Float64Buffer(xParticlesTemp)
        yParticles = Float64Buffer(yParticlesTemp)
        hParticles = Float64Buffer(hParticlesTemp)

    }

    public fun getParticles(): ArrayList<Pair<Particle, Weight>> {
        val ps = ArrayList<Pair<Particle, Weight>>(N)
        for (i in 0 until N) {
            val p = Particle(x=xParticles[i], y=yParticles[i], h=hParticles[i])
            val w = weights[i]
            ps.add(Pair(p, w))
        }
        return ps
    }

    private suspend fun systematicResampling(): Array<Int> {
        val indexes = Array<Int>(N) { 0 }
        val cumulativeWeights = weights.asIterable().cumulativeSum().toList()
        val startPosition = UniformDistribution(range = 0.0..(1/N.toDouble()))

        for (i in 0 until N) {
            val currentPosition = startPosition.next(generator=RNG) + (1.0/N.toDouble()) * i
            var s = 0
            while (currentPosition > cumulativeWeights[s]) {
                s += 1
            }
            indexes[i] = s
        }
        return indexes
    }

    private fun neff(): Double {
        return 1.0 / weights.asList().sumOf { w -> w.pow(2) }
    }

    private suspend fun resample(indexes: Array<Int>) {
        TODO()
    }

    suspend fun stateEstimate(): XY {
        // weighted mean X
        TODO()
    }

    private fun propagate(particle: Particle, dTheta: Float64, dist: Float64): Particle {
        // TODO: make this louder
        particle.h += dTheta
        particle.x += dist * cos(particle.h)
        particle.y += dist * sin(particle.h)
        return particle
    }

    // haha😂
    private suspend fun evaluWeight(): Float64  {
        // TODO: add landmarks - new positions from ML model
        // A mystery
        // TODO: redo
        TODO()
    }

    suspend fun update(hMean: Float64, hStd: Float64, dMean: Float64, dStd: Float64): XY {
        var normalizationFactor: Float64 = 0.0

        val newX = Float64Buffer(DoubleArray(N) { 0.0 })
        val newY = Float64Buffer(DoubleArray(N) { 0.0 })
        val newH = Float64Buffer(DoubleArray(N) { 0.0 })
        val newWeights = Float64Buffer(DoubleArray(N) { 0.0 })

        val hND = NormalDistribution(mean=hMean, standardDeviation=hStd)
        val distND = NormalDistribution(mean=dMean, standardDeviation=dStd)

        for (i in 0 until N) {
            // TODO -> check j and make it according to weight

            // random sample according to weight -> higher weight = more common
            val j = RNG.nextInt(N)

            var particle= Particle(x= xParticles[j], y=yParticles[j], h=hParticles[j])

            // apply state transition model
            particle = propagate(particle=particle, dTheta=hND.next(generator=RNG), dist=distND.next(generator=RNG))


            // TODO: implement
            val wI = evaluWeight()

            // prior * likelihood
            newWeights[i] *= wI

            normalizationFactor += wI

            newX[i] = particle.h
            newY[i] = particle.h
            newH[i] = particle.h

        }

        xParticles = newX
        yParticles = newY
        hParticles = newH
        weights = newWeights

        // Normalize weights
        for (i in 0 until N) {
            weights[i] /= normalizationFactor
        }

        // Resample
        if (neff() < (N/2)) {
            // TODO: check
            val indexes = systematicResampling()
            resample(indexes)
        }

        // TODO: implement
        val meanXY = stateEstimate()

        return meanXY
    }

}