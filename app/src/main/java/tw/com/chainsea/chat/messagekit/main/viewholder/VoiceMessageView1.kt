package tw.com.chainsea.chat.messagekit.main.viewholder

import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tw.com.aile.sdk.bean.message.MessageEntity
import tw.com.chainsea.android.common.client.helper.ClientsHelper
import tw.com.chainsea.android.common.client.type.Media
import tw.com.chainsea.android.common.log.CELog
import tw.com.chainsea.android.common.voice.VoiceHelper
import tw.com.chainsea.ce.sdk.bean.msg.ChannelType
import tw.com.chainsea.ce.sdk.bean.msg.MessageStatus
import tw.com.chainsea.ce.sdk.bean.msg.content.VoiceContent
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity
import tw.com.chainsea.ce.sdk.database.sp.TokenPref
import tw.com.chainsea.chat.databinding.ItemBaseMessageBinding
import tw.com.chainsea.chat.databinding.MsgkitVoiceBinding
import tw.com.chainsea.chat.messagekit.lib.AnimationDrawableManager
import tw.com.chainsea.chat.messagekit.lib.AudioLib
import tw.com.chainsea.chat.messagekit.main.viewholder.base.BaseMessageViewHolder
import tw.com.chainsea.chat.messagekit.main.viewholder.base.OnMessageSlideReply
import tw.com.chainsea.chat.util.DownloadUtil
import java.io.File

class VoiceMessageView1(
    binding: ItemBaseMessageBinding,
    layoutInflater: LayoutInflater,
    chatRoomEntity: ChatRoomEntity,
    onMessageSlideReply: OnMessageSlideReply
) : BaseMessageViewHolder(binding, chatRoomEntity, onMessageSlideReply = onMessageSlideReply) {
    private val voiceBinding = MsgkitVoiceBinding.inflate(layoutInflater)
    private val player by lazy { ExoPlayer.Builder(voiceBinding.root.context).build() }
    private var voiceTextView: TextView? = null
    private var currentVoiceAnim: AnimationDrawable? = null
    private var animationDrawable: AnimationDrawable? = null

    override fun onMessageClick() {
    }

    override fun bind(message: MessageEntity) {
        super.bind(message)
        CoroutineScope(Dispatchers.IO).launch {
            setBubbleView(voiceBinding.root)
            if (message.content.isEmpty()) return@launch
            val voiceContent = message.content() as VoiceContent
            if (message.from == ChannelType.IG) {
                initPlayer(voiceContent.url)
            }

            withContext(Dispatchers.Main) {
                voiceBinding.msgVoiceRightContent.visibility =
                    if (isRightMessage()) View.VISIBLE else View.GONE
                voiceBinding.msgVoiceLeftContent.visibility =
                    if (isRightMessage()) View.GONE else View.VISIBLE
                if (isRightMessage()) {
                    voiceContent.isRead = true
                } else {
                    voiceBinding.messageVoiceDot.visibility =
                        if (voiceContent.isRead) View.INVISIBLE else View.VISIBLE
                }
                voiceTextView =
                    if (isRightMessage()) voiceBinding.msgVoiceRightContent else voiceBinding.msgVoiceLeftContent
                voiceTextView?.text = VoiceHelper.strDuration(voiceContent.duration * 1000)
                voiceBinding.root.setOnClickListener {
                    if (isRightMessage()) {
                        playRight()
                    } else {
                        playLeft()
                    }
                }
            }
        }
    }

    private fun playRight() =
        CoroutineScope(Dispatchers.IO).launch {
            message?.let {
                // 让所有自己发的語音消息都从cache中读取，如果没有，则从新下载
                val audioCachePath =
                    AudioLib
                        .getInstance(voiceBinding.root.context)
                        .getAudioCachePath(voiceBinding.root.context) + File.separator + it.id + ".m4a"
                animationDrawable = voiceTextView?.compoundDrawables?.get(2) as AnimationDrawable
                val cacheFile = File(audioCachePath)
                if (cacheFile.exists()) {
                    startPlayVoice(cacheFile.absolutePath)
                } else {
                    downloadAudio()
                }
            }
        }

    private fun playLeft() =
        CoroutineScope(Dispatchers.IO).launch {
            message?.let {
                (it.content() as VoiceContent).isRead = true
                onContentUpdate(it.content().javaClass.name, it.content().toStringContent())
                animationDrawable = voiceTextView?.compoundDrawables?.get(0) as AnimationDrawable
                val downloadPath = downloadDir + it.id + ".m4a"
                if (it.from == ChannelType.IG) {
                    startPlayAudioWithInstagram()
                } else if (File(downloadPath).exists()) {
                    startPlayVoice(downloadPath)
                } else {
                    downloadAudio()
                }
            }
        }

    private fun startPlayAudioWithInstagram() {
        player.apply {
            seekTo(0)
            play()
        }
    }

    private fun initPlayer(url: String) =
        CoroutineScope(Dispatchers.IO).launch {
            val mediaItem =
                MediaItem
                    .Builder()
                    .setUri(url)
                    .build()
            player.apply {
                setMediaItem(mediaItem)
                prepare()
                addListener(
                    object : Player.Listener {
                        override fun onPlaybackStateChanged(playbackState: Int) {
                            super.onPlaybackStateChanged(playbackState)
                            if (playbackState == ExoPlayer.STATE_READY) {
                                voiceTextView?.text = VoiceHelper.strDuration(player.duration.toDouble())
                            }
                        }

                        override fun onIsPlayingChanged(isPlaying: Boolean) {
                            super.onIsPlayingChanged(isPlaying)
                            if (isPlaying) {
                                startAudioAnimation()
                            } else {
                                stopAudioAnimation()
                            }
                        }
                    }
                )
            }
        }

    private fun startAudioAnimation() =
        CoroutineScope(Dispatchers.Main).launch {
            AnimationDrawableManager.stopAnimations()
            currentVoiceAnim?.apply {
                stop()
                selectDrawable(0)
            }

            animationDrawable?.apply {
                if (!isRunning) {
                    start()
                    AnimationDrawableManager.addDrawable(this)
                }
            }
        }

    private fun stopAudioAnimation() =
        CoroutineScope(Dispatchers.Main).launch {
            AudioLib.getInstance(voiceBinding.root.context).stopPlay()
            animationDrawable?.apply {
                stop()
                selectDrawable(0)
            }
            currentVoiceAnim?.apply {
                stop()
                selectDrawable(0)
            }
        }

    private fun startPlayVoice(path: String?) =
        CoroutineScope(Dispatchers.Main).launch {
            if (AudioLib.getInstance(voiceBinding.root.context).isPlaying(path)) {
                stopAudioAnimation()
            } else {
                startAudioAnimation()
                AudioLib.getInstance(voiceBinding.root.context).playAudio(path) { isSuccess ->
                    animationDrawable?.apply {
                        stop()
                        selectDrawable(0)
                        AnimationDrawableManager.removeDrawable(animationDrawable)
                    }
                }
                currentVoiceAnim = animationDrawable
            }
        }

    private fun downloadAudio() =
        CoroutineScope(Dispatchers.IO).launch {
            message?.let {
                val voiceContent = it.content() as VoiceContent
                val dir = File(downloadDir)
                if (!dir.exists()) dir.mkdir()
                val downloadFile = File("${DownloadUtil.downloadFileDir}${it.id}.m4a")
                val selfToken = TokenPref.getInstance(voiceBinding.root.context).tokenId
                ClientsHelper
                    .post(false)
                    .execute(
                        "${voiceContent.url}?tokenId=$selfToken",
                        Media.OCTET_STREAM.get(),
                        "",
                        object : tw.com.chainsea.android.common.client.callback.impl.FileCallBack(
                            downloadFile.parent,
                            downloadFile.name,
                            false
                        ) {
                            override fun onSuccess(
                                resp: String?,
                                file: File?
                            ) {
                                voiceContent.isDownLoad = true
                                onContentUpdate(
                                    voiceContent.javaClass.name,
                                    voiceContent.toStringContent()
                                )
                                scanMediaFile(file)
                                startPlayVoice(file?.absolutePath)
                            }

                            override fun onFailure(
                                e: Exception?,
                                errorMsg: String?
                            ) {
                                CELog.e("download file, failed: " + e.toString())
                                if (isRightMessage()) {
                                    message?.status = MessageStatus.FAILED
                                }
                            }
                        }
                    )
            }
        }

    private fun scanMediaFile(file: File?) {
        file?.let {
            val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            val contentUri = Uri.fromFile(it)
            mediaScanIntent.data = contentUri
            voiceBinding.root.context.sendBroadcast(mediaScanIntent)
        }
    }
}
