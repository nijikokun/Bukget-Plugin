/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package omg.bukget.utils;

/**
 *
 * @author Nijikokun
 */
public class Calculator {

    private static String charIndex = " !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_'abcdefghijklmnopqrstuvwxyz{|}~⌂ÇüéâäàåçêëèïîìÄÅÉæÆôöòûùÿÖÜø£Ø×ƒáíóúñÑªº¿®¬½¼¡«»";
    private static int[] charWidths = {4, 2, 5, 6, 6, 6, 6, 3, 5, 5, 5, 6, 2, 6, 2, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 2, 2, 5, 6, 5, 6, 7, 6, 6, 6, 6, 6, 6, 6, 6, 4, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 4, 6, 4, 6, 6, 3, 6, 6, 6, 6, 6, 5, 6, 6, 2, 6, 5, 3, 6, 6, 6, 6, 6, 6, 6, 4, 6, 6, 6, 6, 6, 6, 5, 2, 5, 7, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 4, 6, 3, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 4, 6, 6, 3, 6, 6, 6, 6, 6, 6, 6, 7, 6, 6, 6, 2, 6, 6, 8, 9, 9, 6, 6, 6, 8, 8, 6, 8, 8, 8, 8, 8, 6, 6, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 6, 9, 9, 9, 5, 9, 9, 8, 7, 7, 8, 7, 8, 8, 8, 7, 8, 8, 7, 9, 9, 6, 7, 7, 7, 7, 7, 9, 6, 7, 8, 7, 6, 6, 9, 7, 6, 7, 1};

    public static int getStringWidth(String s) {
        int i = 0;

        if (s != null) {
            for (int j = 0; j < s.length(); j++) {
                i += getCharWidth(s.charAt(j));
            }
        }

        return i;
    }

    public static int getCharWidth(char c) {
        int k = charIndex.indexOf(c);

        if ((c != '§') && (k >= 0)) {
            return charWidths[k];
        }

        return 0;
    }

    public static String substring(String name, int left) {
        while (getStringWidth(name) > left) {
            name = name.substring(0, name.length() - 1);
        }
        return name;
    }

    public static String whitespace(int length) {
        int spaceWidth = getCharWidth(' ');

        StringBuilder ret = new StringBuilder();

        for (int i = 0; i < length - spaceWidth; i += spaceWidth) {
            ret.append(" ");
        }

        return ret.toString();
    }

    public static String dashes(int length) {
        int spaceWidth = getCharWidth('-');

        StringBuilder ret = new StringBuilder();

        for (int i = 0; i < length - spaceWidth; i += spaceWidth) {
            ret.append("-");
        }

        return ret.toString();
    }
}
