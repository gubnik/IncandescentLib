/*
    Incandescent Lib, Minecraft Forge light-weight library
    Copyright (C) 2025, nikgub_

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package xyz.nikgub.incandescent.itemgen;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraftforge.fml.loading.FMLPaths;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Class responsible for gathering data from a JSON file
 * and mapping it to string names of properties.
 *
 * @see ItemGenDefinition
 */
public class ItemGenConfigProvider
{
    private final Map<String, ItemGenObjectInfo> itemObjects;

    private ItemGenConfigProvider (final String jsonString)
    {
        Gson gson = new Gson();
        Type type = TypeToken.getParameterized(Map.class, String.class, TypeToken.getParameterized(Map.class, String.class, Object.class).getType()).getType();
        Map<String, Map<String, Object>> map = gson.fromJson(jsonString, type);
        itemObjects = map.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, stringMapEntry -> new ItemGenObjectInfo(stringMapEntry.getValue())));
    }

    @Contract("_ -> new")
    public static @NotNull ItemGenConfigProvider inAnyDir (String rawLocation)
    {
        final String jsonString;
        try
        {
            jsonString = Files.readString(Path.of(rawLocation));
        } catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        return new ItemGenConfigProvider(jsonString);
    }

    @Contract("_ -> new")
    public static @NotNull ItemGenConfigProvider inConfigDir (String configLocation)
    {
        final String jsonString;
        try
        {
            jsonString = Files.readString(Path.of(FMLPaths.CONFIGDIR.toString(),"config", configLocation));
        } catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        return new ItemGenConfigProvider(jsonString);
    }

    @Contract("_ -> new")
    public static @NotNull ItemGenConfigProvider inDataDir (String dataLocation)
    {
        final String jsonString;
        final URL url = ItemGenConfigProvider.class.getClassLoader().getResource("data/" + dataLocation);
        if (url == null)
        {
            throw new RuntimeException("Unable to open file \"" + dataLocation + "\" in data folder");
        }
        try
        {
            jsonString = Files.readString(Path.of(url.getPath()));
        } catch (IOException e)
        {
            throw new RuntimeException("Unable to open file \"" + dataLocation + "\" in data folder", e);
        }
        return new ItemGenConfigProvider(jsonString);
    }

    public Map<String, ItemGenObjectInfo> getItemObjects ()
    {
        return itemObjects;
    }
}
