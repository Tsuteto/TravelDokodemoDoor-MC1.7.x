package tsuteto.tdkddoor;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import tsuteto.tdkddoor.packet.PacketPipeline;
import tsuteto.tdkddoor.packet.PacketTeleportation;

/**
 * The Main Class of Travel Dokodemo Door Mod 3
 *
 * @author tsuteto
 *
 */
@Mod(modid = TravelDokodemoDoorMod.modid, version = "3.0.0-MC1.7.2")
public class TravelDokodemoDoorMod {
    public static final String modid = "TravelDokodemoDoor";
    public static final String resourceDomain = "tdkddoor:";

	public static boolean travelStableArea = true;
	public static boolean isTravelLogEnabled = false;

	public static Item itemTdkdDoorWood;
	public static Item itemTdkdDoorIron;
    public static Item itemTdkdDoorStone;
    public static Item itemTdkdDoorOriginal;

    public static BlockTdkdDoor blockTdkdDoorWood;
	public static BlockTdkdDoor blockTdkdDoorIron;
    public static BlockTdkdDoor blockTdkdDoorStone;

//	@SidedProxy(clientSide = "tsuteto.tdkddoor.TravelDokodemoDoorMod$ClientProxy", serverSide = "tsuteto.tdkddoor.TravelDokodemoDoorMod$ServerProxy")
//	public static ISidedProxy sidedProxy;

    public static PacketPipeline packetPipeline = new PacketPipeline();
	public static TdkdDoorSaveHandler saveHandler = null;

	static
	{
	    // Logger preparation
	    ModLog.modId = TravelDokodemoDoorMod.modid;
	    ModLog.isDebug = Boolean.valueOf(System.getProperty("tdkddoor.debug", "false"));
	}

    @Mod.EventHandler
	public void preinit(FMLPreInitializationEvent event)
	{
	    // Load settings
        Configuration conf = new Configuration(event.getSuggestedConfigurationFile());
        conf.load();
        travelStableArea = conf.get("general", "travelStableArea", travelStableArea,
                "The doors allow you to travel within stable area (<8,300,000) if true, or whole playable area (<30,000,000)")
                .getBoolean(travelStableArea);

        isTravelLogEnabled = conf.get("general", "travelLog", isTravelLogEnabled,
                "This mod logs traveling with the doors if true.")
                .getBoolean(isTravelLogEnabled);
        conf.save();

        // Register items
        itemTdkdDoorWood = registerItem("tdkdDoorWood",
                new ItemTdkdDoor(Material.wood)).setCreativeTab(CreativeTabs.tabTransport);
        itemTdkdDoorIron = registerItem("tdkdDoorIron",
                new ItemTdkdDoor(Material.iron)).setCreativeTab(CreativeTabs.tabTransport);
        itemTdkdDoorStone = registerItem("tdkdDoorStone",
                new ItemTdkdDoor(Material.rock)).setCreativeTab(CreativeTabs.tabTransport);

        // Register blocks
        blockTdkdDoorWood = (BlockTdkdDoor)registerBlock("blockTdkdDoorWood",
                new BlockTdkdDoor(Material.wood))
                .setHardness(3F)
                .setLightLevel(0.75F)
                .setStepSound(Block.soundTypeWood);

        blockTdkdDoorIron = (BlockTdkdDoor)registerBlock("blockTdkdDoorIron",
                new BlockTdkdDoor(Material.iron))
                .setHardness(6F)
                .setLightLevel(0.75F)
                .setResistance(50F)
                .setStepSound(Block.soundTypeMetal);

        blockTdkdDoorStone = (BlockTdkdDoor)registerBlock("blockTdkdDoorStone",
                new BlockTdkdDoor(Material.rock))
                .setHardness(6F)
                .setLightLevel(0.75F)
                .setResistance(50F)
                .setStepSound(Block.soundTypeStone);
    }

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
	    // Register event handler for location adjustment after teleportation of player
	    MinecraftForge.EVENT_BUS.register(new LocationAdjuster());
        // Register packet channel for teleportation sound
        packetPipeline.registerChannel("tdkddoor", PacketTeleportation.class);

		// Add recipes
		GameRegistry.addRecipe(new ItemStack(itemTdkdDoorWood),
					"XX",
					"XY",
					"XX",
					Character.valueOf('X'), Blocks.planks,
					Character.valueOf('Y'), Items.ender_pearl
        );
        GameRegistry.addRecipe(new ItemStack(itemTdkdDoorIron),
					"XX",
					"XY",
					"XX",
					Character.valueOf('X'), Items.iron_ingot,
					Character.valueOf('Y'), Items.ender_pearl
		);
        GameRegistry.addRecipe(new ItemStack(itemTdkdDoorStone),
                "XX",
                "XY",
                "XX",
                Character.valueOf('X'), Blocks.stone,
                Character.valueOf('Y'), Items.ender_pearl
        );
	}

    @Mod.EventHandler
    private void postinit(FMLPostInitializationEvent event)
    {
        packetPipeline.postInitialize();
    }

	@Mod.EventHandler
	private void onServerStarting(FMLServerStartingEvent event)
	{
	    WorldServer world = event.getServer().worldServers[0];

        saveHandler = new TdkdDoorSaveHandler(
                world.getSaveHandler().getWorldDirectory(),
                world.getSaveHandler().getWorldDirectoryName());
	}

    private Item registerItem(String name, Item item)
    {
        item.setUnlocalizedName(TravelDokodemoDoorMod.resourceDomain + name);
        item.setTextureName(TravelDokodemoDoorMod.resourceDomain + name);
        GameRegistry.registerItem(item, name);
        return item;
    }

    private Block registerBlock(String name, Block block)
    {
        block.setBlockName(TravelDokodemoDoorMod.resourceDomain + name);
        block.setBlockTextureName(TravelDokodemoDoorMod.resourceDomain + name);
        return GameRegistry.registerBlock(block, ItemBlock.class, name);
    }

//    @SideOnly(Side.CLIENT)
//	public static class ClientProxy implements ISidedProxy
//	{
//		@Override
//		public void registerTextures(String textureFile)
//		{
//		}
//	}
//
//	@SideOnly(Side.SERVER)
//	public static class ServerProxy implements ISidedProxy
//	{
//		@Override
//		public void registerTextures(String textureFile) {}
//	}
//
//	public static interface ISidedProxy {
//		public void registerTextures(String textureFile);
//	}

}
