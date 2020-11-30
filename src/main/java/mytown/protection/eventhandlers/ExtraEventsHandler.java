package mytown.protection.eventhandlers;


import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import myessentials.entities.api.ChunkPos;
import myessentials.utils.ColorUtils;
import org.apache.commons.lang3.StringUtils;
import myessentials.utils.WorldUtils;
import mytown.MyTown;
import mytown.new_datasource.MyTownUniverse;
import mytown.entities.TownBlock;
import mytown.entities.Wild;
import mytown.entities.flag.FlagType;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraftforge.event.world.ExplosionEvent;

import java.util.List;


/**
 * Handling any events that are not yet compatible with the most commonly used version of forge.
 */
public class ExtraEventsHandler
{

  private static ExtraEventsHandler instance;

  public static ExtraEventsHandler getInstance()
  {
    if( instance == null )
      instance = new ExtraEventsHandler();
    return instance;
  }

  /**
   * Forge 1254 is needed for this
   */
  @SubscribeEvent
  public void onExplosion( ExplosionEvent.Start ev )
  {
    if( ev.world.isRemote )
      return;
    if( ev.isCanceled() )
      return;
    List<ChunkPos> chunks = WorldUtils.getChunksInBox( ev.world.provider.dimensionId, (int) ( ev.explosion.explosionX - ev.explosion.explosionSize - 2 ), (int) ( ev.explosion.explosionZ - ev.explosion.explosionSize - 2 ), (int) ( ev.explosion.explosionX + ev.explosion.explosionSize + 2 ), (int) ( ev.explosion.explosionZ + ev.explosion.explosionSize + 2 ) );
    for( ChunkPos chunk : chunks )
    {
      TownBlock block = MyTownUniverse.instance.blocks.get( ev.world.provider.dimensionId, chunk.getX(), chunk.getZ() );
      if( block == null )
      {
        if( !(Boolean) Wild.instance.flagsContainer.getValue( FlagType.EXPLOSIONS ) )
        {
          ev.setCanceled( true );
          return;
        }
      }
      else
      {
        if( !(Boolean) block.getTown().flagsContainer.getValue( FlagType.EXPLOSIONS ) )
        {
          ev.setCanceled( true );

          StringBuilder sb = new StringBuilder();
          String[] arr = new String[]{
                  StringUtils.split(MyTown.instance.LOCAL.getLocalizationMap().get(FlagType.EXPLOSIONS.getTownNotificationKey()), "|}")[1],". Pos: ",
                  Integer.toString((int) ev.explosion.explosionX),",",
                  Integer.toString((int) ev.explosion.explosionY),",",
                  Integer.toString((int) ev.explosion.explosionZ),
                  " Dim: ",Integer.toString(block.getDim()),
                  " Town: ",block.getTown().getName()
          };

          for (int i = 0; i< arr.length; i++) {
            sb.append(arr[i]);
          }
          block.getTown().notifyEveryone(new ChatComponentText(sb.toString()).setChatStyle(new ChatStyle().setColor(ColorUtils.colorMap.get('3'))));
          return;
        }
      }
    }
  }
}
