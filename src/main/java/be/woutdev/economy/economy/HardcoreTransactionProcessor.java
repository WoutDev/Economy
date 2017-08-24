package be.woutdev.economy.economy;

import be.woutdev.economy.Economy;
import be.woutdev.economy.api.account.Account;
import be.woutdev.economy.api.transaction.TransactionResult.TransactionStatus;
import be.woutdev.economy.api.transaction.TransactionType;
import be.woutdev.economy.economy.transaction.HardcoreTransaction;
import be.woutdev.economy.economy.transaction.HardcoreTransactionResult;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import org.bukkit.Bukkit;

/**
 * Created by Wout on 12/08/2017.
 */
public class HardcoreTransactionProcessor implements Runnable {

    private final BigDecimal MAX_VALUE = new BigDecimal("2147483647");
    private final Economy economy;
    private final Queue<HardcoreTransaction> queue;

    public HardcoreTransactionProcessor(Economy economy) {
        this.economy = economy;
        this.queue = new LinkedBlockingQueue<>();
    }

    public boolean enqueue(HardcoreTransaction transaction) {
        return queue.add(transaction);
    }

    @Override
    public void run() {
        while (!queue.isEmpty()) {
            HardcoreTransaction transaction = queue.poll();

            Account sender = transaction.getSender();
            Account recipient = transaction.getRecipient();
            BigDecimal amount = transaction.getAmount();
            TransactionType type = transaction.getType();

            TransactionStatus status = TransactionStatus.SUCCESS;

            if (type == TransactionType.DEPOSIT) {
                if (sender.getBalance().compareTo(amount) < 0 && !sender.isServer()) {
                    status = TransactionStatus.FAILED_NOT_ENOUGH_FUNDS;
                }

                if (recipient.getBalance().add(amount).compareTo(MAX_VALUE) > 0 && !recipient.isServer()) {
                    status = TransactionStatus.FAILED_REACHED_LIMIT;
                }
            } else {
                if (recipient.getBalance().compareTo(amount) < 0 && !recipient.isServer()) {
                    status = TransactionStatus.FAILED_NOT_ENOUGH_FUNDS;
                }

                if (sender.getBalance().add(amount).compareTo(MAX_VALUE) > 0 && !sender.isServer()) {
                    status = TransactionStatus.FAILED_REACHED_LIMIT;
                }
            }

            if (status != TransactionStatus.SUCCESS) {
                ((HardcoreTransactionResult) transaction.getResult()).updateStatus(status);
                notifyListeners(transaction);
                continue;
            }

            BigDecimal newSenderBalance;
            BigDecimal newRecipientBalance;

            if (type == TransactionType.DEPOSIT) {
                newSenderBalance = sender.getBalance().subtract(amount);
                newRecipientBalance = recipient.getBalance().add(amount);
            } else {
                newSenderBalance = sender.getBalance().add(amount);
                newRecipientBalance = recipient.getBalance().subtract(amount);
            }

            if (!sender.isServer()) {
                economy.getCache().updateAccount(sender.getOwner(), newSenderBalance.setScale(2, RoundingMode.CEILING));
            }
            if (!recipient.isServer()) {
                economy.getCache()
                    .updateAccount(recipient.getOwner(), newRecipientBalance.setScale(2, RoundingMode.CEILING));
            }

            ((HardcoreTransactionResult) transaction.getResult()).updateStatus(status);

            notifyListeners(transaction);

            economy.getPersister().enqueue(sender);
            economy.getPersister().enqueue(recipient);
        }
    }

    private void notifyListeners(HardcoreTransaction transaction) {
        transaction.getListeners().forEach(l -> Bukkit.getScheduler().runTask(economy, () -> l.accept(transaction)));
    }

    public boolean cancel(HardcoreTransaction transaction) {
        if (queue.contains(transaction)) {
            queue.remove(transaction);

            ((HardcoreTransactionResult) transaction.getResult()).updateStatus(TransactionStatus.CANCELLED);

            notifyListeners(transaction);
            return true;
        }

        return false;
    }
}
