package ua.ponarin.mediabackup.util;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.IOUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;

@UtilityClass
@Log4j2
public class AdbUtils {
    @SneakyThrows
    public List<String> executeAdbCommand(String adbCommand) {
        var process = Runtime.getRuntime().exec(adbCommand);
        var consoleOutput = IOUtils.readLines(process.getInputStream(), StandardCharsets.UTF_8);
        var exitCode = process.waitFor();
        if (exitCode != 0) {
            log.error("Failed to execute adb command: '{}'. Console output: {}", adbCommand, consoleOutput);
        }
        return consoleOutput;
    }
}
