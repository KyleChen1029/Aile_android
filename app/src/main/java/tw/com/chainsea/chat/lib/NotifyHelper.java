package tw.com.chainsea.chat.lib;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.media.SoundPool;
import android.net.Uri;
import android.webkit.URLUtil;

import androidx.core.app.NotificationCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.FutureTarget;
import com.google.common.base.Strings;

import java.math.BigInteger;
import java.util.Objects;

import tw.com.aile.sdk.bean.message.MessageEntity;
import tw.com.chainsea.android.common.log.CELog;
import tw.com.chainsea.ce.sdk.bean.msg.MessageFlag;
import tw.com.chainsea.ce.sdk.bean.msg.MessageType;
import tw.com.chainsea.ce.sdk.bean.msg.content.AtContent;
import tw.com.chainsea.ce.sdk.bean.msg.content.ImageContent;
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity;
import tw.com.chainsea.ce.sdk.database.sp.UserPref;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.config.BundleKey;
import tw.com.chainsea.chat.task.LoadImageTask;
import tw.com.chainsea.chat.ui.activity.ChatActivity;

/**
 * current by evan on 2020-08-12
 *
 * @author Evan Wang
 * @date 2020-08-12
 */
public class NotifyHelper {


    /**
     * retract
     *
     * @param context
     * @param roomId
     * @param messageId
     */
    public static void retractNotify(Context context, String roomId, String messageId) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(roomId, uniqueId(messageId));
    }

    /**
     * Local push processing for new messages
     *
     * @param context
     * @param roomEntity
     * @param messageEntity
     */
    public static void sendMessageNewNotify(Context context, ChatRoomEntity roomEntity, MessageEntity messageEntity) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent chatIntent = new Intent(context, ChatActivity.class);
        chatIntent.putExtra(BundleKey.EXTRA_SESSION_ID.key(), messageEntity.getRoomId());

        if (MessageFlag.RETRACT.equals(messageEntity.getFlag())) {
            notificationManager.cancel(roomEntity.getId(), uniqueId(messageEntity.getId()));
        } else {
            String title = roomEntity == null ? context.getString(R.string.notify_new_reminder) : roomEntity.getName();
            CharSequence content = "";

            if (Objects.requireNonNull(messageEntity.getType()) == MessageType.AT) {
                if (roomEntity != null) {
                    content = AtMatcherHelper.matcherAtUsers("@", ((AtContent) messageEntity.content()).getMentionContents(), roomEntity.getMembersTable());
                } else {
                    content = messageEntity.content().simpleContent();
                }
            } else {
                content = messageEntity.content().simpleContent();
            }

            PendingIntent pendingIntent = PendingIntent.getActivity(context, 1, chatIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
            String channelId = context.getString(R.string.app_name);

            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder notificationBuilder =
                    new NotificationCompat.Builder(context, channelId)
                            .setColor(context.getColor(R.color.colorAccent))
                            .setSmallIcon(R.drawable.ce_notification_icon)
                            .setContentTitle(title)
                            .setContentText(content)
                            .setAutoCancel(true)
                            .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
                            .setNumber(UserPref.getInstance(context).getBrand())
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                            .setSound(!Strings.isNullOrEmpty(messageEntity.getRingName()) ? defaultSoundUri : null)
                            .setWhen(System.currentTimeMillis())
                            .setShowWhen(true)
                            .setDefaults(NotificationCompat.DEFAULT_VIBRATE | NotificationCompat.DEFAULT_SOUND | NotificationCompat.DEFAULT_ALL)
                            .setContentIntent(pendingIntent);

            // Since android Oreo notification channel is needed.
            NotificationChannel channel = new NotificationChannel(channelId, title, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setShowBadge(true);
            notificationManager.createNotificationChannel(channel);

//            try {
//                throw new RuntimeException("Update the unread quantity and corner mark");
////
////
////                badgeCountList.set(0, presenter.getMainBadge());
////                if (getActivity() instanceof BaseActivity) {
////                    ((BaseActivity) getActivity()).setMainUnReadNum(presenter.getMainBadge());
////                }
////
////                badgeCountList.set(1, presenter.getServiceBadge());
////                if (getActivity() instanceof BaseActivity) {
////                    ((BaseActivity) getActivity()).setServiceUnReadNum(presenter.getServiceBadge());
////                }
//            } catch (Exception e) {
//                CELog.e("Abnormal setting of corner mark：" + e.toString());
//            }

            if (MessageType.IMAGE.equals(messageEntity.getType())) {
                if (messageEntity.content() instanceof ImageContent) {
                    String thumbnailUrl = ((ImageContent) messageEntity.content()).getThumbnailUrl();
                    if (URLUtil.isValidUrl(thumbnailUrl)) {
                        FutureTarget<Bitmap> futureTarget = Glide
                                .with(context)
                                .asBitmap()
                                .load(thumbnailUrl)
                                .circleCrop()
                                .submit();
                        LoadImageTask task = new LoadImageTask(icon -> {
                            if (icon != null) {
                                notificationBuilder.setLargeIcon(icon);
                                Glide.with(context).clear(futureTarget);
                                CELog.d("send local notify");
                                notificationManager.notify(roomEntity.getId(), uniqueId(messageEntity.getId())/* ID of notification */, notificationBuilder.build());
                            }
                        });
                        task.execute(futureTarget);
                    }
                }
            } else {
                CELog.d("send local notify");
                notificationManager.notify(roomEntity.getId(), uniqueId(messageEntity.getId())/* ID of notification */, notificationBuilder.build());
            }
        }
    }

    private static int uniqueId(String messageId) {
        String uuid = messageId.replaceAll("-", "");
        BigInteger big = new BigInteger(uuid, 32);
        int alarmId = big.intValue();
        return Math.abs(alarmId);
    }


    private static SoundPool soundPool;
    private static int messageSound;

    public static void playNotifyTone(Context context, String ringName) {
        if (Strings.isNullOrEmpty(ringName)) {
            return;
        }
        if (soundPool == null) {
            SoundPool.Builder builder = new SoundPool.Builder();
            // Incoming the maximum number of audio playback,
            builder.setMaxStreams(10);
            // AudioAttributes Is a method to encapsulate various properties of audio
            AudioAttributes.Builder attrBuilder = new AudioAttributes.Builder();
            // Set the appropriate properties of the audio stream
            attrBuilder.setLegacyStreamType(AudioManager.STREAM_RING);
//                attrBuilder.setLegacyStreamType(AudioManager.STREAM_MUSIC);
            // Load an Audio Attributes
            builder.setAudioAttributes(attrBuilder.build());
            soundPool = builder.build();
            messageSound = soundPool.load(context, R.raw.message_ringtone, 1);
            soundPool.setOnLoadCompleteListener((soundPool, sampleId, status) -> soundPool.play(messageSound, 1, 1, 0, 0, 1));
        }

        soundPool.play(messageSound, 1, 1, 0, 0, 1);

//        if (soundPool == null) {
//            soundPool = new SoundPool(10, AudioManager.STREAM_NOTIFICATION, 0);
//            messageSound = soundPool.load(ctx, R.raw.message_ringtone, 1);
//            soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
//                @Override
//                public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
//                    soundPool.play(1, 1, 1, 100, 0, 1);
//                }
//            });
//        } else {
//            soundPool.play(messageSound, 1, 1, 0, 0, 1);
//        }
    }


}
