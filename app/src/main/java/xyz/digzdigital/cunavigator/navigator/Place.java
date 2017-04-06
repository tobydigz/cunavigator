package xyz.digzdigital.cunavigator.navigator;


import java.util.ArrayList;

public enum Place {
    ESTHER_HALL("Esther Hall", 6.669645, 3.155561, "esther"),
    MARY_HALL("Mary Hall", 6.671841, 3.154649, "mary"),
    DEBORAH_HALL("Deborah Hall", 6.671145, 3.155625, "deborah"),
    LYDIA_HALL("Lydia Hall", 6.671841, 3.155453, "lydia"),
    DORCAS_HALL("Dorcas Hall", 6.672096, 3.157003, "dorcas"),
    PETER_HALL("Peter Hall", 6.668441, 3.154540, "peter"),
    JOHN_HALL("John Hall", 6.669325, 3.153199, "john"),
    PAUL_HALL("Paul Hall", 6.670963, 3.153819, "paul"),
    JOSEPH_HALL("Joseph Hall", 6.670734, 3.153159, "joseph"),
    DANIEL_HALL("Daniel Hall", 6.671874, 3.152649, "daniel"),
    PG_MALE_HALL("Post Graduate Male Hall", 6.666316, 3.156415,"pgm"),
    PG_FEMALE_HALL("Post Graduate Female Hall", 6.666719, 3.156874,"pgf"),
    ROUNDABOUT("Center Circle", 6.7671615, 3.157788,"rnd"),
    CLR("Centre for Learning Resources", 6.6717432, 3.1568543,"clr"),
    CHAPEL("University Chapel", 6.6717432, 3.1568543, "chapel"),
    ALDC("African Leadership Development Centre", 6.672305, 3.162879, "aldc"),
    CST("College of Science and Technology", 6.6717124, 3.1543636, "cst"),
    CUCRID("Covenant University Centre for Research, Innovation and Development", 6.672937, 3.161116, "cucrid"),
    CBSS("College of Business and Social Sciences", 6.671671, 3.160256, "cbss"),
    CEDS("Centre for Entrepreneurial Studies", 6.671306, 3.161450, "ceds"),
    MECH("Mechanical Engineering Department", 6.673187, 3.162638, "mech"),
    CVE("Civil Engineering Department", 6.674559, 3.162512, "cve"),
    PET("Petroleum and Chemical Engineering Department", 6.674142, 3.157427, "pet"),
    EIE("Electrical and Information Engineering Department", 6.675785, 3.162421, "eie"),
    LT("Lecture Theatre", 6.6717124, 3.1543636,"lt"),
    ICT_1("Zenith Bank ICT centre", 6.6717124, 3.1543636, "zenith"),
    ICT_2("Diamond Bank ICT centre", 6.673988, 3.157946, "diamond"),
    CAFE_1("Cafeteria One", 6.669453, 3.154080,"cafe1"),
    CAFE_2("Cafeteria Two", 6.672567, 3.162012, "cafe2"),
    CAFE_PG("PostGraduate Cafeteria", 6.6661310, 3.156922,"pgcafe"),
    CUGH("Covenant University Guest House", 6.671479, 3.162654, "cugh"),
    ;
    private final String name;
    private final String uid;
    private final double latitude;
    private final double longitude;

    Place(String name, double latitude, double longitude, String uid){
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.uid = uid;
    }



    public String placeName(){
        return name;
    }

    public double latitude(){
        return latitude;
    }

    public double longitude(){
        return longitude;
    }

    public String uid(){
        return uid;
    }

}
