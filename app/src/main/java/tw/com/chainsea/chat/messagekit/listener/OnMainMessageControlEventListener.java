package tw.com.chainsea.chat.messagekit.listener;

        import tw.com.aile.sdk.bean.message.MessageEntity;

/**
 * current by evan on 2020-02-12
 */
public abstract class OnMainMessageControlEventListener<T extends MessageEntity> implements OnMessageControlEventListener<T> {

        public abstract void makeUpMessages(MessageEntity current, MessageEntity previous);

        public abstract void doRangeSelection(MessageEntity entity);
}
