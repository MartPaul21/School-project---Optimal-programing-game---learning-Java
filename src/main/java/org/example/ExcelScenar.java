package org.example;

import java.io.*;
import java.nio.file.*;
import java.time.OffsetDateTime;
import java.util.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/** Format .xlsx verzie 1. Import cita iba vstupy, nikdy stary vysledok. */
public class ExcelScenar {
    public static final String[] STLPCE = {"Nazov", "Jednotka", "Cena", "Bonus DMG", "Bonus HP",
            "Stamina", "Hmotnost kg", "Cas min", "Katalyzator ml", "Maximum"};

    public static Scenar nacitaj(Path cesta) throws IOException {
        overPriponu(cesta);
        // Citanie cez InputStream nikdy neuklada zmeny do zdrojoveho suboru.
        try (InputStream in = Files.newInputStream(cesta); XSSFWorkbook wb = new XSSFWorkbook(in)) {
            Sheet vstupy = harok(wb, "Scenar");
            if (!text(vstupy, 0, 0).equals("OP_SCENAR") || cislo(vstupy, 0, 1, 0) != 1)
                throw new IllegalArgumentException("Scenar!A1:B1: nepodporovany format. Pouzi exportovanu sablonu verzie 1.");
            hlavicky(vstupy, 2, new String[]{"Kluc", "Hodnota", "Popis"});
            Scenar s = new Scenar();
            Set<String> kluce = new HashSet<>();
            for (int r = 3; r <= vstupy.getLastRowNum(); r++) {
                if (prazdnyRiadok(vstupy.getRow(r))) continue;
                String kluc = text(vstupy, r, 0);
                if (!Arrays.asList(Scenar.KLUCE).contains(kluc))
                    throw chyba(vstupy, r, 0, "Neznamy kluc: " + kluc);
                if (!kluce.add(kluc)) throw chyba(vstupy, r, 0, "Duplicitny kluc: " + kluc);
                s.nastav(kluc, cislo(vstupy, r, 1, Scenar.minimum(kluc)));
            }
            for (String kluc : Scenar.KLUCE)
                if (!kluce.contains(kluc)) throw new IllegalArgumentException("Harok Scenar: chyba parameter " + kluc);

            Sheet polozky = harok(wb, "Vylepsenia");
            hlavicky(polozky, 0, STLPCE);
            Set<String> nazvy = new HashSet<>();
            for (int r = 1; r <= polozky.getLastRowNum(); r++) {
                if (prazdnyRiadok(polozky.getRow(r))) continue;
                String nazov = text(polozky, r, 0);
                String jednotka = text(polozky, r, 1);
                if (!nazvy.add(nazov.toLowerCase(Locale.ROOT)))
                    throw chyba(polozky, r, 0, "Duplicitny nazov: " + nazov);
                double[] a = new double[8];
                for (int j = 0; j < 8; j++) a[j] = cislo(polozky, r, j + 2, 0);
                s.getVylepsenia().add(new Vylepsenie(nazov, jednotka,
                        a[0], a[1], a[2], a[3], a[4], a[5], a[6], a[7]));
            }
            s.over();
            return s;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("Subor sa neda nacitat ako platny .xlsx: " + e.getMessage(), e);
        }
    }

    public static void uloz(Path cesta, Scenar s, double[] mnozstva, String stav) throws IOException {
        overPriponu(cesta);
        s.over();
        if (Files.exists(cesta)) throw new FileAlreadyExistsException(cesta.toString());
        Vysledok vysledok = mnozstva == null ? null : new Vysledok(s, mnozstva);
        byte[] obsah;
        try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            CellStyle hlavicka = wb.createCellStyle();
            hlavicka.setFillForegroundColor(IndexedColors.DARK_TEAL.getIndex());
            hlavicka.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font biele = wb.createFont(); biele.setBold(true); biele.setColor(IndexedColors.WHITE.getIndex());
            hlavicka.setFont(biele); hlavicka.setWrapText(true);
            CellStyle cislo = wb.createCellStyle();
            cislo.setDataFormat(wb.createDataFormat().getFormat("0.##########"));
            CellStyle vstup = wb.createCellStyle(); vstup.cloneStyleFrom(cislo);
            vstup.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
            vstup.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font modre = wb.createFont(); modre.setColor(IndexedColors.BLUE.getIndex()); vstup.setFont(modre);

            Sheet scenar = wb.createSheet("Scenar");
            riadok(scenar, 0, "OP_SCENAR", 1);
            riadok(scenar, 1, "Upravuj hodnoty v stlpci B. Kluc, hlavicky a nazvy harkov nemen.");
            riadok(scenar, 2, "Kluc", "Hodnota", "Popis");
            stylRiadku(scenar.getRow(2), hlavicka);
            for (int i = 0; i < Scenar.KLUCE.length; i++) {
                riadok(scenar, i + 3, Scenar.KLUCE[i], s.hodnota(Scenar.KLUCE[i]), Scenar.POPISY[i]);
                scenar.getRow(i + 3).getCell(1).setCellStyle(vstup);
            }
            scenar.setColumnWidth(0, 32 * 256); scenar.setColumnWidth(1, 20 * 256);
            scenar.setColumnWidth(2, 58 * 256); scenar.createFreezePane(0, 3);

            Sheet polozky = wb.createSheet("Vylepsenia");
            riadok(polozky, 0, (Object[]) STLPCE); stylRiadku(polozky.getRow(0), hlavicka);
            polozky.getRow(0).setHeightInPoints(32);
            for (int i = 0; i < s.getVylepsenia().size(); i++) {
                Vylepsenie v = s.getVylepsenia().get(i);
                Row r = polozky.createRow(i + 1);
                r.createCell(0).setCellValue(v.getNazov()); r.createCell(1).setCellValue(v.getJednotka());
                double[] a = v.cisla();
                for (int j = 0; j < 8; j++) { Cell c = r.createCell(j + 2); c.setCellValue(a[j]); c.setCellStyle(vstup); }
            }
            polozky.setColumnWidth(0, 36 * 256); polozky.setColumnWidth(1, 16 * 256);
            for (int j = 2; j < 10; j++) polozky.setColumnWidth(j, 17 * 256);
            polozky.createFreezePane(2, 1);
            polozky.setAutoFilter(new CellRangeAddress(0, polozky.getLastRowNum(), 0, 9));

            Sheet vystup = wb.createSheet("Vysledok");
            riadok(vystup, 0, "ZAZNAM VYPOCTU", vysledok == null ? stav : "OPTIMALNE - overene obmedzenia");
            stylRiadku(vystup.getRow(0), hlavicka);
            riadok(vystup, 1, "Exportovane", OffsetDateTime.now().toString());
            riadok(vystup, 2, "Staticky zaznam: po uprave vstupov uz nemusi platit. Import vzdy spusti novy vypocet.");
            vystup.addMergedRegion(new CellRangeAddress(2, 2, 0, 5));
            CellStyle poznamka = wb.createCellStyle(); poznamka.setWrapText(true);
            vystup.getRow(2).getCell(0).setCellStyle(poznamka); vystup.getRow(2).setHeightInPoints(32);
            if (vysledok != null) {
                riadok(vystup, 4, "Celkova cena (gold)", vysledok.sucty()[0]);
                riadok(vystup, 6, "Nazov", "Jednotka", "Mnozstvo", "Cena spolu", "Bonus DMG", "Bonus HP");
                stylRiadku(vystup.getRow(6), hlavicka);
                int r = 7;
                for (int i = 0; i < mnozstva.length; i++) {
                    Vylepsenie v = s.getVylepsenia().get(i); double x = mnozstva[i];
                    riadok(vystup, r++, v.getNazov(), v.getJednotka(), x, v.getCena()*x,
                            v.getBonusDamage()*x, v.getBonusHp()*x);
                }
                r++;
                riadok(vystup, r, "Obmedzenie", "Hodnota", "Vztah", "Limit", "Rezerva", "Stav");
                stylRiadku(vystup.getRow(r++), hlavicka);
                double[] hodnoty = vysledok.hodnoty(), limity = vysledok.limity(), rezervy = vysledok.rezervy();
                for (int j = 0; j < 6; j++) riadok(vystup, r++, Vysledok.KONTROLY[j], hodnoty[j],
                        j < 2 ? ">=" : "<=", limity[j], Math.abs(rezervy[j]) < 1e-7 ? 0 : rezervy[j], "SPLNENE");
                riadok(vystup, r + 1, "Rezerva pri >= je hodnota - limit; pri <= je limit - hodnota. Nie je to tienova cena.");
            } else {
                riadok(vystup, 4, "Mnozstva a cena nie su ulozene. Importuj vstupy a spusti vypocet.");
            }
            for (Row r : vystup) for (Cell c : r)
                if (c.getCellType() == CellType.NUMERIC) c.setCellStyle(cislo);
            vystup.setColumnWidth(0, 38 * 256);
            for (int j = 1; j < 6; j++) vystup.setColumnWidth(j, 22 * 256);
            vystup.createFreezePane(0, 3);
            for (Sheet sh : wb) { sh.setDisplayGridlines(false); sh.setFitToPage(true);
                sh.getPrintSetup().setLandscape(true); sh.getPrintSetup().setFitWidth((short)1);
                sh.getPrintSetup().setFitHeight((short)0); }
            wb.write(buffer); obsah = buffer.toByteArray();
        }
        // CREATE_NEW zabrani prepisaniu aj vtedy, ak subor vznikne pocas exportu.
        boolean vytvoreny = false;
        try {
            try (OutputStream out = Files.newOutputStream(cesta, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
                vytvoreny = true; out.write(obsah);
            }
        } catch (IOException e) {
            if (vytvoreny) try { Files.deleteIfExists(cesta); } catch (IOException cleanup) { e.addSuppressed(cleanup); }
            throw e;
        }
    }

    private static void overPriponu(Path cesta) {
        if (cesta.getFileName() == null || !cesta.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".xlsx"))
            throw new IllegalArgumentException("Pouzi subor s priponou .xlsx.");
    }
    private static Sheet harok(Workbook wb, String nazov) {
        Sheet sh = wb.getSheet(nazov);
        if (sh == null) throw new IllegalArgumentException("Chyba harok: " + nazov);
        return sh;
    }
    private static Cell bunka(Sheet s, int r, int c) {
        Row row = s.getRow(r); return row == null ? null : row.getCell(c);
    }
    private static String text(Sheet s, int r, int c) {
        Cell cell = bunka(s, r, c);
        if (cell == null || cell.getCellType() != CellType.STRING || cell.getStringCellValue().isBlank())
            throw chyba(s, r, c, "Ocakavam neprazdny text.");
        return cell.getStringCellValue().trim();
    }
    private static double cislo(Sheet s, int r, int c, double minimum) {
        Cell cell = bunka(s, r, c);
        if (cell == null || cell.getCellType() == CellType.BLANK) throw chyba(s, r, c, "Chyba cislo.");
        if (cell.getCellType() == CellType.FORMULA)
            throw chyba(s, r, c, "Vstup musi byt hodnota, nie vzorec. V Exceli pouzi Prilepit hodnoty.");
        double x;
        try {
            if (cell.getCellType() == CellType.NUMERIC) x = cell.getNumericCellValue();
            else if (cell.getCellType() == CellType.STRING)
                x = Double.parseDouble(cell.getStringCellValue().trim().replace(',', '.'));
            else throw new NumberFormatException();
        } catch (NumberFormatException e) { throw chyba(s, r, c, "Neplatne cislo."); }
        if (!Double.isFinite(x) || x < minimum) throw chyba(s, r, c, "Zadaj konecne cislo >= " + minimum);
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell))
            throw chyba(s, r, c, "Ocakavam cislo, nie datum.");
        return x;
    }
    private static IllegalArgumentException chyba(Sheet s, int r, int c, String sprava) {
        return new IllegalArgumentException(s.getSheetName() + "!" +
                org.apache.poi.ss.util.CellReference.convertNumToColString(c) + (r + 1) + ": " + sprava);
    }
    private static void hlavicky(Sheet s, int r, String[] nazvy) {
        for (int c = 0; c < nazvy.length; c++) if (!text(s, r, c).equals(nazvy[c]))
            throw chyba(s, r, c, "Ocakavana hlavicka: " + nazvy[c]);
    }
    private static boolean prazdnyRiadok(Row r) {
        if (r == null) return true;
        for (Cell c : r) if (c.getCellType() != CellType.BLANK &&
                !(c.getCellType() == CellType.STRING && c.getStringCellValue().isBlank())) return false;
        return true;
    }
    private static void riadok(Sheet s, int index, Object... hodnoty) {
        Row r = s.createRow(index);
        for (int i = 0; i < hodnoty.length; i++) {
            Cell c = r.createCell(i);
            if (hodnoty[i] instanceof Number) c.setCellValue(((Number)hodnoty[i]).doubleValue());
            else c.setCellValue(hodnoty[i].toString());
        }
    }
    private static void stylRiadku(Row r, CellStyle styl) { for (Cell c : r) c.setCellStyle(styl); }
}
