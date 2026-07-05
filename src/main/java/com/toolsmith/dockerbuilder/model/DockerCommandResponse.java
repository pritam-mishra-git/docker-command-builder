package com.toolsmith.dockerbuilder.model;

public class DockerCommandResponse {

    private String command;

    public DockerCommandResponse() {
    }

    public DockerCommandResponse(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }
}
