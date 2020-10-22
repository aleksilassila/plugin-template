package me.aleksilassila.template;

import me.aleksilassila.template.utils.Messages;
import me.aleksilassila.template.utils.UpdateChecker;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
    @Override
    public void onEnable() {
        Messages.init(this);

        new UpdateChecker(this, 84303).getVersion(version -> {
            if (this.getDescription().getVersion().equalsIgnoreCase(version)) {
                getLogger().info("You are up to date.");
            } else {
                getLogger().info("There's a new update available!");
            }
        });
    }
}
