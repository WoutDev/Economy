package be.woutdev.economy.command;

import be.woutdev.economy.api.EconomyAPI;
import be.woutdev.economy.api.account.Account;
import java.math.BigDecimal;
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
public class BalanceCommand implements CommandExecutor
{
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        if (!(sender instanceof Player))
        {
            sender.sendMessage(ChatColor.RED + "This command is for players only!");
            return false;
        }

        Player p = (Player) sender;

        if (args.length == 0)
        {
            if (!p.hasPermission("economy.balance") && !p.isOp())
            {
                p.sendMessage(ChatColor.RED + "Economy: Permission denied.");
                return false;
            }

            BigDecimal balance = EconomyAPI.getAPI().getBalance(p);

            p.sendMessage(
                ChatColor.GOLD + "You currently have " + ChatColor.YELLOW + EconomyAPI.getAPI().format(balance));

            return true;
        }
        else if (args.length == 1)
        {
            if (!p.hasPermission("economy.balance.other") && !p.isOp())
            {
                p.sendMessage(ChatColor.RED + "Economy: Permission denied.");
                return false;
            }

            OfflinePlayer recipient = Bukkit.getOfflinePlayer(args[0]);

            if (recipient == null)
            {
                sender.sendMessage(ChatColor.RED + "That player does not exist!");
                return false;
            }

            Account accRecipient = EconomyAPI.getAPI().getAccount(recipient.getUniqueId()).orElse(null);

            if (accRecipient == null)
            {
                sender.sendMessage(ChatColor.RED + "Err: Recipient does not have an account!");
                return false;
            }

            sender.sendMessage(ChatColor.GOLD + recipient.getName() + " currently has " + ChatColor.YELLOW + EconomyAPI.getAPI().format(accRecipient.getBalance()));

            return true;
        }
        else
        {
            sender.sendMessage(ChatColor.RED + "Usage: /balance [player]");
            return false;
        }
    }
}
