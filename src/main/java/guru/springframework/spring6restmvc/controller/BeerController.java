package guru.springframework.spring6restmvc.controller;


import guru.springframework.spring6restmvc.model.BeerDto;
import guru.springframework.spring6restmvc.model.BeerStyle;
import guru.springframework.spring6restmvc.services.BeerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(BeerController.BEER_PATH)
public class BeerController {
    public static final String BEER_PATH = "/api/v1/beer";
    public static final String BEER_PATH_ID = "/{id}";
    private final BeerService beerService;

    @GetMapping
    public List<BeerDto> listBeers(@RequestParam(required = false) String beerName,
                                   @RequestParam(required = false) BeerStyle beerStyle,
                                   @RequestParam(required = false) Boolean showInventory) {

        log.debug("List beers - in controller");

        return beerService.listBeers(beerName, beerStyle, showInventory);
    }

    @GetMapping(BEER_PATH_ID)
    public BeerDto getBeerById(@PathVariable UUID id) {

        log.debug("Get BeerDto by Id - in controller");

        return beerService.getBeerById(id).orElseThrow(NotFoundException::new);
    }

    @PostMapping
    public ResponseEntity saveBeer(@Validated @RequestBody BeerDto beer) {

        BeerDto savedBeer = beerService.saveBeer(beer);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", "api/v1/beer/" + savedBeer.getId().toString());

        return new ResponseEntity(headers, HttpStatus.CREATED);
    }

    @PutMapping(BEER_PATH_ID)
    public ResponseEntity updateBeerById(@PathVariable UUID id,
                                         @Validated @RequestBody BeerDto beer) {

        if (beerService.updateBeerById(id, beer).isEmpty()) {
            throw new NotFoundException();
        }


        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping(BEER_PATH_ID)
    public ResponseEntity deleteBeerById(@PathVariable UUID id) {

        if (!beerService.deleteBeerById(id)) {
            throw new NotFoundException();
        }

        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @PatchMapping(BEER_PATH_ID)
    public ResponseEntity updateBeerPatchId(@PathVariable UUID id,
                                            @RequestBody BeerDto beer) {

        if (beerService.patchBeerById(id, beer).isEmpty()) {
            throw new NotFoundException();
        }


        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }
}
