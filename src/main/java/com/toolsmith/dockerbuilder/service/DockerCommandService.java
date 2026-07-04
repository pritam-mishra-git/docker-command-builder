package com.toolsmith.dockerbuilder.service;

import com.toolsmith.dockerbuilder.model.DockerCommandRequest;
import com.toolsmith.dockerbuilder.model.KeyValue;
import com.toolsmith.dockerbuilder.model.PortMapping;
import com.toolsmith.dockerbuilder.model.VolumeMapping;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Builds a docker CLI command line, token by token, from a
 * {@link DockerCommandRequest}. Tokens are joined with single spaces at the
 * end; any token containing whitespace is wrapped in double quotes so the
 * resulting string is safe to paste straight into a shell.
 */
@Service
public class DockerCommandService {

    /** Subcommands that accept the full run-style option set. */
    private static final Set<String> RUN_LIKE = Set.of("run", "create");

    public String build(DockerCommandRequest req) {
        List<String> tokens = new ArrayList<>();
        tokens.add("docker");

        String base = blankToDefault(req.getBaseCommand(), "run").trim();
        tokens.add(base);

        if (RUN_LIKE.contains(base)) {
            buildRunLikeOptions(req, tokens);
        } else if ("exec".equals(base)) {
            buildExecOptions(req, tokens);
        } else if ("build".equals(base)) {
            buildBuildOptions(req, tokens);
        } else {
            buildGenericOptions(req, tokens);
        }

        appendExtraFlags(req.getExtraFlags(), tokens);

        // Image / target and trailing command apply broadly across subcommands.
        if (!"build".equals(base) && notBlank(req.getImage())) {
            tokens.add(req.getImage());
        }

        if (notBlank(req.getCommand())) {
            // Split the trailing command on spaces but keep quoted groups intact
            // by simply appending as-is tokens split on whitespace; quoting of
            // individual pieces is handled by joinCommand().
            for (String piece : req.getCommand().trim().split("\\s+")) {
                tokens.add(piece);
            }
        }

        return joinCommand(tokens);
    }

    private void buildRunLikeOptions(DockerCommandRequest req, List<String> tokens) {
        if (req.isDetached()) {
            tokens.add("-d");
        }
        if (req.isInteractiveTty()) {
            tokens.add("-it");
        }
        if (req.isAutoRemove()) {
            tokens.add("--rm");
        }
        if (notBlank(req.getContainerName())) {
            tokens.add("--name");
            tokens.add(req.getContainerName());
        }

        if (req.getPorts() != null) {
            for (PortMapping p : req.getPorts()) {
                String flag = formatPort(p);
                if (flag != null) {
                    tokens.add("-p");
                    tokens.add(flag);
                }
            }
        }
        if (req.isPublishAllPorts()) {
            tokens.add("-P");
        }

        if (req.getVolumes() != null) {
            for (VolumeMapping v : req.getVolumes()) {
                String flag = formatVolume(v);
                if (flag != null) {
                    tokens.add("-v");
                    tokens.add(flag);
                }
            }
        }

        if (req.getEnvVars() != null) {
            for (KeyValue kv : req.getEnvVars()) {
                String flag = formatKeyValue(kv);
                if (flag != null) {
                    tokens.add("-e");
                    tokens.add(flag);
                }
            }
        }

        if (req.getLabels() != null) {
            for (KeyValue kv : req.getLabels()) {
                String flag = formatKeyValue(kv);
                if (flag != null) {
                    tokens.add("--label");
                    tokens.add(flag);
                }
            }
        }

        if (notBlank(req.getNetworkName())) {
            tokens.add("--network");
            tokens.add(req.getNetworkName());
        }
        if (notBlank(req.getRestartPolicy()) && !"no".equalsIgnoreCase(req.getRestartPolicy())) {
            tokens.add("--restart");
            tokens.add(req.getRestartPolicy());
        }
        if (notBlank(req.getWorkDir())) {
            tokens.add("-w");
            tokens.add(req.getWorkDir());
        }
        if (notBlank(req.getUser())) {
            tokens.add("-u");
            tokens.add(req.getUser());
        }
        if (notBlank(req.getHostname())) {
            tokens.add("-h");
            tokens.add(req.getHostname());
        }
        if (notBlank(req.getEntrypoint())) {
            tokens.add("--entrypoint");
            tokens.add(req.getEntrypoint());
        }
        if (notBlank(req.getCpus())) {
            tokens.add("--cpus");
            tokens.add(req.getCpus());
        }
        if (notBlank(req.getMemory())) {
            tokens.add("-m");
            tokens.add(req.getMemory());
        }
    }

    private void buildExecOptions(DockerCommandRequest req, List<String> tokens) {
        if (req.isInteractiveTty()) {
            tokens.add("-it");
        }
        if (req.isDetached()) {
            tokens.add("-d");
        }
        if (notBlank(req.getUser())) {
            tokens.add("-u");
            tokens.add(req.getUser());
        }
        if (notBlank(req.getWorkDir())) {
            tokens.add("-w");
            tokens.add(req.getWorkDir());
        }
        if (req.getEnvVars() != null) {
            for (KeyValue kv : req.getEnvVars()) {
                String flag = formatKeyValue(kv);
                if (flag != null) {
                    tokens.add("-e");
                    tokens.add(flag);
                }
            }
        }
        // For `exec`, the "image" field is used as the target container name.
    }

    private void buildBuildOptions(DockerCommandRequest req, List<String> tokens) {
        if (notBlank(req.getImage())) {
            tokens.add("-t");
            tokens.add(req.getImage());
        }
        if (req.getLabels() != null) {
            for (KeyValue kv : req.getLabels()) {
                String flag = formatKeyValue(kv);
                if (flag != null) {
                    tokens.add("--label");
                    tokens.add(flag);
                }
            }
        }
        // Build context path is supplied via the trailing "command" field
        // (defaults handled below) or via extraFlags, e.g. "-f Dockerfile .".
        if (!notBlank(req.getCommand())) {
            tokens.add(".");
        }
    }

    private void buildGenericOptions(DockerCommandRequest req, List<String> tokens) {
        // Covers stop / start / rm / rmi / logs / ps / pull / push / inspect etc.
        // Only broadly-applicable flags are considered; everything else should
        // go through the "extra flags" field.
        if (notBlank(req.getContainerName())) {
            tokens.add(req.getContainerName());
        }
    }

    private void appendExtraFlags(String extraFlags, List<String> tokens) {
        if (notBlank(extraFlags)) {
            for (String piece : extraFlags.trim().split("\\s+")) {
                tokens.add(piece);
            }
        }
    }

    private String formatPort(PortMapping p) {
        if (p == null || !notBlank(p.getContainerPort())) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        if (notBlank(p.getHostIp())) {
            sb.append(p.getHostIp()).append(":");
        }
        if (notBlank(p.getHostPort())) {
            sb.append(p.getHostPort()).append(":");
        }
        sb.append(p.getContainerPort());
        if (notBlank(p.getProtocol()) && !"tcp".equalsIgnoreCase(p.getProtocol())) {
            sb.append("/").append(p.getProtocol());
        }
        return sb.toString();
    }

    private String formatVolume(VolumeMapping v) {
        if (v == null || !notBlank(v.getHostPath()) || !notBlank(v.getContainerPath())) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(v.getHostPath()).append(":").append(v.getContainerPath());
        if (notBlank(v.getMode())) {
            sb.append(":").append(v.getMode());
        }
        return sb.toString();
    }

    private String formatKeyValue(KeyValue kv) {
        if (kv == null || !notBlank(kv.getKey())) {
            return null;
        }
        String value = kv.getValue() == null ? "" : kv.getValue();
        return kv.getKey() + "=" + value;
    }

    private boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }

    private String blankToDefault(String s, String def) {
        return notBlank(s) ? s : def;
    }

    /**
     * Joins tokens with spaces, quoting any token that itself contains
     * whitespace so the command remains valid when pasted into a shell.
     */
    private String joinCommand(List<String> tokens) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tokens.size(); i++) {
            String t = tokens.get(i);
            if (i > 0) {
                sb.append(" ");
            }
            if (needsQuoting(t)) {
                sb.append("\"").append(t.replace("\"", "\\\"")).append("\"");
            } else {
                sb.append(t);
            }
        }
        return sb.toString();
    }

    private boolean needsQuoting(String token) {
        return token.chars().anyMatch(Character::isWhitespace);
    }
}
