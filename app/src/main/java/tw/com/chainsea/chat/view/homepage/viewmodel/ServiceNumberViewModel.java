package tw.com.chainsea.chat.view.homepage.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import tw.com.chainsea.ce.sdk.bean.CustomerEntity;
import tw.com.chainsea.ce.sdk.bean.servicenumber.ServiceNumberEntity;

public class ServiceNumberViewModel extends ViewModel {
    private final MutableLiveData<ServiceNumberEntity> serviceNumberEntity = new MutableLiveData<>();
    private final MutableLiveData<List<CustomerEntity>> customers = new MutableLiveData<>();

    public MutableLiveData<ServiceNumberEntity> getServiceNumberEntity() {
        return serviceNumberEntity;
    }

    public void setServiceNumberEntity(ServiceNumberEntity serviceNumberEntity) {
        this.serviceNumberEntity.postValue(serviceNumberEntity);
    }

    public MutableLiveData<List<CustomerEntity>> getCustomers() {
        return customers;
    }

    public void setCustomers(List<CustomerEntity> customers) {
        this.customers.postValue(customers);
    }
}
