package tsuteto.tdkddoor;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class BlockTdkdDoorOriginal extends BlockTdkdDoor
{

    protected BlockTdkdDoorOriginal(Material material)
    {
        super(material);
    }

    @Override
    public boolean onBlockActivated(World par1World, int par2, int par3, int par4, EntityPlayer par5EntityPlayer, int par6, float par7, float par8, float par9)
    {
        ItemStack equipped = par5EntityPlayer.getCurrentEquippedItem();
        if (equipped != null && equipped.getItem() == TravelDokodemoDoorMod.itemTdkdDoorOriginal)
        {
            // planned to do something
        }
        else
        {
            super.onBlockActivated(par1World, par2, par3, par4, par5EntityPlayer, par6, par7, par8, par9);
        }

        return true;
    }


}
