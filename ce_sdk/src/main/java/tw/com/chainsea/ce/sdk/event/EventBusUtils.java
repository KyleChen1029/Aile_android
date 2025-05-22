package tw.com.chainsea.ce.sdk.event;

import org.greenrobot.eventbus.EventBus;

public class EventBusUtils {

    /**
     * Binding recipient
     */
    public static void register(Object subscriber) {
        EventBus.getDefault().register(subscriber);
    }

    /**
     * Unbind
     */
    public static void unregister(Object subscriber){
        EventBus.getDefault().unregister(subscriber);
    }

    /**
     * Send message (event)
     */
    public static void sendEvent(EventMsg event){
        EventBus.getDefault().post(event);
    }

}
