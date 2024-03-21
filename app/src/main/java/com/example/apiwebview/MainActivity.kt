package com.example.apiwebview

import android.Manifest
import android.graphics.Picture
import android.os.Build
import android.os.Bundle
import android.webkit.WebView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActionScope
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.drawscope.draw
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.apiwebview.ui.theme.ApiWebViewTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map
import org.json.JSONException
import org.json.JSONObject


class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels { MainViewModel.Factory }

    @OptIn(FlowPreview::class)
    val queryDebounce by lazy { viewModel.state.debounce(1000).map { it.query } }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ApiWebViewTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    val state by viewModel.state.collectAsState()
                    ScreenshotComposable(
                        state.shouldTakeScreenshot,
                        onScreenshotTaken = {
                            viewModel.shouldTakeScreenshot(false)
                        },
                    ) {
                        SearchWebView(state,
                            onQueryChanged = viewModel::onQueryChanged,
                            onSearch = {
                                viewModel.getQueryResponse(state.query)
                            },
                            onWebViewUpdate = {
                                viewModel.onWebViewUpdate()
                            }
                        )
                    }
                    val queryDebouncedState by queryDebounce.collectAsState(initial = state.query)
                    var searchJob: Job? = remember { null }
                    LaunchedEffect(queryDebouncedState) {
                        searchJob?.cancel()
                        searchJob = viewModel.getQueryResponse(queryDebouncedState)
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchWebView(
    state: MainState,
    onQueryChanged: (String) -> Unit,
    onSearch: (KeyboardActionScope.() -> Unit)? = null,
    onWebViewUpdate: (() -> Unit)? = null,
) = Column(Modifier.padding(16.dp)) {
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = state.query,
        onValueChange = onQueryChanged,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = onSearch),
    )
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { WebView(it) },
        update = { webView ->
            val dataString = state.response.body.let { body ->
                try {
                    JSONObject(body).toString(2)
                } catch (e: JSONException) {
                    body
                }
            }

            webView.loadData(dataString, "application/json", null)
            onWebViewUpdate?.invoke()
        },
    )
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun ScreenshotComposable(
    shouldTakeScreenshot: Boolean,
    onScreenshotTaken: () -> Unit = {},
    content: @Composable() (BoxScope.() -> Unit)
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val picture = remember { Picture() }
    val writeStorageAccessState = rememberMultiplePermissionsState(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // No permissions are needed on Android 10+ to add files in the shared storage
            emptyList()
        } else {
            listOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    )

    LaunchedEffect(
        shouldTakeScreenshot,
        writeStorageAccessState.allPermissionsGranted,
        writeStorageAccessState.shouldShowRationale
    ) {
        if (shouldTakeScreenshot) if (writeStorageAccessState.allPermissionsGranted) {
            delay(500)
            val bitmap = createBitmapFromPicture(picture)
            val uri = bitmap.saveToDisk(context)
            onScreenshotTaken()
            Toast.makeText(context, "Screenshot Saved", Toast.LENGTH_SHORT).show()
        } else if (writeStorageAccessState.shouldShowRationale) {
            val result = snackbarHostState.showSnackbar(
                message = "The storage permission is needed to save the image",
                actionLabel = "Grant Access"
            )

            if (result == SnackbarResult.ActionPerformed) {
                writeStorageAccessState.launchMultiplePermissionRequest()
            }
        } else {
            writeStorageAccessState.launchMultiplePermissionRequest()
        }
    }
    Box(
        Modifier
            .fillMaxSize()
            .drawWithCache {
                val width = this.size.width.toInt()
                val height = this.size.height.toInt()

                onDrawWithContent {
                    val pictureCanvas = Canvas(
                        picture.beginRecording(width, height)
                    )
                    draw(this, this.layoutDirection, pictureCanvas, this.size) {
                        this@onDrawWithContent.drawContent()
                    }
                    picture.endRecording()

                    drawIntoCanvas { canvas -> canvas.nativeCanvas.drawPicture(picture) }
                }
            },
        content = content,
    )
}
