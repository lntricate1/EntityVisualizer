package me.lntricate.entityvisualizer.config;

import com.google.common.collect.ImmutableList;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.IConfigHandler;
import fi.dy.masa.malilib.config.options.ConfigBoolean;
import fi.dy.masa.malilib.config.options.ConfigHotkey;
import fi.dy.masa.malilib.config.options.ConfigInteger;
import fi.dy.masa.malilib.config.options.ConfigOptionList;
import fi.dy.masa.malilib.config.options.ConfigStringList;
import fi.dy.masa.malilib.hotkeys.IHotkey;
import fi.dy.masa.malilib.util.restrictions.UsageRestriction.ListType;
import me.lntricate.entityvisualizer.config.options.ConfigGenericRenderer;
import me.lntricate.entityvisualizer.event.RenderHandler;
import me.lntricate.entityvisualizer.network.ClientNetworkHandler;
import net.minecraft.core.Registry;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;

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
    public static final ConfigInteger MAX_RENDERS = new ConfigInteger("maxRenders", 200, "Maximum number of renderers allowed to render at once");

    public static ImmutableList<IConfigBase> getOptions()
    {
      return OPTIONS;
    }

    public static ImmutableList<IHotkey> getHotkeys()
    {
      return HOTKEYS;
    }

    public static final ImmutableList<IConfigBase> OPTIONS = ImmutableList.of
    (
      OPEN_CONFIG_GUI,
      MAX_RENDERS
    );

    public static final ImmutableList<IHotkey> HOTKEYS = ImmutableList.of
    (
      OPEN_CONFIG_GUI
    );
  }

  public enum Renderers
  {
    EXPLOSIONS               ("explosions",              "Shows boxes at explosions",                                 "Explosions"),
    EXPLOSION_ENTITY_RAYS    ("explosionEntityRays",     "Shows raycasts used in entity exposure calculation",        "Explosion Entity Rays"),
    EXPLOSION_MIN_BLOCKS     ("explosionMinBlocks",      "Shows the blocks ALWAYS destroyed by an explosion",         "Explosion Minimum Blocks"),
    EXPLOSION_MAX_BLOCKS     ("explosionMaxBlocks",      "Shows the blocks POSSIBLE to be destroyed by an explosion", "Explosion Maximum Blocks"),
    EXPLOSION_AFFECTED_BLOCKS("explosionAffectedBlocks", "Shows the blocks ACTUALLY destroyed by an explosion",       "Explosion Affected Blocks"),

    ENTITY_CREATION  ("entityCreation",   "Shows boxes at entity spawn locations", "Entity Creation"),
    ENTITY_TICKS     ("entityTicks",      "Shows boxes at entity tick locations",  "Entity Ticks"),
    ENTITY_TRAJECTORY("entityTrajectory", "Shows lines for entity movement",       "Entity Trajectory"),
    ENTITY_DEATHS    ("entityDeaths",     "Shows boxes at entity death locations", "Entity Death");

    private static ImmutableList<ConfigBoolean> entityPacketRequirers = ImmutableList.of(
      ENTITY_TICKS.config,
      ENTITY_TRAJECTORY.config,
      ENTITY_DEATHS.config
    );

    public final ConfigGenericRenderer config;

    static
    {
      for(ConfigBoolean config : entityPacketRequirers)
        config.setValueChangeCallback(Renderers::onEntityRendererChanged);
    }

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

    public static boolean requireEntityPackets()
    {
      for(ConfigBoolean config : entityPacketRequirers)
        if(config.getBooleanValue()) return true;
      return false;
    }

    private static void onEntityRendererChanged(ConfigBoolean configBoolean)
    {
      ClientNetworkHandler.setPacketRecievingState(Renderers.requireEntityPackets());
    }
  }

  public enum Lists
  {
    EXPLOSION_ENTITY_RAYS("explosionEntityRays", ListContext.ENTITIES),
    EXPLOSION_MIN_BLOCKS("explosionMinBlocks", ListContext.BLOCKS),
    EXPLOSION_MAX_BLOCKS("explosionMaxBlocks", ListContext.BLOCKS),
    EXPLOSION_AFFECTED_BLOCKS("explosionAffectedBlocks", ListContext.BLOCKS),

    ENTITY_CREATION("entityCreation", ListContext.ENTITIES),
    ENTITY_TICKS("entityTicks", ListContext.ENTITIES),
    ENTITY_TRAJECTORY("entityTrajectory", ListContext.ENTITIES),
    ENTITY_DEATHS("entityDeaths", ListContext.ENTITIES);

    private ConfigOptionList configListType;
    private ConfigStringList configWhitelist, configBlacklist;

    private enum ListContext
    {
      BLOCKS, ENTITIES;
    }

    private Lists(String name, ListContext context)
    {
      switch(context)
      {
        case BLOCKS:
          instantiate(name, ListType.BLACKLIST, ImmutableList.of(), ImmutableList.of("minecraft:air"));
          break;
        case ENTITIES:
          instantiate(name, ListType.BLACKLIST, ImmutableList.of(), ImmutableList.of("minecraft:player"));
          break;
      }
    }

    private void instantiate(String name, ListType defaultListType, ImmutableList<String> defaultWhitelist, ImmutableList<String> defaultBlacklist)
    {
      this.configListType = new ConfigOptionList(name + "ListType", defaultListType, "The list type for filtering " + name);
      this.configWhitelist = new ConfigStringList(name + "Whitelist", defaultWhitelist, "The whitelist for filtering " + name);
      this.configBlacklist = new ConfigStringList(name + "Blacklist", defaultBlacklist, "The blacklist for filtering " + name);
    }

    public static ImmutableList<IConfigBase> getOptions()
    {
      ImmutableList.Builder<IConfigBase> builder = ImmutableList.builder();
      for(Lists option : values())
      {
        builder.add(option.configListType);
        builder.add(option.configWhitelist);
        builder.add(option.configBlacklist);
      }
      return builder.build();
    }

    public static ImmutableList<IHotkey> getHotkeys()
    {
      return ImmutableList.of();
    }

    public boolean shouldRender(Entity entity)
    {
      return shouldRender(Registry.ENTITY_TYPE.getKey(entity.getType()).toString());
    }

    public boolean shouldRender(Block block)
    {
      return shouldRender(Registry.BLOCK.getKey(block).toString());
    }

    private boolean shouldRender(String id)
    {
      return (ListType)configListType.getOptionListValue() == ListType.WHITELIST ?
        configWhitelist.getStrings().contains(id) :
        !configBlacklist.getStrings().contains(id);
    }
  }
}
