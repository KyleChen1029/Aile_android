package tw.com.chainsea.chat.keyboard.emoticon.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.toColorInt
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomType
import tw.com.chainsea.ce.sdk.service.type.ChatRoomSource
import tw.com.chainsea.chat.databinding.ExtraLayoutBinding
import tw.com.chainsea.chat.util.ThemeHelper

class ExtraLayout : ConstraintLayout {
    private lateinit var onSendBusinessCard: () -> Unit
    private lateinit var onOpenAttachment: () -> Unit
    private lateinit var onSendBusinessMemberCard: () -> Unit
    constructor(context: Context) : this(context, null)
    constructor(
        context: Context,
        isBusinessCardSetting: Boolean,
        entity: ChatRoomEntity,
        isProvisionMember: Boolean,
        isBossServiceNumberOwner: Boolean,
        onSendBusinessCard: () -> Unit,
        onOpenAttachment: () -> Unit,
        onSendBusinessMemberCard: () -> Unit
    ) : this(context, null) {
        this.onSendBusinessCard = onSendBusinessCard
        this.onOpenAttachment = onOpenAttachment
        this.onSendBusinessMemberCard = onSendBusinessMemberCard
        val isGreenTheme = ThemeHelper.isGreenTheme()
        binding.scopeBusinessCard.visibility = if (isBusinessCardSetting || isBossServiceNumberOwner) View.VISIBLE else View.GONE // 該服務號是否有設定電子名片
        binding.root.setBackgroundColor(
            if (entity.listClassify == ChatRoomSource.SERVICE || isProvisionMember) {
                "#FF6BC2BA".toColorInt()
            } else {
                if (isGreenTheme) {
                    "#FF015F57".toColorInt()
                } else {
                    "#FF6B93C2".toColorInt()
                }
            }
        )

        binding.scopeBusinessMemberCard.visibility = if ((!isProvisionMember && entity.listClassify == ChatRoomSource.SERVICE && entity.serviceNumberOpenType.contains("O") && entity.type == ChatRoomType.services) || isBossServiceNumberOwner) View.VISIBLE else View.GONE // 臨時成員不顯示發送企業會員卡片
    }

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    private val binding: ExtraLayoutBinding =
        ExtraLayoutBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        binding.scopeBusinessCard.setOnClickListener {
            onSendBusinessCard()
        }
        binding.scopeAttachment.setOnClickListener {
            onOpenAttachment()
        }
        binding.scopeBusinessMemberCard.setOnClickListener {
            onSendBusinessMemberCard()
        }
    }
}
