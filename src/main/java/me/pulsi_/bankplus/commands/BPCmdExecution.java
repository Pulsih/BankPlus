package me.pulsi_.bankplus.commands;

public abstract class BPCmdExecution {

    public ExecutionType executionType = ExecutionType.VALID_EXECUTION;

    public abstract void execute();

    public enum ExecutionType {
        INVALID_EXECUTION, // When the cmd does not specify enough arguments, or specify incorrect arguments.
        VALID_EXECUTION // When the cmd has correct arguments and is ready to execute.
    }

    public static BPCmdExecution invalidExecution() {
        BPCmdExecution execution = new BPCmdExecution() {
            @Override
            public void execute() {
            }
        };
        execution.executionType = ExecutionType.INVALID_EXECUTION;
        return execution;
    }
}