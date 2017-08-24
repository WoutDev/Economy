package be.woutdev.economy.listener;

import be.woutdev.economy.Economy;
import be.woutdev.economy.api.account.Account;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Created by Wout on 12/08/2017.
 */
public class PlayerJoinListener implements Listener {

    private final Economy economy;

    public PlayerJoinListener(Economy economy) {
        this.economy = economy;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Account account = economy.getDb().load(e.getPlayer());

        economy.getCache().putIfAbsent(account);
    }
}
