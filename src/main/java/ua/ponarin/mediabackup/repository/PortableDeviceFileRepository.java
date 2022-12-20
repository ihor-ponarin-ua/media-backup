package ua.ponarin.mediabackup.repository;

import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Repository;
import ua.ponarin.mediabackup.util.AdbUtils;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@Log4j2
public class PortableDeviceFileRepository {
    private static final String ADB_FIND_TEMPLATE = "adb shell find %s -type f -maxdepth 1";

    @SneakyThrows
    public List<Path> listFiles(Path basePath) {
        log.info("Loading files on the portable device with the base path: {}", basePath);
        return AdbUtils.executeAdbCommand(String.format(ADB_FIND_TEMPLATE, basePath.toString())).stream()
                .map(Path::of)
                .collect(Collectors.toList());
    }


}
