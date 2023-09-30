package guru.springframework.spring6restmvc.services;

import guru.springframework.spring6restmvc.model.Customer;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class CustomerServiceImpl implements CustomerService {
    private final Map<UUID, Customer> customerMap = new HashMap<>();

    public CustomerServiceImpl() {
        Customer customer1 = Customer.builder()
                .id(UUID.randomUUID())
                .name("Customer 1")
                .version(1)
                .createdDate(LocalDateTime.now())
                .updateDate(LocalDateTime.now())
                .build();
        Customer customer2 = Customer.builder()
                .id(UUID.randomUUID())
                .name("Customer 2")
                .version(1)
                .createdDate(LocalDateTime.now())
                .updateDate(LocalDateTime.now())
                .build();
        Customer customer3 = Customer.builder()
                .id(UUID.randomUUID())
                .name("Customer 3")
                .version(1)
                .createdDate(LocalDateTime.now())
                .updateDate(LocalDateTime.now())
                .build();

        customerMap.put(customer1.getId(), customer1);
        customerMap.put(customer2.getId(), customer2);
        customerMap.put(customer3.getId(), customer3);
    }

    @Override
    public Customer getCustomerById(UUID id) {
        return customerMap.get(id);
    }

    @Override
    public List<Customer> listCustomers() {
        return new ArrayList<>(customerMap.values());
    }

    @Override
    public Customer saveCustomer(Customer customer) {
        customer.setId(UUID.randomUUID());
        customer.setCreatedDate(LocalDateTime.now());
        customer.setUpdateDate(LocalDateTime.now());

        customerMap.put(customer.getId(), customer);
        return customer;
    }

    @Override
    public void updateCustomerById(UUID id, Customer customer) {
        Customer existingCustomer = customerMap.get(id);

        existingCustomer.setName(customer.getName());
        existingCustomer.setUpdateDate(LocalDateTime.now());

        customerMap.put(id, existingCustomer);
    }

    @Override
    public void deleteCustomerById(UUID id) {
        customerMap.remove(id);
    }

    @Override
    public void patchCustomerById(UUID id, Customer customer) {
        Customer existedCustomer = customerMap.get(id);

        if (StringUtils.hasText(customer.getName())) {
            existedCustomer.setName(customer.getName());
        }

        customerMap.put(id, existedCustomer);
    }
}
