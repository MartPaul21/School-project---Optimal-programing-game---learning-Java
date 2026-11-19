package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;


public final class Napoveda {

    private Napoveda() {
    }

    public static String parameter(String kluc) {
        String text = switch (kluc) {
            case "hpPostavy" ->
                    "HP postavy pred prípravou. "
                            + "Bonusy vylepšení sa k nim pripočítajú.";

            case "damagePostavy" ->
                    "DMG základnej útočnej kombinácie bez vylepšení.";

            case "stamina" ->
                    "Celková dostupná stamina. Musí pokryť základnú "
                            + "kombináciu aj dodatočnú spotrebu vylepšení.";

            case "zakladnaSpotrebaStaminy" ->
                    "Stamina potrebná na základnú útočnú kombináciu "
                            + "ešte bez vylepšení.";

            case "hpNepriatela" ->
                    "Výsledné DMG postavy musí dosiahnuť "
                            + "aspoň túto hodnotu.";

            case "damageNepriatela" ->
                    "Nepriateľ útočí prvý. Toto poškodenie sa odpočíta "
                            + "od HP postavy po príprave.";

            case "minimalneHpPoUtoku" ->
                    "Najmenšie požadované HP postavy po zásahu nepriateľa.";

            case "maximalnaHmotnost" ->
                    "Limit dodatočnej hmotnosti vylepšení v kilogramoch. "
                            + "Nezahŕňa hmotnosť pôvodnej výbavy.";

            case "maximalnyCas" ->
                    "Najväčší dovolený súčet časov prípravy v minútach. "
                            + "Čas každej položky sa násobí jej množstvom.";

            case "katalyzator" ->
                    "Dostupné množstvo pomocnej látky v ml, "
                            + "spotrebovanej pri príprave vylepšení.";

            default -> "";
        };

        return html(text);
    }

    private static final String[] STLPCE = {
            "Názov vylepšenia. Každá položka musí mať jedinečný názov.",
            "Význam jednej jednotky. Napríklad pri jednotke 10 ml "
                    + "množstvo 2,5 predstavuje 25 ml. "
                    + "Text jednotky sám neprepočítava koeficienty.",
            "Cena jednej jednotky v goldoch. "
                    + "Minimalizujeme súčet cien použitých množstiev.",
            "Dodatočné DMG za jednu jednotku vylepšenia.",
            "Dodatočné HP za jednu jednotku vylepšenia.",
            "Dodatočná spotreba staminy za jednu jednotku.",
            "Dodatočná hmotnosť jednej jednotky v kilogramoch.",
            "Čas prípravy jednej jednotky v minútach.",
            "Spotreba katalyzátora na jednu jednotku v ml.",
            "Najväčšie použiteľné množstvo v jednotkách. "
                    + "Maximum 5 pri jednotke 10 ml znamená najviac 50 ml. "
                    + "Maximum 0 položku vyradí z použitia."
    };

    public static void nastavTabulku(JTable tabulka) {
        javax.swing.table.JTableHeader hlavicka =
                new javax.swing.table.JTableHeader(
                        tabulka.getColumnModel()
                ) {
                    @Override
                    public String getToolTipText(MouseEvent udalost) {
                        int stlpec = columnAtPoint(udalost.getPoint());

                        if (stlpec < 0) {
                            return null;
                        }

                        int index = getColumnModel()
                                .getColumn(stlpec)
                                .getModelIndex();

                        return index >= 0 && index < STLPCE.length
                                ? html(STLPCE[index])
                                : null;
                    }
                };

        tabulka.setTableHeader(hlavicka);

        ToolTipManager.sharedInstance().registerComponent(hlavicka);
    }

    private static String html(String text) {
        if (text.isEmpty()) {
            return null;
        }

        return "<html><body style='width: 310px'>"
                + text
                + "</body></html>";
    }



    public static void nastavFarbyDialogov() {
        ToolTipManager spravca = ToolTipManager.sharedInstance();

        spravca.setEnabled(true);
        spravca.setInitialDelay(500);
        spravca.setReshowDelay(150);
        spravca.setDismissDelay(20000);

// Vysvetlivka sa zobrazí v samostatnom vyskakovacom okne.
// Toto môže pomôcť pri problémoch s vrstvením a fullscreenom.
        spravca.setLightWeightPopupEnabled(false);

        Color pozadie = new Color(40, 37, 35);
        Color text = new Color(230, 218, 192);
        Color ram = new Color(115, 87, 58);

        UIManager.put("OptionPane.background", pozadie);
        UIManager.put("OptionPane.foreground", text);
        UIManager.put("OptionPane.messageForeground", text);
        UIManager.put(
                "OptionPane.messageFont",
                new Font("SansSerif", Font.PLAIN, 14)
        );

        UIManager.put("Panel.background", pozadie);

        UIManager.put("Button.background", new Color(67, 47, 36));
        UIManager.put("Button.foreground", text);
        UIManager.put(
                "Button.font",
                new Font("SansSerif", Font.BOLD, 13)
        );

        UIManager.put("ToolTip.background", new Color(30, 26, 22));
        UIManager.put("ToolTip.foreground", text);
        UIManager.put(
                "ToolTip.font",
                new Font("SansSerif", Font.PLAIN, 13)
        );
        UIManager.put(
                "ToolTip.border",
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(ram),
                        BorderFactory.createEmptyBorder(6, 8, 6, 8)
                )
        );
    }
    public static void obnovRegistraciu(Component komponent) {
        ToolTipManager spravca = ToolTipManager.sharedInstance();

        if (komponent instanceof JComponent swingKomponent) {
            boolean maNapovedu =
                    swingKomponent.getToolTipText() != null
                            || swingKomponent
                            instanceof javax.swing.table.JTableHeader;

            if (maNapovedu) {
                spravca.unregisterComponent(swingKomponent);
                spravca.registerComponent(swingKomponent);
            }
        }

        if (komponent instanceof Container kontajner) {
            for (Component dieta : kontajner.getComponents()) {
                obnovRegistraciu(dieta);
            }
        }
    }
}