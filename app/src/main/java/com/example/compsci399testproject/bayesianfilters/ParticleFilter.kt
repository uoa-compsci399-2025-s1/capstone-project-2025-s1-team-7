package com.example.compsci399testproject.bayesianfilters

import kotlinx.coroutines.*
import org.nd4j.shade.jackson.databind.JsonSerializer
import space.kscience.kmath.distributions.NormalDistribution
import space.kscience.kmath.distributions.UniformDistribution
import space.kscience.kmath.linear.Float64LinearSpace.div
import space.kscience.kmath.misc.cumulative
import space.kscience.kmath.misc.cumulativeSum
import space.kscience.kmath.operations.asIterable
import space.kscience.kmath.structures.Float64
import space.kscience.kmath.random.RandomGenerator
import space.kscience.kmath.stat.next
import space.kscience.kmath.stat.nextBuffer
import space.kscience.kmath.structures.Float64Buffer
import space.kscience.kmath.structures.asList
import space.kscience.kmath.structures.toDoubleArray
import space.kscience.kmath.structures.toFloat64Buffer
import java.util.Queue
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

private typealias XY = Pair<Float64, Float64>
private typealias Weight = Float64

data class Particle(var x: Float64, var y: Float64, var h: Float64)


// TODO: use multivariate distributions? not implemented in kmath library

// TODO List
//      -> Add noise
//      -> Make particles be aware of walls
//      -> Make PF z aware

// I went from Float64Buffer to DoubleTensor to StructureND<Float64> to Nd4jArrayDoubleStructure back to Float64Buffer

// Has to be run in coroutine otherwise main thread will be overloaded
class ParticleFilter(initialX: Float64, initialY: Float64, initialHeading: Float64) {

    private companion object {
        const val N = 1000
        const val SENSOR_STD_ERROR = 0.5 // Adjustable - 0.5 is just arbitrary value

        // custom rate at which a landmark (ML pos) becomes less use full
        // Degradation? Depreciation? Obsolete? I can't think of a good word
        const val LANDMARK_DEGRADATION_RATE = 200

        val RNG = RandomGenerator.default

    }

    //  TODO -> check
    private var xParticles: Float64Buffer
    private var yParticles: Float64Buffer
    private var hParticles: Float64Buffer

    //  Assume equal weightage for now
    private var weights: Float64Buffer = Float64Buffer(DoubleArray(N) { 1.0 / N } )

    // treat like a queue
    private var landmarks: ArrayDeque<XY> = ArrayDeque<XY>()

    // Used to count time steps
    private var dt: Int = 0

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
            hParticlesTemp =
                hND.nextBuffer(generator=RNG, size=N).asList().map { h -> h.mod(360.00) }
                    .toDoubleArray()
        }

        xParticles = Float64Buffer(xParticlesTemp)
        yParticles = Float64Buffer(yParticlesTemp)
        hParticles = Float64Buffer(hParticlesTemp)

    }

   fun getParticles(): ArrayList<Pair<Particle, Weight>> {
        val ps = ArrayList<Pair<Particle, Weight>>(N)
        for (i in 0 until N) {
            val p = Particle(x=xParticles[i], y=yParticles[i], h=hParticles[i])
            val w = weights[i]
            ps.add(Pair(p, w))
        }
        return ps
    }

    fun addLandmark(x: Float64, y: Float64) {
        landmarks.addFirst(XY(x, y))
    }

    private fun dtSupervisor() {
        if (dt > LANDMARK_DEGRADATION_RATE) {
            landmarks.removeLast()
        }
    }

    // Resampling algorithms are kinda swappable
    private suspend fun systematicResampling(): Array<Int> {
        val indexes = Array<Int>(N) { 0 }
        val cumulativeWeights = weights.asIterable().cumulativeSum().toList()
        val start = UniformDistribution(range=0.0..(1/N.toDouble()))

        for (i in 0 until N) {
            val current = start.next(generator=RNG) + (1.0/N.toDouble()) * i
            var s = 0
            while (current > cumulativeWeights[s]) {
                s += 1
            }
            indexes[i] = s
        }
        return indexes
    }

    // nEff = Number of effective
    private fun nEff(): Double {
        return 1.0 / weights.asList().sumOf { w -> w.pow(2) }
    }

    private fun resample(indexes: Array<Int>) {
        val newX = Float64Buffer(DoubleArray(N) { 0.0 })
        val newY = Float64Buffer(DoubleArray(N) { 0.0 })
        val newH = Float64Buffer(DoubleArray(N) { 0.0 })

        for (i in 0 until N) {
            val index = indexes[i]
            newX[i] = xParticles[index]
            newY[i] = yParticles[index]
            newH[i] = hParticles[index]
        }

        xParticles = newX
        yParticles = newY
        hParticles = newH

        // Reset weights
        for (i in 0 until N) {
            weights[i] = 1.0 / N
        }
    }

    private fun stateEstimate(): XY {
        var weightedMeanX = 0.0
        var weightedMeanY = 0.0
        val weightSum = weights.asList().sum()

        for (i in 0 until N) {
            val w = weights[i]
            weightedMeanX += xParticles[i]*w
            weightedMeanY += yParticles[i]*w
        }

        weightedMeanX /= weightSum
        weightedMeanY /= weightSum

        return Pair(weightedMeanX, weightedMeanY)
    }

    // TODO: may be variance instead
    private fun covEstimate() {
        TODO()
    }

    private fun propagate(particle: Particle, dTheta: Float64, dist: Float64): Particle {
        // TODO: make this louder
        particle.h += dTheta
        particle.x += dist * cos(particle.h)
        particle.y += dist * sin(particle.h)
        return particle
    }

    // 😂haha😂
    private suspend fun evaluWeight(index: Int): Float64  {
        if (landmarks.isNotEmpty()) {
            TODO()
        } else {
            TODO()
        }
    }

    suspend fun update(hMean: Float64, hStd: Float64, dMean: Float64, dStd: Float64): XY {

        // Make observer?
        dt +=1
        dtSupervisor()

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

            // prior * likelihood
            val wI = weights[i] * evaluWeight(index=i)

            // New weight = Old weight * p(z | x)
            newWeights[i] = wI

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

        // Resample - N/2 can be adjusted -> N/3. N/4
        if (nEff() < (N / 2)) {
            val indexes = systematicResampling()
            resample(indexes)
        }

        val meanXY = stateEstimate()

        return meanXY
    }

}