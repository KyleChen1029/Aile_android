package tw.com.chainsea.ce.sdk.bean.response.base;

import java.io.Serializable;
/**
 * current by evan on 2020-10-22
 *
 * @author Evan Wang
 * date 2020-10-22
 */
public abstract class RequestBean implements AutoCloseable, Serializable {

    private _Header_ _header_;

    public _Header_ get_header_() {
        return _header_;
    }

    public void set_header_(_Header_ _header_) {
        this._header_ = _header_;
    }


    public abstract String toRequestString();

    public static class _Header_ implements Serializable {
        private String tokenId;
        private String language;

        public String getTokenId() {
            return tokenId;
        }

        public void setTokenId(String tokenId) {
            this.tokenId = tokenId;
        }

        public String getLanguage() {
            return language;
        }

        public void setLanguage(String language) {
            this.language = language;
        }
    }
}
