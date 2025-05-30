package com.regnosys.model.testpacks;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

import static java.util.function.Predicate.not;

public class TranslateTestPackInitialiser {

    public static void createBlankExpectations(Path baseResourcesPath, Path testPackPath, boolean overwrite) throws IOException {
        Files.walk(testPackPath)
                .filter(Files::isDirectory)
                .filter(not(testPackPath::equals))
                .filter(TranslateTestPackInitialiser::containsXmlFile)
                .map(d -> d.resolve("expectations.json"))
                .filter(exp -> overwrite || !Files.exists(exp))
                .forEach(exp -> writeExpectationsFile(baseResourcesPath, exp));
    }

    private static void writeExpectationsFile(Path baseResourcesPath, Path exp) {
        try {
            String contents = Files.list(exp.getParent())
                    .map(baseResourcesPath::relativize)
                    .map(Path::toString)
                    .filter(p -> !p.contains("expectations.json"))
                    .sorted()
                    .map(p -> String.format("{\"fileName\":\"%s\",\"excludedPaths\":0,\"externalPaths\":0,\"outstandingMappings\":0,\"validationFailures\":0,\"qualificationExpectation\":{\"success\":true,\"qualifyResults\":[],\"qualifiableObjectCount\":0}}", p))
                    .collect(Collectors.joining(",\n", "[\n", "]"));

            Files.write(exp, contents.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static boolean containsXmlFile(Path d) {
        try {
            return Files.list(d).anyMatch(s -> s.getFileName().toString().endsWith(".xml"));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
