package be.woutdev.economy.economy.transaction;

import be.woutdev.economy.api.transaction.TransactionResult;

/**
 * Created by Wout on 12/08/2017.
 */
public class HardcoreTransactionResult implements TransactionResult {

    private TransactionStatus status;

    public HardcoreTransactionResult() {
        this.status = TransactionStatus.AWAITING_QUEUE;
    }

    @Override
    public boolean isSuccess() {
        return status == TransactionStatus.SUCCESS;
    }

    @Override
    public TransactionStatus getStatus() {
        return status;
    }

    public void updateStatus(TransactionStatus status) {
        this.status = status;
    }
}
