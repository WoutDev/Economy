package be.woutdev.economy.economy.transaction;

import be.woutdev.economy.Economy;
import be.woutdev.economy.api.transaction.Transaction;
import be.woutdev.economy.api.transaction.TransactionFuture;
import be.woutdev.economy.api.transaction.TransactionResult;
import be.woutdev.economy.api.transaction.TransactionResult.TransactionStatus;
import java.util.function.Consumer;

/**
 * Created by Wout on 12/08/2017.
 */
public class HardcoreTransactionFuture implements TransactionFuture
{
    private final HardcoreTransaction transaction;

    public HardcoreTransactionFuture(HardcoreTransaction transaction) {
        this.transaction = transaction;
    }

    @Override
    public boolean cancel() {
        return Economy.getPlugin(Economy.class).getProcessor().cancel(transaction);
    }

    @Override
    public boolean isCancelled() {
        return transaction.getResult().getStatus() == TransactionStatus.CANCELLED;
    }

    @Override
    public boolean isDone() {
        return transaction.getResult().getStatus() != TransactionStatus.AWAITING_QUEUE &&
               transaction.getResult().getStatus() != TransactionStatus.QUEUED;
    }

    @Override
    public boolean isSuccess() {
        return transaction.getResult().getStatus() == TransactionStatus.SUCCESS;
    }

    @Override
    public Transaction getTransaction() {
        return transaction;
    }

    @Override
    public TransactionResult getResult() {
        return transaction.getResult();
    }

    @Override
    public void addListener(Consumer<Transaction> consumer) {
        transaction.addListener(consumer);
    }
}
