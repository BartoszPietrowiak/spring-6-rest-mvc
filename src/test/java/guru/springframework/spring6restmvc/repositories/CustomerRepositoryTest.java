package guru.springframework.spring6restmvc.repositories;

import guru.springframework.spring6restmvc.entities.Beer;
import guru.springframework.spring6restmvc.entities.Customer;
import guru.springframework.spring6restmvc.model.BeerStyle;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
class CustomerRepositoryTest {

    @Autowired
    CustomerRepository customerRepository;

    @Test
    void SaveCustomer() {
        Customer savedCustomer = customerRepository.save(Customer.builder()
                .name("New Name")
                .build());
        customerRepository.flush();
        assertThat(savedCustomer).isNotNull();
        assertThat(savedCustomer.getId()).isNotNull();
    }

    @Test
    void SaveCustomerNameTooLong() {
        assertThrows(ConstraintViolationException.class, () -> {
            Customer savedCustomer = customerRepository.save(Customer.builder()
                    .name("My beera sdadsa dsadsads ad s ads a ds ads a ds a ds a d a ds a ds a ds a ds a ds a dsa ds a ds adsasdadsadsdsa dsasd ad sa dsa dsa")
                    .build());

            customerRepository.flush();
        });
    }

}
