package xyz.nikgub.incandescent.itemgen.interfaces;

@FunctionalInterface
public interface IConverter<FT, TT>
{
    TT convert (FT fromVal);
}
