package util;

public class ParseHelpers {
    public static String toBinaryString(int i) {
        String binary = Integer.toBinaryString(i);
        System.out.println(i + ", " + binary);
        if (binary.length() < 8) {
            for (int j = 0; j < 8 - binary.length(); j++) {
                binary = "0" + binary;
            }
        }
        // System.out.println(i + ", " + binary.length());
        return binary;
    }

    public static int search(Object[] array, Object element) {
        for (int i = 0; i < array.length; i++) {
            if (array[i].equals(element)) {
                return i;
            }
        }
        return -1;
    }
}
