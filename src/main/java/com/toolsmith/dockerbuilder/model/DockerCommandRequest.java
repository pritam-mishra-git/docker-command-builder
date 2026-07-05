package com.toolsmith.dockerbuilder.model;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

/**
 * Captures every option the UI form can send to build a docker CLI command.
 * Only {@link #image} is required; everything else is optional and simply
 * omitted from the generated command when blank/empty.
 */
public class DockerCommandRequest {

    /** e.g. "run", "exec", "build", "stop", "rm", "logs" ... defaults to "run" */
    private String baseCommand = "run";

    @NotBlank(message = "Image name is required")
    private String image;

    private String containerName;
    private String networkName;
    private String restartPolicy;   // no | on-failure | always | unless-stopped
    private String workDir;         // -w
    private String user;            // -u
    private String hostname;        // -h
    private String entrypoint;      // --entrypoint
    private String command;         // trailing CMD / args, e.g. "npm start"
    private String extraFlags;      // free-text flags appended as-is
    private String cpus;            // --cpus
    private String memory;          // -m / --memory

    private boolean detached;       // -d
    private boolean interactiveTty; // -it
    private boolean autoRemove;     // --rm
    private boolean publishAllPorts; // -P

    private List<PortMapping> ports;
    private List<VolumeMapping> volumes;
    private List<KeyValue> envVars;
    private List<KeyValue> labels;

    public DockerCommandRequest() {
    }

    public String getBaseCommand() {
        return baseCommand;
    }

    public void setBaseCommand(String baseCommand) {
        this.baseCommand = baseCommand;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getContainerName() {
        return containerName;
    }

    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }

    public String getNetworkName() {
        return networkName;
    }

    public void setNetworkName(String networkName) {
        this.networkName = networkName;
    }

    public String getRestartPolicy() {
        return restartPolicy;
    }

    public void setRestartPolicy(String restartPolicy) {
        this.restartPolicy = restartPolicy;
    }

    public String getWorkDir() {
        return workDir;
    }

    public void setWorkDir(String workDir) {
        this.workDir = workDir;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getEntrypoint() {
        return entrypoint;
    }

    public void setEntrypoint(String entrypoint) {
        this.entrypoint = entrypoint;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getExtraFlags() {
        return extraFlags;
    }

    public void setExtraFlags(String extraFlags) {
        this.extraFlags = extraFlags;
    }

    public String getCpus() {
        return cpus;
    }

    public void setCpus(String cpus) {
        this.cpus = cpus;
    }

    public String getMemory() {
        return memory;
    }

    public void setMemory(String memory) {
        this.memory = memory;
    }

    public boolean isDetached() {
        return detached;
    }

    public void setDetached(boolean detached) {
        this.detached = detached;
    }

    public boolean isInteractiveTty() {
        return interactiveTty;
    }

    public void setInteractiveTty(boolean interactiveTty) {
        this.interactiveTty = interactiveTty;
    }

    public boolean isAutoRemove() {
        return autoRemove;
    }

    public void setAutoRemove(boolean autoRemove) {
        this.autoRemove = autoRemove;
    }

    public boolean isPublishAllPorts() {
        return publishAllPorts;
    }

    public void setPublishAllPorts(boolean publishAllPorts) {
        this.publishAllPorts = publishAllPorts;
    }

    public List<PortMapping> getPorts() {
        return ports;
    }

    public void setPorts(List<PortMapping> ports) {
        this.ports = ports;
    }

    public List<VolumeMapping> getVolumes() {
        return volumes;
    }

    public void setVolumes(List<VolumeMapping> volumes) {
        this.volumes = volumes;
    }

    public List<KeyValue> getEnvVars() {
        return envVars;
    }

    public void setEnvVars(List<KeyValue> envVars) {
        this.envVars = envVars;
    }

    public List<KeyValue> getLabels() {
        return labels;
    }

    public void setLabels(List<KeyValue> labels) {
        this.labels = labels;
    }
}
