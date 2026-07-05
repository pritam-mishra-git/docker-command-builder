package com.toolsmith.dockerbuilder.controller;

import com.toolsmith.dockerbuilder.model.DockerCommandRequest;
import com.toolsmith.dockerbuilder.model.DockerCommandResponse;
import com.toolsmith.dockerbuilder.service.DockerCommandService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DockerCommandController {

    private final DockerCommandService dockerCommandService;

    public DockerCommandController(DockerCommandService dockerCommandService) {
        this.dockerCommandService = dockerCommandService;
    }

    @PostMapping("/api/generate")
    public DockerCommandResponse generate(@Valid @RequestBody DockerCommandRequest request) {
        String command = dockerCommandService.build(request);
        return new DockerCommandResponse(command);
    }
}
