package tw.com.chainsea.chat.refactor.loginPage

import android.Manifest.permission
import android.content.Context
import android.os.Build

class LoginCpPresenter(
    private val mView: LoginCpContract.IView
) : LoginCpContract.IPresenter {
    private val permissions =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                permission.POST_NOTIFICATIONS,
                permission.READ_MEDIA_IMAGES,
                permission.READ_MEDIA_VIDEO,
                permission.READ_MEDIA_AUDIO
            )
        } else {
            arrayOf(
                permission.WRITE_EXTERNAL_STORAGE,
                permission.READ_PHONE_STATE,
                permission.RECORD_AUDIO
            )
        }

    override fun getPermission(context: Context) {
//        XXPermissions.with(context).permission(permissions).request { permissions: List<String?>?, all: Boolean -> }
    }
}
