package me.pulsi_.bankplus.bankSystem;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.economy.EconomyUtils;
import me.pulsi_.bankplus.values.ConfigValues;

import java.io.File;
import java.util.*;

public class BankRegistry {

    private final HashMap<String, Bank> banks = new HashMap<>();

    public HashMap<String, Bank> getBanks() {
        return banks;
    }

    public void loadBanks() {
        Set<String> currentRegisteredBanks = new HashSet<>(banks.keySet());

        List<File> bankFiles = new ArrayList<>();
        File defaultBankFile = new File(BankPlus.INSTANCE().getDataFolder(), "banks" + File.separator + ConfigValues.getMainGuiName() + ".yml");

        File[] files = new File(BankPlus.INSTANCE().getDataFolder(), "banks").listFiles();
        if (files != null && files.length > 0) bankFiles.addAll(Arrays.asList(files));

        if (!defaultBankFile.exists()) { // If the default bank file is missing, generate it.
            generateMainBankFile(defaultBankFile);
            bankFiles.add(defaultBankFile);
        }

        Set<String> newRegisteredBanks = new HashSet<>();
        for (File bankFile : bankFiles) {
            String identifier = bankFile.getName().split("\\.")[0]; // Get the name of the bank from the file.
            newRegisteredBanks.add(identifier);

            if (banks.containsKey(identifier)) { // If the registered banks already contains the bank, only update it.
                banks.get(identifier).loadBankProperties(bankFile);
                continue;
            }

            Bank bank = new Bank(identifier);
            bank.loadBankProperties(bankFile);
            banks.put(identifier, bank);
        }

        for (String currentRegisteredBank : currentRegisteredBanks)
            // Remove the banks that are no more in the files but are still in the bank holder.
            if (!newRegisteredBanks.contains(currentRegisteredBank)) banks.remove(currentRegisteredBank);

        EconomyUtils.loadEveryone(); // Load every online player in case a new bank has been created.
    }

    private void generateMainBankFile(File file) {
        BankPlus.INSTANCE().saveResource("banks" + File.separator + "bankplus_main_gui_base_file.yml", false);
        File baseBankFile = new File(BankPlus.INSTANCE().getDataFolder(), "banks" + File.separator + "bankplus_main_gui_base_file.yml");
        baseBankFile.renameTo(file);
    }
}