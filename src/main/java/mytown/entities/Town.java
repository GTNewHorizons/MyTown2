package mytown.entities;

import java.util.ArrayList;
import java.util.Iterator;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

import myessentials.chat.api.ChatComponentFormatted;
import myessentials.chat.api.ChatManager;
import myessentials.chat.api.IChatFormat;
import myessentials.localization.api.LocalManager;
import myessentials.teleport.Teleport;
import myessentials.utils.PlayerUtils;
import mypermissions.permission.api.proxy.PermissionProxy;
import mypermissions.permission.core.entities.PermissionLevel;
import mytown.api.container.ResidentRankMap;
import mytown.api.container.TicketMap;
import mytown.config.Config;
import mytown.entities.flag.Flag;
import mytown.entities.flag.FlagType;
import mytown.handlers.TCWarpHandler;

/**
 * Defines a Town. A Town is made up of Residents, Ranks, Blocks, and Plots.
 */
public class Town implements Comparable<Town>, IChatFormat {

    private String name, oldName = null;

    protected int maxFarClaims = Config.instance.maxFarClaims.get();

    private Nation nation;
    private Teleport spawn;

    public final TicketMap ticketMap = new TicketMap(this);
    public final ResidentRankMap residentsMap = new ResidentRankMap();
    public final Rank.Container ranksContainer = new Rank.Container();
    public final Plot.Container plotsContainer = new Plot.Container(Config.instance.defaultMaxPlots.get());
    public final Flag.Container flagsContainer = new Flag.Container();
    public final TownBlock.Container townBlocksContainer = new TownBlock.Container();
    public final BlockWhitelist.Container blockWhitelistsContainer = new BlockWhitelist.Container();

    public final Bank bank = new Bank(this);

    public Town(String name) {
        this.name = name;
    }

    /**
     * Notifies every resident in this town sending a message.
     */
    public void notifyEveryone(IChatComponent message) {
        for (Resident r : residentsMap.keySet()) {
            ChatManager.send(r.getPlayer(), message);
        }
    }

    public IChatComponent getOwnerComponent() {
        Resident mayor = residentsMap.getMayor();
        return mayor == null ? LocalManager.get("mytown.notification.town.owners.admins") : mayor.toChatMessage();
    }

    /**
     * Checks if the Resident has a low enough WarpLevel in order to Enter town.
     * TODO: Refactor this to eliminate duplicate code in hasPermission and checkWarpLevel!
     */
    public boolean checkWarpLevel(Resident res) {
        if (res == null || res.getFakePlayer()) {
            return false;
        }

        int tWarp = TCWarpHandler.getTotalWarp(res.getPlayer());
        int tMaxWarp = flagsContainer.getValue(FlagType.MAXWARP) != null ? flagsContainer.getValue(FlagType.MAXWARP)
            : FlagType.MAXWARP.defaultValue;

        if (tWarp < tMaxWarp) return true;

        boolean rankBypass;
        boolean permissionBypass;

        if (residentsMap.containsKey(res)) {
            if (flagsContainer.getValue(FlagType.RESTRICTIONS)) {
                rankBypass = hasPermission(res, FlagType.RESTRICTIONS.getBypassPermission());
                permissionBypass = PermissionProxy.getPermissionManager()
                    .hasPermission(res.getUUID(), FlagType.RESTRICTIONS.getBypassPermission());

                if (!rankBypass && !permissionBypass) {
                    ChatManager.send(res.getPlayer(), FlagType.MAXWARP.getDenialKey());
                    ChatManager.send(res.getPlayer(), "mytown.notification.town.owners", getOwnerComponent());
                    return false;
                }
            }

            rankBypass = hasPermission(res, FlagType.MAXWARP.getBypassPermission());
            permissionBypass = PermissionProxy.getPermissionManager()
                .hasPermission(res.getUUID(), FlagType.MAXWARP.getBypassPermission());

            if (!rankBypass && !permissionBypass) {
                ChatManager.send(res.getPlayer(), FlagType.MAXWARP.getDenialKey());
                ChatManager.send(res.getPlayer(), "mytown.notification.town.owners", getOwnerComponent());
                return false;
            }

        } else {
            permissionBypass = PermissionProxy.getPermissionManager()
                .hasPermission(res.getUUID(), FlagType.MAXWARP.getBypassPermission());

            if (!permissionBypass) {
                ChatManager.send(res.getPlayer(), FlagType.MAXWARP.getDenialKey());
                ChatManager.send(res.getPlayer(), "mytown.notification.town.owners", getOwnerComponent());
                return false;
            }
        }

        return true;
    }

    /**
     * Checks if the Resident is allowed to do the action specified by the FlagType at the coordinates given.
     * This method will go through all the plots and prioritize the plot's flags over town flags.
     */
    public boolean hasPermission(Resident res, FlagType<Boolean> flagType, int dim, int x, int y, int z) {
        Plot plot = plotsContainer.get(dim, x, y, z);

        if (plot == null) {
            return hasPermission(res, flagType);
        } else {
            return plot.hasPermission(res, flagType);
        }
    }

    /**
     * Checks if the Resident is allowed to do the action specified by the FlagType in this town.
     */
    public boolean hasPermission(Resident res, FlagType<Boolean> flagType) {
        if (flagType.configurable ? flagsContainer.getValue(flagType) : flagType.defaultValue) {
            return true;
        }

        if (res == null || res.getFakePlayer()) {
            return false;
        }

        boolean rankBypass;
        boolean permissionBypass;

        if (residentsMap.containsKey(res)) {
            if (flagsContainer.getValue(FlagType.RESTRICTIONS)) {
                rankBypass = hasPermission(res, FlagType.RESTRICTIONS.getBypassPermission());
                permissionBypass = PermissionProxy.getPermissionManager()
                    .hasPermission(res.getUUID(), FlagType.RESTRICTIONS.getBypassPermission());

                if (!rankBypass && !permissionBypass) {
                    ChatManager.send(res.getPlayer(), flagType.getDenialKey());
                    ChatManager.send(res.getPlayer(), "mytown.notification.town.owners", getOwnerComponent());
                    return false;
                }
            }

            rankBypass = hasPermission(res, flagType.getBypassPermission());
            permissionBypass = PermissionProxy.getPermissionManager()
                .hasPermission(res.getUUID(), flagType.getBypassPermission());

            if (!rankBypass && !permissionBypass) {
                ChatManager.send(res.getPlayer(), flagType.getDenialKey());
                ChatManager.send(res.getPlayer(), "mytown.notification.town.owners", getOwnerComponent());
                return false;
            }

        } else {
            permissionBypass = PermissionProxy.getPermissionManager()
                .hasPermission(res.getUUID(), flagType.getBypassPermission());

            if (!permissionBypass) {
                ChatManager.send(res.getPlayer(), flagType.getDenialKey());
                ChatManager.send(res.getPlayer(), "mytown.notification.town.owners", getOwnerComponent());
                return false;
            }
        }

        return true;
    }

    /**
     * Permission node check for Residents
     */
    public boolean hasPermission(Resident res, String permission) {
        if (!residentsMap.containsKey(res)) {
            return false;
        }

        Rank rank = residentsMap.get(res);
        return rank.permissionsContainer.hasPermission(permission) == PermissionLevel.ALLOWED;
    }

    public <T> T getValueAtCoords(int dim, int x, int y, int z, FlagType<T> flagType) {
        Plot plot = plotsContainer.get(dim, x, y, z);
        if (plot == null || !flagType.isPlotPerm) {
            return flagsContainer.getValue(flagType);
        } else {
            return plot.flagsContainer.getValue(flagType);
        }
    }

    /**
     * Used to get the owners of a plot (or a town) at the position given
     * Returns null if position is not in town
     */
    public Resident.Container getOwnersAtPosition(int dim, int x, int y, int z) {
        Resident.Container result = new Resident.Container();
        Plot plot = plotsContainer.get(dim, x, y, z);
        if (plot == null) {
            if (isPointInTown(dim, x, z) && !(this instanceof AdminTown) && !residentsMap.isEmpty()) {
                Resident mayor = residentsMap.getMayor();
                if (mayor != null) {
                    result.add(mayor);
                }
            }
        } else {
            for (Resident res : plot.ownersContainer) {
                result.add(res);
            }
        }
        return result;
    }

    public void sendToSpawn(Resident res) {
        EntityPlayer pl = res.getPlayer();
        if (pl != null) {
            PlayerUtils.teleport((EntityPlayerMP) pl, spawn.getDim(), spawn.getX(), spawn.getY(), spawn.getZ());
            res.setTeleportCooldown(Config.instance.teleportCooldown.get());
        }
    }

    public int getMaxFarClaims() {
        return maxFarClaims + townBlocksContainer.getExtraFarClaims();
    }

    public int getMaxBlocks() {
        int mayorBlocks = Config.instance.blocksMayor.get();
        int residentsBlocks = Config.instance.blocksResident.get() * (residentsMap.size() - 1);
        int residentsExtra = 0;
        for (Resident res : residentsMap.keySet()) {
            residentsExtra += res.getExtraBlocks();
        }
        int townExtra = townBlocksContainer.getExtraBlocks();

        return mayorBlocks + residentsBlocks + residentsExtra + townExtra;
    }

    public int getExtraBlocks() {
        int residentsExtra = 0;
        for (Resident res : residentsMap.keySet()) {
            residentsExtra += res.getExtraBlocks();
        }
        return residentsExtra + townBlocksContainer.getExtraBlocks();
    }

    /* ----- Comparable ----- */

    @Override
    public int compareTo(Town t) { // TODO Flesh this out more for ranking towns?
        int thisNumberOfResidents = residentsMap.size(), thatNumberOfResidents = t.residentsMap.size();
        if (thisNumberOfResidents > thatNumberOfResidents) return -1;
        else if (thisNumberOfResidents == thatNumberOfResidents) return 0;
        else if (thisNumberOfResidents < thatNumberOfResidents) return 1;

        return -1;
    }

    public String getName() {
        return name;
    }

    public String getOldName() {
        return oldName;
    }

    /**
     * Renames this current Town setting oldName to the previous name. You MUST set oldName to null after saving it in
     * the Datasource
     */
    public void rename(String newName) {
        oldName = name;
        name = newName;
    }

    /**
     * Resets the oldName to null. You MUST call this after a name change in the Datasource!
     */
    public void resetOldName() {
        oldName = null;
    }

    public Nation getNation() {
        return nation;
    }

    public void setNation(Nation nation) {
        this.nation = nation;
    }

    public boolean hasSpawn() {
        return spawn != null;
    }

    public Teleport getSpawn() {
        return spawn;
    }

    public void setSpawn(Teleport spawn) {
        this.spawn = spawn;
    }

    /**
     * Checks if the given block in non-chunk coordinates is in this Town
     */
    public boolean isPointInTown(int dim, int x, int z) {
        return isChunkInTown(dim, x >> 4, z >> 4);
    }

    public boolean isChunkInTown(int dim, int chunkX, int chunkZ) {
        return townBlocksContainer.contains(dim, chunkX, chunkZ);
    }

    @Override
    public String toString() {
        return toChatMessage().getUnformattedText();
    }

    @Override
    public IChatComponent toChatMessage() {
        IChatComponent header = LocalManager
            .get("myessentials.format.list.header", new ChatComponentFormatted("{9|%s}", getName()));
        IChatComponent hoverComponent = ((ChatComponentFormatted) LocalManager.get(
            "mytown.format.town.long",
            header,
            residentsMap.size(),
            townBlocksContainer.size(),
            getMaxBlocks(),
            plotsContainer.size(),
            residentsMap,
            ranksContainer)).applyDelimiter("\n");

        return LocalManager.get("mytown.format.town.short", name, hoverComponent);
    }

    public static class Container extends ArrayList<Town> implements IChatFormat {

        private Town mainTown;
        public boolean isSelectedTownSaved = false;

        @Override
        public boolean add(Town town) {
            if (mainTown == null) {
                mainTown = town;
            }
            return super.add(town);
        }

        public Town get(String name) {
            for (Town town : this) {
                if (town.getName()
                    .equals(name)) {
                    return town;
                }
            }
            return null;
        }

        public void remove(String name) {
            for (Iterator<Town> it = iterator(); it.hasNext();) {
                Town town = it.next();
                if (town.getName()
                    .equals(name)) {
                    it.remove();
                }
            }
        }

        public boolean contains(String name) {
            for (Town town : this) {
                if (town.getName()
                    .equals(name)) {
                    return true;
                }
            }
            return false;
        }

        public void setMainTown(Town town) {
            if (contains(town)) {
                mainTown = town;
            }
        }

        public Town getMainTown() {
            if (!contains(mainTown) || mainTown == null) {
                if (size() == 0) {
                    return null;
                } else {
                    mainTown = get(0);
                }
            }

            return mainTown;
        }

        @Override
        public IChatComponent toChatMessage() {
            IChatComponent root = new ChatComponentText("");

            for (Town town : this) {
                if (root.getSiblings()
                    .size() > 0) {
                    root.appendSibling(new ChatComponentFormatted("{7|, }"));
                }
                root.appendSibling(town.toChatMessage());
            }

            return root;
        }
    }
}
