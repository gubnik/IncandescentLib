package xyz.nikgub.incandescent.itemgen;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraftforge.fml.loading.FMLPaths;

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

    public static ItemGenConfigProvider inAnyDir (String rawLocation)
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

    public static ItemGenConfigProvider inConfigDir (String configLocation)
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

    public static ItemGenConfigProvider inDataDir (String dataLocation)
    {
        final String jsonString;
        final URL url = ItemGenConfigProvider.class.getClassLoader().getResource(dataLocation);
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
