package xyz.nikgub.incandescent.itemgen_config;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
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

    public ItemGenConfigProvider (String loc)
    {
        Gson gson = new Gson();
        try
        {
            Type type = TypeToken.getParameterized(Map.class, String.class, TypeToken.getParameterized(Map.class, String.class, Object.class).getType()).getType();
            Map<String, Map<String, Object>> map = gson.fromJson(Files.readString(Path.of(System.getProperty("user.dir"),"config", loc)), type);
            itemObjects = map.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, stringMapEntry -> new ItemGenObjectInfo(stringMapEntry.getValue())));
        } catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public Map<String, ItemGenObjectInfo> getItemObjects ()
    {
        return itemObjects;
    }
}
