package ua.ponarin.mediabackup.standalone.store.strategy;

import ua.ponarin.mediabackup.component.store.strategy.FileNameMonthAndYearBasedStoreStrategy;
import ua.ponarin.mediabackup.component.store.strategy.StoreStrategy;

import java.nio.file.Path;

public class StoreStrategyChain implements StoreStrategy {
    private final FileNameMonthAndYearBasedStoreStrategy fileNameMonthAndYearBasedStoreStrategy =
            new FileNameMonthAndYearBasedStoreStrategy();
    private final FileAttributeMonthAndYearBasedStoreStrategy fileAttributeMonthAndYearBasedStoreStrategy =
            new FileAttributeMonthAndYearBasedStoreStrategy();

    @Override
    public boolean isApplicable(Path path) {
        return fileNameMonthAndYearBasedStoreStrategy.isApplicable(path) ||
                fileAttributeMonthAndYearBasedStoreStrategy.isApplicable(path);
    }

    @Override
    public Path apply(Path originalPath, Path storeBasedPath) {
        if (fileNameMonthAndYearBasedStoreStrategy.isApplicable(originalPath)) {
            return fileNameMonthAndYearBasedStoreStrategy.apply(originalPath, storeBasedPath);
        } else if (fileAttributeMonthAndYearBasedStoreStrategy.isApplicable(originalPath)) {
            return fileAttributeMonthAndYearBasedStoreStrategy.apply(originalPath, storeBasedPath);
        }
        throw new IllegalArgumentException("Can't extract year and month from original path: " + originalPath.toAbsolutePath());
    }
}
