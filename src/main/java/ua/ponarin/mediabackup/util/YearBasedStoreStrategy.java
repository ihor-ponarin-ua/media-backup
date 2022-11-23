package ua.ponarin.mediabackup.util;

import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.function.Function;

@Component
public class YearBasedStoreStrategy implements Function<Path, Path> {
    @Override
    public Path apply(Path path) {
        return null;
    }
}
