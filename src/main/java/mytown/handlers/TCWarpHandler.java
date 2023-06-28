package mytown.handlers;

import java.util.Map;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;

import baubles.api.BaublesApi;
import mytown.MyTown;
import thaumcraft.api.IWarpingGear;

public class TCWarpHandler {

    private static Map<String, Integer> warpNormal;
    private static Map<String, Integer> warpTemp;
    private static Map<String, Integer> warpPermanent;

    @SuppressWarnings("unchecked")
    public static boolean tcReflect() {
        try {
            Class tc = Class.forName("thaumcraft.common.Thaumcraft");
            Object proxy = tc.getField("proxy")
                .get(null);
            Object pK = proxy.getClass()
                .getField("playerKnowledge")
                .get(proxy);
            warpNormal = (Map<String, Integer>) pK.getClass()
                .getDeclaredField("warpSticky")
                .get(pK);
            warpTemp = (Map<String, Integer>) pK.getClass()
                .getField("warpTemp")
                .get(pK);
            warpPermanent = (Map<String, Integer>) pK.getClass()
                .getField("warp")
                .get(pK);
        } catch (Exception e) {
            MyTown.instance.LOG.warn(
                "Could not reflect into thaumcraft.common.Thaumcraft to get warpNormal mappings, attempting older reflection");
            e.printStackTrace();
            try {
                Class tc = Class.forName("thaumcraft.common.Thaumcraft");
                Object proxy = tc.getField("proxy")
                    .get(null);
                Object pK = proxy.getClass()
                    .getField("playerKnowledge")
                    .get(proxy);
                warpNormal = (Map<String, Integer>) pK.getClass()
                    .getDeclaredField("warp")
                    .get(pK);
                warpTemp = (Map<String, Integer>) pK.getClass()
                    .getField("warpTemp")
                    .get(pK);
            } catch (Exception x) {
                MyTown.instance.LOG
                    .warn("Failed to reflect into thaumcraft.common.Thaumcraft to get warpNormal mapping");
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    public static int getTotalWarp(EntityPlayer pPlayer) {
        if (pPlayer == null) return 0;
        if ((warpNormal != null && warpTemp != null) || tcReflect()) {
            return ((warpPermanent != null && warpPermanent.get(pPlayer.getDisplayName()) != null)
                ? warpPermanent.get(pPlayer.getDisplayName())
                : 0) + (warpNormal.get(pPlayer.getDisplayName()) != null ? warpNormal.get(pPlayer.getDisplayName()) : 0)
                + (warpTemp.get(pPlayer.getDisplayName()) != null ? warpTemp.get(pPlayer.getDisplayName()) : 0)
                + getWarpFromGear(pPlayer);
        }
        return 0;
    }

    /***
     * Return warp added from Equipment.
     * Make sure to add sanity-checks for pPlayer when switching this to public!!
     * 
     * @param pPlayer
     * @return
     */
    private static int getWarpFromGear(EntityPlayer pPlayer) {
        int w = 0;
        if (pPlayer.getCurrentEquippedItem() != null && pPlayer.getCurrentEquippedItem()
            .getItem() instanceof IWarpingGear)
            w += ((IWarpingGear) pPlayer.getCurrentEquippedItem()
                .getItem()).getWarp(pPlayer.getCurrentEquippedItem(), pPlayer);
        IInventory baubles = BaublesApi.getBaubles(pPlayer);
        for (int i = 0; i < 4; i++) {
            if (pPlayer.inventory.getStackInSlot(i) != null && pPlayer.inventory.getStackInSlot(i)
                .getItem() instanceof IWarpingGear)
                w += ((IWarpingGear) pPlayer.inventory.getStackInSlot(i)
                    .getItem()).getWarp(pPlayer.inventory.getStackInSlot(i), pPlayer);
            if (baubles != null && baubles.getStackInSlot(i) != null
                && baubles.getStackInSlot(i)
                    .getItem() instanceof IWarpingGear)
                w += ((IWarpingGear) baubles.getStackInSlot(i)
                    .getItem()).getWarp(baubles.getStackInSlot(i), pPlayer);
        }
        return w;
    }
}
