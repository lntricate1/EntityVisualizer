package me.lntricate.entityvisualizer.malilib.config.options;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import fi.dy.masa.malilib.gui.interfaces.IDialogHandler;
import fi.dy.masa.malilib.gui.interfaces.IKeybindConfigGui;
import fi.dy.masa.malilib.gui.widgets.WidgetListConfigOptionsBase;
import fi.dy.masa.malilib.util.restrictions.UsageRestriction.ListType;
import me.lntricate.entityvisualizer.malilib.config.IEConfigWidgetable;
import me.lntricate.entityvisualizer.malilib.widgets.EWidgetConfigOption;

public class EConfigWBList extends EConfigMulti
{
  public EConfigWBList(String name, ListType defaultType, ImmutableList<String> defaultWhitelist, ImmutableList<String> defaultBlacklist, String comment, String prettyName)
  {
    super(name, comment, prettyName, List.of(
      new EConfigOptionList("listType", defaultType, "List type"),
      new EConfigStringList("whitelist", defaultWhitelist, comment),
      new EConfigStringList("blacklist", defaultBlacklist, comment)
    ));
  }

  @Override
  public void onValueChanged()
  {
    super.onValueChanged();
    remakeWidgets();
  }

  public boolean test(String string)
  {
    ListType type = (ListType)getConfig(0).getValue();
    return switch(type)
    {
      case WHITELIST -> ((EConfigStringList)getConfig(1)).getValue().contains(string);
      case BLACKLIST -> !((EConfigStringList)getConfig(2)).getValue().contains(string);
      case NONE -> true;
    };
  }

  @Override
  public void createWidgets(EWidgetConfigOption widgetConfigOption, WidgetListConfigOptionsBase<?, ?> parent, int x, int y, int w, int h, IKeybindConfigGui configGui, @Nullable IDialogHandler dialogHandler)
  {
    this.widgetConfigOption = widgetConfigOption;
    switch((ListType)getConfig(0).getValue())
    {
      case NONE:
        break;
      case WHITELIST:
        w /= 2;
        ((IEConfigWidgetable)getConfig(1)).createWidgets(widgetConfigOption, parent, x + w, y, w, h, configGui, dialogHandler);
        break;
      case BLACKLIST:
        w /= 2;
        ((IEConfigWidgetable)getConfig(2)).createWidgets(widgetConfigOption, parent, x + w, y, w, h, configGui, dialogHandler);
        break;
    }
    ((IEConfigWidgetable)getConfig(0)).createWidgets(widgetConfigOption, parent, x, y, w, h, configGui, dialogHandler);
  }
}
