package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

public final class RezimObrazovky {

    private final JFrame okno;

    private boolean fullscreen;
    private Rectangle povodneRozmery;
    private int povodnyStav;
    private boolean povodneBezRamu;

    private RezimObrazovky(JFrame okno) {
        this.okno = okno;

        KeyboardFocusManager.getCurrentKeyboardFocusManager()
                .addKeyEventDispatcher(this::spracujKlaves);
    }

    public static void zapoj(JFrame okno) {
        // Zabráni viacnásobnému zapojeniu do rovnakého okna.
        String kluc = "rezimObrazovky";

        if (okno.getRootPane().getClientProperty(kluc) != null) {
            return;
        }

        RezimObrazovky ovladanie = new RezimObrazovky(okno);

        okno.getRootPane().putClientProperty(
                kluc,
                ovladanie
        );
    }

    private boolean spracujKlaves(KeyEvent udalost) {
        // Klávesy spracujeme iba v hlavnom okne.
        // Matematické okno a dialógy majú vlastné ovládanie.
        Window aktivneOkno = KeyboardFocusManager
                .getCurrentKeyboardFocusManager()
                .getActiveWindow();

        if (aktivneOkno != okno) {
            return false;
        }

        boolean f11 = udalost.getKeyCode() == KeyEvent.VK_F11;
        boolean escape = udalost.getKeyCode() == KeyEvent.VK_ESCAPE;

        if (!f11 && !(escape && fullscreen)) {
            return false;
        }

        // Prepnutie vykonáme pri pustení klávesu.
        // Držanie F11 tak nespôsobí opakované prepínanie.
        if (udalost.getID() == KeyEvent.KEY_RELEASED) {
            if (f11) {
                prepni();
            } else {
                vypniFullscreen();
            }
        }

        // V režime fullscreen ESC neprevezme editor bunky.
        return true;
    }

    private void prepni() {
        if (fullscreen) {
            vypniFullscreen();
        } else {
            zapniFullscreen();
        }
    }

    private void zapniFullscreen() {
        if (!potvrdEditaciu(okno.getContentPane())) {
            return;
        }

        povodneRozmery = new Rectangle(okno.getBounds());
        povodnyStav = okno.getExtendedState();
        povodneBezRamu = okno.isUndecorated();

        // Použijeme monitor, na ktorom sa okno práve nachádza.
        Rectangle obrazovka = new Rectangle(
                okno.getGraphicsConfiguration().getBounds()
        );

        fullscreen = true;

        // Java povoľuje zmenu rámu iba pri nedisplayovateľnom okne.
        // dispose() tu nezmaže komponenty ani údaje.
        okno.dispose();

        okno.setExtendedState(JFrame.NORMAL);
        okno.setUndecorated(true);
        okno.setBounds(obrazovka);
        okno.setVisible(true);

        okno.toFront();
        okno.requestFocus();
    }

    private void vypniFullscreen() {
        if (!fullscreen) {
            return;
        }

        if (!potvrdEditaciu(okno.getContentPane())) {
            return;
        }

        fullscreen = false;

        okno.dispose();

        okno.setExtendedState(JFrame.NORMAL);
        okno.setUndecorated(povodneBezRamu);
        okno.setBounds(povodneRozmery);
        okno.setVisible(true);

        // Ak bolo okno pôvodne maximalizované,
        // vrátime ho do maximalizovaného stavu.
        okno.setExtendedState(povodnyStav);

        okno.toFront();
        okno.requestFocus();
    }

    private boolean potvrdEditaciu(Component komponent) {
        if (komponent instanceof JTable tabulka
                && tabulka.isEditing()) {

            if (!tabulka.getCellEditor().stopCellEditing()) {
                return false;
            }
        }

        if (komponent instanceof Container kontajner) {
            for (Component dieta : kontajner.getComponents()) {
                if (!potvrdEditaciu(dieta)) {
                    return false;
                }
            }
        }

        return true;
    }
}