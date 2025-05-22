package tw.com.chainsea.chat.view.vision.barcode

import android.content.Context
import android.graphics.Point
import android.media.Image
import android.os.Build
import android.util.Log
import android.util.Size
import android.view.WindowManager
import androidx.annotation.OptIn
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.Preview.SurfaceProvider
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.lifecycle.LifecycleOwner
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions.Builder
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

/**
 * Qr code 掃描器
 * @param lifecycleOwner LifecycleOwner
 * @param surfaceProvider SurfaceProvider
 * @param callback 回傳掃描到的 qr code 字串
 * */
class QrcodeScanner(
    private val lifecycleOwner: LifecycleOwner,
    private val surfaceProvider: SurfaceProvider,
    private val callback: (String) -> Unit
) {
    private val tag = javaClass.simpleName
    private val qrcodeScanner: BarcodeScanner by lazy {
        val options = Builder().setBarcodeFormats(Barcode.FORMAT_QR_CODE).build()
        BarcodeScanning.getClient(options)
    }
    private val cameraExecutor = Executors.newFixedThreadPool(2)
    private var isPaused = false
    private var lastScanTime = 0L

    /**
     * 開始掃描
     * @param context Context
     * */
    @OptIn(ExperimentalCamera2Interop::class)
    fun startScan(context: Context) =
        CoroutineScope(Dispatchers.Main).launch {
            val cameraProvideFuture = ProcessCameraProvider.getInstance(context)
            cameraProvideFuture.addListener({
                CoroutineScope(Dispatchers.Main).launch {
                    val preview = Preview.Builder().build()
                    preview.setSurfaceProvider(surfaceProvider)
                    val imageAnalysis = getImageAnalyzer(context)
                    setImageAnalyzer(imageAnalysis)
                    bindCameraProvider(cameraProvideFuture, preview, imageAnalysis)
                }
            }, cameraExecutor)
        }

    /**
     * 釋放資源
     * */
    fun release() {
        qrcodeScanner.close()
        cameraExecutor.shutdown()
    }

    /**
     * @param imageAnalysis ImageAnalysis
     * */
    @OptIn(ExperimentalGetImage::class)
    private fun setImageAnalyzer(imageAnalysis: ImageAnalysis) =
        imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
            try {
                val mediaImage = imageProxy.image
                if (mediaImage != null) {
                    val image = getImage(mediaImage, imageProxy)
                    processQrcode(image, imageProxy)
                } else {
                    imageProxy.close() // 確保即使 mediaImage 為 null，也會釋放資源
                }
            } catch (e: Exception) {
                e.printStackTrace()
                imageProxy.close()
            }
        }

    /**
     * 綁定 camera
     * */
    private fun bindCameraProvider(
        cameraProvideFuture: ListenableFuture<ProcessCameraProvider>,
        preview: Preview,
        imageAnalysis: ImageAnalysis
    ) {
        try {
            val cameraProvider = cameraProvideFuture.get()
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageAnalysis)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
    }

    /**
     * 取得圖片
     * */
    private fun getImage(
        mediaImage: Image,
        imageProxy: ImageProxy
    ): InputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

    /**
     * 取得圖片掃描設定
     * */
    private fun getImageAnalyzer(context: Context): ImageAnalysis =
        ImageAnalysis
            .Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setResolutionSelector(getSupportResolutionSelector(context))
            .build()

    /**
     * 設定 scanner listener
     * */
    private fun processQrcode(
        image: InputImage,
        imageProxy: ImageProxy
    ) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastScanTime < 10) {
            imageProxy.close()
            return
        }
        lastScanTime = currentTime
        qrcodeScanner
            .process(image)
            .addOnSuccessListener {
                if (isPaused) {
                    imageProxy.close()
                    return@addOnSuccessListener
                }
                onScanResult(it)
                imageProxy.close()
            }.addOnFailureListener {
                Log.e(tag, "processQrcode: ${it.message}")
                imageProxy.close()
            }
    }

    /**
     * 掃描後 判斷是否有 qr code
     * @param result List<Barcode>
     * */
    private fun onScanResult(result: List<Barcode>) {
        if (result.isEmpty()) return
        pauseScan()
        for (barcode in result) {
            val rawValue = barcode.rawValue
            rawValue?.let {
                Log.e(tag, "onScanResult: found Qr code")
                callback.invoke(it)
            }
        }
    }

    /**
     * 解析度設定
     * */
    private fun getSupportResolutionSelector(context: Context): ResolutionSelector {
        val screenSize = getScreenResolution(context)
        return ResolutionSelector
            .Builder()
            .setResolutionStrategy(
                ResolutionStrategy(
                    screenSize,
                    ResolutionStrategy.FALLBACK_RULE_CLOSEST_LOWER
                )
            ).build()
    }

    /**
     * 取得螢幕支援的解析度
     * */
    private fun getScreenResolution(context: Context): Size {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = windowManager.currentWindowMetrics
            val bounds = windowMetrics.bounds
            Size(bounds.height(), bounds.width())
        } else {
            val display = windowManager.defaultDisplay
            val size = Point()
            display.getRealSize(size)
            Size(size.x, size.y)
        }
    }

    /**
     * 暫停掃描
     * */
    fun pauseScan() {
        isPaused = true
    }

    /**
     * 恢復掃描
     * */
    fun resumeScan() {
        isPaused = false
    }
}
