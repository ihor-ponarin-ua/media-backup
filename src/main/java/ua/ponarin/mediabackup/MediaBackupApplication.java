package ua.ponarin.mediabackup;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.IOUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;

@SpringBootApplication
@Log4j2
public class MediaBackupApplication implements CommandLineRunner {

    public static void main(String[] args) {
        log.info("Application start");
        SpringApplication.run(MediaBackupApplication.class, args);
        log.info("Application stop");
    }

    @Override
    public void run(String... args) throws Exception {
        var processBuilder = new ProcessBuilder();
        processBuilder.directory(new File("/Users/ihor/env/android/platform-tools"));
        processBuilder.command("adb", "pull", "/storage/self/primary/DCIM/Camera/VID_20221122_202548.mp4", "/Volumes/Backup/test");
        var process = processBuilder.start();
        var exitCode = process.waitFor();
        var out = IOUtils.toString(process.getInputStream());
        System.out.println("Exit code: " + exitCode);
        System.out.println("Out: " + out);
    }
}
