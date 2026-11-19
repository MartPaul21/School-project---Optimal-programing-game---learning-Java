package org.example;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.swing.table.DefaultTableCellRenderer;

public class TabulkaVylepseni extends JPanel {

    private static final String[] STLPCE = {
            "Názov",
            "Jednotka",
            "Cena",
            "Bonus DMG",
            "Bonus HP",
            "Stamina",
            "Hmotnosť kg",
            "Čas min",
            "Katalyzátor ml",
            "Maximum"
    };

    private final DefaultTableModel model;
    private final JTable tabulka;

    private final JButton pridat =
            new JButton("Pridať vylepšenie");

    private final JButton odstranit =
            new JButton("Odstrániť vybrané");

    public TabulkaVylepseni(
            List<Vylepsenie> povodneVylepsenia,
            Runnable priZmene
    ) {
        setLayout(new BorderLayout(8, 8));
        setBackground(new Color(40, 37, 35));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        NadpisPanelu nadpis = new NadpisPanelu(
                "VYLEPŠENIA VÝBAVY",
                NadpisPanelu.Styl.PERGAMEN
        );

        add(nadpis, BorderLayout.NORTH);

        model = new DefaultTableModel(STLPCE, 0);

        // Počiatočné naplnenie tabuľky.
        for (Vylepsenie v : povodneVylepsenia) {
            model.addRow(new Object[]{
                    v.getNazov(),
                    v.getJednotka(),
                    v.getCena(),
                    v.getBonusDamage(),
                    v.getBonusHp(),
                    v.getSpotrebaStaminy(),
                    v.getHmotnost(),
                    v.getCasPripravy(),
                    v.getSpotrebaKatalyzatora(),
                    v.getMaximalneMnozstvo()
            });
        }

        tabulka = new JTable(model);
        Napoveda.nastavTabulku(tabulka);
        tabulka.setRowHeight(30);
        tabulka.setFont(new Font("SansSerif", Font.PLAIN, 14));
        tabulka.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        tabulka.setBackground(new Color(48, 44, 40));
        tabulka.setForeground(new Color(235, 225, 205));
        tabulka.setGridColor(new Color(80, 70, 60));
        tabulka.setSelectionBackground(new Color(110, 45, 40));
        tabulka.setSelectionForeground(Color.WHITE);

        tabulka.getTableHeader().setFont(
                new Font("SansSerif", Font.BOLD, 13)
        );
        tabulka.getTableHeader().setReorderingAllowed(false);
        // Vlastné vykreslenie zabráni tomu, aby systémový vzhľad
        // ponechal hlavičku tabuľky bielu.
        DefaultTableCellRenderer hlavicka =
                new DefaultTableCellRenderer();

        hlavicka.setOpaque(true);
        hlavicka.setBackground(new Color(67, 47, 36));
        hlavicka.setForeground(new Color(230, 218, 192));
        hlavicka.setFont(new Font("SansSerif", Font.BOLD, 13));
        hlavicka.setHorizontalAlignment(SwingConstants.CENTER);

        hlavicka.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(
                                0, 0, 1, 1,
                                new Color(115, 87, 58)
                        ),
                        BorderFactory.createEmptyBorder(8, 5, 8, 5)
                )
        );

        tabulka.getTableHeader().setDefaultRenderer(hlavicka);
        tabulka.getTableHeader().setPreferredSize(
                new Dimension(0, 36)
        );

        // Textové stĺpce majú vnútorný odstup.
        DefaultTableCellRenderer textoveBunky =
                new DefaultTableCellRenderer();

        textoveBunky.setBorder(
                BorderFactory.createEmptyBorder(0, 8, 0, 8)
        );

        tabulka.getColumnModel().getColumn(0)
                .setCellRenderer(textoveBunky);

        tabulka.getColumnModel().getColumn(1)
                .setCellRenderer(textoveBunky);

        // Čísla zarovnáme doprava.
        DefaultTableCellRenderer ciselneBunky =
                new DefaultTableCellRenderer();

        ciselneBunky.setHorizontalAlignment(SwingConstants.RIGHT);
        ciselneBunky.setBorder(
                BorderFactory.createEmptyBorder(0, 8, 0, 8)
        );

        for (int i = 2; i < STLPCE.length; i++) {
            tabulka.getColumnModel().getColumn(i)
                    .setCellRenderer(ciselneBunky);
        }

        // Aj práve upravovaná bunka bude tmavá.
        JTextField editor = new JTextField();
        editor.setBackground(new Color(22, 20, 19));
        editor.setForeground(new Color(230, 218, 192));
        editor.setCaretColor(new Color(230, 218, 192));
        editor.setSelectionColor(new Color(125, 55, 45));
        editor.setSelectedTextColor(Color.WHITE);
        editor.setFont(new Font("SansSerif", Font.PLAIN, 14));

        editor.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(185, 140, 85)
                        ),
                        BorderFactory.createEmptyBorder(0, 6, 0, 6)
                )
        );

        tabulka.setDefaultEditor(
                Object.class,
                new DefaultCellEditor(editor)
        );

        // Zachováme čitateľné šírky; pri potrebe sa zobrazí posuvník.
        tabulka.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (int i = 0; i < STLPCE.length; i++) {
            int sirka;

            if (i == 0) {
                sirka = 240;
            } else if (i == 1) {
                sirka = 100;
            } else {
                sirka = 110;
            }

            tabulka.getColumnModel().getColumn(i)
                    .setPreferredWidth(sirka);
        }

        // Pri kliknutí mimo bunky sa úprava pokúsi potvrdiť.
        tabulka.putClientProperty(
                "terminateEditOnFocusLost",
                Boolean.TRUE
        );

        JScrollPane posuvanie = new JScrollPane(tabulka);

        tabulka.getTableHeader().setOpaque(true);
        tabulka.getTableHeader().setBackground(new Color(67, 47, 36));

        TmavyStyl.upravPosuvanie(posuvanie);

        add(posuvanie, BorderLayout.CENTER);


        JPanel tlacidla = new JPanel(new FlowLayout(FlowLayout.LEFT));
        tlacidla.setOpaque(false);

        tlacidla.add(pridat);
        tlacidla.add(odstranit);
        for (JButton tlacidlo : new JButton[]{pridat, odstranit}) {
            tlacidlo.setBackground(new Color(67, 47, 36));
            tlacidlo.setForeground(new Color(230, 218, 192));
            tlacidlo.setFont(new Font("SansSerif", Font.BOLD, 13));
            tlacidlo.setFocusPainted(false);
            tlacidlo.setOpaque(true);
            tlacidlo.setContentAreaFilled(true);

            tlacidlo.setBorder(
                    BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(
                                    new Color(115, 87, 58)
                            ),
                            BorderFactory.createEmptyBorder(7, 12, 7, 12)
                    )
            );
        }

        JLabel napoveda = new JLabel(
                "Dvojklik upraví bunku. Cena a účinky sú na jednu jednotku."
        );
        napoveda.setForeground(new Color(230, 218, 192));
        tlacidla.add(napoveda);

        add(tlacidla, BorderLayout.SOUTH);

        pridat.addActionListener(event -> pridajRiadok());
        odstranit.addActionListener(event -> odstranRiadok());

        // Potvrdenie úpravy, pridanie aj odstránenie zneplatní výsledok.
        model.addTableModelListener(event -> priZmene.run());

        // Výsledok zneplatníme už pri začatí editácie.
        // Nemusíme čakať na potvrdenie bunky Enterom.
        tabulka.addPropertyChangeListener("tableCellEditor", event -> {
            if (tabulka.isEditing()) {
                priZmene.run();
            }
        });
        HernyStylTlacidiel.uprav(pridat, false);
        HernyStylTlacidiel.uprav(odstranit, false);
    }

    private void pridajRiadok() {
        if (!potvrdUpravu()) {
            return;
        }

        // Nová položka je najprv vypnutá maximom 0.
        // Používateľ nastaví jej vlastnosti a použiteľné maximum.
        model.addRow(new Object[]{
                novyNazov(),
                "jednotka",
                0.0,
                0.0,
                0.0,
                0.0,
                0.0,
                0.0,
                0.0,
                0.0
        });

        int posledny = model.getRowCount() - 1;
        tabulka.setRowSelectionInterval(posledny, posledny);

        tabulka.scrollRectToVisible(
                tabulka.getCellRect(posledny, 0, true)
        );
    }

    private String novyNazov() {
        int cislo = 1;

        while (true) {
            String kandidat = "Nové vylepšenie " + cislo;
            boolean existuje = false;

            for (int r = 0; r < model.getRowCount(); r++) {
                Object hodnota = model.getValueAt(r, 0);

                if (hodnota != null
                        && kandidat.equalsIgnoreCase(
                        hodnota.toString().trim()
                )) {
                    existuje = true;
                    break;
                }
            }

            if (!existuje) {
                return kandidat;
            }

            cislo++;
        }
    }

    private void odstranRiadok() {
        if (!potvrdUpravu()) {
            return;
        }

        int vybrany = tabulka.getSelectedRow();

        if (vybrany == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Najprv označ riadok, ktorý chceš odstrániť."
            );
            return;
        }

        int index = tabulka.convertRowIndexToModel(vybrany);
        model.removeRow(index);
    }

    private boolean potvrdUpravu() {
        if (!tabulka.isEditing()) {
            return true;
        }

        return tabulka.getCellEditor().stopCellEditing();
    }

    public List<Vylepsenie> nacitajVylepsenia() {
        // Dôležité: načítame aj poslednú práve upravovanú bunku.
        if (!potvrdUpravu()) {
            throw new IllegalArgumentException(
                    "Najprv dokonči úpravu bunky v tabuľke."
            );
        }

        if (model.getRowCount() == 0) {
            throw new IllegalArgumentException(
                    "Tabuľka musí obsahovať aspoň jedno vylepšenie."
            );
        }

        List<Vylepsenie> vysledok = new ArrayList<>();
        Set<String> nazvy = new HashSet<>();

        for (int r = 0; r < model.getRowCount(); r++) {
            String nazov = nacitajText(r, 0);
            String jednotka = nacitajText(r, 1);

            if (!nazvy.add(nazov.toLowerCase(Locale.ROOT))) {
                throw new IllegalArgumentException(
                        "Riadok " + (r + 1)
                                + ": názov „" + nazov + "“ už existuje."
                );
            }

            double[] hodnoty = new double[8];

            for (int i = 0; i < hodnoty.length; i++) {
                hodnoty[i] = nacitajCislo(r, i + 2);
            }

            Vylepsenie v = new Vylepsenie(
                    nazov,
                    jednotka,
                    hodnoty[0],
                    hodnoty[1],
                    hodnoty[2],
                    hodnoty[3],
                    hodnoty[4],
                    hodnoty[5],
                    hodnoty[6],
                    hodnoty[7]
            );

            vysledok.add(v);
        }

        return vysledok;
    }

    private String nacitajText(int riadok, int stlpec) {
        Object hodnota = model.getValueAt(riadok, stlpec);

        String text = hodnota == null
                ? ""
                : hodnota.toString().trim();

        if (text.isEmpty()) {
            throw new IllegalArgumentException(
                    "Riadok " + (riadok + 1)
                            + ", stĺpec „" + STLPCE[stlpec]
                            + "“: hodnota nesmie byť prázdna."
            );
        }

        return text;
    }

    private double nacitajCislo(int riadok, int stlpec) {
        String text = nacitajText(riadok, stlpec)
                .replace(',', '.');

        try {
            double cislo = Double.parseDouble(text);

            if (Double.isFinite(cislo) && cislo >= 0) {
                return cislo;
            }

        } catch (NumberFormatException ignored) {
        }

        throw new IllegalArgumentException(
                "Riadok " + (riadok + 1)
                        + ", stĺpec „" + STLPCE[stlpec]
                        + "“: zadaj konečné nezáporné číslo."
        );
    }

    public void nastavPovolene(boolean povolene) {
        tabulka.setEnabled(povolene);
        pridat.setEnabled(povolene);
        odstranit.setEnabled(povolene);
    }

    public void nastavVylepsenia(List<Vylepsenie> vylepsenia) {
        // Rozpracovanú bunku pri nahradení tabuľky zahodíme.
        if (tabulka.isEditing()) {
            tabulka.getCellEditor().cancelCellEditing();
        }

        model.setRowCount(0);

        for (Vylepsenie v : vylepsenia) {
            model.addRow(new Object[]{
                    v.getNazov(),
                    v.getJednotka(),
                    v.getCena(),
                    v.getBonusDamage(),
                    v.getBonusHp(),
                    v.getSpotrebaStaminy(),
                    v.getHmotnost(),
                    v.getCasPripravy(),
                    v.getSpotrebaKatalyzatora(),
                    v.getMaximalneMnozstvo()
            });
        }
    }
}