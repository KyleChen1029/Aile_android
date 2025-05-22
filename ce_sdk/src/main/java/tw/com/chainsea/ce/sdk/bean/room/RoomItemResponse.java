package tw.com.chainsea.ce.sdk.bean.room;

import java.io.Serializable;

import tw.com.chainsea.ce.sdk.network.model.common.Header;

public class RoomItemResponse extends ChatRoomEntity implements Serializable {
    private Header _header_;

    public Header get_header_() {
        return _header_;
    }

    public void set_header_(Header _header_) {
        this._header_ = _header_;
    }
}
