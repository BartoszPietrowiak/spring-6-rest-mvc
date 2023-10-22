package guru.springframework.spring6restmvc.services;

import guru.springframework.spring6restmvc.model.CustomerDto;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class CustomerServiceImpl implements CustomerService {
    private final Map<UUID, CustomerDto> customerMap = new HashMap<>();

    public CustomerServiceImpl() {
        CustomerDto customerDto1 = CustomerDto.builder()
                .id(UUID.randomUUID())
                .name("CustomerDto 1")
                .version(1)
                .createdDate(LocalDateTime.now())
                .updateDate(LocalDateTime.now())
                .build();
        CustomerDto customerDto2 = CustomerDto.builder()
                .id(UUID.randomUUID())
                .name("CustomerDto 2")
                .version(1)
                .createdDate(LocalDateTime.now())
                .updateDate(LocalDateTime.now())
                .build();
        CustomerDto customerDto3 = CustomerDto.builder()
                .id(UUID.randomUUID())
                .name("CustomerDto 3")
                .version(1)
                .createdDate(LocalDateTime.now())
                .updateDate(LocalDateTime.now())
                .build();

        customerMap.put(customerDto1.getId(), customerDto1);
        customerMap.put(customerDto2.getId(), customerDto2);
        customerMap.put(customerDto3.getId(), customerDto3);
    }

    @Override
    public Optional<CustomerDto> getCustomerById(UUID id) {
        return Optional.of(customerMap.get(id));
    }

    @Override
    public List<CustomerDto> listCustomers() {
        return new ArrayList<>(customerMap.values());
    }

    @Override
    public CustomerDto saveCustomer(CustomerDto customerDto) {
        customerDto.setId(UUID.randomUUID());
        customerDto.setCreatedDate(LocalDateTime.now());
        customerDto.setUpdateDate(LocalDateTime.now());

        customerMap.put(customerDto.getId(), customerDto);
        return customerDto;
    }

    @Override
    public Optional<CustomerDto> updateCustomerById(UUID id, CustomerDto customerDto) {
        CustomerDto existingCustomerDto = customerMap.get(id);

        existingCustomerDto.setName(customerDto.getName());
        existingCustomerDto.setUpdateDate(LocalDateTime.now());

        customerMap.put(id, existingCustomerDto);

        return Optional.of(existingCustomerDto);
    }

    @Override
    public boolean deleteCustomerById(UUID id) {
        customerMap.remove(id);

        return true;
    }

    @Override
    public Optional<CustomerDto> patchCustomerById(UUID id, CustomerDto customerDto) {
        CustomerDto existedCustomerDto = customerMap.get(id);

        if (StringUtils.hasText(customerDto.getName())) {
            existedCustomerDto.setName(customerDto.getName());
        }

        customerMap.put(id, existedCustomerDto);

        return Optional.of(existedCustomerDto);
    }
}
