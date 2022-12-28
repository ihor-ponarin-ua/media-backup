package ua.ponarin.mediabackup.standalone.store.strategy;

import lombok.SneakyThrows;
import ua.ponarin.mediabackup.component.store.strategy.StoreStrategy;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class FileAttributeMonthAndYearBasedStoreStrategy implements StoreStrategy {
    private static final ZonedDateTime MINIMAL_DATE_TIME = ZonedDateTime.of(1990, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault());
    @Override
    public boolean isApplicable(Path path) {
        var dateTime = getFileModifiedDateTime(path);
        return dateTime.isAfter(MINIMAL_DATE_TIME);
    }

    @Override
    public Path apply(Path originalPathToFile, Path basePathOnStorageDevice) {
        var dateTime = getFileModifiedDateTime(originalPathToFile);
        return Path.of(
                basePathOnStorageDevice.toString(),
                String.valueOf(dateTime.getYear()),
                String.valueOf(dateTime.getMonthValue()),
                originalPathToFile.getFileName().toString()
        );
    }

    @SneakyThrows
    private ZonedDateTime getFileModifiedDateTime(Path path) {
        var basicFileAttributes = Files.readAttributes(path, BasicFileAttributes.class);
        return basicFileAttributes.lastModifiedTime().toInstant().atZone(ZoneId.systemDefault());
    }
}
