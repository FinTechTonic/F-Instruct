package com.regnosys.drr.validation.iso20022;

import java.util.ArrayList;
import java.util.List;

public class ConditionValidationData {

    static ConditionValidationData empty() {
        return new ConditionValidationData(0, new ArrayList<>(), 0);
    }

    private final int failureCount;
    private final List<String> failedConditions;
    private final int totalConditionsCount;

    public ConditionValidationData(int failureCount, List<String> failedConditions, int totalConditionsCount) {
        this.failureCount = failureCount;
        this.failedConditions = failedConditions;
        this.totalConditionsCount = totalConditionsCount;
    }

    public int getFailureCount() {
        return failureCount;
    }

    public List<String> getFailedConditions() {
        return failedConditions;
    }

    public int getTotalCount() {
        return totalConditionsCount;
    }

    @Override
    public String toString() {
        return "ConditionValidationData{" +
                "failureCount=" + failureCount +
                ", failedConditions=" + failedConditions +
                ", totalConditionsCount=" + totalConditionsCount +
                '}';
    }
}
