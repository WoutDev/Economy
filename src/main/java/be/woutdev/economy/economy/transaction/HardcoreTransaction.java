package be.woutdev.economy.economy.transaction;

import be.woutdev.economy.Economy;
import be.woutdev.economy.api.account.Account;
import be.woutdev.economy.api.transaction.Transaction;
import be.woutdev.economy.api.transaction.TransactionResult;
import be.woutdev.economy.api.transaction.TransactionResult.TransactionStatus;
import be.woutdev.economy.api.transaction.TransactionType;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import org.bukkit.Bukkit;

/**
 * Created by Wout on 12/08/2017.
 */
public class HardcoreTransaction implements Transaction {

    private final Account recipient;
    private final Account sender;
    private final TransactionType type;
    private final BigDecimal amount;
    private final TransactionResult result;
    private final Set<Consumer<Transaction>> listeners;

    public HardcoreTransaction(Account recipient, Account sender,
        TransactionType type, BigDecimal amount) {
        this.recipient = recipient;
        this.sender = sender;
        this.type = type;
        this.amount = amount;
        this.result = new HardcoreTransactionResult();
        this.listeners = new HashSet<>();
    }

    @Override
    public Account getRecipient() {
        return recipient;
    }

    @Override
    public Account getSender() {
        return sender;
    }

    @Override
    public TransactionType getType() {
        return type;
    }

    @Override
    public BigDecimal getAmount() {
        return amount;
    }

    @Override
    public TransactionResult getResult() {
        return result;
    }

    public void addListener(Consumer<Transaction> consumer) {
        listeners.add(consumer);

        if (result.getStatus() != TransactionStatus.AWAITING_QUEUE &&
            result.getStatus() != TransactionStatus.QUEUED) {
            Bukkit.getScheduler().runTask(Economy.getPlugin(Economy.class),
                () -> consumer.accept(this)); // IN CASE WE QUICKLY GET PROCESSED
        }
    }

    public Set<Consumer<Transaction>> getListeners() {
        return listeners;
    }
}
