package com.regnosys.drr.testpack.runner;

import com.regnosys.drr.testpack.TestPackModifier;
import com.regnosys.drr.testpack.modifiers.ModifierContext;
import com.regnosys.rosetta.common.util.ClassPathUtils;
import com.regnosys.rosetta.common.util.UrlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ClasspathScanningTestPackModifierFactory implements TestPackModifierFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestPackModifierRunner.class);
    private static final Class<TestPackModifier> TESTPACKMODIFIER_CLASS = TestPackModifier.class;

    private static boolean hasConstructor(Class<?> loadedClass) {
        try {
            loadedClass.getConstructor(ModifierContext.class);
            return true;
        } catch (NoSuchMethodException e) {
            LOGGER.warn("Class {} does not have default constructor. Ignoring.", loadedClass);
            return false;
        }
    }

    @Override
    public List<TestPackModifier> getTestPackModifierModifiers(ModifierContext context) {
        List<Class<TestPackModifier>> modifiers = findClasses("Modifier", TESTPACKMODIFIER_CLASS);
        LOGGER.info("Found modifiers: " + modifiers.stream().map(Class::getSimpleName).collect(Collectors.toList()));
        return modifiers.stream()
                .map(clazz -> createInstance(clazz, context))
                .collect(Collectors.toList());
    }

    private <T> T createInstance(Class<T> c, ModifierContext context) {
        try {
            return c.getConstructor(ModifierContext.class).newInstance(context);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private <T> List<Class<T>> findClasses(String classPattern, Class<T> interfaceToScan) {
        List<Class<T>> result = new ArrayList<>();
        List<Path> pathsFromClassPath = findClassFiles(classPattern);
        URI currentLocation = getCurrentLocation();
        for (Path classFilePath : pathsFromClassPath) {
            Class<?> loadedClass = loadClassFromPath(classFilePath, currentLocation);

            boolean isInterface = loadedClass.isInterface();
            if (!isInterface && !Modifier.isAbstract(loadedClass.getModifiers()) && interfaceToScan.isAssignableFrom(loadedClass) && hasConstructor(loadedClass)) {
                //noinspection unchecked
                result.add((Class<T>) loadedClass);
            }
        }
        return result;
    }

    private URI getCurrentLocation() {
        try {
            return TESTPACKMODIFIER_CLASS.getProtectionDomain().getCodeSource().getLocation().toURI();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private Class<?> loadClassFromPath(Path classFilePath, URI currentLocation) {
        try {
            URI uri = UrlUtils.toUrl(classFilePath).toURI();
            URI relativePath = currentLocation.relativize(uri);
            String classFqn = relativePath.toString().replace("/", ".").replace(".class", "");
            return TESTPACKMODIFIER_CLASS.getClassLoader().loadClass(classFqn);
        } catch (URISyntaxException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Path> findClassFiles(String classPattern) {
        return ClassPathUtils.findPathsFromClassPath(
                List.of(TESTPACKMODIFIER_CLASS.getPackage().getName().replace(".", "/")),
                ".*" + classPattern + "\\.class",
                Optional.empty(),
                TESTPACKMODIFIER_CLASS.getClassLoader());
    }
}
