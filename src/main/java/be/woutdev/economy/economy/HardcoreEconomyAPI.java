package be.woutdev.economy.economy;

import be.woutdev.economy.Economy;
import be.woutdev.economy.api.EconomyAPI;
import be.woutdev.economy.api.account.Account;
import be.woutdev.economy.api.transaction.Transaction;
import be.woutdev.economy.api.transaction.TransactionFuture;
import be.woutdev.economy.api.transaction.TransactionType;
import be.woutdev.economy.economy.transaction.HardcoreTransaction;
import be.woutdev.economy.economy.transaction.HardcoreTransactionFuture;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.entity.Player;

/**
 * Created by Wout on 12/08/2017.
 */
public class HardcoreEconomyAPI extends EconomyAPI
{
    private final Economy economy;

    public HardcoreEconomyAPI(Economy economy) {
        this.economy = economy;
    }

    @Override
    public BigDecimal getBalance(Player player) {
        return economy.getCache().getAccount(player.getUniqueId()) == null ? new BigDecimal(0) : economy.getCache().getAccount(player.getUniqueId()).getBalance();
    }

    @Override
    public Optional<BigDecimal> getBalance(UUID uuid) {
        return economy.getCache().containsAccount(uuid) ? Optional.of(economy.getCache().getAccount(uuid).getBalance()) : Optional.ofNullable(economy.getDb().getBalance(uuid));
    }

    @Override
    public Account getAccount(Player player) {
        return economy.getCache().getAccount(player.getUniqueId());
    }

    @Override
    public Optional<Account> getAccount(UUID uuid) {
        if (economy.getCache().containsAccount(uuid))
            return Optional.ofNullable(economy.getCache().getAccount(uuid));

        Optional<Account> optionalAccount = Optional.ofNullable(economy.getDb().load(uuid));

        optionalAccount.ifPresent(account -> economy.getCache().putIfAbsent(account));

        return Optional.ofNullable(economy.getCache().getAccount(uuid));
    }

    @Override
    public String format(BigDecimal bigDecimal) {
        return String.format("$%s", bigDecimal.toPlainString());
    }

    @Override
    public TransactionFuture transact(Transaction transaction) {
        HardcoreTransactionFuture future = new HardcoreTransactionFuture((HardcoreTransaction) transaction);
        economy.getProcessor().enqueue((HardcoreTransaction) transaction);

        return future;
    }

    @Override
    public Transaction createTransaction(Account sender, Account recipient, BigDecimal amount) {
        return new HardcoreTransaction(recipient, sender, TransactionType.DEPOSIT, amount);
    }

    @Override
    public Transaction createTransaction(Account recipient, TransactionType type, BigDecimal amount) {
        return new HardcoreTransaction(recipient, economy.getServerAccount(), type, amount);
    }
}
