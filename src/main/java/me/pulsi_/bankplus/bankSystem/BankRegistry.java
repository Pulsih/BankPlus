package me.pulsi_.bankplus.bankSystem;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.economy.EconomyUtils;
import me.pulsi_.bankplus.utils.BPItems;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.*;

public class BankRegistry {

    private final HashMap<String, Bank> banks = new HashMap<>();

    public HashMap<String, Bank> getBanks() {
        return banks;
    }

    public Bank bankListGui;

    public void loadBanks() {
        Set<String> currentRegisteredBanks = banks.keySet();

        List<File> bankFiles = new ArrayList<>();
        File defaultBankFile = new File(BankPlus.INSTANCE().getDataFolder(), "banks" + File.separator + Values.CONFIG.getMainGuiName() + ".yml");

        File[] files = new File(BankPlus.INSTANCE().getDataFolder(), "banks").listFiles();
        if (files != null && files.length > 0) bankFiles.addAll(Arrays.asList(files));

        if (!defaultBankFile.exists()) {
            generateMainBankFile(defaultBankFile);
            bankFiles.add(defaultBankFile);
        }

        Set<String> newRegisteredBanks = new HashSet<>();
        for (File bankFile : bankFiles) {
            String identifier = bankFile.getName().split("\\.")[0];
            newRegisteredBanks.add(identifier);

            if (banks.containsKey(identifier)) {
                banks.get(identifier).updateBankSettings(bankFile);
                continue;
            }

            Bank bank = new Bank(identifier);
            bank.updateBankSettings(bankFile);
            banks.put(identifier, bank);
        }

        for (String oldRegisteredBank : currentRegisteredBanks)
            if (!newRegisteredBanks.contains(oldRegisteredBank)) banks.remove(oldRegisteredBank);

        if (Values.CONFIG.isGuiModuleEnabled() && Values.MULTIPLE_BANKS.enableMultipleBanksModule()) loadMultipleBanksGui();
        else bankListGui = null;

        EconomyUtils.loadEveryone();
    }

    public void loadMultipleBanksGui() {
        Inventory gui = Bukkit.createInventory(new BankHolder(), Math.max(9, Math.min(54, Values.MULTIPLE_BANKS.getBanksGuiLines() * 9)), "");

        if (Values.MULTIPLE_BANKS.isFillerEnabled()) {
            ItemStack filler = BPItems.getFiller(Values.MULTIPLE_BANKS.getFillerMaterial(), Values.MULTIPLE_BANKS.isFillerGlowing());
            for (int i = 0; i < gui.getSize(); i++)
                gui.setItem(i, filler);
        }

        Bank multipleBanksGui = new Bank(BankListGui.multipleBanksGuiID);
        multipleBanksGui.setTitle(Values.MULTIPLE_BANKS.getBanksGuiTitle());
        multipleBanksGui.setSize(Values.MULTIPLE_BANKS.getBanksGuiLines());
        multipleBanksGui.setUpdateDelay(Values.MULTIPLE_BANKS.getUpdateDelay());

        bankListGui = multipleBanksGui;
    }

    private void generateMainBankFile(File file) {
        BankPlus.INSTANCE().saveResource("banks" + File.separator + "bankplus_main_gui_base_file.yml", false);
        File baseBankFile = new File(BankPlus.INSTANCE().getDataFolder(), "banks" + File.separator + "bankplus_main_gui_base_file.yml");
        baseBankFile.renameTo(file);
    }
}