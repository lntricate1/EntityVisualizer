package me.lntricate.entityvisualizer.malilib.config;

import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.IConfigResettable;
import me.lntricate.entityvisualizer.malilib.config.options.EConfigMulti;

public interface IEConfigValueGettable<T> extends IConfigBase, IConfigResettable
{
  public T getValue();
  public void setValue(Object value);
  public void setParent(EConfigMulti parent);
}
