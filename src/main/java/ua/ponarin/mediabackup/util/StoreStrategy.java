package ua.ponarin.mediabackup.util;

import java.nio.file.Path;
import java.util.function.BiFunction;

public interface StoreStrategy extends BiFunction<Path, Path, Path> {
    boolean isApplicable(Path path);
}
