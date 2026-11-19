package org.example;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import org.apache.commons.math3.optim.linear.NoFeasibleSolutionException;
import org.apache.commons.math3.optim.linear.UnboundedSolutionException;

public class Main {
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.println("\nOPTIMALIZACIA VYBAVY");
                System.out.println("1. Zadat novy scenar rucne");
                System.out.println("2. Importovat scenar z Excelu");
                System.out.println("3. Vytvorit excelovu sablonu s povodnymi udajmi");
                System.out.println("0. Ukoncit program");
                System.out.print("Volba: ");
                String volba = riadok(scanner);
                if (volba.equals("0")) break;
                try {
                    if (volba.equals("3")) {
                        exportuj(scanner, Scenar.predvoleny(), null, "NEVYPOCITANE - sablona");
                        continue;
                    }
                    Scenar scenar;
                    if (volba.equals("1")) {
                        scenar = Scenar.predvoleny();
                        upravScenar(scanner, scenar);
                        upravVylepsenia(scanner, scenar.getVylepsenia());
                    } else if (volba.equals("2")) {
                        Path cesta = nacitajCestu(scanner, "Cesta k existujucemu .xlsx (Enter = spat): ");
                        if (cesta == null) continue;
                        scenar = ExcelScenar.nacitaj(cesta);
                        System.out.println("Import uspesny: " + cesta.toAbsolutePath());
                        zobrazScenar(scenar);
                        System.out.println("Stary harok Vysledok sa nepouzije. Vypocet sa spusti nanovo.");
                        if (nacitajAnoNie(scanner, "Chces pred vypoctom upravit vstupy? (a/n): ")) {
                            upravScenar(scanner, scenar);
                            upravVylepsenia(scanner, scenar.getVylepsenia());
                        }
                    } else {
                        System.out.println("Zadaj 0, 1, 2 alebo 3."); continue;
                    }
                    spustiScenar(scanner, scenar);
                } catch (IOException | IllegalArgumentException e) {
                    System.out.println("Operacia sa nepodarila: " + e.getMessage());
                    System.out.println("Vraciam sa do hlavnej ponuky.");
                }
            }
            System.out.println("Program bol ukonceny.");
        } catch (NoSuchElementException e) {
            System.out.println("Vstup z konzoly bol ukonceny.");
        }
    }

    private static void spustiScenar(Scanner scanner, Scenar scenar) {
        double[] mnozstva = null;
        String stav;
        System.out.println("\nVYPOCET OPTIMALNEJ PRIPRAVY");
        try {
            double[] kandidat = Optimalizator.vypocitaj(scenar);
            Vysledok vysledok = new Vysledok(scenar, kandidat);
            mnozstva = kandidat; // Ulozime len vysledok, ktory presiel kontrolou.
            vypisVysledok(scenar, vysledok);
            stav = "OPTIMALNE";
        } catch (NoFeasibleSolutionException e) {
            stav = "NEPRIPUSTNE - neexistuje pripustna priprava";
            System.out.println(stav);
        } catch (UnboundedSolutionException e) {
            stav = "CHYBA - neohraniceny model";
            System.out.println(stav);
        } catch (IllegalStateException e) {
            stav = "VYPOCET NEPOTVRDENY: " + e.getMessage();
            System.out.println(stav);
        }
        // Aj nepripustny scenar sa da ulozit a neskor analyzovat.
        if (nacitajAnoNie(scanner, "\nUlozit scenar a dostupny vysledok do Excelu? (a/n): "))
            exportuj(scanner, scenar, mnozstva, stav);
    }

    private static void exportuj(Scanner scanner, Scenar scenar, double[] x, String stav) {
        System.out.println("Zadaj novy nazov .xlsx. Existujuci subor sa nikdy neprepise.");
        while (true) {
            try {
                Path cesta = nacitajCestu(scanner, "Cesta pre ulozenie (Enter = zrusit): ");
                if (cesta == null) return;
                ExcelScenar.uloz(cesta, scenar, x, stav);
                System.out.println("Ulozene: " + cesta.toAbsolutePath().normalize());
                return;
            } catch (FileAlreadyExistsException e) {
                System.out.println("Subor uz existuje. Zvol iny nazov, napriklad scenar_02.xlsx.");
            } catch (IOException | IllegalArgumentException e) {
                System.out.println("Ukladanie sa nepodarilo: " + e.getMessage());
                System.out.println("Skontroluj existenciu priecinka a zapisove prava alebo zadaj inu cestu.");
            }
        }
    }

    private static Path nacitajCestu(Scanner scanner, String otazka) {
        System.out.print(otazka);
        String vstup = riadok(scanner);
        if (vstup.isEmpty()) return null;
        if (vstup.length() >= 2 && vstup.startsWith("\"") && vstup.endsWith("\""))
            vstup = vstup.substring(1, vstup.length() - 1);
        return Path.of(vstup);
    }

    private static void upravScenar(Scanner scanner, Scenar s) {
        System.out.println("\nNASTAVENIE SCENARA - Enter ponecha zobrazenu hodnotu.");
        for (int i = 0; i < Scenar.KLUCE.length; i++) {
            String kluc = Scenar.KLUCE[i];
            if (kluc.equals("minimalneHpPoUtoku")) continue;
            s.nastav(kluc, nacitajCislo(scanner, Scenar.POPISY[i], s.hodnota(kluc), Scenar.minimum(kluc)));
        }
    }
    private static void zobrazScenar(Scenar s) {
        for (int i = 0; i < Scenar.KLUCE.length; i++)
            System.out.println(Scenar.POPISY[i] + ": " + s.hodnota(Scenar.KLUCE[i]));
        System.out.println("Pocet vylepseni: " + s.getVylepsenia().size());
    }

    private static void upravVylepsenia(Scanner scanner, List<Vylepsenie> vylepsenia) {
        while (true) {
            System.out.println("\n--- UPRAVA VYLEPSENI ---");
            for (int i = 0; i < vylepsenia.size(); i++) {
                Vylepsenie v = vylepsenia.get(i);
                System.out.printf("%d. %s (%s) | cena: %.2f | DMG: %.2f | HP: %.2f%n",
                        i+1, v.getNazov(), v.getJednotka(), v.getCena(), v.getBonusDamage(), v.getBonusHp());
            }
            System.out.println("N. Pridat nove vylepsenie");
            System.out.println("0 alebo Enter. Dokoncit upravy a spustit vypocet");
            System.out.print("Volba: ");
            String vstup = riadok(scanner);
            if (vstup.isEmpty() || vstup.equals("0")) return;
            if (vstup.equalsIgnoreCase("n")) { pridajVylepsenie(scanner, vylepsenia); continue; }
            try {
                int index = Integer.parseInt(vstup) - 1;
                if (index < 0 || index >= vylepsenia.size()) {
                    System.out.println("Neplatne cislo polozky."); continue;
                }
                vylepsenia.set(index, nacitajVylepsenie(scanner, vylepsenia.get(index)));
            } catch (NumberFormatException e) { System.out.println("Zadaj cislo polozky alebo N."); }
        }
    }

    private static void pridajVylepsenie(Scanner scanner, List<Vylepsenie> vylepsenia) {
        String nazov;
        while (true) {
            System.out.print("Nazov noveho vylepsenia: "); nazov = riadok(scanner);
            boolean existuje = false;
            for (Vylepsenie v : vylepsenia) if (v.getNazov().equalsIgnoreCase(nazov)) existuje = true;
            if (!nazov.isBlank() && !existuje) break;
            System.out.println("Zadaj neprazdny jedinecny nazov.");
        }
        String jednotka;
        do { System.out.print("Jedna jednotka znamena (napr. 10 ml alebo 100 g): "); jednotka = riadok(scanner); }
        while (jednotka.isBlank());
        Vylepsenie prazdne = new Vylepsenie(nazov, jednotka, 0,0,0,0,0,0,0,5);
        vylepsenia.add(nacitajVylepsenie(scanner, prazdne));
    }

    private static Vylepsenie nacitajVylepsenie(Scanner scanner, Vylepsenie stare) {
        System.out.println("Upravujes: " + stare.getNazov() + " | jedna jednotka: " + stare.getJednotka());
        System.out.println("Ceny a ucinky zadavaj na jednu jednotku. Enter ponecha hodnotu.");
        String[] otazky = {"Cena v goldoch", "Bonus DMG", "Bonus HP", "Dodatocna stamina",
                "Hmotnost v kg", "Cas v minutach", "Katalyzator v ml", "Maximalne mnozstvo"};
        double[] a = stare.cisla();
        for (int i = 0; i < a.length; i++) a[i] = nacitajCislo(scanner, otazky[i], a[i], 0);
        return new Vylepsenie(stare.getNazov(), stare.getJednotka(), a[0],a[1],a[2],a[3],a[4],a[5],a[6],a[7]);
    }

    private static void vypisVysledok(Scenar s, Vysledok v) {
        System.out.println("POUZITE VYLEPSENIA");
        double[] x = v.mnozstva();
        for (int i = 0; i < x.length; i++) {
            if (x[i] == 0) continue;
            Vylepsenie p = s.getVylepsenia().get(i);
            System.out.println("\nNazov: " + p.getNazov());
            System.out.println("Mnozstvo: " + x[i] + " jednotiek (1 jednotka = " + p.getJednotka() + ")");
            System.out.printf("Cena: %.2f goldov%nPridane DMG: %.2f%nPridane HP: %.2f%n",
                    p.getCena()*x[i], p.getBonusDamage()*x[i], p.getBonusHp()*x[i]);
        }
        double[] t = v.sucty();
        System.out.printf("%nCELKOVE HODNOTY PRIPRAVY%nCelkova cena: %.2f goldov%nBonus DMG: %.2f%nBonus HP: %.2f%n",
                t[0],t[1],t[2]);
        System.out.printf("Dodatocna stamina: %.2f%nHmotnost: %.3f kg%nCas: %.2f min%nKatalyzator: %.2f ml%n",
                t[3],t[4],t[5],t[6]);
        System.out.printf("HP pred utokom: %.2f%n", s.hodnota("hpPostavy") + t[2]);
        System.out.println("\nKONTROLA OBMEDZENI");
        double[] hodnoty = v.hodnoty(), limity = v.limity();
        for (int j = 0; j < 6; j++) System.out.printf("%s: %.6f %s %.6f -> SPLNENE%n",
                Vysledok.KONTROLY[j], hodnoty[j], j < 2 ? ">=" : "<=", limity[j]);
        System.out.println("VYSLEDOK: Priprava je pripustna. Postava prezije a porazi nepriatela.");
    }

    private static double nacitajCislo(Scanner scanner, String otazka, double predvolenaHodnota, double minimum) {
        while (true) {
            System.out.print(otazka + " [" + predvolenaHodnota + "]: ");
            String vstup = riadok(scanner);
            if (vstup.isEmpty()) return predvolenaHodnota;
            try {
                double x = Double.parseDouble(vstup.replace(',', '.'));
                if (Double.isFinite(x) && x >= minimum) return x;
            } catch (NumberFormatException ignored) { }
            System.out.println("Zadaj konecne cislo >= " + minimum + ".");
        }
    }
    private static boolean nacitajAnoNie(Scanner scanner, String otazka) {
        while (true) {
            System.out.print(otazka); String s = riadok(scanner);
            if (s.equalsIgnoreCase("a") || s.equalsIgnoreCase("ano") || s.equalsIgnoreCase("áno")) return true;
            if (s.equalsIgnoreCase("n") || s.equalsIgnoreCase("nie")) return false;
            System.out.println("Zadaj a alebo n.");
        }
    }
    private static String riadok(Scanner scanner) { return scanner.nextLine().trim(); }
}
