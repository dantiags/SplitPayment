package com.aycron.mobile.splitpayment.helpers;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by carlos.dantiags on 15/9/2016.
 */
public class TicketHelper {

    private static Boolean lineChanged = false;

    public static String extractLines(String fullticket){

        String r = "";

        List<String> results = new ArrayList<>();
        String lines[] = fullticket.split("\\r?\\n");
        String line;
        String candidateLine;

        for (int i=0; i<lines.length; i++){
            line = lines[i];

            candidateLine = parsePriceLine(line, i, lines);
            if(!candidateLine.isEmpty()){
                results.add(candidateLine);
            }

        }

        for (String l : results) {
            r += l + "\n";
        }

        return r;
    }

/*
    private static String parsePriceLine(String line, int i,  String lines[]){

        String candidateLine = "";

        if(line.trim().startsWith("$")){
            line = line.substring(1);
        }

        if(tryParseFloat(line.replace(',', '.'))){
            String candidateItem = lines[i-1];

            if(lineChanged){
                lineChanged = false;
                candidateLine = lines[i+1] + " ----- " + line;

            } else {

                if (candidateItem.trim().startsWith("$")) {
                    lineChanged = true;
                    candidateLine = lines[i + 1] + " ----- " + line;
                } else {
                    candidateLine = candidateItem + " ----- " + line;
                }
            }
        }
        return  candidateLine;
    }
*/


    private static String parsePriceLine(String line, int i,  String lines[]){

        String candidateLine = "";

        if(line.trim().startsWith("$")){
            line = line.substring(1);
        }

        if(tryParseFloat(line.replace(',', '.'))) {
            String candidateItem = lines[i - 1];

            if (candidateItem.trim().startsWith("$")) {
                lineChanged = !lineChanged;
            }

            if(lineChanged){
                candidateLine = lines[i + 1] + " ----- " + line;
            }else {
                candidateLine = candidateItem + " ----- " + line;
            }
        }

        return  candidateLine;
    }

    private static boolean tryParseFloat(String value) {
        try {
            Float.parseFloat(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static List<Float> extractNumbers(String fullticket){
        String result = "";

        List<Float> numbers = new ArrayList<>();
        Pattern p = Pattern.compile("-?\\d+");
        Matcher m = p.matcher(fullticket);
        while (m.find()) {
            result +=  m.group() + "\n";
            numbers.add(Float.parseFloat(m.group()));
        }
        return numbers;
    }
}
