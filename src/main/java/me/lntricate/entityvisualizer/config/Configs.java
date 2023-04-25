package me.lntricate.entityvisualizer.config;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import fi.dy.masa.malilib.config.ConfigType;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.IConfigHandler;
import fi.dy.masa.malilib.config.IConfigInteger;
import fi.dy.masa.malilib.config.IHotkeyTogglable;
import fi.dy.masa.malilib.config.options.ConfigBooleanHotkeyed;
import fi.dy.masa.malilib.config.options.ConfigHotkey;
import fi.dy.masa.malilib.hotkeys.IHotkey;
import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.KeybindMulti;
import fi.dy.masa.malilib.hotkeys.KeybindSettings;
import fi.dy.masa.malilib.util.StringUtils;
import fi.dy.masa.malilib.hotkeys.KeyCallbackToggleBoolean;
import me.lntricate.entityvisualizer.EntityVisualizerMod;
import me.lntricate.entityvisualizer.config.options.BooleanHotkeyGuiWrapper;
import me.lntricate.entityvisualizer.config.options.ConfigGenericRenderer;

public class Configs implements IConfigHandler
{
  @Override
  public void load()
  {}

  @Override
  public void save()
  {}

  public static class Generic
  {
    public static final ConfigHotkey OPEN_CONFIG_GUI = new ConfigHotkey("openConfigGui", "V,C", "A hotkey to open the in-game Config GUI");
    public static final ConfigBooleanHotkeyed TEST = new ConfigBooleanHotkeyed("test", false, "", "TEST COMMENT");

    public static final ImmutableList<IConfigBase> OPTIONS = ImmutableList.of
    (
      OPEN_CONFIG_GUI,
      TEST
    );

    public static final ImmutableList<IHotkey> HOTKEY_LIST = ImmutableList.of
    (
      OPEN_CONFIG_GUI
    );
  }

  public enum Renderers// implements IConfigInteger, IHotkeyTogglable
  {
    EXPLOSIONS("explosions", "Shows boxes at explosions", "Explosions");

    // private final String name, prettyName, comment;
    // private final IKeybind keybind;
    //
    // private boolean booleanValue;
    // private final boolean defaultBooleanValue;
    // private int integerValue;
    // private final int defaultIntegerValue;

    public final ConfigGenericRenderer config;

    private Renderers(String name, String comment, String prettyName)
    {
      this.config = new ConfigGenericRenderer(name, comment, prettyName);
    }

    public static ImmutableList<IConfigBase> getOptions()
    {
      return ImmutableList.copyOf(ImmutableList.copyOf(values()).stream().map((renderer) -> renderer.config).toList());
    }

    public static ImmutableList<IHotkey> getHotkeys()
    {
      return ImmutableList.copyOf(ImmutableList.copyOf(values()).stream().map((renderer) -> renderer.config).toList());
    }

    // private Renderers(String name, String comment, String prettyName)
    // {
    //   this.name = name;
    //   this.comment = comment;
    //   this.prettyName = prettyName;
    //
    //   keybind = KeybindMulti.fromStorageString("", KeybindSettings.DEFAULT);
    //   keybind.setCallback(new KeyCallbackToggleBoolean(this));
    //
    //   booleanValue = false;
    //   defaultBooleanValue = false;
    //   integerValue = 100;
    //   defaultIntegerValue = 100;
    // }

  //   public static ImmutableList<IConfigBase> getOptions()
  //   {
  //     List<IConfigBase> list = new ArrayList<>();
  //     list.addAll(ImmutableList.copyOf(values()).stream().
  //       // map((config) -> new BooleanHotkeyGuiWrapper(config.getName(), config, config.getKeybind())).toList());
  //       // map((config) -> new ConfigGenericRenderer(config.getName(), config.getComment(), config.getPrettyName())).toList());
  //       map((config) -> new ConfigBooleanHotkeyed(config.getName(), config.getDefaultBooleanValue(), "", config.getComment())).toList());
  //     return ImmutableList.copyOf(list);
  //   }
  //
  //   @Override public ConfigType getType() {return ConfigType.HOTKEY;}
  //
  //   @Override public String getName() {return name;}
  //
  //   @Override public String getPrettyName() {return prettyName;}
  //
  //   @Override public String getStringValue() {return String.valueOf(booleanValue);}
  //
  //   @Override public String getDefaultStringValue() {return String.valueOf(defaultBooleanValue);}
  //
  //   @Override
  //   public String getComment()
  //   {
  //     String translated = StringUtils.translate("config.comment." + this.getName().toLowerCase());
  //     return translated == null ? comment : translated;
  //   }
  //
  //   @Override public boolean getBooleanValue() {return booleanValue;}
  //
  //   @Override public boolean getDefaultBooleanValue() {return defaultBooleanValue;}
  //
  //   @Override public void setBooleanValue(boolean value) {booleanValue = value;}
  //
  //   @Override public int getIntegerValue() {return integerValue;}
  //
  //   @Override public int getDefaultIntegerValue() {return defaultIntegerValue;}
  //
  //   @Override public void setIntegerValue(int value) {integerValue = value;}
  //
  //   @Override public int getMinIntegerValue() {return 0;}
  //
  //   @Override public int getMaxIntegerValue() {return Integer.MAX_VALUE;}
  //
  //   @Override public IKeybind getKeybind() {return keybind;}
  //
  //   @Override public boolean isModified() {return booleanValue != defaultBooleanValue || integerValue != defaultIntegerValue;}
  //
  //   @Override public void resetToDefault() {booleanValue = defaultBooleanValue; integerValue = defaultIntegerValue;}
  //
  //   @Override
  //   public void setValueFromString(String value)
  //   {
  //     try
  //     {
  //       booleanValue = Boolean.parseBoolean(value);
  //     }
  //     catch(Exception e)
  //     {
  //       EntityVisualizerMod.LOGGER.warn("[EntityVisualizer] Failed to read config value for {} from the JSON config", getName(), e);
  //     }
  //   }
  //
  //   @Override
  //   public void setValueFromJsonElement(JsonElement element)
  //   {
  //     try
  //     {
  //       if(element.isJsonPrimitive())
  //         booleanValue = element.getAsBoolean();
  //       else
  //        EntityVisualizerMod.LOGGER.warn("[EntityVisualizer] Failed to read config value for {} from the JSON config", getName());
  //     }
  //     catch(Exception e)
  //     {
  //       EntityVisualizerMod.LOGGER.warn("[EntityVisualizer] Failed to read config value for {} from the JSON config", getName(), e);
  //     }
  //   }
  //
  //   @Override
  //   public JsonElement getAsJsonElement()
  //   {
  //     return new JsonPrimitive(booleanValue);
  //   }
  }
}
