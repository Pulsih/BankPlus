package me.pulsi_.bankplus.economy;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.account.BPPlayer;
import me.pulsi_.bankplus.account.PlayerRegistry;
import me.pulsi_.bankplus.bankSystem.Bank;
import me.pulsi_.bankplus.bankSystem.BankRegistry;
import me.pulsi_.bankplus.bankSystem.BankUtils;
import me.pulsi_.bankplus.events.BPAfterTransactionEvent;
import me.pulsi_.bankplus.events.BPPreTransactionEvent;
import me.pulsi_.bankplus.listeners.playerChat.PlayerChatMethod;
import me.pulsi_.bankplus.sql.BPSQL;
import me.pulsi_.bankplus.utils.BPUtils;
import me.pulsi_.bankplus.utils.texts.BPFormatter;
import me.pulsi_.bankplus.utils.texts.BPMessages;
import me.pulsi_.bankplus.values.ConfigValues;
import me.pulsi_.bankplus.values.MessageValues;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class BPEconomy {

    private final Bank originBank;
    private final HashMap<UUID, Holder> holders = new HashMap<>();
    private final Set<UUID> operations = new HashSet<>();

    private final String moneyPath, interestPath, debtPath, levelPath;

    public BPEconomy(Bank originBank) {
        this.originBank = originBank;

        String bankName = originBank.getIdentifier();
        this.moneyPath = "banks." + bankName + ".money";
        this.interestPath = "banks." + bankName + ".interest";
        this.debtPath = "banks." + bankName + ".debt";
        this.levelPath = "banks." + bankName + ".level";
    }

    /**
     * Utility method to retrieve easier an economy by its name.
     *
     * @param bankName The name of the bank name where to get the economy.
     * @return The economy of the specified bank, or null if invalid.
     */
    public static BPEconomy get(String bankName) {
        Bank bank = BankRegistry.getBank(bankName);
        if (bank == null) return null;
        return bank.getBankEconomy();
    }

    /**
     * Utility method to retrieve easier all the registered economies.
     *
     * @return A list of economies or registered banks.
     */
    public static List<BPEconomy> list() {
        List<BPEconomy> economies = new ArrayList<>();
        for (Bank bank : BankRegistry.getBanks().values()) economies.add(bank.getBankEconomy());
        return economies;
    }

    /**
     * Utility method to retrieve easier all the registered bank names.
     *
     * @return A set of registered bank names.
     */
    public static Set<String> nameList() {
        return BankRegistry.getBanks().keySet();
    }

    /**
     * Return a list with all player balances of all banks.
     * <p>
     * This is a heavy method, and it is preferable to call it asynchronously.
     *
     * @return A hashmap with the player name as KEY and the sum of all the player bank balances as VALUE.
     */
    public static LinkedHashMap<String, BigDecimal> getAllEconomiesBankBalances() {
        LinkedHashMap<String, BigDecimal> balances = new LinkedHashMap<>();
        for (OfflinePlayer p : Bukkit.getOfflinePlayers()) balances.put(p.getName(), getBankBalancesSum(p));
        return balances;
    }

    /**
     * Get the sum of player bank balances of all banks.
     *
     * @param p The player.
     */
    public static BigDecimal getBankBalancesSum(OfflinePlayer p) {
        BigDecimal amount = BigDecimal.ZERO;
        for (BPEconomy economy : list()) {
            BigDecimal add;
            if (economy.isPlayerLoaded(p)) add = economy.getBankBalance(p);
            else add = economy.getHolder(p).money;

            amount = amount.add(add);
        }
        return amount;
    }

    public Bank getOriginBank() {
        return originBank;
    }

    /**
     * Get a set of player uuids that are loaded to this economy.
     *
     * @return A set of uuids.
     */
    public Set<UUID> getLoadedPlayers() {
        return holders.keySet();
    }

    /**
     * Check if the specified player is loaded to this economy.
     * <p>
     * A loaded player will be able to do transactions, if a player hasn't
     * been loaded yet (for load cooldown, or MySQL waiting for response),
     * all his transactions will be denied to avoid dupe or other kind of problems.
     *
     * @param p The player to check.
     * @return true if the player has been loaded, false otherwise.
     */
    public boolean isPlayerLoaded(OfflinePlayer p) {
        return holders.containsKey(p.getUniqueId());
    }

    /**
     * Returns the holder instance of the specified player, this
     * method will load it completely and may take few moments.
     * <p>
     * If the player is already loaded, it will return the registered holder.
     *
     * @param p The player.
     * @return The holder of the specified player.
     */
    public Holder getHolder(OfflinePlayer p) {
        return getHolder(p, true);
    }

    /**
     * Returns the holder instance of the specified player, this
     * method will load it completely and may take few moments.
     * <p>
     * If the player is already loaded, it will return the registered holder.
     *
     * @param p             The player.
     * @param wasRegistered If false, the money in the main bank will be set as first join amount.
     * @return The holder of the specified player.
     */
    public Holder getHolder(OfflinePlayer p, boolean wasRegistered) {
        if (isPlayerLoaded(p)) return holders.get(p.getUniqueId());

        boolean useStartAmount = !wasRegistered && BankUtils.isMainBank(originBank);
        Holder holder = new Holder();

        String bankName = originBank.getIdentifier();

        holder.bankLevel = BPSQL.getBankLevel(p, bankName);
        holder.debt = BPSQL.getDebt(p, bankName);
        holder.money = useStartAmount ? ConfigValues.getStartAmount() : BPSQL.getMoney(p, bankName);

        return holder;
    }

    /**
     * Load the player information, cache it and mark the player as loaded.
     * <p>
     * It is advised to execute this method asynchronously if
     * loading from MySQL or other processes that requires time.
     *
     * @param p The player to load.
     *          return only a copy of the holder to read values.
     * @return The holder created.
     */
    public Holder loadPlayer(OfflinePlayer p) {
        return loadPlayer(p, true);
    }

    /**
     * Load the player information, cache it and mark the player as loaded.
     * <p>
     * It is advised to execute this method asynchronously if
     * loading from MySQL or other processes that requires time.
     *
     * @param p             The player to load.
     * @param wasRegistered If false, the money in the main bank will be set as first join amount.
     *                      return only a copy of the holder to read values.
     * @return The holder created.
     */
    public Holder loadPlayer(OfflinePlayer p, boolean wasRegistered) {
        Holder holder = getHolder(p, wasRegistered);
        holders.putIfAbsent(p.getUniqueId(), holder);
        return holder;
    }

    /**
     * Unload the player from this economy, deleting the cached holder.
     *
     * @param p The player.
     * @return The holder that has been removed.
     */
    public Holder unloadPlayer(OfflinePlayer p) {
        return holders.remove(p.getUniqueId());
    }

    /**
     * Get the player bank balance of the selected bank.
     * <p>
     * If the player hasn't been loaded or is offline, you should use getHolder to retrieve information.
     *
     * @param p The player.
     */
    public BigDecimal getBankBalance(OfflinePlayer p) {
        // If the player is online but is still loading its account, return 0.
        if (p.isOnline() && !isPlayerLoaded(p)) return BigDecimal.ZERO;

        // Using #getHolder fixes the problem with offline values, loading them if not
        // present in the hashmap, or getting the already registered for better performance.
        return getHolder(p).money;
    }

    /**
     * Set the player's money to the specified amount.
     * <p>
     * ! IMPORTANT !
     * Do not use methods to manage player balance in
     * BPTransactionListeners without ignoring the events,
     * it will cause an infinite loop and crash the server.
     * <p>
     * To fix that, use the method #(OfflinePlayer, BigDecimal, Boolean(ignoreEvents))
     *
     * @return Number representing the actual amount set.
     */
    public BigDecimal setBankBalance(OfflinePlayer p, BigDecimal amount) {
        return setBankBalance(p, amount, TransactionType.SET);
    }

    /**
     * Set the player's money to the specified amount.
     *
     * @param ignoreEvents Choose if ignoring or not the bankplus transaction event.
     * @return Number representing the actual amount set.
     */
    public BigDecimal setBankBalance(OfflinePlayer p, BigDecimal amount, boolean ignoreEvents) {
        return setBankBalance(p, amount, ignoreEvents, TransactionType.SET);
    }

    /**
     * Set the player's money to the specified amount.
     * <p>
     * ! IMPORTANT !
     * Do not use methods to manage player balance in
     * BPTransactionListeners without ignoring the events,
     * it will cause an infinite loop and crash the server.
     * <p>
     * To fix that, use the method #(OfflinePlayer, BigDecimal, Boolean(ignoreEvents))
     *
     * @param type Override the transaction type with the one you choose.
     * @return Number representing the actual amount set.
     */
    public BigDecimal setBankBalance(OfflinePlayer p, BigDecimal amount, TransactionType type) {
        return setBankBalance(p, amount, false, type);
    }

    private BigDecimal setBankBalance(OfflinePlayer p, BigDecimal amount, boolean ignoreEvents, TransactionType type) {
        BigDecimal result = BigDecimal.ZERO;
        if (!startAndCheckOperationAllowed(p)) return result;

        if (!ignoreEvents) {
            BPPreTransactionEvent event = preTransactionEvent(p, type, amount);
            if (event.isCancelled()) {
                endOperation(p);
                return result;
            }

            amount = event.getTransactionAmount();
        }

        BigDecimal capacity = BankUtils.getCapacity(originBank, p);
        if (capacity.compareTo(BigDecimal.ZERO) > 0) amount = amount.min(capacity);

        result = result.max(amount);
        setMoney(p, result);

        if (!ignoreEvents) afterTransactionEvent(p, type, result);
        return result;
    }

    /**
     * Add to the player's money the specified amount.
     * <p>
     * ! IMPORTANT !
     * Do not use methods to manage player balance in
     * BPTransactionListeners without ignoring the events,
     * it will cause an infinite loop and crash the server.
     * <p>
     * To fix that, use the method #(OfflinePlayer, BigDecimal, Boolean(ignoreEvents))
     *
     * @return Number representing the actual amount added.
     */
    public BigDecimal addBankBalance(OfflinePlayer p, BigDecimal amount) {
        return addBankBalance(p, amount, false, TransactionType.ADD);
    }

    /**
     * Add to the player's money the specified amount.
     *
     * @param ignoreEvents Choose if ignoring or not the bankplus transaction event.
     * @return Number representing the actual amount added.
     */
    public BigDecimal addBankBalance(OfflinePlayer p, BigDecimal amount, boolean ignoreEvents) {
        return addBankBalance(p, amount, ignoreEvents, TransactionType.ADD);
    }

    /**
     * Add to the player's money the specified amount.
     * <p>
     * ! IMPORTANT !
     * Do not use methods to manage player balance in
     * BPTransactionListeners without ignoring the events,
     * it will cause an infinite loop and crash the server.
     * <p>
     * To fix that, use the method #(OfflinePlayer, BigDecimal, Boolean(ignoreEvents))
     *
     * @param type Override the transaction type with the one you choose.
     * @return Number representing the actual amount added.
     */
    public BigDecimal addBankBalance(OfflinePlayer p, BigDecimal amount, TransactionType type) {
        return addBankBalance(p, amount, false, type);
    }

    private BigDecimal addBankBalance(OfflinePlayer p, BigDecimal amount, boolean ignoreEvents, TransactionType type) {
        BigDecimal result = BigDecimal.ZERO;
        if (!startAndCheckOperationAllowed(p)) return result;

        if (!ignoreEvents) {
            BPPreTransactionEvent event = preTransactionEvent(p, type, amount);
            if (event.isCancelled()) {
                endOperation(p);
                return result;
            }

            amount = event.getTransactionAmount();
        }

        BigDecimal capacity = BankUtils.getCapacity(originBank, p), balance = getBankBalance(p);
        if (capacity.compareTo(BigDecimal.ZERO) <= 0) {
            result = amount;
            setMoney(p, balance.add(result));
        } else {
            if (balance.add(amount).compareTo(capacity) < 0) {
                result = amount;
                setMoney(p, balance.add(result));
            } else {
                result = capacity.subtract(balance).max(BigDecimal.ZERO);
                setMoney(p, capacity);
            }
        }

        if (!ignoreEvents) afterTransactionEvent(p, type, result);
        return result;
    }

    /**
     * Remove from the player's money the specified amount.
     * <p>
     * ! IMPORTANT !
     * Do not use methods to manage player balance in
     * BPTransactionListeners without ignoring the events,
     * it will cause an infinite loop and crash the server.
     * <p>
     * To fix that, use the method #(OfflinePlayer, BigDecimal, Boolean(ignoreEvents))
     *
     * @return Number representing the actual amount removed.
     */
    public BigDecimal removeBankBalance(OfflinePlayer p, BigDecimal amount) {
        return removeBankBalance(p, amount, false, TransactionType.REMOVE);
    }

    /**
     * Remove from the player's money the specified amount.
     *
     * @param ignoreEvents Choose if ignoring or not the bankplus transaction event.
     * @return Number representing the actual amount removed.
     */
    public BigDecimal removeBankBalance(OfflinePlayer p, BigDecimal amount, boolean ignoreEvents) {
        return removeBankBalance(p, amount, ignoreEvents, TransactionType.REMOVE);
    }

    /**
     * Remove from the player's money the specified amount.
     * <p>
     * ! IMPORTANT !
     * Do not use methods to manage player balance in
     * BPTransactionListeners without ignoring the events,
     * it will cause an infinite loop and crash the server.
     * <p>
     * To fix that, use the method #(OfflinePlayer, BigDecimal, Boolean(ignoreEvents))
     *
     * @param type Override the transaction type with the one you choose.
     * @return Number representing the actual amount removed.
     */
    public BigDecimal removeBankBalance(OfflinePlayer p, BigDecimal amount, TransactionType type) {
        return removeBankBalance(p, amount, false, type);
    }

    private BigDecimal removeBankBalance(OfflinePlayer p, BigDecimal amount, boolean ignoreEvents, TransactionType type) {
        BigDecimal result = BigDecimal.ZERO;
        if (!startAndCheckOperationAllowed(p)) return result;

        if (!ignoreEvents) {
            BPPreTransactionEvent event = preTransactionEvent(p, type, amount);
            if (event.isCancelled()) {
                endOperation(p);
                return result;
            }

            amount = event.getTransactionAmount();
        }

        BigDecimal balance = getBankBalance(p);
        if (balance.subtract(amount).compareTo(BigDecimal.ZERO) < 0) result = balance;
        else result = amount;

        setMoney(p, balance.subtract(result));

        if (!ignoreEvents) afterTransactionEvent(p, type, result);
        return result;
    }

    /**
     * Get the player debt.
     *
     * @param p The player.
     */
    public BigDecimal getDebt(OfflinePlayer p) {
        if (!isPlayerLoaded(p)) return BigDecimal.ZERO;
        return getHolder(p).debt;
    }

    /**
     * Set the player debt to the specified amount.
     *
     * @param p      The player.
     * @param amount The new debt amount.
     */
    public void setDebt(OfflinePlayer p, BigDecimal amount) {
        if (!startAndCheckOperationAllowed(p)) return;
        loadPlayer(p).setDebt(amount);
        endOperation(p);
    }

    /**
     * Get the current bank level.
     *
     * @param p The player.
     * @return The current bank level.
     */
    public int getBankLevel(OfflinePlayer p) {
        if (!isPlayerLoaded(p)) return 1;
        return getHolder(p).bankLevel;
    }

    /**
     * Set the player's bank to the specified level.
     *
     * @param p     The player.
     * @param level The new level to set.
     */
    public void setBankLevel(OfflinePlayer p, int level) {
        if (!startAndCheckOperationAllowed(p)) return;
        loadPlayer(p).setBankLevel(level);
        endOperation(p);
    }

    /**
     * Method internally used to simplify the transactions, automatically end operations.
     */
    private void setMoney(OfflinePlayer p, BigDecimal amount) {
        // In set-operations, use #loadPlayer, to make sure that the instance
        // gets cached and when the saving cycle ends the edits will be applied.
        loadPlayer(p).setMoney(amount);
        endOperation(p);
    }

    /**
     * Shortcut for the deposit method of BankPlus.
     *
     * @param p      The player depositing.
     * @param amount The amount to deposit.
     */
    public void deposit(Player p, BigDecimal amount) {
        // There is no need to check for startAndCheckOperationAllowed because
        // it is done at the end of the method when adding bank balance.
        if (BPUtils.isBankFull(p, originBank)) return;

        BPPreTransactionEvent event = preTransactionEvent(p, TransactionType.DEPOSIT, amount);
        if (event.isCancelled()) return;

        amount = event.getTransactionAmount();
        if (minimumAmount(p, amount, ConfigValues.getDepositMinimumAmount())) return;

        Economy economy = BankPlus.INSTANCE().getVaultEconomy();
        BigDecimal wallet = BigDecimal.valueOf(economy.getBalance(p));
        if (!BPUtils.checkPreRequisites(wallet, amount, p)) return;

        // If it's more than the player's wallet, use the wallet amount.
        if (wallet.compareTo(amount) < 0) amount = wallet;

        BigDecimal maxDeposit = ConfigValues.getMaxDepositAmount(); // If it's more than the max deposit amount, use the max amount.
        if (maxDeposit.compareTo(BigDecimal.ZERO) > 0 && amount.compareTo(maxDeposit) >= 0) amount = maxDeposit;

        BigDecimal capacity = BankUtils.getCapacity(originBank, p).subtract(getBankBalance(p)), finalAmount = amount.min(capacity);

        BigDecimal depositTaxes = ConfigValues.getDepositTaxes(), taxes = BigDecimal.ZERO;
        if (depositTaxes.compareTo(BigDecimal.ZERO) > 0 && !p.hasPermission("bankplus.deposit.bypass-taxes")) {
            if (amount.compareTo(capacity) <= 0)
                taxes = finalAmount.multiply(depositTaxes.divide(BigDecimal.valueOf(100)));
            else {
                // Makes it able to fill the bank if a higher number is used.
                taxes = capacity.multiply(depositTaxes).divide(BigDecimal.valueOf(100), RoundingMode.DOWN);
                taxes = taxes.min(finalAmount).max(BigDecimal.ZERO);
                finalAmount = finalAmount.add(taxes);
            }
        }

        BigDecimal actualDepositingMoney = finalAmount.subtract(taxes);

        EconomyResponse depositResponse = economy.withdrawPlayer(p, finalAmount.doubleValue());
        if (BPUtils.hasFailed(p, depositResponse)) return;

        addBankBalance(p, actualDepositingMoney, true);

        List<String> replacers = BPUtils.placeValues(p, actualDepositingMoney);
        replacers.addAll(BPUtils.placeValues(taxes, "taxes"));
        BPMessages.sendIdentifier(p, "Success-Deposit", replacers);

        if (ConfigValues.isDepositSoundEnabled()) BPUtils.playSound(ConfigValues.getDepositSound(), p);

        afterTransactionEvent(p, TransactionType.DEPOSIT, amount);
    }

    /**
     * Shortcut for the withdraw method of BankPlus.
     *
     * @param p      The player withdrawing.
     * @param amount The amount to withdraw.
     */
    public void withdraw(Player p, BigDecimal amount) {
        // There is no need to check for startAndCheckOperationAllowed because
        // it is done at the end of the method when removing bank balance.
        BPPreTransactionEvent event = preTransactionEvent(p, TransactionType.WITHDRAW, amount);
        if (event.isCancelled()) return;

        amount = event.getTransactionAmount();
        if (minimumAmount(p, amount, ConfigValues.getWithdrawMinimumAmount())) return;

        BigDecimal bankBal = getBankBalance(p);
        if (!BPUtils.checkPreRequisites(bankBal, amount, p)) return;

        BigDecimal maxWithdraw = ConfigValues.getMaxWithdrawAmount();
        if (maxWithdraw.compareTo(BigDecimal.ZERO) > 0 && amount.compareTo(maxWithdraw) >= 0) amount = maxWithdraw;
        if (bankBal.compareTo(amount) < 0) amount = bankBal;

        BigDecimal withdrawTaxes = ConfigValues.getWithdrawTaxes(), taxes = BigDecimal.ZERO;
        if (withdrawTaxes.compareTo(BigDecimal.ZERO) > 0 && !p.hasPermission("bankplus.withdraw.bypass-taxes")) {
            taxes = amount.multiply(withdrawTaxes).divide(BigDecimal.valueOf(100), RoundingMode.DOWN);
            taxes = taxes.min(amount).max(BigDecimal.ZERO);
        }

        EconomyResponse withdrawResponse = BankPlus.INSTANCE().getVaultEconomy().depositPlayer(p, amount.subtract(taxes).doubleValue());
        if (BPUtils.hasFailed(p, withdrawResponse)) return;

        removeBankBalance(p, amount, true);

        List<String> replacers = BPUtils.placeValues(p, amount.subtract(taxes));
        replacers.addAll(BPUtils.placeValues(taxes, "taxes"));
        BPMessages.sendIdentifier(p, "Success-Withdraw", replacers);

        if (ConfigValues.isWithdrawSoundEnabled()) BPUtils.playSound(ConfigValues.getWithdrawSound(), p);

        afterTransactionEvent(p, TransactionType.WITHDRAW, amount);
    }

    /**
     * Initialize the custom withdraw task for the selected player (Withdraw through chat).
     *
     * @param p The player.
     */
    public void customDeposit(Player p) {
        if (MessageValues.isTitleCustomAmountEnabled()) BPUtils.sendTitle(MessageValues.getCustomDepositTitle(), p);

        BPMessages.sendIdentifier(p, "Chat-Deposit");
        p.closeInventory();

        BPPlayer bpPlayer = PlayerRegistry.get(p);
        Bank bank = getOriginBank();
        bpPlayer.setOpenedBank(bank);
        bpPlayer.setDepositing(true);

        bpPlayer.setClosingTask(Bukkit.getScheduler().runTaskLater(BankPlus.INSTANCE(), () -> {
            PlayerChatMethod.reopenBank(bpPlayer, bank.getBankGui());
            BPMessages.sendIdentifier(p, "Chat-Time-Expired");
        }, ConfigValues.getChatExitTime() * 20L));
    }

    /**
     * Initialize the custom deposit task for the selected player (Deposit through chat).
     *
     * @param p The player.
     */
    public void customWithdraw(Player p) {
        if (MessageValues.isTitleCustomAmountEnabled()) BPUtils.sendTitle(MessageValues.getCustomWithdrawTitle(), p);

        BPMessages.sendIdentifier(p, "Chat-Withdraw");
        p.closeInventory();

        BPPlayer bpPlayer = PlayerRegistry.get(p);
        Bank bank = getOriginBank();
        bpPlayer.setOpenedBank(bank);
        bpPlayer.setWithdrawing(true);

        bpPlayer.setClosingTask(Bukkit.getScheduler().runTaskLater(BankPlus.INSTANCE(), () -> {
            PlayerChatMethod.reopenBank(bpPlayer, bank.getBankGui());
            BPMessages.sendIdentifier(p, "Chat-Time-Expired");
        }, ConfigValues.getChatExitTime() * 20L));
    }

    /**
     * Method used to execute the pay transaction.
     *
     * @param from   The player that will give the money.
     * @param to     The player that will receive your money.
     * @param amount How much money you want to pay.
     * @param toBank The bank where the money will be added.
     */
    public void pay(Player from, Player to, BigDecimal amount, Bank toBank) {
        if (!startAndCheckOperationAllowed(from) || !startAndCheckOperationAllowed(to)) return;

        BigDecimal senderBalance = getBankBalance(from);
        // Check if the sender has at least more than 0 money
        if (senderBalance.compareTo(amount) < 0) {
            BPMessages.sendIdentifier(from, "Insufficient-Money");
            return;
        }

        BPEconomy toEconomy = toBank.getBankEconomy();
        // Check if the receiver of the payment has the bank full
        if (toEconomy.getBankBalance(to).compareTo(BankUtils.getCapacity(toBank, to)) >= 0) {
            BPMessages.sendIdentifier(from, "Bank-Full", "%player%$" + to.getName());
            return;
        }

        BigDecimal added = toEconomy.addBankBalance(to, amount, TransactionType.PAY), extra = amount.subtract(added);
        BPMessages.sendIdentifier(to, "Payment-Received", BPUtils.placeValues(from, added));

        BigDecimal removed = removeBankBalance(from, amount.subtract(extra), TransactionType.PAY);
        BPMessages.sendIdentifier(from, "Payment-Sent", BPUtils.placeValues(to, removed));
    }

    private boolean minimumAmount(Player p, BigDecimal amount, BigDecimal minimum) {
        if (amount.compareTo(minimum) < 0) {
            BPMessages.sendIdentifier(p, "Minimum-Number", "%min%$" + minimum);
            return true;
        }
        return false;
    }

    private BPPreTransactionEvent preTransactionEvent(OfflinePlayer p, TransactionType type, BigDecimal amount) {
        BPPreTransactionEvent event = new BPPreTransactionEvent(
                p, type, getBankBalance(p), BankPlus.INSTANCE().getVaultEconomy().getBalance(p), amount, originBank.getIdentifier()
        );
        BPUtils.callEvent(event);
        return event;
    }

    private void afterTransactionEvent(OfflinePlayer p, TransactionType type, BigDecimal amount) {
        BPAfterTransactionEvent event = new BPAfterTransactionEvent(
                p, type, getBankBalance(p), BankPlus.INSTANCE().getVaultEconomy().getBalance(p), amount, originBank.getIdentifier()
        );
        BPUtils.callEvent(event);
    }

    /**
     * Check if the specified player is allowed to perform any type of operation.
     * <p>
     * This method will deny operations in case the player is online but
     * not yet loaded, or when he is already performing another operation.
     * <p>
     * This method, once called, will also automatically set the player
     * in an operating state if all the other check passes.
     *
     * @param p The player to check.
     * @return true if the transaction is allowed, false otherwise.
     */
    private boolean startAndCheckOperationAllowed(OfflinePlayer p) {
        if ((p.isOnline() && !isPlayerLoaded(p)) || operations.contains(p.getUniqueId())) return false;

        operations.add(p.getUniqueId());
        return true;
    }

    /**
     * Remove the specified player from the operating state.
     *
     * @param p The player.
     */
    private void endOperation(OfflinePlayer p) {
        operations.remove(p.getUniqueId());
    }

    private static class Holder {

        private BigDecimal money = BigDecimal.ZERO, debt = BigDecimal.ZERO;
        private int bankLevel = 1;

        public void setMoney(BigDecimal money) {
            this.money = money.max(BigDecimal.ZERO);
        }

        public void setDebt(BigDecimal debt) {
            this.debt = debt.max(BigDecimal.ZERO);
        }

        public void setBankLevel(int bankLevel) {
            this.bankLevel = Math.max(1, bankLevel);
        }
    }
}