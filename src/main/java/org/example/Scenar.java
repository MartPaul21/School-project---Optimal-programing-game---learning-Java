package org.example;

import java.util.*;

/** Vsetky vstupy su spolu, aby sa dali ulozit aj nacitat bez straty udajov. */
public class Scenar {
    public static final String[] KLUCE = {"hpPostavy", "damagePostavy", "stamina",
            "zakladnaSpotrebaStaminy", "hpNepriatela", "damageNepriatela",
            "minimalneHpPoUtoku", "maximalnaHmotnost", "maximalnyCas", "katalyzator"};
    public static final String[] POPISY = {"Aktualne HP postavy", "Zakladne DMG postavy",
            "Dostupna stamina", "Spotreba staminy zakladnej kombinacie", "HP nepriatela",
            "DMG nepriatela", "Minimalne HP po utoku", "Maximalna dodatocna hmotnost v kg",
            "Maximalny cas pripravy v minutach", "Dostupny katalyzator v ml"};
    private final Map<String, Double> parametre = new LinkedHashMap<>();
    private final List<Vylepsenie> vylepsenia = new ArrayList<>();

    public double hodnota(String kluc) {
        Double x = parametre.get(kluc);
        if (x == null) throw new IllegalArgumentException("Chyba parameter: " + kluc);
        return x;
    }
    public static double minimum(String kluc) {
        return kluc.equals("hpPostavy") || kluc.equals("hpNepriatela")
                || kluc.equals("minimalneHpPoUtoku") ? 1.0 : 0.0;
    }
    public void nastav(String kluc, double x) {
        if (!Arrays.asList(KLUCE).contains(kluc))
            throw new IllegalArgumentException("Neznamy parameter: " + kluc);
        if (!Double.isFinite(x) || x < minimum(kluc))
            throw new IllegalArgumentException(kluc + ": minimum je " + minimum(kluc));
        parametre.put(kluc, x);
    }
    public List<Vylepsenie> getVylepsenia() { return vylepsenia; }
    public void over() {
        for (String kluc : KLUCE) hodnota(kluc);
        if (vylepsenia.isEmpty()) throw new IllegalArgumentException("Chyba aspon jedno vylepsenie.");
        Set<String> nazvy = new HashSet<>();
        for (Vylepsenie v : vylepsenia)
            if (!nazvy.add(v.getNazov().toLowerCase(Locale.ROOT)))
                throw new IllegalArgumentException("Duplicitny nazov: " + v.getNazov());
    }
    public static Scenar predvoleny() {
        Scenar s = new Scenar();
        double[] hodnoty = {30, 10, 44, 20, 90, 65, 1, .7, 40, 10};
        for (int i = 0; i < KLUCE.length; i++) s.nastav(KLUCE[i], hodnoty[i]);
        s.vylepsenia.add(new Vylepsenie("Ohnivy olej na mec", "10 ml", 4,12,0,1,.01,2,1,5));
        s.vylepsenia.add(new Vylepsenie("Runova esencia na mec", "10 ml", 6,9,3,1,.01,3,2,5));
        s.vylepsenia.add(new Vylepsenie("Ochranna zivica na stit", "10 ml", 3,0,10,2,.02,2,1,5));
        s.vylepsenia.add(new Vylepsenie("Zliatina na hrudnu vybavu", "100 g", 5,0,16,4,.1,4,0,5));
        s.vylepsenia.add(new Vylepsenie("Zliatina na prilbu", "100 g", 4,0,9,2,.1,3,0,5));
        s.vylepsenia.add(new Vylepsenie("Utocna esencia do rukavic", "10 ml", 6,5,4,1,.01,3,2,5));
        s.vylepsenia.add(new Vylepsenie("Vystuzna tkanina do nohavic", "0.1 m", 3,0,8,2,.05,2,0,5));
        s.vylepsenia.add(new Vylepsenie("Ochranna zivica do topanok", "10 ml", 4,2,6,1,.02,2,1,5));
        return s;
    }
}
