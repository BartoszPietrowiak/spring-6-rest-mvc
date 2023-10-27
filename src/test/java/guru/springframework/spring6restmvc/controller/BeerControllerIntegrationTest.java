package guru.springframework.spring6restmvc.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import guru.springframework.spring6restmvc.Spring6RestMvcApplication;
import guru.springframework.spring6restmvc.entities.Beer;
import guru.springframework.spring6restmvc.mappers.BeerMapper;
import guru.springframework.spring6restmvc.model.BeerDto;
import guru.springframework.spring6restmvc.repositories.BeerRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    void listBeers() {
        List<BeerDto> dtos = controller.listBeers();

        assertThat(dtos).hasSize(2413);
    }

    @Test
    @Transactional
    void emptyList() {
        repository.deleteAll();
        List<BeerDto> dtos = controller.listBeers();

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
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(beerMap)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.length()", is(1)))
                .andReturn();
        System.out.println(mvcResult.getResponse().getContentAsString());
    }

}
