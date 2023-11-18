package guru.springframework.spring6restmvc.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import guru.springframework.spring6restmvc.config.SpringSecurityConfig;
import guru.springframework.spring6restmvc.model.CustomerDto;
import guru.springframework.spring6restmvc.services.CustomerService;
import guru.springframework.spring6restmvc.services.CustomerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CustomerController.class)
@Import(SpringSecurityConfig.class)
class CustomerDtoControllerTest {

    public static final SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor JWT = jwt().jwt(jwt -> {
        jwt.claims(claims -> {
                    claims.put("scope", "message.read");
                    claims.put("scope", "message.write");
                })
                .subject("messaging-client")
                .notBefore(Instant.now().minusSeconds(5l));
    });
    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    CustomerService customerService;

    CustomerServiceImpl customerServiceImpl;

    @Captor
    ArgumentCaptor<UUID> uuidArgumentCaptor;

    @Captor
    ArgumentCaptor<CustomerDto> customerArgumentCaptor;

    @BeforeEach
    void setUp() {
        customerServiceImpl = new CustomerServiceImpl();
    }

    @Test
    void listCustomers() throws Exception {
        given(customerService.listCustomers()).willReturn(customerServiceImpl.listCustomers());

        mockMvc.perform(get(CustomerController.CUSTOMER_PATH)
                        .with(JWT)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()", is(3)));
    }

    @Test
    void getCustomerByIdNotFound() throws Exception {

        given(customerService.getCustomerById(any(UUID.class))).willReturn(Optional.empty());

        mockMvc.perform(get(CustomerController.CUSTOMER_PATH + "/" + UUID.randomUUID())
                        .with(JWT))
                .andExpect(status().isNotFound());
    }

    @Test
    void getCustomerById() throws Exception {
        CustomerDto testCustomerDto = customerServiceImpl.listCustomers().get(0);

        given(customerService.getCustomerById(testCustomerDto.getId())).willReturn(Optional.of(testCustomerDto));

        mockMvc.perform(get(CustomerController.CUSTOMER_PATH + "/" + testCustomerDto.getId())
                        .with(JWT)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(testCustomerDto.getId().toString())))
                .andExpect(jsonPath("$.name", is(testCustomerDto.getName())));
    }

    @Test
    void saveCustomer() throws Exception {
        CustomerDto customerDto = customerServiceImpl.listCustomers().get(0);
        customerDto.setVersion(null);
        customerDto.setId(null);

        given(customerService.saveCustomer(any(CustomerDto.class))).willReturn(customerServiceImpl.listCustomers().get(1));

        mockMvc.perform(post(CustomerController.CUSTOMER_PATH)
                        .with(JWT)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerDto)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));
    }

    @Test
    void saveCustomerNullName() throws Exception {
        CustomerDto customerDto = CustomerDto.builder().build();


        given(customerService.saveCustomer(any(CustomerDto.class))).willReturn(customerServiceImpl.listCustomers().get(1));

        mockMvc.perform(post(CustomerController.CUSTOMER_PATH)
                        .with(JWT)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.length()", is(2)))
                .andReturn();
    }

    @Test
    void updateCustomerById() throws Exception {
        CustomerDto customerDto = customerServiceImpl.listCustomers().get(0);

        given(customerService.updateCustomerById(any(), any())).willReturn(Optional.of(customerDto));

        mockMvc.perform(put(CustomerController.CUSTOMER_PATH + "/" + customerDto.getId())
                        .with(JWT)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerDto)))
                .andExpect(status().isNoContent());

        verify(customerService).updateCustomerById(any(UUID.class), any(CustomerDto.class));
    }

    @Test
    void updateCustomerByIdBlankName() throws Exception {
        CustomerDto customerDto = customerServiceImpl.listCustomers().get(0);

        customerDto.setName("");
        given(customerService.updateCustomerById(any(), any())).willReturn(Optional.of(customerDto));

        mockMvc.perform(put(CustomerController.CUSTOMER_PATH + "/" + customerDto.getId())
                        .with(JWT)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.length()", is(1)));

    }

    @Test
    void deleteCustomerById() throws Exception {

        CustomerDto customerDto = customerServiceImpl.listCustomers().get(0);

        given(customerService.deleteCustomerById(any())).willReturn(true);

        mockMvc.perform(delete(CustomerController.CUSTOMER_PATH + "/" + customerDto.getId())
                        .with(JWT)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(customerService).deleteCustomerById(uuidArgumentCaptor.capture());

        assertThat(customerDto.getId()).isEqualTo(uuidArgumentCaptor.getValue());

    }

    @Test
    void updateCustomerPatchById() throws Exception {

        CustomerDto customerDto = customerServiceImpl.listCustomers().get(0);

        Map<String, Object> customerMap = new HashMap<>();

        customerMap.put("name", "new Name");
        given(customerService.patchCustomerById(any(), any())).willReturn(Optional.of(customerDto));


        mockMvc.perform(patch(CustomerController.CUSTOMER_PATH + "/" + customerDto.getId())
                        .with(JWT)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerMap)))
                .andExpect(status().isNoContent());

        verify(customerService).patchCustomerById(uuidArgumentCaptor.capture(), customerArgumentCaptor.capture());

        assertThat(customerDto.getId()).isEqualTo(uuidArgumentCaptor.getValue());
        assertThat(customerMap.get("name")).isEqualTo(customerArgumentCaptor.getValue().getName());
    }
}
