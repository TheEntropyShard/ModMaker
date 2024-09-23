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

package me.theentropyshard.modmaker.gui.view;

import me.theentropyshard.modmaker.gui.FormPanel;

import javax.swing.*;
import java.awt.*;

public class BlockEditView extends JPanel {
    public BlockEditView() {
        super(new BorderLayout());

        FormPanel formPanel = new FormPanel();

        formPanel.addFormRow("Block Name", "", s -> {}, "Name of your block");

        this.add(formPanel, BorderLayout.CENTER);
    }
}