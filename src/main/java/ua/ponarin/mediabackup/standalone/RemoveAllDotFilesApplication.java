package ua.ponarin.mediabackup.standalone;

import lombok.extern.log4j.Log4j2;
import me.tongfei.progressbar.ProgressBarBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Log4j2
public class RemoveAllDotFilesApplication {
    public static void main(String[] args) throws IOException {
        log.info("Application start");
        var basePath = Path.of("/Volumes/Backup/Backup/HuaweiP20/Camera");

        log.info("Collect files from root directory: {}", basePath);
        List<Path> dotFiles;
        try(Stream<Path> filesStream = Files.walk(basePath)) {
            dotFiles = filesStream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().startsWith("._"))
                    .collect(Collectors.toList());
        }
        log.info("Found {} dot files", dotFiles.size());

        if (dotFiles.size() > 0) {
            var progressBarBuilder = new ProgressBarBuilder()
                    .setTaskName("Deleting dot files")
                    .setInitialMax(dotFiles.size())
                    .setUpdateIntervalMillis(100)
                    .setMaxRenderedLength(140);

            try (var progressBar = progressBarBuilder.build()) {
                dotFiles.forEach(path -> {
                    progressBar.step();
                    progressBar.setExtraMessage(String.format("Current file: %-45s", path.getFileName().toString()));
                    try {
                        Files.deleteIfExists(path);
                    } catch (IOException e) {
                        throw new RuntimeException("Cannot delete file: ", e);
                    }
                });
            }
        }
        log.info("Application completed!");
    }
}
