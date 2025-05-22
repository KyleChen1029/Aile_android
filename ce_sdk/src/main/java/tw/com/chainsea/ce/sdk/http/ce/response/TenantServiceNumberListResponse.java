package tw.com.chainsea.ce.sdk.http.ce.response;

import java.util.List;

import tw.com.chainsea.ce.sdk.http.cp.base.BaseResponse;

public class TenantServiceNumberListResponse extends BaseResponse {

    private List<ItemsDTO> items;

    public List<ItemsDTO> getItems() {
        return items;
    }

    public void setItems(List<ItemsDTO> items) {
        this.items = items;
    }

    public static class ItemsDTO {
        private String serviceNumberId;
        private String statusX;
        private String name;

        public String getServiceNumberId() {
            return serviceNumberId;
        }

        public void setServiceNumberId(String serviceNumberId) {
            this.serviceNumberId = serviceNumberId;
        }

        public String getStatusX() {
            return statusX;
        }

        public void setStatusX(String statusX) {
            this.statusX = statusX;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
