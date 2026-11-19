package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;

public class HernaIkona implements Icon {

    public enum Typ {
        HP,
        DMG,
        STAMINA,
        HMOTNOST,
        CAS,
        KATALYZATOR
    }

    private final Typ typ;
    private final int velkost;

    public HernaIkona(Typ typ, int velkost) {
        this.typ = typ;
        this.velkost = velkost;
    }

    public static void pridaj(JLabel popis, String kluc) {
        Typ typ = switch (kluc) {
            case "hpPostavy", "hpNepriatela",
                 "minimalneHpPoUtoku" -> Typ.HP;

            case "damagePostavy", "damageNepriatela" -> Typ.DMG;

            case "stamina", "zakladnaSpotrebaStaminy" -> Typ.STAMINA;

            case "maximalnaHmotnost" -> Typ.HMOTNOST;

            case "maximalnyCas" -> Typ.CAS;

            case "katalyzator" -> Typ.KATALYZATOR;

            default -> null;
        };

        if (typ != null) {
            popis.setIcon(new HernaIkona(typ, 18));
            popis.setIconTextGap(7);
        }
    }

    @Override
    public int getIconWidth() {
        return velkost;
    }

    @Override
    public int getIconHeight() {
        return velkost;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D grafika = (Graphics2D) g.create();

        try {
            grafika.translate(x, y);
            grafika.scale(velkost / 24.0, velkost / 24.0);

            grafika.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            grafika.setStroke(
                    new BasicStroke(
                            1.5f,
                            BasicStroke.CAP_ROUND,
                            BasicStroke.JOIN_ROUND
                    )
            );

            if (c != null && !c.isEnabled()) {
                grafika.setComposite(
                        AlphaComposite.SrcOver.derive(0.4f)
                );
            }

            switch (typ) {
                case HP -> kresliSrdce(grafika);
                case DMG -> kresliMec(grafika);
                case STAMINA -> kresliBlesk(grafika);
                case HMOTNOST -> kresliZavazie(grafika);
                case CAS -> kresliHodiny(grafika);
                case KATALYZATOR -> kresliFlasu(grafika);
            }

        } finally {
            grafika.dispose();
        }
    }

    private void kresliSrdce(Graphics2D g) {
        Path2D srdce = new Path2D.Double();

        srdce.moveTo(12, 21);
        srdce.curveTo(9, 18, 2, 13, 2, 7);
        srdce.curveTo(2, 1, 9, 1, 12, 6);
        srdce.curveTo(15, 1, 22, 1, 22, 7);
        srdce.curveTo(22, 13, 15, 18, 12, 21);
        srdce.closePath();

        vypln(g, srdce, new Color(163, 55, 46));

        g.setColor(new Color(224, 118, 91));
        g.drawArc(5, 5, 5, 6, 70, 100);
    }

    private void kresliMec(Graphics2D g) {
        Path2D cepel = new Path2D.Double();

        cepel.moveTo(10, 14);
        cepel.lineTo(17, 3);
        cepel.lineTo(22, 2);
        cepel.lineTo(21, 7);
        cepel.lineTo(13, 17);
        cepel.closePath();

        vypln(g, cepel, new Color(185, 185, 167));

        g.setColor(new Color(236, 226, 196));
        g.drawLine(12, 14, 20, 4);

        g.setStroke(new BasicStroke(3f));
        g.setColor(new Color(164, 119, 60));

        // Záštita a rukoväť.
        g.drawLine(6, 12, 15, 21);
        g.drawLine(9, 17, 4, 22);

        g.setColor(new Color(214, 172, 96));
        g.fillOval(2, 19, 4, 4);
    }

    private void kresliBlesk(Graphics2D g) {
        Polygon blesk = new Polygon(
                new int[]{13, 4, 10, 8, 21, 14, 17},
                new int[]{2, 13, 13, 22, 10, 10, 2},
                7
        );

        vypln(g, blesk, new Color(204, 161, 68));
    }

    private void kresliZavazie(Graphics2D g) {
        g.setColor(new Color(174, 162, 136));
        g.setStroke(new BasicStroke(2f));
        g.drawOval(9, 3, 6, 6);

        Polygon telo = new Polygon(
                new int[]{6, 18, 21, 3},
                new int[]{9, 9, 21, 21},
                4
        );

        vypln(g, telo, new Color(136, 128, 108));

        g.setColor(new Color(203, 187, 153));
        g.drawLine(7, 11, 5, 18);
    }

    private void kresliHodiny(Graphics2D g) {
        Path2D sklo = new Path2D.Double();

        sklo.moveTo(6, 4);
        sklo.lineTo(18, 4);
        sklo.curveTo(18, 8, 15, 10, 13, 12);
        sklo.curveTo(15, 14, 18, 16, 18, 20);
        sklo.lineTo(6, 20);
        sklo.curveTo(6, 16, 9, 14, 11, 12);
        sklo.curveTo(9, 10, 6, 8, 6, 4);
        sklo.closePath();

        vypln(g, sklo, new Color(72, 66, 53));

        g.setColor(new Color(215, 179, 105));

        g.fillPolygon(
                new int[]{8, 16, 12},
                new int[]{7, 7, 11},
                3
        );

        g.fillPolygon(
                new int[]{7, 17, 12},
                new int[]{19, 19, 14},
                3
        );

        g.setStroke(new BasicStroke(2.5f));
        g.setColor(new Color(167, 119, 64));
        g.drawLine(5, 3, 19, 3);
        g.drawLine(5, 21, 19, 21);
    }

    private void kresliFlasu(Graphics2D g) {
        Path2D flasa = new Path2D.Double();

        flasa.moveTo(9, 5);
        flasa.lineTo(15, 5);
        flasa.lineTo(15, 9);
        flasa.curveTo(22, 14, 22, 21, 17, 22);
        flasa.lineTo(7, 22);
        flasa.curveTo(2, 21, 2, 14, 9, 9);
        flasa.closePath();

        vypln(g, flasa, new Color(49, 74, 73));

        Graphics2D tekutina = (Graphics2D) g.create();

        try {
            tekutina.clip(flasa);
            tekutina.setColor(new Color(91, 153, 137));
            tekutina.fillRect(2, 15, 20, 9);
        } finally {
            tekutina.dispose();
        }

        g.setColor(new Color(185, 170, 132));
        g.draw(flasa);

        g.setColor(new Color(154, 108, 58));
        g.fillRoundRect(8, 2, 8, 4, 2, 2);

        g.setColor(new Color(180, 213, 191));
        g.drawLine(7, 14, 6, 17);
    }

    private void vypln(Graphics2D g, Shape tvar, Color farba) {
        g.setColor(farba);
        g.fill(tvar);

        g.setColor(new Color(38, 27, 20));
        g.draw(tvar);
    }
}