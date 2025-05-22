package tw.com.chainsea.chat.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject
import tw.com.chainsea.android.common.client.type.Media
import tw.com.chainsea.android.common.json.JsonHelper
import tw.com.chainsea.ce.sdk.database.DBContract
import tw.com.chainsea.ce.sdk.database.sp.TokenPref
import tw.com.chainsea.ce.sdk.database.sp.UserPref
import tw.com.chainsea.ce.sdk.event.EventBusUtils
import tw.com.chainsea.ce.sdk.event.EventMsg
import tw.com.chainsea.ce.sdk.event.MsgConstant
import tw.com.chainsea.ce.sdk.lib.ErrCode
import tw.com.chainsea.ce.sdk.reference.UserProfileReference
import tw.com.chainsea.ce.sdk.service.FileService
import tw.com.chainsea.ce.sdk.service.listener.ServiceCallBack
import tw.com.chainsea.ce.sdk.service.type.RefreshSource
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.base.Constant
import tw.com.chainsea.chat.ui.activity.ClipImageActivity
import tw.com.chainsea.chat.ui.utils.permissionUtils.XPermissionUtils
import tw.com.chainsea.chat.ui.utils.permissionUtils.XPermissionUtils.OnPermissionListener
import tw.com.chainsea.custom.view.alert.AlertView
import kotlin.Unit

class UploadAvatarUtil {

    companion object {
        @JvmField
        val REQUEST_AVATAR_CODE_RESULT = 0x138D
    }

    private val REQUEST_PERMISSION_CODE = 0x02


    private val permission = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
    } else {
        arrayOf(
            Manifest.permission.READ_MEDIA_VIDEO,
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.CAMERA
        )
    }

    private fun checkPermission(context: Context, callback: () -> Unit) {
        XPermissionUtils.requestPermissions(context, REQUEST_PERMISSION_CODE, permission, object : OnPermissionListener {
            override fun onPermissionDenied(
                deniedPermissions: Array<out String>?,
                alwaysDenied: Boolean
            ) {
                Toast.makeText(context, "獲取相機權限失敗", Toast.LENGTH_SHORT).show()
            }

            override fun onPermissionGranted() {
                callback.invoke()
            }
        })
    }

    fun showPickPictureDialog(context: Context) {
        checkPermission(context) {
            AlertView.Builder()
                .setContext(context)
                .setStyle(AlertView.Style.ActionSheet)
                .setOthers(
                    arrayOf(
                        context.getString(R.string.warning_photos),
                        context.getString(R.string.warning_camera)
                    )
                )
                .setCancelText(context.getString(R.string.alert_cancel))
                .setOnItemClickListener { o: Any, position: Int ->
                    val intent = Intent(context, ClipImageActivity::class.java)
                    intent.putExtra(Constant.INTENT_IMAGE_CONFIRM_TYPE, Constant.INTENT_CODE_CROP)
                    if (position == 0) {
                        intent.putExtra(Constant.INTENT_IMAGE_GET_WAY, Constant.INTENT_CODE_ALBUM)
                    } else {
                        intent.putExtra(Constant.INTENT_IMAGE_GET_WAY, Constant.INTENT_CODE_CAMERA)
                    }
                    if (context is Activity) context.startActivityForResult(
                        intent,
                        REQUEST_AVATAR_CODE_RESULT
                    )
                }
                .build()
                .setCancelable(true)
                .show()
        }
    }

    fun uploadServiceNumberAvatar(context: Context, serviceNumberId: String, size: Int, filePath: String, fileName: String?, callback: () -> Unit) = CoroutineScope(Dispatchers.IO).launch {
        val tokenId = TokenPref.getInstance(context).tokenId
        FileService.uploadServiceNumberAvatar(context, tokenId, serviceNumberId, Media.findByFileType(filePath), size, filePath, fileName, object :
            ServiceCallBack<String, RefreshSource> {
            override fun error(message: String) {
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(
                        context,
                        context.getString(R.string.text_upload_avatar_failure),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun complete(t: String?, e: RefreshSource?) {
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(
                        context,
                        context.getString(R.string.text_upload_avatar_success),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                callback.invoke()
            }
        })
    }
}
