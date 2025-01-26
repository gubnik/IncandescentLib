package xyz.nikgub.incandescent.pyranim;

import java.io.*;
import java.net.URL;
import java.util.LinkedList;
import java.util.Queue;

public class PyranimLoader
{
    private final String filename;
    private final Queue<String> lines = new LinkedList<>();

    public PyranimLoader (String loc)
    {
        filename = loc;
        try
        {
            load(loc);
        } catch (FileNotFoundException e)
        {
            throw new RuntimeException("File load failed for file " + loc, e);
        }
    }

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

    public String getFilename ()
    {
        return filename;
    }

    public Queue<String> getLines ()
    {
        return lines;
    }
}
