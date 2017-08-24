package be.woutdev.economy.economy;

import be.woutdev.economy.api.account.Account;
import be.woutdev.economy.economy.account.HardcoreAccount;
import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Wout on 12/08/2017.
 */
public class AccountCache {

    private final ConcurrentHashMap<UUID, Account> accounts;

    public AccountCache() {
        accounts = new ConcurrentHashMap<>();
    }

    public synchronized Account getAccount(UUID uuid) {
        return accounts.getOrDefault(uuid, null);
    }

    public synchronized void removeAccount(UUID uniqueId) {
        accounts.remove(uniqueId);
    }

    public synchronized void updateAccount(UUID owner, BigDecimal newBalance) {
        ((HardcoreAccount) accounts.get(owner)).setBalance(newBalance);
    }

    public synchronized boolean containsAccount(UUID uuid) {
        return accounts.containsKey(uuid);
    }

    public synchronized void putIfAbsent(Account account) {
        accounts.putIfAbsent(account.getOwner(), account);
    }
}
