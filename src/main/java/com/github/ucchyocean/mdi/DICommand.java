/*
 * @author     ucchy
 * @license    GPLv3
 * @copyright  Copyright ucchy 2013
 */
package com.github.ucchyocean.mdi;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

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

            ArrayList<String> log = DeathInv.udhandler.getUserLog(name, id);
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
            try {
                if ( log.get(0).length() > 0 ) {
                    ArrayList<ItemStack> items = DeathInv.khandler.convertToItemStack(log.get(0));
                    for ( ItemStack item : items ) {
                        if ( item != null )
                            player.getInventory().addItem(item);
                    }
                }
                ArrayList<ItemStack> armors = DeathInv.khandler.convertToItemStack(log.get(1));
                player.getInventory().setHelmet(armors.get(0));
                player.getInventory().setChestplate(armors.get(1));
                player.getInventory().setLeggings(armors.get(2));
                player.getInventory().setBoots(armors.get(3));

                sender.sendMessage(PREINFO + "Your inv was restored from " + name + ":" + id + ".");
                return true;
                
            } catch (Exception e) {
                sender.sendMessage(PREERR + e.getMessage());
                return true;
            }

        } else if ( args[0].equalsIgnoreCase("temp") ) {

            if ( !(sender instanceof Player) ) {
                sender.sendMessage(PREERR + "This command can only be run by a player.");
                return true;
            }

            Player player = (Player)sender;
            PlayerInventory inv = player.getInventory();

            DeathInv.tempItems.put(player.getName(),
                    DeathInv.khandler.convertInvToItemString(inv) );
            DeathInv.tempArmors.put(player.getName(),
                    DeathInv.khandler.convertArmorToItemString(inv) );

            sender.sendMessage(PREINFO + "Your inv was stored to temporary data.");
            return true;

        } else if ( args[0].equalsIgnoreCase("restore") ) {

            if ( !(sender instanceof Player) ) {
                sender.sendMessage(PREERR + "This command can only be run by a player.");
                return true;
            }

            Player player = (Player)sender;

            String tempItems = DeathInv.tempItems.get(player.getName());
            String tempArmors = DeathInv.tempArmors.get(player.getName());
            if ( tempItems == null ) {
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
                if ( tempItems .length() > 0 ) {
                    ArrayList<ItemStack> items = DeathInv.khandler.convertToItemStack(tempItems);
                    for ( ItemStack item : items ) {
                        if ( item != null )
                            player.getInventory().addItem(item);
                    }
                }
                ArrayList<ItemStack> armors = DeathInv.khandler.convertToItemStack(tempArmors);
                player.getInventory().setHelmet(armors.get(0));
                player.getInventory().setChestplate(armors.get(1));
                player.getInventory().setLeggings(armors.get(2));
                player.getInventory().setBoots(armors.get(3));

                sender.sendMessage(PREINFO + "Your inv was restored from temporary data.");
                return true;
                
            } catch (Exception e) {
                sender.sendMessage(PREERR + e.getMessage());
                return true;
            }

        } else if ( args[0].equalsIgnoreCase("test") ) {

            if ( !(sender instanceof Player) ) {
                sender.sendMessage(PREERR + "This command can only be run by a player.");
                return true;
            }

            Player player = (Player)sender;
            PlayerInventory inv = player.getInventory();
            player.sendMessage(DeathInv.khandler.convertInvToItemString(inv));
            player.sendMessage(DeathInv.khandler.convertArmorToItemString(inv));

            return true;
        }

        return false;
    }
}
