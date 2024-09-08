package com.mortisdevelopment.mortisbank.commands.subcommands.admin;

import com.mortisdevelopment.mortisbank.MortisBank;
import com.mortisdevelopment.mortisbank.actions.types.WithdrawActionType;
import com.mortisdevelopment.mortisbank.utils.BankUtils;
import com.mortisdevelopment.mortiscore.commands.PermissionCommand;
import com.mortisdevelopment.mortiscore.messages.Messages;
import com.mortisdevelopment.mortiscore.placeholder.Placeholder;
import com.mortisdevelopment.mortiscore.utils.PlayerExecutor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.List;

public class WithdrawCommand extends PermissionCommand {

    private final MortisBank plugin;

    public WithdrawCommand(Messages messages, MortisBank plugin) {
        super("withdraw", "mortisbank.admin.withdraw", messages);
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, String label, String[] args) {
        if (args.length < 4) {
            sender.sendMessage(getMessages().getMessage("admin_deposit_usage"));
            return false;
        }
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        if (!target.hasPlayedBefore()) {
            sender.sendMessage(getMessages().getMessage("invalid_target"));
            return false;
        }
        double amount;
        try {
            amount = Double.parseDouble(args[1]);
        } catch (NumberFormatException exp) {
            sender.sendMessage(getMessages().getMessage("invalid_number"));
            return false;
        }
        if (amount <= 0) {
            sender.sendMessage(getMessages().getMessage("invalid_number"));
            return false;
        }
        new WithdrawActionType(plugin, amount).execute(new PlayerExecutor(target), new Placeholder());
        getMessages().sendPlaceholderMessage(sender, "admin_withdraw", BankUtils.getPlaceholder(amount));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, String s, String[] strings) {
        return null;
    }
}