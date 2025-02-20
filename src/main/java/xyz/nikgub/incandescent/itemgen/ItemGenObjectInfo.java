package xyz.nikgub.incandescent.itemgen;

import java.util.HashMap;
import java.util.Map;

/**
 * Intermediate representation of the item definition's
 * gathered properties.
 */
public class ItemGenObjectInfo
{
    private final Map<String, Object> itemPropertiesValue;

    public ItemGenObjectInfo (final Map<String, Object> map)
    {
        this.itemPropertiesValue = new HashMap<>();
        itemPropertiesValue.putAll(map);
    }

    public Map<String, Object> getPropertyValues ()
    {
        return itemPropertiesValue;
    }
}
