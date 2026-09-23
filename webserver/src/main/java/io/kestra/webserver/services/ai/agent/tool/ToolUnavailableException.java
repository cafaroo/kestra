package io.kestra.webserver.services.ai.agent.tool;

public class ToolUnavailableException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ToolUnavailableException(final String tool, final String tenant) {
        super("Tool '%s' is unavailable for tenant '%s' in the current instance mode.".formatted(tool, tenant));
    }
}
