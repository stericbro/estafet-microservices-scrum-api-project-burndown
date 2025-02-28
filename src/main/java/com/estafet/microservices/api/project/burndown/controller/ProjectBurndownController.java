package com.estafet.microservices.api.project.burndown.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.estafet.microservices.api.project.burndown.model.ProjectBurndown;
import com.estafet.microservices.api.project.burndown.service.ProjectBurndownService;

@RestController
public class ProjectBurndownController {

    @Autowired
    private ProjectBurndownService projectBurndownService;

    @GetMapping("/api")
    public ProjectBurndown getAPI() {
        return ProjectBurndown.getAPI();
    }

    @GetMapping("/project/{id}/burndown")
    public ProjectBurndown getProjectBurndown(@PathVariable int id) {
        return projectBurndownService.getProjectBurndown(id);
    }

}
