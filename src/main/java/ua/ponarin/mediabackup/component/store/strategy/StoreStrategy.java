package ua.ponarin.mediabackup.component.store.strategy;

import java.nio.file.Path;
import java.util.function.BiFunction;

public interface StoreStrategy extends BiFunction<Path, Path, Path> {
    boolean isApplicable(Path path);
}
