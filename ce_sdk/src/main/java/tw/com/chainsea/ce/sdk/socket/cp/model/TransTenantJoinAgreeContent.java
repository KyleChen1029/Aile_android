package tw.com.chainsea.ce.sdk.socket.cp.model;

import java.util.List;

import tw.com.chainsea.ce.sdk.http.cp.model.TransMember;

public class TransTenantJoinAgreeContent {
    private List<TransMember> transMemberArray;

    public List<TransMember> getTransMemberArray() {
        return transMemberArray;
    }
}
