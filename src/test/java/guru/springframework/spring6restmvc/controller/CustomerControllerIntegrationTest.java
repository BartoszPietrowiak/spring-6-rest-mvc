package guru.springframework.spring6restmvc.controller;

import guru.springframework.spring6restmvc.Spring6RestMvcApplication;
import guru.springframework.spring6restmvc.entities.Customer;
import guru.springframework.spring6restmvc.mappers.CustomerMapper;
import guru.springframework.spring6restmvc.model.CustomerDto;
import guru.springframework.spring6restmvc.repositories.CustomerRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.Rollback;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(classes = Spring6RestMvcApplication.class)
class CustomerControllerIntegrationTest {
    @Autowired
    CustomerController controller;
    @Autowired
    CustomerRepository repository;
    @Autowired
    CustomerMapper mapper;

    @Test
    void testListCustomers() {
        List<CustomerDto> dtos = controller.listCustomers();

        assertThat(dtos).hasSize(3);
    }

    @Test
    @Transactional
    void testEmptyList() {
        repository.deleteAll();
        List<CustomerDto> dtos = controller.listCustomers();

        assertThat(dtos).isEmpty();
    }

    @Test
    void testGetCustomerById() {
        Customer customer = repository.findAll().get(0);

        CustomerDto customerDto = controller.getCustomerById(customer.getId());

        assertThat(customerDto).isNotNull();
    }

    @Test
    void testCustomerIdNotFound() {
        assertThrows(NotFoundException.class, () -> {
            controller.getCustomerById(UUID.randomUUID());
        });

    }

    @Test
    @Rollback
    @Transactional
    void testSaveNewCustomer() {
        CustomerDto customerDto = CustomerDto.builder()
                .name("New Name")
                .build();

        ResponseEntity responseEntity = controller.saveCustomer(customerDto);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.valueOf(201));
        assertThat(responseEntity.getHeaders().getLocation()).isNotNull();

        String[] locationUUID = responseEntity.getHeaders().getLocation().getPath().split("/");

        UUID savedUUID = UUID.fromString(locationUUID[3]);

        Customer customer = repository.findById(savedUUID).get();
        assertThat(customer).isNotNull();
    }

    @Test
    @Transactional
    @Rollback
    void testUpdateCustomerById() {
        Customer customer = repository.findAll().get(0);

        CustomerDto customerDto = mapper.customertoCustomerDto(customer);

        customerDto.setId(null);
        customerDto.setVersion(null);

        final String customerName = "UPDATED";

        customerDto.setName(customerName);

        ResponseEntity responseEntity = controller.updateCustomerById(customer.getId(), customerDto);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(204));

        Customer updatedCustomer = repository.findById(customer.getId()).get();

        assertThat(updatedCustomer.getName()).isEqualTo(customerName);
    }

    @Test
    @Transactional
    @Rollback
    void testUpdateCustomerByIdNotFound() {
        assertThrows(NotFoundException.class, () -> {
            controller.updateCustomerById(UUID.randomUUID(), CustomerDto.builder().build());
        });
    }

    @Test
    @Rollback
    @Transactional
    void testDeleteById() {
        Customer customer = repository.findAll().get(0);

        ResponseEntity responseEntity = controller.deleteCustomerById(customer.getId());

        assertThat(repository.findById(customer.getId())).isEmpty();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(204));

    }

    @Test
    @Transactional
    @Rollback
    void testDeleteCustomerByIdNotFound() {
        assertThrows(NotFoundException.class, () -> {
            controller.deleteCustomerById(UUID.randomUUID());
        });
    }

}
