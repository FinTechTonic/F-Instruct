package com.regnosys.drr.testpack.modifiers;

import java.nio.file.Path;
import java.util.Map;

public class ModifierContext {

    private final Map<Path, String> xmlFileToQualifierMap;
    private final Map<String, String> qualifierToCfiMap;

    public ModifierContext(Map<Path, String> xmlFileToQualifierMap, Map<String, String> qualifierToCfiMap) {
        this.xmlFileToQualifierMap = xmlFileToQualifierMap;
        this.qualifierToCfiMap = qualifierToCfiMap;
    }

    public Map<Path, String> getXmlFileToQualifierMap() {
        return xmlFileToQualifierMap;
    }

    public Map<String, String> getQualifierToCfiMap() {
        return qualifierToCfiMap;
    }
}
