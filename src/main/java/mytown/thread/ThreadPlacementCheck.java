package mytown.thread;

import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import myessentials.entities.api.BlockPos;
import mytown.entities.Resident;
import mytown.protection.ProtectionManager;

/**
 * A thread which checks if there's TileEntity on given block.
 * If there isn't found one in 1 second it exits.
 */
public class ThreadPlacementCheck extends Thread {

    private static final int TIMEOUT_IN_MS = 1000;

    private final Resident res;
    private final BlockPos position;

    public ThreadPlacementCheck(Resident res, int x, int y, int z, int dim) {
        super();
        this.res = res;
        this.position = new BlockPos(x, y, z, dim);
    }

    @Override
    public void run() {
        long startTime = System.currentTimeMillis();
        World world = MinecraftServer.getServer()
            .worldServerForDimension(position.getDim());
        TileEntity te = null;
        while (te == null) {
            if (System.currentTimeMillis() - startTime >= TIMEOUT_IN_MS) {
                ProtectionManager.placementThreadTimeout();
                return;
            }
            te = world.getTileEntity(position.getX(), position.getY(), position.getZ());
        }
        ProtectionManager.addTileEntity(te, res);
    }
}
