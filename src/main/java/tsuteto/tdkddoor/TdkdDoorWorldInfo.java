package tsuteto.tdkddoor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.Vec3;

public class TdkdDoorWorldInfo
{
    private Map<DoorPoint, DoorInfoEntry> doorInfoMap = new HashMap<DoorPoint, DoorInfoEntry>();

    public TdkdDoorWorldInfo()
    {
    }

    public TdkdDoorWorldInfo(NBTTagCompound nbttagcompound)
    {
        NBTTagList nbttaglist = (NBTTagList) nbttagcompound.getTag("DoorList");

        for (int i = 0; i < nbttaglist.tagCount(); i++)
        {
            NBTTagCompound doorinfo = nbttaglist.getCompoundTagAt(i);
            DoorPoint block = new DoorPoint(doorinfo.getInteger("x1"), doorinfo.getInteger("y1"),
                    doorinfo.getInteger("z1"));
            DoorInfoEntry entry = new DoorInfoEntry(block, new DoorPoint(doorinfo.getInteger("x2"),
                    doorinfo.getInteger("y2"), doorinfo.getInteger("z2")));
            doorInfoMap.put(block, entry);
        }
    }

    public NBTTagCompound getNBTTagCompound()
    {
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        updateTagCompound(nbttagcompound);
        return nbttagcompound;
    }

    private void updateTagCompound(NBTTagCompound nbttagcompound)
    {
        NBTTagList nbttaglist = new NBTTagList();

        Iterator<DoorPoint> itr = doorInfoMap.keySet().iterator();
        while (itr.hasNext())
        {
            DoorPoint block = itr.next();
            if (block == null) continue;

            DoorInfoEntry entry = doorInfoMap.get(block);
            NBTTagCompound doorinfo = new NBTTagCompound();
            doorinfo.setInteger("x1", entry.block.x);
            doorinfo.setInteger("y1", entry.block.y);
            doorinfo.setInteger("z1", entry.block.z);
            doorinfo.setInteger("x2", entry.travelTo.x);
            doorinfo.setInteger("y2", entry.travelTo.y);
            doorinfo.setInteger("z2", entry.travelTo.z);
            nbttaglist.appendTag(doorinfo);
        }
        nbttagcompound.setTag("DoorList", nbttaglist);
    }

    public DoorInfoEntry getDoorInfo(DoorPoint block)
    {
        return doorInfoMap.get(block);
    }

    public DoorInfoEntry addDoorEntry(DoorPoint block, DoorPoint travelTo)
    {
        DoorInfoEntry entry = new DoorInfoEntry(block, travelTo);
        doorInfoMap.put(block, entry);
        return entry;
    }

    public void removeDoorEntry(DoorPoint block)
    {
        doorInfoMap.remove(block);
    }

    public static class DoorInfoEntry
    {
        public DoorPoint block;
        public DoorPoint travelTo;

        public DoorInfoEntry(DoorPoint block, DoorPoint travelTo)
        {
            this.block = block;
            this.travelTo = travelTo;
        }

        public double getDistance()
        {
            Vec3 vecFrom = Vec3.createVectorHelper(block.x, block.y, block.z);
            Vec3 vecDest = Vec3.createVectorHelper(travelTo.x, travelTo.y, travelTo.z);
            return vecFrom.distanceTo(vecDest);
        }

        @Override
        public String toString()
        {
            return block.toString() + " -> " + travelTo.toString();
        }
    }

    public static class DoorPoint
    {
        public int x, y, z;

        public DoorPoint(int x, int y, int z)
        {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj == null) return false;
            if (!(obj instanceof DoorPoint)) return false;
            DoorPoint p = (DoorPoint) obj;
            return x == p.x && y == p.y && z == p.z;
        }

        @Override
        public int hashCode()
        {
            return x + y + z;
        }

        @Override
        public String toString()
        {
            return "(" + x + ", " + y + ", " + z + ")";
        }

    }
}
