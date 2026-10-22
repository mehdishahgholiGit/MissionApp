package com.missionapp;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.*;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Base64;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;

public class PdfGenerator {

    private static final String LOGO_PNG =
            "iVBORw0KGgoAAAANSUhEUgAAAMMAAAA0CAIAAAC4ihtcAAATIklEQVR42u1ceVQTV9vPJCGQsAoJixhQ" +
            "kE1ZREyqrAqyiUjdKrXaY6t1qQsquLS+HA+KiOLRuqBYwVZcip5IXRBIDVWUIrIGAmgCEkDCGgghe0gy" +
            "3x/znZy8YTGg7ft9vPP7a2by3Dszd3732e5zA1haWiJgwPhooOfMmQOPAoyPB9DY2AiPAoxPwCQQBOFR" +
            "gPHxQMJDAANmEgyYSTBgJsGAATMJBswkGP/ngYaHYMoABEEul1teXj40NDSOmJeXF5SOFgqFr1694wij" +
            "UKgFCxbY29ujUCiYSVMfCoWCy+W+ePEiLy+voqJCIBCMI3zw4EGISf39/ZcuXaqqqvogk4KCgmJjY8df" +
            "WIOZNBVUUWVlZVZWVlVVFYfD+aC8SqXSpODw8PA4wsPDwyUlJfX19Xw+Pyoqys3NDYvFwkyaglAqlXQ6" +
            "PTU1taKiQqVSOTg4BAUFmZqajir59OnTt2/fal03MDAIDw+fNWvWWP0XFxc3NDRcv36dwWCkpKTMnj0b" +
            "ZtIUBJvNTktLKysrA0EwLCxs165d7u7uhoaGIyVlMllHR8dIJmEwmDVr1oSFhY2l8AICAs6ePVtRUVFb" +
            "W3vr1q2dO3cSCASYSVMKb968yc/PZzAYIAiGhIQkJyfb29uPtGX19fXNzc0KheL9+/ej+lilpaVCoRCH" +
            "w/n4+GixBAAAX19fBAJx4sSJ2trae/fukUikqKgomElTCnl5eRcuXIAcnYCAgJE0YrFYDx48KCoqglTR" +
            "qC6RWCzOyspCIpE4HG7hwoURERGLFy+2sLBAo/+XG3p6eoGBgXQ6nclk8vl8Docjl8sxGAzMpCkVssnl" +
            "cugYify31CCfz6+vr79z505+fr5YLIYEnJycCAQCkUhUe0ienp5oNJrFYg0MDEil0vz8fDqdTqPRvv76" +
            "az8/P3WfAACEhoYWFBTQ6XQqlRoUFOTi4gIzaQrC1dV18eLF6lMej3fr1q2bN2/29PRIpVJTU9Pg4GBb" +
            "W9vg4GAnJycMBiMSiRAIhJGRUXx8vEQiKSoqamlpqaioaGxs7Orqys/P7+zsTExMJJPJajJZW1ubmpqC" +
            "INjR0SEUCmHrNjVhbW2tVhIgCFIolIyMDCjlaGJismnTpg0bNkilUhQKZWJiUlBQ8PjxYwQCYWlp+d13" +
            "382aNSs4OJhMJoeGhtbX12dnZ7e0tFRWViYnJx86dGjRokVqMzc+YCZNNXA4nGfPnkE0MjU1XbNmzaJF" +
            "iwoKCvLz89Fo9LJly0pLS588eYJAIAgEgo2NDZFIfPLkCZvNXr58+dq1a42Njc+cOdPV1VVTU3Pjxg0X" +
            "FxcdK/1hJk01z+nly5dlZWUIBMLX13fdunWLFy/Oysq6du2aVCoFAKCqqkqhUEDC/f39586dAwBAJpMp" +
            "lUqRSOTv77969WoQBC9fvsxms6uqqp49e7Zu3Tpdbg2v4E4ptLW1PXr0SCKRmJqaRkZGxsbGWltby+Vy" +
            "iUQCgqBKpZJIJOoIDjoVi8VKpRJioVKpxGKxq1at2rdvH5FI7OzsLCws7Ovrg5n03wW5XF5SUsJgMLBY" +
            "7Nq1ayMjI2UyWXNzM5PJ1KW5SCSqrq7m8/l6enpRUVHBwcFIJLKsrGz8hTnYuk1BSKVSGo3G5XLnzp27" +
            "Zs0aJpOZkpLS2dmpIxV4PF5GRkZlZeWMGTPWrl27fPnyV69etbW1QUkEmEn/Fejv729raxMKhX19fQAA" +
            "hIWF4fH4nJyc3NzcCfXT29ubn5+PxWIVCsX27dsJBAKbzX737t3Q0FB7e3t/fz/MpCmON2/eHDlyRC6X" +
            "19fXIxAIKysrDodTVFQ0ud4kEklPTw8Oh/P392cwGLdv3+7u7m5tbX3z5g3MpKkJLy+vBQsWqG0TdAUA" +
            "AEtLSysrKw8PDysrq8n17OjoaGxsHBoaWltb29fXBy22zJs3D4FA2NjY4HC4kU3gnZP/v2P+UQuSzM3N" +
            "cThcV1cXFJRNAjgcDo/Hy+Xyvr4+rU7QaDQej9fX1/8P6ySoQnTUsoR/Zui5XK5MJoNOLSwsjIyM/qZ7" +
            "cblcaFEC+jB/xyuPumoB6acbN27o6elt2LBh1AqT8TE8PNzX19fR0WFsbKx7q3+OSUKhsLKysr29vaOj" +
            "48cff/znadTd3f38+fPc3FzoAwMAEBUVtX79+lHrwj4GQ0NDDAYjMzOzt7cXuuLm5rZnzx47O7tPe6PH" +
            "jx/n5ORoWxkAWLFiBY1Gq6+vd3d39/Pzm2i3HA4nLS2Nz+dHRkY+ffpUK59kY2MTHx/v5ub2zzFJIBCA" +
            "IGhkZMRisYqLixkMhlAotLGxKS0tjY6O9vDw0BSWyWSFhYWtra1Llizx9PT84KSRSqUgCOrr649Us1oq" +
            "UCwWo9HogYGBhISE8vJysViMxWIBAAAAQKlU+vv7az3JR0IikVy8ePHOnTs8Hs/AwAAqpG9vbw8MDJwok" +
            "x4/fvzu3bv58+f7+/trLfVD6OjoqKys1LqIRCL9/PwMDQ1VKtXg4ODkXqGxsVEgECxYsIDBYHR0dGj+On" +
            "PmzFHzAn8Xk5RKZUJCglgsTk5ObmhoKCsrc3Jyam9vLy0tZTKZdDp9JJMoFAqNRjM1Nf0gk169epWVla" +
            "VSqWJiYtasWTMOjcrLy2/cuOHj4+Ph4SEQCMzMzJYsWUIikeRyuZ2dnbW19aelEcTy3t5eNBrt6+s7f/5" +
            "8PT09PB5PJBI11+p1RG5ubn5+fnh4uLu7u7m5ue4NbWxs3N3dJ/cvNCAIdnd3S6XSoKAgExMTddXKf8y" +
            "6gSD47NkzPp9/6NAhBAJRXV395s0bqMjBxcUFigI00xgvXryQSCTOzs4FBQWOjo5kMnkcZQOFuMPDw+P" +
            "/+dPw8PD9+/fv379Pp9NPnTq1Y8cOuVxOJBJ/+uknPT295cuXjywN+3gYGRlt3LiRRCL5+vrev3//4cOH" +
            "MTEx33zzzSS6WrRo0bRp02JiYiZkf0EQ/PPPP1evXg0AQFFRkZ+fn5mZ2YQUUl5eXldXl4WFxatXr6CQ" +
            "UBd8stUSJpPZ3t4+jo/CZrPFYvGOHTuuXr3q7u6u+evbt2+Tk5MFAkF4eHh5eXl6evr4O7Z0nSVotJ+f" +
            "n7e398KFCx0cHMLDw6Ojo4VCYVVV1fv373WfbRMCEon08fH58ssvHRwchEJhU1OT2luaEPr6+ubOnbty" +
            "5Up9ff2mpqYJMammpkYmk/n7+9+7dy89PX1Cb/r69euXL186OjpOnz69tbV1/J0nn1InUalUOp0OqWJb" +
            "W9uUlBRXV1ctGUtLS0tLS2hAIyIiRgrY29vb2tpaW1uTSKTCwkJra2s9Pb0JPYZMJqPRaCwWKyoqytnZ" +
            "Wf1Rw8LCHBwczM3NbW1toYsqlUrHxIdAIPjtt9+gSTl//vwlS5aMVanT0NDw9OlTmUwWHh4+b948AAA+" +
            "uM9Q/dgXLlwwMzNbv369ZpA1NDS0d+9eGo0Gnc6aNevs2bNQPbUu6O3tvXv37oYNG4qKigoLCxcvXqyj" +
            "383n83NycrhcbkRExNu3b+vq6v6h2K2goCA+Pl6dRH///n1CQsKFCxccHBw0xUgk0uHDh48fP87j8a5c" +
            "ueLm5qYZXkokEhwOd+LECaVSaW9vf/LkSSMjIywW29fXp6+vb2Jiop5qAoFgeHhYfUUNFosVFxfHZrP5" +
            "fH5JSUlSUpJa52EwGCKRqFKpVCrVqE7rWJDL5UePHqVQKFDKICwsjEQiqc2ESCSCHhuHwzGZzISEhNra" +
            "WqVSeffu3ZycHDWVdWHS+fPnkUhkVFSUoaEhn89XKBRGRkZDQ0M0Gg2JRG7atKm2tra6uppCoejOJAQC" +
            "UVNTQyKRjhw5cubMmSNHjiQlJS1YsGCcjIBCoaBQKJcvX+7p6YmJibGxsbl27drElPHHxLpbt27t7+93" +
            "cnKCcqlKpbKiouL777/X0sYGBgZfffVVYmIigUCgUqkJCQldXV1qx/zixYuenp5Hjx7F4/EbN27cu3ev" +
            "WCz+448/vLy89u3bp07J9PT0bNmyJTAwsLS0VOtJ6uvrWSyWiYmJs7NzXV3duXPn1K1aWlpiY2NDQkLG" +
            "z/SPRHFx8ePHj1Eo1OzZs+fMmTN79mxNNZmenu7t7X327FkQBOPi4qqrq4lEIpFI5HA4mzdv1v0uKBTK" +
            "1tbW3Nwc0mHbtm0jk8kPHz5Ux/Nz5syxtLQEQVCdA9MRIpHoypUrHA7n4MGDfD5/3bp1mzZtolKpI/0e" +
            "lUpVV1d3+fLlw4cPv3//PjIy0tHRMTMzU5dNmLrqpIGBgcTERBQKderUqVF3XkJm4tixY8XFxRkZGQQC" +
            "wdjYuK6u7s6dO0eOHNESXr16tUwmS0tLKygoAADg9OnTJiYmIAh2dnaqVCoOhzM4OCgQCKAdohwOR8sM" +
            "CQSCwcFBEARHtU3m5ub79u1zdnY+efKkloBKpZLJZLoU2eTm5hYVFcXGxgYEBECdzJw584cffoAsr+aE" +
            "BkFQbpfD8wEKkuPj4wcHBxMTE3UxndnZ2a9fv162bFlkZGRqaqpUKoVCMxAEhUIhj8fDYrEeHh4MBiMh" +
            "IWHSU10qlWZmZm7evDkxMfHmzZuVlZV0Op1EIk2bNk0rym5oaGhqapo7d66/v7+1tXVmZqZW5P+xTBKL" +
            "xRQKZf369SgUqre3V8tzFAqF0Kjt2rULmjGurq4uLi6ZmZmaudfm5mb1sbe3t7W1dWNj46NHj+Ry+f79" +
            "+xUKxcDAAGRN3r17J5PJ5HJ5S0tLZ2cnZLYbGxshEre3t0skkpFu6aNHj86fPy+TyVQqFZVKraioCAkJ" +
            "mdzQ19bWUigUMzMztVfR1NS0d+/e2bNnp6SkmJubAwAwVlvd3S/Iq6VQKHZ2dlFRUVB2QLPnzs7Ozs7O" +
            "gIAABoPxkV7s4OBgenr61q1bk5KS6urqoM1xIpFIrbP19fWNjY2trKz27Nnj5eXF5XLPnz8/UW2kq58U" +
            "FhaGwWB++eWXs2fPjiqg9pM6OjqkUqnWr9u2bRs12/TkyROomlhtvHbs2AEd79+/HzooKSlZvny5ZkML" +
            "CwvN09u3b9++fVuzFQ6H8/LyGrkba0IrKpp5hP7+fuifGNLS0kZdPQgICMjNzY2Li5vc7UayMyMjIyMj" +
            "Q8s9+OyzzyatmS5cuPDgwYOYmJidO3eCIMhkMqEdAZDSDQ0NhXaCX7p0qbq6evKR8gclTp06FRgYuGHDB" +
            "hqN1tbWJpFIoKjS0NBQM5ZRKpVsNpvNZs+cOXPnzp0oFGrz5s1ZWVmfNsYmk8ne3t5z5869evVqenq6ps" +
            "KDZtju3btjY2PHCf28vb3JZPLQ0ND44RWZTM7JyVEqlUePHq2urubxeKPGwwAAHD9+3MzMjEKhjMxcGBg" +
            "YmJqaGhgY6OjjQyTGYrFaMyE1NTUyMnJkEywWa2hoKJFINP80YlS0t7enp6cXFBRYWFhAUcvWrVvz8vJe" +
            "v37NZDLZbLaOeSM0Gv3FF1+MjL51YhKLxXr9+nVwcHBhYSEIgrt374bqp27dukUmk9VibDb79OnTCoXi" +
            "+PHjM2bMQCAQBw8ejI+P/yTrDzU1NTNnzkSj0TKZDErTRUVFRUREjDQoKBRKPctVKlVzczOfz9cUMDU1v" +
            "X79OgiCYzGps7MTyob7+Ph0d3ePvAWPx9NkMB6PT0lJSUhI8PLy0mLboUOHDhw4MFbM2NzczOPxIK9FJ" +
            "BIlJib+9ddfeDz+zJkzoaGhWt9v1B727NmzaNGi3bt3t7W1fXAYodGAnhwAgJKSEiikndC3sLW1JZFIo8" +
            "aAH2aSUqmMi4uLiIiATqHsUWhoqJOTk+YbOjk5acWNAADouFVq/Jl6//79tLQ0V1dXDAYDguCxY8egMHs" +
            "cpZKXl1dcXKxUKktLS1tbWz///HPNyHysT+vr63vv3r1nz57Fx8dDH7i1tbWxsRGPx4eFhcnl8gMHDkCG" +
            "mEqlGhsbx8bGFhUVPX36VKlUyuVyqPpi2bJlmjca9V6xsbFUKvXRo0cymQwKe4eGhh48eIBAINzd3UdVP" +
            "2NlQR0cHJYuXfrrr79OqIAEBEFNI647fH19x0pGoMdfvjl9+vTPP//c0tKSnZ2tvh4cHHz58uWReZ2/A0" +
            "KhsKysDHK9DQwM0Gi0LquSFRUV0ANjMBg7Ozs/P7/p06d/sNXSpUuTkpJSUlLUbgQUFf7rX/9avXp1b2+" +
            "vehAsLCwcHR39/f1Pnjx58+ZNhUKBRCJnzJixcuXK3bt3f/BGAQEB2dnZW7ZsoVKp6ovTpk2bPn36WM7o" +
            "WCAQCD/88AOUyhoZkXxCoFCogICA7du3j6UdPlzpplKpUlNT1fkMAAAOHDgwiaqXSaOtre3333/39PS0tb" +
            "VlsVjR0dG6ZN6htBOBQFi1apUuNNLUZxUVFZp+1YoVK5BIJJ/PV3/mkJCQwMBA6Pj06dMikQiDwWzZsmVC" +
            "NYovXrzQLJD19fUNDw+f3BDxeDxoja+8vPzv+AT29vbR0dHr1693dHQcM3SAayanDNra2p4/f56RkdHd3" +
            "f2p+kSj0d9++210dLSbm9v4MQrMpCkFKGU6UT96fGAwGF2WEWEmwfg0gPfgwoCZBANmEoyph/8BSDWV01" +
            "whvp4AAAAASUVORK5CYII=";

    public static void generateAndShare(Activity activity, JSONObject m) {
        try {
            PdfDocument doc = new PdfDocument();
            final int W = 595, H = 842;
            String uuid = getOrCreateUuid(m);

            // Page 1: فرم هزینه ماموریت
            PdfDocument.Page p1 = doc.startPage(new PdfDocument.PageInfo.Builder(W, H, 1).create());
            drawPaymentsPage(p1.getCanvas(), m, W, H, uuid);
            doc.finishPage(p1);

            // Page 2: صورت هزینه های تنخواه گردان
            PdfDocument.Page p2 = doc.startPage(new PdfDocument.PageInfo.Builder(W, H, 2).create());
            drawExpensesPage(p2.getCanvas(), m, W, H, uuid);
            doc.finishPage(p2);

            // Page 3: روکش سند حسابداری / درخواست وجه
            PdfDocument.Page p3 = doc.startPage(new PdfDocument.PageInfo.Builder(W, H, 3).create());
            drawCoverPage(p3.getCanvas(), m, W, H, uuid);
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
                    if (o != null) {
                        doc.writeTo(o);
                        o.close();
                    }
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

    private static String getOrCreateUuid(JSONObject m) {
        String u = m.optString("uuid", "").trim();
        if (u.isEmpty()) {
            u = UUID.randomUUID().toString().toUpperCase(Locale.US);
            try { m.put("uuid", u); } catch (Exception ignored) {}
        }
        return u;
    }

    private static Paint paint(float size, boolean bold) {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
        p.setColor(Color.BLACK);
        p.setTextSize(size);
        p.setTypeface(bold ? Typeface.create(Typeface.DEFAULT, Typeface.BOLD) : Typeface.DEFAULT);
        return p;
    }

    private static Paint linePaint(float strokeWidth) {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(Color.BLACK);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(strokeWidth);
        return p;
    }

    private static Paint fillPaint(int color) {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(color);
        p.setStyle(Paint.Style.FILL);
        return p;
    }

    private static Bitmap decodeLogo() {
        try {
            byte[] b = Base64.decode(LOGO_PNG, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(b, 0, b.length);
        } catch (Exception e) {
            return null;
        }
    }

    private static void drawCheckbox(Canvas c, float x, float y, float size, boolean checked) {
        c.drawRect(x, y, x + size, y + size, linePaint(0.9f));
        if (checked) {
            Paint p = paint(size * 0.95f, true);
            p.setTextAlign(Paint.Align.CENTER);
            Paint.FontMetrics fm = p.getFontMetrics();
            float baseline = (y + y + size) / 2f - (fm.ascent + fm.descent) / 2f;
            c.drawText("✓", x + size / 2f, baseline, p);
        }
    }

    private static void drawBarcode(Canvas c, String code, float cx, float top, float width, float height) {
        Paint bp = fillPaint(Color.BLACK);
        String fullCode = "*M" + code + "*";
        float left = cx - width / 2f;
        Random rnd = new Random(fullCode.hashCode());
        float curX = left;
        float endX = left + width;
        while (curX < endX - 3) {
            float bw = rnd.nextBoolean() ? (rnd.nextBoolean() ? 2.8f : 1.8f) : 1.0f;
            if (curX + bw > endX) bw = endX - curX;
            c.drawRect(curX, top, curX + bw, top + height, bp);
            curX += bw + (rnd.nextBoolean() ? 1.8f : 1.0f);
        }
        Paint tp = paint(7.5f, false);
        tp.setTextAlign(Paint.Align.CENTER);
        c.drawText(fullCode, cx, top + height + 9.5f, tp);
    }

    private static String moneyInt(double x) {
        return String.format(Locale.US, "%,.0f", x);
    }

    private static double expenseSum(JSONObject m, String cat) {
        double s = 0;
        JSONArray a = m.optJSONArray("expenses");
        if (a != null) {
            for (int i = 0; i < a.length(); i++) {
                JSONObject o = a.optJSONObject(i);
                if (o != null && (cat == null || cat.equals(o.optString("category")))) {
                    s += o.optDouble("amount", 0);
                }
            }
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
        return missionPay(m) +
                reimbursable(m, "hotelPayer", "hotel") +
                reimbursable(m, "goPayer", "ticketGo") +
                reimbursable(m, "backPayer", "ticketBack") +
                reimbursable(m, "cancelGoPayer", "cancelGo") +
                reimbursable(m, "cancelBackPayer", "cancelBack") +
                expenseSum(m, null);
    }

    private static String amountWords(double v) {
        long n = Math.round(v);
        return words(n) + " ریال";
    }

    private static String words(long n) {
        String[] u = {"صفر","یک","دو","سه","چهار","پنج","شش","هفت","هشت","نه","ده","یازده","دوازده","سیزده","چهارده","پانزده","شانزده","هفده","هجده","نوزده","بیست","بیست و یک","بیست و دو","بیست و سه","بیست و چهار","بیست و پنج","بیست و شش","بیست و هفت","بیست و هشت","بیست و نه"};
        String[] t = {"","","بیست","سی","چهل","پنجاه","شصت","هفتاد","هشتاد","نود"};
        String[] h = {"","یکصد","دویست","سیصد","چهارصد","پانصد","ششصد","هفتصد","هشتصد","نهصد"};
        if (n < 30) return u[(int)n];
        if (n < 100) return t[(int)n/10] + (n % 10 > 0 ? " و " + u[(int)(n % 10)] : "");
        if (n < 1000) return h[(int)n/100] + (n % 100 > 0 ? " و " + words(n % 100) : "");
        if (n < 1000000) return words(n / 1000) + " هزار" + (n % 1000 > 0 ? " و " + words(n % 1000) : "");
        if (n < 1000000000) return words(n / 1000000) + " میلیون" + (n % 1000000 > 0 ? " و " + words(n % 1000000) : "");
        return words(n / 1000000000) + " میلیارد" + (n % 1000000000 > 0 ? " و " + words(n % 1000000000) : "");
    }

    private static void drawCell(Canvas c, String label, String val, float l, float t, float r, float b, boolean shaded) {
        if (shaded) c.drawRect(l, t, r, b, fillPaint(Color.rgb(248, 249, 250)));
        c.drawRect(l, t, r, b, linePaint(0.8f));
        float mid = (t + b) / 2f;
        Paint lp = paint(7.5f, false);
        lp.setTextAlign(Paint.Align.RIGHT);
        lp.setColor(Color.rgb(40, 50, 60));
        c.drawText(label, r - 5, mid + 2.5f, lp);
        float labelW = lp.measureText(label);
        Paint vp = paint(7.5f, true);
        vp.setTextAlign(Paint.Align.RIGHT);
        float maxValW = Math.max(10, (r - l) - labelW - 14);
        while (vp.measureText(val) > maxValW && vp.getTextSize() > 5.5f) {
            vp.setTextSize(vp.getTextSize() - 0.3f);
        }
        c.drawText(val, r - labelW - 9, mid + 2.5f, vp);
    }

    private static void drawPaymentsPage(Canvas c, JSONObject m, int W, int H, String uuid) {
        final float L = 36f, R = W - 36f;
        Paint line = linePaint(1.0f);

        // Header
        Paint formIdP = paint(8.5f, true);
        formIdP.setTextAlign(Paint.Align.LEFT);
        c.drawText("75F0203-B", L, 40, formIdP);
        c.drawLine(L, 45, L + 115, 45, line);
        c.drawRect(L, 48, L + 115, 68, line);
        Paint formTitleP = paint(8.5f, true);
        formTitleP.setTextAlign(Paint.Align.CENTER);
        c.drawText("فرم هزینه ماموریت", L + 57.5f, 61, formTitleP);

        // Barcode
        String formNo = m.optString("form", "6922");
        drawBarcode(c, formNo, W / 2f, 34, 115, 26);

        // Logo
        Bitmap logo = decodeLogo();
        if (logo != null) {
            c.drawBitmap(logo, null, new RectF(R - 145, 30, R, 68), new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
            logo.recycle();
        }

        c.drawLine(L, 80, R, 80, line);

        // Table 1: مشخصات
        float y = 88f;
        c.drawRect(L, y, R, y + 96, line);
        c.drawRect(L, y, R, y + 19, fillPaint(Color.rgb(238, 240, 242)));
        c.drawLine(L, y + 19, R, y + 19, line);
        Paint thP = paint(8.2f, true);
        thP.setTextAlign(Paint.Align.RIGHT);
        c.drawText("مشخصات: شماره فرم ماموریت: " + formNo, R - 8, y + 13.5f, thP);

        float rowH = 19.25f;
        float yRows = y + 19;
        float split = L + (R - L) * 0.52f;
        for (int i = 0; i < 4; i++) {
            float top = yRows + i * rowH;
            float btm = top + rowH;
            c.drawLine(L, btm, R, btm, line);
            if (i == 0) {
                drawCell(c, "شماره سرویس:", m.optString("service"), split, top, R, btm, false);
                drawCell(c, "بخش:", "خدمات پس از فروش، زیمنس آزمایشگاهی", L, top, split, btm, false);
            } else if (i == 1) {
                drawCell(c, "تاریخ:", m.optString("start") + " الی " + m.optString("end"), split, top, R, btm, false);
                drawCell(c, "کمپانی:", "SIEMENS", L, top, split, btm, false);
            } else if (i == 2) {
                drawCell(c, "نام کارشناس:", m.optString("person"), split, top, R, btm, false);
                drawCell(c, "دستگاه:", m.optString("device"), L, top, split, btm, false);
            } else {
                drawCell(c, "موضوع:", m.optString("subject"), split, top, R, btm, false);
                drawCell(c, "مقصد:", m.optString("destination"), L, top, split, btm, false);
            }
        }
        c.drawLine(split, yRows, split, y + 96, line);

        // Section: وضعیت سفر
        y = 192f;
        Paint secP = paint(8.5f, true);
        secP.setTextAlign(Paint.Align.RIGHT);
        c.drawText("وضعیت سفر:", R, y, secP);
        y += 4;
        float tH = 22f;
        c.drawRect(L, y, R, y + 5 * tH, line);
        float xType = R - 60f;
        float xPayer = L + 125f;
        c.drawLine(xType, y, xType, y + 5 * tH, line);
        c.drawLine(xPayer, y, xPayer, y + 5 * tH, line);

        for (int i = 1; i < 5; i++) c.drawLine(L, y + i * tH, R, y + i * tH, line);

        double groundExp = expenseSum(m, "هزینه تردد بین شهری");
        double tGo = m.optDouble("ticketGo", 0), tBack = m.optDouble("ticketBack", 0);
        double cGo = m.optDouble("cancelGo", 0), cBack = m.optDouble("cancelBack", 0);

        // Row 0: زمینی
        Paint labelP = paint(8f, false);
        labelP.setTextAlign(Paint.Align.RIGHT);
        c.drawText("زمینی", R - 6, y + 14.5f, labelP);
        drawCheckbox(c, xType + 8, y + 7, 8, groundExp > 0 || m.optBoolean("ground"));
        c.drawText("هزینه تردد بین شهری:  " + moneyInt(groundExp) + " ریال", xType - 8, y + 14.5f, labelP);
        drawPayer(c, L, y, xPayer, y + tH, "شخص", groundExp > 0);

        // Row 1 & 2: هوایی
        c.drawText("هوایی", R - 6, y + tH + 14.5f, labelP);
        drawCheckbox(c, xType + 8, y + tH + 7, 8, tGo > 0 || tBack > 0 || m.optBoolean("air"));
        c.drawText("هزینه بلیط رفت:  " + (tGo > 0 ? moneyInt(tGo) + " ریال" : " ریال"), xType - 8, y + tH + 14.5f, labelP);
        c.drawText("شماره بلیط: " + m.optString("goNo"), (xType + xPayer) / 2f - 20, y + tH + 14.5f, labelP);
        drawPayer(c, L, y + tH, xPayer, y + 2 * tH, m.optString("goPayer"), tGo > 0);

        c.drawText("هزینه بلیط برگشت:  " + (tBack > 0 ? moneyInt(tBack) + " ریال" : " ریال"), xType - 8, y + 2 * tH + 14.5f, labelP);
        c.drawText("شماره بلیط: " + m.optString("backNo"), (xType + xPayer) / 2f - 20, y + 2 * tH + 14.5f, labelP);
        drawPayer(c, L, y + 2 * tH, xPayer, y + 3 * tH, m.optString("backPayer"), tBack > 0);

        // Row 3 & 4: کنسلی
        c.drawText("کنسلی", R - 6, y + 3 * tH + 14.5f, labelP);
        drawCheckbox(c, xType + 8, y + 3 * tH + 7, 8, cGo > 0 || cBack > 0 || m.optBoolean("cancel"));
        c.drawText("هزینه بلیط رفت:  " + (cGo > 0 ? moneyInt(cGo) + " ریال" : " ریال"), xType - 8, y + 3 * tH + 14.5f, labelP);
        c.drawText("شماره بلیط:", (xType + xPayer) / 2f - 20, y + 3 * tH + 14.5f, labelP);
        drawPayer(c, L, y + 3 * tH, xPayer, y + 4 * tH, m.optString("cancelGoPayer"), cGo > 0);

        c.drawText("هزینه بلیط برگشت:  " + (cBack > 0 ? moneyInt(cBack) + " ریال" : " ریال"), xType - 8, y + 4 * tH + 14.5f, labelP);
        c.drawText("شماره بلیط:", (xType + xPayer) / 2f - 20, y + 4 * tH + 14.5f, labelP);
        drawPayer(c, L, y + 4 * tH, xPayer, y + 5 * tH, m.optString("cancelBackPayer"), cBack > 0);

        // Section: زمان بندی سفر
        y += 5 * tH + 10f;
        c.drawText("زمان بندی سفر:", R, y, secP);
        y += 4;
        c.drawRect(L, y, R, y + 2 * 21f, line);
        c.drawLine(L, y + 21f, R, y + 21f, line);
        c.drawLine(W / 2f, y, W / 2f, y + 42f, line);
        c.drawText("تاریخ شروع: " + m.optString("start"), R - 8, y + 14.5f, labelP);
        c.drawText("تاریخ برگشت: " + m.optString("end"), W / 2f - 8, y + 14.5f, labelP);
        c.drawText("زمان شروع: " + m.optString("startTime"), R - 8, y + 35.5f, labelP);
        c.drawText("زمان اتمام: " + m.optString("endTime"), W / 2f - 8, y + 35.5f, labelP);

        // Section: هزینه سفر
        y += 42f + 10f;
        c.drawText("هزینه سفر:", R, y, secP);
        y += 4;
        float hRow = 23f;
        c.drawRect(L, y, R, y + 5 * hRow, line);
        for (int i = 1; i < 5; i++) c.drawLine(L, y + i * hRow, R, y + i * hRow, line);

        double hotelExp = m.optDouble("hotel", 0);
        c.drawText("هتل:  " + (hotelExp > 0 ? moneyInt(hotelExp) + " ریال" : " ریال"), R - 8, y + 15.5f, labelP);
        drawPayer(c, L, y, L + 140, y + hRow, m.optString("hotelPayer"), hotelExp > 0);

        double cityExp = expenseSum(m, "هزینه تردد درون شهری") + expenseSum(m, "هزینه شهر تهران");
        c.drawText("تردد:  " + moneyInt(cityExp) + " ریال", R - 8, y + hRow + 15.5f, labelP);

        c.drawText("حقوق روزانه:  " + moneyInt(missionPay(m)) + " ریال", R - 8, y + 2 * hRow + 15.5f, labelP);
        c.drawText("تعداد روزهای ماموریت  " + String.format(Locale.US, "%.1f", m.optDouble("days", 1)), R - 190, y + 2 * hRow + 15.5f, labelP);
        c.drawText("تعداد روزهای تعطیل ماموریت  " + String.format(Locale.US, "%.1f", m.optDouble("holiday", 0)), R - 325, y + 2 * hRow + 15.5f, labelP);
        c.drawText("حومه", L + 35, y + 2 * hRow + 15.5f, labelP);
        drawCheckbox(c, L + 15, y + 2 * hRow + 6, 8, m.optBoolean("suburb"));

        double otherExp = expenseSum(m, "سایر هزینه ها");
        c.drawText("متفرقه:  " + moneyInt(otherExp) + " ریال", R - 8, y + 3 * hRow + 15.5f, labelP);

        Paint totP = paint(8.5f, true);
        totP.setTextAlign(Paint.Align.RIGHT);
        c.drawText("جمع کل:", R - 8, y + 4 * hRow + 15.5f, totP);
        totP.setTextAlign(Paint.Align.LEFT);
        c.drawText(moneyInt(total(m)) + " ریال", L + 30, y + 4 * hRow + 15.5f, totP);

        // Signatures
        y += 5 * hRow + 8f;
        c.drawRect(L, y, R, y + 54f, line);
        c.drawText("امضاء کارشناس:", R - 8, y + 16, labelP);
        c.drawText("تائید مدیر گروه:", W / 2f + 40, y + 16, labelP);
        c.drawText("پرداخت هزینه فوق بلا مانع است.", L + 160, y + 24, labelP);
        c.drawText("امضاء:", L + 160, y + 44, labelP);

        // Section: پرداخت
        y += 54f + 8f;
        c.drawText("پرداخت:", R, y, secP);
        y += 4;
        c.drawRect(L, y, R, y + 104f, line);
        c.drawLine(L, y + 52f, R, y + 52f, line);

        c.drawText("مبلغ  " + moneyInt(total(m)) + "  ریال بابت حق علی الحساب ماموریت فوق به اینجانب پرداخت گردید.", R - 8, y + 18, labelP);
        c.drawText("نام و نام خانوادگی:  " + m.optString("person"), R - 8, y + 38, labelP);
        c.drawText("تاریخ:  " + m.optString("end"), W / 2f, y + 38, labelP);

        c.drawText("مبلغ  " + moneyInt(total(m)) + "  ریال بابت تسویه ماموریت فوق به اینجانب پرداخت گردید.", R - 8, y + 52f + 18, labelP);
        c.drawText("نام و نام خانوادگی:  " + m.optString("person"), R - 8, y + 52f + 38, labelP);
        c.drawText("تاریخ:  " + m.optString("end"), W / 2f, y + 52f + 38, labelP);

        // Footer
        drawFooter(c, W, uuid, m.optString("end"));
    }

    private static void drawPayer(Canvas c, float l, float t, float r, float b, String payer, boolean hasAmount) {
        float midY = (t + b) / 2f - 4f;
        Paint p = paint(7.2f, false);
        p.setTextAlign(Paint.Align.RIGHT);

        float curX = r - 4f;
        c.drawText("شرکت", curX, midY + 7.5f, p);
        float wSh = p.measureText("شرکت");
        float b1 = curX - wSh - 4f - 8f;
        drawCheckbox(c, b1, midY, 8, hasAmount && "شرکت".equals(payer));

        float xK = b1 - 10f;
        c.drawText("کارشناس", xK, midY + 7.5f, p);
        float wK = p.measureText("کارشناس");
        float b2 = xK - wK - 4f - 8f;
        drawCheckbox(c, b2, midY, 8, hasAmount && "شخص".equals(payer));
    }

    private static void drawExpensesPage(Canvas c, JSONObject m, int W, int H, String uuid) {
        final float L = 24f, R = W - 24f;
        Paint line = linePaint(1.2f);
        c.drawRect(L, 28, R, 545, line);

        Bitmap logo = decodeLogo();
        if (logo != null) {
            c.drawBitmap(logo, null, new RectF(L + 12, 34, L + 138, 70), new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
            logo.recycle();
        }

        Paint titleP = paint(10f, true);
        titleP.setTextAlign(Paint.Align.RIGHT);
        c.drawText("صورت هزینه های", R - 12, 49, titleP);
        c.drawText("تنخواه گردان", R - 12, 63, titleP);

        c.drawRect(L, 74, 185, 94, line);
        Paint dP = paint(9.5f, false);
        dP.setTextAlign(Paint.Align.CENTER);
        c.drawText(m.optString("end"), 92, 88, dP);
        Paint dLabel = paint(8.5f, false);
        dLabel.setTextAlign(Paint.Align.LEFT);
        c.drawText("تاریخ:", 192, 88, dLabel);
        c.drawLine(L, 95, R, 95, line);

        String[] titles = {"هزینه های شهر تهران", "هزینه های تردد بین شهری", "هزینه های تردد درون شهری به غیر از تهران", "سایر هزینه ها"};
        String[] keys = {"هزینه شهر تهران", "هزینه تردد بین شهری", "هزینه تردد درون شهری", "سایر هزینه ها"};

        float[] x = {L, 170f, 230f, 490f, R};
        String[] heads = {"مبلغ", "فاکتور", "شرح", "تاریخ"};

        JSONArray all = m.optJSONArray("expenses");
        float y = 99f;
        final float titleH = 17f, headH = 20f, rowH = 19f, sumH = 20f;

        for (int g = 0; g < 4; g++) {
            Paint secHead = paint(8.3f, true);
            secHead.setTextAlign(Paint.Align.CENTER);
            c.drawText(titles[g], (L + R) / 2f, y + 12f, secHead);
            y += titleH;

            for (int j = 0; j < 4; j++) cellText(c, heads[j], x[j], y, x[j + 1], y + headH, true, 8.5f);
            y += headH;

            ArrayList<JSONObject> items = new ArrayList<>();
            if (all != null) {
                for (int i = 0; i < all.length(); i++) {
                    JSONObject o = all.optJSONObject(i);
                    if (o != null && keys[g].equals(o.optString("category"))) items.add(o);
                }
            }

            if (items.isEmpty()) {
                for (int j = 0; j < 4; j++) cellText(c, "", x[j], y, x[j + 1], y + rowH, false, 8f);
                y += rowH;
            } else {
                for (JSONObject it : items) {
                    String desc = it.optString("desc");
                    if (desc.isEmpty()) desc = it.optString("tehranItem");
                    String[] vals = {moneyInt(it.optDouble("amount")) + " ریال", it.optString("invoice"), desc, it.optString("date")};
                    for (int j = 0; j < 4; j++) cellText(c, vals[j], x[j], y, x[j + 1], y + rowH, false, 8f);
                    y += rowH;
                }
            }

            cellText(c, moneyInt(expenseSum(m, keys[g])) + " ریال", x[0], y, x[1], y + sumH, false, 8.5f);
            cellText(c, "جمع " + titles[g], x[1], y, R, y + sumH, false, 8.2f);
            y += sumH;
        }

        // Total
        c.drawRect(x[0], y, x[1], y + 22f, line);
        Paint totValP = paint(8.5f, true);
        totValP.setTextAlign(Paint.Align.CENTER);
        c.drawText(moneyInt(expenseSum(m, null)) + " ریال", (x[0] + x[1]) / 2f, y + 15, totValP);

        c.drawRect(x[1], y, R, y + 22f, fillPaint(Color.BLACK));
        Paint totLblP = paint(8.5f, true);
        totLblP.setColor(Color.WHITE);
        totLblP.setTextAlign(Paint.Align.RIGHT);
        c.drawText("جمع کل:", R - 10, y + 15, totLblP);
        y += 22f;

        // Signatures
        c.drawRect(L, y, R, y + 54f, line);
        c.drawLine(230f, y, 230f, y + 54f, line);
        Paint sigP = paint(8f, false);
        sigP.setTextAlign(Paint.Align.CENTER);
        c.drawText("تایید مدیر", 125f, y + 28, sigP);
        sigP.setTextAlign(Paint.Align.RIGHT);
        c.drawText("نام و امضا کارشناس:  " + m.optString("person"), R - 10, y + 28, sigP);

        drawFooter(c, W, uuid, m.optString("end"));
    }

    private static void cellText(Canvas c, String text, float l, float t, float r, float b, boolean isHeader, float fontSize) {
        if (isHeader) c.drawRect(l, t, r, b, fillPaint(Color.rgb(235, 238, 240)));
        c.drawRect(l, t, r, b, linePaint(0.9f));
        if (text == null || text.isEmpty()) return;
        Paint p = paint(fontSize, isHeader);
        p.setTextAlign(Paint.Align.CENTER);
        Paint.FontMetrics fm = p.getFontMetrics();
        float baseline = (t + b) / 2f - (fm.ascent + fm.descent) / 2f;
        c.drawText(text, (l + r) / 2f, baseline, p);
    }

    private static void drawCoverPage(Canvas c, JSONObject m, int W, int H, String uuid) {
        final float L = 36f, R = W - 36f;
        Paint line = linePaint(1.0f);

        Paint titleP = paint(12.5f, true);
        titleP.setTextAlign(Paint.Align.CENTER);
        c.drawText("شرکت فن آوری آزمایشگاهی (با مسئولیت محدود)", W / 2f, 32, titleP);

        Paint subP = paint(10f, true);
        subP.setTextAlign(Paint.Align.CENTER);
        c.drawText("درخواست وجه                         تسویه وجه دریافتی", W / 2f, 54, subP);
        drawCheckbox(c, W / 2f + 85, 43, 9, true);
        drawCheckbox(c, W / 2f - 75, 43, 9, false);

        float y = 68f;
        drawInfoRow(c, "از واحد:", "خدمات پس از فروش، زیمنس آزمایشگاهی", "شماره:", m.optString("form"), L, y, R, y + 24);
        y += 24;
        drawInfoRow(c, "به واحد:", "مالی", "تاریخ:", m.optString("end"), L, y, R, y + 24);
        y += 28;

        // Payment request statement
        c.drawRect(L, y, R, y + 54, line);
        Paint stP = paint(8.5f, false);
        stP.setTextAlign(Paint.Align.RIGHT);
        c.drawText("بدینوسیله خواهشمند است نسبت به پرداخت یا تسویه مبلغ:", R - 10, y + 18, stP);
        Paint amtW = paint(9.5f, true);
        amtW.setTextAlign(Paint.Align.RIGHT);
        c.drawText(amountWords(total(m)), R - 10, y + 38, amtW);
        Paint amtN = paint(9.5f, true);
        amtN.setTextAlign(Paint.Align.LEFT);
        c.drawText(moneyInt(total(m)) + "  ریال/دلار", L + 14, y + 38, amtN);
        y += 60;

        // Pay to mode
        c.drawRect(L, y, R, y + 48, line);
        c.drawText("در وجه:", R - 10, y + 17, stP);
        drawCheckbox(c, R - 60, y + 7, 8, true);
        c.drawText("وجه نقد", R - 75, y + 17, stP);
        drawCheckbox(c, R - 140, y + 7, 8, false);
        c.drawText("چک جاری", R - 155, y + 17, stP);
        drawCheckbox(c, R - 220, y + 7, 8, false);
        c.drawText("ایران چک", R - 235, y + 17, stP);
        drawCheckbox(c, R - 300, y + 7, 8, false);
        c.drawText("چک مدت دار", R - 315, y + 17, stP);

        c.drawText("شرکت/خانم / آقای:  " + m.optString("person"), R - 10, y + 37, stP);
        c.drawText("بابت ماموریت:  " + m.optString("destination"), W / 2f + 40, y + 37, stP);
        y += 54;

        // 8 Category Checkboxes
        c.drawRect(L, y, R, y + 84, line);
        float midX = W / 2f;
        c.drawLine(midX, y, midX, y + 84, line);
        String[] colR = {"تنخواه گردان", "علی الحساب (حقوق/ماموریت/نصب/گارانتی/سایر)", "پیش پرداخت (خرید کالا و لوازم (اموال/هزینه ای/سایر))", "جاری شرکا (برداشت شخصی/قرض)"};
        String[] colL = {"برگشت از فروش/استرداد وجه واریزی", "ثبت سفارش/بارنامه/ترخیص/خزانه داری/بیمه/انرژی اتمی", "هزینه ها (جهت تکمیل به پشت صفحه مراجعه شود)", "سایر"};

        for (int i = 0; i < 4; i++) {
            float cy = y + 12 + i * 19;
            drawCheckbox(c, R - 18, cy - 7, 8, i == 1);
            c.drawText(colR[i], R - 28, cy, stP);

            drawCheckbox(c, midX - 18, cy - 7, 8, false);
            c.drawText(colL[i], midX - 28, cy, stP);
        }
        y += 90;

        // Company attribution
        c.drawRect(L, y, R, y + 48, line);
        c.drawText("مربوط به:", R - 10, y + 17, stP);
        c.drawText("کمپانی = زیمنس آزمایشگاهی", R - 80, y + 17, stP);
        c.drawText("نسبت درصد هر کمپانی = 100%", midX - 10, y + 17, stP);
        c.drawText("شماره سفارش / شماره پروفرم =", R - 80, y + 37, stP);
        c.drawText("پیش فاکتور / فاکتور = پرداخت نمایید.", midX - 10, y + 37, stP);
        y += 54;

        // Remarks
        c.drawRect(L, y, R, y + 40, line);
        String remark = "توضیحات: مأموریت " + m.optString("subject") + " — مقصد: " + m.optString("destination");
        c.drawText(remark, R - 10, y + 24, stP);
        y += 46;

        // Signatures
        c.drawRect(L, y, R, y + 42, line);
        c.drawLine(midX, y, midX, y + 42, line);
        c.drawText("درخواست کننده:  " + m.optString("person"), R - 10, y + 26, stP);
        c.drawText("مدیر واحد:", midX - 10, y + 26, stP);
        y += 48;

        // Accounting
        c.drawRect(L, y, R, y + 126, line);
        Paint accP = paint(8.5f, true);
        accP.setTextAlign(Paint.Align.RIGHT);
        c.drawText("حسابداری:", R - 10, y + 18, accP);
        c.drawText("لطفاً نسبت به پرداخت مبلغ .................................................( " + moneyInt(total(m)) + " ) ریال/ارز اقدام نمایید.", R - 10, y + 36, stP);
        c.drawText("بابت: ماموریت        به سررسید: " + m.optString("end") + "        از محل موجودی بانک:        شعبه:", R - 10, y + 54, stP);
        c.drawText("به شماره حساب:                                                اقدام نمایید.", R - 10, y + 72, stP);
        c.drawText("رسیدگی کننده:                                                 مدیر امور مالی:", R - 10, y + 92, stP);
        drawCheckbox(c, R - 18, y + 104, 8, false);
        c.drawText("الزامات بودجه رعایت نشده است.", R - 28, y + 112, stP);
        drawCheckbox(c, midX - 18, y + 104, 8, false);
        c.drawText("سقف بودجه رعایت نشده است.", midX - 28, y + 112, stP);

        // Footer with form number on left
        Paint foot = paint(7f, false);
        foot.setTextAlign(Paint.Align.LEFT);
        c.drawText("M" + m.optString("form", "6922"), L + 6, 815, foot);
        foot.setTextAlign(Paint.Align.CENTER);
        c.drawText(uuid, W / 2f, 815, foot);
        foot.setTextAlign(Paint.Align.RIGHT);
        c.drawText(m.optString("end"), R - 6, 815, foot);
    }

    private static void drawInfoRow(Canvas c, String rLbl, String rVal, String lLbl, String lVal, float l, float t, float r, float b) {
        c.drawRect(l, t, r, b, linePaint(0.9f));
        float mid = (t + b) / 2f + 2.5f;
        float half = (l + r) / 2f;
        c.drawLine(half, t, half, b, linePaint(0.9f));

        Paint p = paint(8f, false);
        p.setTextAlign(Paint.Align.RIGHT);
        c.drawText(rLbl, r - 6, mid, p);
        Paint vp = paint(8f, true);
        vp.setTextAlign(Paint.Align.RIGHT);
        c.drawText(rVal, r - p.measureText(rLbl) - 14, mid, vp);

        c.drawText(lLbl, half - 6, mid, p);
        c.drawText(lVal, half - p.measureText(lLbl) - 14, mid, vp);
    }

    private static void drawFooter(Canvas c, int W, String uuid, String date) {
        Paint p = paint(7f, false);
        p.setTextAlign(Paint.Align.CENTER);
        c.drawText(uuid, W / 2f, 815, p);
        p.setTextAlign(Paint.Align.RIGHT);
        c.drawText(date, W - 36f, 815, p);
    }
}
