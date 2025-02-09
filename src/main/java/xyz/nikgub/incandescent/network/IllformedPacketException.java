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

package xyz.nikgub.incandescent.network;

/**
 * Exception thrown if the {@link IncandescentPacket} is not properly formed
 *
 * @see IncandescentNetworkCore
 */
public class IllformedPacketException extends RuntimeException
{
    public IllformedPacketException (String message)
    {
        super(message);
    }

    public IllformedPacketException (String message, Throwable cause)
    {
        super(message, cause);
    }
}
