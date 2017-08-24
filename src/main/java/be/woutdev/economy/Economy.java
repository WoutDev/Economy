package be.woutdev.economy;

import be.woutdev.economy.api.EconomyAPI;
import be.woutdev.economy.api.account.Account;
import be.woutdev.economy.command.BalanceCommand;
import be.woutdev.economy.command.PayCommand;
import be.woutdev.economy.command.ResetBalanceCommand;
import be.woutdev.economy.command.ServerBankCommand;
import be.woutdev.economy.economy.AccountCache;
import be.woutdev.economy.economy.HardcoreEconomyAPI;
import be.woutdev.economy.economy.HardcoreTransactionPersister;
import be.woutdev.economy.economy.HardcoreTransactionProcessor;
import be.woutdev.economy.economy.account.HardcoreAccount;
import be.woutdev.economy.listener.PlayerJoinListener;
import be.woutdev.economy.listener.PlayerQuitListener;
import be.woutdev.economy.persistence.Database;
import java.math.BigDecimal;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Created by Wout on 12/08/2017.
 */
public class Economy extends JavaPlugin {

    private static final Account SERVER_ACCOUNT = new HardcoreAccount(
        UUID.fromString("00000000-0000-0000-0000-000000000000"), new BigDecimal(0));
    private HardcoreTransactionProcessor processor;
    private HardcoreTransactionPersister persister;
    private AccountCache cache;
    private Database db;

    @Override
    public void onEnable() {
        EconomyAPI.setAPI(new HardcoreEconomyAPI(this));

        db = new Database(this);
        processor = new HardcoreTransactionProcessor(this);
        persister = new HardcoreTransactionPersister(this);
        cache = new AccountCache();

        Bukkit.getScheduler().runTaskTimerAsynchronously(this, processor, 0L, 1L);
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, persister, 0L, 10L);

        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(this), this);

        getCommand("balance").setExecutor(new BalanceCommand());
        getCommand("pay").setExecutor(new PayCommand());
        getCommand("serverbank").setExecutor(new ServerBankCommand());
        getCommand("resetbalance").setExecutor(new ResetBalanceCommand());
    }

    public Database getDb() {
        return db;
    }

    public HardcoreTransactionProcessor getProcessor() {
        return processor;
    }

    public HardcoreTransactionPersister getPersister() {
        return persister;
    }

    public AccountCache getCache() {
        return cache;
    }

    public Account getServerAccount() {
        return SERVER_ACCOUNT;
    }
}
