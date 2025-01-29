# Pyranim format

Representation of Minecraft's `AnimationDefinition` in a humanly-readable yet easily generated format.

## Syntax

`.pyranim` recognizes following expressions:

- Global directives - define metadata for entire animation
-
    - `.drtion <value>`: sets the total length of the animation as `value` seconds.
-
    - `.doloop`: sets the looping flag to true, making the animation repeat itself after it ends.
- Part declaration
- `<part_name>:`: declares the start of part header section for part `part_name`
- Local directives - define information for a model part until overridden
-
    - `>attime <value>`: sets the current timestamp at `value` second mark for subsequent transforms
-
    - `>intrpl <value>`: sets the interpolation for subsequent transforms as `value`, defined within the parser builder.
- Instructions - define transformations for a model part
-
    - `mov <x, y, z>`: applies the translation transform with values `x, y, z`
-
    - `rot <x, y, z>`: applies the rotation transform with values `x, y, z`
-
    - `scl <x, y, z>`: applies the scale transform with values `x, y, z`

## Specification

`.pyranim` animations consist of 3 main sections:

- Global header: section for defining metadata for the animation. Only global directives are allowed in this section.
- Part header: section for specifying information about the part. Starts either with part declaration, or local
  directives, if the part was previously declared.
- Part instructions: section for applying transforms for the part, using information defined in part header.
  Only instructions are allowed in this section.
- Comment: in-line comments are to be started with `;` symbol. Empty lines are considered comment lines and are
  skipped during parsing.

If the order of transitioning between sections was broken, the exception is thrown.

### Example

```pyranim
.drtion 1.0                ; sets the duration to 1 second
.doloop                    ; makes the animation loop at the end

left_arm:                  ; for model part "left_arm"
    >attime 0              ; sets the current timestamp for following transforms to 0
    >intrpl "catmullrom"   ; uses interpolation "catmullrom" defined in the parser builder
    mov 0, 1, 0            ; translate 0 by X, 1 by Y and 0 by Z
    rot 2, 3, 4            ; rotate by 2 degrees around X axis, 3 degrees around Y axis and 4 degrees around Z axis
    scl 5, 6, 7            ; scale by a factor of 5 on X axis, 6 on Y axis and 7 on Z axis
    >attime 0.4
    >intrpl "linear"
    mov 1, 2, 3
    rot 5, 6, 7
    scl 6, 5, 4
```

## Parser usage

`PyranimParser` is responsible for parsing `.pyranim` animations, and may be reused for multiple files.
It is to be created via `PyranimParser.Builder`, where user-defined interpolations can be provided.
Animations are parsed to Minecraft's `AnimationDefinition` via `PyranimParser.parse(PyranimLoader)` method.

### PyranimLoader

`PyranimLoader` is a class responsible for loading and storing the contents of `.pyranim` files from `resources`
folder of the mod.
Objects of this class are disposable, and store only file's provided name and the contents of the file in a
`LinkedList<String>`.

### Example

```java
private final PyranimParser parser = new PyranimParser.Builder().build();
private final AnimationDefinition ASM = parser.parse(new PyranimLoader("asm.pyranim"));
```

## Parsing process

Once `parse` method was invoked on a `PyranimParser` object, the lines of the provided loader are sequentially
pulled from the queue of lines, and are being matched against known line type regexes,
defined in `PyranimLexer.LineType`. Then, the line is being tokenized into an actual `LexerComponent` object,
and `LexerComponent.handle(PyranimParser, AnimationIR, Matcher)` is invoked on said object to change the current state
of parser and apply changes to `AnimationIR`.

### AnimationIR

`AnimationIR` is an object which represents unbaked animation with unknown data, and its contents are being baked into
`AnimationDefinition.Builder` at the end of the parsing process. This object is handled automatically, and should not
be manually changed to avoid malformed animations.

### LexerComponent

An interface defining `handle` method. Objects of this interface can be tokenized during the parsing process.
By default, `LexerComponent` is implemented for:

- `PyranimLexer.GlobalDirective`: enum of global directives
- `PyranimLexer.LocalDirective`: enum of local directive
- `PyranimLexer.PartDeclaration`: class representation of part declaration
- `PyranimLexer.Instruction`: enum of instructions

## Error handling

`LexerComponent.handle(PyranimParser, AnimationIR, Matcher)` throws a `PyranimLexerException(AnimationIR)` exception
if the component was misplaced or improperly defined in the file.
`PyranimParser.parse(PyranimLoader)` wraps this exception in `PyranimParserException`, which contains the actual line
at which the error occurred, as well as its number and type.
