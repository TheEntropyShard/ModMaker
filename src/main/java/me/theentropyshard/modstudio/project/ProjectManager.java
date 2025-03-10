/*
 * ModStudio - https://github.com/TheEntropyShard/ModStudio
 * Copyright (C) 2024-2025 TheEntropyShard
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

package me.theentropyshard.modstudio.project;

import com.google.gson.reflect.TypeToken;
import me.theentropyshard.modstudio.cosmic.block.Block;
import me.theentropyshard.modstudio.cosmic.block.BlockState;
import me.theentropyshard.modstudio.cosmic.block.model.BlockModel;
import me.theentropyshard.modstudio.cosmic.block.model.BlockModelTexture;
import me.theentropyshard.modstudio.utils.FileUtils;
import me.theentropyshard.modstudio.utils.json.Json;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProjectManager {
    private final Path workDir;

    private final List<Project> projects;
    private final Map<String, Project> projectsByNamespace;
    private final Map<String, String> recentProjects;

    private Project currentProject;

    public ProjectManager(Path workDir) {
        this.workDir = workDir;

        this.projects = new ArrayList<>();
        this.projectsByNamespace = new HashMap<>();
        this.recentProjects = new HashMap<>();
    }

    public void load() throws IOException {
        this.loadRecentProjects();
        this.loadProjects();
    }

    private void loadRecentProjects() throws IOException {
        Path recentProjectsFile = this.workDir.resolve("recent_projects.json");

        if (!Files.exists(recentProjectsFile)) {
            return;
        }

        if (!Files.isRegularFile(recentProjectsFile)) {
            return;
        }

        String content = FileUtils.readUtf8(recentProjectsFile);

        Type type = TypeToken.getParameterized(Map.class, String.class, String.class).getType();

        this.recentProjects.putAll(Json.parse(content, type));
    }

    public void saveRecentProjects() throws IOException {
        Path recentProjectsFile = this.workDir.resolve("recent_projects.json");

        FileUtils.writeUtf8(recentProjectsFile, Json.writePretty(this.recentProjects));
    }

    private void loadProjects() throws IOException {
        List<Path> paths = FileUtils.list(this.workDir);

        for (Path path : paths) {
            if (!Files.isDirectory(path)) {
                continue;
            }

            Project project;

            Path projectFile = path.resolve("project.json");
            boolean notExist = !Files.exists(projectFile);

            if (notExist) {
                String fileName = path.getFileName().toString();

                project = new Project(fileName, fileName, "0.0.1");
            } else {
                project = Json.parse(FileUtils.readUtf8(projectFile), Project.class);
            }

            project.setWorkDir(path);

            if (notExist) {
                project.save();
            }

            this.cacheProject(project);
        }
    }

    public Project loadProject(Project project) throws IOException {
        Path projectDir = project.getWorkDir();

        Path blocksDir = projectDir.resolve("blocks");

        if (!Files.exists(blocksDir)) {
            return project;
        }

        Path modelsDir = projectDir.resolve("models");

        if (!Files.exists(modelsDir)) {
            return project;
        }

        Path texturesDir = projectDir.resolve("textures");

        if (!Files.exists(modelsDir)) {
            return project;
        }

        Path blocksTexturesDir = texturesDir.resolve("blocks");

        if (!Files.exists(blocksTexturesDir)) {
            return project;
        }

        List<Path> blockJsonFiles = FileUtils.list(blocksDir);

        for (Path blockJsonFile : blockJsonFiles) {
            if (!Files.isRegularFile(blockJsonFile)) {
                continue;
            }

            Block block = Json.parse(FileUtils.readUtf8(blockJsonFile), Block.class);

            for (BlockState blockState : block.getBlockStates().values()) {
                String modelJsonPath = blockState.getModelName().split(":")[1];

                Path modelJsonFile = projectDir.resolve(modelJsonPath);

                BlockModel blockModel = Json.parse(FileUtils.readUtf8(modelJsonFile), BlockModel.class);

                Map<String, BlockModelTexture> textures = blockModel.getTextures();

                if (textures == null) {
                    continue;
                }

                for (BlockModelTexture modelTexture : textures.values()) {
                    String textureFileName = modelTexture.getFileName();

                    if (textureFileName == null) {
                        continue;
                    }

                    String fileName = textureFileName.split(":")[1];

                    BufferedImage texture;

                    try (InputStream inputStream = Files.newInputStream(projectDir.resolve(fileName))) {
                        texture = ImageIO.read(inputStream);
                    }

                    modelTexture.setTexture(texture);
                }

                blockState.setBlockModel(blockModel);
            }

            project.addBlock(block);
        }

        //this.cacheProject(project);

        return project;
    }

    public Project createProject(String name, String namespace, String version) throws IOException {
        Path projectDir = this.workDir.resolve(namespace);
        FileUtils.createDirectoryIfNotExists(projectDir);

        Project project = new Project(name, namespace, version);
        project.setWorkDir(projectDir);
        project.save();

        this.cacheProject(project);

        return project;
    }

    public void deleteProject(String namespace) throws IOException {
        Project project = this.getProjectByNamespace(namespace);

        if (project == null) {
            return;
        }

        FileUtils.delete(project.getWorkDir());

        this.uncacheProject(project);
    }

    public boolean projectExists(String namespace) {
        Path projectDir = this.workDir.resolve(namespace);

        return Files.exists(projectDir) && Files.isDirectory(projectDir);
    }

    private void cacheProject(Project project) {
        if (this.projectsByNamespace.containsKey(project.getName())) {
            return;
        }

        this.projects.add(project);
        this.projectsByNamespace.put(project.getName(), project);
    }

    private void uncacheProject(Project project) {
        if (!this.projectsByNamespace.containsKey(project.getName())) {
            return;
        }

        this.projects.remove(project);
        this.projectsByNamespace.remove(project.getName());
    }

    private void uncacheAll() {
        this.projects.clear();
        this.projectsByNamespace.clear();
    }

    public Project getProjectByNamespace(String namespace) {
        return this.projectsByNamespace.get(namespace);
    }

    public List<Project> getProjects() {
        return this.projects;
    }

    public Map<String, String> getRecentProjects() {
        return this.recentProjects;
    }

    public Project getCurrentProject() {
        return this.currentProject;
    }

    public void setCurrentProject(Project currentProject) {
        this.currentProject = currentProject;
    }
}
