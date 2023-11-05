package me.lntricate.entityvisualizer.malilib.widgets;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import org.apache.commons.lang3.tuple.Pair;

import com.mojang.blaze3d.vertex.PoseStack;

import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.IConfigResettable;
import fi.dy.masa.malilib.config.IConfigValue;
import fi.dy.masa.malilib.config.IStringRepresentable;
import fi.dy.masa.malilib.gui.GuiTextFieldGeneric;
import fi.dy.masa.malilib.gui.GuiConfigsBase.ConfigOptionWrapper;
import fi.dy.masa.malilib.gui.interfaces.IConfigInfoProvider;
import fi.dy.masa.malilib.gui.interfaces.IKeybindConfigGui;
import fi.dy.masa.malilib.gui.interfaces.ITextFieldListener;
import fi.dy.masa.malilib.gui.widgets.WidgetBase;
import fi.dy.masa.malilib.gui.widgets.WidgetConfigOptionBase;
import fi.dy.masa.malilib.gui.widgets.WidgetHoverInfo;
import fi.dy.masa.malilib.gui.widgets.WidgetListConfigOptionsBase;
import fi.dy.masa.malilib.gui.wrappers.TextFieldWrapper;
import fi.dy.masa.malilib.hotkeys.IHotkey;
import fi.dy.masa.malilib.hotkeys.KeybindSettings;
import fi.dy.masa.malilib.render.RenderUtils;
import fi.dy.masa.malilib.util.KeyCodes;
import me.lntricate.entityvisualizer.malilib.config.IEConfigWidgetable;
import me.lntricate.entityvisualizer.malilib.config.options.EConfigBoolean;
import me.lntricate.entityvisualizer.malilib.config.options.EConfigHotkey;

public class EWidgetConfigOption extends WidgetConfigOptionBase<ConfigOptionWrapper>
{
  protected final ConfigOptionWrapper wrapper;
  protected final IKeybindConfigGui host;
  private int x, y;
  private final int labelWidth, w, h;
  private List<TextFieldWrapper<? extends GuiTextFieldGeneric>> textFields = new ArrayList<>();
  @Nullable protected final Set<Pair<KeybindSettings, IHotkey>> initialKeybindSettings = new HashSet<>();
  private boolean modified;

  public EWidgetConfigOption(int x, int y, int width, int height, int labelWidth, int configWidth, ConfigOptionWrapper wrapper, int listIndex, IKeybindConfigGui host, WidgetListConfigOptionsBase<?, ?> parent)
  {
    super(x, y, width, height, parent, wrapper, listIndex);
    this.host = host;
    this.wrapper = wrapper;
    this.labelWidth = labelWidth;
    this.x = x; this.y = y;
    IConfigBase config = wrapper.getConfig();
    w = config instanceof EConfigBoolean || config instanceof EConfigHotkey ? Math.min(configWidth, 400) : configWidth;
    h = 20;
    addLabel(config);
    addConfigOption(w, config);
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
    ResetButton resetButton = new ResetButton(x + w + 2, y, (IConfigResettable)config, this);
    ((IEConfigWidgetable)config).createWidgets(this, parent, x, y, w, h, host, resetButton);
    addWidget(resetButton);
  }

  public void remakeWidgets()
  {
    ((EWidgetListConfigOptions)parent).reCreateListEntryWidgetsPublic();
  }

  @Override
  public boolean wasConfigModified()
  {
    if(modified)
    {
      modified = false;
      return true;
    }
    return false;
  }

  public void modify()
  {
    modified = true;
  }

  public void addWidgetPublic(WidgetBase widget)
  {
    subWidgets.add(widget);
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
    applyNewValueToConfig();

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
        boolean ret = wrapper.onKeyTyped(keyCode, scanCode, modifiers);
        return keyCode == KeyCodes.KEY_ENTER || ret;
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
}
