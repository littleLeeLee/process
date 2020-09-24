package com.kintex.check.activity

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.os.PowerManager.WakeLock
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.*
import android.widget.FrameLayout
import com.kintex.check.R

/**
 * This is an example of using the accelerometer to integrate the device's
 * acceleration to a position using the Verlet method. This is illustrated with
 * a very simple particle system comprised of a few iron balls freely moving on
 * an inclined wooden table. The inclination of the virtual table is controlled
 * by the device's accelerometer.
 *
 * @see SensorManager
 *
 * @see SensorEvent
 *
 * @see Sensor
 */
class AccelerometerPlayActivity : Activity() {
    private var mSimulationView: SimulationView? = null
    private var mSensorManager: SensorManager? = null
    private var mPowerManager: PowerManager? = null
    private var mWindowManager: WindowManager? = null
    private var mDisplay: Display? = null
    private var mWakeLock: WakeLock? = null
    /** Called when the activity is first created.  */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Get an instance of the SensorManager
        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        // Get an instance of the PowerManager
        mPowerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        // Get an instance of the WindowManager
        mWindowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        mDisplay = mWindowManager!!.defaultDisplay
        // Create a bright wake lock
        mWakeLock = mPowerManager!!.newWakeLock(
            PowerManager.SCREEN_BRIGHT_WAKE_LOCK, javaClass
                .name
        )
        // instantiate our simulation view and set it as the activity's content
        mSimulationView = SimulationView(this)
        mSimulationView!!.setBackgroundResource(R.mipmap.wood)
        setContentView(mSimulationView)
    }

    override fun onResume() {
        super.onResume()
        /*
         * when the activity is resumed, we acquire a wake-lock so that the
         * screen stays on, since the user will likely not be fiddling with the
         * screen or buttons.
         */mWakeLock!!.acquire(10*60*1000L /*10 minutes*/)
        // Start the simulation
        mSimulationView!!.startSimulation()
    }

    override fun onPause() {
        super.onPause()
        /*
         * When the activity is paused, we make sure to stop the simulation,
         * release our sensor resources and wake locks
         */
// Stop the simulation
        mSimulationView!!.stopSimulation()
        // and release our wake-lock
        mWakeLock!!.release()
    }

    internal inner class SimulationView(context: Context?) :
        FrameLayout(context!!), SensorEventListener {
        private val mDstWidth: Int
        private val mDstHeight: Int
        private val mAccelerometer: Sensor
        private var mLastT: Long = 0
        private val mXDpi: Float
        private val mYDpi: Float
        private val mMetersToPixelsX: Float
        private val mMetersToPixelsY: Float
        private var mXOrigin = 0f
        private var mYOrigin = 0f
        private var mSensorX = 0f
        private var mSensorY = 0f
        private var mHorizontalBound = 0f
        private var mVerticalBound = 0f
        private val mParticleSystem: ParticleSystem

        /*
         * Each of our particle holds its previous and current position, its
         * acceleration. for added realism each particle has its own friction
         * coefficient.
         */
        internal inner class Particle : View {
            var mPosX = Math.random().toFloat()
            var mPosY = Math.random().toFloat()
            private var mVelX = 0f
            private var mVelY = 0f

            constructor(context: Context?) : super(context) {}
            constructor(
                context: Context?,
                attrs: AttributeSet?
            ) : super(context, attrs) {
            }

            constructor(
                context: Context?,
                attrs: AttributeSet?,
                defStyleAttr: Int
            ) : super(context, attrs, defStyleAttr) {
            }

            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            constructor(
                context: Context?,
                attrs: AttributeSet?,
                defStyleAttr: Int,
                defStyleRes: Int
            ) : super(context, attrs, defStyleAttr, defStyleRes) {
            }

            fun computePhysics(sx: Float, sy: Float, dT: Float) {
                val ax = -sx / 5
                val ay = -sy / 5
                mPosX += mVelX * dT + ax * dT * dT / 2
                mPosY += mVelY * dT + ay * dT * dT / 2
                mVelX += ax * dT
                mVelY += ay * dT
            }

            /*
             * Resolving constraints and collisions with the Verlet integrator
             * can be very simple, we simply need to move a colliding or
             * constrained particle in such way that the constraint is
             * satisfied.
             */
            fun resolveCollisionWithBounds() {
                val xmax = mHorizontalBound
                val ymax = mVerticalBound
                val x = mPosX
                val y = mPosY
                if (x > xmax) {
                    mPosX = xmax
                    mVelX = 0f
                } else if (x < -xmax) {
                    mPosX = -xmax
                    mVelX = 0f
                }
                if (y > ymax) {
                    mPosY = ymax
                    mVelY = 0f
                } else if (y < -ymax) {
                    mPosY = -ymax
                    mVelY = 0f
                }
            }
        }

        /*
         * A particle system is just a collection of particles
         */
        internal inner class ParticleSystem {
            val mBalls =
                arrayOfNulls<Particle>(
                    5
                )

            /*
             * Update the position of each particle in the system using the
             * Verlet integrator.
             */
            private fun updatePositions(
                sx: Float,
                sy: Float,
                timestamp: Long
            ) {
                if (mLastT != 0L) {
                    val dT = (timestamp - mLastT).toFloat() / 1000f
                    /** (1.0f / 1000000000.0f) */
                    val count = mBalls.size
                    for (i in 0 until count) {
                        val ball =
                            mBalls[i]
                        ball!!.computePhysics(sx, sy, dT)
                    }
                }
                mLastT = timestamp
            }

            /*
             * Performs one iteration of the simulation. First updating the
             * position of all the particles and resolving the constraints and
             * collisions.
             */
            fun update(
                sx: Float,
                sy: Float,
                now: Long
            ) { // update the system's positions
                updatePositions(sx, sy, now)
                // We do no more than a limited number of iterations
                val NUM_MAX_ITERATIONS = 10
                /*
                 * Resolve collisions, each particle is tested against every
                 * other particle for collision. If a collision is detected the
                 * particle is moved away using a virtual spring of infinite
                 * stiffness.
                 */
                var more = true
                val count = mBalls.size
                var k = 0
                while (k < NUM_MAX_ITERATIONS && more) {
                    more = false
                    for (i in 0 until count) {
                        val curr =
                            mBalls[i]
                        for (j in i + 1 until count) {
                            val ball =
                                mBalls[j]
                            var dx = ball!!.mPosX - curr!!.mPosX
                            var dy = ball.mPosY - curr.mPosY
                            var dd = dx * dx + dy * dy
                            // Check for collisions
                            if (dd <= 0.004f * 0.004f) { /*
                                 * add a little bit of entropy, after nothing is
                                 * perfect in the universe.
                                 */
                                dx += (Math.random().toFloat() - 0.5f) * 0.0001f
                                dy += (Math.random().toFloat() - 0.5f) * 0.0001f
                                dd = dx * dx + dy * dy
                                // simulate the spring
                                val d =
                                    Math.sqrt(dd.toDouble()).toFloat()
                                val c =
                                    0.5f * (0.004f  - d) / d
                                val effectX = dx * c
                                val effectY = dy * c
                                curr.mPosX -= effectX
                                curr.mPosY -= effectY
                                ball.mPosX += effectX
                                ball.mPosY += effectY
                                more = true
                            }
                        }
                        curr!!.resolveCollisionWithBounds()
                    }
                    k++
                }
            }

            val particleCount: Int
                get() = mBalls.size

            fun getPosX(i: Int): Float {
                return mBalls[i]!!.mPosX
            }

            fun getPosY(i: Int): Float {
                return mBalls[i]!!.mPosY
            }


            init { /*
                 * Initially our particles have no speed or acceleration
                 */
                for (i in mBalls.indices) {
                    mBalls[i] =
                        Particle(
                            context
                        )
                    mBalls[i]!!.setBackgroundResource(R.mipmap.ball)
                    mBalls[i]!!.setLayerType(View.LAYER_TYPE_HARDWARE, null)
                    addView(mBalls[i], ViewGroup.LayoutParams(mDstWidth, mDstHeight))
                }
            }
        }

        fun startSimulation() { /*
             * It is not necessary to get accelerometer events at a very high
             * rate, by using a slower rate (SENSOR_DELAY_UI), we get an
             * automatic low-pass filter, which "extracts" the gravity component
             * of the acceleration. As an added benefit, we use less power and
             * CPU resources.
             */
            mSensorManager!!.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME)
        }

        fun stopSimulation() {
            mSensorManager!!.unregisterListener(this)
        }

        override fun onSizeChanged(
            w: Int,
            h: Int,
            oldw: Int,
            oldh: Int
        ) { // compute the origin of the screen relative to the origin of
// the bitmap
            mXOrigin = (w - mDstWidth) * 0.5f
            mYOrigin = (h - mDstHeight) * 0.5f
            mHorizontalBound =
                (w / mMetersToPixelsX - 0.004f) * 0.5f
            mVerticalBound =
                (h / mMetersToPixelsY - 0.004f) * 0.5f
        }

        override fun onSensorChanged(event: SensorEvent) {
            if (event.sensor.type != Sensor.TYPE_ACCELEROMETER) return
            when (mDisplay!!.rotation) {
                Surface.ROTATION_0 -> {
                    mSensorX = event.values[0]
                    mSensorY = event.values[1]
                }
                Surface.ROTATION_90 -> {
                    mSensorX = -event.values[1]
                    mSensorY = event.values[0]
                }
                Surface.ROTATION_180 -> {
                    mSensorX = -event.values[0]
                    mSensorY = -event.values[1]
                }
                Surface.ROTATION_270 -> {
                    mSensorX = event.values[1]
                    mSensorY = -event.values[0]
                }
            }
        }

        override fun onDraw(canvas: Canvas) { /*
             * Compute the new position of our object, based on accelerometer
             * data and present time.
             */
            val particleSystem = mParticleSystem
            val now = System.currentTimeMillis()
            val sx = mSensorX
            val sy = mSensorY
            particleSystem.update(sx, sy, now)
            val xc = mXOrigin
            val yc = mYOrigin
            val xs = mMetersToPixelsX
            val ys = mMetersToPixelsY
            val count = particleSystem.particleCount
            for (i in 0 until count) { /*
                 * We transform the canvas so that the coordinate system matches
                 * the sensors coordinate system with the origin in the center
                 * of the screen and the unit is the meter.
                 */
                val x = xc + particleSystem.getPosX(i) * xs
                val y = yc - particleSystem.getPosY(i) * ys
                particleSystem.mBalls[i]!!.translationX = x
                particleSystem.mBalls[i]!!.translationY = y
            }
            // and make sure to redraw asap
            invalidate()
        }

        override fun onAccuracyChanged(
            sensor: Sensor,
            accuracy: Int
        ) {
        }

        init {
            mAccelerometer =
                mSensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            val metrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(metrics)
            mXDpi = metrics.xdpi
            mYDpi = metrics.ydpi
            mMetersToPixelsX = mXDpi / 0.0254f
            mMetersToPixelsY = mYDpi / 0.0254f
            // rescale the ball so it's about 0.5 cm on screen
            mDstWidth = (0.004f * mMetersToPixelsX + 0.5f).toInt()
            mDstHeight = (0.004f * mMetersToPixelsY + 0.5f).toInt()
            mParticleSystem = ParticleSystem()
            val opts = BitmapFactory.Options()
            opts.inDither = true
            opts.inPreferredConfig = Bitmap.Config.RGB_565
        }
    }


    companion object{
        fun start(context: Context){
            context.startActivity(Intent(context,AccelerometerPlayActivity::class.java))
        }
    }

}