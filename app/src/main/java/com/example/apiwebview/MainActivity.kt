package com.example.apiwebview

import android.os.Bundle
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActionScope
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.apiwebview.ui.theme.ApiWebViewTheme
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map
import org.json.JSONException
import org.json.JSONObject

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels { MainViewModel.Factory }

    @OptIn(FlowPreview::class)
    val queryDebounce by lazy { viewModel.state.map { it.query }.debounce(500) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ApiWebViewTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    val state by viewModel.state.collectAsState()
                    SearchWebView(state) {
                        viewModel.getQueryResponse(state.query)
                    }
                    val queryDebouncedState by queryDebounce.collectAsState(initial = state.query)
                    LaunchedEffect(queryDebouncedState) {
                        viewModel.getQueryResponse(state.query)
                    }
                }
            }
        }
    }

    @Composable
    private fun SearchWebView(state: MainState, onSearch: (KeyboardActionScope.() -> Unit)?) =
        Column(Modifier.padding(16.dp)) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.query,
                onValueChange = viewModel::onQueryChanged,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = onSearch)
            )
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { WebView(it) },
                update = {
                    val dataString = try {
                        JSONObject(state.response).toString(2)
                    } catch (e: JSONException) {
                        state.response
                    }

                    it.loadData(dataString, "application/json", null)
                },
            )
        }
}