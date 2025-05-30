package com.regnosys.model.testpacks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.nio.file.attribute.PosixFilePermission.*;

public class TestPackBootstrapRecordKeeping {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestPackBootstrapRecordKeeping.class);
    /**
     * @param args the first argument is the path to the sample files for example:
     *             "/Users/davidalk/REGNOSYS/External - ISDA/"
     *             or "/Users/hugohills/Library/CloudStorage/OneDrive-REGNOSYS/Documents - External/ISDA"
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            throw new IllegalArgumentException("Requires argument with path to sample files");
        }

        String oneDriveBasePath = args[0];
        Path baseResourcesPath = Path.of("rosetta-source/src/main/resources");

        Path recordKeepingTestPackBase = baseResourcesPath.resolve("cdm-sample-files/fpml-5-10/record-keeping");
        Path recordKeepingTestPackOneDriveSource = Path.of(oneDriveBasePath + "/CDM Workstreams/Reporting/Test Pack/Contribution Test Pack/Record Keeping");
        copySourceFiles(recordKeepingTestPackOneDriveSource, recordKeepingTestPackBase);

        Path transparencyTestPackBase = baseResourcesPath.resolve("cdm-sample-files/fpml-5-10/transparency");
        Path transparencyTestPackOneDriveSource = Path.of(oneDriveBasePath + "/CDM Workstreams/Reporting/Test Pack/Contribution Test Pack/Transparency");
        copySourceFiles(transparencyTestPackOneDriveSource, transparencyTestPackBase);

        Path cftcEventsTestPackBase = baseResourcesPath.resolve("cdm-sample-files/fpml-5-10/record-keeping/events/cftc-event-scenarios");
        Path cftcTestPackOneDriveSource = Path.of(oneDriveBasePath + "/CDM Workstreams/Reporting/Test Pack/Native CDM Events");
        copySourceFiles(cftcTestPackOneDriveSource, cftcEventsTestPackBase);

        TranslateTestPackInitialiser.createBlankExpectations(baseResourcesPath, recordKeepingTestPackBase, true);
    }

    private static void copySourceFiles(Path sourceFolderPath, Path targetFolderPath) throws IOException {
        Files.walk(sourceFolderPath, FileVisitOption.FOLLOW_LINKS)
                .filter(f -> f.getFileName().toString().endsWith(".xml"))
                .filter(Files::isRegularFile)
                .forEach(sourceFilePath -> {
                    Path relativeFilePath = sourceFolderPath.relativize(sourceFilePath);
                    Path targetFilePath = sanitizePath(targetFolderPath.resolve(relativeFilePath));
                    LOGGER.info("Copying {}", sourceFilePath);
                    try {
                        Files.createDirectories(targetFilePath.getParent());
                        String contents = Files.readString(sourceFilePath, StandardCharsets.UTF_8);
                        String cleanedContents = contents.replaceAll("\r\n", "\n");
                        Files.write(targetFilePath, cleanedContents.getBytes(StandardCharsets.UTF_8));
                        Files.setPosixFilePermissions(targetFilePath, Set.of(OWNER_READ, OWNER_WRITE, GROUP_READ, OTHERS_READ));
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });
    }

    private static Path sanitizePath(Path path) {
        Path fileName = path.getFileName();
        Path parent = Path.of(path.getParent().toString().toLowerCase(Locale.ROOT));
        return Path.of(parent.resolve(fileName).toString()
                .replace(" ", "-")
                .replaceAll("-+", "-")
        );
    }
}
