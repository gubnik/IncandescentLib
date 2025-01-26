package xyz.nikgub.incandescent.pyranim;

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PyranimParser
{
    private boolean doneRunning = false;
    private final PyranimLoader loader;

    private final Map<String, AnimationChannel.Interpolation> interpolationMap = new HashMap<>(
        Map.of(
            "catmullrom", AnimationChannel.Interpolations.CATMULLROM,
            "linear", AnimationChannel.Interpolations.LINEAR
        )
    );

    private static final Pattern DIRECTIVE_PATTERN = Pattern.compile(PyranimFormat.DIRECTIVE_DECLARATION_REGEX);
    private static final Pattern PART_PATTERN = Pattern.compile(PyranimFormat.PART_DECLARATION_REGEX);
    private static final Pattern INSTRUCTION_PATTERN = Pattern.compile(PyranimFormat.INSTRUCTION_LINE_REGEX);

    private final Map<String, AnimationPartInfo> partsInfo = new HashMap<>();
    private State state = State.HEADER;
    private float duration = -1;
    private boolean doLoop = false;
    private String currentPart = "";
    private AnimationChannel.Interpolation currentInterpolation = null;
    private float currentTime = -1;

    public PyranimParser (PyranimLoader loader)
    {
        this.loader = loader;
    }

    public PyranimLoader getLoader ()
    {
        return loader;
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

    public void defineInterpolation (String s, AnimationChannel.Interpolation interpolation)
    {
        interpolationMap.putIfAbsent(s, interpolation);
    }

    public AnimationDefinition parse ()
    {
        if (doneRunning)
        {
            throw new IllegalStateException("PyranimParser " + this + " cannot be reused");
        }
        final Queue<String> lines = new LinkedList<>(loader.getLines());
        int i = 0;
        while (!lines.isEmpty())
        {
            i++;
            final String line = lines.poll();
            PyranimFormat.LineType lineType = PyranimFormat.LineType.match(line);
            switch (lineType)
            {
                case WRONG -> throw new PyranimParserException(this, i);
                case DIRECTIVE -> handleDirective(line, i);
                case PART -> handlePart(line, i);
                case INSTRUCTION -> handleInstruction(line, i);
            }
        }
        final AnimationDefinition.Builder builder = bakeAnimations();
        AnimationDefinition definition = builder.build();
        doneRunning = true;
        return definition;
    }

    private void handleDirective (String line, int i)
    {
        Matcher matcher = DIRECTIVE_PATTERN.matcher(line);
        if (!matcher.matches())
        {
            throw new PyranimParserException(this, i, "Mystical directive failed to be parsed");
        }
        PyranimFormat.Directive directive = PyranimFormat.Directive.match(matcher.group(1));
        var arg = directive.getArgumentPolicy().handle(this, matcher.group(2));
        if (directive.getAppropriateState() != state)
        {
            throw new PyranimParserException(this, i, "Directive" + directive.getRepresentation() + " must be in " + directive.getAppropriateState());
        }
        switch (directive)
        {
            case DURATION -> duration = (Float) arg;
            case LOOPING -> doLoop = true;
            case AT ->
            {
                AnimationPartInfo partInfo = partsInfo.get(currentPart);
                if (partInfo == null)
                {
                    throw new PyranimParserException(this, i, ".at directive for a non-existent part");
                }
                currentTime = (Float) arg;
            }
            case INTERPOLATION -> currentInterpolation = (AnimationChannel.Interpolation) arg;
        }
    }

    private void handlePart (String line, int i)
    {
        if (state != State.HEADER && state != State.PART_BODY)
        {
            throw new PyranimParserException("File " + loader.getFilename() + " cannot be parsed at line " + i + ": unexpected part declaration");
        }
        state = State.PART_HEADER;
        final Matcher matcher = PART_PATTERN.matcher(line);
        if (!matcher.matches())
        {
            throw new PyranimParserException(this, i, "Bad part declaration");
        }
        final String partName = matcher.group(1);
        partsInfo.putIfAbsent(partName, new AnimationPartInfo());
        currentPart = partName;
    }

    private void handleInstruction (String line, int i)
    {
        if (state != State.PART_HEADER && state != State.PART_BODY)
        {
            throw new PyranimParserException(this, i, "Unexpected instruction");
        }
        state = State.PART_BODY;
        final var animationInfo = partsInfo.get(currentPart);
        if (animationInfo == null)
        {
            throw new PyranimParserException(this, i, "Instruction for a non-existent part");
        }
        final AnimationIR animationIR = parseInstructionLine(line, i);
        animationInfo.addAnimation(currentTime, animationIR);
    }

    private AnimationIR parseInstructionLine (String line, int i)
    {
        final Matcher matcher = INSTRUCTION_PATTERN.matcher(line);
        if (!matcher.matches())
        {
            throw new PyranimParserException(this, i, "Mystical instruction failed to be parsed");
        }
        final String instruction = matcher.group(1);
        final float xVal = Float.parseFloat(matcher.group(2));
        final float yVal = Float.parseFloat(matcher.group(5));
        final float zVal = Float.parseFloat(matcher.group(8));
        return new AnimationIR(PyranimFormat.Instruction.translate(instruction), xVal, yVal, zVal, currentInterpolation);
    }

    private AnimationDefinition.@NotNull Builder bakeAnimations ()
    {
        if (duration < 0)
        {
            throw new PyranimParserException("File " + loader.getFilename() + " cannot be parsed: duration not defined or defined as a negative number");
        }
        final AnimationDefinition.Builder builder = AnimationDefinition.Builder.withLength(duration);
        if (doLoop)
        {
            builder.looping();
        }
        for (var entry : partsInfo.entrySet())
        {
            final String boneName = entry.getKey();
            final AnimationPartInfo animationPartInfo = entry.getValue();
            List<Keyframe> mov = animationPartInfo.getMov();
            for (var keyframe : mov)
            {
                builder.addAnimation(boneName, new AnimationChannel(AnimationChannel.Targets.POSITION, keyframe));
            }
            List<Keyframe> rot = animationPartInfo.getRot();
            for (var keyframe : rot)
            {
                builder.addAnimation(boneName, new AnimationChannel(AnimationChannel.Targets.ROTATION, keyframe));
            }
            List<Keyframe> scl = animationPartInfo.getScl();
            for (var keyframe : scl)
            {
                builder.addAnimation(boneName, new AnimationChannel(AnimationChannel.Targets.SCALE, keyframe));
            }
        }
        return builder;
    }

    enum State
    {
        HEADER,
        PART_HEADER,
        PART_BODY
    }
}