package me.pulsi_.bankplus.bankSystem;

import me.clip.placeholderapi.PlaceholderAPI;
import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.account.BPPlayer;
import me.pulsi_.bankplus.account.PlayerRegistry;
import me.pulsi_.bankplus.utils.BPChat;
import me.pulsi_.bankplus.utils.BPLogger;
import me.pulsi_.bankplus.utils.BPUtils;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * This class is used to create different instances of the bank list gui based on the player-accessible banks.
 * It must be initialized, every gui can be different depending on the player.
 */
public class BankListGui extends BankGui {

    private final HashMap<Integer, Bank> bankListGuiClickHolder = new HashMap<>();

    public BankListGui() {
        super("BankListGui");
    }

    public void openBankListGui(Player p) {
        BPPlayer player = PlayerRegistry.get(p);

        BukkitTask updating = player.getBankUpdatingTask();
        if (updating != null) updating.cancel();

        if (Values.MULTIPLE_BANKS.isDirectlyOpenIf1IsAvailable()) {
            List<Bank> availableBanks = BankUtils.getAvailableBanks(p);
            if (availableBanks.size() == 1) {
                availableBanks.get(0).openBankGui(p);
                return;
            }
        }

        String title = Values.MULTIPLE_BANKS.getBanksGuiTitle();
        if (!BankPlus.INSTANCE().isPlaceholderApiHooked()) title = BPChat.color(title);
        else title = PlaceholderAPI.setPlaceholders(p, BPChat.color(title));

        Inventory bankListInventory = Bukkit.createInventory(new BankHolder(), Values.MULTIPLE_BANKS.getBankListGuiLines(), title);
        placeContent(getBankItems(), bankListInventory, p);
        updateBankGuiMeta(bankListInventory, p);

        long delay = Values.MULTIPLE_BANKS.getUpdateDelay();
        if (delay >= 0) player.setBankUpdatingTask(Bukkit.getScheduler().runTaskTimer(BankPlus.INSTANCE(), () -> updateBankGuiMeta(bankListInventory, p), delay, delay));

        player.setOpenedBankGui(this);
        BPUtils.playSound("PERSONAL", p);
        p.openInventory(bankListInventory);
    }

    @Override
    public void placeContent(HashMap<Integer, BankItem> items, Inventory bankInventory, Player p) {
        int slot = 0;

        for (Bank bank : BankPlus.INSTANCE().getBankRegistry().getBanks().values()) {
            if (Values.MULTIPLE_BANKS.isShowNotAvailableBanks() && !BankUtils.isAvailable(bank, p)) continue;

            BankItem bankItem = BankUtils.isAvailable(bank, p) ? bank.getAvailableBankListItem() : bank.getUnavailableBankListItem();

            bankInventory.setItem(slot, bankItem.getItem());
            bankListGuiClickHolder.put(slot, bank);
            getBankItems().put(slot, bankItem);
            slot++;
        }
    }

    public HashMap<Integer, Bank> getBankListGuiClickHolder() {
        return bankListGuiClickHolder;
    }
}