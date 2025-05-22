package tw.com.chainsea.chat.aiff;

public interface OnBridgeOpenRoom {
    void open(String roomId);

    void close();
    void openExternalWindow(String url);
    void openInternalAiffWindow(String url);

    void quote(String quote);
    void quoteAndSend(String type, String quote);
}
