package ua.ponarin.mediabackup.repository;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;

public class PortableDeviceFileRepository {
    public List<Path> listFiles(Path basePath) {
        return null;
    }

    public void backupFiles(List<Path> files, Path basePathOnStorageDevice, Function<Path, Path> storeStrategy) {

    }
}
