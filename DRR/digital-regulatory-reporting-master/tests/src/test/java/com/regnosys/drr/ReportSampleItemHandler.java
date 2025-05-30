package com.regnosys.drr;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rosetta.model.lib.RosettaModelObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Set;

public class ReportSampleItemHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReportSampleItemHandler.class);

	private final ObjectMapper objectMapper;

    public ReportSampleItemHandler(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
    }


    public void copyFiles(Path outFolder, Set<TestPackCreatorModel.ReportSampleConfigItem> reportSampleConfigItems) {
        reportSampleConfigItems.forEach(configItem -> {
            var fileLocation = configItem.getSampleFileLocation();
            try {
                InputStream sourceStream = getClass().getClassLoader().getResourceAsStream(fileLocation);

                Path fileName = Paths.get(configItem.targetLocation()).getFileName();

                Path targetPath = outFolder.resolve(directoryName(configItem.getTestPack().getName())).resolve(fileName);

                LOGGER.info("Copying file from {} to {}", fileLocation, targetPath);
                InputStream sourceFileStream = Objects.requireNonNull(sourceStream, "Resource to copy was null");
                String sourceFileContent = new String(sourceFileStream.readAllBytes(), StandardCharsets.UTF_8);

                Files.createDirectories(targetPath.getParent());
                String contentToWrite;
                if (configItem.getTransformFunction() == null) {
                    contentToWrite = sourceFileContent;
                } else {
                    RosettaModelObject rosettaModelObject = configItem.getTransformFunction().invoke(sourceFileContent);

                    contentToWrite = objectMapper.writerWithDefaultPrettyPrinter()
                            .writeValueAsString(rosettaModelObject);
                }

                Files.write(targetPath, contentToWrite.
                        getBytes(StandardCharsets.UTF_8));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    static String directoryName(String name) {
        return name
                .replace(" ", "-")
                .replace("_", "-")
                .trim().toLowerCase();
    }
}
