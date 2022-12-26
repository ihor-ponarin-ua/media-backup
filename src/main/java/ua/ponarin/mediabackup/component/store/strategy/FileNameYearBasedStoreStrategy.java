package ua.ponarin.mediabackup.component.store.strategy;

import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

@Component
public class FileNameYearBasedStoreStrategy implements StoreStrategy {
    private static final Pattern DATE_STATEMENT_PATTERN = Pattern.compile(".*(\\d{8})_\\d{6}");
    private static final Integer DATE_STATEMENT_GROUP_NUMBER = 1;
    private static final DateTimeFormatter DEFAULT_DATE_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;

    @Override
    public Path apply(Path pathToFileOnPortableDevice, Path basePathOnStorageDevice) {
        var fileName = pathToFileOnPortableDevice.getFileName().toString();
        var matcher = DATE_STATEMENT_PATTERN.matcher(fileName);
        if (matcher.find()) {
            var dateAsString = matcher.group(DATE_STATEMENT_GROUP_NUMBER);
            var localDate = LocalDate.parse(dateAsString, DEFAULT_DATE_FORMATTER);
            var year = Integer.valueOf(localDate.getYear());
            return Path.of(basePathOnStorageDevice.toString(), year.toString(), pathToFileOnPortableDevice.getFileName().toString());
        } else {
            throw new IllegalArgumentException(String.format("The filename: '%s' in the path: '%s' doesn't match the pattern",
                    pathToFileOnPortableDevice.getFileName(),
                    pathToFileOnPortableDevice));
        }
    }

    @Override
    public boolean isApplicable(Path path) {
        return DATE_STATEMENT_PATTERN.matcher(path.getFileName().toString()).find();
    }

    @Override
    public String toString() {
        return "FileNameYearBasedStoreStrategy";
    }
}
/*
 * File name potential options:
 * IMG_20191117_100647.jpg
 * IMG_20191117_100647(1).jpg
 * IMG_20190809_192706_1.jpg
 * VID_20190529_185229.mp4
 * SL_MO_VID_20190908_153722.mp4
 * ._IMG_20190801_120404.jpg
 * ._VID_20190427_111659.mp4
 * ._SL_MO_VID_20190908_153821(1).mp4
 * */