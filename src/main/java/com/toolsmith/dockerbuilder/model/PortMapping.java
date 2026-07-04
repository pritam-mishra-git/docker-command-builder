package com.toolsmith.dockerbuilder.model;

/**
 * Represents a single -p host:container[/protocol] mapping.
 */
public class PortMapping {

    private String hostPort;
    private String containerPort;
    /** tcp, udp, or blank for docker's default (tcp) */
    private String protocol;
    /** Optional host interface to bind to, e.g. 127.0.0.1 */
    private String hostIp;
    
    private String port;

    public PortMapping() {
    }

    public String getHostPort() {
        return hostPort;
    }

    public void setHostPort(String hostPort) {
        this.hostPort = hostPort;
    }

    public String getContainerPort() {
        return containerPort;
    }

    public void setContainerPort(String containerPort) {
        this.containerPort = containerPort;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getHostIp() {
        return hostIp;
    }

    public void setHostIp(String hostIp) {
        this.hostIp = hostIp;
    }
}
