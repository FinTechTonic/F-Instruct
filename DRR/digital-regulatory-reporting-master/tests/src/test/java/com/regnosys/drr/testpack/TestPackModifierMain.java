package com.regnosys.drr.testpack;

import com.regnosys.drr.testpack.runner.ClasspathScanningTestPackModifierFactory;
import com.regnosys.drr.testpack.runner.TestPackModifierRunner;

import java.nio.file.Path;
import java.util.Arrays;

public class TestPackModifierMain {

    public static void main(String[] args) throws Exception {
        boolean dryRun = Arrays.asList(args).contains("dryRun");

        String first = "rosetta-source/src/main/resources/cdm-sample-files";
        Path xmlRootDir = Path.of(first);
        ClasspathScanningTestPackModifierFactory testPackModifierFactory = new ClasspathScanningTestPackModifierFactory();

        TestPackModifierRunner testPackModifierRunner = new TestPackModifierRunner(xmlRootDir.normalize().toAbsolutePath(), testPackModifierFactory);
        testPackModifierRunner.run(dryRun);


    }
}
