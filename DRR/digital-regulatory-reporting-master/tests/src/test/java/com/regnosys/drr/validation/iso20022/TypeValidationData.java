package com.regnosys.drr.validation.iso20022;

public class TypeValidationData {
    private final AttributeValidationData cardinalityValidationData;
    private final AttributeValidationData typeFormatValidationData;
    private final ConditionValidationData conditionValidationData;
    private final Exception exceptionData;

    public TypeValidationData(AttributeValidationData cardinalityValidationData,
                              AttributeValidationData typeFormatValidationData,
                              ConditionValidationData conditionValidationData) {
        this.cardinalityValidationData = cardinalityValidationData;
        this.typeFormatValidationData = typeFormatValidationData;
        this.conditionValidationData = conditionValidationData;
        this.exceptionData = null;
    }

    public TypeValidationData(Exception exceptionData) {
        this.cardinalityValidationData = AttributeValidationData.empty();
        this.typeFormatValidationData = AttributeValidationData.empty();
        this.conditionValidationData = ConditionValidationData.empty();
        this.exceptionData = exceptionData;
    }

    public AttributeValidationData getCardinalityValidationData() {
        return cardinalityValidationData;
    }

    public AttributeValidationData getTypeFormatValidationData() {
        return typeFormatValidationData;
    }

    public ConditionValidationData getConditionValidationData() {
        return conditionValidationData;
    }

    public Exception getExceptionData() {
        return exceptionData;
    }

    @Override
    public String toString() {
        return "TypeValidationData{" +
                "cardinalityValidationData=" + cardinalityValidationData +
                ", typeFormatValidationData=" + typeFormatValidationData +
                ", conditionValidationData=" + conditionValidationData +
                ", exceptionData=" + exceptionData +
                '}';
    }
}