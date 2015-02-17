package tsuteto.tdkddoor;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemDoor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class ItemTdkdDoor extends ItemDoor
{
    public Material doorMaterial;

    public ItemTdkdDoor(Material material)
    {
        super(material);
        doorMaterial = material;
    }

    @Override
    public boolean onItemUse(ItemStack itemstack, EntityPlayer entityplayer, World world, int i, int j, int k, int l, float par8, float par9, float par10)
    {
        if(l != 1)
        {
            return false;
        }
        else
        {
	        j++;
	        BlockTdkdDoor block;

	        if (doorMaterial == Material.wood)
	        {
	        	block = TravelDokodemoDoorMod.blockTdkdDoorWood;
	        }
            else if (doorMaterial == Material.rock)
            {
                block = TravelDokodemoDoorMod.blockTdkdDoorStone;
            }
	        else
	        {
	        	block = TravelDokodemoDoorMod.blockTdkdDoorIron;
	        }

	        if(entityplayer.canPlayerEdit(i, j, k, l, itemstack) && entityplayer.canPlayerEdit(i, j + 1, k, l, itemstack))
	        {
		        if(!block.canPlaceBlockAt(world, i, j, k))
		        {
		            return false;
		        }
		        else
		        {
		            int i1 = MathHelper.floor_double((((entityplayer.rotationYaw + 180F) * 4F) / 360F) - 0.5D) & 3;
		            placeDoorBlock(world, i, j, k, i1, block);
		            itemstack.stackSize--;
		            return true;
		        }
	        }
	        else
	        {
	        	return false;
	        }
        }
    }

    public static void placeDoorBlock(World world, int i, int j, int k, int l, Block block)
    {
        byte byte0 = 0;
        byte byte1 = 0;
        if(l == 0)
        {
            byte1 = 1;
        }
        if(l == 1)
        {
            byte0 = -1;
        }
        if(l == 2)
        {
            byte1 = -1;
        }
        if(l == 3)
        {
            byte0 = 1;
        }
        int i1 = (world.getBlock(i - byte0, j, k - byte1).isNormalCube() ? 1 : 0) + (world.getBlock(i - byte0, j + 1, k - byte1).isNormalCube() ? 1 : 0);
        int j1 = (world.getBlock(i + byte0, j, k + byte1).isNormalCube() ? 1 : 0) + (world.getBlock(i + byte0, j + 1, k + byte1).isNormalCube() ? 1 : 0);
        boolean flag = world.getBlock(i - byte0, j, k - byte1) == block || world.getBlock(i - byte0, j + 1, k - byte1) == block;
        boolean flag1 = world.getBlock(i + byte0, j, k + byte1) == block || world.getBlock(i + byte0, j + 1, k + byte1) == block;
        boolean flag2 = false;
        if(flag && !flag1)
        {
            flag2 = true;
        }
        else if(j1 > i1)
        {
            flag2 = true;
        }
        world.setBlock(i, j, k, block, l, 2);
        world.setBlock(i, j + 1, k, block, 8 | (flag2 ? 1 : 0), 2);
        world.notifyBlocksOfNeighborChange(i, j, k, block);
        world.notifyBlocksOfNeighborChange(i, j + 1, k, block);
    }
}
