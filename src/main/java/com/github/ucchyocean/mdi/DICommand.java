/*
 * @author     ucchy
 * @license    GPLv3
 * @copyright  Copyright ucchy 2013
 */
package com.github.ucchyocean.mdi;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.github.ucchyocean.mdi.item.ItemConfigParseException;

/**
 * @author ucchy
 *
 */
public class DICommand implements CommandExecutor {

    private static final String PREERR = ChatColor.RED.toString();
    private static final String PREINFO = ChatColor.GRAY.toString();

    /**
     * プラグインのコマンドが実行されたときに呼び出されるメソッド
     * @see org.bukkit.plugin.java.JavaPlugin#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
     */
    public boolean onCommand(
            CommandSender sender, Command command, String label, String[] args) {

        // 引数なしは、処理せず終了する
        if ( args.length <= 0 ) {
            return false;
        }

        if ( args[0].equalsIgnoreCase("list") ) {

            if ( args.length == 1 ) {
                sender.sendMessage(PREERR + "Command error.");
                sender.sendMessage(PREINFO + "Usage: /" + label + " list (player) [(page)]");
                return true;
            }
            String name = args[1];

            int page = 1;
            if ( args.length >= 3 && args[2].matches("^[0-9]+$") ) {
                int temp = Integer.parseInt(args[2]);
                if ( 0 < temp && temp < DeathInv.udhandler.getMaxOfPage() ) {
                    page = temp;
                }
            }

            ArrayList<String> list = DeathInv.udhandler.getListUserLog(name, page);
            if ( list == null ) {
                sender.sendMessage(PREERR + "Player was not found, or player was not logged.");
                return true;
            }

            for ( String l : list ) {
                sender.sendMessage(l);
            }

            return true;

        } else if ( args[0].equalsIgnoreCase("show") ) {

            if ( args.length <= 2 ) {
                sender.sendMessage(PREERR + "Command error.");
                sender.sendMessage(PREINFO + "Usage: /" + label + " show (player) (id)");
                return true;
            }
            String name = args[1];

            int id = -1;
            if ( args.length >= 3 && args[2].matches("^[0-9]+$") ) {
                int temp = Integer.parseInt(args[2]);
                if ( 0 < temp && temp < UserDataHandler.MAX_LOG_SIZE ) {
                    id = temp;
                }
            }
            if ( id == -1 ) {
                sender.sendMessage(PREERR + "Command error, id was invalid.");
                sender.sendMessage(PREINFO + "Usage: /" + label + " show (player) (id)");
                return true;
            }

            ArrayList<String> list = DeathInv.udhandler.getShowUserLog(name, id);
            if ( list == null ) {
                sender.sendMessage(PREERR + "Player log was not found, or player was not logged.");
                return true;
            }

            for ( String l : list ) {
                sender.sendMessage(l);
            }

            return true;

        } else if ( args[0].equalsIgnoreCase("get") ) {

            if ( !(sender instanceof Player) ) {
                sender.sendMessage(PREERR + "This command can only be run by a player.");
                return true;
            }

            Player player = (Player)sender;

            if ( args.length <= 2 ) {
                sender.sendMessage(PREERR + "Command error.");
                sender.sendMessage(PREINFO + "Usage: /" + label + " get (player) (id)");
                return true;
            }
            String name = args[1];

            int id = -1;
            if ( args.length >= 3 && args[2].matches("^[0-9]+$") ) {
                int temp = Integer.parseInt(args[2]);
                if ( 0 < temp && temp < UserDataHandler.MAX_LOG_SIZE ) {
                    id = temp;
                }
            }
            if ( id == -1 ) {
                sender.sendMessage(PREERR + "Command error, id was invalid.");
                sender.sendMessage(PREINFO + "Usage: /" + label + " get (player) (id)");
                return true;
            }

            HashMap<String, ItemStack> log;
            try {
                log = DeathInv.udhandler.getUserLog(name, id);
            } catch (ItemConfigParseException e1) {
                e1.printStackTrace();
                sender.sendMessage(PREERR + "Item parser exception : " + e1.getLocalizedMessage());
                return true;
            }
            if ( log == null ) {
                sender.sendMessage(PREERR + "Player log was not found, or player was not logged.");
                return true;
            }

            // インベントリを消去する
            player.getInventory().clear();
            player.getInventory().setHelmet(null);
            player.getInventory().setChestplate(null);
            player.getInventory().setLeggings(null);
            player.getInventory().setBoots(null);

            // ログからインベントリを復元する
            for ( String key : log.keySet() ) {
                ItemStack item = log.get(key);
                if ( key.equals("boots") ) {
                    player.getInventory().setBoots(item);
                } else if ( key.equals("leggings") ) {
                    player.getInventory().setLeggings(item);
                } else if ( key.equals("chestplate") ) {
                    player.getInventory().setChestplate(item);
                } else if ( key.equals("helmet") ) {
                    player.getInventory().setHelmet(item);
                } else {
                    player.getInventory().addItem(item);
                }
            }

            sender.sendMessage(PREINFO + "Your inv was restored from " + name + ":" + id + ".");
            return true;

        } else if ( args[0].equalsIgnoreCase("temp") ) {

            if ( !(sender instanceof Player) ) {
                sender.sendMessage(PREERR + "This command can only be run by a player.");
                return true;
            }

            Player player = (Player)sender;
            PlayerInventory inv = player.getInventory();

            YamlConfiguration itemConfig = new YamlConfiguration();
            YamlConfiguration armorConfig = new YamlConfiguration();
            DIUtility.convInventoryItemsToSection(inv, itemConfig);
            DIUtility.convInventoryArmorsToSection(inv, armorConfig);
            DeathInv.tempItems.put(player.getName(), itemConfig);
            DeathInv.tempArmors.put(player.getName(), armorConfig);

            sender.sendMessage(PREINFO + "Your inv was stored to temporary data.");
            return true;

        } else if ( args[0].equalsIgnoreCase("restore") ) {

            if ( !(sender instanceof Player) ) {
                sender.sendMessage(PREERR + "This command can only be run by a player.");
                return true;
            }

            Player player = (Player)sender;

            ConfigurationSection tempItems = DeathInv.tempItems.get(player.getName());
            ConfigurationSection tempArmors = DeathInv.tempArmors.get(player.getName());
            if ( tempItems == null || tempArmors == null ) {
                sender.sendMessage(PREERR + "Your inv was not found in temporary data.");
                return true;
            }

            // インベントリを消去する
            player.getInventory().clear();
            player.getInventory().setHelmet(null);
            player.getInventory().setChestplate(null);
            player.getInventory().setLeggings(null);
            player.getInventory().setBoots(null);

            // ログからインベントリを復元する
            try {
                if ( tempItems.getKeys(false).size() > 0 ) {
                    HashMap<String, ItemStack> items =
                            DIUtility.convSectionToItemStack(tempItems);
                    for ( ItemStack item : items.values() ) {
                        if ( item != null && item.getType() != Material.AIR ) {
                            player.getInventory().addItem(item);
                        }
                    }
                }
                HashMap<String, ItemStack> armors =
                        DIUtility.convSectionToItemStack(tempArmors);
                player.getInventory().setHelmet(armors.get("helmet"));
                player.getInventory().setChestplate(armors.get("chestplate"));
                player.getInventory().setLeggings(armors.get("leggings"));
                player.getInventory().setBoots(armors.get("boots"));

                sender.sendMessage(PREINFO + "Your inv was restored from temporary data.");
                return true;

            } catch (Exception e) {
                sender.sendMessage(PREERR + e.getMessage());
                return true;
            }
        }

        return false;
    }
}
