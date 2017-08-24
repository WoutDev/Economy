package be.woutdev.economy.command;

import be.woutdev.economy.api.EconomyAPI;
import be.woutdev.economy.api.account.Account;
import be.woutdev.economy.api.transaction.Transaction;
import be.woutdev.economy.api.transaction.TransactionType;
import java.util.Optional;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Wout on 15/08/2017.
 */
public class ResetBalanceCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;

            if (!p.hasPermission("economy.resetbalance") && !p.isOp()) {
                p.sendMessage(ChatColor.RED + "Economy: Permission denied.");
                return false;
            }
        }

        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /resetbalance <name>");
            return false;
        }

        OfflinePlayer recipient = Bukkit.getOfflinePlayer(args[0]);

        if (recipient == null) {
            sender.sendMessage(ChatColor.RED + "That player does not exist!");
            return false;
        }

        Optional<Account> account = EconomyAPI.getAPI().getAccount(recipient.getUniqueId());

        if (!account.isPresent()) {
            sender.sendMessage(ChatColor.RED + "That player does not exist!");
            return false;
        }

        Transaction transaction = EconomyAPI.getAPI()
            .createTransaction(account.get(), TransactionType.WITHDRAW, account.get().getBalance());

        EconomyAPI.getAPI().transact(transaction).addListener((t) -> {
            if (t.getResult().isSuccess()) {
                sender.sendMessage(ChatColor.GREEN + "Successfully reset balance of " + recipient.getName() + "!");
            } else {
                sender.sendMessage(
                    ChatColor.RED + "Error resetting balance of " + recipient.getName() + "! Error: " + t.getResult()
                        .getStatus());
            }
        });

        return true;
    }
}
