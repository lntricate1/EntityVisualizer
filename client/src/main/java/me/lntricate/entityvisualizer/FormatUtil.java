package me.lntricate.entityvisualizer;

import me.lntricate.entityvisualizer.malilib.config.options.EConfigString;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;

public class FormatUtil
{
  public static record Variable<T>(String key, String defaultFormat, T value)
  {
    public String apply(String text, int i)
    {
      text = text.replace("%" + key + "$", "\0" + i + "$");
      return text.replace("%" + key, "\0" + i + "$" + defaultFormat);
    }
  }

  public static <T> Variable<T> var(String key, String defaultFormat, T value)
  {
    return new Variable<T>(key, defaultFormat, value);
  }

  public static MutableComponent format(EConfigString config, Variable<?>... vars)
  {
    try
    {
      String text = ("[\"\"," + config.getStringValue() + "]")
        .replace("#black",        "#000000")
        .replace("#dark_red",     "#AA0000")
        .replace("#dark_green",   "#00AA00")
        .replace("#gold",         "#FFAA00")
        .replace("#dark_blue",    "#0000AA")
        .replace("#dark_purple",  "#AA00AA")
        .replace("#dark_aqua",    "#00AAAA")
        .replace("#gray",         "#AAAAAA")

        .replace("#dark_gray",    "#555555")
        .replace("#red",          "#FF5555")
        .replace("#green",        "#55FF55")
        .replace("#yellow",       "#FFFF55")
        .replace("#blue",         "#5555FF")
        .replace("#light_purple", "#FF55FF")
        .replace("#aqua",         "#55FFFF")
        .replace("#white",        "#FFFFFF");

      text = text.replaceAll("(#[a-fA-F\\d]{6,8})(\".*?(?<!\\\\)\")",
        "{\"color\":\"$1\",\"text\":$2}");

      Object[] values = new Object[vars.length];
      int i = 0;
      for(Variable<?> var : vars)
      {
        values[i] = var.value;
        text = var.apply(text, ++i);
      }
      text = text
        .replace("%", "§")
        .replace("\0", "%");
      text = String.format(text, values);

      return Component.Serializer.fromJsonLenient(text.replace("§", "%"));
    }
    catch(Exception e)
    {
      return new TextComponent("Failed to format - Invalid JSON");
    }
  }
}
