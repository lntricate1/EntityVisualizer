package me.lntricate.entityvisualizer.malilib.widgets;

import fi.dy.masa.malilib.config.IConfigResettable;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.button.IButtonActionListener;
import fi.dy.masa.malilib.util.StringUtils;

public class ResetButton extends ButtonGeneric
{
  private final IConfigResettable config;

  public ResetButton(int x, int y, IConfigResettable config, EWidgetConfigOption widgetConfigOption)
  {
    super(x, y, -1, 20, StringUtils.translate("malilib.gui.button.reset.caps"), null, new String[]{});
    this.config = config;
    textCentered = true;
    setActionListener(new ListenerButtonReset(config, widgetConfigOption));
    setEnabled(config.isModified());
  }

  public void update()
  {
    setEnabled(config.isModified());
  }

  private static final class ListenerButtonReset implements IButtonActionListener
  {
    private final IConfigResettable config;
  private final EWidgetConfigOption widgetConfigOption;

    public ListenerButtonReset(IConfigResettable config, EWidgetConfigOption widgetConfigOption)
    {
      this.config = config;
      this.widgetConfigOption = widgetConfigOption;
    }

    @Override
    public void actionPerformedWithButton(ButtonBase button, int mouseButton)
    {
      config.resetToDefault();
      widgetConfigOption.remakeWidgets();
    }
  }
}
