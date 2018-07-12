/*
 * Transliterator.java
 *
 * Created on 4 ���� 2007 �., 1:45
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package kz.wg.utils;

/**
 *
 * @author Alexey Shklyaev
 */
public class Transliterator { 

    static char russianAlphabet[] = {
        'а', 'ә', 'б', 'в', 'г', 'ғ', 'д', 'е', 'ё', 'ж', 'з', 'и', 'й', 'к', 'қ', 'л', 'м', 'н', 'ң', 'о', 'ө', 'п', 'р',
        'с', 'т', 'у', 'ү', 'ұ', 'ф', 'х', 'ц', 'ч', 'ш', 'щ', 'ъ', 'ы', 'і', 'ь', 'э', 'ю', 'я',
        'А', 'Ә', 'Б', 'В', 'Г', 'Ғ', 'Д', 'Е', 'Ё', 'Ж', 'З', 'И', 'Й', 'К', 'Қ', 'Л', 'М', 'Н', 'Ң', 'О', 'Ө', 'П',
        'Р', 'С', 'Т', 'У', 'Ү', 'Ұ', 'Ф', 'Х', 'Ц', 'Ч', 'Ш', 'Щ', 'Ъ', 'Ы', 'Ь', 'І', 'Э', 'Ю', 'Я'
    };
    static String englishAlphabet[] = {
        "a", "a", "b", "v", "g", "g", "d", "e", "yo", "zh", "z", "i", "i", "k", "k", "l", "m", "n", "n", "o", "o", "p",
        "r", "s", "t", "u", "u", "u", "f", "h", "ts", "ch", "sh", "sch",
        "i", "y", "i", "i", "e", "yu", "ya",
        "A", "A", "B", "V", "G", "G", "D", "E", "Yo", "Zh", "Z", "I", "I", "K", "K", "L", "M",
        "N", "N", "O", "O", "P", "R", "S", "T", "U", "U", "U", "F", "H", "Ts", "Ch", "Sh", "Sch",
        "I", "Y", "I", "I", "E", "Yu", "Ya"
    };

    /**
     * Creates a new instance of Transliterator
     */
    public Transliterator() {
    }

    public static String translit(String src) {
        String convertedString = "";
        for (int i = 0; i < src.length(); i++) {
            char temp = src.charAt(i);
            boolean found = false;
            for (int j = 0; j < russianAlphabet.length; j++) {
                if (temp == russianAlphabet[j]) {
                    convertedString += englishAlphabet[j];
                    found = true;
                    break;
                }
            }
            if (!found) {
                convertedString += temp;
            }
        }
        return convertedString;
    }


}
