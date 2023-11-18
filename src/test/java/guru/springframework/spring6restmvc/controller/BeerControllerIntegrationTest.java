package guru.springframework.spring6restmvc.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import guru.springframework.spring6restmvc.Spring6RestMvcApplication;
import guru.springframework.spring6restmvc.entities.Beer;
import guru.springframework.spring6restmvc.mappers.BeerMapper;
import guru.springframework.spring6restmvc.model.BeerDto;
import guru.springframework.spring6restmvc.model.BeerStyle;
import guru.springframework.spring6restmvc.repositories.BeerRepository;
import jakarta.transaction.Transactional;
import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = Spring6RestMvcApplication.class)
class BeerControllerIntegrationTest {
    @Autowired
    BeerController controller;
    @Autowired
    BeerRepository repository;
    @Autowired
    BeerMapper mapper;
    @Autowired
    WebApplicationContext wac;
    @Autowired
    ObjectMapper objectMapper;

    MockMvc mockMvc;
    public static final SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor JWT = jwt().jwt(jwt -> {
        jwt.claims(claims -> {
                    claims.put("scope", "message.read");
                    claims.put("scope", "message.write");
                })
                .subject("messaging-client")
                .notBefore(Instant.now().minusSeconds(5l));
    });
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .apply(springSecurity())
                .build();
    }

    @Test
    void listBeers() {
        Page<BeerDto> dtos = controller.listBeers(null, null, false, 1, 25);

        assertThat(dtos).hasSize(25);
    }

    @Test
    void listBeersByName() throws Exception {
        mockMvc.perform(get(BeerController.BEER_PATH)
                        .with(JWT)
                        .queryParam("beerName", "IPA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()", is(25)));

    }

    @Test
    void listBeersByStyle() throws Exception {
        mockMvc.perform(get(BeerController.BEER_PATH)
                        .with(JWT)
                        .queryParam("beerStyle", BeerStyle.IPA.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()", is(25)));

    }

    @Test
    void listBeersByNameAndStyle() throws Exception {
        mockMvc.perform(get(BeerController.BEER_PATH)
                        .with(JWT)
                        .queryParam("beerName", "IPA")
                        .queryParam("beerStyle", BeerStyle.IPA.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()", is(25)));

    }

    @Test
    void listBeersByNameAndStyleAndShowInventoryPage2() throws Exception {
        mockMvc.perform(get(BeerController.BEER_PATH)
                        .with(JWT)
                        .queryParam("beerName", "IPA")
                        .queryParam("beerStyle", BeerStyle.IPA.name())
                        .queryParam("showInventory", "TRUE")
                        .queryParam("pageNumber", "2")
                        .queryParam("pageSize", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()", is(50)))
                .andExpect(jsonPath("$.content.[0].quantityOnHand").value(IsNull.notNullValue()));

    }

    @Test
    void listBeersByNameAndStyleAndDontShowInventory() throws Exception {
        mockMvc.perform(get(BeerController.BEER_PATH)
                        .with(JWT)
                        .queryParam("beerName", "IPA")
                        .queryParam("beerStyle", BeerStyle.IPA.name())
                        .queryParam("showInventory", "FALSE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()", is(25)))
                .andExpect(jsonPath("$.content.[0].quantityOnHand").value(IsNull.nullValue()));

    }

    @Test
    @Transactional
    void emptyList() {
        repository.deleteAll();
        Page<BeerDto> dtos = controller.listBeers(null, null, false, 1, 25);

        assertThat(dtos).isEmpty();
    }

    @Test
    void getBeerById() {
        Beer beer = repository.findAll().get(0);

        BeerDto beerDto = controller.getBeerById(beer.getId());

        assertThat(beerDto).isNotNull();
    }

    @Test
    void beerIdNotFound() {
        assertThrows(NotFoundException.class, () -> {
            controller.getBeerById(UUID.randomUUID());
        });

    }

    @Test
    @Rollback
    @Transactional
    void saveNewBeer() {
        BeerDto beerDto = BeerDto.builder()
                .beerName("New Beer")
                .build();

        ResponseEntity responseEntity = controller.saveBeer(beerDto);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.valueOf(201));
        assertThat(responseEntity.getHeaders().getLocation()).isNotNull();

        String[] locationUUID = responseEntity.getHeaders().getLocation().getPath().split("/");

        UUID savedUUID = UUID.fromString(locationUUID[3]);

        Beer beer = repository.findById(savedUUID).get();
        assertThat(beer).isNotNull();
    }

    @Test
    @Transactional
    @Rollback
    void updateBeerById() {
        Beer beer = repository.findAll().get(0);

        BeerDto beerDto = mapper.beerToBeerDto(beer);

        beerDto.setId(null);
        beerDto.setVersion(null);

        final String beerName = "UPDATED";

        beerDto.setBeerName(beerName);

        ResponseEntity responseEntity = controller.updateBeerById(beer.getId(), beerDto);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(204));

        Beer updatedBeer = repository.findById(beer.getId()).get();

        assertThat(updatedBeer.getBeerName()).isEqualTo(beerName);
    }

    @Test
    @Transactional
    @Rollback
    void updateBeerByIdNotFound() {
        assertThrows(NotFoundException.class, () -> {
            controller.updateBeerById(UUID.randomUUID(), BeerDto.builder().build());
        });
    }

    @Test
    @Rollback
    @Transactional
    void deleteById() {
        Beer beer = repository.findAll().get(0);

        ResponseEntity responseEntity = controller.deleteBeerById(beer.getId());

        assertThat(repository.findById(beer.getId())).isEmpty();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(204));

    }

    @Test
    @Transactional
    @Rollback
    void deleteBeerByIdNotFound() {
        assertThrows(NotFoundException.class, () -> {
            controller.deleteBeerById(UUID.randomUUID());
        });
    }

    @Test
    void updateBeerPatchBadName() throws Exception {
        Beer beer = repository.findAll().get(0);

        Map<String, Object> beerMap = new HashMap<>();

        beerMap.put("beerName", "new Name 123 132132 1 32 1 32 13 21 3 21 3 21 32 1 32 1 32 1 32 1 321 3 21 32 132 1 32 1 321  32 13 21 32 1 32 1132132 1 32 132 1 32 13 21 32 1 32 1 32 1 32 132 1 321321");

        MvcResult mvcResult = mockMvc.perform(patch(BeerController.BEER_PATH + "/" + beer.getId())
                        .with(JWT)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(beerMap)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.length()", is(1)))
                .andReturn();
        System.out.println(mvcResult.getResponse().getContentAsString());
    }

}
