package tsuteto.tdkddoor;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.IconFlipped;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.*;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import tsuteto.tdkddoor.LocationAdjuster.AdjustPoint;
import tsuteto.tdkddoor.TdkdDoorWorldInfo.DoorInfoEntry;
import tsuteto.tdkddoor.TdkdDoorWorldInfo.DoorPoint;
import tsuteto.tdkddoor.packet.PacketTeleportation;

import java.util.List;
import java.util.Random;

public class BlockTdkdDoor extends BlockDoor
{
    private static final String[] iconNames = new String[] {
            "tdkddoor:tdkdDoorWood_lower", "tdkddoor:tdkdDoorWood_upper",
            "tdkddoor:tdkdDoorIron_lower", "tdkddoor:tdkdDoorIron_upper",
            "tdkddoor:tdkdDoorStone_lower", "tdkddoor:tdkdDoorStone_upper"};
    private final int iconId;
    @SideOnly(Side.CLIENT)
    private IIcon[] icons;

	private Random rand = new Random();
	private int passingTime = 0;
	private static int[][] dirTable = {{1, 0}, {0, 1}, {-1, 0}, {0, -1}};

	protected DoorInfoHandler doorInfoHandler = new DoorInfoHandler();

    protected BlockTdkdDoor(Material material)
    {
        super(material);
        if (material == Material.iron)
        {
            this.iconId = 2;
        }
        else if (material == Material.rock)
        {
            this.iconId = 4;
        }
        else
        {
            this.iconId = 0;
        }
        disableStats();
    }

    @SideOnly(Side.CLIENT)

    /**
     * From the specified side and block metadata retrieves the blocks texture. Args: side, metadata
     */
    @Override
    public IIcon getIcon(int par1, int par2)
    {
        return this.icons[this.iconId];
    }

    /**
     * Retrieves the block texture to use based on the display side. Args: iBlockAccess, x, y, z, side
     */
    @Override
    public IIcon getIcon(IBlockAccess par1IBlockAccess, int par2, int par3, int par4, int par5)
    {
        if (par5 != 1 && par5 != 0)
        {
            int i1 = this.getFullMetadata(par1IBlockAccess, par2, par3, par4);
            int j1 = i1 & 3;
            boolean flag = (i1 & 4) != 0;
            boolean flag1 = false;
            boolean flag2 = (i1 & 8) != 0;

            if (flag)
            {
                if (j1 == 0 && par5 == 2)
                {
                    flag1 = !flag1;
                }
                else if (j1 == 1 && par5 == 5)
                {
                    flag1 = !flag1;
                }
                else if (j1 == 2 && par5 == 3)
                {
                    flag1 = !flag1;
                }
                else if (j1 == 3 && par5 == 4)
                {
                    flag1 = !flag1;
                }
            }
            else
            {
                if (j1 == 0 && par5 == 5)
                {
                    flag1 = !flag1;
                }
                else if (j1 == 1 && par5 == 3)
                {
                    flag1 = !flag1;
                }
                else if (j1 == 2 && par5 == 4)
                {
                    flag1 = !flag1;
                }
                else if (j1 == 3 && par5 == 2)
                {
                    flag1 = !flag1;
                }

                if ((i1 & 16) != 0)
                {
                    flag1 = !flag1;
                }
            }

            return this.icons[this.iconId + (flag1 ? iconNames.length : 0) + (flag2 ? 1 : 0)];
        }
        else
        {
            return this.icons[this.iconId];
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerBlockIcons(IIconRegister par1IconRegister)
    {
        this.icons = new IIcon[iconNames.length * 2];

        for (int i = 0; i < iconNames.length; ++i)
        {
            this.icons[i] = par1IconRegister.registerIcon(iconNames[i]);
            this.icons[i + iconNames.length] = new IconFlipped(this.icons[i], true, false);
        }
    }

    /**
     * Referenced to BlockPressurePlate
     */
    @Override
    public void updateTick(World world, int i, int j, int k, Random random)
    {
        if(world.isRemote)
        {
            return;
        }
        setStateIfMobInteractsWithPlate(world, i, j, k);
    }

    @Override
    public void onEntityCollidedWithBlock(World world, int i, int j, int k, Entity entity)
    {
        if(world.isRemote)
        {
            return;
        }
        if (world.getBlockMetadata(i, j, k) == 1)
        {
            return;
        }
        else
        {
        	setStateIfMobInteractsWithPlate(world, i, j, k);
        }
    }

    private void setStateIfMobInteractsWithPlate(World world, int i, int j, int k)
    {
        if (!this.func_150015_f(world, i, j, k)) // isDoorOpen
        {
            return;
        }

        int m = getFullMetadata(world, i, j, k);
        if ((m & 8) != 0)
        {
            return;
        }

        int blockMetadata = world.getBlockMetadata(i, j, k);
        boolean flag1 = false;
        float f = 0.125F;
        List list = world.getEntitiesWithinAABB(EntityLivingBase.class,
                AxisAlignedBB.getBoundingBox(i + f, j, k + f, (i + 1) - f, j + 0.25D, (k + 1) - f));

        if (list.size() != 0)
        {
            for (Object obj : list)
            {
                if (obj instanceof EntityLivingBase)
                {
                    EntityLivingBase entity = (EntityLivingBase) obj;
                    int direction = blockMetadata % 4;

                    Vec3 vec3d = Vec3.createVectorHelper(
                            entity.posX - entity.prevPosX,
                            entity.posY - entity.prevPosY,
                            entity.posZ - entity.prevPosZ);
                    Vec3 norm = vec3d.normalize();
                    double speed = vec3d.lengthVector();

                    if (speed > 0.1D
                            && (direction == 0 && norm.xCoord > 0.95D || direction == 1 && norm.zCoord > 0.95D
                                    || direction == 2 && norm.xCoord < -0.95D || direction == 3 && norm.zCoord < -0.95D))
                    {
                        activateTeleportation(world, entity, i, j, k, direction);
                    }
                }
            }
        }
    }

    private void activateTeleportation(World world, EntityLivingBase entity, int x, int y, int z, int doorDirection)
    {
        ModLog.debug("Travel Dokodemo Door teleportation activated!");

        if (blockMaterial != Material.wood)
        {
            DoorInfoEntry entry = doorInfoHandler.getDoorInfo(x, y, z);

            if (entry == null)
            {
                teleportSomewhere(world, entity, x, y, z, doorDirection);
            }
            else
            {
                teleportToLocatedPoint(world, entry, entity, x, y, z, doorDirection);
            }
            doorInfoHandler.save();
        }
        else
        {
            teleportSomewhere(world, entity, x, y, z, doorDirection);
        }
    }

    /**
     * Random teleportation
     */
    private void teleportSomewhere(World world, EntityLivingBase entity, int x, int y, int z, int d)
    {

		if (entity.dimension != 0) return;

		Vec3 vecDept = Vec3.createVectorHelper(entity.posX, entity.posY, entity.posZ);

        double destX, destZ;

        if (blockMaterial != Material.rock)
        {
            int travelLimit = (TravelDokodemoDoorMod.travelStableArea) ? 8300000 : 30000000;
            destX = (rand.nextDouble() - rand.nextDouble()) * travelLimit;
            destZ = (rand.nextDouble() - rand.nextDouble()) * travelLimit;
        }
        else
        {
            destX = x + (rand.nextDouble() * 0.9 + 0.1) * 10000 * (rand.nextBoolean() ? 1 : -1);
            destZ = z + (rand.nextDouble() * 0.9 + 0.1) * 10000 * (rand.nextBoolean() ? 1 : -1);
        }

        if(entity.isEntityAlive())
        {
            world.updateEntityWithOptionalForce(entity, false);
            entity.setLocationAndAngles(destX, entity.posY, destZ, entity.rotationYaw, entity.rotationPitch);
        }

        Vec3 spawnPoint = locateSpawnPoint(world, entity, d);
        int spawnPointX = (int)spawnPoint.xCoord;
        int spawnPointY = (int)spawnPoint.yCoord;
        int spawnPointZ = (int)spawnPoint.zCoord;

        if(entity.isEntityAlive())
        {
        	entity.setPositionAndUpdate(spawnPointX, spawnPointY, spawnPointZ);
        	world.updateEntityWithOptionalForce(entity, false);
        }

        Entity newEntity = EntityList.createEntityByName(EntityList.getEntityString(entity), world);

        if (newEntity != null)
        {
        	newEntity.copyDataFrom(entity, true);
        	world.spawnEntityInWorld(newEntity);
        }

    	if (blockMaterial != Material.wood) {
	        // Create a door on the other side
	        int d1 = d ^ 2;
	        DoorPoint posNewDoor = new DoorPoint(spawnPointX + dirTable[d1][0], spawnPointY, spawnPointZ + dirTable[d1][1]);

	        createNewDoor(world, posNewDoor, d1);
	        doorInfoHandler.addDoorEntry(posNewDoor, new DoorPoint(x, y, z));

	        // Set the spawn point to this side
	        DoorInfoEntry entry = doorInfoHandler.addDoorEntry(new DoorPoint(x, y, z), posNewDoor);
    	}

    	if (entity instanceof EntityPlayer) {
    	    // For location adjustment after teleportation
    	    LocationAdjuster.playersTeleporting.put(entity.getEntityId(), new AdjustPoint(spawnPointX + 0.5D, spawnPointY, spawnPointZ + 0.5D));

            // Show the distance traveled and some features for display
    		EntityPlayer player = (EntityPlayer)entity;
            double distance = vecDept.distanceTo(spawnPoint);
            player.addChatComponentMessage(new ChatComponentTranslation("tdkddoor.distance", String.format("%.1f", distance / 1000)));

    		if (TravelDokodemoDoorMod.isTravelLogEnabled)
    		{
        		String msg = String.format("[%s] %s traveled: %s -> %s",
        		        TravelDokodemoDoorMod.modid, player.getDisplayName(),
        		        new DoorPoint(x, y, z).toString(),
        		        new DoorPoint(spawnPointX, spawnPointY, spawnPointZ).toString());
                this.printServerLog(msg);
    		}

        	// Make a sound on client side
            TravelDokodemoDoorMod.packetPipeline.sendTo(new PacketTeleportation(), (EntityPlayerMP)player);
    	}
    }

    /**
     * Door-to-door teleportation
     */
    private void teleportToLocatedPoint(World world, DoorInfoEntry entry, EntityLivingBase entity, int x, int y, int z, int d)
    {
		if (entity.dimension != 0) return;

    	int destX = entry.travelTo.x;
    	int destY = entry.travelTo.y;
    	int destZ = entry.travelTo.z;

    	double shiftX = dirTable[d][0] + 0.5D;
    	double shiftZ = dirTable[d][1] + 0.5D;

    	world.getChunkProvider().loadChunk(destX >> 4, destZ >> 4);

    	entity.setPositionAndUpdate(destX + shiftX, destY, destZ + shiftZ);

    	// When the other door is lost, create the door again
    	if (blockMaterial != Material.wood &&
    			world.getBlock(destX, destY, destZ) != TravelDokodemoDoorMod.blockTdkdDoorWood)
    	{
	        int d1 = d ^ 2;
	        DoorPoint posNewDoor = new DoorPoint(destX, destY, destZ);
	        createNewDoor(world, posNewDoor, d1);
	        doorInfoHandler.addDoorEntry(posNewDoor, new DoorPoint(x, y, z));
    	}

        // Show the distance traveled and some features for display
    	if (entity instanceof EntityPlayer)
    	{
    		EntityPlayer player = (EntityPlayer)entity;

    		if (TravelDokodemoDoorMod.isTravelLogEnabled)
    		{
            	String msg = String.format("[%s] %s traveled: %s", TravelDokodemoDoorMod.modid,
            	        player.getDisplayName(), entry.toString());
                this.printServerLog(msg);
    		}

        	// Make a sound on client side
            TravelDokodemoDoorMod.packetPipeline.sendTo(new PacketTeleportation(), (EntityPlayerMP) player);
    	}
    }

    private void createNewDoor(World world, DoorPoint p, int d)
    {
        DoorPoint base = new DoorPoint(p.x, p.y - 1, p.z);

    	world.setBlock(base.x, base.y, base.z, Blocks.stonebrick);
    	world.notifyBlocksOfNeighborChange(base.x, base.y, base.z, Blocks.stonebrick);

        ItemTdkdDoor.placeDoorBlock(world, p.x, p.y, p.z, d, this);
        openDoor(world, p.x, p.y, p.z);
    }

    @Override
    public void randomDisplayTick(World world, int i, int j, int k, Random random)
    {
    	int blockMetadata = world.getBlockMetadata(i, j, k);
    	if (!this.func_150015_f(world, i, j, k)) // isDoorOpen
    	{
    		return;
    	}

    	boolean upperblock = (blockMetadata & 8) != 0;

        int direction = world.getBlockMetadata(i, upperblock ? j - 1 : j, k) % 4;
        for(int l = 0; l < 4; l++)
        {
            double d = i + random.nextFloat();
            double d1 = j + random.nextFloat();
            double d2 = k + random.nextFloat();
            double d3 = 0.0D;
            double d4 = 0.0D;
            double d5 = 0.0D;
            int i1 = random.nextInt(1) + 1;
            d3 = (random.nextFloat() - 0.5D) * 0.5D;
            d4 = (random.nextFloat() - 0.5D) * 0.5D;
            d5 = (random.nextFloat() - 0.5D) * 0.5D;
            if(direction == 0)
            {
                d = i + 0.5D - 0.25D * i1;
                d3 = random.nextFloat() * 2.0F * -i1;
            }
            else if (direction == 1)
            {
                d2 = k + 0.5D - 0.25D * i1;
                d5 = random.nextFloat() * 2.0F * -i1;
            }
            else if (direction == 2)
            {
	            d = i + 0.5D + 0.25D * i1;
	            d3 = random.nextFloat() * 2.0F * i1;
            }
            else if (direction == 3)
            {
	            d2 = k + 0.5D + 0.25D * i1;
	            d5 = random.nextFloat() * 2.0F * i1;
            }
            world.spawnParticle("portal", d, d1, d2, d3, d4, d5);
        }

    }

    @Override
    public boolean onBlockActivated(World par1World, int par2, int par3, int par4, EntityPlayer par5EntityPlayer, int par6, float par7, float par8, float par9)
    {
        int i = getFullMetadata(par1World, par2, par3, par4);
        int j = i & 7;
        j ^= 4;

        if ((i & 8) != 0)
        {
            par1World.setBlockMetadataWithNotify(par2, par3 - 1, par4, j, 2);
            par1World.markBlockRangeForRenderUpdate(par2, par3 - 1, par4, par2, par3, par4);
        }
        else
        {
            par1World.setBlockMetadataWithNotify(par2, par3, par4, j, 2);
            par1World.markBlockRangeForRenderUpdate(par2, par3, par4, par2, par3, par4);
        }

        par1World.playAuxSFXAtEntity(par5EntityPlayer, 1003, par2, par3, par4, 0);
        return true;
    }

    @Override
    public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z, boolean willHarvest)
    {
        String msg = String.format("[%s] %s destroyed a travel door at (%d, %d, %d)",
                TravelDokodemoDoorMod.modid, player.getDisplayName(), x, y, z);
        this.printServerLog(msg);

        return super.removedByPlayer(world, player, x, y, z, willHarvest);
    }

    @Override
    public void onBlockDestroyedByExplosion(World world, int x, int y, int z, Explosion explosion)
    {
        super.onBlockDestroyedByExplosion(world, x, y, z, explosion);

        String msg;
        if (explosion.getExplosivePlacedBy() != null)
        {
            msg = String.format("[%s] Travel door destroyed by explosion of %s at (%d, %d, %d)",
                    TravelDokodemoDoorMod.modid, explosion.getExplosivePlacedBy().getCommandSenderName(), x, y, z);
        }
        else
        {
            msg = String.format("[%s] Travel door destroyed by explosion at (%d, %d, %d)",
                    TravelDokodemoDoorMod.modid, x, y, z);
        }
        this.printServerLog(msg);
    }

    @Override
	public void breakBlock(World world, int x, int y, int z, Block i, int j)
    {
		super.breakBlock(world, x, y, z, i, j);
        if (doorInfoHandler.getDoorInfo(x, y, z) != null)
        {
    		doorInfoHandler.removeDoorInfo(x, y, z);
    		doorInfoHandler.save();
        }
	}

    public void openDoor(World par1World, int par2, int par3, int par4)
    {
        int i = getFullMetadata(par1World, par2, par3, par4);
        int j = i & 7;
        j |= 4;

        if ((i & 8) != 0)
        {
            par1World.setBlockMetadataWithNotify(par2, par3 - 1, par4, j, 2);
            par1World.markBlockRangeForRenderUpdate(par2, par3 - 1, par4, par2, par3, par4);
        }
        else
        {
            par1World.setBlockMetadataWithNotify(par2, par3, par4, j, 2);
            par1World.markBlockRangeForRenderUpdate(par2, par3, par4, par2, par3, par4);
        }
    }

	@Override
    public Item getItemDropped(int i, Random random, int j)
    {
        if((i & 8) != 0)
        {
            return null;
        }

        if (blockMaterial == Material.iron)
        {
            return TravelDokodemoDoorMod.itemTdkdDoorIron;
        }
        else if (blockMaterial == Material.rock)
        {
            return TravelDokodemoDoorMod.itemTdkdDoorStone;
        }
        else
        {
        	return TravelDokodemoDoorMod.itemTdkdDoorWood;
        }
    }

    public void onNeighborBlockChange(World world, int x, int y, int z, Block block)
    {
        int l = world.getBlockMetadata(x, y, z);

        if ((l & 8) == 0)
        {
            boolean flag = false;
            Block blockUpper = world.getBlock(x, y + 1, z);

            if (blockUpper != this)
            {
                world.setBlockToAir(x, y, z);
                flag = true;
            }

            if (flag)
            {
                if (!world.isRemote)
                {
                    this.dropBlockAsItem(world, x, y, z, l, 0);
                }

            }
            else
            {
                boolean flag1 = world.isBlockIndirectlyGettingPowered(x, y, z) || world.isBlockIndirectlyGettingPowered(x, y + 1, z);

                if ((flag1 || block.canProvidePower()) && block != this)
                {
                    this.func_150014_a(world, x, y, z, flag1);
                }
            }
        }
        else
        {
            Block blockLower = world.getBlock(x, y - 1, z);
            if (blockLower != this)
            {
                world.setBlockToAir(x, y, z);
            }

            if (block != this)
            {
                this.onNeighborBlockChange(world, x, y - 1, z, block);
            }
        }
    }

    public int getMobilityFlag()
    {
        return 2;
    }

    public int getFullMetadata(IBlockAccess blockAccess, int x, int y, int z)
    {
        return this.func_150012_g(blockAccess, x, y, z);
    }

    protected void printServerLog(String msg)
    {
        if (!MinecraftServer.getServer().isSinglePlayer())
        {
            MinecraftServer.getServer().logInfo(msg);
        }
        ModLog.info(msg);
    }

    /*
     * Referenced to Teleporter#createPortal
     */
    public Vec3 locateSpawnPoint(World world, Entity entity, int doorDirection)
    {
        byte byte0 = 16;
        double d = -1D;
        int i = MathHelper.floor_double(entity.posX);
        int j = MathHelper.floor_double(entity.posY);
        int k = MathHelper.floor_double(entity.posZ);
        int l = i;
        int i1 = j;
        int j1 = k;
        int k1 = 0;
        int l1 = rand.nextInt(4);
        for(int i2 = i - byte0; i2 <= i + byte0; i2++)
        {
            double d1 = (i2 + 0.5D) - entity.posX;
            for(int j3 = k - byte0; j3 <= k + byte0; j3++)
            {
                double d3 = (j3 + 0.5D) - entity.posZ;
                for(int k4 = 127; k4 >= 0; k4--)
                {
                    if(!world.isAirBlock(i2, k4, j3))
                    {
                        continue;
                    }
                    for(; k4 > 0 && world.isAirBlock(i2, k4 - 1, j3); k4--) { }
                    label0:
                    for(int k5 = l1; k5 < l1 + 4; k5++)
                    {
                        int l6 = k5 % 2;
                        int i8 = 1 - l6;
                        if(k5 % 4 >= 2)
                        {
                            l6 = -l6;
                            i8 = -i8;
                        }
                        for(int j9 = 0; j9 < 3; j9++)
                        {
                            for(int k10 = 0; k10 < 4; k10++)
                            {
                                for(int l11 = -1; l11 < 4; l11++)
                                {
                                    int j12 = i2 + (k10 - 1) * l6 + j9 * i8;
                                    int l12 = k4 + l11;
                                    int j13 = (j3 + (k10 - 1) * i8) - j9 * l6;
                                    if(l11 < 0 && !world.getBlock(j12, l12, j13).getMaterial().isSolid() || l11 >= 0 && !world.isAirBlock(j12, l12, j13))
                                    {
                                        break label0;
                                    }
                                }

                            }

                        }

                        double d5 = (k4 + 0.5D) - entity.posY;
                        double d7 = d1 * d1 + d5 * d5 + d3 * d3;
                        if(d < 0.0D || d7 < d)
                        {
                            d = d7;
                            l = i2;
                            i1 = k4;
                            j1 = j3;
                            k1 = k5 % 4;
                        }
                    }

                }

            }

        }

        if(d < 0.0D)
        {
            for(int j2 = i - byte0; j2 <= i + byte0; j2++)
            {
                double d2 = (j2 + 0.5D) - entity.posX;
                for(int k3 = k - byte0; k3 <= k + byte0; k3++)
                {
                    double d4 = (k3 + 0.5D) - entity.posZ;
                    for(int l4 = 127; l4 >= 0; l4--)
                    {
                        if(!world.isAirBlock(j2, l4, k3))
                        {
                            continue;
                        }
                        for(; l4 > 0 && world.isAirBlock(j2, l4 - 1, k3); l4--) { }
                        label1:
                        for(int l5 = l1; l5 < l1 + 2; l5++)
                        {
                            int i7 = l5 % 2;
                            int j8 = 1 - i7;
                            for(int k9 = 0; k9 < 4; k9++)
                            {
                                for(int l10 = -1; l10 < 4; l10++)
                                {
                                    int i12 = j2 + (k9 - 1) * i7;
                                    int k12 = l4 + l10;
                                    int i13 = k3 + (k9 - 1) * j8;
                                    if(l10 < 0 && !world.getBlock(i12, k12, i13).getMaterial().isSolid() || l10 >= 0 && !world.isAirBlock(i12, k12, i13))
                                    {
                                        break label1;
                                    }
                                }

                            }

                            double d6 = (l4 + 0.5D) - entity.posY;
                            double d8 = d2 * d2 + d6 * d6 + d4 * d4;
                            if(d < 0.0D || d8 < d)
                            {
                                d = d8;
                                l = j2;
                                i1 = l4;
                                j1 = k3;
                                k1 = l5 % 2;
                            }
                        }

                    }

                }

            }

        }
        int k2 = k1;
        int l2 = l;
        int i3 = i1;
        int l3 = j1;
        int i4 = k2 % 2;
        int j4 = 1 - i4;
        if(k2 % 4 >= 2)
        {
            i4 = -i4;
            j4 = -j4;
        }
        if(d < 0.0D)
        {
            if(i1 < 70)
            {
                i1 = 70;
            }
            if(i1 > 118)
            {
                i1 = 118;
            }
            i3 = i1;
            for(int i5 = -1; i5 <= 1; i5++)
            {
                for(int i6 = 0; i6 < 3; i6++)
                {
                    for(int j7 = -1; j7 < 3; j7++)
                    {
                        int k8 = l2 + (i6 - 1) * i4 + i5 * j4;
                        int l9 = i3 + j7;
                        int i11 = (l3 + (i6 - 1) * j4) - i5 * i4;
                        boolean flag = j7 < 0;
                        world.setBlock(k8, l9, i11, flag ? Blocks.stonebrick : Blocks.air);
                    }

                }
            }

        }
        return Vec3.createVectorHelper(l2 + i4, i3, l3 + j4);
    }

}
