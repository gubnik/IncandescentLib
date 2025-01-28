package xyz.nikgub.incandescent.pyranim.interfaces;

@FunctionalInterface
public interface ComponentProvider
{
    LexerComponent get (String representation);
}
