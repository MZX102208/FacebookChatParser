import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Main {

    private static final String FILE_NAME = "log.txt";
    private static int mNumLinkText = 0;
    private static int mNumVidText = 0;
    private static boolean mIsReactionNumber = false;
    private static String currentName = "";
    private static Map<String, Stat> people = new HashMap<>();

    public static void main(String[] args) throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader(new File(FILE_NAME)));
        String storage = "";
        String temp;
        while ((temp = reader.readLine()) != null) storage += "\n" + temp;
        System.out.println(storage.length());
        System.out.println(storage.substring(storage.length()-20, storage.length()));

        Scanner s = new Scanner(storage);
        s.useDelimiter("(><)");
        while (s.hasNext()) {
            String str = s.next();
            if (str.contains("video muted=")) { //detect if extra video information is coming and don't display them
                mNumVidText = 6;
            } else if (str.contains("style=\"background-image:") && mNumLinkText == 0) { //If an attachment is shared
                System.out.println("Image posted");
                people.get(currentName).mNumImages++;
            } else if (str.contains("class=\"_4kf7 preview\"") && str.contains("div data-tooltip-content")) { //Reaction counters
                String[] reactList = removeReactTags(str).split("\n");
                for (String name : reactList) {
                    String firstName = name.split(" ")[0];
                    System.out.println("Reaction: " + firstName);
                    if (!people.containsKey(firstName)) people.put(firstName, new Stat());
                    people.get(firstName).mNumReacts++;
                }
                mIsReactionNumber = true;
            } else if (str.contains("time class=\"_3oh-\"")) { //When facebook marks a new time in conversation
                System.out.println("New Time: " + removeTagChars(str));
            } else if (str.contains("h5 class=\"_ih3\"") || str.contains("h5 class=\"_ih3 accessible_elem\"")) { //Name is displayed
                currentName = removeTagChars(str);
                System.out.println("Name: " + currentName);
                if (!people.containsKey(currentName)) people.put(currentName, new Stat());
            } else if (str.matches("[^>|^<]+>[^>|^<]+<[^>|^<]+")) { //Any kind of raw string from html, parsed as message/link
                String message = removeTagChars(str);
                mNumLinkText = mNumLinkText > 0 ? mNumLinkText - 1 : 0;
                mNumVidText = mNumVidText > 0 ? mNumVidText - 1 : 0;
                if (isLink(message)) {
                    System.out.println("Link: " + message);
                    mNumLinkText = 4;
                    people.get(currentName).mNumLinks++;
                }
                if (!mIsReactionNumber && mNumLinkText == 0 && mNumVidText == 0) {
                    System.out.println(message);
                    people.get(currentName).mMessages.add(message);
                }
                if (mIsReactionNumber) mIsReactionNumber = false;
            }
        }

        System.out.println();
        System.out.println("Number of messages sent: ");
        for (String name : people.keySet()) {
            System.out.println(name + ": " + people.get(name).mMessages.size());
        }
        System.out.println();
        System.out.println("Number of images sent: ");
        for (String name : people.keySet()) {
            System.out.println(name + ": " + people.get(name).mNumImages);
        }
        System.out.println();
        System.out.println("Number of links sent: ");
        for (String name : people.keySet()) {
            System.out.println(name + ": " + people.get(name).mNumLinks);
        }
        System.out.println();
        System.out.println("Number of reactions: ");
        for (String name : people.keySet()) {
            System.out.println(name + ": " + people.get(name).mNumReacts);
        }
    }

    private static String removeTagChars(String s) {
        return s.replaceAll("[^>|^<]+>", "")
                .replaceAll("<[^>|^<]+", "");
    }

    private static String removeReactTags(String s) {
        return s.replace("div data-tooltip-content=\"", "")
                .replaceAll("\".+", "");
    }

    private static boolean isLink(String s) {
        return (s.contains("https://") || s.contains("http://")) && mNumLinkText == 0;
    }

    private static void writeToFile(String fileName, String str) {
        try {
            FileWriter fileWriter = new FileWriter(fileName);
            PrintWriter printWriter = new PrintWriter(fileWriter);
            printWriter.println(str);
            printWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class Stat {
    ArrayList<String> mMessages = new ArrayList<>();
    int mNumImages = 0;
    int mNumReacts = 0;
    int mNumLinks = 0;
}