package com.example.compsci399testproject.bayesianfilters

import space.kscience.kmath.distributions.NormalDistribution
import space.kscience.kmath.distributions.UniformDistribution
import space.kscience.kmath.samplers.GaussianSampler
import space.kscience.kmath.misc.cumulativeSum
import space.kscience.kmath.operations.asIterable
import space.kscience.kmath.structures.Float64
import space.kscience.kmath.random.RandomGenerator
import space.kscience.kmath.stat.next
import space.kscience.kmath.structures.Float64Buffer
import space.kscience.kmath.structures.asBuffer
import space.kscience.kmath.structures.asList
import space.kscience.kmath.structures.toFloat64Buffer
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt


// TODO Future List:
//      -> Make particles be aware of walls
//      -> Make PF z aware

// TODO: use multivariate distributions? not implemented in kmath library

// Vent: I went from Float64Buffer to DoubleTensor to StructureND<Float64> to Nd4jArrayDoubleStructure back to Float64Buffer

private typealias XY = Pair<Float64, Float64>
private typealias Weight = Float64

data class Particle(var x: Float64, var y: Float64, var h: Float64)

private fun randomNormal(size: Int): Float64Buffer {
    val nd = GaussianSampler(mean=0.0, standardDeviation=1.0)
    val generator = RandomGenerator.default
    return nd.sample(generator=generator).nextBufferBlocking(size)
}


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

    private var meanXY: XY = Pair(initialX, initialX)

    init {

        // TODO -> check
        // since we have initial position through ML, get mean and std and generate a normal distribution
        val xND = NormalDistribution(mean=initialX, standardDeviation=SENSOR_STD_ERROR)
        val yND = NormalDistribution(mean=initialY, standardDeviation=SENSOR_STD_ERROR)
        val hND = NormalDistribution(mean=initialHeading, standardDeviation=SENSOR_STD_ERROR)

        // TODO -> check
        xParticles = xND.sample(generator=RNG).nextBufferBlocking(size=N)
        yParticles = yND.sample(generator=RNG).nextBufferBlocking(size=N)

            // 0.00 <= h < 360.00
        hParticles = hND.sample(generator=RNG)
                        .nextBufferBlocking(size=N)
                        .asList()
                        .map { h -> h.mod(360.00) }
                        .asBuffer()
                        .toFloat64Buffer()

        addLandmark(x=initialX, y=initialX)
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
        if (landmarks.isNotEmpty() && dt > LANDMARK_DEGRADATION_RATE) {
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
            weightedMeanX += xParticles[i] * w
            weightedMeanY += yParticles[i] * w
        }

        weightedMeanX /= weightSum
        weightedMeanY /= weightSum

        return Pair(weightedMeanX, weightedMeanY)
    }


    private fun stdEstimate(): Pair<Float64, Float64> {

        val weightedMean = stateEstimate()
        val weightedMeanX = weightedMean.first
        val weightedMeanY = weightedMean.second


        var weightedStdX = 0.0
        var weightedStdY = 0.0
        val weightSum = weights.asList().sum()

        for (i in 0 until N) {
            val w = weights[i]
            weightedStdX += (xParticles[i] - weightedMeanX) * w
            weightedStdY += (yParticles[i] - weightedMeanY) * w
        }

        weightedStdX /= weightSum
        weightedStdY /= weightSum

        return Pair(weightedStdX, weightedStdY)
    }

    private fun propagate(particle: Particle, dTheta: Float64, dist: Float64): Particle {

        // dTheta + noise
        val dThetaN = dTheta + (randomNormal(size=1)[0] * SENSOR_STD_ERROR)
        particle.h = (particle.h + dThetaN).mod(360.00)

        // dist + noise
        val distN = dist + (randomNormal(size=1)[0] * SENSOR_STD_ERROR)
        particle.x += distN * cos(particle.h)
        particle.y += distN * sin(particle.h)

        return particle
    }

    // l2Norm aka ||x|| aka euclidean distance
    private fun l2Norm(x: Float64, y: Float64, landmark: XY = Pair(0.0,0.0)): Float64 {
        return sqrt((x - landmark.first).pow(2) + (y - landmark.second).pow(2) )
    }

    // 😂haha😂
    private fun evaluWeight(zs: Float64Buffer, particle: Particle): Float64  {

        /*

        Create a distribution using l2Norm from propagated particles -> our importance distribution

        Feed observed readings (z) into distribution to get PDF -> importance sampling

         */

        val x = particle.x
        val y = particle.y

        if (landmarks.isEmpty()) {
            val norm = l2Norm(x, y)
            val nND = NormalDistribution(mean=norm, standardDeviation=SENSOR_STD_ERROR)
            return nND.probability(zs[0])
        }

        var zProb = 0.0
        for (i in 0 until landmarks.size) {
            val norm = l2Norm(x, y, landmarks[i])
            val nND = NormalDistribution(mean=norm, standardDeviation=SENSOR_STD_ERROR)
            zProb += nND.probability(zs[i])
        }

        return zProb
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

        // observed measurements
        val zs: Float64Buffer

        // educated guess?
        // where we think person has moved given old positions, distance mean and heading mean
        val observedX = meanXY.first + (dMean * cos(hMean))
        val observedY = meanXY.second + (dMean * sin(hMean))

        if (landmarks.isEmpty()) {
            zs = Float64Buffer(doubleArrayOf(observedX, observedY))
            val noise = randomNormal(size=zs.size)

            zs[0] = zs[0] + (noise[0] * SENSOR_STD_ERROR)
            zs[1] = zs[1] + (noise[1] * SENSOR_STD_ERROR)

        } else {
            zs = randomNormal(size=landmarks.size)
            for (i in 0 until landmarks.size) {
                val norm = l2Norm(x=observedX, y=observedY, landmark=landmarks[i])
                zs[i] = (zs[i] * SENSOR_STD_ERROR) + norm
            }
        }

        for (i in 0 until N) {

            // TODO -> ? check j and make it according to weight
            // random sample according to weight -> higher weight = more common
//            val j = RNG.nextInt(N)

            var particle= Particle(x= xParticles[i], y=yParticles[i], h=hParticles[i])

            // apply state transition model
            particle = propagate(particle=particle, dTheta=hND.next(generator=RNG), dist=distND.next(generator=RNG))

            // prior * likelihood <-> Old weight * p(z | x)
            val wI = weights[i] * evaluWeight(zs=zs, particle=particle)

            newWeights[i] = wI

            normalizationFactor += wI

            newX[i] = particle.x
            newY[i] = particle.y
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

        meanXY = stateEstimate()

        return meanXY
    }

}