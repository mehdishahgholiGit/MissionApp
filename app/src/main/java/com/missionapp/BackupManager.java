package com.missionapp;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.DocumentsContract;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Backup file helpers built on the Storage Access Framework.
 * The user picks a folder once; the app writes MissionApp-backup.json there.
 * Because the file lives outside the app's private storage, it survives uninstall.
 */
final class BackupManager {
    static final String FILE_NAME = "MissionApp-backup.json";
    static final String PREF_TREE = "backupTree";
    static final String PREF_LAST = "backupLast";
    static final String PREF_ERR = "backupErr";
    static final String PREF_ASKED = "restoreAsked";

    private BackupManager() {}

    static String buildJson(List<JSONObject> data) {
        try {
            JSONArray a = new JSONArray();
            for (JSONObject m : data) a.put(m);
            JSONObject o = new JSONObject();
            o.put("app", "MissionApp");
            o.put("version", 1);
            o.put("savedAt", System.currentTimeMillis());
            o.put("count", data.size());
            o.put("missions", a);
            return o.toString(2);
        } catch (Exception e) {
            return null;
        }
    }

    /** Accepts {"missions":[...]} or a bare [...] array. Returns null if the text is not a valid backup. */
    static JSONArray parse(String text) {
        if (text == null) return null;
        try {
            String t = text.trim();
            JSONArray a;
            if (t.startsWith("[")) {
                a = new JSONArray(t);
            } else {
                a = new JSONObject(t).optJSONArray("missions");
            }
            if (a == null) return null;
            for (int i = 0; i < a.length(); i++) if (a.optJSONObject(i) == null) return null;
            return a;
        } catch (Exception e) {
            return null;
        }
    }

    static Uri findBackup(Context c, Uri tree) {
        Cursor cur = null;
        try {
            String id = DocumentsContract.getTreeDocumentId(tree);
            Uri children = DocumentsContract.buildChildDocumentsUriUsingTree(tree, id);
            cur = c.getContentResolver().query(children,
                    new String[]{DocumentsContract.Document.COLUMN_DOCUMENT_ID, DocumentsContract.Document.COLUMN_DISPLAY_NAME},
                    null, null, null);
            while (cur != null && cur.moveToNext()) {
                if (FILE_NAME.equals(cur.getString(1))) {
                    return DocumentsContract.buildDocumentUriUsingTree(tree, cur.getString(0));
                }
            }
        } catch (Exception ignored) {
        } finally {
            if (cur != null) cur.close();
        }
        return null;
    }

    static synchronized boolean write(Context c, Uri tree, String json) {
        if (json == null) return false;
        try {
            Uri doc = findBackup(c, tree);
            if (doc == null) {
                String id = DocumentsContract.getTreeDocumentId(tree);
                Uri parent = DocumentsContract.buildDocumentUriUsingTree(tree, id);
                doc = DocumentsContract.createDocument(c.getContentResolver(), parent, "application/json", FILE_NAME);
            }
            if (doc == null) return false;
            try (OutputStream os = c.getContentResolver().openOutputStream(doc, "wt")) {
                if (os == null) return false;
                os.write(json.getBytes(StandardCharsets.UTF_8));
                os.flush();
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    static String read(Context c, Uri doc) {
        try (InputStream is = c.getContentResolver().openInputStream(doc);
             ByteArrayOutputStream bo = new ByteArrayOutputStream()) {
            if (is == null) return null;
            byte[] b = new byte[8192];
            int n;
            while ((n = is.read(b)) > 0) bo.write(b, 0, n);
            String s = new String(bo.toByteArray(), StandardCharsets.UTF_8);
            if (s.startsWith("\uFEFF")) s = s.substring(1);
            return s;
        } catch (Exception e) {
            return null;
        }
    }

    static synchronized boolean deleteBackup(Context c, Uri tree) {
        try {
            Uri d = findBackup(c, tree);
            return d != null && DocumentsContract.deleteDocument(c.getContentResolver(), d);
        } catch (Exception e) {
            return false;
        }
    }
}
