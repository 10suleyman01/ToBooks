package com.suleyman.tobooks.ui.fragment.audios

import androidx.lifecycle.ViewModel
import com.suleyman.tobooks.model.AudioBookModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AudioBooksViewModel : ViewModel() {

    private val audios: MutableList<AudioBookModel> = mutableListOf()

    private val _audioStates: MutableStateFlow<AudioState> = MutableStateFlow(AudioState.Empty)
    val audioStates: StateFlow<AudioState> = _audioStates

    fun loadAudioBooks() {

    }

    sealed class AudioState {
        object Empty : AudioState()
        data class Loading(val isLoading: Boolean) : AudioState()
        data class Success(val audios: MutableList<AudioBookModel>) : AudioState()
    }

}