package me.lntricate.entityvisualizer.malilib.config.options;

import java.util.List;

import javax.annotation.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import fi.dy.masa.malilib.config.ConfigType;
import fi.dy.masa.malilib.config.options.ConfigBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.interfaces.IDialogHandler;
import fi.dy.masa.malilib.gui.interfaces.IKeybindConfigGui;
import fi.dy.masa.malilib.gui.widgets.WidgetListConfigOptionsBase;
import me.lntricate.entityvisualizer.EntityVisualizerMod;
import me.lntricate.entityvisualizer.malilib.config.EConfigType;
import me.lntricate.entityvisualizer.malilib.config.IEConfigValueGettable;
import me.lntricate.entityvisualizer.malilib.config.IEConfigWidgetable;
import me.lntricate.entityvisualizer.malilib.widgets.EWidgetConfigOption;

public class EConfigMulti extends ConfigBase<EConfigMulti> implements IEConfigWidgetable
{
  @Nullable protected EWidgetConfigOption widgetConfigOption;

  private final List<IEConfigValueGettable<?>> configs;

  public EConfigMulti(String name, String comment, List<IEConfigValueGettable<?>> configs)
  {
    this(name,comment, name, configs);
  }

  public EConfigMulti(String name, String comment, String prettyName, List<IEConfigValueGettable<?>> configs)
  {
    super(ConfigType.BOOLEAN, name, comment, prettyName);
    this.configs = configs;
    for(IEConfigValueGettable<?> config : configs)
      config.setParent(this);
  }

  @Override
  public EConfigType eGetType()
  {
    return EConfigType.MULTI;
  }

  // IEConfigMulti

  public int size()
  {
    return configs.size();
  }

  public IEConfigValueGettable<?> getConfig(int i)
  {
    return configs.get(i);
  }

  public Object getValue(int i)
  {
    return configs.get(i).getValue();
  }

  public void setValue(int i, Object value)
  {
    configs.get(i).setValue(value);
  }

  // IEConfigResettable overrides

  @Override public boolean isModified(){
    for(IEConfigValueGettable<?> config : configs)
      if(config.isModified())
        return true;
    return false;
  }

  @Override
  public void resetToDefault()
  {
    for(IEConfigValueGettable<?> config : configs)
      config.resetToDefault();
  }

  // JSON stuff

  @Override
  public JsonElement getAsJsonElement()
  {
    JsonObject object = new JsonObject();
    int i = 0;
    for(IEConfigValueGettable<?> config : configs)
    {
      object.add(Integer.toString(i), config.getAsJsonElement());
      i ++;
    }
    return object;
  }

  @Override
  public void setValueFromJsonElement(JsonElement element)
  {
    try
    {
      for(int i = 0; i < ((JsonObject)element).size(); i ++)
        configs.get(i).setValueFromJsonElement(((JsonObject)element).get(Integer.toString(i)));
    }
    catch(Exception e)
    {
      EntityVisualizerMod.LOGGER.warn("[EntityVisualizer] failed to set config value for '{}' from the JSON element '{}'", getName(), element);
    }
  }

  @Override
  public void createWidgets(EWidgetConfigOption widgetConfigOption, WidgetListConfigOptionsBase<?, ?> parent, int x, int y, int w, int h, IKeybindConfigGui configGui, @Nullable IDialogHandler dialogHandler)
  {
    this.widgetConfigOption = widgetConfigOption;
    w /= configs.size();
    for(IEConfigValueGettable<?> config : configs)
    {
      ((IEConfigWidgetable)config).createWidgets(widgetConfigOption, parent, x, y, w, h, configGui, dialogHandler);
      x += w;
    }
  }

  ButtonGeneric resetButton;

  @Override
  public void onValueChanged()
  {
    super.onValueChanged();
    if(resetButton != null)
      resetButton.setEnabled(isModified());
  }

  public void remakeWidgets()
  {
    if(widgetConfigOption != null)
      widgetConfigOption.remakeWidgets();
  }

  @Override
  public void createResetButton(EWidgetConfigOption widgetConfigOption, int x, int y, int w, int h)
  {
    resetButton = widgetConfigOption.createResetButton(x, y, this);
    widgetConfigOption.addWidgetPublic(resetButton);
  }

  @Override
  public void render(int x, int y, int w, int h)
  {
    w /= configs.size();
    for(IEConfigValueGettable<?> config : configs)
    {
      ((IEConfigWidgetable)config).render(x, y, w, h);
      x += w;
    }
  }
}
