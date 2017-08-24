package be.woutdev.economy.listener;

import be.woutdev.economy.Economy;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Created by Wout on 12/08/2017.
 */
public class PlayerQuitListener implements Listener
{
    private final Economy economy;

    public PlayerQuitListener(Economy economy) {
        this.economy = economy;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e)
    {
        economy.getCache().removeAccount(e.getPlayer().getUniqueId());
    }
}
