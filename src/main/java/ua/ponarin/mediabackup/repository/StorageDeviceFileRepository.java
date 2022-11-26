package ua.ponarin.mediabackup.repository;

import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Repository;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Repository
@Log4j2
public class StorageDeviceFileRepository {
    @SneakyThrows
    public List<Path> listFilesRecursively(Path basePath) {
        log.info("Loading files on the storage device with the base path: {}", basePath);
        try (Stream<Path> stream = Files.walk(basePath)) {
            return stream.filter(Files::isRegularFile).collect(Collectors.toList());
        }
    }
}
