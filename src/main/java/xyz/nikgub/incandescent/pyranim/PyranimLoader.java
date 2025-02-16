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

package xyz.nikgub.incandescent.pyranim;

import java.io.*;
import java.net.URL;
import java.util.LinkedList;
import java.util.Queue;

/**
 * One-time use loader to provide the contents of {@code .pyranim} file
 *
 * @see PyranimParser
 */
public class PyranimLoader
{
    /**
     * Defines if the loader was previously used
     */
    private boolean wasAccessed = false;

    /**
     * Collected lines of the file
     */
    private final Queue<String> lines = new LinkedList<>();

    /**
     * Constructs the object for the file {@code loc}
     *
     * @param loc {@code String} name of the file to be processed
     */
    public PyranimLoader (String loc)
    {
        try
        {
            load(loc);
        } catch (FileNotFoundException e)
        {
            throw new RuntimeException("File load failed for file " + loc, e);
        }
    }


    /**
     * '
     * Loads and stores the contents of the file {@code loc}
     *
     * @param loc {@code String} name of the file to be processed
     * @throws FileNotFoundException if the file was missing or could not be opened
     */
    private void load (String loc) throws FileNotFoundException
    {
        final URL fileUrl = this.getClass().getClassLoader().getResource(loc);
        if (fileUrl == null)
        {
            throw new FileNotFoundException("Unable to find file " + loc);
        }
        try (InputStream inputStream = fileUrl.openStream())
        {
            final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null)
            {
                lines.add(line);
            }
        } catch (IOException e)
        {
            throw new FileNotFoundException("Unable to open file " + loc);
        }
    }

    /**
     * Getter for {@link PyranimLoader#lines}
     *
     * @return {@link PyranimLoader#lines}
     * @throws IllegalStateException if the loader was previously accessed
     */
    public Queue<String> getLines ()
    {
        if (wasAccessed)
        {
            throw new IllegalStateException("PyranimLoader objects must not be reused for multiple parsings");
        }
        wasAccessed = true;
        return lines;
    }
}
