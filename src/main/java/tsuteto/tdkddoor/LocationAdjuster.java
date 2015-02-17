package tsuteto.tdkddoor;

import java.util.HashMap;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.EntityEvent.EnteringChunk;

public class LocationAdjuster
{
    public static HashMap<Integer, AdjustPoint> playersTeleporting = new HashMap<Integer, AdjustPoint>();

    @SubscribeEvent
    public void onEnteringChunk(EnteringChunk event)
    {
        if (event.entity instanceof EntityPlayer && playersTeleporting.containsKey(event.entity.getEntityId()))
        {
            this.adjustLocation((EntityPlayer) event.entity, playersTeleporting.get(event.entity.getEntityId()));
            playersTeleporting.remove(event.entity.getEntityId());
        }
    }

    private void adjustLocation(EntityPlayer player, AdjustPoint info)
    {
        double prevX = player.posX;
        double prevY = player.posY;
        double prevZ = player.posZ;
        player.setPositionAndUpdate(info.x, info.y, info.z);
        player.fallDistance = 0f;
        ModLog.debug("Adjust location: (%.1f, %.1f, %.1f) => (%.1f, %.1f, %.1f)", prevX, prevY, prevZ, player.posX, player.posY, player.posZ);
    }

    public static class AdjustPoint
    {
        public double x, y, z;
        public AdjustPoint(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
        @Override
        public String toString() {
            return String.format("(%.1f, %.1f, %.1f)", x, y, z);
        }
    }
}
