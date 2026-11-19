package org.example;

import java.util.*;
import org.apache.commons.math3.exception.TooManyIterationsException;
import org.apache.commons.math3.optim.*;
import org.apache.commons.math3.optim.linear.*;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;

public class Optimalizator {
    public static double[] najdiOptimalneMnozstva(List<Vylepsenie> vylepsenia,
            double potrebnyBonusDamage, double potrebnyBonusHp,
            double dostupnaDodatocnaStamina, double maximalnaHmotnost,
            double maximalnyCas, double dostupnyKatalyzator) {
        int n = vylepsenia.size();
        if (n == 0) throw new IllegalArgumentException("Zoznam vylepseni je prazdny.");
        double[] ceny = new double[n];
        double[][] a = new double[6][n];
        List<LinearConstraint> obmedzenia = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            double[] hodnoty = vylepsenia.get(i).cisla();
            ceny[i] = hodnoty[0];
            for (int j = 0; j < 6; j++) a[j][i] = hodnoty[j + 1];
            double[] jednotkovy = new double[n];
            jednotkovy[i] = 1;
            obmedzenia.add(new LinearConstraint(jednotkovy, Relationship.LEQ, hodnoty[7]));
        }
        double[] b = {potrebnyBonusDamage, potrebnyBonusHp, dostupnaDodatocnaStamina,
                maximalnaHmotnost, maximalnyCas, dostupnyKatalyzator};
        for (int j = 0; j < 6; j++) obmedzenia.add(new LinearConstraint(a[j],
                j < 2 ? Relationship.GEQ : Relationship.LEQ, b[j]));
        try {
            double[] x = new SimplexSolver(1e-9).optimize(new MaxIter(10000),
                    new LinearObjectiveFunction(ceny, 0), new LinearConstraintSet(obmedzenia),
                    GoalType.MINIMIZE, new NonNegativeConstraint(true)).getPoint();
            for (int i = 0; i < n; i++) {
                double max = vylepsenia.get(i).getMaximalneMnozstvo();
                if (x[i] < 0 && x[i] >= -1e-7) x[i] = 0;
                if (x[i] > max && x[i] <= max + 1e-7) x[i] = max;
            }
            return x;
        } catch (TooManyIterationsException e) {
            throw new IllegalStateException("Vypocet prekrocil pocet iteracii.", e);
        }
    }

    public static double[] vypocitaj(Scenar s) {
        s.over();
        return najdiOptimalneMnozstva(s.getVylepsenia(),
                s.hodnota("hpNepriatela") - s.hodnota("damagePostavy"),
                s.hodnota("damageNepriatela") + s.hodnota("minimalneHpPoUtoku") - s.hodnota("hpPostavy"),
                s.hodnota("stamina") - s.hodnota("zakladnaSpotrebaStaminy"),
                s.hodnota("maximalnaHmotnost"), s.hodnota("maximalnyCas"), s.hodnota("katalyzator"));
    }
}
