package ua.ponarin.mediabackup;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import ua.ponarin.mediabackup.service.MediaBackupService;

import java.nio.file.Path;

@SpringBootApplication
@Log4j2
@RequiredArgsConstructor
public class MediaBackupApplication implements CommandLineRunner {
    private static final String BASE_PATH_ON_PORTABLE_DEVICE_CLI_OPTION = "base-path-on-portable-device";
    private static final String BASE_PATH_ON_STORAGE_DEVICE_CLI_OPTION = "base-path-on-storage-device";
    private static final Integer FIRST_LIST_ELEMENT = 0;
    private final ApplicationArguments applicationArguments;
    private final MediaBackupService mediaBackupService;

    public static void main(String[] args) {
        log.info("Application start");
        SpringApplication.run(MediaBackupApplication.class, args);
        log.info("Application stop");
    }

    @Override
    public void run(String... args) {
        if (validateCliArguments()) {
            mediaBackupService.backupMediaFiles(
                    Path.of(applicationArguments.getOptionValues(BASE_PATH_ON_PORTABLE_DEVICE_CLI_OPTION).get(FIRST_LIST_ELEMENT)),
                    Path.of(applicationArguments.getOptionValues(BASE_PATH_ON_STORAGE_DEVICE_CLI_OPTION).get(FIRST_LIST_ELEMENT)));
        } else {
            log.error("Required CLI arguments ({}, {}) are missing. Please use the following syntax: {}. Also avoid using whitespaces in the paths", BASE_PATH_ON_PORTABLE_DEVICE_CLI_OPTION, BASE_PATH_ON_STORAGE_DEVICE_CLI_OPTION, String.format("--%s=/storage/self/primary/DCIM/Camera --%s=/Users/ihor/Documents/mediaBackup", BASE_PATH_ON_PORTABLE_DEVICE_CLI_OPTION, BASE_PATH_ON_STORAGE_DEVICE_CLI_OPTION));
        }
    }

    private boolean validateCliArguments() {
        return applicationArguments.containsOption(BASE_PATH_ON_PORTABLE_DEVICE_CLI_OPTION) && applicationArguments.containsOption(BASE_PATH_ON_STORAGE_DEVICE_CLI_OPTION);
    }
}
