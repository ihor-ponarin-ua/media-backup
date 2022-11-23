package ua.ponarin.mediabackup.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import ua.ponarin.mediabackup.repository.OSFileRepository;
import ua.ponarin.mediabackup.repository.PortableDeviceFileRepository;
import ua.ponarin.mediabackup.util.YearBasedStoreStrategy;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutorService;

@Service
@RequiredArgsConstructor
public class MediaBackupService {
    private final PortableDeviceFileRepository portableDeviceFileRepository;
    private final OSFileRepository osFileRepository;
    private final ExecutorService executorService;
    private final YearBasedStoreStrategy yearBasedStoreStrategy;

    @SneakyThrows
    public void backupMediaFiles(Path basePathOnPortableDevice, Path basePathOnStorageDevice) {
        var portableDeviceFilesFuture = executorService
                .submit(() -> portableDeviceFileRepository.listFiles(basePathOnPortableDevice));
        var storageDeviceFilesFuture = executorService
                .submit(() -> osFileRepository.listFilesRecursively(basePathOnStorageDevice));

        var portableDeviceFiles = portableDeviceFilesFuture.get();
        var storageDeviceFiles = storageDeviceFilesFuture.get();
        executorService.shutdown();

        var portableDeviceFilesToBackup = retainNewFiles(portableDeviceFiles, storageDeviceFiles);
        portableDeviceFileRepository.backupFiles(portableDeviceFilesToBackup, basePathOnStorageDevice, yearBasedStoreStrategy);
    }

    private List<Path> retainNewFiles(List<Path> portableDeviceFiles, List<Path> storageDeviceFiles) {
        return null;
    }
}
