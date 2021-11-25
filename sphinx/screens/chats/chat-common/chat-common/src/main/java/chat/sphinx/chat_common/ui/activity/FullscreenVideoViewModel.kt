package chat.sphinx.chat_common.ui.activity

import android.app.Application
import android.content.Context
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import chat.sphinx.concept_repository_message.MessageRepository
import chat.sphinx.video_player_controller.VideoPlayerController
import chat.sphinx.wrapper_common.message.MessageId
import chat.sphinx.wrapper_message.Message
import chat.sphinx.wrapper_message.retrieveTextToShow
import dagger.hilt.android.lifecycle.HiltViewModel
import io.matthewnelson.android_feature_navigation.util.navArgs
import io.matthewnelson.android_feature_viewmodel.SideEffectViewModel
import io.matthewnelson.android_feature_viewmodel.currentViewState
import io.matthewnelson.android_feature_viewmodel.updateViewState
import io.matthewnelson.concept_coroutines.CoroutineDispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
internal class FullscreenVideoViewModel @Inject constructor(
    val app: Application,
    handle: SavedStateHandle,
    messageRepository: MessageRepository,
    dispatchers: CoroutineDispatchers,
): SideEffectViewModel<
        Context,
        FullscreenVideoSideEffect,
        FullscreenVideoViewState
        >(dispatchers, FullscreenVideoViewState.Idle) {

    private val args: FullscreenVideoActivityArgs by handle.navArgs()
    private val messageId = MessageId(args.argMessageId)
    private val videoFile = args.argVideoFilepath?.let {
        File(it)
    }

    internal val videoPlayerController: VideoPlayerController by lazy {
        VideoPlayerController(
            viewModelScope = viewModelScope,
            updateIsPlaying = { isPlaying ->
                if (isPlaying) {
                    updateViewState(
                        FullscreenVideoViewState.ContinuePlayback(
                            currentViewState.name,
                            currentViewState.duration,
                            currentViewState.videoDimensions,
                            currentViewState.currentTime,
                            isPlaying
                        )
                    )
                } else {
                    updateViewState(
                        FullscreenVideoViewState.PausePlayback(
                            currentViewState.name,
                            currentViewState.duration,
                            currentViewState.videoDimensions,
                            currentViewState.currentTime,
                            isPlaying
                        )
                    )
                }
            },
            updateMetaDataCallback = { duration, videoWidth, videoHeight ->
                updateViewState(
                    FullscreenVideoViewState.MetaDataLoaded(
                        currentViewState.name,
                        duration,
                        Pair(videoWidth, videoHeight),
                        currentViewState.currentTime,
                        currentViewState.isPlaying
                    )
                )
            },
            updateCurrentTimeCallback = { currentTime ->
                updateViewState(
                    FullscreenVideoViewState.CurrentTimeUpdate(
                        currentViewState.name,
                        currentViewState.duration,
                        currentViewState.videoDimensions,
                        currentTime,
                        currentViewState.isPlaying
                    )
                )
            },
            completePlaybackCallback = {
                updateViewState(
                    FullscreenVideoViewState.CompletePlayback(
                        currentViewState.name,
                        currentViewState.duration,
                        currentViewState.videoDimensions,
                        currentTime = 0,
                        isPlaying = false
                    )
                )
            },
            dispatchers
        )
    }

    private val messageSharedFlow: SharedFlow<Message?> = flow {
        emitAll(messageRepository.getMessageById(messageId))
    }.distinctUntilChanged().shareIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(2_000),
        replay = 1,
    )

    private suspend fun getMessage(): Message? {
        messageSharedFlow.replayCache.firstOrNull()?.let { message ->
            return message
        }

        messageSharedFlow.firstOrNull()?.let { message ->
            return message
        }

        var message: Message? = null

        try {
            messageSharedFlow.collect {
                if (it != null) {
                    message = it
                    throw Exception()
                }
            }
        } catch (e: Exception) {}
        delay(25L)

        return message
    }

    private suspend fun getVideoFile(): File? {
        return videoFile ?: getMessage()?.messageMedia?.localFile
    }

    private suspend fun getVideoTitle(): String? {
        return videoFile?.name ?: getMessage()?.retrieveTextToShow()
    }

    fun initializeVideo() {
        viewModelScope.launch(mainImmediate) {
            getVideoTitle()?.let { title ->
                updateViewState(
                    FullscreenVideoViewState.VideoMessage(
                        title,
                        currentViewState.duration,
                        currentViewState.videoDimensions,
                        currentViewState.currentTime,
                        currentViewState.isPlaying
                    )
                )
            }
            getVideoFile()?.let { videoFile ->
                videoPlayerController.initializeVideo(videoFile.toUri())
            }
        }
    }

    override fun onCleared() {
        super.onCleared()

        videoPlayerController.clear()
    }
}