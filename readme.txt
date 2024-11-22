Vincente Buenaventura
301422086


I have used, implemented, and built on top of the code below, that was shown in lectures in my assignment 5.



/*
 *  Copyright 2006-2007 Columbia University.
 *
 *  This file is part of MEAPsoft.
 *
 *  MEAPsoft is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License version 2 as
 *  published by the Free Software Foundation.
 *
 *  MEAPsoft is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with MEAPsoft; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 *  02110-1301 USA
 *
 *  See the file "COPYING" for the text of the license.
 */

package com.xd.myrundatacollectorkotlin;

/**
 * Utility class to perform a fast fourier transform without allocating any
 * extra memory.
 * 
 * @author Mike Mandel (mim@ee.columbia.edu)
 */

//I removed some unused variables to get rid of warnings
//Xiaochao

public class FFT {

	int n, m;

	// Lookup tables. Only need to recompute when size of FFT changes.
	double[] cos;
	double[] sin;

	double[] window;

	public FFT(int n) {
		this.n = n;
		this.m = (int) (Math.log(n) / Math.log(2));

		// Make sure n is a power of 2
		if (n != (1 << m))
			throw new RuntimeException("FFT length must be power of 2");

		// precompute tables
		cos = new double[n / 2];
		sin = new double[n / 2];

		// for(int i=0; i<n/4; i++) {
		// cos[i] = Math.cos(-2*Math.PI*i/n);
		// sin[n/4-i] = cos[i];
		// cos[n/2-i] = -cos[i];
		// sin[n/4+i] = cos[i];
		// cos[n/2+i] = -cos[i];
		// sin[n*3/4-i] = -cos[i];
		// cos[n-i] = cos[i];
		// sin[n*3/4+i] = -cos[i];
		// }

		for (int i = 0; i < n / 2; i++) {
			cos[i] = Math.cos(-2 * Math.PI * i / n);
			sin[i] = Math.sin(-2 * Math.PI * i / n);
		}

		makeWindow();
	}

	protected void makeWindow() {
		// Make a blackman window:
		// w(n)=0.42-0.5cos{(2*PI*n)/(N-1)}+0.08cos{(4*PI*n)/(N-1)};
		window = new double[n];
		for (int i = 0; i < window.length; i++)
			window[i] = 0.42 - 0.5 * Math.cos(2 * Math.PI * i / (n - 1)) + 0.08
					* Math.cos(4 * Math.PI * i / (n - 1));
	}

	public double[] getWindow() {
		return window;
	}

	/***************************************************************
	 * fft.c Douglas L. Jones University of Illinois at Urbana-Champaign January
	 * 19, 1992 http://cnx.rice.edu/content/m12016/latest/
	 * 
	 * fft: in-place radix-2 DIT DFT of a complex input
	 * 
	 * input: n: length of FFT: must be a power of two m: n = 2**m input/output
	 * x: double array of length n with real part of data y: double array of
	 * length n with imag part of data
	 * 
	 * Permission to copy and use this program is granted as long as this header
	 * is included.
	 ****************************************************************/
	public void fft(double[] x, double[] y) {
		int i, j, k, n1, n2, a;
		double c, s, t1, t2;

		// Bit-reverse
		j = 0;
		n2 = n / 2;
		for (i = 1; i < n - 1; i++) {
			n1 = n2;
			while (j >= n1) {
				j = j - n1;
				n1 = n1 / 2;
			}
			j = j + n1;

			if (i < j) {
				t1 = x[i];
				x[i] = x[j];
				x[j] = t1;
				t1 = y[i];
				y[i] = y[j];
				y[j] = t1;
			}
		}

		// FFT
		n1 = 0;
		n2 = 1;

		for (i = 0; i < m; i++) {
			n1 = n2;
			n2 = n2 + n2;
			a = 0;

			for (j = 0; j < n1; j++) {
				c = cos[a];
				s = sin[a];
				a += 1 << (m - i - 1);

				for (k = j; k < n; k = k + n2) {
					t1 = c * x[k + n1] - s * y[k + n1];
					t2 = s * x[k + n1] + c * y[k + n1];
					x[k + n1] = x[k] - t1;
					y[k + n1] = y[k] - t2;
					x[k] = x[k] + t1;
					y[k] = y[k] + t2;
				}
			}
		}
	}

	// Test the FFT to make sure it's working
	public static void main(String[] args) {
		int N = 8;

		FFT fft = new FFT(N);

		double[] re = new double[N];
		double[] im = new double[N];

		// Impulse
		re[0] = 1;
		im[0] = 0;
		for (int i = 1; i < N; i++)
			re[i] = im[i] = 0;
		beforeAfter(fft, re, im);

		// Nyquist
		for (int i = 0; i < N; i++) {
			re[i] = Math.pow(-1, i);
			im[i] = 0;
		}
		beforeAfter(fft, re, im);

		// Single sin
		for (int i = 0; i < N; i++) {
			re[i] = Math.cos(2 * Math.PI * i / N);
			im[i] = 0;
		}
		beforeAfter(fft, re, im);

		// Ramp
		for (int i = 0; i < N; i++) {
			re[i] = i;
			im[i] = 0;
		}
		beforeAfter(fft, re, im);

		long time = System.currentTimeMillis();
		double iter = 30000;
		for (int i = 0; i < iter; i++)
			fft.fft(re, im);
		time = System.currentTimeMillis() - time;
		System.out.println("Averaged " + (time / iter) + "ms per iteration");
	}

	public static void beforeAfter(FFT fft, double[] re, double[] im) {
		System.out.println("Before: ");
		printReIm(re, im);
		fft.fft(re, im);
		System.out.println("After: ");
		printReIm(re, im);
	}

	public static void printReIm(double[] re, double[] im) {
		System.out.print("Re: [");
		for (int i = 0; i < re.length; i++)
			System.out.print(((int) (re[i] * 1000) / 1000.0) + " ");

		System.out.print("]\nIm: [");
		for (int i = 0; i < im.length; i++)
			System.out.print(((int) (im[i] * 1000) / 1000.0) + " ");

		System.out.println("]");
	}
}





package com.xd.myrundatacollectorkotlin

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.AsyncTask
import android.os.Environment
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import weka.core.Attribute
import weka.core.DenseInstance
import weka.core.Instance
import weka.core.Instances
import weka.core.converters.ArffSaver
import weka.core.converters.ConverterUtils
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.text.DecimalFormat
import java.util.*
import java.util.concurrent.ArrayBlockingQueue


class SensorService : Service(), SensorEventListener {
    private val mFeatLen = Globals.ACCELEROMETER_BLOCK_CAPACITY + 2
    private lateinit var mFeatureFile: File
    private lateinit var mSensorManager: SensorManager
    private lateinit var mAccelerometer: Sensor
    private var mServiceTaskType = 0
    private lateinit var mLabel: String
    private lateinit var mDataset: Instances
    private lateinit var mClassAttribute: Attribute
    private lateinit var mAsyncTask: OnSensorChangedTask
    private lateinit var mAccBuffer: ArrayBlockingQueue<Double>
    val mdf = DecimalFormat("#.##")

    override fun onCreate() {
        super.onCreate()
        mAccBuffer = ArrayBlockingQueue<Double>(Globals.ACCELEROMETER_BUFFER_CAPACITY)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        mSensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST)
        val extras = intent.extras
        mLabel = extras!!.getString(Globals.CLASS_LABEL_KEY)!!
        mFeatureFile = File(getExternalFilesDir(null), Globals.FEATURE_FILE_NAME)
        Log.d(Globals.TAG, mFeatureFile.absolutePath)
        mServiceTaskType = Globals.SERVICE_TASK_TYPE_COLLECT

        // Create the container for attributes
        val allAttr = ArrayList<Attribute>()

        // Adding FFT coefficient attributes
        val df = DecimalFormat("0000")
        for (i in 0 until Globals.ACCELEROMETER_BLOCK_CAPACITY) {
            allAttr.add(Attribute(Globals.FEAT_FFT_COEF_LABEL + df.format(i.toLong())))
        }
        // Adding the max feature
        allAttr.add(Attribute(Globals.FEAT_MAX_LABEL))

        // Declare a nominal attribute along with its candidate values
        val labelItems = ArrayList<String>(3)
        labelItems.add(Globals.CLASS_LABEL_STANDING)
        labelItems.add(Globals.CLASS_LABEL_WALKING)
        labelItems.add(Globals.CLASS_LABEL_RUNNING)
        labelItems.add(Globals.CLASS_LABEL_OTHER)
        mClassAttribute = Attribute(Globals.CLASS_LABEL_KEY, labelItems)
        allAttr.add(mClassAttribute)

        // Construct the dataset with the attributes specified as allAttr and
        // capacity 10000
        mDataset = Instances(Globals.FEAT_SET_NAME, allAttr, Globals.FEATURE_SET_CAPACITY)

        // Set the last column/attribute (standing/walking/running) as the class
        // index for classification
        mDataset.setClassIndex(mDataset.numAttributes() - 1)
        val i = Intent(this, MainActivity::class.java)
        // Read:
        // http://developer.android.com/guide/topics/manifest/activity-element.html#lmode
        // IMPORTANT!. no re-create activity
        i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val pi = PendingIntent.getActivity(this, 0, i, 0)
        val notification: Notification = Notification.Builder(this)
                .setContentTitle(
                        applicationContext.getString(
                                R.string.ui_sensor_service_notification_title))
                .setContentText(
                        resources
                                .getString(
                                        R.string.ui_sensor_service_notification_content))
                .setSmallIcon(R.drawable.logo).setContentIntent(pi).build()
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notification.flags = (notification.flags
                or Notification.FLAG_ONGOING_EVENT)
        notificationManager.notify(0, notification)
        mAsyncTask = OnSensorChangedTask()
        mAsyncTask.execute()
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        mAsyncTask.cancel(true)
        try {
            Thread.sleep(100)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        mSensorManager.unregisterListener(this)
        Log.i("", "")
        super.onDestroy()
    }

    inner class OnSensorChangedTask : AsyncTask<Void, Void, Void>() {
        override fun doInBackground(vararg arg0: Void?): Void? {
            val inst: Instance = DenseInstance(mFeatLen)
            inst.setDataset(mDataset)
            var blockSize = 0
            val fft = FFT(Globals.ACCELEROMETER_BLOCK_CAPACITY)
            val accBlock = DoubleArray(Globals.ACCELEROMETER_BLOCK_CAPACITY)
            val im = DoubleArray(Globals.ACCELEROMETER_BLOCK_CAPACITY)
            var max = Double.MIN_VALUE
            while (true) {
                try {
                    // need to check if the AsyncTask is cancelled or not in the while loop
                    if (isCancelled() == true) {
                        return null
                    }

                    // Dumping buffer
                    accBlock[blockSize++] = mAccBuffer.take().toDouble()
                    if (blockSize == Globals.ACCELEROMETER_BLOCK_CAPACITY) {
                        blockSize = 0

                        // time = System.currentTimeMillis();
                        max = .0
                        for (`val` in accBlock) {
                            if (max < `val`) {
                                max = `val`
                            }
                        }
                        fft.fft(accBlock, im)
                        for (i in accBlock.indices) {
                            val mag = Math.sqrt(accBlock[i] * accBlock[i] + im[i]
                                    * im[i])
                            inst.setValue(i, mag)
                            im[i] = .0 // Clear the field
                        }

                        // Append max after frequency component
                        inst.setValue(Globals.ACCELEROMETER_BLOCK_CAPACITY, max)
                        inst.setValue(mClassAttribute, mLabel)
                        mDataset.add(inst)
                        Log.i("new instance", mDataset.size.toString() + "")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        override fun onCancelled() {
            Log.e("123", mDataset.size.toString() + "")
            if (mServiceTaskType == Globals.SERVICE_TASK_TYPE_CLASSIFY) {
                super.onCancelled()
                return
            }
            Log.i("in the loop", "still in the loop cancelled")
            var toastDisp: String
            if (mFeatureFile.exists()) {

                // merge existing and delete the old dataset
                val source: ConverterUtils.DataSource
                try {
                    // Create a datasource from mFeatureFile where
                    // mFeatureFile = new File(getExternalFilesDir(null),
                    // "features.arff");
                    source = ConverterUtils.DataSource(FileInputStream(mFeatureFile))
                    // Read the dataset set out of this datasource
                    val oldDataset = source.dataSet
                    oldDataset.setClassIndex(mDataset.numAttributes() - 1)
                    // Sanity checking if the dataset format matches.
                    if (!oldDataset.equalHeaders(mDataset)) {
                        // Log.d(Globals.TAG,
                        // oldDataset.equalHeadersMsg(mDataset));
                        throw java.lang.Exception(
                                "The two datasets have different headers:\n")
                    }

                    // Move all items over manually
                    for (i in mDataset.indices) {
                        oldDataset.add(mDataset[i])
                    }
                    mDataset = oldDataset
                    // Delete the existing old file.
                    mFeatureFile.delete()
                    Log.i("delete", "delete the file")
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
                toastDisp = getString(R.string.ui_sensor_service_toast_success_file_updated)
            } else {
                toastDisp = getString(R.string.ui_sensor_service_toast_success_file_created)
            }
            Log.i("save", "create saver here")
            // create new Arff file
            val saver = ArffSaver()
            // Set the data source of the file content
            saver.instances = mDataset
            Log.e("1234", mDataset.size.toString() + "")
            try {
                // Set the destination of the file.
                // mFeatureFile = new File(getExternalFilesDir(null),
                // "features.arff");
                saver.setFile(mFeatureFile)
                // Write into the file
                saver.writeBatch()
                Log.i("batch", "write batch here")
                Toast.makeText(applicationContext, toastDisp,
                        Toast.LENGTH_SHORT).show()
            } catch (e: IOException) {
                toastDisp = getString(R.string.ui_sensor_service_toast_error_file_saving_failed)
                e.printStackTrace()
            }
            Log.i("toast", "toast here")
            super.onCancelled()
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_LINEAR_ACCELERATION) {
            val m = Math.sqrt((event.values[0] * event.values[0] + event.values[1] * event.values[1] + (event.values[2]
                    * event.values[2])).toDouble())

            // Inserts the specified element into this queue if it is possible
            // to do so immediately without violating capacity restrictions,
            // returning true upon success and throwing an IllegalStateException
            // if no space is currently available. When using a
            // capacity-restricted queue, it is generally preferable to use
            // offer.
            try {
                mAccBuffer.add(m)
            } catch (e: IllegalStateException) {

                // Exception happens when reach the capacity.
                // Doubling the buffer. ListBlockingQueue has no such issue,
                // But generally has worse performance
                val newBuf = ArrayBlockingQueue<Double>(mAccBuffer.size * 2)
                mAccBuffer.drainTo(newBuf)
                mAccBuffer = newBuf
                mAccBuffer.add(m)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}





package com.xd.myrundatacollectorkotlin

import android.Manifest
import android.app.Activity
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File

class MainActivity : AppCompatActivity() {
    private enum class State {
        IDLE, COLLECTING, TRAINING, CLASSIFYING
    }

    private val mLabels = arrayOf<String>(Globals.CLASS_LABEL_STANDING,
            Globals.CLASS_LABEL_WALKING, Globals.CLASS_LABEL_RUNNING,
            Globals.CLASS_LABEL_OTHER)

    private lateinit var radioGroup: RadioGroup
    private val radioBtns = arrayOfNulls<RadioButton>(4)
    private lateinit var mServiceIntent: Intent
    private lateinit var mFeatureFile: File
    private lateinit var mState: State
    private lateinit var btnDelete: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setContentView(R.layout.activity_main)
        checkPermissions(this)
        radioGroup = findViewById(R.id.radioGroupLabels) as RadioGroup
        radioBtns[0] = findViewById(R.id.radioStanding) as RadioButton
        radioBtns[1] = findViewById(R.id.radioWalking) as RadioButton
        radioBtns[2] = findViewById(R.id.radioRunning) as RadioButton
        radioBtns[3] = findViewById(R.id.radioOther) as RadioButton

        btnDelete = findViewById(R.id.btnDeleteData) as Button

        mState = State.IDLE
        mFeatureFile = File(getExternalFilesDir(null), Globals.FEATURE_FILE_NAME)
        mServiceIntent = Intent(this, SensorService::class.java)
    }

    fun onCollectClicked(view: View) {
        if (mState == State.IDLE) {
            mState = State.COLLECTING
            (view as Button).setText(R.string.ui_collector_button_stop_title)
            btnDelete.isEnabled = false
            radioBtns[0]!!.isEnabled = false
            radioBtns[1]!!.isEnabled = false
            radioBtns[2]!!.isEnabled = false
            radioBtns[3]!!.isEnabled = false
            val acvitivtyId = radioGroup.indexOfChild(findViewById(radioGroup.checkedRadioButtonId))
            val label = mLabels[acvitivtyId]
            val extras = Bundle()
            extras.putString(Globals.CLASS_LABEL_KEY, label)
            mServiceIntent.putExtras(extras)
            startService(mServiceIntent)
        } else if (mState == State.COLLECTING) {
            mState = State.IDLE
            (view as Button).setText(R.string.ui_collector_button_start_title)
            btnDelete.isEnabled = true
            radioBtns[0]!!.isEnabled = true
            radioBtns[1]!!.isEnabled = true
            radioBtns[2]!!.isEnabled = true
            radioBtns[3]!!.isEnabled = true
            stopService(mServiceIntent)
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).cancelAll()
        }
    }

    fun onDeleteDataClicked(view: View?) {

        if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()) {
            if (mFeatureFile.exists()) {
                mFeatureFile.delete()
            }
            Toast.makeText(applicationContext,
                    R.string.ui_collector_toast_file_deleted,
                    Toast.LENGTH_SHORT).show()
        }
    }

    override fun onBackPressed() {
        if (mState == State.TRAINING) {
            return
        } else if (mState == State.COLLECTING || mState == State.CLASSIFYING) {
            stopService(mServiceIntent)
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).cancel(Globals.NOTIFICATION_ID)
        }
        super.onBackPressed()
    }

    override fun onDestroy() {
        // Stop the service and the notification.
        // Need to check whether the mSensorService is null or not.
        if (mState == State.TRAINING) {
            return
        } else if (mState == State.COLLECTING || mState == State.CLASSIFYING) {
            stopService(mServiceIntent)
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).cancelAll()
        }
        finish()
        super.onDestroy()
    }

    fun checkPermissions(activity: Activity) {
        if (Build.VERSION.SDK_INT < 23) return
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) !== PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 0)
        }
    }
}





package com.example.shakesensorkotlin

import android.content.Intent
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

class MainActivity : AppCompatActivity(), SensorEventListener {
    private lateinit var xLabel: TextView
    private lateinit var yLabel: TextView
    private lateinit var zLabel: TextView
    private lateinit var titleLabel: TextView
    private lateinit var sensorManager: SensorManager
    private var x: Double = 0.0
    private var y: Double = 0.0
    private var z: Double = 0.0
    private var lastTime: Long = 0
    private var currentTime: Long =0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        xLabel = findViewById(R.id.xval)
        yLabel = findViewById(R.id.yval)
        zLabel = findViewById(R.id.zval)
        titleLabel = findViewById(R.id.textView)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        lastTime = System.currentTimeMillis()
    }

    override fun onResume() {
        super.onResume()
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null && event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            x = (event.values[0] / SensorManager.GRAVITY_EARTH).toDouble()
            y = (event.values[1] / SensorManager.GRAVITY_EARTH).toDouble()
            z = (event.values[2] / SensorManager.GRAVITY_EARTH).toDouble()
            xLabel.text = "X axis: $x"
            yLabel.text = "Y axis: $y"
            zLabel.text = "Z axis: $z"
            checkShake()
        }
    }

    private fun checkShake() {
        val magnitude = Math.sqrt(x * x + y * y + z * z)
        currentTime = System.currentTimeMillis()
        if (magnitude > 3 && currentTime - lastTime > 300) {
            titleLabel.setBackgroundColor(Color.RED)
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel: 123456")
            startActivity(intent)
            lastTime = currentTime
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

}