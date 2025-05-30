package drr.regulation.common.processor;

import com.regnosys.rosetta.common.translation.MappingContext;
import com.regnosys.rosetta.common.translation.MappingProcessor;
import com.regnosys.rosetta.common.translation.Path;
import com.regnosys.rosetta.common.translation.Path.PathElement;
import com.rosetta.model.lib.RosettaModelObjectBuilder;
import com.rosetta.model.lib.path.RosettaPath;
import drr.regulation.common.ReportableInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.regnosys.rosetta.common.translation.Mapping;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Optional;

import static com.regnosys.rosetta.common.translation.MappingProcessorUtils.getNonNullMappedValue;

public class CryptoBasedMappingProcessor extends MappingProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(CryptoBasedMappingProcessor.class);
    public static final String CRYPTO_SCHEME = "http://www.fpml.org/coding-scheme/esma-emir-refit-crypto-asset-indicator";

    public CryptoBasedMappingProcessor(RosettaPath modelPath, List<Path> synonymPaths, MappingContext context) {
        super(modelPath, synonymPaths, context);
    }

    @Override
    public <T> void mapBasic(Path xmlPath, Optional<T> instance, RosettaModelObjectBuilder parent) {
        Boolean cryptoBased = determineCrypto(xmlPath);
        if (cryptoBased != null) {
            // Assuming you have access to the appropriate JSON element in the parent object
            // Update the JSON element based on the cryptoBased value
            ReportableInformation.ReportableInformationBuilder reportableInformationBuilder = (ReportableInformation.ReportableInformationBuilder) parent;
            reportableInformationBuilder.setCryptoBased(cryptoBased);
        }
    }

    private Boolean determineCrypto(Path xmlPath) {
        Boolean cryptoBased = isCryptoBasedByProductType(xmlPath);
        if (cryptoBased != null) {
            return cryptoBased;
        }

        return null; // No matching conditions found
    }

    private Boolean isCryptoBasedByProductType(Path xmlPath) {
        List<Path> productTypePaths = getMappings().stream()
                .filter(m -> m.getXmlPath().endsWith("productType"))
                .map(Mapping::getXmlPath)
                .collect(Collectors.toList());

        for (Path productTypePath : productTypePaths) {
            Optional<String> mappedValue = getNonNullMappedValue(productTypePath, getMappings());

            if (mappedValue.isPresent()) {
                if (mappedValue.get().equals("true")) {
                    PathElement schemeElement = productTypePath.getElements().stream()
                            .filter(element -> element.getMetas().containsKey("productTypeScheme"))
                            .findFirst()
                            .orElse(null);

                    if (schemeElement != null) {
                        String productTypeScheme = schemeElement.getMetas().get("productTypeScheme");
                        if (CRYPTO_SCHEME.equals(productTypeScheme)) {
                            return true;
                        }
                    } else {
                        return null; // No productTypeScheme attribute found, return null
                    }
                } else if (mappedValue.get().equals("false")) {
                    PathElement schemeElement = productTypePath.getElements().stream()
                            .filter(element -> element.getMetas().containsKey("productTypeScheme"))
                            .findFirst()
                            .orElse(null);

                    if (schemeElement != null) {
                        String productTypeScheme = schemeElement.getMetas().get("productTypeScheme");
                        if (CRYPTO_SCHEME.equals(productTypeScheme)) {
                            return false;
                        }
                    }
                }
            }
        }

        return null; // No matching conditions found
    }

}
