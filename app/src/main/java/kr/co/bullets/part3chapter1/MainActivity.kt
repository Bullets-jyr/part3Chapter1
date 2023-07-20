package kr.co.bullets.part3chapter1

import android.graphics.PointF
import android.graphics.RectF
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.transition.TransitionManager
import android.util.SizeF
import androidx.core.view.isVisible
import kr.co.bullets.face_recognition.Camera
import kr.co.bullets.face_recognition.recognition.FaceAnalyzerListener
import kr.co.bullets.part3chapter1.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), FaceAnalyzerListener {
    private lateinit var binding: ActivityMainBinding

    private val camera = Camera(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setProgressText("시작하기를 눌러주세요")

        camera.initCamera(binding.cameraLayout, this)

        binding.startDetectButton.setOnClickListener {
            it.isVisible = false
            binding.overlayView.reset()
            camera.startFaceDetect()
            setProgressText("얼굴을 보여주세요")
        }
    }

    override fun detect() {

    }

    override fun stopDetect() {
        camera.stopFaceDetect()
        reset()
    }

    override fun notDetect() {
        binding.overlayView.reset()
    }

    override fun detectProgress(progress: Float, message: String) {
        setProgressText(message)
        binding.overlayView.setProgress(progress)
    }

    override fun faceSize(rectF: RectF, sizeF: SizeF, pointF: PointF) {
        binding.overlayView.setSize(rectF, sizeF, pointF)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        camera.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun reset() {
        binding.startDetectButton.isVisible = true
    }

    private fun setProgressText(text: String) {
        TransitionManager.beginDelayedTransition(binding.root)
        binding.progressTextView.text = text
    }
}