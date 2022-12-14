package ua.ponarin.mediabackup.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import me.tongfei.progressbar.ProgressBarBuilder;
import org.springframework.stereotype.Service;
import ua.ponarin.mediabackup.repository.StorageDeviceFileRepository;
import ua.ponarin.mediabackup.repository.PortableDeviceFileRepository;
import ua.ponarin.mediabackup.util.AdbUtils;
import ua.ponarin.mediabackup.component.store.strategy.StoreStrategy;
import ua.ponarin.mediabackup.component.store.strategy.FileNameMonthAndYearBasedStoreStrategy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class MediaBackupService {
    private static final String PROGRESS_BAR_TITLE = "MediaBackup Progress";
    private static final Integer PROGRESS_BAR_MAX_RENDERING_LENGTH = 140;
    private static final Integer PROGRESS_BAR_UPDATE_INTERVAL = 100;
    private static final String CURRENT_FILE_MESSAGE_TEMPLATE = "Current file: %-45s";
    private static final Path STORE_STRATEGY_REJECTED_FILES_PATH = Path.of("storeStrategyRejectedFiles.txt");
    private final PortableDeviceFileRepository portableDeviceFileRepository;
    private final StorageDeviceFileRepository storageDeviceFileRepository;
    private final ExecutorService executorService;
    private final FileNameMonthAndYearBasedStoreStrategy fileNameYearBasedStoreStrategy;

    @SneakyThrows
    public void backupMediaFiles(Path basePathOnPortableDevice, Integer portableDeviceFileLookupDepth, Path basePathOnStorageDevice) {
        log.info("Start to backup media files. Base path on the portable device: '{}'. Base path on the storage device: '{}'",
                basePathOnPortableDevice, basePathOnStorageDevice);

        var portableDeviceFilesToBackup = findNewFilesToBackup(basePathOnPortableDevice, portableDeviceFileLookupDepth, basePathOnStorageDevice);

        log.info("Found {} new files to backup", portableDeviceFilesToBackup.size());
        var groupedPortableDeviceFiles = portableDeviceFilesToBackup.parallelStream()
                .collect(Collectors.groupingBy(fileNameYearBasedStoreStrategy::isApplicable));
        var storeStrategyAcceptedPortableDeviceFiles = groupedPortableDeviceFiles.getOrDefault(true, List.of());
        var storeStrategyRejectedPortableDeviceFiles = groupedPortableDeviceFiles.getOrDefault(false, List.of());

        if (storeStrategyAcceptedPortableDeviceFiles.size() > 0) {
            log.info("Found {} files that successfully passed the sore strategy filter", storeStrategyAcceptedPortableDeviceFiles.size());
            backupFiles(storeStrategyAcceptedPortableDeviceFiles, basePathOnStorageDevice, fileNameYearBasedStoreStrategy);
            log.info("Done!");
        } else {
            log.info("There is nothing to backup. Done!");
        }

        if (storeStrategyRejectedPortableDeviceFiles.size() > 0) {
            log.warn("Found {} files that failed to pass the store strategy filter. The list of the files will be stored in the file '{}'",
                    storeStrategyRejectedPortableDeviceFiles.size(), STORE_STRATEGY_REJECTED_FILES_PATH.toAbsolutePath());
            saveRejectedByStoreStrategyFiles(storeStrategyRejectedPortableDeviceFiles);
        }
    }

    @SneakyThrows
    private List<Path> findNewFilesToBackup(Path basePathOnPortableDevice, Integer portableDeviceFileLookupDepth, Path basePathOnStorageDevice) {
        var portableDeviceFilesFuture = executorService
                .submit(() -> portableDeviceFileRepository.listFiles(basePathOnPortableDevice, portableDeviceFileLookupDepth));
        var storageDeviceFilesFuture = executorService
                .submit(() -> storageDeviceFileRepository.listFilesRecursively(basePathOnStorageDevice));

        var portableDeviceFiles = portableDeviceFilesFuture.get();
        log.info("Found {} files on the portable device", portableDeviceFiles.size());
        var storageDeviceFiles = storageDeviceFilesFuture.get();
        log.info("Found {} files on the storage device", storageDeviceFiles.size());
        executorService.shutdown();

        return retainNewFiles(portableDeviceFiles, storageDeviceFiles);
    }

    private List<Path> retainNewFiles(List<Path> portableDeviceFiles, List<Path> storageDeviceFiles) {
        var storageDeviceFileNames = storageDeviceFiles.parallelStream()
                .map(Path::getFileName)
                .collect(Collectors.toList());
        return portableDeviceFiles.stream()
                .filter(portableDeviceFile -> !storageDeviceFileNames.contains(portableDeviceFile.getFileName()))
                .collect(Collectors.toList());
    }

    @SneakyThrows
    public void backupFiles(List<Path> files, Path basePathOnStorageDevice, StoreStrategy storeStrategy) {
        log.info("Start to backup media files. It is expected to backup {} files with the base path on the storage device: '{}' and using '{}' store strategy",
                files.size(), basePathOnStorageDevice, storeStrategy);
        var progressBarBuilder = new ProgressBarBuilder()
                .setTaskName(PROGRESS_BAR_TITLE)
                .setInitialMax(files.size())
                .setUpdateIntervalMillis(PROGRESS_BAR_UPDATE_INTERVAL)
                .setMaxRenderedLength(PROGRESS_BAR_MAX_RENDERING_LENGTH);
        try (var progressBar = progressBarBuilder.build()) {
            files.forEach(pathToFileOnPortableDevice -> {
                progressBar.step();
                progressBar.setExtraMessage(String.format(
                        CURRENT_FILE_MESSAGE_TEMPLATE, pathToFileOnPortableDevice.getFileName().toString()));
                try {
                    var storePath = storeStrategy.apply(pathToFileOnPortableDevice, basePathOnStorageDevice);
                    Files.createDirectories(storePath.getParent());
                    AdbUtils.pull(pathToFileOnPortableDevice, storePath);
                } catch (IOException e) {
                    log.error("Unable to create a directory", e);
                    throw new RuntimeException(e);
                }
            });
        }
    }

    @SneakyThrows
    private void saveRejectedByStoreStrategyFiles(List<Path> files) {
        var filesAsString = files.parallelStream()
                .map(Path::toAbsolutePath)
                .map(Path::toString)
                .collect(Collectors.joining(System.lineSeparator()));
        Files.write(STORE_STRATEGY_REJECTED_FILES_PATH, filesAsString.getBytes());
    }
}
