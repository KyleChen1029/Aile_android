package tw.com.chainsea.chat.util;

import static tw.com.chainsea.ce.sdk.socket.cp.CpSocket.BASE_URL;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import tw.com.chainsea.android.common.system.ThreadExecutorHelper;
import tw.com.chainsea.ce.sdk.database.sp.TokenPref;
import tw.com.chainsea.chat.App;
import tw.com.chainsea.chat.R;

public class AvatarKit {
    public final static String DEFAULT_AVATAR_ID = "161312a6-34d0-07f0-1614-f894c2bf94a1";
    public final static String DEFAULT_AVATAR_ID2 = "17a03675-b850-05be-283d-0603d33e881c";
    private NameKit nameKit = new NameKit();

    private final RequestOptions tenantOptions = new RequestOptions()
        .placeholder(R.drawable.invalid_name)
        .error(R.drawable.invalid_name)
        .fitCenter();
    private final RequestOptions avatarOptions = new RequestOptions()
        .placeholder(R.drawable.custom_default_avatar)
        .error(R.drawable.custom_default_avatar)
        .fitCenter();

    public static String getCeAvatarUrl(String avatarId, String size) {
        return TokenPref.getInstance(App.getContext()).getCurrentTenantUrl() +
            "/openapi/base/avatar/view?args=%7B%22id%22:%22" + avatarId + "%22,%20%22size%22:%22" + size + "%22%7D";
    }

    public static String getCeAvatarUrl(String avatarId) {
        return getCeAvatarUrl(avatarId, "m");
    }

    public static String getCpAvatarUrl(String avatarId, String size) {
        return BASE_URL + "/openapi/base/avatar/view?args=%7B%22id%22:%22" + avatarId + "%22,%20%22size%22:%22" + size + "%22%7D";
    }

    public static String getCpAvatarUrl(String avatarId) {
        return getCpAvatarUrl(avatarId, "s");
    }

    public void loadCpTenantAvatar(String avatarId, ImageView target) {
        if (avatarId == null || avatarId.isEmpty()) {
            target.setImageResource(R.drawable.invalid_name);
            return;
        }
        try {
            Glide.with(App.getContext())
                .load(getCpAvatarUrl(avatarId))
                .apply(tenantOptions)
                .into(target);
        } catch (Exception ignored) {
        }

    }

    public void loadCEAvatar(String avatarId, ImageView target) {
        if (avatarId == null || avatarId.isEmpty()) {
            target.setImageResource(R.mipmap.ic_new_head);
            return;
        }
        try {
            Glide.with(App.getContext())
                .load(avatarId.startsWith("http") ? avatarId : getCeAvatarUrl(avatarId))
                .apply(avatarOptions)
                .into(target);
        } catch (Exception ignored) {
        }

    }

    public void loadCEAvatar(String avatarId, ImageView img, TextView tv, String name) {
        try {
            Glide.with(App.getContext())
                .load(avatarId.startsWith("http") ? avatarId : getCeAvatarUrl(avatarId))
                .addListener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, @NonNull Target<Drawable> target, boolean isFirstResource) {
                        ThreadExecutorHelper.getMainThreadExecutor().execute(() -> {
                            try {
                                String updateName = nameKit.getAvatarName(name);
                                GradientDrawable gradientDrawable = (GradientDrawable) tv.getBackground();
                                if (gradientDrawable != null)
                                    gradientDrawable.setColor(Color.parseColor(nameKit.getBackgroundColor(updateName)));
                                else
                                    tv.setBackgroundColor(Color.parseColor(nameKit.getBackgroundColor(updateName)));
                                //CELog.d("Kyle2 onLoadFailed name="+name+", updateName="+updateName);
                                tv.setText(updateName);
                                img.setVisibility(View.INVISIBLE);
                                tv.setVisibility(View.VISIBLE);
                            } catch (Exception err) {
                            }
                        });
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(@NonNull Drawable resource, @NonNull Object model, Target<Drawable> target, @NonNull DataSource dataSource, boolean isFirstResource) {
                        ThreadExecutorHelper.getMainThreadExecutor().execute(() -> {
                            //CELog.d("Kyle2 onResourceReady name="+name);
                            img.setImageDrawable(resource.getCurrent());
                            img.setVisibility(View.VISIBLE);
                            tv.setVisibility(View.INVISIBLE);
                        });
                        return false;
                    }
                })
                .submit();
        } catch (Exception ignored) {
        }
    }
}
