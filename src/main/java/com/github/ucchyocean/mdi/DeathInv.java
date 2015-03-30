/*
 * @author     ucchy
 * @license    GPLv3
 * @copyright  Copyright ucchy 2013
 */
package com.github.ucchyocean.mdi;

import java.util.Hashtable;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author ucchy
 * MyCra Death Inv プラグイン
 */
public class DeathInv extends JavaPlugin {

    protected static UserDataHandler udhandler;
    protected static Hashtable<String, ConfigurationSection> tempItems;
    protected static Hashtable<String, ConfigurationSection> tempArmors;

    /**
     * プラグインが有効化されたときに呼び出されるメソッド
     * @see org.bukkit.plugin.java.JavaPlugin#onEnable()
     */
    @Override
    public void onEnable() {

        // 初期化
        udhandler = new UserDataHandler(getDataFolder());
        tempItems = new Hashtable<String, ConfigurationSection>();
        tempArmors = new Hashtable<String, ConfigurationSection>();

        // コマンド登録
        getCommand("deathinv").setExecutor(new DICommand());

        // リスナー登録
        getServer().getPluginManager().registerEvents(
                new PlayerDeathListener(), this);
    }
}
