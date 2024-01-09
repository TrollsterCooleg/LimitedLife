package me.cooleg.limitedlife.utils;

public class TextFormatting {

    public static String secondsToTime(long seconds) {
        String result = "";

        long minutes = seconds/60;
        seconds %= 60;
        long hours = minutes/60;
        minutes %= 60;

        if (hours/10 == 0) {
            result += "0";
        }

        result += hours + ":";

        if (minutes/10 == 0) {
            result += "0";
        }

        result += minutes + ":";

        if (seconds/10 == 0) {
            result += "0";
        }

        result += seconds;

        return result;
    }

    public static String replaceWithUnicode(String input) {
        input = input.replaceAll("0", "\uEC00");
        input = input.replaceAll("1", "\uEC01");
        input = input.replaceAll("2", "\uEC02");
        input = input.replaceAll("3", "\uEC03");
        input = input.replaceAll("4", "\uEC04");
        input = input.replaceAll("5", "\uEC05");
        input = input.replaceAll("6", "\uEC06");
        input = input.replaceAll("7", "\uEC07");
        input = input.replaceAll("8", "\uEC08");
        input = input.replaceAll("9", "\uEC09");
        input = input.replaceAll(":", "\uEC0A");

        return input;
    }

}
