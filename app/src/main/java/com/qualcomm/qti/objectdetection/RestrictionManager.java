package com.qualcomm.qti.objectdetection;

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
        String label = item.toLowerCase();

        // 1. DANGEROUS GOODS / WEAPONS (DANGER)
        if (label.contains("knife") || label.contains("scissors") || label.contains("blade") || 
            label.contains("tool") || label.contains("cutter") || label.contains("lighter") || 
            label.contains("hammer") || label.contains("ax") || label.contains("drill") || 
            label.contains("screwdriver") || label.contains("wrench") || label.contains("pliers")) {
            return new TravelInfo("PROHIBITED IN CARRY-ON. Pack in checked baggage only.", Level.DANGER);
        }

        // 2. BIOSECURITY / CUSTOMS RISKS (DANGER)
        if (label.contains("meat") || label.contains("salami") || label.contains("ham") || 
            label.contains("beef") || label.contains("pork") || label.contains("sausage") ||
            label.contains("fruit") || label.contains("apple") || label.contains("banana") || 
            label.contains("seed") || label.contains("plant") || label.contains("flower") ||
            label.contains("honey") || label.contains("dairy") || label.contains("cheese") || 
            label.contains("egg") || label.contains("vegetable")) {
            
            String msg = "DECLARE TO CUSTOMS. High risk of fines or seizure.";
            if (country.equals("China")) msg = "STRICTLY PROHIBITED. Do not bring raw or cooked food from overseas.";
            return new TravelInfo(msg, Level.DANGER);
        }

        // 3. BATTERIES & POWER (CAUTION)
        if (label.contains("battery") || label.contains("power bank") || label.contains("vape") || label.contains("e-cigarette")) {
            String msg = "CARRY-ON ONLY. Do not pack in checked luggage.";
            if (country.equals("United States")) msg = "CARRY-ON ONLY. Max 100Wh per battery.";
            else if (country.equals("China")) msg = "CARRY-ON ONLY. Max 160Wh. Must have visible capacity labels.";
            return new TravelInfo(msg, Level.CAUTION);
        }

        // 4. LIQUIDS & GELS (CAUTION)
        if (label.contains("bottle") || label.contains("alcohol") || label.contains("perfume") || 
            label.contains("liquid") || label.contains("shampoo") || label.contains("toothpaste") ||
            label.contains("gel") || label.contains("cream") || label.contains("can") || label.contains("aerosol")) {
            
            String msg = "LIQUIDS RULE: Max 100ml (3.4oz) containers in one clear bag.";
            if (country.equals("United States")) msg = "TSA 3-1-1 RULE: 100ml containers in 1 quart bag.";
            if (label.contains("alcohol") && country.equals("China")) msg = "100ml limit for carry-on. No alcohol > 70% ABV.";
            return new TravelInfo(msg, Level.CAUTION);
        }
        
        // 5. SAFE ELECTRONICS & STATIONERY (INFO)
        if (label.contains("laptop") || label.contains("phone") || label.contains("tablet") || 
            label.contains("camera") || label.contains("watch") || label.contains("computer") || 
            label.contains("headphone") || label.contains("pen")) {
            return new TravelInfo("ALLOWED. Standard personal item.", Level.INFO);
        }

        // 6. VALUABLES (INFO)
        if (label.contains("passport") || label.contains("wallet") || label.contains("money") || 
            label.contains("card") || label.contains("jewelry") || label.contains("id")) {
            return new TravelInfo("VALUABLE. Keep on your person. Do not check in.", Level.INFO);
        }

        return new TravelInfo("Standard regulations apply. Ensure item is safe for flight.", Level.INFO);
    }
}
