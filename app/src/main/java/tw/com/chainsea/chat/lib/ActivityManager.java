package tw.com.chainsea.chat.lib;

import android.app.Activity;

import androidx.appcompat.app.AppCompatActivity;

import com.google.common.collect.Lists;

import java.util.Iterator;
import java.util.List;

import tw.com.chainsea.chat.view.base.HomeActivity;

/**
 * Created by sunhui on 2018/5/8.
 */

public class ActivityManager {
    public static List<AppCompatActivity> activities = Lists.newArrayList();
    public static List<AppCompatActivity> baseActivities = Lists.newArrayList();

    public static void addActivity(AppCompatActivity activity) {
        activities.add(activity);
    }

    public static void removeActivity(AppCompatActivity activity) {
        activities.remove(activity);
    }

    public static void finishAll() {
        for (Activity activity : activities) {
            if (!activity.isFinishing()) {
                activity.finish();
            }
        }
    }

    public static boolean hasActiviryInCacheBy(Class zlass) {
        for (Activity activity : activities) {
            if (!activity.isFinishing() && zlass.isInstance(activity)) {
                return true;
            }
        }
        return false;
    }

    public static void finishBy(Class zlass) {
        Iterator<AppCompatActivity> iterator = activities.iterator();
        while (iterator.hasNext()) {
            Activity activity = iterator.next();
            if (!activity.isFinishing() && zlass.isInstance(activity)) {
                activity.finish();
//                activity.overridePendingTransition(R.anim.slide_left_in, R.anim.slide_left_out);
                iterator.remove();
            }
        }
    }

    public static void finishBy(List<Class> zlasss) {
        for (Activity activity : activities) {
            for (Class zlass : zlasss) {
                if (!activity.isFinishing() && zlass.isInstance(activity)) {
                    activity.finish();
                }
            }
        }
    }

    public static void addBaseActivity(AppCompatActivity activity) {
        baseActivities.add(activity);
    }

    public static void removeBaseActivity(AppCompatActivity activity) {
        baseActivities.remove(activity);
    }

    public static void finishBaseActivity() {
        for (AppCompatActivity baseActivity : baseActivities) {
            baseActivity.finish();
        }
    }

    public static AppCompatActivity findBaseActivity() {
        for (AppCompatActivity activity : baseActivities) {
            if (activity instanceof HomeActivity) {
                return activity;
            }
        }

        for (AppCompatActivity activity : activities) {
            if (activity instanceof HomeActivity) {
                return activity;
            }
        }
        return null;
    }

    public static AppCompatActivity getTopActivity() {
        if (!activities.isEmpty()) {
            return activities.get(activities.size() - 1);
        }
        return baseActivities.get(0);
    }
}
