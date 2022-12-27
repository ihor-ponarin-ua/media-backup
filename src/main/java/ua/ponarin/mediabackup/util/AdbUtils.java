package ua.ponarin.mediabackup.util;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.IOUtils;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

@UtilityClass
@Log4j2
public class AdbUtils {
    private static final String FIND_TEMPLATE = "adb shell find %s -type f -maxdepth %s";
    private static final String PULL_TEMPLATE = "adb pull -a %s %s"; // -a preserves file's timestamp

    public List<String> find(Path basePath, Integer depth) {
        return executeAdbCommand(String.format(FIND_TEMPLATE, basePath.toString(), depth));
    }

    public void pull(Path pathToFileOnPortableDevice, Path pathToFileOnStorageDevice) {
        executeAdbCommand(String.format(PULL_TEMPLATE, pathToFileOnPortableDevice.toString(), pathToFileOnStorageDevice.toString()));
    }

    @SneakyThrows
    private List<String> executeAdbCommand(String adbCommand) {
        var process = Runtime.getRuntime().exec(adbCommand);
        var consoleOutput = IOUtils.readLines(process.getInputStream(), StandardCharsets.UTF_8);
        var exitCode = process.waitFor();
        if (exitCode != 0) {
            log.error("Failed to execute adb command: '{}'. Console output: {}", adbCommand, consoleOutput);
        }
        return consoleOutput;
    }
}
