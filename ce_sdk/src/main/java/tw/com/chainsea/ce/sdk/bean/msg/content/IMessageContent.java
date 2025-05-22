package tw.com.chainsea.ce.sdk.bean.msg.content;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Message entity Implementation of polymorphism Content interface
 * current by evan on 2020-01-08
 */
public interface IMessageContent<T> extends Serializable {

    T getType();

    String toStringContent();

    String simpleContent();

    String getFilePath();

    JSONObject getSendObj();

}
