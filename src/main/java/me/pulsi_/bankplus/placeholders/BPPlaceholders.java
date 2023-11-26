package me.pulsi_.bankplus.placeholders;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.bankSystem.BankManager;
import me.pulsi_.bankplus.economy.BPEconomy;
import me.pulsi_.bankplus.interest.BPInterest;
import me.pulsi_.bankplus.managers.BPBankTop;
import me.pulsi_.bankplus.utils.BPChat;
import me.pulsi_.bankplus.utils.BPFormatter;
import me.pulsi_.bankplus.utils.BPUtils;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.util.List;

public class BPPlaceholders extends PlaceholderExpansion {

    private final BPInterest interest;
    private final BPEconomy economy;

    public BPPlaceholders() {
        interest = BankPlus.INSTANCE().getInterest();
        economy = BankPlus.getBPEconomy();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String getAuthor() {
        return "Pulsi_";
    }

    @Override
    public String getIdentifier() {
        return "bankplus";
    }

    @Override
    public String getVersion() {
        return BankPlus.INSTANCE().getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player p, String identifier) {
        if (p == null) return "Player not online";

        BPBankTop bankTop = BankPlus.INSTANCE().getBankTopManager();

        String target = null;
        if (identifier.contains("{") && identifier.endsWith("}"))
            target = identifier.substring(identifier.indexOf("{") + 1, identifier.indexOf("}"));

        if (identifier.startsWith("capacity")) {
            BankManager reader = BankPlus.getBankManager();
            String bankName = Values.CONFIG.getMainGuiName();

            if (target != null) {
                bankName = target;
                if (!reader.exist(bankName)) return "&cThe selected bank does not exist.";
            }

            BigDecimal capacity = reader.getCapacity(bankName, p);
            if (capacity.longValue() <= 0) return Values.CONFIG.getInfiniteCapacityText();

            return getFormat(identifier, capacity);
        }

        if (identifier.startsWith("level")) {
            BankManager manager = BankPlus.getBankManager();
            String bankName = Values.CONFIG.getMainGuiName();

            if (target != null) {
                bankName = target;
                if (!manager.exist(bankName)) return "&cThe selected bank does not exist.";
            }

            return String.valueOf(manager.getCurrentLevel(bankName, p));
        }

        if (identifier.startsWith("next_level_cost")) {
            BankManager manager = BankPlus.getBankManager();
            String bankName = Values.CONFIG.getMainGuiName();

            if (target != null) {
                bankName = target;
                if (!manager.exist(bankName)) return "&cThe selected bank does not exist.";
            }

            if (!manager.hasNextLevel(bankName, p))
                return Values.CONFIG.getUpgradesMaxedPlaceholder();

            BigDecimal cost = manager.getLevelCost(bankName, manager.getCurrentLevel(bankName, p) + 1);

            return getFormat(identifier, cost);
        }

        if (identifier.startsWith("next_level_capacity")) {
            BankManager manager = BankPlus.getBankManager();
            String bankName = Values.CONFIG.getMainGuiName();

            if (target != null) {
                bankName = target;
                if (!manager.exist(bankName)) return "&cThe selected bank does not exist.";
            }

            if (!manager.hasNextLevel(bankName, p))
                return Values.CONFIG.getUpgradesMaxedPlaceholder();

            BigDecimal capacity = manager.getCapacity(bankName, manager.getCurrentLevel(bankName, p) + 1);
            if (capacity.longValue() <= 0) return Values.CONFIG.getInfiniteCapacityText();

            return getFormat(identifier, capacity);
        }

        if (identifier.startsWith("next_level_required_items")) {
            BankManager manager = BankPlus.getBankManager();
            String bankName = Values.CONFIG.getMainGuiName();

            if (target != null) {
                bankName = target;
                if (!manager.exist(bankName)) return "&cThe selected bank does not exist.";
            }

            if (!manager.hasNextLevel(bankName, p))
                return Values.CONFIG.getUpgradesMaxedPlaceholder();

            List<ItemStack> requiredItems = manager.getRequiredItems(bankName, manager.getCurrentLevel(bankName, p) + 1);
            if (requiredItems == null) return Values.CONFIG.getUpgradesNoRequiredItems();
            return BPUtils.getRequiredItems(requiredItems);
        }

        if (identifier.startsWith("next_level_interest_rate")) {
            BankManager manager = BankPlus.getBankManager();
            String bankName = Values.CONFIG.getMainGuiName();

            if (target != null) {
                bankName = target;
                if (!manager.exist(bankName)) return "&cThe selected bank does not exist.";
            }

            if (!manager.hasNextLevel(bankName, p))
                return Values.CONFIG.getUpgradesMaxedPlaceholder();

            return manager.getInterestRate(bankName, p, manager.getCurrentLevel(bankName, p) + 1) + "%";
        }

        if (identifier.startsWith("next_level_offline_interest_rate")) {
            BankManager manager = BankPlus.getBankManager();
            String bankName = Values.CONFIG.getMainGuiName();

            if (target != null) {
                bankName = target;
                if (!manager.exist(bankName)) return "&cThe selected bank does not exist.";
            }

            if (!manager.hasNextLevel(bankName, p))
                return Values.CONFIG.getUpgradesMaxedPlaceholder();

            return manager.getOfflineInterestRate(bankName, p, manager.getCurrentLevel(bankName, p) + 1) + "%";
        }

        if (identifier.startsWith("next_level")) {
            BankManager manager = BankPlus.getBankManager();
            String bankName = Values.CONFIG.getMainGuiName();

            if (target != null) {
                bankName = target;
                if (!manager.exist(bankName)) return "&cThe selected bank does not exist.";
            }

            if (!manager.hasNextLevel(bankName, p))
                return Values.CONFIG.getUpgradesMaxedPlaceholder();

            return (manager.getCurrentLevel(bankName, p) + 1) + "";
        }

        if (identifier.startsWith("debt")) {
            String formatter = identifier.replace("debt", "");
            return getFormat(formatter, BankPlus.getBPEconomy().getDebts(p));
        }

        if (identifier.startsWith("balance")) {
            BigDecimal bal = economy.getBankBalance(p);

            if (target != null) {
                if (!BankPlus.getBankManager().exist(target))
                    return "&cThe selected bank does not exist.";
                bal = economy.getBankBalance(p, target);
            }

            return getFormat(identifier, bal);
        }

        if (identifier.startsWith("next_interest")) {
            BankManager manager = BankPlus.getBankManager();
            String bankName = Values.CONFIG.getMainGuiName();

            if (target != null) {
                bankName = target;
                if (!manager.exist(bankName)) return "&cThe selected bank does not exist.";
            }

            return getFormat(identifier, interest.getInterestMoney(bankName, p, manager.getInterestRate(bankName, p)));
        }

        if (identifier.startsWith("next_offline_interest")) {
            BankManager manager = BankPlus.getBankManager();
            String bankName = Values.CONFIG.getMainGuiName();

            if (target != null) {
                bankName = target;
                if (!manager.exist(bankName)) return "&cThe selected bank does not exist.";
            }

            return getFormat(identifier, interest.getInterestMoney(bankName, p, manager.getOfflineInterestRate(bankName, p)));
        }

        if (identifier.startsWith("banktop_money_")) {
            if (!Values.CONFIG.isBanktopEnabled())
                return BPChat.color("&cThe banktop is not enabled!");

            String number = identifier.replace("banktop_money_", "");
            int position;
            try {
                position = Integer.parseInt(number);
            } catch (NumberFormatException e) {
                return "&cInvalid banktop number!";
            }

            if (position > Values.CONFIG.getBankTopSize())
                return "&cThe banktop limit is " + Values.CONFIG.getBankTopSize() + "!";

            BigDecimal money = bankTop.getBankTopBalancePlayer(position);
            switch (Values.CONFIG.getBankTopMoneyFormat()) {
                case "default_amount":
                    return BPFormatter.formatCommas(money);
                case "amount_long":
                    return String.valueOf(money);
                default:
                    return BPFormatter.format(money);
                case "amount_formatted_long":
                    return BPFormatter.formatLong(money);
            }
        }

        if (identifier.startsWith("banktop_name_")) {
            if (!Values.CONFIG.isBanktopEnabled())
                return BPChat.color("&cThe banktop is not enabled!");

            String number = identifier.replace("banktop_name_", "");
            int position;
            try {
                position = Integer.parseInt(number);
            } catch (NumberFormatException e) {
                return "&cInvalid banktop number!";
            }

            if (position > Values.CONFIG.getBankTopSize())
                return "&cThe banktop limit is " + Values.CONFIG.getBankTopSize() + "!";

            return bankTop.getBankTopNamePlayer(position);
        }

        if (identifier.startsWith("banktop_position")) {
            if (!Values.CONFIG.isBanktopEnabled())
                return BPChat.color("&cThe banktop is not enabled!");

            Player result = p;
            if (target != null) {
                Player pTarget = Bukkit.getPlayer(target);

                if (pTarget == null)
                    return "&cThe selected player does not exist!";

                result = pTarget;
            }

            return bankTop.getPlayerBankTopPosition(result) + "";
        }

        if (identifier.startsWith("interest_rate")) {
            BankManager manager = BankPlus.getBankManager();
            String bankName = Values.CONFIG.getMainGuiName();

            if (target != null) {
                bankName = target;
                if (!manager.exist(bankName)) return "&cThe selected bank does not exist.";
            }

            return manager.getInterestRate(bankName, p) + "%";
        }

        if (identifier.startsWith("offline_interest_rate")) {
            BankManager manager = BankPlus.getBankManager();
            String bankName = Values.CONFIG.getMainGuiName();

            if (target != null) {
                bankName = target;
                if (!manager.exist(bankName)) return "&cThe selected bank does not exist.";
            }

            return manager.getOfflineInterestRate(bankName, p) + "%";
        }

        if (identifier.startsWith("calculate_deposit_taxes_")) {
            String secondIdentifier = identifier.replace("calculate_deposit_taxes_", "");

            BigDecimal taxes = null;
            if (secondIdentifier.startsWith("number_")) {
                String number = secondIdentifier.replace("number_", "");

                BigDecimal amount;
                if (target != null) {
                    if (!BankPlus.getBankManager().exist(target))
                        return "The selected bank does not exist.";

                    number = number.replace("{" + target + "}", "");
                }

                try {
                    amount = new BigDecimal(number.replace("%", ""));
                } catch (NumberFormatException e) {
                    return "Invalid Number!";
                }

                taxes = amount.multiply(Values.CONFIG.getDepositTaxes().divide(BigDecimal.valueOf(100)));
            }

            if (secondIdentifier.startsWith("percentage_")) {
                String number = secondIdentifier.replace("percentage_", "");

                BigDecimal amount, balance;
                try {
                    balance = BigDecimal.valueOf(BankPlus.INSTANCE().getVaultEconomy().getBalance(p));
                } catch (NumberFormatException e) {
                    return "Invalid vault balance!";
                }

                try {
                    amount = new BigDecimal(number.replace("%", ""));
                } catch (NumberFormatException e) {
                    return "Invalid Number!";
                }

                BigDecimal percentageBalance = balance.multiply(amount.divide(BigDecimal.valueOf(100)));
                taxes = percentageBalance.multiply(Values.CONFIG.getDepositTaxes().divide(BigDecimal.valueOf(100)));
            }
            return getFormat(identifier, taxes);
        }

        if (identifier.startsWith("calculate_withdraw_taxes_")) {
            String secondIdentifier = identifier.replace("calculate_withdraw_taxes_", "");

            BigDecimal taxes = null;
            if (secondIdentifier.startsWith("number_")) {
                String number = secondIdentifier.replace("number_", "");

                BigDecimal amount;
                if (target != null) {
                    if (!BankPlus.getBankManager().exist(target))
                        return "The selected bank does not exist.";

                    number = number.replace("{" + target + "}", "");
                }

                try {
                    amount = new BigDecimal(number.replace("%", ""));
                } catch (NumberFormatException e) {
                    return "Invalid Number!";
                }

                taxes = amount.multiply(Values.CONFIG.getWithdrawTaxes().divide(BigDecimal.valueOf(100)));
            }

            if (secondIdentifier.startsWith("percentage_")) {
                String number = secondIdentifier.replace("percentage_", "");

                BigDecimal amount, balance;

                if (target == null) balance = economy.getBankBalance(p);
                else {
                    if (!BankPlus.getBankManager().exist(target))
                        return "The selected bank does not exist.";

                    number = number.replace("{" + target + "}", "");
                    balance = economy.getBankBalance(p, target);
                }

                try {
                    amount = new BigDecimal(number.replace("%", ""));
                } catch (NumberFormatException e) {
                    return "Invalid Number!";
                }

                BigDecimal percentageBalance = balance.multiply(amount.divide(BigDecimal.valueOf(100)));
                taxes = percentageBalance.multiply(Values.CONFIG.getWithdrawTaxes().divide(BigDecimal.valueOf(100)));
            }
            return getFormat(identifier, taxes);
        }

        BPInterest interest = BankPlus.INSTANCE().getInterest();
        switch (identifier) {
            case "interest_cooldown":
                return Values.CONFIG.isInterestEnabled() ? BPUtils.formatTime(interest.getInterestCooldownMillis()) : "Interest disabled.";

            case "interest_cooldown_millis":
                return Values.CONFIG.isInterestEnabled() ? String.valueOf(interest.getInterestCooldownMillis()) : "Interest disabled.";

            case "withdraw_taxes":
                return Values.CONFIG.getWithdrawTaxesString();

            case "deposit_taxes":
                return Values.CONFIG.getDepositTaxesString();
        }

        return null;
    }

    private String getFormat(String formatter, BigDecimal value) {
        if (value == null) return "Invalid number!";

        if (formatter.contains("_long")) return String.valueOf(value);
        if (formatter.contains("_formatted")) return BPFormatter.format(value);
        if (formatter.contains("_formatted_long")) return BPFormatter.formatLong(value);
        return BPFormatter.formatCommas(value);
    }
}