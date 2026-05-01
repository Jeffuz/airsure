package com.qualcomm.qti.objectdetection;

import android.util.Log;
import java.util.HashMap;
import java.util.Map;

public class RestrictionManager {

    public enum Level {
        INFO, CAUTION, DANGER
    }

    public static class TravelInfo {
        public String message;
        public Level level;

        public TravelInfo(String message, Level level) {
            this.message = message;
            this.level = level;
        }
    }

    public static TravelInfo getRestriction(String item, String country) {
        if (item == null || item.isEmpty()) {
            return new TravelInfo("Checking TSA rules...", Level.INFO);
        }
        
        String label = item.toLowerCase().trim();
        Log.d("RestrictionManager", "Checking restrictions for: '" + label + "' in " + country);

        // --- 1. DANGER: PROHIBITED ITEMS ---
        if (label.contains("scissor") || label.contains("knife") || label.contains("knive") ||
            label.contains("blade") || label.contains("cutter") || label.contains("tool") || 
            label.contains("hammer") || label.contains("ax") || label.contains("drill") || 
            label.contains("screwdriver") || label.contains("wrench") || label.contains("pliers") ||
            label.contains("fork") || label.contains("lighter")) {
            
            return new TravelInfo("NOT ALLOWED in carry-on. Prohibited sharp object or tool. Pack in checked baggage.", Level.DANGER);
        }

        // --- 2. DANGER: BIOSECURITY / FOOD ---
        if (label.contains("meat") || label.contains("salami") || label.contains("ham") || 
            label.contains("beef") || label.contains("pork") || label.contains("sausage") ||
            label.contains("apple") || label.contains("banana") || label.contains("orange") || 
            label.contains("broccoli") || label.contains("carrot") || label.contains("vegetable") ||
            label.contains("fruit") || label.contains("seed") || label.contains("plant") || 
            label.contains("flower") || label.contains("honey") || label.contains("dairy") || 
            label.contains("cheese") || label.contains("egg")) {
            
            String msg = "DECLARE TO CUSTOMS. High risk of fines or seizure.";
            if ("China".equalsIgnoreCase(country)) msg = "STRICTLY PROHIBITED. Do not bring raw or cooked food from overseas.";
            return new TravelInfo(msg, Level.DANGER);
        }

        // --- 3. CAUTION: BATTERIES & LIQUIDS ---
        if (label.contains("battery") || label.contains("power bank") || label.contains("vape") || label.contains("e-cigarette")) {
            String msg = "CAREFUL: Carry-on only. Do not pack in checked luggage.";
            if ("United States".equalsIgnoreCase(country)) msg = "CAREFUL: Carry-on only. Max 100Wh per battery.";
            else if ("China".equalsIgnoreCase(country)) msg = "CAREFUL: Carry-on only. Max 160Wh. Must have visible labels.";
            return new TravelInfo(msg, Level.CAUTION);
        }

        if (label.contains("bottle") || label.contains("wine glass") || label.contains("cup") ||
            label.contains("liquid") || label.contains("shampoo") || label.contains("toothpaste") ||
            label.contains("gel") || label.contains("cream") || label.contains("can") || label.contains("aerosol") ||
            label.contains("alcohol") || label.contains("perfume")) {
            
            String msg = "CAREFUL: Max 100ml (3.4oz) containers in one clear bag.";
            if ("United States".equalsIgnoreCase(country)) msg = "CAREFUL: TSA 3-1-1 Rule applies. 100ml containers only.";
            return new TravelInfo(msg, Level.CAUTION);
        }
        
        // --- 4. INFO: ALLOWED ITEMS ---
        if (label.contains("laptop") || label.contains("phone") || label.contains("tablet") || 
            label.contains("camera") || label.contains("watch") || label.contains("computer") || 
            label.contains("headphone") || label.contains("mouse") || label.contains("keyboard") ||
            label.contains("remote") || label.contains("book") || label.contains("pen") ||
            label.contains("backpack") || label.contains("handbag") || label.contains("suitcase") ||
            label.contains("toothbrush") || label.contains("umbrella") || label.contains("teddy bear") ||
            label.contains("person")) {
            return new TravelInfo("ALLOWED in carry-on. Standard personal item.", Level.INFO);
        }

        // --- 5. INFO: VALUABLES ---
        if (label.contains("passport") || label.contains("wallet") || label.contains("money") || 
            label.contains("card") || label.contains("jewelry") || label.contains("id")) {
            return new TravelInfo("ALLOWED: Valuable item. Keep on your person.", Level.INFO);
        }

        return new TravelInfo("Standard regulations apply. Safe for flight if no sharp/liquid parts.", Level.INFO);
    }
}
