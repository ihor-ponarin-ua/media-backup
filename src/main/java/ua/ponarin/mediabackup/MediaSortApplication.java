package ua.ponarin.mediabackup;

import me.tongfei.progressbar.ProgressBarBuilder;
import ua.ponarin.mediabackup.util.YearBasedStoreStrategy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MediaSortApplication {
    public static void main(String[] args) throws IOException {
        var basePath = Path.of("/Volumes/Backup/Backup/Huawei P30/Camera");
        var yearBaseStoreStrategy = new YearBasedStoreStrategy();

        List<Path> paths;
        try (Stream<Path> stream = Files.walk(basePath)) {
            paths = stream.filter(Files::isRegularFile).collect(Collectors.toList());
        }

        List<ExceptionDetails> exceptionDetails = new ArrayList<>();

        var progressBarBuilder = new ProgressBarBuilder()
                .setTaskName("Sorting")
                .setInitialMax(paths.size())
                .setUpdateIntervalMillis(100)
                .setMaxRenderedLength(130);

        try (var progressBar = progressBarBuilder.build()) {
            paths.forEach(path -> {
                progressBar.step();
                progressBar.setExtraMessage("Current file: " + path.getFileName());
                try {
                    var newPath = yearBaseStoreStrategy.apply(path, basePath);
                    Files.createDirectories(newPath.getParent());
                    Files.move(path, newPath);
                } catch (Exception e) {
                    exceptionDetails.add(new ExceptionDetails().setPath(path).setException(e));
                }
            });
        }

        System.out.println("Number of exceptions: " + exceptionDetails.size());
        if (exceptionDetails.size() > 0) {
            exceptionDetails.forEach(System.out::println);
        }
    }

    private static class ExceptionDetails {
        private Path path;
        private Exception exception;

        public ExceptionDetails setPath(Path path) {
            this.path = path;
            return this;
        }

        public ExceptionDetails setException(Exception exception) {
            this.exception = exception;
            return this;
        }

        @Override
        public String toString() {
            return "ExceptionDetails{" +
                    "path=" + path +
                    ", exception=" + exception +
                    '}';
        }
    }
}
