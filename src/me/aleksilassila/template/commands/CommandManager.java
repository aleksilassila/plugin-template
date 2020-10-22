package me.aleksilassila.template.commands;

import com.sun.istack.internal.Nullable;
import me.aleksilassila.template.Main;
import me.aleksilassila.template.utils.ConfirmItem;
import me.aleksilassila.template.utils.Messages;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.*;

public abstract class CommandManager implements TabExecutor {
    private final Main plugin;
    public HashMap<String, ConfirmItem> confirmations;

    public CommandManager(Main plugin) {
        this.plugin = plugin;

        plugin.getCommand(getName()).setExecutor(this);

        confirmations = new HashMap<>();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.getLogger().info("This command is for players only.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission(getPermission()) && getPermission() != null) {
            player.sendMessage(Messages.get("error.NO_PERMISSION"));
            return true;
        }

        boolean confirmed = false;
        String issuedCommand = String.join(" ", label, String.join(" ", args));

        ConfirmItem item = confirmations.get(player.getUniqueId().toString());
        if (item != null
                && item.command.equals(issuedCommand)
                && !item.expired()) {
            confirmations.remove(player.getUniqueId().toString());

            confirmed = true;
        } else {
            confirmations.put(player.getUniqueId().toString(), new ConfirmItem(issuedCommand, 8 * 1000));
        }

        if (args.length >= 1) {
            Subcommand target = getSubcommand(args[0]);

            if (target == null) {
                player.sendMessage(Messages.get("error.SUBCOMMAND_NOT_FOUND"));
                sendCommandHelp(player);
                return true;
            }

            if (target.getPermission() != null && !player.hasPermission(target.getPermission())) {
                player.sendMessage(Messages.get("error.NO_PERMISSION"));
                return true;
            }

            try {
                target.onCommand(player, Arrays.copyOfRange(args, 1, args.length), confirmed);
            } catch (Exception e) {
                player.sendMessage(Messages.get("error.INTERNAL_ERROR"));
            }
        } else {
            sendCommandHelp(player);
        }

        return true;
    }

    @Nullable
    private Subcommand getSubcommand(String name) {
        for (Subcommand subcommand : getSubcommands()) {
            if (subcommand.getName().equalsIgnoreCase(name)) return subcommand;
        }

        return null;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return null;

        Player player = (Player) sender;

        List<String> availableArgs = new ArrayList<>();

        if (args.length == 1) {
            for (Subcommand subcommand : getSubcommands()) {
                if (subcommand.getPermission() == null || player.hasPermission(subcommand.getPermission()))
                    availableArgs.add(subcommand.getName());
            }
        } else if (args.length > 1) {
            Subcommand currentSubcommand = getSubcommand(args[0]);
            if (currentSubcommand == null) return null;

            if (currentSubcommand.getPermission() == null || player.hasPermission(currentSubcommand.getPermission()))
                availableArgs = currentSubcommand.onTabComplete(player, Arrays.copyOfRange(args, 1, args.length));
        }

        return availableArgs;
    }

    public abstract void sendCommandHelp(Player player);
    public abstract String getName();
    public abstract Set<Subcommand> getSubcommands();
    public abstract String getPermission();
}
