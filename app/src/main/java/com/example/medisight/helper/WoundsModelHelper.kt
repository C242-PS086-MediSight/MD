package com.example.medisight.helper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.*
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.io.InputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class WoundsModelHelper(private val context: Context) {

    private val labels = mapOf(
        0 to "Abrasions",
        1 to "Bruises",
        2 to "Burns",
        3 to "Cut",
        4 to "Normal"
    )

    private val interpreter: Interpreter

    init {
        interpreter = Interpreter(loadModelFile(), Interpreter.Options())
    }

    private fun loadModelFile(): MappedByteBuffer {
        context.assets.openFd("wounds_model.tflite").use { assetFileDescriptor ->
            FileInputStream(assetFileDescriptor.fileDescriptor).channel.use { fileChannel ->
                return fileChannel.map(
                    FileChannel.MapMode.READ_ONLY,
                    assetFileDescriptor.startOffset,
                    assetFileDescriptor.declaredLength
                )
            }
        }
    }

    private var inferenceRunning = false

    fun classifyImageAsync(uri: Uri, callback: (Pair<String, Float>) -> Unit) {
        if (inferenceRunning) {
            Log.d("DebugFlow", "Inference already running, skipping duplicate call.")
            return
        }
        inferenceRunning = true

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val inputArray = prepareInputFromUri(uri)
                val outputArray = Array(1) { FloatArray(labels.size) }
                interpreter.run(inputArray, outputArray)

                val results = outputArray[0]
                Log.d("DebugFlow", "Model output: ${results.joinToString(", ")}")

                withContext(Dispatchers.Main) {
                    val maxIndex = results.indices.maxByOrNull { results[it] } ?: 0
                    val label = labels[maxIndex] ?: "Unknown"
                    val confidence = results[maxIndex]

                    Log.d(
                        "ClassificationResult",
                        "Max Index: $maxIndex, Label: $label, Confidence: $confidence"
                    )

                    callback(Pair(label, confidence))

                    inferenceRunning = false
                }
            } catch (e: Exception) {
                Log.e("TFLiteError", "Classification error: ${e.message}")
                withContext(Dispatchers.Main) {
                    callback(Pair("Error", 0f))
                    inferenceRunning = false
                }
            }
        }
    }


    private fun prepareInputFromUri(uri: Uri): Array<Array<Array<FloatArray>>> {
        val bitmap = loadImageFromUri(uri)
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true)

        return Array(1) { Array(224) { Array(224) { FloatArray(3) } } }.apply {
            for (y in 0 until 224) {
                for (x in 0 until 224) {
                    val pixel = resizedBitmap.getPixel(x, y)
                    this[0][y][x][0] = (pixel shr 16 and 0xFF) / 255.0f
                    this[0][y][x][1] = (pixel shr 8 and 0xFF) / 255.0f
                    this[0][y][x][2] = (pixel and 0xFF) / 255.0f
                }
            }
        }
    }

    private fun loadImageFromUri(uri: Uri): Bitmap {
        val inputStream: InputStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("Cannot open URI: $uri")
        return BitmapFactory.decodeStream(inputStream).also {
            inputStream.close()
        }
    }
}