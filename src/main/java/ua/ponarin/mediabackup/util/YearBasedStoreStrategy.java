package ua.ponarin.mediabackup.util;

import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.function.BiFunction;

@Component
public class YearBasedStoreStrategy implements BiFunction<Path, Path, Path> {
    private static final String DELIMITER = "_";
    private static final Integer DATE_STATEMENT_POSITION = 1;
    private static final DateTimeFormatter DEFAULT_DATE_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;

    @Override
    public Path apply(Path pathToFileOnPortableDevice, Path basePathOnStorageDevice) {
        var dateAsString = pathToFileOnPortableDevice.getFileName().toString()
                .split(DELIMITER)[DATE_STATEMENT_POSITION];
        var localDate = LocalDate.parse(dateAsString, DEFAULT_DATE_FORMATTER);
        var year = Integer.valueOf(localDate.getYear());
        return Path.of(basePathOnStorageDevice.toString(), year.toString(), pathToFileOnPortableDevice.getFileName().toString());
    }

    @Override
    public String toString() {
        return "YearBasedStorageStrategy";
    }
}
