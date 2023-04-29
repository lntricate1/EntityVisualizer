package me.lntricate.entityvisualizer.malilib.config;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonElement;

public interface IEConfigBase
{
  public EConfigType getType();
  public String getName();
  @Nullable public String getComment();
  public JsonElement getAsJsonElement();
  public void setValueFromJsonElement(JsonElement element);

  default String getPrettyName()
  {
    return this.getName();
  }

  default String getConfigGuiDisplayName()
  {
    return this.getName();
  }
}
