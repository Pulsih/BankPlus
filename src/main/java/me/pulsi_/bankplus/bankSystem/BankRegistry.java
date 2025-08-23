package me.pulsi_.bankplus.bankSystem;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.account.PlayerRegistry;
import me.pulsi_.bankplus.values.ConfigValues;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.*;

public class BankRegistry {

    private static final HashMap<String, Bank> banks = new HashMap<>();

    public static Bank getBank(String bankName) {
        return banks.get(bankName);
    }

    public static HashMap<String, Bank> getBanks() {
        return banks;
    }

    public static void loadBanks() {
        Set<String> currentBanks = new HashSet<>(banks.keySet()), newBanks = new HashSet<>();;

        // Get the folder of the bank files.
        File banksFolder = new File(BankPlus.INSTANCE().getDataFolder(), "banks");

        List<File> bankFiles = new ArrayList<>();
        File[] availableFiles = banksFolder.listFiles(); // Get the list of files created in the banks folder.
        if (availableFiles != null && availableFiles.length > 0) bankFiles.addAll(Arrays.asList(availableFiles));

        File defaultBankFile = new File(banksFolder, ConfigValues.getMainGuiName() + ".yml");
        if (!defaultBankFile.exists()) { // If the default bank file is missing, generate it.
            generateMainBankFile(defaultBankFile);

            // If the default bank file is already present in
            // the list, do not add it to avoid duplicated files.
            boolean add = true;
            for (File file : bankFiles)
                if (file.getName().equals(defaultBankFile.getName())) {
                    add = false;
                    break;
                }
            if (add) bankFiles.add(defaultBankFile);
        }

        for (File bankFile : bankFiles) {
            String identifier = bankFile.getName().split("\\.")[0]; // Get the name of the bank from the file.
            newBanks.add(identifier);

            if (banks.containsKey(identifier)) { // If the registered banks already contains the bank, only update it.
                banks.get(identifier).loadBankProperties(bankFile);
                continue;
            }

            Bank bank = new Bank(identifier);
            bank.loadBankProperties(bankFile);
            banks.put(identifier, bank);

            for (Player p : Bukkit.getOnlinePlayers()) // Load the players to the bank economy once registered.
                PlayerRegistry.loadPlayer(p, bank,true);
        }

        for (String bank : currentBanks)
            // Remove the banks that are no more in the files but are still in the bank holder.
            if (!newBanks.contains(bank)) banks.remove(bank);
    }

    private static void generateMainBankFile(File file) {
        BankPlus.INSTANCE().saveResource("banks" + File.separator + "bankplus_main_gui_base_file.yml", false);
        File baseBankFile = new File(BankPlus.INSTANCE().getDataFolder(), "banks" + File.separator + "bankplus_main_gui_base_file.yml");
        baseBankFile.renameTo(file);
    }
}