package tw.com.chainsea.chat.ui.ife;

import java.util.List;

import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity;
import tw.com.chainsea.chat.base.IBaseView;

/**
 * Created by sunhui on 2018/3/5.
 */

public interface IChoiceView extends IBaseView {

    void initSelectAccount(List<UserProfileEntity> accounts);

    void initAccountList(List<UserProfileEntity> accounts);

    void selectRefresh();

    void finish();

    void refreshList(List<String> ids);

    void showSearchAccounts();

    void initSearchHistory(List<String> records);
}
