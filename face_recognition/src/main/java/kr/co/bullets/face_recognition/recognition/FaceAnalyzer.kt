package kr.co.bullets.face_recognition.recognition

import android.media.Image
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.view.PreviewView
import androidx.lifecycle.Lifecycle
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions

internal class FaceAnalyzer(lifecycle: Lifecycle, private val previewView: PreviewView, private val listener: FaceAnalyzerListener?) : ImageAnalysis.Analyzer{
    private var widthScaleFactor = 1F
    private var heightScaleFactor = 1F

    private val options = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
        .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
        .setMinFaceSize(0.4F)
        .build()

    private val detector = FaceDetection.getClient(options)
    private var detectStatus = FaceAnalyzerStatus.UnDetect

    private val successListener = OnSuccessListener<List<Face>> { faces ->
        val face = faces.firstOrNull()
        if (face != null) {
            if (detectStatus == FaceAnalyzerStatus.UnDetect) {
                detectStatus = FaceAnalyzerStatus.Detect
                listener?.detect()
                listener?.detectProgress(25F, "얼굴을 인식했습니다.\n왼쪽 눈만 깜빡여주세요.")
            } else if (detectStatus == FaceAnalyzerStatus.Detect && (face.leftEyeOpenProbability ?: 0F) > EYE_SUCCESS_VALUE  && (face.rightEyeOpenProbability ?: 0F) < EYE_SUCCESS_VALUE) {
                detectStatus = FaceAnalyzerStatus.LeftWink
                listener?.detectProgress(50F, "오른쪽 눈만 깜빡여주세요.")
            } else if (detectStatus == FaceAnalyzerStatus.LeftWink && (face.leftEyeOpenProbability ?: 0F) < EYE_SUCCESS_VALUE && (face.rightEyeOpenProbability ?: 0F) > EYE_SUCCESS_VALUE) {
                detectStatus = FaceAnalyzerStatus.RightWink
                listener?.detectProgress(75F, "활짝 웃어보세요.")
            } else if (detectStatus == FaceAnalyzerStatus.RightWink && (face.smilingProbability ?: 0F) > SMILE_SUCCESS_VALUE) {
                detectStatus = FaceAnalyzerStatus.Smile
                listener?.detectProgress(100F, "얼굴 인식이 완료되었습니다.")
                listener?.stopDetect()
                detector.close()
            } else if (detectStatus != FaceAnalyzerStatus.UnDetect && detectStatus != FaceAnalyzerStatus.Smile) {
                detectStatus = FaceAnalyzerStatus.UnDetect
                listener?.notDetect()
                listener?.detectProgress(0F, "얼굴을 인식하지 못했습니다.\n처음으로 돌아갑니다.")
            }
        }
    }

    private val failureListener = OnFailureListener { e ->
        detectStatus = FaceAnalyzerStatus.UnDetect
    }

    init {
        lifecycle.addObserver(detector)
    }

    override fun analyze(image: ImageProxy) {
        widthScaleFactor = previewView.width.toFloat() / image.height
        heightScaleFactor = previewView.height.toFloat() / image.width
        detectFaces(image)
    }

    private fun detectFaces(imageProxy: ImageProxy) {
        val image = InputImage.fromMediaImage(imageProxy.image as Image, imageProxy.imageInfo.rotationDegrees)
        detector.process(image)
            .addOnSuccessListener(successListener)
            .addOnFailureListener(failureListener)
            .addOnCompleteListener {
                imageProxy.close()
            }
    }

    companion object {
        private const val EYE_SUCCESS_VALUE = 0.1F
        private const val SMILE_SUCCESS_VALUE = 0.8F
    }
}