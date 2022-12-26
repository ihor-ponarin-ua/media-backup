package ua.ponarin.mediabackup.standalone;

import lombok.extern.log4j.Log4j2;
import me.tongfei.progressbar.ProgressBarBuilder;
import org.apache.commons.lang3.StringUtils;
import ua.ponarin.mediabackup.component.store.strategy.FileNameYearBasedStoreStrategy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Log4j2
public class MediaSortApplication {
    public static void main(String[] args) throws IOException {
        log.info("Application start");
        var basePath = Path.of("/Volumes/Backup/Backup/HuaweiP20/Camera");
        var storeStrategyRejectedFiles = Path.of("storeStrategyRejectedFiles.txt");
        var yearBaseStoreStrategy = new FileNameYearBasedStoreStrategy();

        log.info("Loading files from external drive...");
        List<Path> paths;
        try (Stream<Path> stream = Files.walk(basePath, 1)) {
            paths = stream.filter(Files::isRegularFile).collect(Collectors.toList());
        }
        log.info("Found {} files.", paths.size());
        var storageSupportBasedGroupedFiles = paths.parallelStream()
                .collect(Collectors.groupingBy(yearBaseStoreStrategy::isApplicable));
        var storeStrategyAcceptedPortableDeviceFiles = storageSupportBasedGroupedFiles.get(true);
        var storeStrategyRejectedPortableDeviceFiles = storageSupportBasedGroupedFiles.get(false);

        if (storeStrategyRejectedPortableDeviceFiles != null) {
            log.warn("Found {} files that failed to pass the store strategy filter. The list of the files will be stored in the file '{}'",
                    storeStrategyRejectedPortableDeviceFiles.size(), storeStrategyRejectedFiles.toAbsolutePath());
            var filesAsString = storeStrategyRejectedPortableDeviceFiles.parallelStream()
                    .map(Path::toAbsolutePath)
                    .map(Path::toString)
                    .collect(Collectors.joining(System.lineSeparator()));
            Files.write(storeStrategyRejectedFiles, filesAsString.getBytes());
        }

        if (storeStrategyAcceptedPortableDeviceFiles != null) {
            log.info("Fount {} files that successfully passed the sore strategy filter", storeStrategyAcceptedPortableDeviceFiles.size());
            var progressBarBuilder = new ProgressBarBuilder()
                    .setTaskName("Sorting")
                    .setInitialMax(paths.size())
                    .setUpdateIntervalMillis(100)
                    .setMaxRenderedLength(130);

            try (var progressBar = progressBarBuilder.build()) {
                storeStrategyAcceptedPortableDeviceFiles.forEach(path -> {
                    progressBar.step();
                    progressBar.setExtraMessage("Current file: " + StringUtils.abbreviate(path.getFileName().toString(), 23));
                    try {
                        var newPath = yearBaseStoreStrategy.apply(path, basePath);
                        Files.createDirectories(newPath.getParent());
                        Files.move(path, newPath);
                    } catch (IOException e) {
                        throw new RuntimeException("Cannot create a directory or move file: ", e);
                    }
                });
            }
        }
        log.info("Application completed!");
    }
}
