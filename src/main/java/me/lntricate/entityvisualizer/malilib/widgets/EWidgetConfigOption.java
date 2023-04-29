package me.lntricate.entityvisualizer.malilib.widgets;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.IConfigResettable;
import fi.dy.masa.malilib.config.IConfigValue;
import fi.dy.masa.malilib.config.IStringRepresentable;
import fi.dy.masa.malilib.gui.GuiTextFieldGeneric;
import fi.dy.masa.malilib.gui.GuiConfigsBase.ConfigOptionWrapper;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.button.IButtonActionListener;
import fi.dy.masa.malilib.gui.interfaces.IConfigInfoProvider;
import fi.dy.masa.malilib.gui.interfaces.IKeybindConfigGui;
import fi.dy.masa.malilib.gui.interfaces.ITextFieldListener;
import fi.dy.masa.malilib.gui.widgets.WidgetBase;
import fi.dy.masa.malilib.gui.widgets.WidgetConfigOptionBase;
import fi.dy.masa.malilib.gui.widgets.WidgetHoverInfo;
import fi.dy.masa.malilib.gui.widgets.WidgetListConfigOptionsBase;
import fi.dy.masa.malilib.gui.wrappers.TextFieldWrapper;
import fi.dy.masa.malilib.render.RenderUtils;
import fi.dy.masa.malilib.util.KeyCodes;
import fi.dy.masa.malilib.util.StringUtils;
import me.lntricate.entityvisualizer.malilib.config.IEConfigWidgetable;

public class EWidgetConfigOption extends WidgetConfigOptionBase<ConfigOptionWrapper>
{
  protected final ConfigOptionWrapper wrapper;
  protected final IKeybindConfigGui host;
  private int x, y;
  private final int labelWidth, w, h;
  private List<TextFieldWrapper<? extends GuiTextFieldGeneric>> textFields = new ArrayList<>();

  public EWidgetConfigOption(int x, int y, int width, int height, int labelWidth, int configWidth, ConfigOptionWrapper wrapper, int listIndex, IKeybindConfigGui host, WidgetListConfigOptionsBase<?, ?> parent)
  {
    super(x, y, width, height, parent, wrapper, listIndex);
    this.host = host;
    this.wrapper = wrapper;
    this.labelWidth = labelWidth;
    this.x = x; this.y = y;
    w = configWidth; h = 20;
    addLabel(wrapper.getConfig());
    addConfigOption(configWidth, wrapper.getConfig());
  }

  // ONLY RUN ONCE
  private void addLabel(IConfigBase config)
  {
    y += 1;
    addLabel(x, y + 7, labelWidth, 8, 0xFFFFFFFF, config.getConfigGuiDisplayName());
    IConfigInfoProvider infoProvider = this.host.getHoverInfoProvider();

    String comment = null;
    if(infoProvider != null)
      comment = infoProvider.getHoverInfo(config);
    else
      comment = config.getComment();

    if(comment != null)
      addWidget(new WidgetHoverInfo(x, y + 5, labelWidth, 12, comment));

    x += labelWidth + 10;
  }

  protected void addConfigOption(int w, IConfigBase config)
  {

    ((IEConfigWidgetable)config).createWidgets(this, parent, x, y, w, h, host, host.getDialogHandler());
    ((IEConfigWidgetable)config).createResetButton(this, x + w + 2, y, w, h);
  }

  public void remakeWidgets()
  {
    ((EWidgetListConfigOptions)parent).reCreateListEntryWidgetsPublic();
  }

  @Override
  public boolean wasConfigModified()
  {
    return false;
  }

  public void addWidgetPublic(WidgetBase widget)
  {
    subWidgets.add(widget);
  }

  public ButtonGeneric createResetButton(int x, int y, IConfigResettable config)
  {
    String label = StringUtils.translate("malilib.gui.button.reset.caps");
    ButtonGeneric resetButton = new ButtonGeneric(x, y, -1, 20, label);
    resetButton.setEnabled(config.isModified());
    resetButton.setActionListener(new ListenerButtonReset(config));
    return resetButton;
  }

  public void addConfigTextFieldEntry(int x, int y, int configWidth, int configHeight, IConfigValue config)
  {
    GuiTextFieldGeneric field = createTextField(x, y + 1, configWidth - 4, configHeight - 3);
    field.setMaxLength(maxTextfieldTextLength);
    field.setValue(config.getStringValue());

    TextFieldWrapper<? extends GuiTextFieldGeneric> wrapper = new TextFieldWrapper<>(field, new ListenerTextField(config));
    parent.addTextField(wrapper);
    textFields.add(wrapper);
  }

  @Override public void render(int mouseX, int mouseY, boolean selected, PoseStack poseStack)
  {
    RenderUtils.color(1F, 1F, 1F, 1F);
    drawSubWidgets(mouseX, mouseY, poseStack);
    for(TextFieldWrapper<? extends GuiTextFieldGeneric> wrapper : textFields)
      wrapper.getTextField().render(poseStack, mouseX, mouseY, 0f);
    super.render(mouseX, mouseY, selected, poseStack);
    ((IEConfigWidgetable)wrapper.getConfig()).render(x, y, w, h);
  }

  @Override
  protected boolean onMouseClickedImpl(int mouseX, int mouseY, int mouseButton)
  {
    if(super.onMouseClickedImpl(mouseX, mouseY, mouseButton))
      return true;

    boolean ret = false;
    for(TextFieldWrapper<? extends GuiTextFieldGeneric> wrapper : textFields)
      ret |= wrapper.getTextField().mouseClicked(mouseX, mouseY, mouseButton);
    return ret;
  }

  @Override
  public boolean onKeyTypedImpl(int keyCode, int scanCode, int modifiers)
  {
    for(TextFieldWrapper<? extends GuiTextFieldGeneric> wrapper : textFields)
      if(wrapper.isFocused())
      {
        if(keyCode == KeyCodes.KEY_ENTER)
        {
          applyNewValueToConfig();
          return true;
        }
        else
          return wrapper.onKeyTyped(keyCode, scanCode, modifiers);
      }
    return false;
  }

  @Override
  protected boolean onCharTypedImpl(char charIn, int modifiers)
  {
    for(TextFieldWrapper<? extends GuiTextFieldGeneric> wrapper : textFields)
      if(wrapper.isFocused())
        if(wrapper.onCharTyped(charIn, modifiers))
          return true;
    return super.onCharTypedImpl(charIn, modifiers);
  }

  @Override
  public void applyNewValueToConfig()
  {
    for(TextFieldWrapper<? extends GuiTextFieldGeneric> wrapper : textFields)
      ((ListenerTextField)wrapper.getListener()).getConfig().setValueFromString(wrapper.getTextField().getValue());
  }

  private static class ListenerTextField implements ITextFieldListener<GuiTextFieldGeneric>
  {
    private final IStringRepresentable config;

    public ListenerTextField(IStringRepresentable config)
    {
      this.config = config;
    }

    public IStringRepresentable getConfig()
    {
      return config;
    }

    @Override
    public boolean onTextChange(GuiTextFieldGeneric textField)
    {
      config.setValueFromString(textField.getValue());
      return false;
    }
  }

  private static class ListenerButtonReset implements IButtonActionListener
  {
    private final IConfigResettable config;

    public ListenerButtonReset(IConfigResettable config)
    {
      this.config = config;
    }

    @Override
    public void actionPerformedWithButton(ButtonBase button, int mouseButton)
    {
      config.resetToDefault();
    }
  }
}
