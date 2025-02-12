package com.mortisdevelopment.mortisbank.bank;

import com.mortisdevelopment.mortisbank.transactions.Transaction;
import com.mortisdevelopment.mortiscore.currencies.Currency;
import com.mortisdevelopment.mortiscore.items.CustomItem;
import com.mortisdevelopment.mortiscore.messages.Messages;
import com.mortisdevelopment.mortiscore.placeholders.Placeholder;
import com.mortisdevelopment.mortiscore.utils.NumberUtils;
import de.rapha149.signgui.SignGUI;
import lombok.Getter;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

@Getter
public class BankSettings {

    public enum InputMode{
        ANVIL,
        SIGN
    }
    private final Currency currency;
    private final boolean leaderboard;
    private final InputMode mode;
    private final int inputSlot;
    private final CustomItem anvilItem;

    public BankSettings(Currency currency, boolean leaderboard, @NotNull InputMode mode, int inputSlot, CustomItem anvilItem) {
        this.currency = currency;
        this.leaderboard = leaderboard;
        this.mode = mode;
        this.inputSlot = inputSlot - 1;
        this.anvilItem = anvilItem;
    }

    public boolean open(JavaPlugin plugin, BankManager bankManager, Player player, Transaction.TransactionType type, Placeholder placeholder) {
        if (player == null) {
            return false;
        }
        switch (mode) {
            case SIGN -> openSign(plugin, bankManager, player, type, placeholder);
            case ANVIL -> openAnvil(plugin, bankManager, player, type, placeholder);
        }
        return true;
    }

    public void openSign(JavaPlugin plugin, BankManager bankManager, Player player, Transaction.TransactionType type, Placeholder placeholder) {
        Messages messages = bankManager.getMessages(type);
        SignGUI.builder()
                .setLines(messages.getSimpleMessage("sign_1"), messages.getSimpleMessage("sign_2"), messages.getSimpleMessage("sign_3"), messages.getSimpleMessage("sign_4"))
                .callHandlerSynchronously(plugin)
                .setHandler((p, result) -> {
                    String line = result.getLine(inputSlot);
                    if (line == null) {
                        return null;
                    }
                    double amount = NumberUtils.getMoney(line);
                    if (amount == 0) {
                        return null;
                    }
                    switch (type) {
                        case DEPOSIT -> bankManager.deposit(plugin, player, amount);
                        case WITHDRAW -> bankManager.withdraw(plugin, player, amount);
                    }
                    bankManager.getPersonalMenu().open(player, placeholder);
                    return null;
                })
                .build()
                .open(player);
    }

    public void openAnvil(JavaPlugin plugin, BankManager bankManager, Player player, Transaction.TransactionType type, Placeholder placeholder) {
        Messages messages = bankManager.getMessages(type);
        new AnvilGUI.Builder()
                .onClose(state -> {
                    double amount = NumberUtils.getMoney(state.getText());
                    if (amount == 0) {
                        return;
                    }
                    switch (type) {
                        case DEPOSIT -> bankManager.deposit(plugin, player, amount);
                        case WITHDRAW -> bankManager.withdraw(plugin, player, amount);
                    }
                    bankManager.getPersonalMenu().open(player, placeholder);
                })
                .onClick((slot, state) -> {
                    if(slot != AnvilGUI.Slot.OUTPUT) {
                        return Collections.emptyList();
                    }
                    double amount = NumberUtils.getMoney(state.getText());
                    if (amount == 0) {
                        return Collections.emptyList();
                    }
                    switch (type) {
                        case DEPOSIT -> bankManager.deposit(plugin, player, amount);
                        case WITHDRAW -> bankManager.withdraw(plugin, player, amount);
                    }
                    bankManager.getPersonalMenu().open(player, placeholder);
                    return Collections.singletonList(AnvilGUI.ResponseAction.close());
                })
                .text(messages.getSimplePlaceholderMessage("anvil_text", placeholder))
                .itemLeft(anvilItem.getItem(placeholder))
                .title(messages.getSimplePlaceholderMessage("anvil_title", placeholder))
                .plugin(plugin)
                .open(player);
    }
}
