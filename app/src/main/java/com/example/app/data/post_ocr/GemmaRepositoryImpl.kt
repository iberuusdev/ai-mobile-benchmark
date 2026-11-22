package com.example.app.data.post_ocr

import android.content.Context
import com.example.app.domain.repository.post_ocr.PostOCREngineRepository
import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Contents
import com.google.ai.edge.litertlm.ConversationConfig
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import com.google.ai.edge.litertlm.SamplerConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

private const val MODEL_PATH = "gemma3-1b-it-int4.litertlm"

class GemmaRepositoryImpl(
    context: Context,
) : PostOCREngineRepository {

    private val appContext = context.applicationContext
    private val modelFile = File(
        appContext.filesDir,
        MODEL_PATH,
    )
    private var engine: Engine? = null
    private val engineMutex = Mutex()

    private suspend fun getEngine(): Engine = engineMutex.withLock {
        engine?.let { return it }

        withContext(Dispatchers.IO) {
            copyModelFromAssets()
        }

        val initializedEngine = withContext(Dispatchers.Default) {
            Engine(
                EngineConfig(
                    modelPath = modelFile.absolutePath,
                    backend = Backend.CPU(),
                    cacheDir = appContext.cacheDir.absolutePath,
                ),
            ).also { it.initialize() }
        }

        engine = initializedEngine
        initializedEngine
    }

    override suspend fun correctText(ocrText: String): String =
        withContext(Dispatchers.Default) {
            val currentEngine = getEngine()
            val prompt = """
                Corrige únicamente errores ortográficos y OCR del siguiente texto en español.
                Conserva nombres propios, números, marcas, siglas y términos técnicos.
                Mantén la puntuación y el formato que pueda conservarse.
                Si una palabra no puede corregirse con seguridad, déjala exactamente igual.
                Devuelve únicamente el texto corregido, sin explicaciones ni comentarios.
    
                Texto:
                $ocrText
            """.trimIndent()

            val config = ConversationConfig(
                systemInstruction = Contents.of(
                    "Eres un corrector conservador de texto OCR en español.",
                ),
                samplerConfig = SamplerConfig(
                    topK = 1,
                    topP = 1.0,
                    temperature = 0.0,
                ),
            )

            currentEngine.createConversation(config).use { conversation ->
                val response = conversation.sendMessage(prompt).toString()
                response
                    .substringAfter("Texto corregido:", response)
                    .trim()
                    .ifEmpty { ocrText }
            }
        }

    override suspend fun close() {
        engine?.close()
    }

    private fun copyModelFromAssets() {
        if (modelFile.exists() && modelFile.length() > 0) return

        appContext.assets.open("gemma3-1b-it-int4.litertlm").use { input ->
            modelFile.parentFile?.mkdirs()
            modelFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    }
}