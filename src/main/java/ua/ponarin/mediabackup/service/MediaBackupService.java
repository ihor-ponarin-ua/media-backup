package ua.ponarin.mediabackup.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import ua.ponarin.mediabackup.repository.StorageDeviceFileRepository;
import ua.ponarin.mediabackup.repository.PortableDeviceFileRepository;
import ua.ponarin.mediabackup.util.YearBasedStoreStrategy;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class MediaBackupService {
    private final PortableDeviceFileRepository portableDeviceFileRepository;
    private final StorageDeviceFileRepository storageDeviceFileRepository;
    private final ExecutorService executorService;
    private final YearBasedStoreStrategy yearBasedStoreStrategy;

    @SneakyThrows
    public void backupMediaFiles(Path basePathOnPortableDevice, Path basePathOnStorageDevice) {
        log.info("Start to backup media files. Base path on the portable device: '{}'. Base path on the storage device: '{}'",
                basePathOnPortableDevice, basePathOnStorageDevice);
        var portableDeviceFilesFuture = executorService
                .submit(() -> portableDeviceFileRepository.listFiles(basePathOnPortableDevice));
        var storageDeviceFilesFuture = executorService
                .submit(() -> storageDeviceFileRepository.listFilesRecursively(basePathOnStorageDevice));

        var portableDeviceFiles = portableDeviceFilesFuture.get();
        log.info("Found {} files on the portable device", portableDeviceFiles.size());
        var storageDeviceFiles = storageDeviceFilesFuture.get();
        log.info("Found {} files on the storage device", storageDeviceFiles.size());
        executorService.shutdown();

        var portableDeviceFilesToBackup = retainNewFiles(portableDeviceFiles, storageDeviceFiles);
        log.info("Found {} new files to backup", portableDeviceFilesToBackup.size());
        if (portableDeviceFilesToBackup.size() > 0) {
            portableDeviceFileRepository.backupFiles(portableDeviceFilesToBackup, basePathOnStorageDevice, yearBasedStoreStrategy);
        } else {
            log.info("There is nothing to backup. Done!");
        }
    }

    private List<Path> retainNewFiles(List<Path> portableDeviceFiles, List<Path> storageDeviceFiles) {
        var storageDeviceFileNames = storageDeviceFiles.parallelStream()
                .map(Path::getFileName)
                .collect(Collectors.toList());
        return portableDeviceFiles.stream()
                .filter(portableDeviceFile -> !storageDeviceFileNames.contains(portableDeviceFile.getFileName()))
                .collect(Collectors.toList());
    }
}
