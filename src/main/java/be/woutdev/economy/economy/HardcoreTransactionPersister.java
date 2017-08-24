package be.woutdev.economy.economy;

import be.woutdev.economy.Economy;
import be.woutdev.economy.api.account.Account;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Wout on 12/08/2017.
 */
public class HardcoreTransactionPersister implements Runnable {

    private final Economy economy;
    private final Queue<Account> queue;

    public HardcoreTransactionPersister(Economy economy) {
        this.economy = economy;
        this.queue = new LinkedBlockingQueue<>();
    }

    @Override
    public void run() {
        while (!queue.isEmpty()) {
            Account account = queue.poll();

            if (account.isServer()) {
                continue;
            }

            economy.getDb().save(account);
        }
    }

    public boolean enqueue(Account account) {
        return queue.add(account);
    }
}
