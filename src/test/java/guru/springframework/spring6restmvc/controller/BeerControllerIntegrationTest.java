package guru.springframework.spring6restmvc.controller;

import guru.springframework.spring6restmvc.Spring6RestMvcApplication;
import guru.springframework.spring6restmvc.entities.Beer;
import guru.springframework.spring6restmvc.mappers.BeerMapper;
import guru.springframework.spring6restmvc.model.BeerDto;
import guru.springframework.spring6restmvc.repositories.BeerRepository;
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
class BeerControllerIntegrationTest {
    @Autowired
    BeerController controller;
    @Autowired
    BeerRepository repository;
    @Autowired
    BeerMapper mapper;

    @Test
    void testListBeers() {
        List<BeerDto> dtos = controller.listBeers();

        assertThat(dtos).hasSize(3);
    }

    @Test
    @Transactional
    void testEmptyList() {
        repository.deleteAll();
        List<BeerDto> dtos = controller.listBeers();

        assertThat(dtos).isEmpty();
    }

    @Test
    void testGetBeerById() {
        Beer beer = repository.findAll().get(0);

        BeerDto beerDto = controller.getBeerById(beer.getId());

        assertThat(beerDto).isNotNull();
    }

    @Test
    void testBeerIdNotFound() {
        assertThrows(NotFoundException.class, () -> {
            controller.getBeerById(UUID.randomUUID());
        });

    }

    @Test
    @Rollback
    @Transactional
    void testSaveNewBeer() {
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
    void testUpdateBeerById() {
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
    void testUpdateBeerByIdNotFound() {
        assertThrows(NotFoundException.class, () -> {
            controller.updateBeerById(UUID.randomUUID(), BeerDto.builder().build());
        });
    }

    @Test
    @Rollback
    @Transactional
    void testDeleteById() {
        Beer beer = repository.findAll().get(0);

        ResponseEntity responseEntity = controller.deleteBeerById(beer.getId());

        assertThat(repository.findById(beer.getId())).isEmpty();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(204));

    }

    @Test
    @Transactional
    @Rollback
    void testDeleteBeerByIdNotFound() {
        assertThrows(NotFoundException.class, () -> {
            controller.deleteBeerById(UUID.randomUUID());
        });
    }

}
