package org.example;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.nio.file.Path;
import java.util.Locale;

import org.apache.commons.math3.optim.linear.NoFeasibleSolutionException;

public class HerneOkno extends JFrame {

    // Farby nášho prvého tmavého rozhrania.
    private static final Color POZADIE = new Color(25, 24, 24);
    private static final Color PANEL = new Color(40, 37, 35);
    private static final Color TEXT = new Color(230, 218, 192);
    private static final Color CERVENA = new Color(125, 35, 35);

    // Ku každému parametru si uložíme jeho vstupné políčko.
    private final Map<String, JTextField> polia = new LinkedHashMap<>();

    private final JTextArea vysledok = new JTextArea();

    private final JButton vypocitat =
            new JButton("Vypočítať optimum");

    private final JButton obnovitScenar =
            new JButton("Obnoviť pôvodný scenár");

    private final JButton matematickyModel =
            new JButton("Matematický model a kontrola");

    private Scenar poslednyScenar;
    private Vysledok poslednyVysledok;
    private TabulkaVylepseni tabulkaVylepseni;
    private final JButton importExcel =
            new JButton("Importovať Excel");

    private final JButton exportExcel =
            new JButton("Exportovať scenár");

    private final JButton vytvoritSablonu =
            new JButton("Vytvoriť šablónu");

    // Uchováme aj tento parameter z importu.
    // V hlavnom okne naň zatiaľ nemáme samostatné políčko.
    private double minimalneHpPoUtoku = 1.0;

    private PanelVysledku panelVysledku;

    public HerneOkno() {
        Napoveda.nastavFarbyDialogov();
        setTitle("Príprava na súboj — optimalizácia výbavy");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 850);
        setMinimumSize(new Dimension(850, 650));

        PozadiePanel obsah = new PozadiePanel(
                new BorderLayout(15, 15)
        );

        obsah.setBorder(new EmptyBorder(20, 20, 20, 20));
        setContentPane(obsah);

        // NADPIS
        JLabel nadpis = new JLabel(
                "PRÍPRAVA NA SÚBOJ",
                SwingConstants.CENTER
        );

        nadpis.setToolTipText(
                "F11: zapnúť alebo vypnúť celú obrazovku. "
                        + "ESC: návrat z celej obrazovky."
        );
        nadpis.setFont(new Font("Serif", Font.BOLD, 30));
        nadpis.setForeground(TEXT);
        JPanel excelTlacidla = new JPanel(
                new FlowLayout(FlowLayout.CENTER, 12, 5)
        );
        excelTlacidla.setOpaque(false);

        JButton[] tlacidlaExcelu = {
                importExcel,
                exportExcel,
                vytvoritSablonu,
                obnovitScenar
        };

        for (JButton tlacidlo : tlacidlaExcelu) {
            tlacidlo.setBackground(PANEL);
            tlacidlo.setForeground(TEXT);
            tlacidlo.setFocusPainted(false);
            tlacidlo.setFont(
                    new Font("SansSerif", Font.BOLD, 14)
            );

            excelTlacidla.add(tlacidlo);
        }

        importExcel.addActionListener(event -> importujExcel());
        exportExcel.addActionListener(event -> exportujExcel(false));
        vytvoritSablonu.addActionListener(event -> exportujExcel(true));
        obnovitScenar.addActionListener(event -> obnovPovodnyScenar());

        JPanel hlavicka = new JPanel(new BorderLayout(0, 10));
        hlavicka.setOpaque(false);
        hlavicka.add(nadpis, BorderLayout.NORTH);
        hlavicka.add(excelTlacidla, BorderLayout.SOUTH);

        obsah.add(hlavicka, BorderLayout.NORTH);

        // VSTUPY A VÝSLEDOK
        Scenar predvoleny = Scenar.predvoleny();

        // ĽAVÝ PANEL — HRDINA
        JPanel udajePostavy = vytvorPanel(
                "POSTAVA",
                predvoleny,
                new String[]{
                        "hpPostavy",
                        "damagePostavy",
                        "stamina",
                        "zakladnaSpotrebaStaminy"
                },
                new String[]{
                        "Aktuálne HP",
                        "Základné DMG",
                        "Dostupná stamina",
                        "Spotreba základnej kombinácie"
                }
        );

        JPanel hrdinaPanel = vytvorPanelPostavy(
                "POSTAVA",
                "/obrazky/hrdina.png",
                udajePostavy
        );

        obsah.add(hrdinaPanel, BorderLayout.WEST);

        // PRAVÝ PANEL — NEPRIATEĽ
        JPanel udajeNepriatela = vytvorPanel(
                "NEPRIATEĽ",
                predvoleny,
                new String[]{
                        "hpNepriatela",
                        "damageNepriatela"
                },
                new String[]{
                        "HP nepriateľa",
                        "DMG nepriateľa"
                }
        );

        JLabel pravidlo = new JLabel("Nepriateľ útočí prvý.");
        pravidlo.setForeground(TEXT);
        pravidlo.setAlignmentX(Component.LEFT_ALIGNMENT);
        udajeNepriatela.add(pravidlo);

        JPanel nepriatelPanel = vytvorPanelPostavy(
                "NEPRIATEĽ",
                "/obrazky/nepriatel.png",
                udajeNepriatela
        );

        obsah.add(nepriatelPanel, BorderLayout.EAST);

        // STRED — LIMITY, TABUĽKA A VÝSLEDOK
        JPanel stred = new JPanel(new BorderLayout(10, 10));
        stred.setOpaque(false);

        JPanel limity = vytvorPanelLimitov(predvoleny);


        stred.add(limity, BorderLayout.NORTH);

        // Výsledok zostáva iba na čítanie.
        vysledok.setEditable(false);
        vysledok.setFont(
                new Font("Monospaced", Font.PLAIN, 14)
        );
        vysledok.setBackground(PANEL);
        vysledok.setForeground(TEXT);
        vysledok.setMargin(new Insets(15, 15, 15, 15));

        vysledok.setText(
                "Uprav vstupy a klikni na „Vypočítať optimum“.\n\n"
                        + "Cieľ: minimalizovať cenu prípravy.\n"
                        + "Vylepšenia upravíš dvojklikom v tabuľke."
        );

        tabulkaVylepseni = new TabulkaVylepseni(
                predvoleny.getVylepsenia(),
                this::zneplatniVysledok
        );

        // Tabuľka a výsledok majú posuvnú deliacu čiaru.
        panelVysledku = new PanelVysledku(vysledok);

        JSplitPane rozdelenie = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                tabulkaVylepseni,
                panelVysledku
        );

        TmavyStyl.upravRozdelenie(rozdelenie);

        rozdelenie.setResizeWeight(0.6);
        rozdelenie.setDividerLocation(250);

        tabulkaVylepseni.setMinimumSize(
                new Dimension(250, 150)
        );

        stred.add(rozdelenie, BorderLayout.CENTER);
        obsah.add(stred, BorderLayout.CENTER);

        // TLAČIDLO
        vypocitat.setFont(HernePismo.nadpis(22f));
        vypocitat.setBackground(CERVENA);
        vypocitat.setForeground(TEXT);
        vypocitat.setFocusPainted(false);
        vypocitat.setPreferredSize(new Dimension(300, 55));
        vypocitat.addActionListener(event -> spustiVypocet());

        matematickyModel.setEnabled(false);
        matematickyModel.setFont(
                new Font("SansSerif", Font.BOLD, 15)
        );
        matematickyModel.setBackground(PANEL);
        matematickyModel.setForeground(TEXT);
        matematickyModel.setFocusPainted(false);

        matematickyModel.addActionListener(event -> {
            if (poslednyScenar != null && poslednyVysledok != null) {
                MatematickeOkno okno = new MatematickeOkno(
                        this,
                        poslednyScenar,
                        poslednyVysledok
                );

                okno.setVisible(true);
            }
        });

// Dve existujúce tlačidlá.
        JPanel tlacidla = new JPanel(new GridLayout(1, 2, 12, 0));
        tlacidla.setOpaque(false);
        tlacidla.add(vypocitat);
        tlacidla.add(matematickyModel);

// Odstup zhora a zdola zachová primeranú výšku tlačidiel.
        JPanel stredSpodku = new JPanel(new BorderLayout());
        stredSpodku.setOpaque(false);
        stredSpodku.setBorder(
                BorderFactory.createEmptyBorder(10, 0, 10, 0)
        );
        stredSpodku.add(tlacidla, BorderLayout.CENTER);

// Dekorácie majú vlastný priestor vedľa tlačidiel.
        JPanel spodnyPanel = new JPanel(new BorderLayout(10, 0));
        spodnyPanel.setOpaque(false);

        spodnyPanel.add(
                new DekoraciaPanel(false),
                BorderLayout.WEST
        );

        spodnyPanel.add(
                stredSpodku,
                BorderLayout.CENTER
        );

        spodnyPanel.add(
                new DekoraciaPanel(true),
                BorderLayout.EAST
        );

        obsah.add(spodnyPanel, BorderLayout.SOUTH);

        // Každá zmena vstupu zneplatní predchádzajúci výsledok.
        DocumentListener sledovanieZmien = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                zneplatniVysledok();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                zneplatniVysledok();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                zneplatniVysledok();
            }
        };

        for (JTextField pole : polia.values()) {
            pole.getDocument().addDocumentListener(sledovanieZmien);
        }

        // Otvorenie okna v strede obrazovky.

        HernyStylTlacidiel.uprav(vypocitat, true);

        HernyStylTlacidiel.uprav(matematickyModel, false);
        HernyStylTlacidiel.uprav(importExcel, false);
        HernyStylTlacidiel.uprav(exportExcel, false);
        HernyStylTlacidiel.uprav(vytvoritSablonu, false);
        HernyStylTlacidiel.uprav(obnovitScenar, false);
        importExcel.setToolTipText(
                "Načíta vstupy z Excelu. Výsledok potom vypočítaj znovu."
        );

        exportExcel.setToolTipText(
                "Uloží aktuálne vstupy a platný výsledok, ak je k dispozícii."
        );

        vytvoritSablonu.setToolTipText(
                "Vytvorí Excel s pôvodnými údajmi z kódu, nie s aktuálnymi úpravami."
        );

        obnovitScenar.setToolTipText(
                "Po potvrdení obnoví pôvodné vstupy a vylepšenia."
        );

        matematickyModel.setToolTipText(
                "Zobrazí model a kontrolu posledného úspešného výpočtu."
        );

        setLocationRelativeTo(null);
        RezimObrazovky.zapoj(this);
    }

    private JPanel vytvorPanel(
            String nadpis,
            Scenar scenar,
            String[] kluce,
            String[] popisy
    ) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(PANEL);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));



        for (int i = 0; i < kluce.length; i++) {
            JLabel popis = new JLabel(popisy[i]);
            popis.setForeground(TEXT);
            popis.setAlignmentX(Component.LEFT_ALIGNMENT);

            HernaIkona.pridaj(popis, kluce[i]);
            popis.setToolTipText(Napoveda.parameter(kluce[i]));

            JTextField pole = new JTextField(
                    Double.toString(scenar.hodnota(kluce[i]))
            );
            upravVzhledPola(pole);
            pole.setToolTipText(Napoveda.parameter(kluce[i]));

            pole.setFont(new Font("SansSerif", Font.PLAIN, 16));
            pole.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
            pole.setAlignmentX(Component.LEFT_ALIGNMENT);

            polia.put(kluce[i], pole);

            panel.add(popis);
            panel.add(Box.createVerticalStrut(4));
            panel.add(pole);
            panel.add(Box.createVerticalStrut(10));
        }

        return panel;
    }

    private Scenar nacitajScenar() {
        Scenar scenar = Scenar.predvoleny();
        scenar.nastav(
                "minimalneHpPoUtoku",
                minimalneHpPoUtoku
        );
        for (Map.Entry<String, JTextField> zaznam : polia.entrySet()) {
            String kluc = zaznam.getKey();

            String vstup = zaznam.getValue()
                    .getText()
                    .trim()
                    .replace(',', '.');

            double hodnota;

            try {
                hodnota = Double.parseDouble(vstup);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                        "Parameter " + kluc + ": zadaj platné číslo."
                );
            }

            // Scenar už kontroluje nezápornosť a ostatné hranice.
            scenar.nastav(kluc, hodnota);
        }

        // Predvolené položky nahradíme aktuálnym obsahom tabuľky.
        // Najprv načítame a skontrolujeme celý zoznam.
        java.util.List<Vylepsenie> upravene =
                tabulkaVylepseni.nacitajVylepsenia();

        scenar.getVylepsenia().clear();
        scenar.getVylepsenia().addAll(upravene);

        scenar.over();
        return scenar;
    }

    private void spustiVypocet() {
        final Scenar scenar;

        try {
            scenar = nacitajScenar();
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(
                    this,
                    e.getMessage(),
                    "Neplatný vstup",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        zneplatniVysledok();
        nastavVstupy(false);

        panelVysledku.vymaz("Počítam…");
        vysledok.setText("Prebieha výpočet...");

        // Výpočet beží mimo vlákna, ktoré obsluhuje okno.
        // Rozhranie preto počas výpočtu nezamrzne.
        SwingWorker<Vysledok, Void> uloha =
                new SwingWorker<>() {

                    @Override
                    protected Vysledok doInBackground() {
                        double[] mnozstva =
                                Optimalizator.vypocitaj(scenar);

                        return new Vysledok(scenar, mnozstva);
                    }

                    @Override
                    protected void done() {
                        try {
                            zobrazVysledok(scenar, get());

                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            panelVysledku.oznacChybu("Prerušené");
                            vysledok.setText("Čakanie na výsledok bolo prerušené.");

                        } catch (ExecutionException e) {
                            Throwable pricina = e.getCause();

                            if (pricina instanceof NoFeasibleSolutionException) {
                                panelVysledku.oznacChybu("Bez riešenia");

                                vysledok.setText(
                                        DiagnostikaScenara.vysvetli(scenar)
                                );

                                vysledok.setCaretPosition(0);

                            } else {
                                panelVysledku.oznacChybu("Chyba výpočtu");

                                vysledok.setText(
                                        "Výpočet sa nepodaril:\n"
                                                + pricina.getMessage()
                                );
                            }

                        } finally {
                            nastavVstupy(true);
                        }
                    }
                };

        uloha.execute();
    }

    private void nastavVstupy(boolean povolene) {
        vypocitat.setEnabled(povolene);
        importExcel.setEnabled(povolene);
        exportExcel.setEnabled(povolene);
        vytvoritSablonu.setEnabled(povolene);
        obnovitScenar.setEnabled(povolene);

        for (JTextField pole : polia.values()) {
            pole.setEnabled(povolene);
        }

        tabulkaVylepseni.nastavPovolene(povolene);

        matematickyModel.setEnabled(
                povolene
                        && poslednyScenar != null
                        && poslednyVysledok != null
        );
        if (povolene) {
            Napoveda.obnovRegistraciu(getContentPane());
        }
    }

    private void zobrazVysledok(Scenar scenar, Vysledok v) {
        panelVysledku.zobraz(scenar, v);

        poslednyScenar = scenar;
        poslednyVysledok = v;

        matematickyModel.setEnabled(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            HerneOkno okno = new HerneOkno();
            okno.setVisible(true);
        });
    }
    private void zneplatniVysledok() {
        poslednyScenar = null;
        poslednyVysledok = null;
        matematickyModel.setEnabled(false);

        if (panelVysledku != null) {
            panelVysledku.vymaz("Neaktuálne");
        }

        vysledok.setText(
                "Vstupy sa zmenili.\n"
                        + "Klikni na „Vypočítať optimum“ "
                        + "pre aktuálny výsledok."
        );
    }
    private JFileChooser vytvorVyberSuboru() {
        JFileChooser vyber = new JFileChooser();

        vyber.setFileFilter(
                new FileNameExtensionFilter(
                        "Excel scenár (*.xlsx)",
                        "xlsx"
                )
        );

        vyber.setAcceptAllFileFilterUsed(false);
        return vyber;
    }

    private void importujExcel() {
        JFileChooser vyber = vytvorVyberSuboru();
        vyber.setDialogTitle("Vyber scenár na import");

        int odpoved = vyber.showOpenDialog(this);

        if (odpoved != JFileChooser.APPROVE_OPTION) {
            return;
        }

        Path cesta = vyber.getSelectedFile().toPath();

        nastavVstupy(false);

        SwingWorker<Scenar, Void> uloha = new SwingWorker<>() {

            @Override
            protected Scenar doInBackground() throws Exception {
                // Najprv načítame a overíme celý súbor.
                return ExcelScenar.nacitaj(cesta);
            }

            @Override
            protected void done() {
                try {
                    Scenar nacitany = get();

                    // Polia meníme až po úspešnom importe.
                    zobrazImportovanyScenar(nacitany);

                    vysledok.setText(
                            "Scenár bol úspešne importovaný.\n\n"
                                    + "Súbor: " + cesta.getFileName() + "\n"
                                    + "Počet vylepšení: "
                                    + nacitany.getVylepsenia().size()
                                    + "\nMinimálne HP po útoku: "
                                    + minimalneHpPoUtoku
                                    + "\n\nKlikni na „Vypočítať optimum“."
                                    + "\nStarý výsledok z Excelu sa nepoužíva."
                    );

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();

                    zobrazChybuExcelu(

                            "Čakanie na import bolo prerušené.",
                            e
                    );

                } catch (ExecutionException e) {
                    zobrazChybuExcelu(
                            "Import sa nepodaril.",
                            e.getCause()
                    );

                } finally {
                    nastavVstupy(true);
                }
            }
        };

        uloha.execute();
    }

    private void zobrazImportovanyScenar(Scenar scenar) {
        minimalneHpPoUtoku =
                scenar.hodnota("minimalneHpPoUtoku");

        for (Map.Entry<String, JTextField> zaznam : polia.entrySet()) {
            zaznam.getValue().setText(
                    Double.toString(
                            scenar.hodnota(zaznam.getKey())
                    )
            );
        }

        tabulkaVylepseni.nastavVylepsenia(
                scenar.getVylepsenia()
        );

        zneplatniVysledok();
    }

    private void exportujExcel(boolean sablona) {
        final Scenar scenar;
        final double[] mnozstva;
        final String stav;

        if (sablona) {
            scenar = Scenar.predvoleny();
            mnozstva = null;
            stav = "NEVYPOCITANE - sablona";

        } else {
            try {
                scenar = nacitajScenar();

            } catch (IllegalArgumentException e) {
                zobrazChybuExcelu(
                        "Scenár sa nedá exportovať.",
                        e
                );
                return;
            }

            if (poslednyScenar != null && poslednyVysledok != null) {
                mnozstva = poslednyVysledok.mnozstva();
                stav = "OPTIMALNE";
            } else {
                mnozstva = null;
                stav = "BEZ AKTUALNEHO VYSLEDKU";
            }
        }

        JFileChooser vyber = vytvorVyberSuboru();

        vyber.setDialogTitle(
                sablona
                        ? "Uložiť novú šablónu"
                        : "Uložiť aktuálny scenár"
        );

        vyber.setSelectedFile(
                new File(
                        sablona
                                ? "Sablona.xlsx"
                                : "Moj_scenar.xlsx"
                )
        );

        int odpoved = vyber.showSaveDialog(this);

        if (odpoved != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File subor = vyber.getSelectedFile();

        if (!subor.getName().toLowerCase(Locale.ROOT).endsWith(".xlsx")) {
            subor = new File(
                    subor.getParentFile(),
                    subor.getName() + ".xlsx"
            );
        }

        if (subor.exists()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Súbor už existuje.\n"
                            + "Zvoľ nový názov, aby pôvodný zostal zachovaný.",
                    "Súbor sa neprepísal",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        final Path cesta = subor.toPath();
        nastavVstupy(false);

        SwingWorker<Void, Void> uloha = new SwingWorker<>() {

            @Override
            protected Void doInBackground() throws Exception {
                ExcelScenar.uloz(cesta, scenar, mnozstva, stav);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();

                    String sprava;

                    if (sablona) {
                        sprava = "Šablóna s pôvodnými údajmi bola uložená.";
                    } else if (mnozstva != null) {
                        sprava = "Scenár aj aktuálny výsledok boli uložené.";
                    } else {
                        sprava = "Vstupy scenára boli uložené bez výsledku.";
                    }

                    JOptionPane.showMessageDialog(
                            HerneOkno.this,
                            sprava + "\n\n"
                                    + cesta.toAbsolutePath().normalize(),
                            "Uloženie dokončené",
                            JOptionPane.INFORMATION_MESSAGE
                    );

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();

                    zobrazChybuExcelu(
                            "Čakanie na export bolo prerušené.",
                            e
                    );

                } catch (ExecutionException e) {
                    zobrazChybuExcelu(
                            "Export sa nepodaril.",
                            e.getCause()
                    );

                } finally {
                    nastavVstupy(true);
                }
            }
        };

        uloha.execute();
    }
    private void zobrazChybuExcelu(String nadpis, Throwable chyba) {
        String detail = chyba == null
                ? "Neznáma chyba."
                : chyba.getMessage();

        if (detail == null || detail.isBlank()) {
            detail = chyba.getClass().getSimpleName();
        }

        JOptionPane.showMessageDialog(
                this,
                nadpis + "\n\n" + detail,
                "Excel",
                JOptionPane.ERROR_MESSAGE
        );
    }
    private JPanel vytvorPanelPostavy(
            String nadpis,
            String cestaObrazka,
            JPanel udaje
    ) {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(PANEL);
        panel.setBorder(new OzdobnyRam());

        panel.setPreferredSize(new Dimension(255, 600));

        NadpisPanelu titulok = new NadpisPanelu(
                nadpis,
                NadpisPanelu.Styl.CERVENY
        );

        panel.add(titulok, BorderLayout.NORTH);

        panel.add(
                new ObrazkovyPanel(cestaObrazka),
                BorderLayout.CENTER
        );

        panel.add(udaje, BorderLayout.SOUTH);

        return panel;
    }
    private void upravVzhledPola(JTextField pole) {
        pole.setBackground(new Color(22, 20, 19));
        pole.setForeground(TEXT);
        pole.setCaretColor(TEXT);

        pole.setSelectionColor(new Color(125, 55, 45));
        pole.setSelectedTextColor(Color.WHITE);
        pole.setDisabledTextColor(new Color(145, 135, 120));

        pole.setFont(new Font("SansSerif", Font.PLAIN, 16));
        pole.setHorizontalAlignment(JTextField.RIGHT);

        pole.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(115, 87, 58)
                        ),
                        BorderFactory.createEmptyBorder(4, 8, 4, 8)
                )
        );
    }
    private JPanel vytvorPanelLimitov(Scenar scenar) {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(PANEL);

        panel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(115, 87, 58)
                        ),
                        new EmptyBorder(12, 15, 12, 15)
                )
        );

        NadpisPanelu nadpis = new NadpisPanelu(
                "LIMITY PRÍPRAVY",
                NadpisPanelu.Styl.PERGAMEN
        );

        panel.add(nadpis, BorderLayout.NORTH);

        // Tri rovnako široké skupiny vedľa seba.
        JPanel riadok = new JPanel(new GridLayout(1, 3, 15, 0));
        riadok.setOpaque(false);

        String[] kluce = {
                "maximalnaHmotnost",
                "maximalnyCas",
                "katalyzator"
        };

        String[] popisy = {
                "Dodatočná hmotnosť",
                "Čas prípravy",
                "Katalyzátor"
        };

        String[] jednotky = {
                "kg",
                "min",
                "ml"
        };

        for (int i = 0; i < kluce.length; i++) {
            JPanel skupina = new JPanel(new BorderLayout(6, 6));
            skupina.setOpaque(false);

            JLabel popis = new JLabel(popisy[i]);
            popis.setForeground(TEXT);
            popis.setFont(new Font("SansSerif", Font.PLAIN, 13));

            HernaIkona.pridaj(popis, kluce[i]);
            popis.setToolTipText(Napoveda.parameter(kluce[i]));


            JTextField pole = new JTextField(
                    Double.toString(scenar.hodnota(kluce[i]))
            );

            upravVzhledPola(pole);
            pole.setToolTipText(Napoveda.parameter(kluce[i]));

            JLabel jednotka = new JLabel(jednotky[i]);
            jednotka.setForeground(TEXT);
            jednotka.setFont(new Font("SansSerif", Font.PLAIN, 14));

            skupina.add(popis, BorderLayout.NORTH);
            skupina.add(pole, BorderLayout.CENTER);
            skupina.add(jednotka, BorderLayout.EAST);

            // Rovnaké kľúče ako predtým:
            // načítanie vstupov a import Excelu zostávajú funkčné.
            polia.put(kluce[i], pole);

            riadok.add(skupina);
        }

        panel.add(riadok, BorderLayout.CENTER);

        return panel;
    }
    private void obnovPovodnyScenar() {
        Object[] moznosti = {
                "Obnoviť pôvodné údaje",
                "Zrušiť"
        };

        int odpoved = JOptionPane.showOptionDialog(
                this,
                "Obnoviť pôvodný scenár?\n\n"
                        + "Parametre postavy, nepriateľa, limity "
                        + "a všetky vylepšenia sa vrátia "
                        + "na pôvodné hodnoty.\n\n"
                        + "Neexportované úpravy sa stratia.\n"
                        + "Uložené excelové súbory zostanú zachované.",
                "Obnovenie pôvodného scenára",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.WARNING_MESSAGE,
                null,
                moznosti,
                moznosti[1]
        );

        // Zrušenie alebo zatvorenie dialógu nič nezmení.
        if (odpoved != 0) {
            return;
        }

        Scenar povodny = Scenar.predvoleny();

        // Existujúca metóda obnoví vstupné polia, skryté minimum HP,
        // tabuľku vylepšení a zneplatní predchádzajúci výsledok.
        zobrazImportovanyScenar(povodny);

        panelVysledku.vymaz("Nevypočítané");

        vysledok.setText(
                "Pôvodný scenár bol obnovený.\n\n"
                        + "Obnovené boli parametre postavy, nepriateľa, "
                        + "limity prípravy a pôvodných osem vylepšení.\n\n"
                        + "Klikni na „Vypočítať optimum“."
        );

        vysledok.setCaretPosition(0);
    }
}