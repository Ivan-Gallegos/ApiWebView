package com.example.apiwebview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.Repository
import com.example.pokeapi.PokeService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class MainViewModel(
    private val repository: Repository
) : ViewModel() {

    private val _state: MutableStateFlow<MainState> = MutableStateFlow(MainState())
    val state: StateFlow<MainState> = _state


    fun onQueryChanged(query: String) {
        _state.value = state.value.copy(query = query)
    }

    fun getQueryResponse(query: String) = viewModelScope.launch {
        repository.getQueryResponse(query).run {
            if (isSuccessful) {
                _state.value = state.value.copy(response = body().orEmpty())
            } else {
                _state.value = state.value.copy(response = errorBody()?.string().orEmpty())
            }
        }
    }


    companion object {
        val Factory = viewModelFactory {
            initializer {
                val application = checkNotNull(this[APPLICATION_KEY])
                val pokeService = PokeService.getInstance(application)
                val repo = Repository.getInstance(pokeService)
                MainViewModel(repo)
            }
        }
    }
}


data class MainState(
    val query: String = "",
    val response: String = "",
)
