package guru.springframework.spring6restmvc.services;

import guru.springframework.spring6restmvc.model.BeerDto;
import guru.springframework.spring6restmvc.model.BeerStyle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class BeerServiceImpl implements BeerService {

    private Map<UUID, BeerDto> beerMap;

    public BeerServiceImpl() {
        this.beerMap = new HashMap<>();

        BeerDto beer1 = BeerDto.builder()
                .id(UUID.randomUUID())
                .version(1)
                .beerName("Tyskie")
                .beerStyle(BeerStyle.LAGER)
                .upc("123456")
                .price(new BigDecimal("12.99"))
                .quantityOnHand(122)
                .createdDate(LocalDateTime.now())
                .updateDate(LocalDateTime.now())
                .build();

        BeerDto beer2 = BeerDto.builder()
                .id(UUID.randomUUID())
                .version(1)
                .beerName("Zubr")
                .beerStyle(BeerStyle.PALE_ALE)
                .upc("123456222")
                .price(new BigDecimal("11.99"))
                .quantityOnHand(392)
                .createdDate(LocalDateTime.now())
                .updateDate(LocalDateTime.now())
                .build();

        BeerDto beer3 = BeerDto.builder()
                .id(UUID.randomUUID())
                .version(1)
                .beerName("Lomza")
                .beerStyle(BeerStyle.PALE_ALE)
                .upc("123456")
                .price(new BigDecimal("13.99"))
                .quantityOnHand(144)
                .createdDate(LocalDateTime.now())
                .updateDate(LocalDateTime.now())
                .build();

        beerMap.put(beer1.getId(), beer1);
        beerMap.put(beer2.getId(), beer2);
        beerMap.put(beer3.getId(), beer3);
    }

    @Override
    public Optional<BeerDto> getBeerById(UUID id) {

        log.debug("Get BeerDto by Id - in service. Id: " + id.toString());

        return Optional.of(beerMap.get(id));
    }

    @Override
    public Page<BeerDto> listBeers(String beerName, BeerStyle beerStyle, Boolean showInventory, Integer pageNumber, Integer pageSize) {
        return new PageImpl<>( new ArrayList<>(this.beerMap
                .values()));
    }

    @Override
    public BeerDto saveBeer(BeerDto beer) {
        beer.setId(UUID.randomUUID());
        beer.setCreatedDate(LocalDateTime.now());
        beer.setUpdateDate(LocalDateTime.now());

        beerMap.put(beer.getId(), beer);
        return beer;
    }

    @Override
    public Optional<BeerDto> updateBeerById(UUID id, BeerDto beer) {
        BeerDto existingBeer = beerMap.get(id);

        existingBeer.setBeerName(beer.getBeerName());
        existingBeer.setPrice(beer.getPrice());
        existingBeer.setUpc(beer.getUpc());
        existingBeer.setQuantityOnHand(beer.getQuantityOnHand());
        existingBeer.setUpdateDate(LocalDateTime.now());

        beerMap.put(id, existingBeer);
        return Optional.of(existingBeer);
    }

    @Override
    public boolean deleteBeerById(UUID id) {
        beerMap.remove(id);

        return true;
    }

    @Override
    public Optional<BeerDto> patchBeerById(UUID id, BeerDto beer) {
        BeerDto existedBeer = beerMap.get(id);

        if (StringUtils.hasText(beer.getBeerName())) {
            existedBeer.setBeerName(beer.getBeerName());
        }
        if (beer.getBeerStyle() != null) {
            existedBeer.setBeerStyle(beer.getBeerStyle());
        }
        if (beer.getPrice() != null) {
            existedBeer.setPrice(beer.getPrice());
        }
        if (beer.getQuantityOnHand() != null) {
            existedBeer.setQuantityOnHand(beer.getQuantityOnHand());
        }
        if (StringUtils.hasText(beer.getUpc())) {
            existedBeer.setUpc(beer.getUpc());
        }

        beerMap.put(id, existedBeer);

        return Optional.of(existedBeer);
    }

}
