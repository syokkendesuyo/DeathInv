/*
 * @author     ucchy
 * @license    GPLv3
 * @copyright  Copyright ucchy 2013
 */
package com.github.ucchyocean.mdi;

import java.util.ArrayList;

/**
 * @author ucchy
 * ユーティリティクラス
 */
public class Utility {

    /**
     * 文字列内のカラーコードを置き換えする
     * @param source 置き換え元の文字列
     * @return 置き換え後の文字列
     */
    public static String replaceColorCode(String source) {
        return source.replaceAll("&([0-9a-fk-or])", "\u00A7$1");
    }

    public static ArrayList<String> replaceColorCode(ArrayList<String> source) {
        for ( int i=0; i<source.size(); i++ ) {
            String temp = replaceColorCode(source.get(i));
            source.set(i, temp);
        }
        return source;
    }
}
