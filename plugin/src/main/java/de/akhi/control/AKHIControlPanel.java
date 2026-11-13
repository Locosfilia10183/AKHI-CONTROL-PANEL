package de.akhi.control;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class AKHIControlPanel extends JavaPlugin implements Listener {
    private static final String PREFIX = ChatColor.DARK_PURPLE + "[AKHI] " + ChatColor.LIGHT_PURPLE;
    private final Map<UUID, String> chatModes = new ConcurrentHashMap<>();

    @Override public void onEnable() {
        saveDefaultConfig();
        Bukkit.getPluginManager().registerEvents(this, this);
        Objects.requireNonNull(getCommand("menu")).setExecutor(this::onMenuCommand);
        getLogger().info("AKHI Control Panel enabled. Admin permission: akhicontrol.admin");
    }

    private boolean allowed(CommandSender sender) {
        if (sender.hasPermission("akhicontrol.admin")) return true;
        sender.sendMessage(PREFIX + ChatColor.RED + "Kein Zugriff. Nur Server-Admins dürfen dieses Menü öffnen.");
        return false;
    }

    private boolean onMenuCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) { sender.sendMessage("This menu can only be opened in-game."); return true; }
        if (!allowed(player)) return true;
        openMain(player); return true;
    }

    private ItemStack item(Material material, String name, String... lore) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(ChatColor.LIGHT_PURPLE + name);
        meta.setLore(Arrays.stream(lore).map(s -> ChatColor.GRAY + s).toList());
        stack.setItemMeta(meta); return stack;
    }

    private Inventory create(String title) { return Bukkit.createInventory(null, 27, ChatColor.DARK_PURPLE + "AKHI » " + title); }

    private void openMain(Player p) {
        Inventory inv = create("CONTROL CENTER");
        inv.setItem(10, item(Material.LIGHTNING_ROD, "PERFORMANCE", "TPS, RAM und Leistungsprofile"));
        inv.setItem(12, item(Material.SPYGLASS, "CHUNKS & SICHTWEITE", "View- und Simulation-Distance"));
        inv.setItem(14, item(Material.COMMAND_BLOCK, "ADMIN CONSOLE", "Befehle als Server-Konsole ausführen"));
        inv.setItem(16, item(Material.PLAYER_HEAD, "SPIELER", "Online-Spieler und Verwaltung"));
        inv.setItem(22, item(Material.BARRIER, "SCHLIESSEN", "Menü schließen"));
        p.openInventory(inv);
    }

    private void openPerformance(Player p) {
        Inventory inv = create("PERFORMANCE");
        double[] tps = Bukkit.getTPS();
        long max = Runtime.getRuntime().maxMemory() / 1048576;
        long used = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576;
        inv.setItem(4, item(Material.NETHER_STAR, "LIVE STATUS", String.format(Locale.US,"TPS 1m: %.2f | 5m: %.2f | 15m: %.2f", tps[0],tps[1],tps[2]), "Heap: " + used + " / " + max + " MB"));
        inv.setItem(10, item(Material.EMERALD, "HOCHLEISTUNG", "Setzt Sichtweite 6, Simulation 4"));
        inv.setItem(12, item(Material.DIAMOND, "AUSGEWOGEN", "Setzt Sichtweite 8, Simulation 5"));
        inv.setItem(14, item(Material.REDSTONE, "MEHR SICHTWEITE", "Setzt Sichtweite 10, Simulation 6"));
        inv.setItem(16, item(Material.ARROW, "ZURÜCK", "Hauptmenü"));
        p.openInventory(inv);
    }

    private void openChunks(Player p) {
        Inventory inv = create("CHUNKS & SICHTWEITE");
        inv.setItem(10, item(Material.GREEN_WOOL, "SICHTWEITE 6", "Alle Welten auf 6 Chunks"));
        inv.setItem(11, item(Material.YELLOW_WOOL, "SICHTWEITE 8", "Alle Welten auf 8 Chunks"));
        inv.setItem(12, item(Material.RED_WOOL, "SICHTWEITE 10", "Alle Welten auf 10 Chunks"));
        inv.setItem(14, item(Material.ENDER_EYE, "SIMULATION 4", "Alle Welten auf 4 Chunks"));
        inv.setItem(15, item(Material.ENDER_PEARL, "SIMULATION 5", "Alle Welten auf 5 Chunks"));
        inv.setItem(16, item(Material.END_CRYSTAL, "SIMULATION 6", "Alle Welten auf 6 Chunks"));
        inv.setItem(22, item(Material.ARROW, "ZURÜCK", "Hauptmenü"));
        p.openInventory(inv);
    }

    private void applyDistances(Player p, Integer view, Integer simulation) {
        for (World w : Bukkit.getWorlds()) {
            if (view != null) w.setViewDistance(view);
            if (simulation != null) w.setSimulationDistance(simulation);
        }
        p.sendMessage(PREFIX + ChatColor.GREEN + "Einstellungen übernommen. Sie gelten bis zum nächsten Serverstart.");
    }

    private void openPlayers(Player p) {
        Inventory inv = create("SPIELER");
        int slot = 0;
        for (Player target : Bukkit.getOnlinePlayers()) {
            if (slot >= 26) break;
            inv.setItem(slot++, item(Material.PLAYER_HEAD, target.getName(), "Linksklick: Spieler kicken", "Rechtsklick: Spieler teleportieren"));
        }
        inv.setItem(26, item(Material.ARROW, "ZURÜCK", "Hauptmenü")); p.openInventory(inv);
    }

    @EventHandler public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        String title = e.getView().getTitle();
        if (!title.startsWith(ChatColor.DARK_PURPLE + "AKHI » ")) return;
        e.setCancelled(true);
        if (!allowed(p)) { p.closeInventory(); return; }
        int slot = e.getRawSlot();
        if (slot < 0 || slot >= e.getInventory().getSize()) return;
        String page = title.substring((ChatColor.DARK_PURPLE + "AKHI » ").length());
        switch (page) {
            case "CONTROL CENTER" -> {
                if (slot == 10) openPerformance(p);
                else if (slot == 12) openChunks(p);
                else if (slot == 14) promptConsole(p);
                else if (slot == 16) openPlayers(p);
                else if (slot == 22) p.closeInventory();
            }
            case "PERFORMANCE" -> {
                if (slot == 10) applyDistances(p, 6, 4);
                else if (slot == 12) applyDistances(p, 8, 5);
                else if (slot == 14) applyDistances(p, 10, 6);
                else if (slot == 16) { openMain(p); return; }
                openPerformance(p);
            }
            case "CHUNKS & SICHTWEITE" -> {
                if (slot == 10) applyDistances(p, 6, null);
                else if (slot == 11) applyDistances(p, 8, null);
                else if (slot == 12) applyDistances(p, 10, null);
                else if (slot == 14) applyDistances(p, null, 4);
                else if (slot == 15) applyDistances(p, null, 5);
                else if (slot == 16) applyDistances(p, null, 6);
                else if (slot == 22) { openMain(p); return; }
                openChunks(p);
            }
            case "SPIELER" -> {
                if (slot == 26) { openMain(p); return; }
                ItemStack clicked = e.getCurrentItem();
                if (clicked != null && clicked.hasItemMeta() && clicked.getItemMeta().hasDisplayName()) {
                    String name = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
                    Player target = Bukkit.getPlayerExact(name);
                    if (target != null && !target.equals(p)) {
                        if (e.isLeftClick()) target.kickPlayer("Vom Server-Admin entfernt.");
                        else if (e.isRightClick()) p.teleport(target.getLocation());
                    }
                }
                openPlayers(p);
            }
        }
    }

    private void promptConsole(Player p) {
        p.closeInventory(); chatModes.put(p.getUniqueId(), "console");
        p.sendMessage(PREFIX + ChatColor.YELLOW + "Konsolenbefehl eingeben (ohne /). 'abbrechen' zum Beenden.");
        p.sendMessage(ChatColor.DARK_GRAY + "Achtung: Der Befehl wird mit Server-Konsolenrechten ausgeführt.");
    }

    @EventHandler public void onChat(AsyncPlayerChatEvent e) {
        String mode = chatModes.remove(e.getPlayer().getUniqueId());
        if (mode == null) return;
        e.setCancelled(true);
        String input = e.getMessage().trim();
        Player player = e.getPlayer();
        if (input.equalsIgnoreCase("abbrechen")) { player.sendMessage(PREFIX + "Abgebrochen."); return; }
        if (input.isBlank() || input.startsWith("/")) { player.sendMessage(PREFIX + ChatColor.RED + "Ungültig. Befehl ohne führenden Slash eingeben."); return; }
        new BukkitRunnable() { @Override public void run() {
            if (!player.isOnline() || !player.hasPermission("akhicontrol.admin")) return;
            boolean ok = Bukkit.dispatchCommand(Bukkit.getConsoleSender(), input);
            player.sendMessage(PREFIX + (ok ? ChatColor.GREEN : ChatColor.RED) + "Befehl wurde verarbeitet: " + input);
            openMain(player);
        }}.runTask(this);
    }
}
