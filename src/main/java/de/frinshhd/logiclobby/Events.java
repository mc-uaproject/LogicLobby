package de.frinshhd.logiclobby;

import de.frinshhd.logiclobby.model.Portal;
import de.frinshhd.logiclobby.model.Server;
import de.frinshhd.logiclobby.utils.SpigotTranslator;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

import static de.frinshhd.logiclobby.Main.getInstance;
import static de.frinshhd.logiclobby.Main.getManager;

public class Events implements Listener {
    private final List<Player> teleporting = new ArrayList<>();

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        if (getManager().getConfig().getEvents().isNoDamage()) {
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler
    public void onPlayerHunger(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        if (getManager().getConfig().getEvents().isNoHunger()) {
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (getManager().getConfig().getEvents().isNoBlockBreak() && !event.getPlayer().hasPermission("logiclobby.admin.build")) {
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (getManager().getConfig().getEvents().isNoBlockPlace() && !event.getPlayer().hasPermission("logiclobby.admin.build")) {
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        if (getManager().getConfig().getEvents().isNoItemDrop() && !event.getPlayer().hasPermission("logiclobby.admin.build")) {
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler
    public void onItemPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (getManager().getConfig().getEvents().isNoItemPickup() && !player.hasPermission("logiclobby.admin.build")) {
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler
    public void onItemCraft(PrepareItemCraftEvent event) {
        event.getViewers().forEach(player -> {
            if (getManager().getConfig().getEvents().isNoItemCraft() && !player.hasPermission("logiclobby.admin.build")) {
                event.getInventory().setResult(new ItemStack(Material.AIR));
            }
        });
    }

    @EventHandler
    public void onItemConsume(PlayerItemConsumeEvent event) {
        if (getManager().getConfig().getEvents().isNoItemConsume() && !event.getPlayer().hasPermission("logiclobby.admin.build")) {
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (getManager().getConfig().getEvents().isNoEntityDamage()) {
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event) {
        if (getManager().getConfig().getEvents().isNoEntitySpawn()) {
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler
    public void onUnderLowestY(PlayerMoveEvent event) {
        if (getManager().getConfig().getEvents().getLowestY() != null && event.getPlayer().getLocation().getY() < getManager().getConfig().getEvents().getLowestY()) {
            event.getPlayer().setFallDistance(0);
            event.getPlayer().teleport(event.getPlayer().getWorld().getSpawnLocation());
            return;
        }
    }

    @EventHandler
    public void onArrowPickup(PlayerPickupArrowEvent event) {
        if (!event.getPlayer().hasPermission("logiclobby.admin.build")) {
            event.setCancelled(true);
            event.getArrow().remove();
            return;
        }
    }

    @EventHandler
    public void onPortalUse(PlayerPortalEvent event) {
        event.setCancelled(true);
        for (Portal portal : getManager().getConfig().getPortals()) {
            if (portal.isInRange(event.getPlayer().getLocation())) {
                String serverName = portal.getDestinationServer();
                for (Server server : getManager().getConfig().getTeleporter().getServers()) {
                    if (server.getServerName().equals(serverName)) {
                        server.canJoin(event.getPlayer()).thenApply(canJoin -> {
                            if (canJoin) {
                                server.execute(event.getPlayer());
                            } else {
                                event.getPlayer().sendMessage(SpigotTranslator.build("server.full"));
                            }
                            return null;
                        });
                        break;
                    }
                }
                break;
            }
        }
    }

    @EventHandler
    public void onEnderPortalCollision(PlayerMoveEvent event) {
        if (teleporting.contains(event.getPlayer())) {
            if (!event.getFrom().getBlock().equals(event.getTo().getBlock())) {
                event.setCancelled(true);
            }
            return;
        }
        if (event.getTo().getBlock().getType().equals(Material.END_GATEWAY)) {
            String serverName = getManager().getConfig().getMainServerName();
            for (Server server : getManager().getConfig().getTeleporter().getServers()) {
                if (server.getServerName().equals(serverName)) {
                    event.getPlayer().playSound(Sound.sound().type(org.bukkit.Sound.BLOCK_PORTAL_TRAVEL).volume(0.6f).build());
                    event.getPlayer().spawnParticle(Particle.ENCHANTMENT_TABLE, event.getTo(), 10000);
                    teleporting.add(event.getPlayer());
                    server.canJoin(event.getPlayer()).thenApply(canJoin -> Bukkit.getScheduler().runTaskLater(getInstance(), () -> {
                        if (canJoin) {
                            server.execute(event.getPlayer());
                        } else {
                            event.getPlayer().sendMessage(SpigotTranslator.build("server.full"));
                            event.getPlayer().teleport(event.getFrom().getWorld().getSpawnLocation());
                        }
                        teleporting.remove(event.getPlayer());
                    }, 40));
                    break;
                }
            }
        }
    }
}
