package be.woutdev.economy.command;

import be.woutdev.economy.api.EconomyAPI;
import be.woutdev.economy.api.account.Account;
import be.woutdev.economy.api.transaction.Transaction;
import be.woutdev.economy.api.transaction.TransactionFuture;
import be.woutdev.economy.api.transaction.TransactionType;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Wout on 13/08/2017.
 */
public class ServerBankCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;

            if (!p.hasPermission("economy.serverbank") && !p.isOp()) {
                p.sendMessage(ChatColor.RED + "Economy: Permission denied.");
                return false;
            }
        }

        if (args.length != 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /serverbank <withdraw/deposit/set> <player> <amount>");
            return false;
        }

        OfflinePlayer recipient = Bukkit.getOfflinePlayer(args[1]);

        if (recipient == null) {
            sender.sendMessage(ChatColor.RED + "That player does not exist!");
            return false;
        }

        BigDecimal amount;

        try {
            amount = new BigDecimal(args[2]).setScale(2, RoundingMode.CEILING);

            if (amount.doubleValue() < 0.01) {
                throw new NumberFormatException("invalid amount");
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Error: Invalid amount of money!");
            return false;
        }

        Account accRecipient = EconomyAPI.getAPI().getAccount(recipient.getUniqueId()).orElse(null);

        if (accRecipient == null) {
            sender.sendMessage(ChatColor.RED + "Err: Recipient does not have an account!");
            return false;
        }

        String action = args[0];
        Transaction transaction;

        if (action.equalsIgnoreCase("withdraw") ||
            action.equalsIgnoreCase("w")) {
            transaction = EconomyAPI.getAPI().createTransaction(accRecipient, TransactionType.WITHDRAW, amount);
        } else if (action.equalsIgnoreCase("deposit") ||
            action.equalsIgnoreCase("d")) {
            transaction = EconomyAPI.getAPI().createTransaction(accRecipient, TransactionType.DEPOSIT, amount);
        } else if (action.equalsIgnoreCase("set") ||
            action.equalsIgnoreCase("s")) {
            if (accRecipient.getBalance().doubleValue() > amount.doubleValue()) {
                transaction = EconomyAPI.getAPI().createTransaction(accRecipient, TransactionType.WITHDRAW,
                    BigDecimal.valueOf(accRecipient.getBalance().doubleValue() - amount.doubleValue()));
            } else {
                transaction = EconomyAPI.getAPI().createTransaction(accRecipient, TransactionType.DEPOSIT,
                    BigDecimal.valueOf(amount.doubleValue() - accRecipient.getBalance().doubleValue()));
            }
        } else {
            sender.sendMessage(ChatColor.RED + "Usage: /serverbank <withdraw/deposit/set> <player> <amount>");
            return false;
        }

        TransactionFuture future = EconomyAPI.getAPI().transact(transaction);

        future.addListener((t) ->
        {
            switch (t.getResult().getStatus()) {
                case SUCCESS:
                    sender.sendMessage(String.format("%sSuccessfully %s %s to/from %s's account!",
                        ChatColor.GREEN,
                        t.getType().toString(),
                        EconomyAPI.getAPI().format(t.getAmount()),
                        recipient.getName()));
                    break;
                default:
                    sender.sendMessage(String.format("%sError %s %s to/from %s's account! %s",
                        ChatColor.RED,
                        t.getType().toString(),
                        EconomyAPI.getAPI().format(t.getAmount()),
                        recipient.getName(),
                        t.getResult().getStatus().toString()));
                    break;
            }
        });

        return true;
    }
}
