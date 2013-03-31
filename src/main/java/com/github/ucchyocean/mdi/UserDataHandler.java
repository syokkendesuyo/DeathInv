/*
 * @author     ucchy
 * @license    GPLv3
 * @copyright  Copyright ucchy 2013
 */
package com.github.ucchyocean.mdi;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * @author ucchy
 * ユーザー情報をハンドリングするためのクラス
 */
public class UserDataHandler {

    private static final String LIST_START =
            "&7----- Player: &c%-15s&7  Page (&c%d&7/&c%d&7) -----";
    private static final String LIST_END =
            "&7----------------------------------------";
    private static final String SHOW_START =
            "&7----- Player: &c%-15s&7  ID: &c%d&7 ----------";
    private static final String SHOW_END =
            "&7----------------------------------------";

    private static final String USER_FOLDER = "user";
    protected static final int MAX_LOG_SIZE = 1000;
    protected static final int PAGE_SIZE = 10;

    private SimpleDateFormat keyDateFormat;
    private SimpleDateFormat logDateFormat;
    private File folder;

    /**
     * コンストラクタ
     * @param dataFolder プラグインのデータフォルダ
     */
    public UserDataHandler(File dataFolder) {

        keyDateFormat = new SimpleDateFormat("yyMMddHHmmssSSS");
        logDateFormat = new SimpleDateFormat("yy-MM-dd,HH:mm:ss");
        folder = new File(dataFolder, USER_FOLDER);
        if ( !folder.exists() ) {
            folder.mkdirs();
        }
    }

    /**
     * ユーザーのログを追加する。MAX_LOG_SIZEを超えた場合、古いログが削除される。
     * @param name ユーザー名
     * @param items 記録するアイテム情報（kit形式文字列）
     * @param armors 記録する防具情報（kit形式文字列）
     * @param deathMessage 記録するデスメッセージ
     */
    public void addUserLog(String name, String items, String armors, String deathMessage) {

        Date date = new Date();
        String key = keyDateFormat.format(date);
        String time = logDateFormat.format(date);

        // ファイルからロード（なければ作る）
        File file = new File(folder, name + ".yml");
        if ( !file.exists() ) {
            YamlConfiguration config = new YamlConfiguration();
            try {
                config.save(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        // データを追加
        config.set(key + ".time", time);
        config.set(key + ".items", items);
        config.set(key + ".armors", armors);
        config.set(key + ".msg", deathMessage);

        // ログデータがMAX値を超える場合は、古いものを削除する
        Set<String> dataKeys = config.getKeys(false);
        if ( dataKeys.size() > MAX_LOG_SIZE ) {

            // ソート
            List<String> sortedKeys = new ArrayList<String>(dataKeys);
            Collections.sort(sortedKeys);
            Collections.reverse(sortedKeys);

            while ( sortedKeys.size() > MAX_LOG_SIZE ) {
                String oldKey = sortedKeys.get(0);
                config.set(oldKey, null); // 削除
                sortedKeys.remove(0);
            }
        }

        // 保存
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * ユーザーログを取得する
     * @param name ユーザー名
     * @param id ログID
     * @return ログ情報（1行目がitems, 2行目がarmors, ログがなければnullになる。）
     */
    public ArrayList<String> getUserLog(String name, int id) {

        // ファイルからロード（なければnullを返して終了）
        File file = new File(folder, name + ".yml");
        if ( !file.exists() ) {
            return null;
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        // 指定したIDがログの数より多ければ、nullを返して終了
        Set<String> dataKeys = config.getKeys(false);
        if ( dataKeys.size() < id ) {
            return null;
        }

        // ソート
        List<String> sortedKeys = new ArrayList<String>(dataKeys);
        Collections.sort(sortedKeys);
        Collections.reverse(sortedKeys);

        ArrayList<String> result = new ArrayList<String>();
        result.add( config.getString(sortedKeys.get(id - 1) + ".items", "") );
        result.add( config.getString(sortedKeys.get(id - 1) + ".armors", "") );
        return result;
    }

    /**
     * ユーザーログの一覧表示を取得する
     * @param name ユーザー名
     * @param page 表示するページ
     * @return ログ一覧
     */
    public ArrayList<String> getListUserLog(String name, int page) {

        // ファイルからロード（なければnullを返して終了）
        File file = new File(folder, name + ".yml");
        if ( !file.exists() ) {
            return null;
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        int start = (page - 1) * PAGE_SIZE + 1;
        int end = page * PAGE_SIZE;

        // ソート
        Set<String> dataKeys = config.getKeys(false);
        List<String> sortedKeys = new ArrayList<String>(dataKeys);
        Collections.sort(sortedKeys);
        Collections.reverse(sortedKeys);
        int pages = (sortedKeys.size() / PAGE_SIZE) + 1;

        ArrayList<String> messages = new ArrayList<String>();
        messages.add(String.format(LIST_START, name, page, pages));
        for ( int id=start; id<=end; id++ ) {
            if ( sortedKeys.size() >= id ) {
                String key = sortedKeys.get(id - 1);
                ConfigurationSection section = config.getConfigurationSection(key);
                messages.addAll(convToListMessage(id, section));
            }
        }
        messages.add(LIST_END);

        return Utility.replaceColorCode(messages);
    }

    /**
     * ユーザーログの詳細表示を取得する
     * @param name ユーザー名
     * @param id 表示するログID
     * @return ログ詳細
     */
    public ArrayList<String> getShowUserLog(String name, int id) {

        // ファイルからロード（なければnullを返して終了）
        File file = new File(folder, name + ".yml");
        if ( !file.exists() ) {
            return null;
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        // 指定したIDがログの数より多ければ、nullを返して終了
        Set<String> dataKeys = config.getKeys(false);
        if ( dataKeys.size() < id ) {
            return null;
        }

        List<String> sortedKeys = new ArrayList<String>(dataKeys);
        Collections.sort(sortedKeys);
        Collections.reverse(sortedKeys);

        ArrayList<String> messages = new ArrayList<String>();
        messages.add(String.format(SHOW_START, name, id));

        String key = sortedKeys.get(id - 1);
        ConfigurationSection section = config.getConfigurationSection(key);
        messages.addAll(convToShowMessage(section));

        messages.add(SHOW_END);

        return Utility.replaceColorCode(messages);
    }

    /**
     * 指定したコンフィグセクションを、一覧形式に変換して返す
     * @param id ログID（メッセージの内容に使われる）
     * @param section セクション
     * @return 一覧形式
     */
    private ArrayList<String> convToListMessage(int id, ConfigurationSection section) {

        String time = section.getString("time", "");
        String items = section.getString("items", "");
        String armors = section.getString("armors", "");
        String msg = section.getString("msg", "");

        ArrayList<String> result = new ArrayList<String>();
        result.add(String.format("&7| ID:&c%d&7 %s  items:&c%d&7, armors:&c%d&7",
                id, time, getItemCount(items), getItemCount(armors)) );
        result.add("&7|   &c" + msg);

        return result;
    }

    /**
     * 指定したコンフィグセクションを、詳細形式に変換して返す
     * @param section セクション
     * @return 詳細形式
     */
    private ArrayList<String> convToShowMessage(ConfigurationSection section) {

        String time = section.getString("time", "");
        String msg = section.getString("msg", "");
        String[] items = section.getString("items", "").split(",");
        String[] armors = section.getString("armors", "").split(",");

        ArrayList<String> result = new ArrayList<String>();
        result.add("&7| &c" + time);
        result.add("&7|   &c" + msg);
        result.add("&7| items: ");
        for ( String item : items ) {
            result.addAll(DeathInv.khandler.getDescFromItemInfo(item));
        }
        result.add("&7| armors: ");
        for ( String armor : armors ) {
            result.addAll(DeathInv.khandler.getDescFromItemInfo(armor));
        }

        return result;
    }

    private int getItemCount(String info) {

        if ( info == null || info.length() == 0 ) {
            return 0;
        }

        int count = 0;
        String[] items = info.split(",");
        for ( String i : items ) {
            if ( !i.equals("0") ) {
                count++;
            }
        }

        return count;
    }

    /**
     * 最大のページ数
     * @return 最大のページ数
     */
    public int getMaxOfPage() {
        return MAX_LOG_SIZE / PAGE_SIZE;
    }
}
