package guru.springframework.spring6restmvc.controller;


import guru.springframework.spring6restmvc.model.Customer;
import guru.springframework.spring6restmvc.services.CustomerService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/customer")
public class CustomerController {
    private final CustomerService customerService;

    @GetMapping
    public List<Customer> listCustomers() {

        log.debug("List Customer - in controller");

        return customerService.listCustomers();
    }

    @GetMapping("/{id}")
    public Customer getCustomerById(@PathVariable UUID id) {

        log.debug("Get Customer by Id - in controller");

        return customerService.getCustomerById(id);
    }

    @PostMapping
    public ResponseEntity saveCustomer(@RequestBody Customer customer) {

        Customer savedCustomer = customerService.saveCustomer(customer);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", "api/v1/customer/" + savedCustomer.getId().toString());

        return new ResponseEntity(headers, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity updateCustomerById(@PathVariable UUID id,
                                     @RequestBody Customer customer) {

        customerService.updateCustomerById(id, customer);

        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity deleteCustomerById(@PathVariable UUID id) {

        customerService.deleteCustomerById(id);

        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @PatchMapping("/{id}")
    public ResponseEntity updateCustomerPatchById(@PathVariable UUID id,
                                     @RequestBody Customer customer) {

        customerService.patchCustomerById(id, customer);

        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }
}
