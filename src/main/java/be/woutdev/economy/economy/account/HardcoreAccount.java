package be.woutdev.economy.economy.account;

import be.woutdev.economy.Economy;
import be.woutdev.economy.api.account.Account;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Created by Wout on 12/08/2017.
 */
public class HardcoreAccount implements Account
{
    private final UUID owner;
    private BigDecimal balance;

    public HardcoreAccount(UUID owner, BigDecimal balance) {
        this.owner = owner;
        this.balance = balance;
    }

    @Override
    public UUID getOwner() {
        return owner;
    }

    @Override
    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance)
    {
        this.balance = balance;
    }

    @Override
    public boolean isServer() {
        return equals(Economy.getPlugin(Economy.class).getServerAccount());
    }
}
