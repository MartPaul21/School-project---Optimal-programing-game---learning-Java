package org.example;

/** Spolocny vypocet pre konzolu aj export; numericka kontrola riesenia. */
public class Vysledok {
    private final double[] mnozstva;
    // cena, bonus DMG, bonus HP, dodatocna stamina, hmotnost, cas, katalyzator
    private final double[] sucty = new double[7];
    private final double[] hodnoty, limity, rezervy;
    public static final String[] KONTROLY = {"DMG", "HP po utoku", "Celkova stamina",
            "Dodatocna hmotnost (kg)", "Cas pripravy (min)", "Katalyzator (ml)"};

    public Vysledok(Scenar s, double[] x) {
        s.over();
        if (x.length != s.getVylepsenia().size()) throw new IllegalArgumentException("Nesedi pocet mnozstiev.");
        mnozstva = x.clone();
        for (int i = 0; i < x.length; i++) {
            Vylepsenie v = s.getVylepsenia().get(i);
            if (!Double.isFinite(x[i]) || x[i] < -1e-7 || x[i] > v.getMaximalneMnozstvo() + 1e-7)
                throw new IllegalStateException("Neplatne mnozstvo: " + v.getNazov());
            double[] c = v.cisla();
            for (int j = 0; j < 7; j++) sucty[j] += c[j] * x[i];
        }
        for (double xsum : sucty) if (!Double.isFinite(xsum))
            throw new IllegalStateException("Vysledok pretiekol: pouzi mensie vstupne hodnoty.");
        hodnoty = new double[]{s.hodnota("damagePostavy") + sucty[1],
                s.hodnota("hpPostavy") + sucty[2] - s.hodnota("damageNepriatela"),
                s.hodnota("zakladnaSpotrebaStaminy") + sucty[3], sucty[4], sucty[5], sucty[6]};
        limity = new double[]{s.hodnota("hpNepriatela"), s.hodnota("minimalneHpPoUtoku"),
                s.hodnota("stamina"), s.hodnota("maximalnaHmotnost"), s.hodnota("maximalnyCas"), s.hodnota("katalyzator")};
        rezervy = new double[6];
        for (int j = 0; j < 6; j++) {
            rezervy[j] = j < 2 ? hodnoty[j] - limity[j] : limity[j] - hodnoty[j];
            if (!Double.isFinite(hodnoty[j]) || rezervy[j] < -1e-6)
                throw new IllegalStateException("Vysledok nesplna kontrolu: " + KONTROLY[j]);
        }
    }
    public double[] mnozstva() { return mnozstva.clone(); }
    public double[] sucty() { return sucty.clone(); }
    public double[] hodnoty() { return hodnoty.clone(); }
    public double[] limity() { return limity.clone(); }
    public double[] rezervy() { return rezervy.clone(); }
}
