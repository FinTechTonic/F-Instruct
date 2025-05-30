package com.regnosys.model.functions;

import java.util.function.Supplier;

import com.rosetta.model.lib.expression.ComparisonResult;
import com.rosetta.model.lib.functions.ConditionValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NoOpConditionValidator implements ConditionValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(NoOpConditionValidator.class);

    @Override
    public void validate(Supplier<ComparisonResult> condition, String description) {
        if (!condition.get().get()) {
            LOGGER.warn("Ignoring failed condition: " + description);
        }
    }
}