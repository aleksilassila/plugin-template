package me.aleksilassila.template.utils;

import me.aleksilassila.template.Main;

import java.net.URL;
import java.net.URLClassLoader;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

public class Messages {
    public static Messages instance;
    private static Main plugin;

    private static ResourceBundle bundle;

    static String BUNDLE_NAME = "messages";

    public static Messages init(Main plugin) {
        if (instance != null) {
            return instance;
        }

        instance = new Messages();
        Messages.plugin = plugin;

        Locale locale = new Locale(Optional.ofNullable(plugin.getConfig().getString("locale")).orElse("en"));

        try {
            URL[] urls = new URL[]{plugin.getDataFolder().toURI().toURL()};
            ClassLoader loader = new URLClassLoader(urls);
            bundle = ResourceBundle.getBundle(BUNDLE_NAME, locale, loader);
        } catch (Exception ignored) {
            bundle = ResourceBundle.getBundle(BUNDLE_NAME, locale);
        }

        plugin.getLogger().info("Using " + locale.getDisplayName() + " locales");

        return instance;

    }

    public static String get(final String string, final Object... objects) {
        if (instance == null) {
            return "";
        }

        return instance.format(string, objects);
    }

    public String format(final String string, final Object... objects) {
        String format = bundle.getString(string);
        MessageFormat messageFormat;

        try {
            messageFormat = new MessageFormat(format);
        } catch (final IllegalArgumentException e) {
            plugin.getLogger().severe("Invalid Translation key for '" + string + "': " + e.getMessage());
            format = format.replaceAll("\\{(\\D*?)\\}", "\\[$1\\]");
            messageFormat = new MessageFormat(format);
        }

        return messageFormat.format(objects);
    }
}