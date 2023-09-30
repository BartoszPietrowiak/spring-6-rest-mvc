package guru.springframework.spring6restmvc.services;

import guru.springframework.spring6restmvc.model.Beer;

import java.util.List;
import java.util.UUID;

public interface BeerService {
    Beer getBeerById(UUID id);

    List<Beer> listBeers();

    Beer saveBeer(Beer beer);

    void updateBeerById(UUID id, Beer beer);

    void deleteBeerById(UUID id);

    void patchBeerById(UUID id, Beer beer);
}
