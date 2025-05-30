package com.regnosys.drr.validation.iso20022;

import java.util.ArrayList;
import java.util.List;

public class AttributeValidationData {

    static AttributeValidationData empty() {
        return new AttributeValidationData(0, new ArrayList<>(), 0);
    }

    private final int failureCount;
    private final List<String> failedAttributeNames;
    private final int totalCount;

    public AttributeValidationData(int failureCount, List<String> failedAttributeNames, int totalCount) {
        this.failureCount = failureCount;
        this.failedAttributeNames = failedAttributeNames;
        this.totalCount = totalCount;
    }

    public int getFailureCount() {
        return failureCount;
    }

    public List<String> getFailedAttributeNames() {
        return failedAttributeNames;
    }

    public int getTotalCount() {
        return totalCount;
    }

    @Override
    public String toString() {
        return "AttributeValidationData{" +
                "failureCount=" + failureCount +
                ", failedAttributeNames=" + failedAttributeNames +
                ", totalCount=" + totalCount +
                '}';
    }
}
