package me.pulsi_.bankplus.listeners.bankListener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class BankClickHigh implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onBankClick(InventoryClickEvent e) {
        BankClickMethod.process(e);
    }
}