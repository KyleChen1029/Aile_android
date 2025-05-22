//package tw.com.chainsea.chat.util
//
//import android.util.Log
//import com.arthenica.ffmpegkit.FFmpegKit
//import com.arthenica.ffmpegkit.FFmpegKitConfig
//import com.arthenica.ffmpegkit.FFprobeKit
//import com.arthenica.ffmpegkit.SessionState
//import java.io.File
//import java.text.DecimalFormat
//
//object FfmpegUtil {
//
//    fun convertVideoCodec(inputPath: String, path: String, outputPathCallback: (String) -> Unit, progressCallback: (String) -> Unit) {
//        val mediaInformation = FFprobeKit.getMediaInformation(inputPath).mediaInformation
//        var videoBitRate = 0
//        var audioBitRate = 0
//        var fileSize = 0L
//        var tmpProgress = ""
//        val outputPath = path + "/video_" + inputPath.split("/").last() //APP內部存儲目錄的路徑
//        val file = File(outputPath)
//        if(file.exists()) {
//            outputPathCallback.invoke(outputPath)
//        } else {
//            Log.d("VideoConverter", "path = $path")
//            for (stream in mediaInformation.streams) {
//                when (stream.allProperties.getString("codec_type")) {
//                    "video" -> {
//                        videoBitRate = stream.allProperties.getInt("bit_rate")
//                    }
//
//                    "audio" -> {
//                        audioBitRate = stream.allProperties.getInt("bit_rate")
//                    }
//                }
//            }
//
//            fileSize = mediaInformation.size.toLong()
//
//            val command = "-i $inputPath -vcodec h264 -acodec aac -b:a $audioBitRate -b:v $videoBitRate -preset ultrafast $outputPath"
//
//            FFmpegKit.executeAsync(
//                command,
//                { session ->
//                    val state = session.state
//                    val returnCode = session.returnCode
//                    progressCallback.invoke("6666666") //影片轉碼完成
//                    Log.d("VideoConverter", String.format("FFmpeg process exited with state %s and rc %s.%s", FFmpegKitConfig.sessionStateToString(state), returnCode, session.failStackTrace, "\n"))
//                    if (state == SessionState.FAILED || !returnCode.isValueSuccess) {
//                        outputPathCallback.invoke(session.failStackTrace)
//                        Log.e("VideoConverter", "Error: ${session.failStackTrace}")
//                    } else {
//                        outputPathCallback.invoke(outputPath)//影片轉碼完成，發送轉碼後影片路徑
//                    }
//                },
//                {
//
//                },
//                { statistics ->
//                    statistics?.let {
//                        val progress = (it.size.toDouble() / fileSize) * 100
//                        val df = DecimalFormat("#")
//                        val rounded = df.format(progress)
//                        if (rounded != tmpProgress) {
//                            tmpProgress = rounded
//                            Log.d("VideoConverter", "size=${it.size}, fileSize=$fileSize, progress = $tmpProgress")
//                            progressCallback.invoke(tmpProgress)
//                        }
//                    }
//                }
//            )
//        }
//    }
//
//    fun isVideoCodecCorrect(inputPath: String): Boolean {
//        var isH264 = false
//        var isAAC = false
//        val mediaInformation = FFprobeKit.getMediaInformation(inputPath).mediaInformation
//        val isMp4 = inputPath.endsWith(".mp4")
//        for (stream in mediaInformation.streams) {
//            Log.d("VideoConverter", "codec_name=${stream.allProperties.getString("codec_name")}, codec_type=${stream.allProperties.getString("codec_type")}, bit_rate=${stream.allProperties.getString("bit_rate")}, path=$inputPath")
//            when(stream.allProperties.getString("codec_type")){
//                "video" -> {
//                    if(stream.allProperties.getString("codec_name") == "h264")
//                        isH264 = true
//                }
//                "audio" -> {
//                    if(stream.allProperties.getString("codec_name") == "aac")
//                        isAAC = true
//                }
//            }
//            if(isH264 && isAAC) break
//        }
//        return isMp4 && isH264 && isAAC
//    }
//
//}
