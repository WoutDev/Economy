package be.woutdev.economy.persistence;

import be.woutdev.economy.Economy;
import be.woutdev.economy.api.account.Account;
import be.woutdev.economy.economy.account.HardcoreAccount;
import com.skygrind.api.API;
import com.skygrind.api.framework.user.User;
import com.skygrind.api.framework.user.profile.Profile;
import com.skygrind.core.framework.user.CoreUserManager;
import java.math.BigDecimal;
import java.util.UUID;
import org.bukkit.entity.Player;

/**
 * Edit this to match your database
 */
public class Database {

    private final Economy economy;

    public Database(Economy economy) {
        this.economy = economy;
    }

    /**
     * Load an online Player
     *
     * @param p The online Player
     * @return The Account of the online Player
     */
    public Account load(Player p) {
        User user = API.getUserManager().findByUniqueId(p.getUniqueId());

        return getAccountByUser(user);
    }

    /**
     * Load an offline UUID
     *
     * @param uuid The offline UUID
     * @return The Account matching the offline UUID or null if the Account does not exist
     */
    public Account load(UUID uuid) {
        User user = ((CoreUserManager) API.getUserManager()).getUserDataDriver().findById(uuid);

        return user == null ? null : getAccountByUser(user);
    }

    /**
     * Get the balance of an offline UUID
     *
     * @param uuid The offline UUID
     * @return The balance or null if there is no Account under the given UUID
     */
    public BigDecimal getBalance(UUID uuid) {
        Account account = load(uuid);

        return account == null ? null : account.getBalance();
    }

    /**
     * Save the Account into the database
     *
     * @param account The Account to persist
     */
    public void save(Account account) {
        User user = API.getUserManager().findByUniqueId(account.getOwner());

        if (user == null) {
            user = ((CoreUserManager) API.getUserManager()).getUserDataDriver()
                .findById(account.getOwner()); // OFFLINE LOAD

            if (user == null) {
                economy.getLogger().severe(
                    "Tried to save account that does not exist in the database! Id: " + account.getOwner().toString()
                        + ", balance: " + account.getBalance().toPlainString());
                return;
            }
        }

        Profile profile = user.getProfile("economy");

        if (profile == null) {
            profile = new Profile("economy");
            profile.set("balance", BigDecimal.ZERO);

            user.getAllProfiles().add(profile);
        }

        profile.set("balance", account.getBalance());

        user.update(); // WRITE TO DB
    }

    /**
     * Helper method for the private backend
     * Replace and/or remove if not needed
     */
    private Account getAccountByUser(User user) {
        Profile profile = user.getProfile("economy");

        if (profile == null) {
            profile = new Profile("economy");
            profile.set("balance", 0D);

            user.getAllProfiles().add(profile);
            user.update();
        }

        return new HardcoreAccount(user.getUniqueId(), new BigDecimal(profile.getDouble("balance")));
    }
}
