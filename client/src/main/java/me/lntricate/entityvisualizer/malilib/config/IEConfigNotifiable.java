package me.lntricate.entityvisualizer.malilib.config;

public interface IEConfigNotifiable<T extends IEConfigBase>
{
  public void onValueChanged();
  public void setValueChangeCallback(IEValueChangeCallback<T> callback);
}
