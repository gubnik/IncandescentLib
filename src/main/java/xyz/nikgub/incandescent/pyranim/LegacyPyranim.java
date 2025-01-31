package xyz.nikgub.incandescent.pyranim;

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;
import org.joml.Vector3f;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <h2>Class to convert .pyranim format files into {@link AnimationDefinition}</h2>
 * <p>.pyranim describes an animation and consists of total animation length and model parts' keyframes.<p/>
 * <h2>Syntax of .pyranim</h2>
 * NOTE: in the following text %something% represents either a number or a string
 * <h3>Total animation length</h3>
 * <p>Announced as !_%somefloatnumber%_!, must be a first line</p>
 * <h3>Model parts</h3>
 * <p>Announced with $_%somemodelpart%_$, must be terminated with $$ at the end</p>
 * <h3>Keyframes</h3>
 * <p>Consist of 3 parts in 1 line: type-defining character (T, R or S), moment in time in which the keyframe is placed (@%somefloatnumber%)
 * and a vector (a b c), where a, b and c are floats. All parts must be written in one line back-to-back without any symbols in between</p>
 * <h3>Example:</h3>
 * <p>!_0.5_!</p>
 * <p>$_left_arm_$</p>
 * <p>T@0.0@(0 0 0)</p>
 * <p>T@0.5@(0 2 0)C</p>
 * <p>R@0.0@(0 0 0)</p>
 * <p>R@0.5@(90 90 90)C</p>
 * <p>$$</p>
 *
 * @since 1.4
 * @deprecated Use {@link PyranimParser} instead. This format was deprecated in favour of more concise syntax
 * as defined in {@link PyranimLexer} and must only be used to provide backwards compatibility with already existing
 * animations.
 */
@Deprecated(forRemoval = true, since = "1.4.0")
public class LegacyPyranim
{
    private static final Map<Character, AnimationChannel.Interpolation> INTERPOLATION_MAP = new HashMap<>(
        Map.of(
            'C', AnimationChannel.Interpolations.CATMULLROM,
            'L', AnimationChannel.Interpolations.LINEAR
        )
    );

    /**
     * Creates an AnimationDefinition from .pyranim file for an entity.
     *
     * @param location Location of pyranim from resources folder
     * @return AnimationDefinition created from a provided .pyranim
     */
    public static AnimationDefinition ofEntity (String location)
    {
        return new LegacyPyranim(location).createEntity();
    }

    /**
     * Creates an AnimationDefinition from .pyranim file for a player.<p>
     * Fails if .pyranim defines animations for a part not present in humanoid model
     *
     * @param location Location of pyranim from resources folder
     * @return AnimationDefinition created from a provided .pyranim
     */
    public static AnimationDefinition ofPlayer (String location)
    {
        return new LegacyPyranim(location).createPlayer();
    }

    private final List<String> contents;
    private Map<String, List<AnimationChannel>> map = new HashMap<>();
    private float animLength = 0f;

    private LegacyPyranim (String location)
    {
        if (!location.endsWith(".pyranim")) location += ".pyranim";
        float t = 0f;
        this.contents = new ArrayList<>();
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        try (InputStream inputStream = classloader.getResourceAsStream(location))
        {
            if (inputStream == null) return;
            InputStreamReader reader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(reader);
            for (String s; (s = bufferedReader.readLine()) != null; )
            {
                if (s.matches("!_[0-9]+(.[0-9]+)?_!"))
                {
                    t = Float.parseFloat(s.substring(s.indexOf("!_") + 2, s.indexOf("_!")));
                    s = "";
                }
                if (!s.equals("")) this.contents.add(s);
            }
            this.animLength = t;

        } catch (Exception e)
        {
            throw new RuntimeException("Failed to locate a file '" + location + "' in resources");
        }
    }

    private static Vector3f parseVector (String s)
    {
        Vector3f vector3f = new Vector3f();
        String nb = s.substring(s.indexOf('(') + 1, s.indexOf(')'));
        vector3f.x = Float.parseFloat(nb.substring(0, nb.indexOf(' ')));
        nb = nb.substring(nb.indexOf(" ") + 1);
        vector3f.y = Float.parseFloat(nb.substring(0, nb.indexOf(' ')));
        nb = nb.substring(nb.indexOf(" ") + 1);
        vector3f.z = Float.parseFloat(nb);
        return vector3f;
    }

    private void toMap ()
    {
        final Map<String, List<AnimationChannel>> resMap = new HashMap<>();
        final List<Character> interpolationSymbols = INTERPOLATION_MAP.keySet().stream().toList();
        final String interpolationRegex = interpolationSymbols.stream().map(String::valueOf).collect(Collectors.joining());
        final String finalRegex = String.format("[RST]@[0-9]+(.[0-9]+)?@[(]-?[0-9]+(.[0-9]+)? -?[0-9]+(.[0-9]+)? -?[0-9]+(.[0-9]+)?[)][%s]?", interpolationRegex);
        Vector3f vector3f;
        List<Keyframe> rotationKeyframes = new ArrayList<>();
        List<Keyframe> translationKeyframes = new ArrayList<>();
        List<Keyframe> scaleKeyframes = new ArrayList<>();
        Keyframe[] kfR, kfT, kfS;
        String currKey = "";
        for (String line : this.contents)
        {
            if (line.equals("$$"))
            {
                kfR = new Keyframe[rotationKeyframes.size()];
                kfT = new Keyframe[translationKeyframes.size()];
                kfS = new Keyframe[scaleKeyframes.size()];
                kfR = rotationKeyframes.toArray(kfR);
                kfT = translationKeyframes.toArray(kfT);
                kfS = scaleKeyframes.toArray(kfS);
                if (kfT.length > 0)
                    resMap.get(currKey).add(new AnimationChannel(AnimationChannel.Targets.POSITION, kfT));
                if (kfR.length > 0)
                    resMap.get(currKey).add(new AnimationChannel(AnimationChannel.Targets.ROTATION, kfR));
                if (kfS.length > 0)
                    resMap.get(currKey).add(new AnimationChannel(AnimationChannel.Targets.SCALE, kfS));
                rotationKeyframes = new ArrayList<>();
                translationKeyframes = new ArrayList<>();
                scaleKeyframes = new ArrayList<>();
                continue;
            }
            if (line.matches(finalRegex))
            {
                vector3f = parseVector(line.substring(line.indexOf("@(") + 2, line.indexOf(")")) + ')');
                float moment = Float.parseFloat(line.substring(line.indexOf("T@") + 3, line.indexOf("@(")));
                AnimationChannel.Interpolation interpolation = INTERPOLATION_MAP.get(line.charAt(line.length() - 1));
                switch (line.charAt(0))
                {
                    case ('T') -> translationKeyframes.add(new Keyframe(
                        moment,
                        KeyframeAnimations.posVec(vector3f.x, vector3f.y, vector3f.z),
                        interpolation
                    ));
                    case ('R') -> rotationKeyframes.add(new Keyframe(
                        moment,
                        KeyframeAnimations.degreeVec(vector3f.x, vector3f.y, vector3f.z),
                        interpolation
                    ));
                    case ('S') -> scaleKeyframes.add(new Keyframe(
                        moment,
                        KeyframeAnimations.scaleVec(vector3f.x, vector3f.y, vector3f.z),
                        interpolation
                    ));
                }
                continue;
            }
            if (line.matches("[$]_[a-zA-Z_]+_[$]"))
            {
                currKey = line.substring(line.indexOf("$_") + 2, line.indexOf("_$"));
                resMap.put(currKey, new ArrayList<>());
            }
        }
        this.map = resMap;
    }

    private AnimationDefinition createEntity ()
    {
        this.toMap();
        AnimationDefinition.Builder builder = AnimationDefinition.Builder.withLength(this.animLength);
        for (String part : map.keySet())
        {
            for (AnimationChannel channel : map.get(part))
            {
                builder.addAnimation(part, channel);
            }
        }
        return builder.build();
    }

    private AnimationDefinition createPlayer ()
    {
        this.toMap();
        AnimationDefinition.Builder builder = AnimationDefinition.Builder.withLength(this.animLength);
        for (String part : map.keySet())
        {
            if (!acceptablePart(part))
                throw new RuntimeException("Unable to create player animation from .pyranim. Cause: part '" + part + "' is not a part of humanoid model");
            for (AnimationChannel channel : map.get(part))
            {
                builder.addAnimation(part, channel);
            }
        }
        return builder.build();
    }

    private static boolean acceptablePart (String key)
    {
        return switch (key)
        {
            case ("body"), ("right_leg"), ("left_arm"), ("left_leg"), ("right_arm"), ("hat"), ("head") -> true;
            default -> false;
        };
    }

}
