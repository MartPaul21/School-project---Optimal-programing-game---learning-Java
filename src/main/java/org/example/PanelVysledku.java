package org.example;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Locale;

public class PanelVysledku extends JPanel {

    private static final Color POZADIE = new Color(40, 37, 35);
    private static final Color KARTA = new Color(25, 22, 20);
    private static final Color TEXT = new Color(230, 218, 192);
    private static final Color ZLATA = new Color(220, 175, 95);
    private static final Color ZELENA = new Color(135, 190, 115);
    private static final Color CERVENA = new Color(225, 110, 95);

    private static final Locale FORMAT =
            Locale.forLanguageTag("sk-SK");

    private static final double TOLERANCIA = 1e-6;

    private final JLabel cena = new JLabel("—");
    private final JLabel damage = new JLabel("—");
    private final JLabel hp = new JLabel("—");
    private final JLabel stav = new JLabel("Nevypočítané");

    private final JTextArea podrobnosti;
    private final JTabbedPane zalozky;

    private final JProgressBar stamina = vytvorPasik();
    private final JProgressBar hmotnost = vytvorPasik();
    private final JProgressBar cas = vytvorPasik();
    private final JProgressBar katalyzator = vytvorPasik();

    public PanelVysledku(JTextArea podrobnosti) {
        this.podrobnosti = podrobnosti;

        setLayout(new BorderLayout(10, 10));
        setBackground(POZADIE);
        setBorder(new EmptyBorder(12, 12, 12, 12));
        setMinimumSize(new Dimension(300, 300));

        // Nadpis a hlavné karty zostávajú stále viditeľné.
        JPanel hornaCast = new JPanel(new BorderLayout(0, 10));
        hornaCast.setOpaque(false);

        NadpisPanelu nadpis = new NadpisPanelu(
                "VÝSLEDOK PRÍPRAVY",
                NadpisPanelu.Styl.TMAVY
        );

        hornaCast.add(nadpis, BorderLayout.NORTH);

        JPanel karty = new JPanel(new GridLayout(1, 4, 10, 0));
        karty.setOpaque(false);

        karty.add(vytvorKartu("CENA PRÍPRAVY", cena, ZLATA));
        karty.add(vytvorKartu("VÝSLEDNÉ DMG", damage, TEXT));
        karty.add(vytvorKartu("HP PO ÚTOKU", hp, TEXT));
        karty.add(vytvorKartu("STAV", stav, TEXT));

        stav.setFont(new Font("SansSerif", Font.BOLD, 15));

        hornaCast.add(karty, BorderLayout.CENTER);
        add(hornaCast, BorderLayout.NORTH);

        // Prvá záložka: použité vylepšenia.
        podrobnosti.setEditable(false);
        podrobnosti.setBackground(POZADIE);
        podrobnosti.setForeground(TEXT);
        podrobnosti.setFont(new Font("SansSerif", Font.PLAIN, 14));
        podrobnosti.setMargin(new Insets(10, 8, 10, 8));
        podrobnosti.setLineWrap(true);
        podrobnosti.setWrapStyleWord(true);

        JScrollPane posuvanie = new JScrollPane(podrobnosti);
        TmavyStyl.upravPosuvanie(posuvanie);
        posuvanie.setBorder(null);

        // Druhá záložka: využitie zdrojov.
        JPanel zdroje = new JPanel(new GridLayout(2, 2, 16, 12));
        zdroje.setOpaque(false);
        zdroje.setBorder(new EmptyBorder(12, 8, 12, 8));

        zdroje.add(vytvorUkazovatel("STAMINA CELKOM", stamina));
        zdroje.add(vytvorUkazovatel("DODATOČNÁ HMOTNOSŤ", hmotnost));
        zdroje.add(vytvorUkazovatel("ČAS PRÍPRAVY", cas));
        zdroje.add(vytvorUkazovatel("KATALYZÁTOR", katalyzator));

        JPanel panelZdrojov = new JPanel(new BorderLayout());
        panelZdrojov.setBackground(POZADIE);
        panelZdrojov.add(zdroje, BorderLayout.NORTH);

        // Aj pri menšom okne budú zdroje dostupné posúvaním.
        JScrollPane posuvanieZdrojov = new JScrollPane(panelZdrojov);
        TmavyStyl.upravPosuvanie(posuvanieZdrojov);
        posuvanieZdrojov.setBorder(null);

        zalozky = vytvorZalozky();

        zalozky.addTab("Použité vylepšenia", posuvanie);
        zalozky.addTab("Využitie zdrojov", posuvanieZdrojov);

        zalozky.setToolTipTextAt(
                0,
                "Množstvá a ceny vylepšení vybraných optimalizátorom."
        );

        zalozky.setToolTipTextAt(
                1,
                "Spotreba zdrojov, ich limity a zostávajúce rezervy."
        );

        add(zalozky, BorderLayout.CENTER);

        vymaz("Nevypočítané");
    }

    private JTabbedPane vytvorZalozky() {
        JTabbedPane zalozky = new JTabbedPane();

        zalozky.setBackground(POZADIE);
        zalozky.setForeground(TEXT);
        zalozky.setFont(new Font("SansSerif", Font.BOLD, 13));
        zalozky.setOpaque(true);
        zalozky.setBorder(null);

        zalozky.setUI(
                new javax.swing.plaf.basic.BasicTabbedPaneUI() {

                    @Override
                    protected void installDefaults() {
                        super.installDefaults();

                        tabInsets = new Insets(8, 16, 8, 16);
                        selectedTabPadInsets = new Insets(0, 0, 0, 0);
                        tabAreaInsets = new Insets(0, 0, 0, 0);
                        contentBorderInsets = new Insets(1, 0, 0, 0);
                    }

                    @Override
                    protected void paintTabBackground(
                            Graphics g,
                            int tabPlacement,
                            int tabIndex,
                            int x,
                            int y,
                            int w,
                            int h,
                            boolean isSelected
                    ) {
                        Color farba;

                        if (isSelected) {
                            farba = new Color(91, 63, 39);
                        } else if (getRolloverTab() == tabIndex) {
                            farba = new Color(65, 49, 36);
                        } else {
                            farba = new Color(30, 26, 23);
                        }

                        g.setColor(farba);
                        g.fillRect(x, y, w, h);
                    }

                    @Override
                    protected void paintTabBorder(
                            Graphics g,
                            int tabPlacement,
                            int tabIndex,
                            int x,
                            int y,
                            int w,
                            int h,
                            boolean isSelected
                    ) {
                        g.setColor(
                                isSelected
                                        ? new Color(190, 150, 95)
                                        : new Color(115, 87, 58)
                        );

                        g.drawRect(x, y, w - 1, h - 1);
                    }

                    @Override
                    protected void paintContentBorder(
                            Graphics g,
                            int tabPlacement,
                            int selectedIndex
                    ) {
                        // Odstránime svetlý systémový rám obsahu.
                    }

                    @Override
                    protected void paintFocusIndicator(
                            Graphics g,
                            int tabPlacement,
                            Rectangle[] rects,
                            int tabIndex,
                            Rectangle iconRect,
                            Rectangle textRect,
                            boolean isSelected
                    ) {
                        if (tabPane.hasFocus() && isSelected) {
                            Rectangle r = rects[tabIndex];

                            g.setColor(ZLATA);
                            g.drawRect(
                                    r.x + 3,
                                    r.y + 3,
                                    r.width - 7,
                                    r.height - 7
                            );
                        }
                    }
                }
        );

        return zalozky;
    }
    private JPanel vytvorKartu(
            String nazov,
            JLabel hodnota,
            Color farba
    ) {
        JPanel karta = new JPanel(new BorderLayout(0, 8));
        karta.setBackground(KARTA);

        karta.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(115, 87, 58)
                        ),
                        new EmptyBorder(10, 10, 10, 10)
                )
        );

        JLabel titulok = new JLabel(nazov);
        titulok.setForeground(TEXT);
        titulok.setFont(new Font("SansSerif", Font.BOLD, 11));

        hodnota.setForeground(farba);
        hodnota.setFont(new Font("Serif", Font.BOLD, 24));

        karta.add(titulok, BorderLayout.NORTH);
        karta.add(hodnota, BorderLayout.CENTER);

        return karta;
    }

    private JProgressBar vytvorPasik() {
        JProgressBar pasik = new JProgressBar(0, 1000);

        // BasicProgressBarUI umožní použiť naše farby
        // aj pri systémovom vzhľade Windows.
        pasik.setUI(new javax.swing.plaf.basic.BasicProgressBarUI() {
            @Override
            protected Color getSelectionForeground() {
                return KARTA;
            }

            @Override
            protected Color getSelectionBackground() {
                return TEXT;
            }
        });

        pasik.setBackground(KARTA);
        pasik.setForeground(ZELENA);
        pasik.setStringPainted(true);
        pasik.setString("—");
        pasik.setFont(new Font("SansSerif", Font.BOLD, 12));
        pasik.setPreferredSize(new Dimension(150, 24));

        pasik.setBorder(
                BorderFactory.createLineBorder(
                        new Color(115, 87, 58)
                )
        );

        return pasik;
    }

    private JPanel vytvorUkazovatel(
            String nazov,
            JProgressBar pasik
    ) {
        JPanel panel = new JPanel(new BorderLayout(0, 4));
        panel.setOpaque(false);

        JLabel popis = new JLabel(nazov);
        popis.setForeground(TEXT);
        popis.setFont(new Font("SansSerif", Font.BOLD, 11));

        String kluc = switch (nazov) {
            case "STAMINA CELKOM" -> "stamina";
            case "DODATOČNÁ HMOTNOSŤ" -> "maximalnaHmotnost";
            case "ČAS PRÍPRAVY" -> "maximalnyCas";
            case "KATALYZÁTOR" -> "katalyzator";
            default -> "";
        };

        HernaIkona.pridaj(popis, kluc);

        panel.add(popis, BorderLayout.NORTH);
        panel.add(pasik, BorderLayout.CENTER);

        return panel;
    }

    private void nastavPasik(
            JProgressBar pasik,
            double spotreba,
            double limit,
            String jednotka,
            int desatinneMiesta
    ) {
        boolean prekroceny = spotreba > limit + TOLERANCIA;

        double pomer;

        if (limit > 0) {
            pomer = spotreba / limit;
        } else {
            // Nulový limit: pri nulovej spotrebe je pásik prázdny.
            pomer = spotreba > TOLERANCIA ? 1.0 : 0.0;
        }

        // Grafická výplň je najviac 100 %.
        // Text stále ukazuje skutočnú spotrebu.
        double vypln = Math.max(0.0, Math.min(1.0, pomer));
        pasik.setValue((int) Math.round(vypln * 1000));

        if (prekroceny) {
            pasik.setForeground(CERVENA);
        } else if (limit > 0 && pomer >= 0.9) {
            pasik.setForeground(ZLATA);
        } else {
            pasik.setForeground(ZELENA);
        }

        String format = "%." + desatinneMiesta
                + "f / %." + desatinneMiesta + "f";

        String text = String.format(FORMAT, format, spotreba, limit);

        if (!jednotka.isBlank()) {
            text += " " + jednotka;
        }

        pasik.setString(text);

        double rezerva = limit - spotreba;

        if (prekroceny) {
            pasik.setToolTipText(
                    "Limit prekročený o "
                            + cislo(-rezerva, desatinneMiesta)
                            + " " + jednotka
            );
        } else {
            String vyuzitie = limit > 0
                    ? cislo(pomer * 100, 2) + " %"
                    : "nulový limit";

            pasik.setToolTipText(
                    "Využitie: " + vyuzitie
                            + " | Rezerva: "
                            + cislo(Math.max(0, rezerva), desatinneMiesta)
                            + " " + jednotka
            );
        }
    }

    private String cislo(double hodnota, int desatinneMiesta) {
        return String.format(
                FORMAT,
                "%." + desatinneMiesta + "f",
                hodnota
        );
    }

    public void vymaz(String sprava) {
        cena.setText("—");
        damage.setText("—");
        hp.setText("—");

        cena.setToolTipText(null);
        damage.setToolTipText(null);
        hp.setToolTipText(null);

        stav.setText(sprava);
        stav.setForeground(TEXT);
        stav.setToolTipText(sprava);

        for (JProgressBar pasik : new JProgressBar[]{
                stamina, hmotnost, cas, katalyzator
        }) {
            pasik.setValue(0);
            pasik.setString("—");
            pasik.setForeground(ZELENA);
            pasik.setToolTipText(null);
        }
    }

    public void oznacChybu(String sprava) {
        vymaz(sprava);
        stav.setForeground(CERVENA);

        zalozky.setSelectedIndex(0);
    }

    public void zobraz(Scenar scenar, Vysledok vysledok) {
        double[] sucty = vysledok.sucty();
        double[] hodnoty = vysledok.hodnoty();
        double[] limity = vysledok.limity();
        double[] mnozstva = vysledok.mnozstva();

        cena.setText(cislo(sucty[0], 2) + " golda");
        damage.setText(cislo(hodnoty[0], 2));
        hp.setText(cislo(hodnoty[1], 2));

        cena.setToolTipText(
                "Celková cena: " + cislo(sucty[0], 2) + " golda"
        );

        damage.setToolTipText(
                "Výsledné DMG: " + cislo(hodnoty[0], 2)
        );

        hp.setToolTipText(
                "HP po útoku: " + cislo(hodnoty[1], 2)
        );

        stav.setText("Splnené");
        stav.setForeground(ZELENA);
        stav.setToolTipText("Výsledok spĺňa kontrolované obmedzenia.");

        // hodnoty[2] už zahŕňajú aj základnú spotrebu staminy.
        nastavPasik(stamina, hodnoty[2], limity[2], "", 2);
        nastavPasik(hmotnost, hodnoty[3], limity[3], "kg", 2);
        nastavPasik(cas, hodnoty[4], limity[4], "min", 2);
        nastavPasik(katalyzator, hodnoty[5], limity[5], "ml", 2);

        StringBuilder text = new StringBuilder(
                "POUŽITÉ VYLEPŠENIA\n\n"
                        + "Hodnoty sú zobrazené na dve desatinné miesta.\n"
                        + "Výpočet používa nezaokrúhlené množstvá; "
                        + "presné hodnoty nájdeš v matematickom okne.\n\n"
        );
        boolean niecoPouzite = false;

        for (int i = 0; i < mnozstva.length; i++) {
            if (mnozstva[i] == 0.0) {
                continue;
            }

            niecoPouzite = true;

            Vylepsenie vylepsenie = scenar.getVylepsenia().get(i);

            text.append(vylepsenie.getNazov()).append("\n");
            text.append("Množstvo: ")
                    .append(cislo(mnozstva[i], 2))
                    .append(" jednotiek; 1 jednotka = ")
                    .append(vylepsenie.getJednotka())
                    .append("\n");

            text.append("Cena: ")
                    .append(cislo(
                            mnozstva[i] * vylepsenie.getCena(),
                            2
                    ))
                    .append(" golda\n\n");
        }

        if (!niecoPouzite) {
            text.append("Postava nepotrebuje žiadne vylepšenia.\n\n");
        }

        text.append(
                "Podrobné rovnice a kontroly nájdeš "
                        + "v okne „Matematický model a kontrola“."
        );

        podrobnosti.setText(text.toString());
        podrobnosti.setCaretPosition(0);
    }
}