package me.lntricate.entityvisualizer.malilib.config.options;

import org.jetbrains.annotations.Nullable;

import fi.dy.masa.malilib.config.IConfigResettable;
import fi.dy.masa.malilib.util.StringUtils;
import me.lntricate.entityvisualizer.malilib.config.EConfigType;
import me.lntricate.entityvisualizer.malilib.config.IEConfigBase;
import me.lntricate.entityvisualizer.malilib.config.IEConfigNotifiable;
import me.lntricate.entityvisualizer.malilib.config.IEValueChangeCallback;

public abstract class EConfigBase<T extends IEConfigBase> implements IEConfigBase, IConfigResettable, IEConfigNotifiable<T>
{
  private final EConfigType type;
  private final String name;
  private final String prettyName;
  private String comment;
  @Nullable private IEValueChangeCallback<T> callback;

  public EConfigBase(EConfigType type, String name, String comment)
  {
    this(type, name, comment, name);
  }

  public EConfigBase(EConfigType type, String name, String comment, String prettyName)
  {
    this.type = type;
    this.name = name;
    this.prettyName = prettyName;
    this.comment = comment;
  }

  @Override public EConfigType getType(){return this.type;}

  @Override public String getName(){return this.name;}

  @Override public String getPrettyName(){return StringUtils.translate(this.prettyName);}

  @Override @Nullable public String getComment(){return StringUtils.translate(this.comment);}

  @Override public void setValueChangeCallback(IEValueChangeCallback<T> callback){this.callback = callback;}

  @SuppressWarnings("unchecked")
  @Override
  public void onValueChanged()
  {
      if (this.callback != null)
      {
          this.callback.onValueChanged((T) this);
      }
  }

  public void setComment(String comment)
  {
      this.comment = comment;
  }
}
