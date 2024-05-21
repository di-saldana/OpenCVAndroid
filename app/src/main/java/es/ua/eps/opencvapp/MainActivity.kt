package es.ua.eps.opencvapp

import android.os.Bundle
import es.ua.eps.opencvapp.databinding.ActivityMainBinding

import android.Manifest
import android.content.pm.PackageManager
import android.widget.TextView
import androidx.annotation.NonNull
import com.google.android.material.slider.Slider
import org.opencv.android.CameraActivity
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.OpenCVLoader
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import java.util.Collections

class MainActivity : CameraActivity() {

    private lateinit var bindings: ActivityMainBinding
    private lateinit var cameraBridgeViewBase: CameraBridgeViewBase

    private val cameraCode = 500
    private var cannyFilterEnabled = false
    private var blur: Float = 3.0f
    private var edge_gradient: Float = 60.0f
    private var angle: Float = 3.0f

    private lateinit var slideBlur: Slider
    private lateinit var slideEdge: Slider
    private lateinit var slideAngle: Slider
    private lateinit var blurValue: TextView
    private lateinit var edgeValue: TextView
    private lateinit var angleValue: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bindings = ActivityMainBinding.inflate(layoutInflater)

        with(bindings) {
            setContentView(root)

            requestPermission()
            cameraBridgeViewBase = camera
            slideBlur = sliderBlur
            slideEdge = sliderEdge
            slideAngle = sliderAngle

            blurValue = blurVal
            edgeValue = edgeVal
            angleValue = angleVal

            blur = 3.0f
            angle = 3.0f
            edge_gradient = 60.0f

            button.setOnClickListener {
                cannyFilterEnabled = !cannyFilterEnabled
                button.text = if (cannyFilterEnabled) "Deshabilitar Filtro" else "Aplicar Filtro"
                if (cannyFilterEnabled) {
                    aplicarCannyFilter()
                } else {
                    deshabilitarCannyFilter()
                }
            }
        }

        cameraBridgeViewBase.setCvCameraViewListener(object : CameraBridgeViewBase.CvCameraViewListener2 {
            override fun onCameraViewStarted(width: Int, height: Int) {}
            override fun onCameraViewStopped() {}
            override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame): Mat {
                return inputFrame.rgba()
            }
        })

        if (OpenCVLoader.initDebug()) {
            cameraBridgeViewBase.enableView()
        }

        slideBlur.addOnChangeListener { slider, value, fromUser ->
            blurValue.text = value.toInt().toString()
            blur = value
        }

        slideEdge.addOnChangeListener { slider, value, fromUser ->
            edgeValue.text = value.toInt().toString()
            edge_gradient = value
        }

        slideAngle.addOnChangeListener { slider, value, fromUser ->
            angleValue.text = value.toInt().toString()
            angle = value
        }
    }

    override fun onResume() {
        super.onResume()
        cameraBridgeViewBase.enableView()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraBridgeViewBase.disableView()
    }

    override fun onPause() {
        super.onPause()
        cameraBridgeViewBase.disableView()
    }

    override fun getCameraViewList(): List<CameraBridgeViewBase> {
        return Collections.singletonList(cameraBridgeViewBase)
    }

    private fun aplicarCannyFilter() {
        cameraBridgeViewBase.setCvCameraViewListener(object : CameraBridgeViewBase.CvCameraViewListener2 {
            override fun onCameraViewStarted(width: Int, height: Int) {}
            override fun onCameraViewStopped() {}

            override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame): Mat {
                val grayMat = Mat()
                val blurredMat = Mat()
                val edgesMat = Mat()

                Imgproc.cvtColor(inputFrame.rgba(), grayMat, Imgproc.COLOR_RGBA2GRAY)
                Imgproc.GaussianBlur(grayMat, blurredMat, Size(blur.toDouble(), blur.toDouble()), 0.0)

                Imgproc.Canny(blurredMat, edgesMat, edge_gradient.toDouble(),
                    (edge_gradient * angle).toDouble()
                )
                return edgesMat
            }
        })
    }

    private fun deshabilitarCannyFilter() {
        cameraBridgeViewBase.setCvCameraViewListener(object : CameraBridgeViewBase.CvCameraViewListener2 {
            override fun onCameraViewStarted(width: Int, height: Int) {}
            override fun onCameraViewStopped() {}
            override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame): Mat {
                return inputFrame.rgba()
            }
        })
    }

    private fun requestPermission() {
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), cameraCode)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, @NonNull permissions: Array<out String>, @NonNull grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            requestPermission()
        }
    }
}

