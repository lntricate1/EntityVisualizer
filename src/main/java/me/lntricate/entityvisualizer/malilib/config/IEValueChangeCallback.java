package me.lntricate.entityvisualizer.malilib.config;

public interface IEValueChangeCallback<T extends IEConfigBase>
{
  void onValueChanged(T config);
}
