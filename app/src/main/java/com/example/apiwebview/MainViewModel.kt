package com.example.apiwebview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.Repository
import com.example.domain.GetSearchResponseUseCase
import com.example.model.SearchResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class MainViewModel(
    private val getSearchResponseUseCase: GetSearchResponseUseCase
) : ViewModel() {

    private val _state: MutableStateFlow<MainState> = MutableStateFlow(MainState())
    val state: StateFlow<MainState> = _state


    fun onQueryChanged(query: String) {
        _state.value = state.value.copy(query = query)
    }

    fun getQueryResponse(query: String) = viewModelScope.launch {
        _state.value = state.value.copy(response = getSearchResponseUseCase(query))
    }

    fun shouldTakeScreenshot(b: Boolean) {
        _state.value = state.value.copy(shouldTakeScreenshot = b)
    }

    fun onWebViewUpdate() {
        if (state.value.response.fromNetwork) {
            shouldTakeScreenshot(true)
            _state.value = state.value.copy(
                response = state.value.response.copy(
                    fromNetwork = false
                )
            )
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val application = checkNotNull(this[APPLICATION_KEY])
                val repo = Repository.getInstance(application)
                val getSearchResponseUseCase = GetSearchResponseUseCase(repo)
                MainViewModel(getSearchResponseUseCase)
            }
        }
    }
}


data class MainState(
    val query: String = "",
    val response: SearchResponse = SearchResponse(),
    val shouldTakeScreenshot: Boolean = false,
)
