(function () {
  "use strict";

  const rowConfig = {
    port: { containerId: "portRows", templateId: "portRowTemplate" },
    volume: { containerId: "volumeRows", templateId: "volumeRowTemplate" },
    env: { containerId: "envRows", templateId: "envRowTemplate" },
    label: { containerId: "labelRows", templateId: "labelRowTemplate" },
  };

  function addRow(kind) {
    const cfg = rowConfig[kind];
    const template = document.getElementById(cfg.templateId);
    const container = document.getElementById(cfg.containerId);
    const node = template.content.firstElementChild.cloneNode(true);
    node.querySelector(".btn-remove").addEventListener("click", () => {
      node.remove();
      scheduleGenerate();
    });
    node.querySelectorAll("input, select").forEach((el) => {
      el.addEventListener("input", scheduleGenerate);
      el.addEventListener("change", scheduleGenerate);
    });
    container.appendChild(node);
    return node;
  }

  document.querySelectorAll("[data-add]").forEach((btn) => {
    btn.addEventListener("click", () => {
      addRow(btn.getAttribute("data-add"));
      scheduleGenerate();
    });
  });

  // Seed with one friendly example row each, so the form isn't intimidatingly empty.
  addRow("port");
  addRow("volume");
  addRow("env");

  function readRows(kind, fields) {
    const cfg = rowConfig[kind];
    const container = document.getElementById(cfg.containerId);
    const result = [];
    container.querySelectorAll(".row").forEach((row) => {
      const entry = {};
      let hasValue = false;
      fields.forEach((f) => {
        const input = row.querySelector(`[data-field="${f}"]`);
        const value = input ? input.value.trim() : "";
        entry[f] = value;
        if (value) hasValue = true;
      });
      if (hasValue) result.push(entry);
    });
    return result;
  }

  function val(id) {
    const el = document.getElementById(id);
    return el ? el.value.trim() : "";
  }

  function checked(id) {
    const el = document.getElementById(id);
    return el ? el.checked : false;
  }

  function buildPayload() {
    return {
      baseCommand: val("baseCommand") || "run",
      image: val("image"),
      containerName: val("containerName"),
      networkName: val("networkName"),
      restartPolicy: val("restartPolicy"),
      workDir: val("workDir"),
      user: val("user"),
      hostname: val("hostname"),
      entrypoint: val("entrypoint"),
      command: val("command"),
      extraFlags: val("extraFlags"),
      cpus: val("cpus"),
      memory: val("memory"),
      detached: checked("detached"),
      interactiveTty: checked("interactiveTty"),
      autoRemove: checked("autoRemove"),
      publishAllPorts: checked("publishAllPorts"),
      ports: readRows("port", ["hostIp", "hostPort", "containerPort", "protocol"]),
      volumes: readRows("volume", ["hostPath", "containerPath", "mode"]),
      envVars: readRows("env", ["key", "value"]),
      labels: readRows("label", ["key", "value"]),
    };
  }

  const commandOut = document.getElementById("commandOut");
  const errorLine = document.getElementById("errorLine");
  const copyBtn = document.getElementById("copyBtn");
  const copyStatus = document.getElementById("copyStatus");

  let debounceTimer = null;
  function scheduleGenerate() {
    clearTimeout(debounceTimer);
    debounceTimer = setTimeout(generate, 250);
  }

  async function generate() {
    const payload = buildPayload();
    if (!payload.image && payload.baseCommand !== "build") {
      // Not enough info yet; show a friendly placeholder rather than an error.
      commandOut.textContent = `docker ${payload.baseCommand || "run"}`;
      errorLine.textContent = "";
      return;
    }
    try {
      const res = await fetch("/api/generate", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload),
      });
      if (!res.ok) {
        const err = await res.json().catch(() => ({}));
        errorLine.textContent = err.error || "Couldn't generate the command — check the fields above.";
        return;
      }
      const data = await res.json();
      commandOut.textContent = data.command;
      errorLine.textContent = "";
    } catch (e) {
      errorLine.textContent = "Couldn't reach the server. Is the app running?";
    }
  }

  // Wire up every static field to regenerate on change.
  [
    "baseCommand", "image", "containerName", "networkName", "restartPolicy",
    "workDir", "user", "hostname", "entrypoint", "command", "extraFlags",
    "cpus", "memory", "detached", "interactiveTty", "autoRemove", "publishAllPorts",
  ].forEach((id) => {
    const el = document.getElementById(id);
    if (!el) return;
    el.addEventListener("input", scheduleGenerate);
    el.addEventListener("change", scheduleGenerate);
  });

  document.getElementById("generateBtn").addEventListener("click", generate);

  copyBtn.addEventListener("click", async () => {
    const text = commandOut.textContent || "";
    try {
      await navigator.clipboard.writeText(text);
    } catch (e) {
      // Fallback for browsers/contexts without Clipboard API access.
      const ta = document.createElement("textarea");
      ta.value = text;
      ta.style.position = "fixed";
      ta.style.opacity = "0";
      document.body.appendChild(ta);
      ta.select();
      document.execCommand("copy");
      document.body.removeChild(ta);
    }
    copyStatus.textContent = "Copied!";
    copyStatus.classList.add("show");
    setTimeout(() => copyStatus.classList.remove("show"), 1500);
  });

  // ---------- Scope visibility: show only panels/fields relevant to the
  // selected base command (docker run / exec / build / ...). ----------
  const imageLabel = document.getElementById("imageLabel");
  const imageHint = document.getElementById("imageHint");
  const imageInput = document.getElementById("image");
  const scopeTip = document.getElementById("scopeTip");

  const labelsByCommand = {
    run: { label: "Image name", hint: "Repository and tag, e.g. postgres:16", placeholder: "e.g. nginx:latest" },
    create: { label: "Image name", hint: "Repository and tag, e.g. postgres:16", placeholder: "e.g. nginx:latest" },
    exec: { label: "Target container", hint: "Name or ID of a running container", placeholder: "e.g. my-nginx" },
    build: { label: "Image tag", hint: "Name to tag the built image with", placeholder: "e.g. myapp:1.0" },
    start: { label: "Container", hint: "Name or ID of the container", placeholder: "e.g. my-nginx" },
    stop: { label: "Container", hint: "Name or ID of the container", placeholder: "e.g. my-nginx" },
    restart: { label: "Container", hint: "Name or ID of the container", placeholder: "e.g. my-nginx" },
    rm: { label: "Container", hint: "Name or ID of the container to remove", placeholder: "e.g. my-nginx" },
    rmi: { label: "Image", hint: "Name or ID of the image to remove", placeholder: "e.g. nginx:latest" },
    logs: { label: "Container", hint: "Name or ID of the container", placeholder: "e.g. my-nginx" },
    ps: { label: "(not used by ps)", hint: "ps lists containers; leave blank", placeholder: "" },
    pull: { label: "Image", hint: "Repository and tag to pull", placeholder: "e.g. redis:7" },
    push: { label: "Image", hint: "Repository and tag to push", placeholder: "e.g. myrepo/app:1.0" },
  };

  function applyScope() {
    const cmd = val("baseCommand") || "run";
    document.querySelectorAll("[data-scope]").forEach((el) => {
      const scopes = el.getAttribute("data-scope").split(" ");
      el.style.display = scopes.includes(cmd) ? "" : "none";
    });

    const meta = labelsByCommand[cmd] || labelsByCommand.run;
    imageLabel.textContent = meta.label;
    imageHint.innerHTML = meta.hint.replace(/(\S+:\S+)/, "<code>$1</code>");
    imageInput.placeholder = meta.placeholder;

    scopeTip.innerHTML = cmd === "run" || cmd === "create"
      ? "Options that don't apply to <code>docker run</code> / <code>create</code> are hidden automatically as you switch the base command above."
      : `Showing only the options relevant to <code>docker ${cmd}</code>. Anything extra can go in the "Extra flags" field.`;
  }

  document.getElementById("baseCommand").addEventListener("change", () => {
    applyScope();
    generate();
  });

  applyScope();
  generate();
})();
