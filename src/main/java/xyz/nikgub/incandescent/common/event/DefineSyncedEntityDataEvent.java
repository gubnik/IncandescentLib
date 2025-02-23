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

package xyz.nikgub.incandescent.common.event;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.EntityEvent;
import org.jetbrains.annotations.ApiStatus;
import xyz.nikgub.incandescent.util.HashCacheHypermap;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * An {@link net.minecraftforge.eventbus.api.Event} occurring at the end of {@link Entity} constructor.
 * It should <b>ONLY</b> be used to define additional synced data, and the API is not responsible for
 * any errors caused by its misuse.
 *
 * @see xyz.nikgub.incandescent.mixin.EntityMixin
 *
 * @apiNote As of 1.5.0.x, this does not work due to an unknown reason. It is suggested to avoid using this
 * event until this notice is removed.
 */
@ApiStatus.Experimental
public class DefineSyncedEntityDataEvent extends EntityEvent
{
    /**
     * Static mapping of accessors defined via {@link #defineSyncedData(String, EntityDataSerializer, Object)}.
     * Its contents are produced via calls to this method, and before any occur this map is empty.
     * The storage limit is set to 256 per entity class to avoid severe memory issues.
     *
     * <p>Stored accessors are to be accessed via {@link #accessor(Class, String)}</p>
     *
     * @see #accessor(Class, String)
     * @see HashCacheHypermap
     */
    private static final HashCacheHypermap<Class<? extends Entity>, String, EntityDataAccessor<?>> ACCESSORS = new HashCacheHypermap<>(256);

    private final Set<DataEntry<?>> additionalData = new HashSet<>();

    public DefineSyncedEntityDataEvent (Entity entity)
    {
        super(entity);
    }

    /**
     * Method providing static access to {@link #ACCESSORS}.
     *
     * <p>This method should be used to recall a defined accessor for an entity,
     * if the accessor was not defined the return value will be of {@link Optional#empty()},
     * this is done to enforce the safety.</p>
     *
     * @param name {@code String} name of the accessor to recall
     * @return {@link Optional} of the accessor, empty if it doesn't exist, or had not been
     * stored into {@link #ACCESSORS} yet.
     */
    public static Optional<EntityDataAccessor<?>> accessor (Class<? extends Entity> entityClass, String name)
    {
        return ACCESSORS.get(entityClass, name);
    }

    /**
     * Defines the additional synced data for the {@code entity} of the event.
     * This method produces and stores the {@link EntityDataAccessor} by the {@code name}
     * in a map of defined accessor ({@link #ACCESSORS}).
     *
     * <p>Should the serializer duplicate another, it will not be added, and the return value will be
     * of {@link Optional#empty()}.</p>
     *
     * @param name         {@code String} by which to store the accessor
     * @param serializer   {@link EntityDataSerializer} that will be used to produce the accessor
     * @param defaultValue {@code T} default value of the synced data
     * @param <T>          Type of the synced data
     * @return {@link Optional} of the accessor, {@link Optional#empty()} if the accessor was duplicated.
     */
    public <T> Optional<EntityDataAccessor<T>> defineSyncedData (String name, EntityDataSerializer<T> serializer, T defaultValue)
    {
        EntityDataAccessor<T> accessor = SynchedEntityData.defineId(this.getEntity().getClass(), serializer);
        if (!additionalData.add(new DataEntry<>(accessor, defaultValue)))
        {
            return Optional.empty();
        }
        ACCESSORS.put(this.getEntity().getClass(), name, accessor);
        return Optional.of(accessor);
    }

    /**
     * Getter for {@link #additionalData}
     *
     * @return {@link #additionalData}
     */
    public Set<DataEntry<?>> getAdditionalData ()
    {
        return additionalData;
    }

    /**
     * Utility class representing typed tuple of {@link EntityDataSerializer} and the default value
     * for it.
     * <p>Used for providing explicit typing for {@link SynchedEntityData#define(EntityDataAccessor, Object)}</p>
     *
     * @param <T> Type of the synced data
     * @see xyz.nikgub.incandescent.mixin.EntityMixin
     * @see DefineSyncedEntityDataEvent
     */
    public static class DataEntry<T>
    {
        private final EntityDataAccessor<T> accessor;
        private final T defaultValue;

        private DataEntry (EntityDataAccessor<T> accessor, T defaultValue)
        {
            this.accessor = accessor;
            this.defaultValue = defaultValue;
        }

        public void defineFor (SynchedEntityData entityData)
        {
            entityData.define(accessor, defaultValue);
        }
    }
}
