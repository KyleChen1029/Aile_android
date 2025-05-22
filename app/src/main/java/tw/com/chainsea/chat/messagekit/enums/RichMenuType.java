package tw.com.chainsea.chat.messagekit.enums;

import com.google.common.collect.Lists;

import java.util.List;

import static tw.com.chainsea.chat.messagekit.enums.RichMenuBottom.*;

public enum RichMenuType {
    TEXT_RICH(Lists.newArrayList(REPLY, DELETE, SHARE, MULTI_TRANSPOND, MULTI_COPY, SCREENSHOTS, TASK, TODO)),
    AT_RICH(Lists.newArrayList(REPLY, DELETE, SHARE, MULTI_TRANSPOND, MULTI_COPY, SCREENSHOTS, TASK, TODO)),
    VOICE_RICH(Lists.newArrayList(REPLY, DELETE, MULTI_TRANSPOND, SCREENSHOTS, TASK, TODO)),
    VIDEO_RICH(Lists.newArrayList(REPLY, DELETE, MULTI_TRANSPOND, SCREENSHOTS, TASK, TODO)),
    OTHER_RICH(Lists.newArrayList(REPLY, DELETE, MULTI_TRANSPOND, SCREENSHOTS, TASK, TODO)),
    STICKER_RICH(Lists.newArrayList(REPLY, DELETE, SCREENSHOTS, TASK, TODO)),
    IMAGE_RICH(Lists.newArrayList(REPLY, DELETE, SHARE, MULTI_TRANSPOND, SCREENSHOTS, TASK, TODO)),
    CALL_RICH(Lists.newArrayList(DELETE, SCREENSHOTS, TODO)),
    REPLY_RICH(Lists.newArrayList(REPLY, DELETE, SHARE, MULTI_TRANSPOND, MULTI_COPY, SCREENSHOTS, TASK, TODO)),
    TEMPLATE_RICH(Lists.newArrayList(SCREENSHOTS, DELETE, TODO)),
    AI_CONSULT_RICH(Lists.newArrayList(QUOTE, COPY, TODO));

    private List<RichMenuBottom> list;

    public List<RichMenuBottom> get() {
        return list;
    }

    RichMenuType(List<RichMenuBottom> list) {
        this.list = list;
    }
}
