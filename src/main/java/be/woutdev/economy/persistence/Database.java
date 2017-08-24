package be.woutdev.economy.persistence;

import be.woutdev.economy.Economy;
import be.woutdev.economy.api.account.Account;
import be.woutdev.economy.economy.account.HardcoreAccount;
import com.skygrind.api.API;
import com.skygrind.api.framework.user.User;
import com.skygrind.api.framework.user.profile.Profile;
import com.skygrind.core.framework.user.CoreUserManager;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.UUID;
import org.bukkit.entity.Player;

/**
 * Edit this to match your database
 */
public class Database
{
    private final Economy economy;

    public Database(Economy economy)
    {
        this.economy = economy;
    }

    public Account load(Player p)
    {
        User user = API.getUserManager().findByUniqueId(p.getUniqueId());

        return getAccountByUser(user);
    }

    public Account load(UUID uuid)
    {
        User user = ((CoreUserManager) API.getUserManager()).getUserDataDriver().findById(uuid);

        return user == null ? null : getAccountByUser(user);
    }

    public BigDecimal getBalance(UUID uuid)
    {
        Account account = load(uuid);

        return account == null ? null : account.getBalance();
    }

    private Account getAccountByUser(User user)
    {
        Profile profile = user.getProfile("economy");

        if (profile == null)
        {
            profile = new Profile("economy");
            profile.set("balance", 0D);

            user.getAllProfiles().add(profile);
            user.update();
        }

        return new HardcoreAccount(user.getUniqueId(), new BigDecimal(profile.getDouble("balance")));
    }

    public void save(Account account)
    {
        User user = API.getUserManager().findByUniqueId(account.getOwner());

        if (user == null)
        {
            user = ((CoreUserManager) API.getUserManager()).getUserDataDriver().findById(account.getOwner()); // OFFLINE LOAD

            if (user == null)
            {
                economy.getLogger().severe("Tried to save account that does not exist in the database! Id: " + account.getOwner().toString() + ", balance: " + account.getBalance().toPlainString());
                return;
            }
        }

        Profile profile = user.getProfile("economy");

        if (profile == null)
        {
            profile = new Profile("economy");
            profile.set("balance", BigDecimal.ZERO);

            user.getAllProfiles().add(profile);
        }

        profile.set("balance", account.getBalance());

        user.update(); // WRITE TO DB
    }
}
