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

package me.theentropyshard.modstudio.utils;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import me.theentropyshard.modstudio.logging.Log;

import java.io.IOException;

public final class FlatLafUtils {
    public static FlatSVGIcon getSvgIcon(String path) {
        try {
            return new FlatSVGIcon(FlatLafUtils.class.getResourceAsStream(path));
        } catch (IOException e) {
            Log.error("Could not load icon from path " + path, e);

            throw new RuntimeException(e);
        }
    }

    private FlatLafUtils() {
        throw new UnsupportedOperationException();
    }
}
