package tw.com.chainsea.ce.sdk.http.ce.request;


/**
 * Created by jerry.yang on 2017/10/11.
 * desc: 业务附件/文档上传
 * https://server/ecp/openapi/ecp/contact/attachment/upload
 */

public class UploadAttachmentRequest {
    private Listener mListener;

    public UploadAttachmentRequest(Listener listener) {
        mListener = listener;
    }

    public interface Listener {
        void onUplodeAttachmentSuccess(String attachmentId);

        void onUplodeAttachmentFailed(String reason, String path);

        void onUplodeAttachmentProgress(int progress);
    }

}
