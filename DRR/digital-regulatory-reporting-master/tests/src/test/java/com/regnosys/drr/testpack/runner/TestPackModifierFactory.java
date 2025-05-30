package com.regnosys.drr.testpack.runner;

import com.regnosys.drr.testpack.TestPackModifier;
import com.regnosys.drr.testpack.modifiers.ModifierContext;

import java.util.List;

public interface TestPackModifierFactory {
    List<TestPackModifier> getTestPackModifierModifiers(ModifierContext context);
}
