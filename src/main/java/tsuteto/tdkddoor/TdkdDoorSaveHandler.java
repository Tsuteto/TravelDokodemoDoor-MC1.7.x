package tsuteto.tdkddoor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;

public class TdkdDoorSaveHandler
{
	private final String datname = "tdkddoor";
    private final File saveDirectory;
    private final long now = System.currentTimeMillis();
    private final String saveDirectoryName;
    private TdkdDoorWorldInfo worldInfo;

    public TdkdDoorSaveHandler(File savedir, String s)
    {
        saveDirectory = savedir;
        saveDirectory.mkdirs();
        saveDirectoryName = s;
        worldInfo = loadModInfo();
    }

    protected File getSaveDirectory()
    {
        return saveDirectory;
    }

    public TdkdDoorWorldInfo getWorldInfo() {
    	if (worldInfo == null) {
    		worldInfo = new TdkdDoorWorldInfo();
    		saveModInfo();
    	}
    	return worldInfo;
    }

    private TdkdDoorWorldInfo loadModInfo()
    {
        File file = new File(saveDirectory, datname + ".dat");
        if (file.exists())
        {
            try
            {
                NBTTagCompound nbttagcompound = CompressedStreamTools.readCompressed(new FileInputStream(file));
                NBTTagCompound nbttagcompound2 = nbttagcompound.getCompoundTag("Data");
                return new TdkdDoorWorldInfo(nbttagcompound2);
            }
            catch (Exception exception)
            {
                exception.printStackTrace();
            }
        }
        file = new File(saveDirectory, datname + ".dat_old");
        if (file.exists())
        {
            try
            {
                NBTTagCompound nbttagcompound1 = CompressedStreamTools.readCompressed(new FileInputStream(file));
                NBTTagCompound nbttagcompound3 = nbttagcompound1.getCompoundTag("Data");
                return new TdkdDoorWorldInfo(nbttagcompound3);
            }
            catch (Exception exception1)
            {
                exception1.printStackTrace();
            }
        }
        return null;
    }

    public void saveModInfo()
    {
        NBTTagCompound nbttagcompound = worldInfo.getNBTTagCompound();
        NBTTagCompound nbttagcompound1 = new NBTTagCompound();
        nbttagcompound1.setTag("Data", nbttagcompound);
        try
        {
            File file = new File(saveDirectory, datname + ".dat_new");
            File file1 = new File(saveDirectory, datname + ".dat_old");
            File file2 = new File(saveDirectory, datname + ".dat");
            CompressedStreamTools.writeCompressed(nbttagcompound1, new FileOutputStream(file));
            if (file1.exists())
            {
                file1.delete();
            }
            file2.renameTo(file1);
            if (file2.exists())
            {
                file2.delete();
            }
            file.renameTo(file2);
            if (file.exists())
            {
                file.delete();
            }
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
    }

    public String getSaveDirectoryName()
    {
        return saveDirectoryName;
    }
}
