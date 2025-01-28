package xyz.nikgub.incandescent.pyranim;

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class PyranimParser
{

    private final Map<String, AnimationChannel.Interpolation> interpolationMap;

    private PyranimParser (Map<String, AnimationChannel.Interpolation> interpolationMap)
    {
        this.interpolationMap = interpolationMap;
    }

    @NotNull
    public AnimationChannel.Interpolation getInterpolation (String s)
    {
        final String toSearch = s.replace("\"", "");
        var val = interpolationMap.get(toSearch);
        if (val == null)
        {
            throw new IllegalArgumentException("Undefined interpolation \"" + toSearch + "\"");
        }
        return val;
    }

    public AnimationDefinition parse (@NotNull PyranimLoader loader)
    {
        final AnimationIR animationIR = new AnimationIR();
        final Queue<String> lines = new LinkedList<>(loader.getLines());
        int i = 0;
        while (!lines.isEmpty())
        {
            i++;
            final String line = lines.poll();
            final PyranimLexer.LineType lineType = PyranimLexer.LineType.match(line);
            final LineContext context = new LineContext(lineType, line, i);
            try
            {
                animationIR.setCurrentState(lineType.handle(this, animationIR, context));
            } catch (PyranimLexerException e)
            {
                throw new PyranimParserException("Lexing failed at line of type: " + lineType, i, e);
            }
        }
        final AnimationDefinition.Builder builder = animationIR.bakeIntoBuilder();
        return builder.build();
    }

    public enum State
    {
        GLOBAL_HEADER,
        PART_HEADER,
        PART_INSTRUCTION
    }

    public static final class LineContext
    {
        private final PyranimLexer.LineType lineType;
        private final String line;
        private final int lineNum;

        private LineContext (PyranimLexer.LineType lineType, String line, int lineNum)
        {
            this.lineType = lineType;
            this.line = line;
            this.lineNum = lineNum;
        }

        public PyranimLexer.LineType lineType ()
        {
            return lineType;
        }

        public String line ()
        {
            return line;
        }

        public int lineNum ()
        {
            return lineNum;
        }

        @Override
        public boolean equals (Object obj)
        {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (LineContext) obj;
            return Objects.equals(this.lineType, that.lineType) &&
                Objects.equals(this.line, that.line) &&
                this.lineNum == that.lineNum;
        }

        @Override
        public int hashCode ()
        {
            return Objects.hash(lineType, line, lineNum);
        }

        @Override
        public String toString ()
        {
            return "LineContext[" +
                "lineType=" + lineType + ", " +
                "s=" + line + ", " +
                "lineNum=" + lineNum + ']';
        }


    }

    public static class Builder
    {
        private final Map<String, AnimationChannel.Interpolation> interpolationMap = new HashMap<>(
            Map.of(
                "catmullrom", AnimationChannel.Interpolations.CATMULLROM,
                "linear", AnimationChannel.Interpolations.LINEAR
            )
        );

        public Builder defineInterpolation (String name, AnimationChannel.Interpolation interpolation)
        {
            if (interpolationMap.putIfAbsent(name, interpolation) != null)
            {
                throw new IllegalArgumentException("Interpolation '" + name + "' cannot be redefined");
            }
            return this;
        }

        public PyranimParser build ()
        {
            return new PyranimParser(interpolationMap);
        }
    }
}