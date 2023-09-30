package guru.springframework.spring6restmvc.controller;


import guru.springframework.spring6restmvc.model.Beer;
import guru.springframework.spring6restmvc.services.BeerService;
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
@RequestMapping("/api/v1/beer")
public class BeerController {
    private final BeerService beerService;

    @GetMapping
    public List<Beer> listBeers() {

        log.debug("List beers - in controller");

        return beerService.listBeers();
    }

    @GetMapping("/{id}")
    public Beer getBeerById(@PathVariable UUID id) {

        log.debug("Get Beer by Id - in controller");

        return beerService.getBeerById(id);
    }

    @PostMapping
    public ResponseEntity saveBeer(@RequestBody Beer beer) {

        Beer savedBeer = beerService.saveBeer(beer);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", "api/v1/beer/" + savedBeer.getId().toString());

        return new ResponseEntity(headers, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity updateBeerById(@PathVariable UUID id,
                                     @RequestBody Beer beer) {

        beerService.updateBeerById(id, beer);


        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity deleteBeerById(@PathVariable UUID id) {

        beerService.deleteBeerById(id);

        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @PatchMapping("/{id}")
    public ResponseEntity updateBeerPatchId(@PathVariable UUID id,
                                     @RequestBody Beer beer) {

        beerService.patchBeerById(id, beer);


        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }
}
