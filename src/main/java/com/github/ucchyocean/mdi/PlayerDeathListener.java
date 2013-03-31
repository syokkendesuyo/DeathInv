/*
 * @author     ucchy
 * @license    GPLv3
 * @copyright  Copyright ucchy 2013
 */
package com.github.ucchyocean.mdi;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.PlayerInventory;

/**
 * @author ucchy
 * プレイヤーの死亡を監視するリスナークラス
 */
public class PlayerDeathListener implements Listener {

    /**
     * プレイヤーが死亡したときに呼び出されるメソッド
     * @param event 死亡イベント
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {

        Player player = event.getEntity();
        String message = ChatColor.stripColor(event.getDeathMessage());
        PlayerInventory inv = player.getInventory();
        String items = DeathInv.khandler.convertInvToItemString(inv);
        String armors = DeathInv.khandler.convertArmorToItemString(inv);

        DeathInv.udhandler.addUserLog(player.getName(), items, armors, message);
    }
}
