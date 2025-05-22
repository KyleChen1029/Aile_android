package tw.com.chainsea.chat.ui.dialog

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import tw.com.chainsea.ce.sdk.bean.msg.ChannelType
import tw.com.chainsea.ce.sdk.database.sp.TokenPref
import tw.com.chainsea.ce.sdk.network.model.response.DeviceRecordItem
import tw.com.chainsea.ce.sdk.network.model.response.ServicesIdentityListResponse
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.databinding.BottomSheetFilterSortBinding
import tw.com.chainsea.chat.databinding.BottomSheetLoginDeviceOperationBinding
import tw.com.chainsea.chat.databinding.BottomSheetMediaBinding
import tw.com.chainsea.chat.databinding.BottomSheetQuitTenantBinding
import tw.com.chainsea.chat.databinding.BottomSheetSavePictureToAlbumBinding
import tw.com.chainsea.chat.databinding.BottomSheetSwitchChannelBinding
import tw.com.chainsea.chat.databinding.BottomSheetSwitchIdentityBinding
import tw.com.chainsea.chat.databinding.BottomSheetUnBindFansPageBinding
import tw.com.chainsea.chat.databinding.BottomSheetUploadPictureBinding
import tw.com.chainsea.chat.ui.adapter.ReplyChannelListAdapter
import tw.com.chainsea.chat.ui.adapter.ServicesIdentityListAdapter
import tw.com.chainsea.chat.ui.adapter.entity.ChannelEntity

class BottomSheetDialogBuilder(
    val context: Context,
    val inflater: LayoutInflater
) {
    // 商務號中，右下角頭像，可切換商務號身份或是秘書身份
    fun getSwitchIdentityDialog(
        identities: List<ServicesIdentityListResponse>,
        callback: (ServicesIdentityListResponse) -> Unit,
        cancelCallback: () -> Unit
    ): BottomSheetDialog {
        val binding = BottomSheetSwitchIdentityBinding.inflate(inflater)
        val bottomSheetDialog = BottomSheetDialog(context)
        bottomSheetDialog.setContentView(
            binding.root,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
        (binding.root.parent as ViewGroup).setBackgroundResource(android.R.color.transparent)
        binding.btnCancel.setOnClickListener { bottomSheetDialog.dismiss() }
        binding.btnStopService.setOnClickListener {
            bottomSheetDialog.dismiss()
            cancelCallback.invoke()
        }
        binding.rvIdentity.apply {
            layoutManager = LinearLayoutManager(context)
            adapter =
                ServicesIdentityListAdapter(identities) {
                    bottomSheetDialog.dismiss()
                    callback.invoke(it)
                }
            isNestedScrollingEnabled = false
        }
        return bottomSheetDialog
    }

    fun getQuitTenantDialog(callback: () -> Unit): BottomSheetDialog {
        val binding = BottomSheetQuitTenantBinding.inflate(inflater)
        val bottomSheetDialog = BottomSheetDialog(context)
        bottomSheetDialog.setContentView(
            binding.root,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
        (binding.root.parent as ViewGroup).setBackgroundResource(android.R.color.transparent)
        binding.btnCancel.setOnClickListener { bottomSheetDialog.dismiss() }
        binding.btnQuitTenant.setOnClickListener {
            bottomSheetDialog.dismiss()
            callback.invoke()
        }
        return bottomSheetDialog
    }

    fun getUploadBackgroundPicture(
        albumCallback: () -> Unit,
        cameraCallback: () -> Unit
    ): BottomSheetDialog {
        val binding = BottomSheetUploadPictureBinding.inflate(inflater)
        binding.dialogTitle.text = context.getString(R.string.text_upload_background_picture)
        val bottomSheetDialog = BottomSheetDialog(context)
        bottomSheetDialog.setContentView(
            binding.root,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
        (binding.root.parent as ViewGroup).setBackgroundResource(android.R.color.transparent)
        binding.btnCancel.setOnClickListener { bottomSheetDialog.dismiss() }
        binding.btnPhotoAlbum.setOnClickListener {
            bottomSheetDialog.dismiss()
            albumCallback.invoke()
        }
        binding.btnCamera.setOnClickListener {
            bottomSheetDialog.dismiss()
            cameraCallback.invoke()
        }
        return bottomSheetDialog
    }

    fun getUploadAvatar(
        albumCallback: () -> Unit,
        cameraCallback: () -> Unit
    ): BottomSheetDialog {
        val binding = BottomSheetUploadPictureBinding.inflate(inflater)
        binding.dialogTitle.text = context.getString(R.string.text_upload_avatar)
        val bottomSheetDialog = BottomSheetDialog(context)
        bottomSheetDialog.setContentView(
            binding.root,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
        (binding.root.parent as ViewGroup).setBackgroundResource(android.R.color.transparent)
        binding.btnCancel.setOnClickListener { bottomSheetDialog.dismiss() }
        binding.btnPhotoAlbum.setOnClickListener {
            bottomSheetDialog.dismiss()
            albumCallback.invoke()
        }
        binding.btnCamera.setOnClickListener {
            bottomSheetDialog.dismiss()
            cameraCallback.invoke()
        }
        return bottomSheetDialog
    }

    fun getOnlineDeviceOperation(
        deviceRecordItem: DeviceRecordItem,
        firstBtnCallBack: (Boolean) -> Unit,
        secondBtnCallBack: () -> Unit,
        thirdBtnCallBack: () -> Unit
    ): BottomSheetDialog {
        val binding = BottomSheetLoginDeviceOperationBinding.inflate(inflater)
        val isSelf = deviceRecordItem.isOnline == true && deviceRecordItem.uniqueID == TokenPref.getInstance(context).uniqueID && deviceRecordItem.bundleId == "tw.com.chainsea.chat"
        // 裝置名稱
        binding.dialogTitle.text =
            if (isSelf) {
                context.getString(R.string.text_device_self, deviceRecordItem.deviceName)
            } else {
                deviceRecordItem.deviceName
            }

        // 登出
        binding.btnLogout.apply {
            text = context.getString(R.string.text_device_logout)
            visibility = if (deviceRecordItem.isOnline == true) View.VISIBLE else View.GONE
        }

        // 自動登入
        binding.btnAutoLogin.apply {
            if (!isSelf) {
                text = context.getString(if (deviceRecordItem.rememberMe == true) R.string.text_device_cancel_auto_login else R.string.text_device_allow_auto_login)
                visibility = View.VISIBLE
            } else {
                visibility = View.GONE
            }
        }

        // 刪除設備
        binding.btnDeleteDevice.apply {
            text = context.getString(R.string.text_device_delete)
            visibility = if (!isSelf) View.VISIBLE else View.GONE
        }

        val bottomSheetDialog = BottomSheetDialog(context)
        bottomSheetDialog.setContentView(
            binding.root,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
        (binding.root.parent as ViewGroup).setBackgroundResource(android.R.color.transparent)
        binding.btnCancel.setOnClickListener { bottomSheetDialog.dismiss() }
        binding.btnLogout.setOnClickListener {
            bottomSheetDialog.dismiss()
            firstBtnCallBack.invoke(isSelf)
        }

        binding.btnAutoLogin.setOnClickListener {
            bottomSheetDialog.dismiss()
            secondBtnCallBack.invoke()
        }

        binding.btnDeleteDevice.setOnClickListener {
            bottomSheetDialog.dismiss()
            thirdBtnCallBack.invoke()
        }
        return bottomSheetDialog
    }

    fun doMediaDownloadAction(savedCallback: () -> Unit): BottomSheetDialog {
        val binding = BottomSheetSavePictureToAlbumBinding.inflate(inflater)
        val bottomSheetDialog = BottomSheetDialog(context)
        bottomSheetDialog.setContentView(
            binding.root,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
        (binding.root.parent as ViewGroup).setBackgroundResource(android.R.color.transparent)
        binding.btnCancel.setOnClickListener { bottomSheetDialog.dismiss() }
        binding.btnSavePictureToAlbum.setOnClickListener {
            bottomSheetDialog.dismiss()
            savedCallback.invoke()
        }
        return bottomSheetDialog
    }

    fun getUnBindFansPageDialog(
        name: String,
        confirmationCallback: () -> Unit
    ): BottomSheetDialog {
        val bottomSheetDialog = BottomSheetDialog(context)
        val binding = BottomSheetUnBindFansPageBinding.inflate(inflater)
        binding.tvTitle.text =
            binding.root.context
                .getString(R.string.unbind_fans_page)
                .format(name)
        binding.btnConfirm.setOnClickListener {
            bottomSheetDialog.dismiss()
            confirmationCallback.invoke()
        }

        binding.btnCancel.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.setContentView(
            binding.root,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
        (binding.root.parent as ViewGroup).setBackgroundResource(android.R.color.transparent)
        return bottomSheetDialog
    }

    fun doOpenMedia(
        cameraCallback: () -> Unit,
        videoCallback: () -> Unit
    ): BottomSheetDialog {
        val binding = BottomSheetMediaBinding.inflate(inflater)
        val bottomSheetDialog = BottomSheetDialog(context)
        bottomSheetDialog.setContentView(
            binding.root,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
        (binding.root.parent as ViewGroup).setBackgroundResource(android.R.color.transparent)
        binding.btnCancel.setOnClickListener { bottomSheetDialog.dismiss() }
        binding.btnCamera.setOnClickListener {
            bottomSheetDialog.dismiss()
            cameraCallback.invoke()
        }
        binding.btnVideo.setOnClickListener {
            bottomSheetDialog.dismiss()
            videoCallback.invoke()
        }
        return bottomSheetDialog
    }

    fun getFilterSortDialog(callback: (String) -> Unit): BottomSheetDialog {
        val binding = BottomSheetFilterSortBinding.inflate(inflater)
        val bottomSheetDialog = BottomSheetDialog(context)
        bottomSheetDialog.setContentView(
            binding.root,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
        (binding.root.parent as ViewGroup).setBackgroundResource(android.R.color.transparent)
        binding.btnSortNewToOld.setOnClickListener {
            callback.invoke("DESC")
            bottomSheetDialog.dismiss()
        }

        binding.btnSortOldToNew.setOnClickListener {
            callback.invoke("ASC")
            bottomSheetDialog.dismiss()
        }

        binding.btnCancel.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        return bottomSheetDialog
    }

    fun getSwitchReplyChannelDialog(
        channelList: MutableList<ChannelEntity>,
        callback: (ChannelType) -> Unit
    ): BottomSheetDialog {
        val binding = BottomSheetSwitchChannelBinding.inflate(inflater)
        val bottomSheetDialog = BottomSheetDialog(context)
        bottomSheetDialog.setContentView(
            binding.root,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
        (binding.root.parent as ViewGroup).setBackgroundResource(android.R.color.transparent)
        binding.btnCancel.setOnClickListener { bottomSheetDialog.dismiss() }
        binding.rvChannel.apply {
            layoutManager = LinearLayoutManager(context)
            adapter =
                ReplyChannelListAdapter(channelList) {
                    bottomSheetDialog.dismiss()
                    callback.invoke(it)
                }
            isNestedScrollingEnabled = false
        }
        return bottomSheetDialog
    }
}
