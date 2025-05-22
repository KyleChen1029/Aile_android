package tw.com.chainsea.chat.pchat.ui.ife;


import androidx.annotation.StringRes;

import java.util.List;

import tw.com.chainsea.ce.sdk.http.ce.request.ServiceNumberSearchRequest;
import tw.com.chainsea.chat.base.IBaseView;

/**
 * Created by jerry.yang on 2018/3/19.
 * desc:
 */
public interface ISearchVipcnView extends IBaseView {

    void refreshRequestResult(ServiceNumberSearchRequest.Resp searchResp);

    void refreshSearchResult(List<ServiceNumberSearchRequest.Resp.Item> items, String keyWord);

//    void showLoadingView(String tip);

    void showLoadingView(@StringRes int resId);

    void hideLoadingView();
}
