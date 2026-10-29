package com.missionapp;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.*;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Renders the three-page mission form (Letter size) so that it matches the original
 * Excel printout (Mission.pdf). Static geometry and labels come from assets/form/spec.json,
 * which was extracted from the reference file; this class only fills in the values.
 */
public class PdfGenerator {

    private static final float W = 612f, H = 792f;
    private static final float ROW_H = 21.48f;           // expense row height on page 2
    private static final float[] CUT = {149.88f, 235.80f, 321.72f, 407.64f}; // end of the first data row per section
    private static final float[] SECTION_TOP = {85.92f, 171.84f, 257.76f, 343.68f};

    private static final int GRAY1 = 0xD9D9D9, GRAY2 = 0xD0CECE, ROW_FILL = 0xF2F2F2;

    // ---------------------------------------------------------------- entry point

    public static void generateAndShare(Activity activity, JSONObject m) {
        try {
            JSONObject spec = loadSpec(activity);
            Bitmap logo1 = loadAsset(activity, "form/logo1.jpg");
            Bitmap logo2 = loadAsset(activity, "form/logo2.jpg");
            String uuid = getOrCreateUuid(m);
            String today = todayJalali();
            JSONArray pages = spec.getJSONArray("pages");

            PdfDocument doc = new PdfDocument();
            PdfDocument.Page p1 = doc.startPage(new PdfDocument.PageInfo.Builder((int) W, (int) H, 1).create());
            drawPage1(p1.getCanvas(), pages.getJSONObject(0), m, logo1, uuid, today);
            doc.finishPage(p1);

            PdfDocument.Page p2 = doc.startPage(new PdfDocument.PageInfo.Builder((int) W, (int) H, 2).create());
            drawPage2(p2.getCanvas(), pages.getJSONObject(1), m, logo2, uuid, today);
            doc.finishPage(p2);

            PdfDocument.Page p3 = doc.startPage(new PdfDocument.PageInfo.Builder((int) W, (int) H, 3).create());
            drawPage3(p3.getCanvas(), pages.getJSONObject(2), m, uuid, today);
            doc.finishPage(p3);

            String formNo = m.optString("form", "").trim().replaceAll("[^\\p{L}\\p{N}_-]", "_");
            if (formNo.isEmpty()) formNo = "بدون_شماره";
            String fn = "M" + formNo + ".pdf";
            Uri u;
            if (Build.VERSION.SDK_INT >= 29) {
                ContentValues v = new ContentValues();
                v.put(MediaStore.Downloads.DISPLAY_NAME, fn);
                v.put(MediaStore.Downloads.MIME_TYPE, "application/pdf");
                v.put(MediaStore.Downloads.IS_PENDING, 1);
                u = activity.getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, v);
                if (u != null) {
                    OutputStream o = activity.getContentResolver().openOutputStream(u);
                    if (o != null) { doc.writeTo(o); o.close(); }
                    v.clear();
                    v.put(MediaStore.Downloads.IS_PENDING, 0);
                    activity.getContentResolver().update(u, v, null, null);
                }
            } else {
                File f = new File(activity.getExternalFilesDir(null), fn);
                OutputStream o = new FileOutputStream(f);
                doc.writeTo(o);
                o.close();
                u = Uri.fromFile(f);
            }
            doc.close();
            if (logo1 != null) logo1.recycle();
            if (logo2 != null) logo2.recycle();

            if (u != null) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("application/pdf");
                intent.putExtra(Intent.EXTRA_STREAM, u);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                activity.startActivity(Intent.createChooser(intent, "ارسال / ذخیره PDF"));
            }
        } catch (Exception e) {
            Toast.makeText(activity, "خطا در ساخت PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // ---------------------------------------------------------------- loading helpers

    private static JSONObject loadSpec(Activity a) throws Exception {
        try (InputStream is = a.getAssets().open("form/spec.json"); ByteArrayOutputStream bo = new ByteArrayOutputStream()) {
            byte[] b = new byte[8192]; int n;
            while ((n = is.read(b)) > 0) bo.write(b, 0, n);
            return new JSONObject(new String(bo.toByteArray(), StandardCharsets.UTF_8));
        }
    }

    private static Bitmap loadAsset(Activity a, String path) {
        try (InputStream is = a.getAssets().open(path)) {
            return BitmapFactory.decodeStream(is);
        } catch (Exception e) {
            return null;
        }
    }

    private static String getOrCreateUuid(JSONObject m) {
        String u = m.optString("uuid", "").trim();
        if (u.isEmpty()) {
            u = UUID.randomUUID().toString().toUpperCase(Locale.US);
            try { m.put("uuid", u); } catch (Exception ignored) {}
        }
        return u;
    }

    // ---------------------------------------------------------------- paint helpers

    private static Paint textPaint(float size, int style, int color) {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
        p.setColor(color);
        p.setTextSize(size);
        Typeface tf;
        switch (style) {
            case 1: tf = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); break;
            case 2: tf = Typeface.create(Typeface.SERIF, Typeface.NORMAL); break;
            case 3: tf = Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC); break;
            default: tf = Typeface.DEFAULT;
        }
        p.setTypeface(tf);
        return p;
    }

    private static Paint fill(int rgb) {
        Paint p = new Paint();
        p.setStyle(Paint.Style.FILL);
        p.setColor(0xFF000000 | rgb);
        return p;
    }

    private static void drawRect(Canvas c, JSONArray r, int rgb, float dy) throws Exception {
        c.drawRect((float) r.getDouble(0), (float) r.getDouble(1) + dy, (float) r.getDouble(2), (float) r.getDouble(3) + dy, fill(rgb));
    }

    /** Draws text so that it spans exactly [x0, x1] (same width as in the reference). */
    private static void drawFit(Canvas c, String t, float x0, float x1, float base, float size, int style, int color) {
        if (t == null || t.isEmpty()) return;
        Paint p = textPaint(size, style, color);
        float w = p.measureText(t), target = x1 - x0;
        if (w > 0.1f && target > 0.1f) {
            float sx = target / w;
            if (sx < 0.6f) sx = 0.6f;
            if (sx > 1.4f) sx = 1.4f;
            p.setTextScaleX(sx);
            w = p.measureText(t);
        }
        c.drawText(t, x0 + (target - w) / 2f, base, p);
    }

    private static void drawText(Canvas c, String t, float anchorX, float base, float size, int style, int color, int align) {
        if (t == null || t.isEmpty()) return;
        Paint p = textPaint(size, style, color);
        float w = p.measureText(t);
        float maxW = 400f;
        if (w > maxW) { p.setTextScaleX(maxW / w); w = p.measureText(t); }
        float x = align == 0 ? anchorX - w / 2f : (align > 0 ? anchorX - w : anchorX);   // 0 center, 1 right edge, -1 left edge
        c.drawText(t, x, base, p);
    }

    /** Centered inside a cell, shrinking the font if the text does not fit. */
    private static void drawInCell(Canvas c, String t, float l, float r, float base, float size, int style, int color) {
        if (t == null || t.isEmpty()) return;
        Paint p = textPaint(size, style, color);
        float avail = (r - l) - 6f;
        float w = p.measureText(t);
        while (w > avail && p.getTextSize() > 5f) { p.setTextSize(p.getTextSize() - 0.4f); w = p.measureText(t); }
        c.drawText(t, (l + r) / 2f - w / 2f, base, p);
    }

    private static void drawCheckMark(Canvas c, float cx, float cy, float size) {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(Math.max(0.9f, size * 0.14f));
        p.setStrokeCap(Paint.Cap.ROUND);
        p.setColor(Color.BLACK);
        Path path = new Path();
        path.moveTo(cx - size * 0.38f, cy + size * 0.02f);
        path.lineTo(cx - size * 0.10f, cy + size * 0.32f);
        path.lineTo(cx + size * 0.42f, cy - size * 0.34f);
        c.drawPath(path, p);
    }

    private static void drawBox(Canvas c, float x, float y, float side, boolean checked) {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(0.75f);
        p.setColor(Color.BLACK);
        c.drawRect(x, y, x + side, y + side, p);
        if (checked) drawCheckMark(c, x + side / 2f, y + side / 2f, side * 0.95f);
    }

    /** Wingdings checkbox glyphs from the reference are drawn as vector boxes. */
    private static void drawGlyphBox(Canvas c, JSONObject w, boolean checked) throws Exception {
        float x0 = (float) w.getDouble("x0"), x1 = (float) w.getDouble("x1"), b = (float) w.getDouble("b"), s = (float) w.getDouble("s");
        float side = s * 0.78f;
        float x = (x0 + x1) / 2f - side / 2f;
        float y = b - side * 0.98f;
        drawBox(c, x, y, side, checked);
    }

    // ---------------------------------------------------------------- Code 39 barcode (matches the reference font)

    private static final String C39 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ-. *";
    private static final String[] C39P = {
            "nnnwwnwnn", "wnnwnnnnw", "nnwwnnnnw", "wnwwnnnnn", "nnnwwnnnw", "wnnwwnnnn", "nnwwwnnnn", "nnnwnnwnw", "wnnwnnwnn", "nnwwnnwnn",
            "wnnnnwnnw", "nnwnnwnnw", "wnwnnwnnn", "nnnnwwnnw", "wnnnwwnnn", "nnwnwwnnn", "nnnnnwwnw", "wnnnnwwnn", "nnwnnwwnn", "nnnnwwwnn",
            "wnnnnnnww", "nnwnnnnww", "wnwnnnnwn", "nnnnwnnww", "wnnnwnnwn", "nnwnwnnwn", "nnnnnnwww", "wnnnnnwwn", "nnwnnnwwn", "nnnnwnwwn",
            "wwnnnnnnw", "nwwnnnnnw", "wwwnnnnnn", "nwnnwnnnw", "wwnnwnnnn", "nwwnwnnnn", "nwnnnnwnw", "wwnnnnwnn", "nwwnnnwnn", "nwnnwnwnn"};

    private static void drawBarcode(Canvas c, String code, float cx, float top, float height) {
        String full = "*" + code.toUpperCase(Locale.US) + "*";
        final float N = 0.9f, WIDE = 2.34f;
        float total = 0;
        for (int i = 0; i < full.length(); i++) {
            int idx = C39.indexOf(full.charAt(i));
            if (idx < 0) { full = "*M*"; break; }
        }
        for (int i = 0; i < full.length(); i++) {
            String pat = C39P[C39.indexOf(full.charAt(i))];
            for (int k = 0; k < 9; k++) total += pat.charAt(k) == 'w' ? WIDE : N;
            if (i < full.length() - 1) total += N;
        }
        float x = cx - total / 2f;
        Paint bar = fill(0);
        for (int i = 0; i < full.length(); i++) {
            String pat = C39P[C39.indexOf(full.charAt(i))];
            for (int k = 0; k < 9; k++) {
                float w = pat.charAt(k) == 'w' ? WIDE : N;
                if (k % 2 == 0) c.drawRect(x, top, x + w, top + height, bar);
                x += w;
            }
            x += N;
        }
    }

    // ---------------------------------------------------------------- data helpers

    private static String moneyInt(double x) { return String.format(Locale.US, "%,.0f", x); }

    private static double expenseSum(JSONObject m, String cat) {
        double s = 0;
        JSONArray a = m.optJSONArray("expenses");
        if (a != null) for (int i = 0; i < a.length(); i++) {
            JSONObject o = a.optJSONObject(i);
            if (o != null && (cat == null || cat.equals(o.optString("category")))) s += o.optDouble("amount", 0);
        }
        return s;
    }

    private static double missionPay(JSONObject m) {
        double d = m.optDouble("days", 1), h = m.optDouble("holiday", 0);
        if (m.optBoolean("suburb")) return d * 15000000 + h * 16000000;
        return (d == 1 ? 39000000 : d * 35000000) + h * 16000000;
    }

    private static double reimbursable(JSONObject m, String payerKey, String amountKey) {
        return "شخص".equals(m.optString(payerKey, "شخص")) ? m.optDouble(amountKey, 0) : 0;
    }

    private static double total(JSONObject m) {
        return missionPay(m) + reimbursable(m, "hotelPayer", "hotel") + reimbursable(m, "goPayer", "ticketGo")
                + reimbursable(m, "backPayer", "ticketBack") + reimbursable(m, "cancelGoPayer", "cancelGo")
                + reimbursable(m, "cancelBackPayer", "cancelBack") + expenseSum(m, null);
    }

    /** yyyy/MM/dd (as stored by the app) -> dd/MM/yyyy (as printed by the Excel form). */
    private static String shownDate(String s) {
        if (s == null) return "";
        s = s.trim();
        String[] p = s.replace("-", "/").split("/");
        if (p.length == 3 && p[0].length() == 4) return p[2] + "/" + p[1] + "/" + p[0];
        return s;
    }

    private static String num(double v) {
        if (v == Math.floor(v)) return String.format(Locale.US, "%.1f", v);
        return String.format(Locale.US, "%.1f", v);
    }

    private static String todayJalali() {
        Calendar c = Calendar.getInstance();
        int[] j = gregorianToJalali(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH));
        return String.format(Locale.US, "%04d/%02d/%02d", j[0], j[1], j[2]);
    }

    private static int[] gregorianToJalali(int gy, int gm, int gd) {
        int[] gdm = {0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
        int gy2 = gy - 1600, gm2 = gm - 1, gd2 = gd - 1;
        int gdn = 365 * gy2 + (gy2 + 3) / 4 - (gy2 + 99) / 100 + (gy2 + 399) / 400;
        for (int i = 0; i < gm2; i++) gdn += gdm[i];
        if (gm2 > 1 && ((gy % 4 == 0 && gy % 100 != 0) || gy % 400 == 0)) gdn++;
        gdn += gd2;
        int jdn = gdn - 79;
        int jnp = jdn / 12053;
        jdn %= 12053;
        int jy = 979 + 33 * jnp + 4 * (jdn / 1461);
        jdn %= 1461;
        if (jdn >= 366) { jy += (jdn - 1) / 365; jdn = (jdn - 1) % 365; }
        int jm = jdn < 186 ? 1 + jdn / 31 : 7 + (jdn - 186) / 30;
        int jd = 1 + (jdn % (jdn < 186 ? 31 : 30));
        return new int[]{jy, jm, jd};
    }

    private static String amountWords(double v) { return words(Math.round(v)); }

    private static String words(long n) {
        String[] u = {"صفر","یک","دو","سه","چهار","پنج","شش","هفت","هشت","نه","ده","یازده","دوازده","سیزده","چهارده","پانزده","شانزده","هفده","هجده","نوزده","بیست","بیست و یک","بیست و دو","بیست و سه","بیست و چهار","بیست و پنج","بیست و شش","بیست و هفت","بیست و هشت","بیست و نه"};
        String[] t = {"","","بیست","سی","چهل","پنجاه","شصت","هفتاد","هشتاد","نود"};
        String[] h = {"","یکصد","دویست","سیصد","چهارصد","پانصد","ششصد","هفتصد","هشتصد","نهصد"};
        if (n < 30) return u[(int) n];
        if (n < 100) return t[(int) n / 10] + (n % 10 > 0 ? " و " + u[(int) (n % 10)] : "");
        if (n < 1000) return h[(int) n / 100] + (n % 100 > 0 ? " و " + words(n % 100) : "");
        if (n < 1000000) return words(n / 1000) + " هزار" + (n % 1000 > 0 ? " و " + words(n % 1000) : "");
        if (n < 1000000000) return words(n / 1000000) + " میلیون" + (n % 1000000 > 0 ? " و " + words(n % 1000000) : "");
        return words(n / 1000000000) + " میلیارد" + (n % 1000000000 > 0 ? " و " + words(n % 1000000000) : "");
    }

    private static boolean has(String s) { return s != null && !s.trim().isEmpty(); }

    // ---------------------------------------------------------------- page 1: mission form

    private static void drawPage1(Canvas c, JSONObject page, JSONObject m, Bitmap logo, String uuid, String today) throws Exception {
        String formNo = m.optString("form", "").trim();
        String service = m.optString("service").trim();
        String start = shownDate(m.optString("start")), end = shownDate(m.optString("end"));
        String person = m.optString("person").trim(), subject = m.optString("subject").trim();
        String device = m.optString("device").trim(), dest = m.optString("destination").trim();
        String goNo = m.optString("goNo").trim(), backNo = m.optString("backNo").trim();
        String sTime = m.optString("startTime").trim(), eTime = m.optString("endTime").trim();

        double groundExp = expenseSum(m, "هزینه تردد بین شهری");
        double tGo = m.optDouble("ticketGo", 0), tBack = m.optDouble("ticketBack", 0);
        double cGo = m.optDouble("cancelGo", 0), cBack = m.optDouble("cancelBack", 0);
        double hotelExp = m.optDouble("hotel", 0);
        double cityExp = expenseSum(m, "هزینه تردد درون شهری") + expenseSum(m, "هزینه شهر تهران");
        double otherExp = expenseSum(m, "سایر هزینه ها");

        // which "payer" cells are ticked: rows 0..4 = ground, air go, air back, cancel go, cancel back
        String[] payer = {"شخص", m.optString("goPayer"), m.optString("backPayer"), m.optString("cancelGoPayer"), m.optString("cancelBackPayer")};
        boolean[] active = {groundExp > 0, tGo > 0, tBack > 0, cGo > 0, cBack > 0};
        Map<String, Boolean> filled = new HashMap<>();
        filled.put("formNo", has(formNo)); filled.put("service", has(service)); filled.put("start", has(start)); filled.put("end", has(end));
        filled.put("person", has(person)); filled.put("subject", has(subject)); filled.put("device", has(device)); filled.put("dest", has(dest));
        filled.put("goNo", has(goNo)); filled.put("backNo", has(backNo)); filled.put("startTime", has(sTime)); filled.put("endTime", has(eTime));
        filled.put("cost1", tGo > 0); filled.put("cost2", tBack > 0); filled.put("cost3", cGo > 0); filled.put("cost4", cBack > 0);
        for (int r = 0; r < 5; r++) {
            filled.put("pK" + r, active[r] && "شخص".equals(payer[r]));
            filled.put("pC" + r, active[r] && "شرکت".equals(payer[r]));
        }

        JSONArray rects = page.getJSONArray("rects");
        for (int i = 0; i < rects.length(); i++) {
            JSONObject r = rects.getJSONObject(i);
            String k = r.optString("k", "");
            if (!k.isEmpty() && Boolean.TRUE.equals(filled.get(k))) continue; // filled cells are white instead of gray/red
            drawRect(c, r.getJSONArray("r"), r.getInt("c"), 0);
        }

        // logo + barcode
        if (logo != null) c.drawBitmap(logo, null, new RectF(399.72f, 64.92f, 569.04f, 106.56f), new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
        drawBarcode(c, "M" + formNo, 316.7f, 82.68f, 32f);

        // static words and value placeholders
        Map<String, String> vals = new HashMap<>();
        vals.put("gAmt", moneyInt(groundExp));
        vals.put("hotelAmt", moneyInt(hotelExp > 0 ? hotelExp : 0));
        vals.put("cityAmt", moneyInt(cityExp));
        vals.put("payAmt", moneyInt(missionPay(m)));
        vals.put("miscAmt", moneyInt(otherExp));
        vals.put("total", moneyInt(total(m)));
        vals.put("days", num(m.optDouble("days", 1)));
        vals.put("holidays", num(m.optDouble("holiday", 0)));
        vals.put("today", today);
        vals.put("uuid", uuid);
        drawWords(c, page.getJSONArray("words"), vals, 0, null, null);

        // header values (centered in their cells)
        float b1 = 147.8f, b2 = 162.8f;
        drawInCell(c, service, 276.12f, 525.24f, 146.6f, 10.5f, 0, Color.BLACK);
        drawInCell(c, start, 459.96f, 525.24f, 162.0f, 10.5f, 0, Color.BLACK);
        drawInCell(c, end, 330.72f, 400.80f, 162.0f, 10.5f, 0, Color.BLACK);
        drawInCell(c, person, 276.12f, 525.24f, 177.3f, 10.5f, 0, Color.BLACK);
        drawInCell(c, subject, 276.12f, 525.24f, 192.0f, 10.5f, 0, Color.BLACK);
        drawInCell(c, device, 33.36f, 233.40f, 177.3f, 10.5f, 0, Color.BLACK);
        drawInCell(c, dest, 33.36f, 233.40f, 192.0f, 10.5f, 0, Color.BLACK);
        drawInCell(c, formNo, 400.68f, 460.08f, 130.8f, 10.5f, 0, Color.BLACK);

        // transport type checkboxes (7.08 pt squares in the reference)
        drawBox(c, 505.68f, 214.8f, 7.08f, groundExp > 0 || m.optBoolean("ground"));
        drawBox(c, 505.68f, 236.28f, 7.08f, tGo > 0 || tBack > 0 || m.optBoolean("air"));
        drawBox(c, 505.68f, 264.72f, 7.08f, cGo > 0 || cBack > 0 || m.optBoolean("cancel"));

        // payer ticks
        float[] rowMid = {219.0f, 233.6f, 248.3f, 263.5f, 278.6f};
        for (int r = 0; r < 5; r++) {
            if (Boolean.TRUE.equals(filled.get("pK" + r))) drawCheckMark(c, 48.4f, rowMid[r], 9f);
            if (Boolean.TRUE.equals(filled.get("pC" + r))) drawCheckMark(c, 109.0f, rowMid[r], 9f);
        }
        if (hotelExp > 0) {
            String hp = m.optString("hotelPayer", "شخص");
            drawCheckMark(c, "شرکت".equals(hp) ? 109.0f : 48.4f, 356.5f, 9f);
        }

        // ticket numbers + ticket costs
        drawInCell(c, goNo, 146.88f, 233.40f, 237.2f, 11f, 0, Color.BLACK);
        drawInCell(c, backNo, 146.88f, 233.40f, 251.8f, 11f, 0, Color.BLACK);
        double[] cost = {tGo, tBack, cGo, cBack};
        float[] costBase = {237.2f, 251.8f, 267.6f, 282.6f};
        for (int i = 0; i < 4; i++) if (cost[i] > 0) drawInCell(c, "ريال " + moneyInt(cost[i]), 276.12f, 400.80f, costBase[i], 10.5f, 0, Color.BLACK);

        // timing table
        drawInCell(c, start, 459.96f, 525.24f, 313.4f, 10.5f, 0, Color.BLACK);
        drawInCell(c, end, 146.88f, 233.40f, 313.4f, 10.5f, 0, Color.BLACK);
        drawInCell(c, sTime, 459.96f, 525.24f, 328.6f, 10.5f, 0, Color.BLACK);
        drawInCell(c, eTime, 146.88f, 233.40f, 328.6f, 10.5f, 0, Color.BLACK);

        if (m.optBoolean("suburb")) drawCheckMark(c, 70f, 387.5f, 9f);
    }

    /**
     * Draws the static words of a page. Words with a key in {@code vals} are placeholders in the
     * reference (e.g. "0") and are replaced by the real value, right-aligned to the placeholder.
     */
    private static void drawWords(Canvas c, JSONArray words, Map<String, String> vals, float dyAll, float[] cutsY, float[] shifts) throws Exception {
        for (int i = 0; i < words.length(); i++) {
            JSONObject w = words.getJSONObject(i);
            float x0 = (float) w.getDouble("x0"), x1 = (float) w.getDouble("x1"), b = (float) w.getDouble("b") + dyAll, s = (float) w.getDouble("s");
            int st = w.optInt("st", 0);
            int color = w.optInt("white", 0) == 1 ? Color.WHITE : Color.BLACK;
            String key = w.optString("k", "");
            if (w.has("g")) { drawGlyphBox(c, w, false); continue; }
            if (!key.isEmpty() && vals.containsKey(key)) {
                String v = vals.get(key);
                if (key.equals("today") || key.equals("uuid") || key.equals("days") || key.equals("holidays")) {
                    drawText(c, v, x1, b, s, st, color, 1);
                } else {
                    drawText(c, v, x1, b, s, st, color, 1);
                }
                continue;
            }
            drawFit(c, w.getString("t"), x0, x1, b, s, st, color);
        }
    }

    // ---------------------------------------------------------------- page 2: petty cash expense statement

    private static void drawPage2(Canvas c, JSONObject page, JSONObject m, Bitmap logo, String uuid, String today) throws Exception {
        String[] keys = {"هزینه شهر تهران", "هزینه تردد بین شهری", "هزینه تردد درون شهری", "سایر هزینه ها"};
        List<List<JSONObject>> items = new ArrayList<>();
        JSONArray all = m.optJSONArray("expenses");
        for (int g = 0; g < 4; g++) {
            List<JSONObject> l = new ArrayList<>();
            if (all != null) for (int i = 0; i < all.length(); i++) {
                JSONObject o = all.optJSONObject(i);
                if (o != null && keys[g].equals(o.optString("category"))) l.add(o);
            }
            items.add(l);
        }
        int[] rowsN = new int[4];
        float[] extra = new float[4];
        for (int g = 0; g < 4; g++) { rowsN[g] = Math.max(1, items.get(g).size()); extra[g] = (rowsN[g] - 1) * ROW_H; }
        float[] cum = new float[4];            // cumulative shift applied after section g's first row
        float acc = 0;
        for (int g = 0; g < 4; g++) { acc += extra[g]; cum[g] = acc; }

        // y mapping: anything at/after CUT[g] is pushed down by the extra rows of sections 0..g
        JSONArray rects = page.getJSONArray("rects");
        for (int i = 0; i < rects.length(); i++) {
            JSONObject r = rects.getJSONObject(i);
            JSONArray a = r.getJSONArray("r");
            float y0 = (float) a.getDouble(1), y1 = (float) a.getDouble(3);
            float ny0 = mapY(y0, cum), ny1 = mapY(y1, cum);
            c.drawRect((float) a.getDouble(0), ny0, (float) a.getDouble(2), ny1, fill(r.getInt("c")));
        }
        // repeat the horizontal row separator + vertical column lines for the extra rows
        for (int g = 0; g < 4; g++) {
            for (int j = 0; j < rowsN[g] - 1; j++) {
                float y = CUT[g] + (g > 0 ? cumBefore(g, cum) : 0) + j * ROW_H;
                float yt = y;
                float left = g == 3 ? 10.56f : 9.12f, right = g == 3 ? 601.56f : 602.04f;
                c.drawRect(left, yt, right, yt + 0.96f, fill(0));
            }
        }

        if (logo != null) c.drawBitmap(logo, null, new RectF(50.4f, 33.36f, 170.28f, 61.92f), new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));

        // words
        Map<String, String> sums = new HashMap<>();
        String person = m.optString("person").trim();
        double[] secTotals = new double[4];
        for (int g = 0; g < 4; g++) secTotals[g] = expenseSum(m, keys[g]);
        JSONArray words = page.getJSONArray("words");
        for (int i = 0; i < words.length(); i++) {
            JSONObject w = words.getJSONObject(i);
            float x0 = (float) w.getDouble("x0"), x1 = (float) w.getDouble("x1"), s = (float) w.getDouble("s");
            float b = mapY((float) w.getDouble("b") - s * 0.6f, cum) + s * 0.6f;
            int st = w.optInt("st", 0);
            int color = w.optInt("white", 0) == 1 ? Color.WHITE : Color.BLACK;
            String k = w.optString("k", "");
            if (k.equals("today")) { drawText(c, today, x1, b, s, st, color, 1); continue; }
            if (k.equals("uuid")) { drawText(c, uuid, x1, b, s, st, color, 1); continue; }
            if (k.equals("person")) { drawText(c, person, x1, b, s, st, color, 1); continue; }
            if (k.equals("totalAll")) { drawText(c, "ريال " + moneyInt(expenseSum(m, null)), (x0 + x1) / 2f, b, s, st, color, 0); continue; }
            if (k.length() == 2 && k.charAt(0) == 's') {
                int g = k.charAt(1) - '1';
                drawText(c, "ريال " + moneyInt(secTotals[g]), (x0 + x1) / 2f, b, s, st, color, 0);
                continue;
            }
            drawFit(c, w.getString("t"), x0, x1, b, s, st, color);
        }

        // data rows: columns (right to left) date | description | invoice | amount
        final float[][] cols = {{510.24f, 602.88f}, {238.56f, 509.28f}, {181.68f, 237.6f}, {10.92f, 180.72f}};
        for (int g = 0; g < 4; g++) {
            float shiftBefore = g == 0 ? 0 : cum[g - 1];
            float rowTop = 129.36f + g * 85.92f + shiftBefore;
            List<JSONObject> l = items.get(g);
            for (int j = 0; j < l.size(); j++) {
                JSONObject it = l.get(j);
                String desc = it.optString("desc");
                if (desc.isEmpty()) desc = it.optString("tehranItem");
                String[] vals = {shownDate(it.optString("date")), desc, it.optString("invoice"), "ريال " + moneyInt(it.optDouble("amount"))};
                float top = rowTop + j * ROW_H;
                float base = top + ROW_H / 2f + 3.4f;
                for (int k = 0; k < 4; k++) drawInCell(c, vals[k], cols[k][0], cols[k][1], base, 9.9f, 0, Color.BLACK);
            }
        }
    }

    private static float cumBefore(int g, float[] cum) { return cum[g - 1]; }

    private static float mapY(float y, float[] cum) {
        float shift = 0;
        for (int g = 0; g < 4; g++) if (y >= CUT[g] - 0.001f) shift = cum[g];
        return y + shift;
    }

    // ---------------------------------------------------------------- page 3: payment request

    private static void drawPage3(Canvas c, JSONObject page, JSONObject m, String uuid, String today) throws Exception {
        JSONArray rects = page.getJSONArray("rects");
        for (int i = 0; i < rects.length(); i++) {
            JSONObject r = rects.getJSONObject(i);
            drawRect(c, r.getJSONArray("r"), r.getInt("c"), 0);
        }
        String person = m.optString("person").trim();
        String dest = m.optString("destination").trim();
        double tot = total(m);
        String formNo = m.optString("form", "").trim();

        JSONArray words = page.getJSONArray("words");
        for (int i = 0; i < words.length(); i++) {
            JSONObject w = words.getJSONObject(i);
            float x0 = (float) w.getDouble("x0"), x1 = (float) w.getDouble("x1"), b = (float) w.getDouble("b"), s = (float) w.getDouble("s");
            int st = w.optInt("st", 0);
            String k = w.optString("k", "");
            if (w.has("g")) { drawGlyphBox(c, w, "chk".equals(w.getString("g"))); continue; }
            switch (k) {
                case "amtNum": drawText(c, String.format(Locale.US, "%.0f", tot), x1, b, s, st, Color.BLACK, 1); continue;
                case "amtWords": drawText(c, amountWords(tot), x0, b, s, st, Color.BLACK, -1); continue;
                case "person": case "person2": drawText(c, person, x1, b, s, st, Color.BLACK, 1); continue;
                case "dest": drawText(c, dest, x1, b, s, st, Color.BLACK, 1); continue;
                case "formM": drawText(c, "M" + formNo, x0, b, s, st, Color.BLACK, -1); continue;
                case "today": drawText(c, today, x1, b, s, st, Color.BLACK, 1); continue;
                case "uuid": drawText(c, uuid, x1, b, s, st, Color.BLACK, 1); continue;
                default: break;
            }
            drawFit(c, w.getString("t"), x0, x1, b, s, st, Color.BLACK);
        }
    }
}
