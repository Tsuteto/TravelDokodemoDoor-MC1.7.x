package tsuteto.tdkddoor;

import tsuteto.tdkddoor.TdkdDoorWorldInfo.DoorInfoEntry;
import tsuteto.tdkddoor.TdkdDoorWorldInfo.DoorPoint;

public class DoorInfoHandler
{
    public DoorInfoEntry getDoorInfo(int x, int y, int z)
    {
        return getDoorInfo(new DoorPoint(x, y, z));
    }
    public DoorInfoEntry getDoorInfo(DoorPoint door)
    {
        TdkdDoorSaveHandler saveHandler = this.getSaveHandler();
        return saveHandler.getWorldInfo().getDoorInfo(door);
    }

    public DoorInfoEntry addDoorEntry(DoorPoint door1, DoorPoint door2)
    {
        TdkdDoorSaveHandler saveHandler = this.getSaveHandler();
        return saveHandler.getWorldInfo().addDoorEntry(door1, door2);
    }

    public void removeDoorInfo(int x, int y, int z)
    {
        TdkdDoorSaveHandler saveHandler = this.getSaveHandler();
        saveHandler.getWorldInfo().removeDoorEntry(new DoorPoint(x, y, z));
    }

    public void save()
    {
        this.getSaveHandler().saveModInfo();
    }

    public TdkdDoorSaveHandler getSaveHandler()
    {
        return TravelDokodemoDoorMod.saveHandler;
    }


}
