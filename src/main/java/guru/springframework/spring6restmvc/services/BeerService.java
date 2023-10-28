package guru.springframework.spring6restmvc.services;

import guru.springframework.spring6restmvc.model.BeerDto;
import guru.springframework.spring6restmvc.model.BeerStyle;
import org.springframework.data.domain.Page;

import java.util.Optional;
import java.util.UUID;

public interface BeerService {
    Optional<BeerDto> getBeerById(UUID id);

    Page<BeerDto> listBeers(String beerName, BeerStyle beerStyle, Boolean showInventory, Integer pageNumber, Integer pageSize);

    BeerDto saveBeer(BeerDto beer);

    Optional<BeerDto> updateBeerById(UUID id, BeerDto beer);

    boolean deleteBeerById(UUID id);

    Optional<BeerDto> patchBeerById(UUID id, BeerDto beer);
}
