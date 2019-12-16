package io.dropwizard.auth;

public class Demo {
    public static final String RPOSITORY = "testRepo";
    public static void main(String[] args) {
        int wek = 4;
        String day;
    
        switch (wek) {
            case 1:
                day = "Sunday";
                break;
            case 2:
                day = "Monday";
                break;
            case 3:
                day = "Tuesday";
                break;
            case 4:
                day = "Wednesday";
                break;
            case 5:
                day = "Thursday";
                break;
            case 6:
                day = "Friday";
                break;
            case 7:
                day = "Saturday";
                break;
        }
        System.out.println(day);
    }
}
