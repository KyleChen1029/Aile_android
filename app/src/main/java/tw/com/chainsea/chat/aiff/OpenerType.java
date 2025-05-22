package tw.com.chainsea.chat.aiff;

import static tw.com.chainsea.chat.aiff.AiffKey.BROADCAST;
import static tw.com.chainsea.chat.aiff.AiffKey.BUSINESS;
import static tw.com.chainsea.chat.aiff.AiffKey.DISCUSS;
import static tw.com.chainsea.chat.aiff.AiffKey.FRIEND;
import static tw.com.chainsea.chat.aiff.AiffKey.GROUP;
import static tw.com.chainsea.chat.aiff.AiffKey.PERSON;
import static tw.com.chainsea.chat.aiff.AiffKey.SERVICE;
import static tw.com.chainsea.chat.aiff.AiffKey.SERVICES;
import static tw.com.chainsea.chat.aiff.AiffKey.SERVICE_MEMBER;
import static tw.com.chainsea.chat.aiff.AiffKey.STRANGE;
import static tw.com.chainsea.chat.aiff.AiffKey.SUBSCRIBE;
import static tw.com.chainsea.chat.aiff.AiffKey.SYSTEM;
import static tw.com.chainsea.chat.aiff.AiffKey.TEMP;

public class OpenerType {

    public static String returnType(String roomType, boolean isSubscriber) {
        switch (roomType) {
            case PERSON:
                return "self";
            case STRANGE:
                return "vistor";
            case FRIEND:
                return "friend";
            case DISCUSS:
                return "discuss";
            case GROUP:
                return "group";
            case SERVICES:
                if(isSubscriber)
                    return "serviceSub";
                else
                    return "serviceRoom";
            case SUBSCRIBE:
                return "serviceSub";
            case SYSTEM:
                return "system";
            case SERVICE_MEMBER:
                return "serviceMember";
            case BUSINESS:
            case SERVICE:
            case BROADCAST:
            case TEMP:
                return "";
        }
        return "unknown";
    }
}
