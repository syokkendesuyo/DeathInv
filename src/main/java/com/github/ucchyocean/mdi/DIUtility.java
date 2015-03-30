/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2015
 */
package com.github.ucchyocean.mdi;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.github.ucchyocean.mdi.item.ItemConfigParseException;
import com.github.ucchyocean.mdi.item.ItemConfigParser;

/**
 * DeathInvのユーティリティクラス
 * @author ucchy
 */
public class DIUtility {

    /**
     * 指定されたプレイヤーインベントリのアイテムを、コンフィグセクションに変換する
     * @param inventory インベントリ
     * @param section コンフィグセクション
     */
    protected static void convInventoryItemsToSection(PlayerInventory inventory, ConfigurationSection section) {

        int index = 1;
        for (ItemStack item : inventory.getContents()) {
            if (item != null && item.getType() != Material.AIR) {
                String key = "item" + index;
                ConfigurationSection sub = section.createSection(key);
                ItemConfigParser.setItemToSection(sub, item);
                index++;
            }
        }
    }

    /**
     * 指定されたプレイヤーインベントリの防具を、コンフィグセクションに変換する
     * @param inventory インベントリ
     * @param section コンフィグセクション
     */
    protected static void convInventoryArmorsToSection(PlayerInventory inventory, ConfigurationSection section) {

        String[] armorNames = new String[]{"boots", "leggings", "chestplate", "helmet"};
        for ( int i=0; i<4; i++ ) {
            ItemStack item = inventory.getArmorContents()[i];
            if ( item != null && item.getType() != Material.AIR ) {
                ConfigurationSection sub = section.createSection(armorNames[i]);
                ItemConfigParser.setItemToSection(sub, item);
            }
        }
    }

    /**
     * コンフィグセクションから、アイテムのハッシュマップを生成して取得する
     * @param section コンフィグセクション
     * @return アイテムのハッシュマップ
     */
    protected static HashMap<String, ItemStack> convSectionToItemStack(ConfigurationSection section)
                throws ItemConfigParseException {

        if ( section == null ) return new HashMap<String, ItemStack>();

        HashMap<String, ItemStack> result = new HashMap<String, ItemStack>();
        for ( String key : section.getKeys(false) ) {
            ConfigurationSection sub = section.getConfigurationSection(key);
            ItemStack item = ItemConfigParser.getItemFromSection(sub);
            if ( item != null && item.getType() != Material.AIR ) {
                result.put(key, item);
            }
        }
        return result;
    }

    /**
     * アイテムの情報を文字列表現で返す
     * @param item アイテム
     * @param prefix プレフィックス
     * @return 文字列表現
     */
    public static ArrayList<String> getItemInfoWithPrefix(ItemStack item, String prefix) {
        String[] description = ItemConfigParser.getItemInfo(item).split("\n");
        ArrayList<String> result = new ArrayList<String>();
        for ( String line : description ) {
            result.add(prefix + line);
        }
        return result;
    }
}
