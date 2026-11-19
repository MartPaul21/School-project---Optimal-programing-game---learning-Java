package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class DiagnostikaScenara {

    private static final double TOLERANCIA = 1e-6;

    private static final Locale FORMAT =
            Locale.forLanguageTag("sk-SK");

    private DiagnostikaScenara() {
    }

    // Voláme až vtedy, keď optimalizátor oznámi,
    // že neexistuje prípustné riešenie.
    public static String vysvetli(Scenar scenar) {
        List<String> dovody = new ArrayList<>();

        skontrolujStaminu(scenar, dovody);
        skontrolujDamageAHp(scenar, dovody);

        StringBuilder text = new StringBuilder(
                "PRÍPRAVA NIE JE MOŽNÁ\n\n"
        );

        if (dovody.isEmpty()) {
            text.append(
                    "Optimalizátor nenašiel prípustné riešenie.\n\n"
            );

            text.append(
                    "Jednoduché kontroly neodhalili samostatnú "
                            + "príčinu. Základná stamina postačuje "
                            + "a teoretické maximá DMG a HP "
                            + "dosahujú požadované hodnoty.\n\n"
            );

            text.append(
                    "Tieto maximá však nemusia byť dosiahnuteľné "
                            + "pri súčasnom dodržaní všetkých limitov.\n\n"
            );

            text.append(
                    "Presné konfliktné obmedzenia táto základná "
                            + "diagnostika zatiaľ neurčuje. "
                            + "Skontroluj limity zdrojov a vlastnosti "
                            + "vylepšení."
            );

        } else {
            text.append("Zistené dôvody:\n\n");

            for (int i = 0; i < dovody.size(); i++) {
                text.append(i + 1)
                        .append(". ")
                        .append(dovody.get(i))
                        .append("\n\n");
            }

            text.append(
                    "Odstránenie uvedených problémov ešte "
                            + "nezaručuje prípustné riešenie. "
                            + "Stále musia byť splnené všetky "
                            + "ostatné obmedzenia."
            );
        }

        text.append(
                "\n\nPo úprave vstupov klikni na "
                        + "„Vypočítať optimum“."
        );

        return text.toString();
    }

    private static void skontrolujStaminu(
            Scenar scenar,
            List<String> dovody
    ) {
        double dostupna = scenar.hodnota("stamina");
        double zakladna = scenar.hodnota(
                "zakladnaSpotrebaStaminy"
        );

        if (zakladna > dostupna + TOLERANCIA) {
            dovody.add(
                    "NEDOSTATOK STAMINY\n"
                            + "Základná útočná kombinácia potrebuje "
                            + cislo(zakladna)
                            + " bodov staminy, ale postava má iba "
                            + cislo(dostupna)
                            + ".\n"
                            + "Chýba najmenej "
                            + cislo(zakladna - dostupna)
                            + " bodov už pred použitím vylepšení."
            );
        }
    }

    private static void skontrolujDamageAHp(
            Scenar scenar,
            List<String> dovody
    ) {
        double maximalnyBonusDamage = 0;
        double maximalnyBonusHp = 0;

        for (Vylepsenie vylepsenie : scenar.getVylepsenia()) {
            // Poradie z metódy cisla():
            // cena, DMG, HP, stamina, hmotnosť,
            // čas, katalyzátor, maximálne množstvo.
            double[] udaje = vylepsenie.cisla();
            double maximum = udaje[7];

            maximalnyBonusDamage += udaje[1] * maximum;
            maximalnyBonusHp += udaje[2] * maximum;
        }

        double maximalneDamage =
                scenar.hodnota("damagePostavy")
                        + maximalnyBonusDamage;

        double potrebneDamage =
                scenar.hodnota("hpNepriatela");

        // Pri extrémnych vstupoch môže súčet pretiecť.
        // Vtedy z neho nevyvodzujeme konkrétny dôvod.
        if (Double.isFinite(maximalneDamage)
                && maximalneDamage + TOLERANCIA < potrebneDamage) {

            dovody.add(
                    "NEDOSIAHNUTEĽNÉ DMG\n"
                            + "Aj pri maximálnom množstve všetkých "
                            + "vylepšení dosiahne postava najviac "
                            + cislo(maximalneDamage)
                            + " DMG, ale potrebuje "
                            + cislo(potrebneDamage)
                            + ".\n"
                            + "Chýba najmenej "
                            + cislo(potrebneDamage - maximalneDamage)
                            + " DMG.\n"
                            + "Tento horný odhad navyše ignoruje "
                            + "spotrebu zdrojov."
            );
        }

        double maximalneHpPoUtoku =
                scenar.hodnota("hpPostavy")
                        + maximalnyBonusHp
                        - scenar.hodnota("damageNepriatela");

        double potrebneHp =
                scenar.hodnota("minimalneHpPoUtoku");

        if (Double.isFinite(maximalneHpPoUtoku)
                && maximalneHpPoUtoku + TOLERANCIA < potrebneHp) {

            dovody.add(
                    "NEDOSIAHNUTEĽNÉ HP\n"
                            + "Aj pri maximálnom množstve všetkých "
                            + "vylepšení vychádza HP po útoku najviac "
                            + cislo(maximalneHpPoUtoku)
                            + ", ale požadované minimum je "
                            + cislo(potrebneHp)
                            + ".\n"
                            + "Chýba najmenej "
                            + cislo(potrebneHp - maximalneHpPoUtoku)
                            + " HP.\n"
                            + "Tento horný odhad navyše ignoruje "
                            + "spotrebu zdrojov."
            );
        }
    }

    private static String cislo(double hodnota) {
        // Viac desatinných miest pri veľmi malých rozdieloch,
        // aby sa nenulový nedostatok nezobrazil ako 0,00.
        if (hodnota != 0 && Math.abs(hodnota) < 0.01) {
            return String.format(FORMAT, "%.6f", hodnota);
        }

        return String.format(FORMAT, "%.2f", hodnota);
    }
}