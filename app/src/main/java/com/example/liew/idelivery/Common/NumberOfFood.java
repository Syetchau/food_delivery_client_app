package com.example.liew.idelivery.Common;

public class NumberOfFood {

    public static String convertIdToName(String code){

        if(code.equals("1"))
            return "1-Pc Cheesy Chicken Rice";
        else if(code.equals("2"))
            return "1-Pc Chicken Rice";
        else if(code.equals("3"))
            return "2-Pc Cheesy Chicken Rice";
        else if (code.equals("4"))
            return "2-Pc Chicken Rice";
        else if (code.equals("5"))
            return "1-Pc Chicken Porridge";
        else if (code.equals("6"))
            return "2-Pc Meal";
        else if (code.equals("7"))
            return "3-Pc Meal";
        else if (code.equals("8"))
            return "Half-Spring Chicken Meal";
        else if (code.equals("9"))
            return "6-Pcs Chicken Nuggets Meal";
        else if (code.equals("10"))
            return "Single Cheesy Burger Set Meal";
        else if(code.equals("11"))
            return "Single Premium Burger Set Meal";
        else if(code.equals("12"))
            return "Double Cheesy Burger Set Meal";
        else if (code.equals("13"))
            return "Double Premium Burger Set Meal";
        else if (code.equals("14"))
            return "Full-Spring Chicken Meal";
        else if (code.equals("15"))
            return "2 Person Combo";
        else if (code.equals("16"))
            return "3 Person Combo";
        else if (code.equals("17"))
            return "5 Person Combo";
        else if (code.equals("18"))
            return "Potato Platter (L)";
        else if (code.equals("19"))
            return "Criss Cut Fries (L)";
        else if (code.equals("20"))
            return "Borenos Rice" ;
        else if (code.equals("21"))
            return "Cheesy Wedges (L)";
        else if (code.equals("22"))
            return "Chicken Porridge";
        else if (code.equals("23"))
            return "Mashed Potato (L)";
        else if (code.equals("24"))
            return "3 Pc Fried Buns";
        else if (code.equals("25"))
            return "Coleslaw (L)";
        else if (code.equals("26"))
            return "Chicken Nuggets (6-Pcs)";
        else if (code.equals("27"))
            return "Chicken Nuggets (10-Pcs)";
        else if (code.equals("28"))
            return "Chicken Nuggets (21-Pcs)" ;
        else if (code.equals("29"))
            return "Borenos Sauce";
        else if (code.equals("30"))
            return "Cheese Dip";
        else if (code.equals("31"))
            return "Tartar Sauce";
        else if (code.equals("32"))
            return "Onion Sour Cream Sauce";
        else if (code.equals("33"))
            return "1-Pc Chicken";
        else if (code.equals("34"))
            return "2-Pc Chicken";
        else if (code.equals("35"))
            return "3-Pc Chicken" ;
        else if (code.equals("36"))
            return "5-Pc Chicken";
        else if (code.equals("37"))
            return "9-Pc Chicken";
        else if (code.equals("38"))
            return "15-Pc Chicken";
        else if (code.equals("39"))
            return "Full Spring A La Carte";
        else if (code.equals("40"))
            return "Coca Cola Tin";
        else if (code.equals("41"))
            return "Ice Lemon Tea Tin";
        else if (code.equals("42"))
            return "Passionfruit Tea Tin";
        else if (code.equals("43"))
            return "Ayataka Green Tea Tin";
        else if (code.equals("44"))
            return "Sprite Bottle (1.5L)";
        else if (code.equals("45"))
            return "Coca Cola Bottle (1.5L)";
        else if (code.equals("46"))
            return "AL 5 (Promo) - 5 Pc Chicken";
        else
            return null;
    }
}
