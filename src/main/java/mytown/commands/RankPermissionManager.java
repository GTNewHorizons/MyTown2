package mytown.commands;

import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;

import myessentials.utils.PlayerUtils;
import mypermissions.permission.core.bridge.IPermissionBridge;
import mypermissions.permission.core.entities.PermissionLevel;
import mytown.entities.Resident;
import mytown.entities.Town;
import mytown.new_datasource.MyTownUniverse;
import mytown.util.exceptions.MyTownCommandException;

public class RankPermissionManager implements IPermissionBridge {

    @Override
    public boolean hasPermission(UUID uuid, String permission) {
        if (permission.startsWith("mytown.cmd.outsider") || permission.equals("mytown.cmd")) return true;

        EntityPlayer player = PlayerUtils.getPlayerFromUUID(uuid);
        Resident resident = MyTownUniverse.instance.getOrMakeResident(player);
        Town town = Commands.getTownFromResident(resident);
        if (town.residentsMap.get(resident).permissionsContainer.hasPermission(permission) != PermissionLevel.ALLOWED) {
            throw new MyTownCommandException("mytown.cmd.err.rankPerm");
        }
        return true;
    }
}
