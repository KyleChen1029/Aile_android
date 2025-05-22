package tw.com.chainsea.chat.ui.utils.permissionUtils;

/**
 * 请求码
 *
 * @author AndSync
 * @date 2017/10/30
 * Copyright © 2014-2017 AndSync All rights reserved.
 */
public interface RequestCodeManger {
    /**
     * 电話
     */
    int PHONE = 0x00;
    /**
     * 位置
     */
    int LOCATION = 0x01;
    /**
     * 相機
     */
    int CAMERA = 0x02;
    /**
     * 語音
     */
    int AUDIO = 0x04;
    /**
     * 存储
     */
    int EXTERNAL = 0x08;
    /**
     * 多个
     */
    int MORE = 0x10;

    /* **************************************非系統************************************** */
    /**
     * 語音
     */
    int AUDIO_SELF = 0x12;
}