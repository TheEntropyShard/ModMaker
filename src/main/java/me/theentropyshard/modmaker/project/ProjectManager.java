/*
 * ModMaker - https://github.com/TheEntropyShard/ModMaker
 * Copyright (C) 2024 TheEntropyShard
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package me.theentropyshard.modmaker.project;

import me.theentropyshard.modmaker.utils.FileUtils;
import me.theentropyshard.modmaker.utils.ListUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ProjectManager {
    public static final String PROJECT_FILE_NAME = "project.json";

    private final Path workDir;

    private final List<Project> projects;

    public ProjectManager(Path workDir) {
        this.workDir = workDir;

        this.projects = new ArrayList<>();
    }

    public void load() throws IOException {
        for (Path projectDir : FileUtils.list(this.workDir)) {
            if (!Files.isDirectory(projectDir)) {
                continue;
            }

            this.loadProject(projectDir);
        }
    }

    public void loadProject(Path projectDir) throws IOException {
        Project project = Project.load(projectDir);

        if (project == null) {
            return;
        }

        this.projects.add(project);
    }

    public void save() throws IOException {
        for (Project project : this.projects) {
            project.save();
        }
    }

    public Project createProject(String name, String namespace, String version) throws IOException {
        return Project.create(this.workDir, name, namespace, version);
    }

    public Project getProject(String name) {
        return ListUtils.search(this.projects, project -> project.getName().equals(name));
    }

    public Path getProjectDir(String name) {
        Project project = this.getProject(name);

        if (project == null) {
            throw new IllegalArgumentException("Project with name " + name + " does not exist");
        }

        return project.getWorkDir();
    }
}
