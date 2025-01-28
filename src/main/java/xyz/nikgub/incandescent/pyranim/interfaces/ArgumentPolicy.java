package xyz.nikgub.incandescent.pyranim.interfaces;

import org.jetbrains.annotations.Nullable;
import xyz.nikgub.incandescent.pyranim.PyranimParser;

@FunctionalInterface
public interface ArgumentPolicy<T>
{
    @Nullable
    T handle (PyranimParser parser, String arg);
}