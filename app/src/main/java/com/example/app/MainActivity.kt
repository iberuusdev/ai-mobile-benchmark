package com.example.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import com.example.app.data.ocr.OCRAssetLoaderRepositoryImpl
import com.example.app.data.ocr.OCREvaluatorRepositoryImpl
import com.example.app.data.post_ocr.PostOCRAssetLoaderRepositoryImpl
import com.example.app.data.post_ocr.PostOCREvaluatorRepositoryImpl
import com.example.app.domain.usecase.ocr.LoadOCRAssetsUseCase
import com.example.app.domain.usecase.ocr.RunOCRUseCase
import com.example.app.domain.usecase.post_ocr.LoadPostOCRAssetsUseCase
import com.example.app.domain.usecase.post_ocr.RunPostOCRUseCase
import com.example.app.features.ocr.OCRBenchmarkScreen
import com.example.app.features.ocr.OCRBenchmarkViewModel
import com.example.app.features.post_ocr.PostOCRBenchmarkScreen
import com.example.app.features.post_ocr.PostOCRBenchmarkViewModel
import com.example.app.ui.theme.AppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                MainScreen()
            }
        }
    }
}

@PreviewScreenSizes
@Composable
fun MainScreen() {
    var currentDestination by rememberSaveable {
        mutableStateOf(AppDestination.OCR)
    }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestination.entries.forEach { destination ->
                item(
                    icon = {
                        Icon(
                            painterResource(destination.icon),
                            contentDescription = destination.label,
                        )
                    },
                    label = { Text(destination.label) },
                    selected = currentDestination == destination,
                    onClick = { currentDestination = destination },
                )
            }
        },
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            val modifier = Modifier.padding(innerPadding)

            when (currentDestination) {
                AppDestination.OCR -> OCRSection(modifier)
                AppDestination.OBJECT_DETECTION -> {
                    Text(
                        text = "Benchmark de detección de objetos",
                        modifier = modifier,
                    )
                }
                AppDestination.POST_OCR -> PostOCRSection(modifier)
            }
        }
    }
}

@Composable
private fun OCRSection(modifier: Modifier) {
    val context = LocalContext.current.applicationContext

    val ocrViewModel: OCRBenchmarkViewModel = remember(context) {
        val evaluator = OCREvaluatorRepositoryImpl(context)
        val assetLoader = OCRAssetLoaderRepositoryImpl(context)
        val runUseCase = RunOCRUseCase(evaluator)
        val loadUseCase = LoadOCRAssetsUseCase(assetLoader)

        OCRBenchmarkViewModel(
            runOCRUseCase = runUseCase,
            loadOCRAssetsUseCase = loadUseCase,
        )
    }

    OCRBenchmarkScreen(
        viewModel = ocrViewModel,
        modifier = modifier,
    )
}

@Composable
private fun PostOCRSection(modifier: Modifier) {
    val context = LocalContext.current.applicationContext

    val postOCRViewModel: PostOCRBenchmarkViewModel = remember(context) {
        val evaluator = PostOCREvaluatorRepositoryImpl(context)
        val assetLoader = PostOCRAssetLoaderRepositoryImpl(context)
        val runUseCase = RunPostOCRUseCase(evaluator)
        val loadUseCase = LoadPostOCRAssetsUseCase(assetLoader)

        PostOCRBenchmarkViewModel(
            runPostOCRUseCase = runUseCase,
            loadPostOCRAssetsUseCase = loadUseCase,
        )
    }

    PostOCRBenchmarkScreen(
        viewModel = postOCRViewModel,
        modifier = modifier,
    )
}

enum class AppDestination(
    val label: String,
    val icon: Int,
) {
    OCR(
        label = "OCR",
        icon = R.drawable.ic_home,
    ),
    OBJECT_DETECTION(
        label = "Objetos",
        icon = R.drawable.ic_favorite,
    ),
    POST_OCR(
        label = "Post-OCR",
        icon = R.drawable.ic_account_box,
    ),
}