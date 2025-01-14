package guru.springframework.spring6restmvc.services;

import guru.springframework.spring6restmvc.model.BeerCsvRecord;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

public interface BeerCsvService {
    List<BeerCsvRecord> convertCSV(File csvFile);
}
