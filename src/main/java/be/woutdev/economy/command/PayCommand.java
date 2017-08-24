package be.woutdev.economy.command;

import be.woutdev.economy.api.EconomyAPI;
import be.woutdev.economy.api.account.Account;
import be.woutdev.economy.api.transaction.Transaction;
import be.woutdev.economy.api.transaction.TransactionFuture;
import be.woutdev.economy.api.transaction.TransactionResult.TransactionStatus;
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
 * Created by Wout on 12/08/2017.
 */
public class PayCommand implements CommandExecutor
{
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        if (!(sender instanceof Player))
        {
            sender.sendMessage(ChatColor.RED + "This command is for players only!");
            return false;
        }

        Player p = (Player) sender;

        if (!p.hasPermission("economy.pay") && !p.isOp())
        {
            p.sendMessage(ChatColor.RED + "Economy: Permission denied.");
            return false;
        }

        if (args.length != 2)
        {
            sender.sendMessage(ChatColor.RED + "Usage: /pay <name> <amount>");
            return false;
        }

        OfflinePlayer recipient = Bukkit.getOfflinePlayer(args[0]);

        if (recipient == null)
        {
            sender.sendMessage(ChatColor.RED + "That player does not exist!");
            return false;
        }

        BigDecimal amount;

        try {
            amount = new BigDecimal(args[1]).setScale(2, RoundingMode.CEILING);

            if (amount.doubleValue() < 0.01)
                throw new NumberFormatException("invalid amount");
        }
        catch (NumberFormatException e)
        {
            sender.sendMessage(ChatColor.RED + "Error: Invalid amount of money!");
            return false;
        }

        Account accSender = EconomyAPI.getAPI().getAccount(p);
        Account accRecipient = EconomyAPI.getAPI().getAccount(recipient.getUniqueId()).orElse(null);

        if (accRecipient == null)
        {
            sender.sendMessage(ChatColor.RED + "Err: Recipient does not have an account!");
            return false;
        }

        Transaction transaction = EconomyAPI.getAPI().createTransaction(accSender, accRecipient, amount);

        TransactionFuture future = EconomyAPI.getAPI().transact(transaction);

        future.addListener((t) ->
        {
            switch(t.getResult().getStatus())
            {
                case SUCCESS:
                    success(p, recipient, amount);
                    break;
                case FAILED_NOT_ENOUGH_FUNDS:
                    notEnoughFunds(p, recipient, amount);
                    break;
                default:
                    error(p, recipient, amount, t.getResult().getStatus());
                    break;
            }
        });

        return true;
    }

    private void error(Player p, OfflinePlayer recipient, BigDecimal amount, TransactionStatus status)
    {
        if (p.isOnline())
        {
            p.sendMessage(String
                .format("%sFailed to transfer %s to %s! Error: %s", ChatColor.RED, EconomyAPI.getAPI().format(amount),
                    recipient.getName(), status.toString()));
        }
    }

    private void notEnoughFunds(Player p, OfflinePlayer recipient, BigDecimal amount)
    {
        if (p.isOnline())
        {
            p.sendMessage(String
                .format("%sFailed to transfer %s to %s! You have insufficient funds.", ChatColor.RED, EconomyAPI.getAPI().format(amount),
                    recipient.getName()));
        }
    }

    private void success(Player p, OfflinePlayer recipient, BigDecimal amount)
    {
        if (p.isOnline())
        {
            p.sendMessage(String
                .format("%sSuccessfully transferred %s to %s!", ChatColor.GREEN, EconomyAPI.getAPI().format(amount),
                    recipient.getName()));
        }

        if (recipient.isOnline())
        {
            ((Player) recipient).sendMessage(String.format("%sYou received %s from %s!", ChatColor.GREEN, EconomyAPI.getAPI().format(amount), p.getName()));
        }
    }
}
