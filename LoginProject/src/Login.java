import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;

public class Login {
    //Log into Existing Account
    //Register for New Account
    //Change Password
    //Log Out
    private static final int SALTLENGTH = 10;
    private static final String HASHSCHEME = "SHA-256";
    private static final HashSet<Integer> VALIDANSWERS = new HashSet<>(Arrays.asList(1,2,3,4));
    private static final char REGEX = ':';
    private static final int USERNAMEi = 0;
    private static final int USERIDi= 1;
    private static final int PASSWORDi= 2;
    private static final int SALTi = 3;
    private static final int HASHSCHEMEi = 4;
    private static final File data = new File("data.txt");

    public static void main(String[] args) {
        while (true) {
            Scanner userInput = new Scanner(System.in);
            System.out.println("What would you like to do?");
            System.out.println("1 - Sign In");
            System.out.println("2 - Create Account");
            System.out.println("3 - Change Account Password");
            System.out.println("4 - Sign Out [Exits Program]");
            int userAnswer = userInput.nextInt();
            while (!VALIDANSWERS.contains(userAnswer)) {
                System.out.println("Invalid command, please try again");
                userAnswer = userInput.nextInt();
            }
            switch (userAnswer) {
                case 1:
                    signIn();
                    break;
                case 2:
                    createAccount();
                    break;
                case 3:
                    updatePassword();
                    break;
                case 4:
                    System.out.println("You have been signed out - Exiting Program");
                    System.exit(0);
                default:
                    System.out.println("VALIDANSWERS and Switch statement are not in sync");
            }
            System.out.println();
            try {
                Thread.sleep(1500);
            } catch (Exception e) {
                //Do nothing
            }
        }
    }
    public static int signIn() {
        Scanner userInput = new Scanner(System.in);
        System.out.println("Enter your userID: ");
        String userID = userInput.nextLine();

        int userLine = searchData(userID, USERIDi);
        if (userLine > -1) {
            System.out.println("Enter your password: ");
            String password = userInput.nextLine();
            String hashedPassword = generateHash(password,getData(userLine, SALTi),getData(userLine, HASHSCHEMEi));
            if (searchData(hashedPassword,PASSWORDi) == userLine ) {
                System.out.println("Welcome, " + getData(userLine, USERNAMEi)+ "!");
            } else {
                System.out.println("The password was incorrect.");
                userLine = -1;
            }
        } else {
            System.out.println("No user with the userID \"" + userID+ "\" was found.");
        }
        return userLine;
    }
    public static void createAccount() {
        Scanner userInput = new Scanner(System.in);
        System.out.println("Enter your username: ");
        String userName = userInput.nextLine();
        System.out.println("Enter your unique userID: ");
        String userID = userInput.nextLine();
        while (searchData(userID, USERIDi) != -1) {
            System.out.println("This userID is already in use, please pick another:");
            userID = userInput.nextLine();
        }
        System.out.println("Enter your password: ");
        String password = userInput.nextLine();
        addData(userName, userID, password);
        System.out.println("Account Created");
    }
    public static void updatePassword() {
        System.out.println("Please sign in in order to edit your account information.");
        int userLine = signIn();
        if (userLine > -1) {
            Scanner userInput = new Scanner(System.in);
            System.out.println("Enter your new password:");
            String newPassword = userInput.nextLine();
            String hashedPassword = generateHash(newPassword, getData(userLine, SALTi), HASHSCHEME);
            changePassword(hashedPassword, userLine, HASHSCHEME);
            System.out.println("Password Changed");
        } else {
            System.out.println("Sign in invalid");
        }
    }
    private static String generateHash(String password, String salt, String scheme) {
        MessageDigest hash = null;
        try {
            hash = MessageDigest.getInstance(scheme);
        } catch (Exception e) {
            System.out.println("Algorithm Not Found, Exiting Program");
            System.exit(1);
        }
        String passwordToHash = password + salt;
        byte[] hashed = hash.digest(passwordToHash.getBytes(StandardCharsets.UTF_8));
        return hexStringFromByteArray(hashed);
    }
    private static String hexStringFromByteArray(byte[] array) {
        StringBuilder hexString = new StringBuilder();
        for (byte item : array) {
            String toAdd = Integer.toHexString(0xff & item);
            if (toAdd.length() == 1) {
                hexString.append("0");
            }
            hexString.append(toAdd);
        }
        return hexString.toString();
    }
    private static String generateSalt() {
        StringBuilder salt = new StringBuilder();
        for (int i = 0; i < SALTLENGTH; i++) {
            char c = (char)((int)(Math.random()*93)+33);
            if (c != REGEX) {
                salt.append(c);
            } else {
                i--;
            }
        }
        return salt.toString();
    }
    private static void addData(String userName, String userID, String password) {
        try {
            PrintWriter writer = new PrintWriter(new FileWriter(data, true));
            String salt = generateSalt();
            String toWrite = userName + REGEX + userID + REGEX + generateHash(password, salt, HASHSCHEME) + REGEX + salt + REGEX + HASHSCHEME;
            writer.println(toWrite);
            writer.close();
        } catch (Exception e) {
            System.out.println("File Not Found, Exiting Program");
            System.exit(1);
        }
    }
    private static int searchData(String find, int at) {
        Scanner scan = null;
        try {
            scan = new Scanner(data);

        } catch (Exception e) {
            System.out.println("File Not Found, Exiting Program");
            System.exit(1);
        }
        int lineI = 0;
        while (scan.hasNextLine()) {
            String line = scan.nextLine();
            String[] tokens = line.split(REGEX + "");
            if (tokens[at].equals(find)) {
                return lineI;
            }
            lineI++;
        }
        scan.close();
        return -1;
    }
    private static String getData(int line, int at) {
        String userLine = getLine(line);
        return userLine.split(REGEX+"")[at];
    }
    private static String getLine(int line) {
        Scanner scan = null;
        try {
            scan = new Scanner(data);

        } catch (Exception e) {
            System.out.println("File Not Found, Exiting Program");
            System.exit(1);
        }
        int lineI = 0;
        while (scan.hasNextLine() && lineI < line) {
            scan.nextLine();
            lineI++;
        }
        return scan.nextLine();
    }
    private static void changePassword(String newPassword, int line, String scheme) {
        String oldLine = getLine(line);
        String toWrite = getToWrite(newPassword, line, scheme);

        File temp = new File("temp.txt");

        Scanner scan = null;
        PrintWriter writer = null;
        try {
            scan = new Scanner(data);
            writer = new PrintWriter(new FileWriter(temp));
        } catch (Exception e) {
            System.out.println("File Not Found, Exiting Program");
            System.exit(1);
        }
        writer.print("");
        while (scan.hasNextLine()) {
            String nextLine = scan.nextLine();
            if (!nextLine.equals(oldLine)) {
                writer.println(nextLine);
            }
        }
        writer.println(toWrite);
        writer.close();
        scan.close();
        rewriteData(temp);
    }
    private static void rewriteData(File from) {
        Scanner scan = null;
        PrintWriter writer = null;
        try {
            scan = new Scanner(from);
            writer = new PrintWriter(new FileWriter(data));
        } catch (Exception e) {
            System.out.println("File Not Found, Exiting Program");
            System.exit(1);
        }
        writer.print("");
        while (scan.hasNextLine()) {
                writer.println(scan.nextLine());
        }
        writer.close();
        scan.close();
    }
    private static String getToWrite(String newData, int line, String scheme) {
        String oldLine = getLine(line);
        String[] tokens = oldLine.split(REGEX+"");
        StringBuilder toWrite = new StringBuilder();

        for (int i = 0; i < tokens.length; i++) {
            if  (i == HASHSCHEMEi) {
                toWrite.append(scheme);
            } else if (i != PASSWORDi) {
                toWrite.append(tokens[i]).append(":");
            } else {
                toWrite.append(newData).append(":");
            }
        }
        return toWrite.toString();
    }
}