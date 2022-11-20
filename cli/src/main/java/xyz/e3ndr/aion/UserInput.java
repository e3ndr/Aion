package xyz.e3ndr.aion;

import java.util.Scanner;

public class UserInput {
    private static final Scanner scanner = new Scanner(System.in);

    public static final String YES = "y";
    public static final String NO = "n";

    public static boolean confirm() {
        while (true) {
            String answer = scanner.next().toLowerCase();

            switch (answer) {
                case YES: {
                    return true;
                }

                case NO: {
                    return false;
                }
            }
        }
    }

}
