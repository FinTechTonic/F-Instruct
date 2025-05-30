package com.regnosys.drr.transform;

import cdm.event.workflow.WorkflowStep;
import cdm.event.workflow.functions.Create_AcceptedWorkflowStepFromInstruction;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.regnosys.drr.DrrRuntimeModuleExternalApi;

public class RunCreateWorkflowStepFromInstruction {

    @Inject
    private Create_AcceptedWorkflowStepFromInstruction createWorkflowStepFromInstruction;

    public RunCreateWorkflowStepFromInstruction() {
        Injector injector = Guice.createInjector(new DrrRuntimeModuleExternalApi());
        injector.injectMembers(this);
    }

    public WorkflowStep createWorkflowStepFromInstruction(WorkflowStep workflowStep) {
        return isWorkflowStepInstruction(workflowStep) ?
                createWorkflowStepFromInstruction.evaluate(workflowStep) :
                workflowStep;
    }

    private boolean isWorkflowStepInstruction(WorkflowStep workflowStep) {
        return workflowStep.getBusinessEvent() == null && workflowStep.getProposedEvent() != null;
    }
}
