package ua.ponarin.mediabackup.standalone;

import lombok.extern.log4j.Log4j2;
import me.tongfei.progressbar.ProgressBarBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Log4j2
public class DuplicateRemovalApplication {
    public static void main(String[] args) throws IOException {
        var basePath = Path.of("/Volumes/Backup/Backup/HuaweiP30/Camera");

        var duplicateNumberPatternString = "(.*)(\\(\\d\\))(.*)";
        var duplicateNumberPattern = Pattern.compile(duplicateNumberPatternString);
        Predicate<Path> doesntStartFromDotSymbolPredicate = path ->
                !path.getFileName().toString().startsWith(".");
        Predicate<Path> pathWithDuplicateNumberInFileNamePredicate = path ->
                path.getFileName().toString().matches(duplicateNumberPatternString);

        log.info("Application start");
        List<Path> regularFiles;
        try (Stream<Path> fileStream = Files.walk(basePath)) {
            regularFiles = fileStream
                    .filter(Files::isRegularFile)
                    .filter(doesntStartFromDotSymbolPredicate)
                    .collect(Collectors.toList());
        }
        log.info("Found {} regular files", regularFiles.size());

        var potentialDuplicateFiles = regularFiles.stream()
                .filter(pathWithDuplicateNumberInFileNamePredicate)
                .collect(Collectors.toList());
        log.info("Found {} potential duplicate files", potentialDuplicateFiles.size());

        var duplicateFiles = potentialDuplicateFiles.parallelStream()
                .map(Path::toString)
                .map(duplicateNumberPattern::matcher)
                .filter(Matcher::find)
                .filter(matcher -> regularFiles.contains(Path.of(matcher.group(1) + matcher.group(3))))
                .map(matcher -> Path.of(matcher.group(1) + matcher.group(2) + matcher.group(3)))
                .collect(Collectors.toList());
        log.info("Found {} duplicate files", duplicateFiles.size());

        var progressBarBuilder = new ProgressBarBuilder()
                .setTaskName("Deleting")
                .setInitialMax(duplicateFiles.size())
                .setUpdateIntervalMillis(100)
                .setMaxRenderedLength(135);

        try (var progressBar = progressBarBuilder.build()) {
            duplicateFiles.forEach(path -> {
                progressBar.step();
                progressBar.setExtraMessage(String.format("Current file: %-45s", path.getFileName().toString()));
                try {
                    Files.deleteIfExists(path);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        log.info("Application completed!");
    }
}