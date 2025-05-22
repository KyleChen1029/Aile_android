package tw.com.chainsea.ce.sdk.bean.chatrobot;

import java.io.Serializable;
import java.util.List;

public class ChatRoomRobotServiceListRsp implements Serializable {

    public boolean hasNextPage;
    public int count;
    public List<Object> items;
    public int refreshTime;

}
