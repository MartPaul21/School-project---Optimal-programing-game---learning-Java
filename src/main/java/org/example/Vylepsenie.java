package org.example;

/** Cena a ucinky sa vztahuju na jednu uvedenu jednotku materialu. */
public class Vylepsenie {
    private final String nazov;
    private final String jednotka;
    private final double cena, bonusDamage, bonusHp, spotrebaStaminy;
    private final double hmotnost, casPripravy, spotrebaKatalyzatora, maximalneMnozstvo;

    // Povodny konstruktor zostava dostupny.
    public Vylepsenie(String nazov, double cena, double bonusDamage, double bonusHp,
                     double spotrebaStaminy, double hmotnost, double casPripravy,
                     double spotrebaKatalyzatora, double maximalneMnozstvo) {
        this(nazov, "jednotka", cena, bonusDamage, bonusHp, spotrebaStaminy,
                hmotnost, casPripravy, spotrebaKatalyzatora, maximalneMnozstvo);
    }

    public Vylepsenie(String nazov, String jednotka, double cena, double bonusDamage,
                     double bonusHp, double spotrebaStaminy, double hmotnost,
                     double casPripravy, double spotrebaKatalyzatora, double maximalneMnozstvo) {
        if (nazov == null || nazov.isBlank() || jednotka == null || jednotka.isBlank()) {
            throw new IllegalArgumentException("Nazov a jednotka nesmu byt prazdne.");
        }
        for (double x : new double[]{cena, bonusDamage, bonusHp, spotrebaStaminy,
                hmotnost, casPripravy, spotrebaKatalyzatora, maximalneMnozstvo}) {
            if (!Double.isFinite(x) || x < 0) {
                throw new IllegalArgumentException("Ucinky a ceny musia byt konecne nezaporne cisla.");
            }
        }
        this.nazov = nazov.trim(); this.jednotka = jednotka.trim();
        this.cena = cena; this.bonusDamage = bonusDamage; this.bonusHp = bonusHp;
        this.spotrebaStaminy = spotrebaStaminy; this.hmotnost = hmotnost;
        this.casPripravy = casPripravy; this.spotrebaKatalyzatora = spotrebaKatalyzatora;
        this.maximalneMnozstvo = maximalneMnozstvo;
    }
    public String getNazov() { return nazov; }
    public String getJednotka() { return jednotka; }
    public double getCena() { return cena; }
    public double getBonusDamage() { return bonusDamage; }
    public double getBonusHp() { return bonusHp; }
    public double getSpotrebaStaminy() { return spotrebaStaminy; }
    public double getHmotnost() { return hmotnost; }
    public double getCasPripravy() { return casPripravy; }
    public double getSpotrebaKatalyzatora() { return spotrebaKatalyzatora; }
    public double getMaximalneMnozstvo() { return maximalneMnozstvo; }
    public double[] cisla() {
        return new double[]{cena, bonusDamage, bonusHp, spotrebaStaminy,
                hmotnost, casPripravy, spotrebaKatalyzatora, maximalneMnozstvo};
    }
}
