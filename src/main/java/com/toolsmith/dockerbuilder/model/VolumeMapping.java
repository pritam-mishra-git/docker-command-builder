package com.toolsmith.dockerbuilder.model;

/**
 * Represents a single -v host:container[:mode] volume/bind mount mapping.
 */
public class VolumeMapping {

    private String hostPath;
    private String containerPath;
    /** ro, rw, or blank for docker's default (rw) */
    private String mode;

    public VolumeMapping() {
    }

    public String getHostPath() {
        return hostPath;
    }

    public void setHostPath(String hostPath) {
        this.hostPath = hostPath;
    }

    public String getContainerPath() {
        return containerPath;
    }

    public void setContainerPath(String containerPath) {
        this.containerPath = containerPath;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }
}
