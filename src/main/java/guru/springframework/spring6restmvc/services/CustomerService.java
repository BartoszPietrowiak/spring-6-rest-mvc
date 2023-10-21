package guru.springframework.spring6restmvc.services;

import guru.springframework.spring6restmvc.model.CustomerDto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerService {
    Optional<CustomerDto> getCustomerById(UUID id);

    List<CustomerDto> listCustomers();

    CustomerDto saveCustomer(CustomerDto customerDto);

    Optional<CustomerDto> updateCustomerById(UUID id, CustomerDto customerDto);

    boolean deleteCustomerById(UUID id);

    void patchCustomerById(UUID id, CustomerDto customerDto);
}
