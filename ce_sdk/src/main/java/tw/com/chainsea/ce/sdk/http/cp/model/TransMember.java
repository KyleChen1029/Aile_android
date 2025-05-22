package tw.com.chainsea.ce.sdk.http.cp.model;

public class TransMember implements Comparable<TransMember>{
    private String accountId;
    private String name;
    private String status;

    public String getAccountId() {
        return accountId;
    }

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }


    @Override
    public int compareTo(TransMember o) {
        return this.status.compareTo(o.status);
    }
}
