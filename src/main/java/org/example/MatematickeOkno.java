package org.example;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

public class MatematickeOkno extends JDialog {

    public MatematickeOkno(
            JFrame rodic,
            Scenar scenar,
            Vysledok vysledok
    ) {
        super(rodic, "Matematický model a kontrola", true);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1150, 800);
        setMinimumSize(new Dimension(800, 550));

        Color pozadie = new Color(25, 22, 20);
        Color panel = new Color(40, 37, 35);
        Color text = new Color(230, 218, 192);

        JPanel obsah = new JPanel(new BorderLayout(12, 12));
        obsah.setBackground(pozadie);
        obsah.setBorder(
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        );
        setContentPane(obsah);

        JPanel hlavicka = new JPanel(new BorderLayout(0, 8));
        hlavicka.setOpaque(false);

        hlavicka.add(
                new NadpisPanelu(
                        "MATEMATICKÝ MODEL A KONTROLA",
                        NadpisPanelu.Styl.PERGAMEN
                ),
                BorderLayout.NORTH
        );

        JLabel informacia = new JLabel(
                "Posledný úspešný výpočet • iba na čítanie "
                        + "• bez zaokrúhlenia na dve desatinné miesta"
        );
        informacia.setForeground(text);
        informacia.setFont(new Font("SansSerif", Font.PLAIN, 13));

        hlavicka.add(informacia, BorderLayout.SOUTH);
        obsah.add(hlavicka, BorderLayout.NORTH);

        // Pôvodná metóda vytvorí rovnaký zápis ako predtým.
        String celyZapis = vytvorZapis(scenar, vysledok);

        String[] znacky = {
                "1. ROZHODOVACIE PREMENNÉ",
                "2. ÚČELOVÁ FUNKCIA",
                "3. OHRANIČUJÚCE PODMIENKY",
                "4. VÝSLEDOK OPTIMALIZÁCIE",
                "5. KONTROLA DOSADENÍM",
                "POZNÁMKY"
        };

        String[] nazvy = {
                "1. Premenné",
                "2. Účelová funkcia",
                "3. Obmedzenia",
                "4. Výsledok",
                "5. Kontrola dosadením",
                "6. Poznámky",
                "Celý zápis"
        };

        CardLayout rozlozenie = new CardLayout();
        JPanel strany = new JPanel(rozlozenie);
        strany.setBackground(panel);

        // Rozdelíme hotový text; nič znovu neprepočítavame.
        int[] zaciatky = new int[znacky.length];
        int hladatOd = 0;

        for (int i = 0; i < znacky.length; i++) {
            zaciatky[i] = celyZapis.indexOf(znacky[i], hladatOd);

            if (zaciatky[i] < 0) {
                throw new IllegalStateException(
                        "V matematickom zápise chýba časť: " + znacky[i]
                );
            }

            hladatOd = zaciatky[i] + znacky[i].length();
        }

        for (int i = 0; i < znacky.length; i++) {
            int koniec = i + 1 < znacky.length
                    ? zaciatky[i + 1]
                    : celyZapis.length();

            String cast = celyZapis
                    .substring(zaciatky[i], koniec)
                    .strip();

            strany.add(
                    vytvorTextovuStranu(cast),
                    Integer.toString(i)
            );
        }

        strany.add(vytvorTextovuStranu(celyZapis), "6");

        JList<String> navigacia = new JList<>(nazvy);
        navigacia.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );
        navigacia.setBackground(panel);
        navigacia.setForeground(text);
        navigacia.setSelectionBackground(new Color(91, 63, 39));
        navigacia.setSelectionForeground(new Color(245, 229, 193));
        navigacia.setFont(new Font("SansSerif", Font.BOLD, 14));
        navigacia.setFixedCellHeight(44);
        navigacia.setBorder(
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        );

        navigacia.addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()
                    && navigacia.getSelectedIndex() >= 0) {

                rozlozenie.show(
                        strany,
                        Integer.toString(navigacia.getSelectedIndex())
                );
            }
        });

        navigacia.setSelectedIndex(0);

        JScrollPane posuvanieNavigacie = new JScrollPane(navigacia);
        TmavyStyl.upravPosuvanie(posuvanieNavigacie);
        posuvanieNavigacie.setPreferredSize(new Dimension(225, 0));

        obsah.add(posuvanieNavigacie, BorderLayout.WEST);
        obsah.add(strany, BorderLayout.CENTER);

        JButton zavriet = new JButton("Zavrieť");
        HernyStylTlacidiel.uprav(zavriet, false);
        zavriet.addActionListener(event -> dispose());

        JPanel spodok = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        spodok.setOpaque(false);
        spodok.add(zavriet);
        obsah.add(spodok, BorderLayout.SOUTH);

        getRootPane().registerKeyboardAction(
                event -> dispose(),
                KeyStroke.getKeyStroke("ESCAPE"),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        setLocationRelativeTo(rodic);
    }

    private JScrollPane vytvorTextovuStranu(String obsah) {
        JTextArea text = new JTextArea(obsah);

        text.setEditable(false);
        text.setLineWrap(true);
        text.setWrapStyleWord(true);
        text.setFont(new Font("Monospaced", Font.PLAIN, 15));
        text.setBackground(new Color(35, 32, 30));
        text.setForeground(new Color(230, 218, 192));
        text.setCaretColor(new Color(230, 218, 192));
        text.setSelectionColor(new Color(110, 65, 40));
        text.setSelectedTextColor(Color.WHITE);
        text.setMargin(new Insets(20, 20, 20, 20));
        zvyrazniStavy(text);

        JScrollPane posuvanie = new JScrollPane(text);
        TmavyStyl.upravPosuvanie(posuvanie);

        return posuvanie;
    }

    private static String vytvorZapis(Scenar s, Vysledok vysledok) {
        List<Vylepsenie> polozky = s.getVylepsenia();
        double[] x = vysledok.mnozstva();
        int pocet = polozky.size();

        double[] ceny = new double[pocet];

        // Riadky: DMG, HP, stamina, hmotnosť, čas, katalyzátor.
        double[][] koeficienty = new double[6][pocet];

        for (int i = 0; i < pocet; i++) {
            Vylepsenie v = polozky.get(i);

            ceny[i] = v.getCena();
            koeficienty[0][i] = v.getBonusDamage();
            koeficienty[1][i] = v.getBonusHp();
            koeficienty[2][i] = v.getSpotrebaStaminy();
            koeficienty[3][i] = v.getHmotnost();
            koeficienty[4][i] = v.getCasPripravy();
            koeficienty[5][i] = v.getSpotrebaKatalyzatora();
        }

        double[] praveStrany = {
                s.hodnota("hpNepriatela")
                        - s.hodnota("damagePostavy"),

                s.hodnota("damageNepriatela")
                        + s.hodnota("minimalneHpPoUtoku")
                        - s.hodnota("hpPostavy"),

                s.hodnota("stamina")
                        - s.hodnota("zakladnaSpotrebaStaminy"),

                s.hodnota("maximalnaHmotnost"),
                s.hodnota("maximalnyCas"),
                s.hodnota("katalyzator")
        };

        String[] nazvy = {
                "Dostatočný bonus DMG",
                "Dostatočný bonus HP na prežitie",
                "Dodatočná spotreba staminy",
                "Dodatočná hmotnosť (kg)",
                "Čas prípravy (min)",
                "Spotreba katalyzátora (ml)"
        };

        StringBuilder zapis = new StringBuilder();

        zapis.append("MATEMATICKÝ MODEL A KONTROLA\n");
        zapis.append("Údaje z posledného úspešného výpočtu.\n\n");

        zapis.append("1. ROZHODOVACIE PREMENNÉ\n\n");

        for (int i = 0; i < pocet; i++) {
            Vylepsenie v = polozky.get(i);

            zapis.append("x").append(i + 1)
                    .append(" — ").append(v.getNazov())
                    .append("; jedna jednotka = ")
                    .append(v.getJednotka())
                    .append("\n");
        }

        zapis.append("\nPremenné sú spojité, nie celočíselné.\n");

        zapis.append("\n2. ÚČELOVÁ FUNKCIA\n\n");
        zapis.append("Minimalizácia celkovej ceny v goldoch:\n\n");
        zapis.append("min z = ").append(vyraz(ceny, null)).append("\n");

        zapis.append("\n3. OHRANIČUJÚCE PODMIENKY\n\n");

        for (int j = 0; j < koeficienty.length; j++) {
            String vztah = j < 2 ? " >= " : " <= ";

            zapis.append(j + 1).append(") ")
                    .append(nazvy[j]).append("\n")
                    .append(vyraz(koeficienty[j], null))
                    .append(vztah)
                    .append(cislo(praveStrany[j]))
                    .append("\n\n");
        }

        zapis.append("Horné hranice a nezápornosť:\n\n");

        for (int i = 0; i < pocet; i++) {
            zapis.append("0 <= x").append(i + 1)
                    .append(" <= ")
                    .append(cislo(polozky.get(i).getMaximalneMnozstvo()))
                    .append("\n");
        }

        zapis.append("\n4. VÝSLEDOK OPTIMALIZÁCIE\n\n");

        for (int i = 0; i < pocet; i++) {
            zapis.append("x").append(i + 1)
                    .append(" = ").append(cislo(x[i]))
                    .append("\n");
        }

        zapis.append("\nz* = ")
                .append(cislo(vysledok.sucty()[0]))
                .append(" golda\n");

        zapis.append("\n5. KONTROLA DOSADENÍM\n\n");

        zapis.append("Cena:\n")
                .append(vyraz(ceny, x))
                .append("\n= ")
                .append(cislo(sucinASucet(ceny, x)))
                .append(" golda\n\n");

        double tolerancia = 0.000001;

        for (int j = 0; j < koeficienty.length; j++) {
            double lavaStrana = sucinASucet(koeficienty[j], x);
            double pravaStrana = praveStrany[j];

            boolean splnene = j < 2
                    ? lavaStrana + tolerancia >= pravaStrana
                    : lavaStrana <= pravaStrana + tolerancia;

            double rezerva = j < 2
                    ? lavaStrana - pravaStrana
                    : pravaStrana - lavaStrana;
            String stavObmedzenia;

            if (!splnene) {
                stavObmedzenia = "[NESPLNENÉ]";
            } else if (Math.abs(rezerva) <= tolerancia) {
                stavObmedzenia = "[AKTÍVNE]";
            } else {
                stavObmedzenia = "[S REZERVOU]";
            }

            String vztah = j < 2 ? " >= " : " <= ";

            zapis.append(nazvy[j]).append(":\n")
                    .append(vyraz(koeficienty[j], x))
                    .append("\n= ").append(cislo(lavaStrana))
                    .append(vztah).append(cislo(pravaStrana))
                    .append(" → ")
                    .append(stavObmedzenia)
                    .append("\nRezerva / prebytok: ")
                    .append(cislo(rezerva))
                    .append("\n\n");
        }

        zapis.append("Kontrola hraníc premenných:\n\n");

        for (int i = 0; i < pocet; i++) {
            double maximum = polozky.get(i).getMaximalneMnozstvo();

            boolean splnene = x[i] >= -tolerancia
                    && x[i] <= maximum + tolerancia;

            boolean dolnaAktivna = Math.abs(x[i]) <= tolerancia;
            boolean hornaAktivna =
                    Math.abs(maximum - x[i]) <= tolerancia;

            String stavHranic;

            if (!splnene) {
                stavHranic = "[NESPLNENÉ]";
            } else if (dolnaAktivna && hornaAktivna) {
                stavHranic = "[AKTÍVNE] dolná aj horná hranica";
            } else if (dolnaAktivna) {
                stavHranic = "[AKTÍVNE] dolná hranica";
            } else if (hornaAktivna) {
                stavHranic = "[AKTÍVNE] horná hranica";
            } else {
                stavHranic = "[S REZERVOU] medzi hranicami";
            }

            zapis.append("x").append(i + 1)
                    .append(": 0 <= ").append(cislo(x[i]))
                    .append(" <= ").append(cislo(maximum))
                    .append(" → ")
                    .append(stavHranic)
                    .append("\n");
        }

        zapis.append("\nPOZNÁMKY\n\n");
        zapis.append(
                "[AKTÍVNE] — podmienka je splnená na hranici "
                        + "v rámci tolerancie.\n"
                        + "[S REZERVOU] — podmienka má kladnú rezervu "
                        + "alebo prebytok väčší než tolerancia.\n"
                        + "[NESPLNENÉ] — podmienka je porušená "
                        + "nad rámec tolerancie.\n\n"
                        + "Aktívne obmedzenie nemusí mať nenulovú "
                        + "tieňovú cenu. Toto označenie nie je "
                        + "výpočtom citlivosti.\n\n"
        );
        zapis.append(
                "Pri porovnávaní používame toleranciu 0,000001 "
                        + "kvôli desatinným výpočtom typu double.\n\n"
        );
        zapis.append(
                "Pri >= je prebytok ľavá strana mínus pravá strana.\n"
                        + "Pri <= je rezerva pravá strana mínus ľavá strana.\n"
                        + "Veľmi malá záporná hodnota v rámci tolerancie "
                        + "môže vzniknúť numerickou nepresnosťou.\n\n"
        );
        zapis.append(
                "Kontrola dosadením overuje prípustnosť riešenia. "
                        + "Optimálnosť vyplýva z úspešného dokončenia "
                        + "simplexového riešiteľa; samotné dosadenie "
                        + "nie je samostatným dôkazom optimálnosti.\n\n"
        );
        zapis.append("Rezervy obmedzení nie sú tieňové ceny.\n");

        return zapis.toString();
    }

    // Bez množstiev zostaví napr. 12*x1 + 9*x2.
    // S množstvami zostaví napr. 12*(5) + 9*(2,222...).
    private static String vyraz(double[] koeficienty, double[] mnozstva) {
        StringBuilder text = new StringBuilder();

        for (int i = 0; i < koeficienty.length; i++) {
            if (koeficienty[i] == 0.0) {
                continue;
            }

            if (text.length() > 0) {
                text.append(" + ");
            }

            text.append(cislo(koeficienty[i])).append("*");

            if (mnozstva == null) {
                text.append("x").append(i + 1);
            } else {
                text.append("(")
                        .append(cislo(mnozstva[i]))
                        .append(")");
            }
        }

        return text.length() == 0 ? "0" : text.toString();
    }

    private static double sucinASucet(double[] koeficienty, double[] x) {
        double sucet = 0.0;

        for (int i = 0; i < koeficienty.length; i++) {
            sucet += koeficienty[i] * x[i];
        }

        return sucet;
    }

    private static String cislo(double hodnota) {
        // Odstráni nepotrebné koncové nuly, ale nezaokrúhľuje výpočet.
        return BigDecimal.valueOf(hodnota)
                .stripTrailingZeros()
                .toPlainString()
                .replace('.', ',');
    }

    private void zvyrazniStavy(JTextArea text) {
        String obsah = text.getText();

        String[] znacky = {
                "[AKTÍVNE]",
                "[S REZERVOU]",
                "[NESPLNENÉ]"
        };

        Color[] farby = {
                new Color(108, 78, 27),
                new Color(40, 78, 45),
                new Color(120, 37, 32)
        };

        for (int i = 0; i < znacky.length; i++) {
            int hladatOd = 0;
            int pozicia;

            while ((pozicia = obsah.indexOf(
                    znacky[i], hladatOd
            )) >= 0) {
                int koniec = pozicia + znacky[i].length();

                try {
                    text.getHighlighter().addHighlight(
                            pozicia,
                            koniec,
                            new javax.swing.text.DefaultHighlighter
                                    .DefaultHighlightPainter(farby[i])
                    );
                } catch (javax.swing.text.BadLocationException e) {
                    throw new IllegalStateException(
                            "Nepodarilo sa zvýrazniť stav obmedzenia.",
                            e
                    );
                }

                hladatOd = koniec;
            }
        }
    }
}