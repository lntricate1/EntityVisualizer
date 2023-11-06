package me.lntricate.entityvisualizer.config;

import java.io.File;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import fi.dy.masa.malilib.config.ConfigUtils;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.IConfigHandler;
import fi.dy.masa.malilib.config.options.ConfigBoolean;
import fi.dy.masa.malilib.hotkeys.IHotkey;
import fi.dy.masa.malilib.util.FileUtils;
import fi.dy.masa.malilib.util.JsonUtils;
import fi.dy.masa.malilib.util.restrictions.UsageRestriction.ListType;
import me.lntricate.entityvisualizer.EntityVisualizerMod;
import me.lntricate.entityvisualizer.malilib.config.options.EConfigBoolean;
import me.lntricate.entityvisualizer.malilib.config.options.EConfigDouble;
import me.lntricate.entityvisualizer.malilib.config.options.EConfigHotkey;
import me.lntricate.entityvisualizer.malilib.config.options.EConfigRenderer;
import me.lntricate.entityvisualizer.malilib.config.options.EConfigString;
import me.lntricate.entityvisualizer.malilib.config.options.EConfigWBList;
import me.lntricate.entityvisualizer.network.ClientNetworkHandler;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;

public class Configs implements IConfigHandler
{
  private static final String CONFIG_FILE_NAME = EntityVisualizerMod.MOD_ID + ".json";

  @Override
  public void load()
  {
    File configFile = new File(FileUtils.getConfigDirectory(), CONFIG_FILE_NAME);

    if(configFile.exists() && configFile.isFile() && configFile.canRead())
    {
      JsonElement element = JsonUtils.parseJsonFile(configFile);

      if(element != null && element.isJsonObject())
      {
        JsonObject root = element.getAsJsonObject();

        ConfigUtils.readConfigBase(root, "Generic", Generic.getOptions());
        ConfigUtils.readConfigBase(root, "Renderers", Renderers.getOptions());
        ConfigUtils.readConfigBase(root, "Lists", Lists.getOptions());
      }
    }
  }

  @Override
  public void save()
  {
    File dir = FileUtils.getConfigDirectory();

    if((dir.exists() && dir.isDirectory()) || dir.mkdirs())
    {
      JsonObject root = new JsonObject();

      ConfigUtils.writeConfigBase(root, "Generic", Generic.getOptions());
      ConfigUtils.writeConfigBase(root, "Renderers", Renderers.getOptions());
      ConfigUtils.writeConfigBase(root, "Lists", Lists.getOptions());

      JsonUtils.writeJsonToFile(root, new File(dir, CONFIG_FILE_NAME));
    }
  }

  public static class Generic
  {
    public static final EConfigHotkey OPEN_CONFIG_GUI = new EConfigHotkey("openConfigGui", "V,C", "A hotkey to open the in-game Config GUI");
    public static final EConfigHotkey ENTITY_DATA = new EConfigHotkey("getEntityData", "LEFT_ALT,BUTTON_2", "A hotkey to get entity position and motion\nBy default, you can shift-click the message to copy it into chat");
    public static final EConfigString ENTITY_DATA_FORMAT = new EConfigString("getEntityDataFormat",
      "#gold\"%countx \", #light_purple\"%name\", \" | \", {\"insertion\":\"%x, %y, %z\",\"text\":\"\",\"extra\":[#red\"%x \", #green\"%y \", #blue\"%z\"]}, \" | \", {\"insertion\":\"%mx, %my, %mz\",\"color\":\"yellow\",\"text\":\"%mx %my %mz\"}, {\"clickEvent\":{\"action\":\"suggest_command\",\"value\":\"/summon %name %x %y %z {Motion:[%mxd, %myd, %mzd]}\"}, \"color\":\"gold\", \"text\":\" [summon]\"}", "The format for getEntityData.\nSee https://github.com/axialeaa/colorful-minihud for how to format\n§6%%%%x, %%%%y, %%%%z: XYZ position\n§6%%%%mx, %%%%my, %%%%mz: XYZ motion\n§6%%%%count: Number of entities in the same state\n§6%%%%name: The type of entity");
    public static final EConfigString ENTITY_DATA_TNT_FORMAT = new EConfigString("getEntityDataTNTFormat",
      "#gold\"%countx \", #light_purple\"TNT\", \" | \", {\"insertion\":\"%x, %y, %z\",\"text\":\"\",\"extra\":[#red\"%x \", #green\"%y \", #blue\"%z\"]}, \" | \", {\"insertion\":\"%mx, %my, %mz\",\"color\":\"yellow\",\"text\":\"%mx %my %mz\"}, \" (Fuse \", #light_purple\"%fuse\", \") \", {\"clickEvent\":{\"action\":\"suggest_command\",\"value\":\"/summon tnt %x %y %z {Motion:[%mxd, %myd, %mzd],Fuse:%fuse}\"}, \"color\":\"gold\", \"text\":\"[summon]\"}", "The format for getEntityData when the entity is TNT.\nSee https://github.com/axialeaa/colorful-minihud for how to format\n§6%%%%x, %%%%y, %%%%z: XYZ position\n§6%%%%mx, %%%%my, %%%%mz: XYZ motion\n§6%%%%count: Number of entities in the same state\n§6%%%%fuse: Fuse of the tnt");
    public static final EConfigDouble ENTITY_DATA_RANGE = new EConfigDouble("getEntityDataRange", 10, "The length of the raycast for getting entity data");
    public static final EConfigDouble EXPLOSION_BOX_SIZE = new EConfigDouble("explosionBoxSize", 0.25, "Size of the explosion boxes");
    public static final EConfigDouble POINT_SIZE = new EConfigDouble("pointSize", 0.02, "Size of the points in the Block Rays renderer");

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
      ENTITY_DATA,
      ENTITY_DATA_FORMAT,
      ENTITY_DATA_TNT_FORMAT,
      ENTITY_DATA_RANGE,
      EXPLOSION_BOX_SIZE,
      POINT_SIZE
    );

    public static final ImmutableList<IHotkey> HOTKEYS = ImmutableList.of
    (
      OPEN_CONFIG_GUI,
      ENTITY_DATA
    );
  }

  public enum Renderers
  {
    EXPLOSIONS               ("explosions",              "#FF00FFFF",                  "#00000000",
      "Shows boxes at explosions", "Explosions"),
    EXPLOSION_ENTITY_RAYS    ("explosionEntityRays",     "#FF00FF00", "Ray hit color", "#FFFF0000", "Ray blocked color",
      "Shows raycasts used in entity exposure calculation", "Explosion Entity Rays"),
    EXPLOSION_BLOCK_RAYS     ("explosionBlockRays",      "#FFFF0000", "Minimum color", "#FFFFFF00", "Maximum color",
      "Shows minimum and maximum \"rays\" checked for block breaking", "Explosion Block Rays"),
    EXPLOSION_AFFECTED_BLOCKS("explosionAffectedBlocks", "#44FF0000", "Minimum color", "#44FFFF00", "Maximum color",
      "Shows the minimum and maximum blocks destroyed by an explosion", "Explosion Affected Blocks"),

    ENTITY_CREATION  ("entityCreation",   "#4400FF00",                          "#00000000",                          "Shows boxes at entity spawn locations", "Entity Creation"),
    ENTITY_TICKS     ("entityTicks",      "#440000FF",                          "#00000000",                          "Shows boxes at entity tick locations",  "Entity Ticks"),
    ENTITY_VELOCITY  ("entityVelocity",   "#FFFF0000",                                                                "Shows lines for entity velocity",       "Entity Velocity"),
    ENTITY_TRAJECTORY("entityTrajectory", "#FF8800FF", "Normal movement color", "#FFFFFF00", "Piston movement color", "Shows lines for entity movement",       "Entity Trajectory"),
    ENTITY_DEATHS    ("entityDeaths",     "#44FF0000",                          "#00000000",                          "Shows boxes at entity death locations", "Entity Death");

    private static ImmutableList<EConfigBoolean> entityPacketRequirers = ImmutableList.of(
      ENTITY_TICKS.config.booleanConfig(),
      ENTITY_VELOCITY.config.booleanConfig(),
      ENTITY_TRAJECTORY.config.booleanConfig(),
      ENTITY_DEATHS.config.booleanConfig()
    );

    // public final ConfigGenericRenderer config;
    public final EConfigRenderer config;

    static
    {
      for(ConfigBoolean config : entityPacketRequirers)
        config.setValueChangeCallback(Renderers::onEntityRendererChanged);
    }

    private Renderers(String name, String color, String comment, String prettyName)
    {
      config = new EConfigRenderer(name, color, comment, prettyName);
    }

    private Renderers(String name, String color1, String color2, String comment, String prettyName)
    {
      config = new EConfigRenderer(name, color1, color2, comment, prettyName);
    }

    private Renderers(String name, String color1, String color1Comment, String color2, String color2Comment, String comment, String prettyName)
    {
      config = new EConfigRenderer(name, color1, color1Comment, color2, color2Comment, comment, prettyName);
    }

    public static ImmutableList<IConfigBase> getOptions()
    {
      return ImmutableList.copyOf(ImmutableList.copyOf(values()).stream().map((renderer) -> renderer.config).toList());
    }

    public static ImmutableList<IHotkey> getHotkeys()
    {
      return ImmutableList.copyOf(ImmutableList.copyOf(values()).stream().map((renderer) -> renderer.config.key()).toList());
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
    EXPLOSION_ENTITY_RAYS("explosionEntityRays", ListContext.ENTITIES, "Explosion Entity Rays"),
    EXPLOSION_BLOCK_RAYS("explosionBlockRays", ListContext.BLOCKS, "Explosion Block Rays"),
    EXPLOSION_AFFECTED_BLOCKS("explosionAffectedBlocks", ListContext.BLOCKS, "Explosion Affected Blocks"),

    ENTITY_CREATION("entityCreation", ListContext.ENTITIES, "Entity Creation"),
    ENTITY_TICKS("entityTicks", ListContext.ENTITIES, "Entity Ticks"),
    ENTITY_VELOCITY("entityVelocity", ListContext.ENTITIES, "Entity Velocity"),
    ENTITY_TRAJECTORY("entityTrajectory", ListContext.ENTITIES, "Entity Trajectory"),
    ENTITY_DEATHS("entityDeaths", ListContext.ENTITIES, "Entity Deaths");

    private EConfigWBList config;

    private enum ListContext
    {
      BLOCKS, ENTITIES;
    }

    private Lists(String name, ListContext context, String prettyName)
    {
      switch(context)
      {
        case BLOCKS:
          instantiate(name, ListType.BLACKLIST, ImmutableList.of(), ImmutableList.of("air"), prettyName);
          break;
        case ENTITIES:
          instantiate(name, ListType.BLACKLIST, ImmutableList.of(), ImmutableList.of("player"), prettyName);
          break;
      }
    }

    private void instantiate(String name, ListType defaultListType, ImmutableList<String> defaultWhitelist, ImmutableList<String> defaultBlacklist, String prettyName)
    {
      config = new EConfigWBList(name, defaultListType, defaultWhitelist, defaultBlacklist, "The list for filtering " + name, prettyName);
    }

    public static ImmutableList<IConfigBase> getOptions()
    {
      ImmutableList.Builder<IConfigBase> builder = ImmutableList.builder();
      for(Lists option : values())
        builder.add(option.config);
      return builder.build();
    }

    public static ImmutableList<IHotkey> getHotkeys()
    {
      return ImmutableList.of();
    }

    public boolean shouldRender(String key)
    {
      return config.test(key);
    }

    public boolean shouldRender(EntityType<?> type)
    {
      return config.test(type.toShortString());
    }

    public boolean shouldRender(Block block)
    {
      String key = block.getDescriptionId();
      return config.test(key.substring(key.lastIndexOf(46) + 1));
    }
  }
}
