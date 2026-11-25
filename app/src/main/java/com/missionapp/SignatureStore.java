package com.missionapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.security.MessageDigest;

/** Per-person signature images kept in app storage (transparent PNG). */
final class SignatureStore {
    private SignatureStore() {}

    static String key(String person) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] d = md.digest(person.trim().replaceAll("\\s+", " ").getBytes("UTF-8"));
            StringBuilder b = new StringBuilder();
            for (byte x : d) b.append(String.format("%02x", x));
            return b.toString();
        } catch (Exception e) {
            return Integer.toHexString(person.hashCode());
        }
    }

    static File file(Context c, String person) {
        File dir = new File(c.getFilesDir(), "sig");
        dir.mkdirs();
        return new File(dir, key(person) + ".png");
    }

    static boolean has(Context c, String person) {
        return person != null && !person.trim().isEmpty() && file(c, person).exists();
    }

    static Bitmap load(Context c, String person) {
        if (!has(c, person)) return null;
        try { return BitmapFactory.decodeFile(file(c, person).getAbsolutePath()); } catch (Throwable e) { return null; }
    }

    static void delete(Context c, String person) {
        if (person != null && !person.trim().isEmpty()) file(c, person).delete();
    }

    static boolean save(Context c, String person, Bitmap bmp) {
        try (OutputStream os = new FileOutputStream(file(c, person))) {
            return bmp.compress(Bitmap.CompressFormat.PNG, 100, os);
        } catch (Exception e) {
            return false;
        }
    }

    static boolean enabled(Context c) { return c.getSharedPreferences("mission_settings", 0).getBoolean("showSig", true); }
    static void setEnabled(Context c, boolean v) { c.getSharedPreferences("mission_settings", 0).edit().putBoolean("showSig", v).apply(); }

    /**
     * Photo of a signature on paper -> bitmap where the paper is transparent and only the ink remains,
     * cropped to the ink and scaled to at most 900 px wide. The paper brightness is estimated per block
     * and smoothed, so shadows and uneven lighting do not turn into "ink". Returns null if no ink was found.
     */
    static Bitmap process(Bitmap src) {
        final int w = src.getWidth(), h = src.getHeight();
        int[] px = new int[w * h];
        src.getPixels(px, 0, w, 0, 0, w, h);
        int[] lum = new int[w * h];
        for (int i = 0; i < px.length; i++) {
            int p = px[i];
            lum[i] = (((p >> 16) & 255) * 299 + ((p >> 8) & 255) * 587 + (p & 255) * 114) / 1000;
        }
        // local paper level: 85th percentile of every block
        final int cell = Math.max(24, Math.max(w, h) / 24);
        final int gw = (w + cell - 1) / cell, gh = (h + cell - 1) / cell;
        float[] bg = new float[gw * gh];
        int[] hist = new int[256];
        for (int gy = 0; gy < gh; gy++) {
            for (int gx = 0; gx < gw; gx++) {
                java.util.Arrays.fill(hist, 0);
                int x0 = gx * cell, y0 = gy * cell, x1 = Math.min(w, x0 + cell), y1 = Math.min(h, y0 + cell), cnt = 0;
                for (int y = y0; y < y1; y++) for (int x = x0; x < x1; x++) { hist[lum[y * w + x]]++; cnt++; }
                int target = (int) (cnt * 0.85), acc = 0, v = 255;
                for (int i = 0; i < 256; i++) { acc += hist[i]; if (acc >= target) { v = i; break; } }
                bg[gy * gw + gx] = Math.max(60, v);
            }
        }
        int minX = w, minY = h, maxX = -1, maxY = -1;
        int[] out = new int[px.length];
        for (int y = 0; y < h; y++) {
            float fy = (y + 0.5f) / cell - 0.5f;
            int gy0 = Math.max(0, Math.min(gh - 1, (int) Math.floor(fy))), gy1 = Math.min(gh - 1, gy0 + 1);
            float ty = Math.max(0f, Math.min(1f, fy - gy0));
            for (int x = 0; x < w; x++) {
                float fx = (x + 0.5f) / cell - 0.5f;
                int gx0 = Math.max(0, Math.min(gw - 1, (int) Math.floor(fx))), gx1 = Math.min(gw - 1, gx0 + 1);
                float tx = Math.max(0f, Math.min(1f, fx - gx0));
                float top = bg[gy0 * gw + gx0] * (1 - tx) + bg[gy0 * gw + gx1] * tx;
                float bot = bg[gy1 * gw + gx0] * (1 - tx) + bg[gy1 * gw + gx1] * tx;
                float paper = top * (1 - ty) + bot * ty;
                int i = y * w + x, p = px[i];
                float n = lum[i] / Math.max(1f, paper);
                float a = (0.90f - n) / (0.90f - 0.55f);
                a = Math.max(0f, Math.min(1f, a));
                int al = (int) (a * 255);
                if (al < 30) { out[i] = 0; continue; }
                int r = (int) (((p >> 16) & 255) * 0.55f), g = (int) (((p >> 8) & 255) * 0.55f), b = (int) ((p & 255) * 0.55f);
                out[i] = (al << 24) | (r << 16) | (g << 8) | b;
                if (x < minX) minX = x;
                if (x > maxX) maxX = x;
                if (y < minY) minY = y;
                if (y > maxY) maxY = y;
            }
        }
        if (maxX < 0) return null;
        int pad = Math.max(4, Math.min(w, h) / 80);
        minX = Math.max(0, minX - pad); minY = Math.max(0, minY - pad);
        maxX = Math.min(w - 1, maxX + pad); maxY = Math.min(h - 1, maxY + pad);
        Bitmap full = Bitmap.createBitmap(out, w, h, Bitmap.Config.ARGB_8888);
        Bitmap crop = Bitmap.createBitmap(full, minX, minY, maxX - minX + 1, maxY - minY + 1);
        if (crop != full) full.recycle();
        if (crop.getWidth() > 900) {
            int nh = Math.max(1, Math.round(crop.getHeight() * 900f / crop.getWidth()));
            Bitmap sc = Bitmap.createScaledBitmap(crop, 900, nh, true);
            crop.recycle();
            return sc;
        }
        return crop;
    }
}
