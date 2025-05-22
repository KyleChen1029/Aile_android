package tw.com.chainsea.chat.ui.ife;

import java.util.List;

import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity;
import tw.com.chainsea.chat.base.IBaseView;

/**
 * Created by sunhui on 2018/3/21.
 */

public interface ILabelPreviewView extends IBaseView {

    void initSelectAccount(List<UserProfileEntity> choicedAccount);

    void initAccountList(List<UserProfileEntity> accounts);

    void selectRefresh();

    void updateSuccess(String name, List<UserProfileEntity> choicedAccounts);

    void updateFailed(String value);

    void showSearchAccounts();

    void initSearchHistory(List<String> records);

    void refreshList(List<String> ids);
    void broastcardRefresh(UserProfileEntity accountCE);
}
