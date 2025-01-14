package guru.springframework.spring6restmvc.bootstrap;

import guru.springframework.spring6restmvc.entities.Beer;
import guru.springframework.spring6restmvc.entities.Customer;
import guru.springframework.spring6restmvc.model.BeerCsvRecord;
import guru.springframework.spring6restmvc.model.BeerStyle;
import guru.springframework.spring6restmvc.repositories.BeerRepository;
import guru.springframework.spring6restmvc.repositories.CustomerRepository;
import guru.springframework.spring6restmvc.services.BeerCsvService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class BootstrapData implements CommandLineRunner {

    private final BeerRepository beerRepository;
    private final CustomerRepository customerRepository;
    private final BeerCsvService beerCsvService;

    @Override
    @Transactional
    public void run(String... args) throws FileNotFoundException {
        log.info("Initialize data");
        loadBeerData();
        loadCsvData();
        loadCustomerData();
    }

    private void loadCsvData() throws FileNotFoundException {
        if (beerRepository.count() < 10) {
            File csvFile = ResourceUtils.getFile("classpath:csvdata/beers.csv");

            List<BeerCsvRecord> beerCsvRecords = beerCsvService.convertCSV(csvFile);

            beerCsvRecords.forEach(beerCsvRecord -> {
                BeerStyle beerStyle = switch (beerCsvRecord.getStyle()) {
                    case "American Pale Lager" -> BeerStyle.LAGER;
                    case "American Pale Ale (APA)", "American Black Ale", "Belgian Dark Ale", "American Blonde Ale" ->
                            BeerStyle.ALE;
                    case "American IPA", "American Double / Imperial IPA", "Belgian IPA" -> BeerStyle.IPA;
                    case "American Porter" -> BeerStyle.PORTER;
                    case "Oatmeal Stout", "American Stout" -> BeerStyle.STOUT;
                    case "Saison / Farmhouse Ale" -> BeerStyle.SAISON;
                    case "Fruit / Vegetable Beer", "Winter Warmer", "Berliner Weissbier" -> BeerStyle.WHEAT;
                    case "English Pale Ale" -> BeerStyle.PALE_ALE;
                    default -> BeerStyle.PILSNER;
                };
                beerRepository.save(Beer.builder()
                        .beerStyle(beerStyle)
                        .beerName(StringUtils.abbreviate(beerCsvRecord.getBeer(), 50))
                        .price(BigDecimal.TEN)
                        .upc(beerCsvRecord.getRow().toString())
                        .quantityOnHand(beerCsvRecord.getCountX())
                        .build());
            });
        }
    }

    private void loadBeerData() {
        if (beerRepository.count() == 0) {
            Beer beer1 = Beer.builder()
                    .beerName("Tyskie")
                    .beerStyle(BeerStyle.LAGER)
                    .upc("123456")
                    .price(new BigDecimal("12.99"))
                    .quantityOnHand(122)
                    .createdDate(LocalDateTime.now())
                    .updateDate(LocalDateTime.now())
                    .build();

            Beer beer2 = Beer.builder()
                    .beerName("Zubr")
                    .beerStyle(BeerStyle.PALE_ALE)
                    .upc("123456222")
                    .price(new BigDecimal("11.99"))
                    .quantityOnHand(392)
                    .createdDate(LocalDateTime.now())
                    .updateDate(LocalDateTime.now())
                    .build();

            Beer beer3 = Beer.builder()
                    .beerName("Lomza")
                    .beerStyle(BeerStyle.PALE_ALE)
                    .upc("123456")
                    .price(new BigDecimal("13.99"))
                    .quantityOnHand(144)
                    .createdDate(LocalDateTime.now())
                    .updateDate(LocalDateTime.now())
                    .build();

            beerRepository.save(beer1);
            beerRepository.save(beer2);
            beerRepository.save(beer3);
        }
    }

    private void loadCustomerData() {
        if (customerRepository.count() == 0) {
            Customer customer1 = Customer.builder()
                    .name("CustomerDto 1")
                    .createdDate(LocalDateTime.now())
                    .updateDate(LocalDateTime.now())
                    .build();
            Customer customer2 = Customer.builder()
                    .name("CustomerDto 2")
                    .createdDate(LocalDateTime.now())
                    .updateDate(LocalDateTime.now())
                    .build();
            Customer customer3 = Customer.builder()
                    .name("CustomerDto 3")
                    .createdDate(LocalDateTime.now())
                    .updateDate(LocalDateTime.now())
                    .build();
            customerRepository.save(customer1);
            customerRepository.save(customer2);
            customerRepository.save(customer3);
        }
    }
}
