/*
 * ModMaker - https://github.com/TheEntropyShard/ModMaker
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

package me.theentropyshard.modmaker.cosmic;

import me.theentropyshard.modmaker.cosmic.block.Block;
import me.theentropyshard.modmaker.cosmic.block.BlockState;
import me.theentropyshard.modmaker.cosmic.block.model.BlockModel;
import me.theentropyshard.modmaker.cosmic.block.model.BlockModelTexture;
import me.theentropyshard.modmaker.utils.FileUtils;
import me.theentropyshard.modmaker.utils.json.Json;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Project {
    public static Project currentProject;

    private String name;
    private String namespace;

    private final List<Block> blocks;

    public Project(String name, String namespace) {
        this.name = name;
        this.namespace = namespace;

        this.blocks = new ArrayList<>();
    }

    public static Project load(Path modDir) throws IOException {
        Project project = new Project(modDir.getFileName().toString(), modDir.getFileName().toString());

        if (!Files.exists(modDir)) {
            throw new IOException("Mod at " + modDir + " does not exist!");
        }

        Path blocksDir = modDir.resolve("blocks");

        if (!Files.exists(blocksDir)) {
            throw new IOException("Mod at " + modDir + " does not have blocks!");
        }

        Path modelsDir = modDir.resolve("models");

        if (!Files.exists(modelsDir)) {
            throw new IOException("Mod at " + modDir + " does not have models!");
        }

        Path texturesDir = modDir.resolve("textures");

        if (!Files.exists(modelsDir)) {
            throw new IOException("Mod at " + modDir + " does not have textures!");
        }

        Path blocksTexturesDir = texturesDir.resolve("blocks");

        if (!Files.exists(blocksTexturesDir)) {
            throw new IOException("Mod at " + modDir + " does not have blocks textures!");
        }

        List<Path> blockJsonFiles = FileUtils.list(blocksDir);

        for (Path blockJsonFile : blockJsonFiles) {
            if (!Files.isRegularFile(blockJsonFile)) {
                continue;
            }

            Block block = Json.parse(FileUtils.readUtf8(blockJsonFile), Block.class);

            for (BlockState blockState : block.getBlockStates().values()) {
                String modelJsonPath = blockState.getModelName().split(":")[1];

                Path modelJsonFile = modDir.resolve(modelJsonPath);

                BlockModel blockModel = Json.parse(FileUtils.readUtf8(modelJsonFile), BlockModel.class);

                Map<String, BlockModelTexture> textures = blockModel.getTextures();

                if (textures == null) {
                    continue;
                }

                for (BlockModelTexture modelTexture : textures.values()) {
                    String fileName = modelTexture.getFileName().split(":")[1];

                    BufferedImage texture;

                    try (InputStream inputStream = Files.newInputStream(modDir.resolve(fileName))) {
                        texture = ImageIO.read(inputStream);
                    }

                    modelTexture.setTexture(texture);
                }

                blockState.setBlockModel(blockModel);
            }

            project.addBlock(block);
        }

        Project.currentProject = project;

        return project;
    }

    public void addBlock(Block block) {
        this.blocks.add(block);
    }

    public void removeBlock(Block block) {
        this.blocks.remove(block);
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNamespace() {
        return this.namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public List<Block> getBlocks() {
        return this.blocks;
    }
}
