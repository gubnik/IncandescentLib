package xyz.nikgub.incandescent.itemgen_config.interfaces;

@FunctionalInterface
public interface IConverter<FT, TT>
{
    TT convert (FT fromVal);
}
