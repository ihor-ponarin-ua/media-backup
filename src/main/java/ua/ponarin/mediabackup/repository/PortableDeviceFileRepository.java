package ua.ponarin.mediabackup.repository;

import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import me.tongfei.progressbar.ProgressBarBuilder;
import org.springframework.stereotype.Repository;
import ua.ponarin.mediabackup.util.AdbUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@Repository
@Log4j2
public class PortableDeviceFileRepository {
    private static final String ADB_FIND_TEMPLATE = "adb shell find %s -type f -maxdepth 1";
    private static final String ADB_PULL_TEMPLATE = "adb pull %s %s";
    private static final String PROGRESS_BAR_TITLE = "MediaBackup Progress";
    private static final Integer PROGRESS_BAR_MAX_RENDERING_LENGTH = 130;
    private static final Integer PROGRESS_BAR_UPDATE_INTERVAL = 100;
    private static final String CURRENT_FILE_MESSAGE_TEMPLATE = "Current file: %s";

    @SneakyThrows
    public List<Path> listFiles(Path basePath) {
        log.info("Loading files on the portable device with the base path: {}", basePath);
        return AdbUtils.executeAdbCommand(String.format(ADB_FIND_TEMPLATE, basePath.toString())).stream()
                .map(Path::of)
                .collect(Collectors.toList());
    }

    public void backupFiles(List<Path> files, Path basePathOnStorageDevice, BiFunction<Path, Path, Path> storeStrategy) {
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
                progressBar.setExtraMessage(String.format(CURRENT_FILE_MESSAGE_TEMPLATE, pathToFileOnPortableDevice.getFileName().toString()));
                var storePath = storeStrategy.apply(pathToFileOnPortableDevice, basePathOnStorageDevice);
                try {
                    Files.createDirectories(storePath.getParent());
                } catch (IOException e) {
                    log.error("Unable to create a directory", e);
                    throw new RuntimeException(e);
                }
                AdbUtils.executeAdbCommand(String.format(ADB_PULL_TEMPLATE, pathToFileOnPortableDevice, storePath));
            });
        }

    }
}
