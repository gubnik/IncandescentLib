# Item Interfaces
Incandescent Lib provides a set of interfaces for Minecraft's items, allowing to extend
their functionality beyond regular Forge capabilities without falling back on Mixins.

## List of interfaces
- [IContainerItem](IContainerItem.java) provides a predefined set of functions to automate the creation
of items with internal storage, akin to bundles or shulker boxes.
- [ICustomSwingItem](ICustomSwingItem.java) allows for overriding default item swing animation via overriding
functions. Custom animation is understood to be but a function that takes in common parameters and performs a
set of transforms on the model.
- [IExtensibleTooltipItem](IExtensibleTooltipItem.java) provides a predefined set of function to automatically
create an extensible list of tooltip lines gathered from the translation.
- [IGradientName](IGradientNameItem.java) allows for an item to have a gradient coloring on its displayed 
inventory name. Custom coloring is defined as a function of ARGB integer from time.
- [IDefaultAttributesItem](IDefaultAttributesItem.java) allows for the definition of item's "default" attribute modifiers, which will be displayed in a fashion similar to
how default attack damage and speed are displayed on tools.
- `deprecated: use IDefaultAttributesItem` [INotStupidTooltipItem](INotStupidTooltipItem.java)
allows for the definition of item's "default" attribute modifiers, which will be displayed in a fashion similar to
how default attack damage and speed are displayed on tools. This was deprecated in favour of more simple
`IDefaultAttributesItem` which uses less specific algorithm to go about flagging attribute modifiers as default.

## Use considerations
Of these interfaces, 2 provide a functionality which ascribes a cautious usage: `INotStupidTooltipItem` and
`IGradientNameItem`, the reason being the fact that their provided functionality is based on Mixins.
This is especially important for `INotStupidTooltipItem`, since its mixin is done in a particularly awful manner,
and may or may not break in conjunction with other mixins providing similar functionality.