package com.missionapp;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.*;
import android.text.style.*;
import android.webkit.WebView;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import org.json.*;
import java.io.*;
import java.util.*;

public class MainActivity extends Activity {
    LinearLayout box;
    ScrollView scrollRoot;
    JSONObject cur;
    ArrayList<JSONObject> data = new ArrayList<>();

    final int DAILY=39000000, MULTI=35000000, SUBURB=15000000, HOLIDAY=16000000;

    String[] people={"احسان صنعتی","الهه ناجی","امیرمحمد گلک","امین ترابی نیا","بنفشه رئیسی دهکردی","بهمن لاله زارزاده","ستوده ملکی","سعید سوهانی","سوگند حداد","سهیل قاسم پور مطلق","سیدعلی سعادتمندی","علی رزاقی","فرزاد میرآبادی دستگردی","گویک کارپیانس","مریم فریدون فاراب","معصومه غلامی","مهدی جوادی مجره","مهدی شاهقلی دستگردی","مهدی فراهانی","مهدی مردانپورجرتوده","نیما فیروزان بلسی"};
    String[] devices={"ADVIA 2120","ADVIA 2120i","ADVIA 360","ADVIA 560","ADVIA Centaur CP","ADVIA Centaur XP","ADVIA Centaur XPT","ADVIA Chemistry 1800","Atellica CH 930 Analyzer","Atellica IM 1300 Analyzer","Atellica IM 1600 Analyzer","Atellica MagLine","Atellica Solution","Atellica UAS 800","BEP2000","BFT II","CLINITEK ADVANTUS","CLINITEK Novus","CLINITEK STATUS","epoc Host","epoc Reader","IMMULITE 2000","IMMULITE 2000 XPi","PCR QS 5","PFA-200","Prisca","RAPIDCHEM 744","RAPIDCHEM 754","RAPIDPoint 500","RL348EX","VersaCell X3"};
    String[] payers={"شخص","شرکت"};
    String[] cats={"هزینه شهر تهران","هزینه تردد بین شهری","هزینه تردد درون شهری","سایر هزینه ها"};
    String[] tehranItems={"بازار (خرید لوازم و ابزار کار)","ترمینال یا فرودگاه به منزل","سایت مشتری به سایت مشتری","سایت مشتری به شرکت","سایت مشتری به منزل","شرکت به ترمینال یا فرودگاه","شرکت به سایت مشتری","شرکت به منزل","فرودگاه یا ترمینال به شرکت","منزل به ترمینال یا فرودگاه","منزل به سایت مشتری","منزل به شرکت"};

    int bg, card, primary, primaryDark, border, text, muted, green, red;
    boolean darkMode=false;
    int palette(int id){ return getResources().getColor(id, getTheme()); }
    void applyColors(){
        darkMode=getPreferences(0).getBoolean("darkMode",false);
        if(darkMode){
            bg=palette(R.color.navy); card=palette(R.color.navy_surface); primary=palette(R.color.orange); primaryDark=palette(R.color.light_text);
            border=palette(R.color.dark_divider); text=palette(R.color.light_text); muted=palette(R.color.dark_muted); green=palette(R.color.teal); red=palette(R.color.orange);
        } else {
            bg=palette(R.color.background); card=palette(R.color.white); primary=palette(R.color.orange); primaryDark=palette(R.color.navy);
            border=palette(R.color.divider); text=palette(R.color.navy); muted=palette(R.color.muted); green=palette(R.color.teal_dark); red=palette(R.color.orange);
        }
        getWindow().setStatusBarColor(palette(R.color.navy));
        getWindow().setNavigationBarColor(palette(R.color.navy));
        getWindow().getDecorView().setSystemUiVisibility(0);
    }

    void toggleDarkMode(){
        getPreferences(0).edit().putBoolean("darkMode",!darkMode).apply();
        recreate();
    }

    public void onCreate(Bundle b){ super.onCreate(b); darkMode=getPreferences(0).getBoolean("darkMode",false); setTheme(darkMode?R.style.AppThemeDark:R.style.AppTheme); applyColors(); load(); home(); maybeAskRestore(); }

    int dp(int v){ return Math.round(v*getResources().getDisplayMetrics().density); }

    TextView tv(String s,float z){
        TextView v=new TextView(this); v.setText(s); v.setTextSize(z); v.setTypeface(Typeface.create("tahoma",Typeface.NORMAL)); v.setTextColor(text);
        v.setPadding(dp(4),dp(2),dp(4),dp(2)); v.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);
        v.setLayoutDirection(View.LAYOUT_DIRECTION_RTL); return v;
    }

    TextView label(String s){
        TextView v=tv(s,15); v.setTextColor(muted); v.setPadding(dp(2),dp(0),dp(2),dp(5));
        return v;
    }

    GradientDrawable shape(int color,float radius,int strokeColor){
        GradientDrawable g=new GradientDrawable(); g.setColor(color); g.setCornerRadius(dp(Math.round(radius)));
        if(strokeColor!=Color.TRANSPARENT) g.setStroke(Math.max(1,dp(1)),strokeColor); return g;
    }

    EditText ed(String hint){
        EditText v=new EditText(this); v.setHint(hint); v.setHintTextColor(palette(R.color.muted));
        v.setTypeface(Typeface.create("tahoma",Typeface.NORMAL)); v.setTextSize(18); v.setTextColor(text); v.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);
        v.setSingleLine(true); v.setPadding(dp(14),dp(0),dp(14),dp(0)); v.setBackground(shape(card,14,border));
        v.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        if(hint.contains("هزینه")||hint.contains("مبلغ")||hint.contains("روز")||hint.contains("شماره"))
            v.setInputType(android.text.InputType.TYPE_CLASS_NUMBER|android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        if(hint.contains("مبلغ")) addThousands(v);
        return v;
    }

    Button bt(String s){
        Button v=new Button(this); v.setText(s); v.setTypeface(Typeface.create("tahoma",Typeface.NORMAL)); v.setTextSize(17); v.setAllCaps(false); v.setTextColor(palette(R.color.navy));
        v.setPadding(dp(12),dp(4),dp(12),dp(4)); v.setMinHeight(dp(48)); v.setMinimumHeight(dp(48)); v.setGravity(Gravity.CENTER);
        v.setBackground(shape(primary,14,Color.TRANSPARENT)); return v;
    }

    Button secondary(String s){
        Button v=bt(s); v.setTextColor(palette(darkMode?R.color.teal:R.color.teal_dark)); v.setBackground(shape(card,14,palette(darkMode?R.color.teal:R.color.teal_dark))); v.setMinHeight(dp(48)); v.setTextSize(15); return v;
    }

    Spinner spinner(String[] a){
        Spinner s=new Spinner(this);
        ArrayAdapter<String> adapter=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,a){
            @Override public View getView(int position,View convertView,android.view.ViewGroup parent){
                TextView v=(TextView)super.getView(position,convertView,parent);
                v.setTypeface(Typeface.create("tahoma",Typeface.NORMAL));v.setTextSize(17);v.setTextColor(text);v.setGravity(Gravity.CENTER_VERTICAL|Gravity.RIGHT);return v;
            }
            @Override public View getDropDownView(int position,View convertView,android.view.ViewGroup parent){
                TextView v=(TextView)super.getDropDownView(position,convertView,parent);
                v.setTypeface(Typeface.create("tahoma",Typeface.NORMAL));v.setTextSize(17);v.setTextColor(text);v.setBackgroundColor(card);v.setPadding(dp(18),dp(14),dp(18),dp(14));return v;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        s.setAdapter(adapter);
        s.setBackground(shape(card,14,border)); s.setPadding(dp(10),dp(0),dp(10),dp(0)); s.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        return s;
    }

    LinearLayout field(String name,View v){
        LinearLayout l=new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL); l.setPadding(dp(0),dp(3),dp(0),dp(3));
        TextView lab=label(name);
        lab.setTypeface(Typeface.create("tahoma",Typeface.NORMAL),Typeface.BOLD);
        lab.setTextColor(primaryDark);
        lab.setPadding(dp(10),dp(5),dp(10),dp(5));
        lab.setBackground(shape(darkMode?palette(R.color.navy_surface):palette(R.color.background),8,Color.TRANSPARENT));
        lab.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);
        lab.setMinHeight(dp(38));
        lab.setSingleLine(false);
        lab.setMaxLines(2);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);
        lp.bottomMargin=dp(6);
        l.addView(lab,lp);
        LinearLayout.LayoutParams vp=new LinearLayout.LayoutParams(-1,dp(54)); vp.topMargin=dp(0);
        l.addView(v,vp);
        return l;
    }

    LinearLayout section(String title){
        LinearLayout c=new LinearLayout(this); c.setOrientation(LinearLayout.VERTICAL); c.setPadding(dp(16),dp(14),dp(16),dp(14));
        c.setBackground(shape(card,18,border)); c.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        TextView h=tv(title,18); h.setTextColor(primaryDark); h.setTypeface(Typeface.DEFAULT,Typeface.BOLD); h.setPadding(dp(0),dp(0),dp(0),dp(10));
        c.addView(h); box.addView(c,new LinearLayout.LayoutParams(-1,-2));
        Space sp=new Space(this); box.addView(sp,new LinearLayout.LayoutParams(dp(1),dp(10))); return c;
    }

    LinearLayout row(){
        LinearLayout r=new LinearLayout(this); r.setOrientation(LinearLayout.HORIZONTAL); r.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        return r;
    }

    void add2(LinearLayout r,String n1,View v1,String n2,View v2){
        // Compact two-column layout: labels wrap, but the form does not grow excessively tall.
        r.setOrientation(LinearLayout.HORIZONTAL);
        r.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        LinearLayout a=field(n1,v1),b=field(n2,v2);
        LinearLayout.LayoutParams ap=new LinearLayout.LayoutParams(0,-2,1f);
        ap.setMargins(dp(3),dp(0),dp(3),dp(0));
        LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(0,-2,1f);
        bp.setMargins(dp(3),dp(0),dp(3),dp(0));
        r.addView(a,ap);
        r.addView(b,bp);
    }

    void base(String title){
        box=new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL); box.setPadding(dp(12),dp(12),dp(12),dp(12));
        box.setBackgroundColor(bg); box.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        scrollRoot=new ScrollView(this); scrollRoot.setFillViewport(true); scrollRoot.setBackgroundColor(bg); scrollRoot.addView(box);
        // Keep Android status/navigation bars visible; the system already insets this content.
        getWindow().getDecorView().setSystemUiVisibility(0);
        setContentView(scrollRoot);
        LinearLayout bar=new LinearLayout(this); bar.setPadding(dp(14),dp(10),dp(14),dp(10)); bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setBackground(shape(palette(R.color.navy),16,Color.TRANSPARENT));
        bar.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        WebView brandMark=new WebView(this);
        brandMark.setBackgroundColor(Color.TRANSPARENT);
        brandMark.setVerticalScrollBarEnabled(false);
        brandMark.setHorizontalScrollBarEnabled(false);
        brandMark.setOverScrollMode(View.OVER_SCROLL_NEVER);
        brandMark.loadDataWithBaseURL("file:///android_asset/brand/","<html><body style='margin:0;background:transparent;display:flex;align-items:center;justify-content:center'><img src='logo-full.svg' style='width:48px;height:48px;object-fit:contain'/></body></html>","text/html","UTF-8",null);
        bar.addView(brandMark,new LinearLayout.LayoutParams(dp(52),dp(52)));
        TextView h=tv(title,21); h.setTextColor(palette(R.color.white)); h.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        bar.addView(h,new LinearLayout.LayoutParams(0,dp(60),1)); box.addView(bar);
        Space s=new Space(this); box.addView(s,new LinearLayout.LayoutParams(dp(1),dp(12)));
    }

    /** Amounts are rials: whole numbers with thousands separators. */
    String money(double x){ return String.format(Locale.US,"%,.0f",x); }
    /** For input fields: empty for zero, otherwise 1,234,567. */
    String moneyText(double v){ return v==0?"":String.format(Locale.US,"%,.0f",v); }

    /** Persian/Arabic digits and separators -> ASCII. */
    String normDigits(String s){
        if(s==null) return "";
        StringBuilder b=new StringBuilder();
        for(char c:s.toCharArray()){
            if(c>='\u06F0'&&c<='\u06F9') b.append((char)('0'+(c-'\u06F0')));
            else if(c>='\u0660'&&c<='\u0669') b.append((char)('0'+(c-'\u0660')));
            else if(c=='\u066C'||c=='\u060C') b.append(',');
            else if(c=='\u066B') b.append('.');
            else b.append(c);
        }
        return b.toString();
    }

    /** Live thousands separators for rial amount fields (digits only). */
    void addThousands(final EditText e){
        e.setKeyListener(android.text.method.DigitsKeyListener.getInstance("0123456789,\u06F0\u06F1\u06F2\u06F3\u06F4\u06F5\u06F6\u06F7\u06F8\u06F9\u0660\u0661\u0662\u0663\u0664\u0665\u0666\u0667\u0668\u0669\u066C\u060C"));
        e.addTextChangedListener(new android.text.TextWatcher(){
            boolean busy;
            public void beforeTextChanged(CharSequence a,int b,int c,int d){}
            public void onTextChanged(CharSequence a,int b,int c,int d){}
            public void afterTextChanged(android.text.Editable ed){
                if(busy) return; busy=true;
                try{
                    String curText=ed.toString();
                    int cursor=Math.min(e.getSelectionStart()<0?curText.length():e.getSelectionStart(),curText.length());
                    String before=normDigits(curText.substring(0,cursor)).replaceAll("[^0-9]","");
                    String digits=normDigits(curText).replaceAll("[^0-9]","");
                    if(digits.length()>15) digits=digits.substring(0,15);
                    String f=digits.isEmpty()?"":String.format(Locale.US,"%,d",Long.parseLong(digits));
                    if(!f.equals(curText)){
                        e.setText(f);
                        int cnt=0,pos=0; int want=Math.min(before.length(),digits.length());
                        while(pos<f.length()&&cnt<want){ if(Character.isDigit(f.charAt(pos))) cnt++; pos++; }
                        e.setSelection(Math.min(pos,f.length()));
                    }
                }catch(Exception ignored){}
                busy=false;
            }
        });
    }

    String normForm(String s){ return normDigits(s).replaceAll("\\s+","").toLowerCase(Locale.ROOT); }

    /** True if another saved mission already uses this form number. */
    boolean formExists(String form,JSONObject except){
        String f=normForm(form); if(f.isEmpty()) return false;
        for(JSONObject d:data) if(d!=except && f.equals(normForm(d.optString("form","")))) return true;
        return false;
    }
    String moneyInt(double x){ return String.format(Locale.US,"%,.0f",x); }

    String words(long n){
        String[] u={"صفر","یک","دو","سه","چهار","پنج","شش","هفت","هشت","نه","ده","یازده","دوازده","سیزده","چهارده","پانزده","شانزده","هفده","هجده","نوزده","بیست","بیست و یک","بیست و دو","بیست و سه","بیست و چهار","بیست و پنج","بیست و شش","بیست و هفت","بیست و هشت","بیست و نه"};
        String[] t={"","","بیست","سی","چهل","پنجاه","شصت","هفتاد","هشتاد","نود"};
        String[] h={"","یکصد","دویست","سیصد","چهارصد","پانصد","ششصد","هفتصد","هشتصد","نهصد"};
        if(n<30)return u[(int)n]; if(n<100)return t[(int)n/10]+(n%10>0?" و "+u[(int)n%10]:"");
        if(n<1000)return h[(int)n/100]+(n%100>0?" و "+words(n%100):"");
        if(n<1000000)return words(n/1000)+" هزار"+(n%1000>0?" و "+words(n%1000):"");
        if(n<1000000000)return words(n/1000000)+" میلیون"+(n%1000000>0?" و "+words(n%1000000):"");
        return words(n/1000000000)+" میلیارد"+(n%1000000000>0?" و "+words(n%1000000000):"");
    }

    String amountWords(double v){ return words(Math.round(v))+" ریال"; }
    double num(EditText e){ try{return Double.parseDouble(normDigits(e.getText().toString()).replace(",","").trim());}catch(Exception x){return 0;} }

    double expenseSum(JSONObject m,String cat){
        double s=0; JSONArray a=m.optJSONArray("expenses");
        if(a!=null) for(int i=0;i<a.length();i++){JSONObject o=a.optJSONObject(i); if(cat==null||cat.equals(o.optString("category")))s+=o.optDouble("amount");}
        return s;
    }
    double missionPay(JSONObject m){
        double d=m.optDouble("days",1),h=m.optDouble("holiday",0);
        if(m.optBoolean("suburb"))return d*SUBURB+h*HOLIDAY;
        return (d==1?DAILY:d*MULTI)+h*HOLIDAY;
    }
    double reimbursable(JSONObject m,String payerKey,String amountKey){return "شخص".equals(payer(m,payerKey))?m.optDouble(amountKey):0;}
    double total(JSONObject m){return missionPay(m)+reimbursable(m,"hotelPayer","hotel")+reimbursable(m,"goPayer","ticketGo")+reimbursable(m,"backPayer","ticketBack")+reimbursable(m,"cancelGoPayer","cancelGo")+reimbursable(m,"cancelBackPayer","cancelBack")+expenseSum(m,null);}
    String payer(JSONObject m,String key){return m.optString(key,"شخص");}

    void home(){
        base("مدیریت مأموریت و هزینه‌ها");
        TextView intro=tv("فرم مأموریت، محاسبات و روکش سند حسابداری",14); intro.setTextColor(muted); intro.setPadding(dp(4),dp(0),dp(4),dp(12)); box.addView(intro);

        Button mode=secondary(darkMode?"تغییر به حالت روشن":"تغییر به حالت تاریک"); mode.setOnClickListener(v->toggleDarkMode()); box.addView(mode);
        Space modeSp=new Space(this); box.addView(modeSp,new LinearLayout.LayoutParams(dp(1),dp(8)));

        Button bk=secondary("پشتیبان‌گیری و بازیابی اطلاعات"); bk.setOnClickListener(v->backupScreen()); box.addView(bk);
        Space bkSp=new Space(this); box.addView(bkSp,new LinearLayout.LayoutParams(dp(1),dp(8)));
        if(prefs().getString(BackupManager.PREF_TREE,null)==null && !data.isEmpty()){
            LinearLayout warn=new LinearLayout(this); warn.setPadding(dp(14),dp(10),dp(14),dp(10)); warn.setBackground(shape(card,14,red)); warn.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            TextView wt=tv("هشدار: اطلاعات مأموریت‌ها فقط داخل برنامه است و با حذف برنامه از بین می‌رود. برای محافظت از آن‌ها، پشتیبان‌گیری را فعال کنید.",14); wt.setTextColor(red); warn.addView(wt);
            warn.setOnClickListener(v->backupScreen()); box.addView(warn,new LinearLayout.LayoutParams(-1,-2));
            Space wSp=new Space(this); box.addView(wSp,new LinearLayout.LayoutParams(dp(1),dp(8)));
        }

        Button n=bt("＋  ثبت مأموریت جدید"); n.setOnClickListener(v->{cur=new JSONObject();try{cur.put("expenses",new JSONArray());}catch(Exception e){}edit();}); box.addView(n);
        Space sp=new Space(this); box.addView(sp,new LinearLayout.LayoutParams(dp(1),dp(12)));

        TextView count=tv("مأموریت‌های ثبت‌شده  ("+data.size()+")",17); count.setTypeface(Typeface.DEFAULT,Typeface.BOLD); box.addView(count);
        Space sp2=new Space(this); box.addView(sp2,new LinearLayout.LayoutParams(dp(1),dp(6)));

        if(data.isEmpty()){
            LinearLayout empty=new LinearLayout(this); empty.setGravity(Gravity.CENTER); empty.setPadding(dp(20),dp(30),dp(20),dp(30)); empty.setBackground(shape(card,18,border));
            TextView e=tv("هنوز مأموریتی ثبت نشده است\nبرای شروع، «ثبت مأموریت جدید» را بزنید.",15); e.setGravity(Gravity.CENTER); e.setTextColor(muted); empty.addView(e); box.addView(empty);
        } else {
            for(JSONObject m:data){
                LinearLayout c=new LinearLayout(this); c.setOrientation(LinearLayout.VERTICAL); c.setPadding(dp(16),dp(12),dp(16),dp(12)); c.setBackground(shape(card,16,border));
                TextView a=tv("فرم "+(m.optString("form").isEmpty()?"بدون شماره":m.optString("form"))+" — "+m.optString("subject","بدون موضوع"),18); a.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
                TextView b=tv("شماره فرم: "+(m.optString("form").isEmpty()?"—":m.optString("form"))+"    |    مقصد: "+m.optString("destination","")+"    |    کارشناس: "+m.optString("person",""),15); b.setTextColor(muted);
                TextView z=tv("جمع قابل پرداخت: "+money(total(m))+" ریال",14); z.setTextColor(green); z.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
                c.addView(a); c.addView(b); c.addView(z);
                c.setOnClickListener(v->{cur=m;edit();}); box.addView(c);
                Space q=new Space(this); box.addView(q,new LinearLayout.LayoutParams(dp(1),dp(8)));
            }
        }
    }

    int index(String[] a,String s){for(int i=0;i<a.length;i++)if(a[i].equals(s))return i;return 0;}

    void edit(){
        base(cur.length()==0?"ثبت مأموریت جدید":"ویرایش مأموریت");

        EditText form=ed("شماره فرم"),service=ed("شماره سرویس"),subject=ed("موضوع"),dest=ed("مقصد"),start=ed("تاریخ شروع"),end=ed("تاریخ پایان"),startTime=ed("زمان شروع"),endTime=ed("زمان پایان"),days=ed("تعداد روز"),holiday=ed("تعداد روز تعطیل");
        EditText hotel=ed("مبلغ هتل"),go=ed("مبلغ بلیط رفت"),back=ed("مبلغ بلیط برگشت"),cancelGo=ed("مبلغ کنسلی رفت"),cancelBack=ed("مبلغ کنسلی برگشت"),goNo=ed("شماره بلیط رفت"),backNo=ed("شماره بلیط برگشت");
        Spinner ps=spinner(people),ds=spinner(devices),hp=spinner(payers),gp=spinner(payers),bp=spinner(payers),cgp=spinner(payers),cbp=spinner(payers);

        form.setText(cur.optString("form"));service.setText(cur.optString("service"));subject.setText(cur.optString("subject"));dest.setText(cur.optString("destination"));start.setText(cur.optString("start"));end.setText(cur.optString("end"));startTime.setText(cur.optString("startTime"));endTime.setText(cur.optString("endTime"));
        attachDatePicker(start); attachDatePicker(end);
        days.setText(String.valueOf(cur.optDouble("days",1))); holiday.setText(String.valueOf(cur.optDouble("holiday",0))); hotel.setText(String.valueOf(cur.optDouble("hotel",0)));go.setText(String.valueOf(cur.optDouble("ticketGo",0)));back.setText(String.valueOf(cur.optDouble("ticketBack",0)));cancelGo.setText(String.valueOf(cur.optDouble("cancelGo",0)));cancelBack.setText(String.valueOf(cur.optDouble("cancelBack",0)));goNo.setText(cur.optString("goNo"));backNo.setText(cur.optString("backNo"));
        ps.setSelection(index(people,cur.optString("person")));ds.setSelection(index(devices,cur.optString("device")));hp.setSelection(index(payers,payer(cur,"hotelPayer")));gp.setSelection(index(payers,payer(cur,"goPayer")));bp.setSelection(index(payers,payer(cur,"backPayer")));cgp.setSelection(index(payers,payer(cur,"cancelGoPayer")));cbp.setSelection(index(payers,payer(cur,"cancelBackPayer")));

        LinearLayout s1=section("اطلاعات اصلی مأموریت");
        LinearLayout r=row(); add2(r,"شماره فرم",form,"شماره سرویس",service);s1.addView(r);
        r=row(); add2(r,"نام کارشناس",ps,"دستگاه",ds);s1.addView(r);
        s1.addView(field("موضوع",subject)); s1.addView(field("مقصد",dest));
        r=row(); add2(r,"تاریخ شروع",start,"تاریخ پایان",end);s1.addView(r);
        r=row(); add2(r,"زمان شروع",startTime,"زمان پایان",endTime);s1.addView(r);
        r=row(); add2(r,"تعداد روز مأموریت",days,"تعداد روز تعطیل",holiday);s1.addView(r);

        LinearLayout s2=section("نوع مأموریت و محاسبه حق مأموریت");
        CheckBox suburb=new CheckBox(this); suburb.setText("مأموریت حومه تهران"); suburb.setTextSize(15); suburb.setChecked(cur.optBoolean("suburb",false)); suburb.setTextColor(text);
        s2.addView(suburb);
        TextView rule=tv("یک‌روزه: ۳۹,۰۰۰,۰۰۰  |  چندروزه: روزانه ۳۵,۰۰۰,۰۰۰  |  حومه: روزانه ۱۵,۰۰۰,۰۰۰  |  تعطیل: روزانه ۱۶,۰۰۰,۰۰۰ ریال",12); rule.setTextColor(muted); s2.addView(rule);

        LinearLayout s3=section("هتل، بلیط و کنسلی");
        r=row(); add2(r,"هزینه هتل",hotel,"پرداخت هتل توسط",hp);s3.addView(r);
        r=row(); add2(r,"بلیط رفت",go,"پرداخت بلیط رفت توسط",gp);s3.addView(r);
        r=row(); add2(r,"شماره بلیط رفت",goNo,"بلیط برگشت",back);s3.addView(r);
        r=row(); add2(r,"پرداخت بلیط برگشت توسط",bp,"شماره بلیط برگشت",backNo);s3.addView(r);
        r=row(); add2(r,"کنسلی بلیط رفت",cancelGo,"پرداخت کنسلی رفت توسط",cgp);s3.addView(r);
        r=row(); add2(r,"کنسلی بلیط برگشت",cancelBack,"پرداخت کنسلی برگشت توسط",cbp);s3.addView(r);

        LinearLayout s4=section("ریز هزینه‌ها");
        LinearLayout exList=new LinearLayout(this);exList.setOrientation(LinearLayout.VERTICAL);s4.addView(exList);
        TextView sum=tv("جمع قابل پرداخت: "+money(total(cur))+" ریال",18);sum.setTextColor(green);sum.setTypeface(Typeface.DEFAULT,Typeface.BOLD);sum.setGravity(Gravity.CENTER);sum.setPadding(dp(5),dp(14),dp(5),dp(14));s4.addView(sum);
        Button ex=bt("＋  افزودن ردیف هزینه");ex.setOnClickListener(v->expenseDialog(exList,sum));s4.addView(ex);render(exList,sum);

        LinearLayout actions=section("عملیات");
        Button save=bt("✓  ذخیره مأموریت"); save.setOnClickListener(v->{
            try{
                validate7(end.getText().toString());
                cur.put("form",form.getText().toString());cur.put("service",service.getText().toString());cur.put("person",ps.getSelectedItem().toString());cur.put("device",ds.getSelectedItem().toString());cur.put("subject",subject.getText().toString());cur.put("destination",dest.getText().toString());cur.put("start",start.getText().toString());cur.put("end",end.getText().toString());cur.put("startTime",startTime.getText().toString());cur.put("endTime",endTime.getText().toString());cur.put("days",num(days));cur.put("holiday",num(holiday));cur.put("suburb",suburb.isChecked());cur.put("hotel",num(hotel));cur.put("ticketGo",num(go));cur.put("ticketBack",num(back));cur.put("cancelGo",num(cancelGo));cur.put("cancelBack",num(cancelBack));cur.put("goNo",goNo.getText().toString());cur.put("backNo",backNo.getText().toString());cur.put("hotelPayer",hp.getSelectedItem().toString());cur.put("goPayer",gp.getSelectedItem().toString());cur.put("backPayer",bp.getSelectedItem().toString());cur.put("cancelGoPayer",cgp.getSelectedItem().toString());cur.put("cancelBackPayer",cbp.getSelectedItem().toString());
                if(!data.contains(cur))data.add(cur); save(); Toast.makeText(this,"مأموریت ذخیره شد",Toast.LENGTH_SHORT).show(); home();
            }catch(Exception e){toast(e.getMessage());}
        });
        Button pdf=secondary("تولید PDF با فرم رسمی"); pdf.setOnClickListener(v->pdf(cur));
        Button backBtn=secondary("‹  بازگشت"); backBtn.setOnClickListener(v->home());
        actions.addView(save); Space x=new Space(this);actions.addView(x,new LinearLayout.LayoutParams(dp(1),dp(6)));actions.addView(pdf);Space y=new Space(this);actions.addView(y,new LinearLayout.LayoutParams(dp(1),dp(6)));actions.addView(backBtn);
    }

    void expenseDialog(LinearLayout list,TextView sum){ expenseDialog(list,sum,-1); }

    String plainNumber(double v){ return v==Math.floor(v)?String.valueOf((long)v):String.valueOf(v); }

    /** idx < 0: add a new expense; idx >= 0: edit the expense at that position. */
    void expenseDialog(final LinearLayout list,final TextView sum,final int idx){
        JSONArray all=cur.optJSONArray("expenses");
        JSONObject old=(idx>=0&&all!=null)?all.optJSONObject(idx):null;
        LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);l.setPadding(dp(10),dp(4),dp(10),dp(4));l.setBackgroundColor(bg);
        final Spinner cat=spinner(cats),ti=spinner(tehranItems); l.addView(field("نوع هزینه",cat));l.addView(field("شرح/مورد تهران",ti));
        final EditText date=ed("تاریخ"),desc=ed("شرح"),invoice=ed("شماره فاکتور"),amount=ed("مبلغ");
        attachDatePicker(date); l.addView(field("تاریخ",date));l.addView(field("شرح",desc));l.addView(field("شماره فاکتور",invoice));l.addView(field("مبلغ",amount));
        if(old!=null){
            cat.setSelection(index(cats,old.optString("category"))); ti.setSelection(index(tehranItems,old.optString("tehranItem")));
            date.setText(old.optString("date")); desc.setText(old.optString("desc")); invoice.setText(old.optString("invoice"));
            amount.setText(moneyText(old.optDouble("amount",0)));
        }
        ti.setVisibility(cat.getSelectedItemPosition()==0?View.VISIBLE:View.GONE);
        cat.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){public void onItemSelected(android.widget.AdapterView<?> p,View v,int pos,long id){ti.setVisibility(pos==0?View.VISIBLE:View.GONE);}public void onNothingSelected(android.widget.AdapterView<?> p){}});
        ScrollView sv=new ScrollView(this); sv.addView(l);
        new AlertDialog.Builder(this).setTitle(idx>=0?"ویرایش هزینه":"افزودن هزینه").setView(sv).setNegativeButton("انصراف",null).setPositiveButton(idx>=0?"ذخیره تغییرات":"ثبت هزینه",(d,w)->{
            try{
                JSONArray a=cur.optJSONArray("expenses"); if(a==null){a=new JSONArray();cur.put("expenses",a);}
                JSONObject o=new JSONObject();o.put("category",cat.getSelectedItem().toString());o.put("tehranItem",ti.getSelectedItem().toString());o.put("date",date.getText().toString());o.put("desc",desc.getText().toString());o.put("invoice",invoice.getText().toString());o.put("amount",num(amount));
                if(idx>=0&&idx<a.length()) a.put(idx,o); else a.put(o);
                render(list,sum);
            }catch(Exception e){toast(e.getMessage());}
        }).show();
    }

    void render(final LinearLayout list,final TextView sum){
        list.removeAllViews(); final JSONArray a=cur.optJSONArray("expenses");
        if(a!=null) for(int i=0;i<a.length();i++){
            final int k=i; JSONObject o=a.optJSONObject(i); if(o==null) continue;
            LinearLayout r=new LinearLayout(this);r.setOrientation(LinearLayout.HORIZONTAL);r.setGravity(Gravity.CENTER_VERTICAL);r.setPadding(dp(8),dp(6),dp(8),dp(6));r.setBackground(shape(bg,12,border));r.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            TextView t=tv((i+1)+"  "+o.optString("category")+"\n"+o.optString("desc")+"   |   "+money(o.optDouble("amount"))+" ریال",13);t.setTextColor(text);t.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            Button edt=secondary("ویرایش");edt.setTextSize(13);edt.setMinHeight(dp(44));edt.setOnClickListener(v->expenseDialog(list,sum,k));
            Button del=secondary("حذف");del.setTextSize(13);del.setTextColor(red);del.setMinHeight(dp(44));
            del.setOnClickListener(v->new AlertDialog.Builder(this).setTitle("حذف هزینه").setMessage("این هزینه حذف شود؟").setNegativeButton("انصراف",null).setPositiveButton("حذف",(dd,ww)->{a.remove(k);render(list,sum);}).show());
            r.setOnClickListener(v->expenseDialog(list,sum,k));
            r.addView(t,new LinearLayout.LayoutParams(0,-2,1));
            LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(dp(72),dp(48)); bp.setMargins(dp(4),0,0,0);
            r.addView(edt,bp); r.addView(del,new LinearLayout.LayoutParams(dp(64),dp(48)));
            list.addView(r);Space sp=new Space(this);list.addView(sp,new LinearLayout.LayoutParams(dp(1),dp(6)));
        }
        sum.setText("جمع قابل پرداخت: "+money(total(cur))+" ریال");
    }

    void saveLocal(){try{JSONArray a=new JSONArray();for(JSONObject m:data)a.put(m);getPreferences(0).edit().putString("data",a.toString()).apply();}catch(Exception e){}}
    void save(){ saveLocal(); backupAuto(); }
    void load(){try{JSONArray a=new JSONArray(getPreferences(0).getString("data","[]"));for(int i=0;i<a.length();i++)data.add(a.getJSONObject(i));}catch(Exception e){}}
    // ---------- Backup / restore (survives uninstall) ----------
    static final int REQ_TREE=7101, REQ_RESTORE=7102;
    static final java.util.concurrent.ExecutorService BK=java.util.concurrent.Executors.newSingleThreadExecutor();
    LinearLayout backupBox;

    SharedPreferences prefs(){ return getPreferences(0); }

    String stamp(){
        Calendar c=Calendar.getInstance();
        int[] j=gregorianToJalali(c.get(Calendar.YEAR),c.get(Calendar.MONTH)+1,c.get(Calendar.DAY_OF_MONTH));
        if(j==null) j=new int[]{c.get(Calendar.YEAR),c.get(Calendar.MONTH)+1,c.get(Calendar.DAY_OF_MONTH)};
        return String.format(Locale.US,"%04d/%02d/%02d  %02d:%02d",j[0],j[1],j[2],c.get(Calendar.HOUR_OF_DAY),c.get(Calendar.MINUTE));
    }

    void msg(String s){ Toast.makeText(this,s,Toast.LENGTH_LONG).show(); }

    /** Called from save(): silently rewrites the backup file if a folder was chosen. */
    void backupAuto(){
        final SharedPreferences p=prefs(); String t=p.getString(BackupManager.PREF_TREE,null); if(t==null)return;
        final String json=BackupManager.buildJson(data); if(json==null)return;
        final Context app=getApplicationContext(); final Uri tree=Uri.parse(t); final String when=stamp();
        BK.execute(()->{
            boolean ok=BackupManager.write(app,tree,json);
            SharedPreferences.Editor e=p.edit().putBoolean(BackupManager.PREF_ERR,!ok);
            if(ok) e.putString(BackupManager.PREF_LAST,when);
            e.apply();
        });
    }

    void backupNow(){
        final SharedPreferences p=prefs(); String t=p.getString(BackupManager.PREF_TREE,null);
        if(t==null){ pickFolder(); return; }
        final String json=BackupManager.buildJson(data); final Context app=getApplicationContext(); final Uri tree=Uri.parse(t); final String when=stamp();
        BK.execute(()->{
            boolean ok=BackupManager.write(app,tree,json);
            SharedPreferences.Editor e=p.edit().putBoolean(BackupManager.PREF_ERR,!ok);
            if(ok) e.putString(BackupManager.PREF_LAST,when);
            e.apply();
            runOnUiThread(()->{
                msg(ok?"پشتیبان ذخیره شد":"پشتیبان‌گیری ناموفق بود؛ پوشه را دوباره انتخاب کنید");
                if(box==backupBox) backupScreen();
            });
        });
    }

    void pickFolder(){
        Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_WRITE_URI_PERMISSION|Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION|Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);
        try{ startActivityForResult(i,REQ_TREE); }catch(Exception e){ msg("انتخاب پوشه روی این گوشی ممکن نیست"); }
    }

    void pickRestoreFile(){
        Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT); i.addCategory(Intent.CATEGORY_OPENABLE); i.setType("*/*");
        try{ startActivityForResult(i,REQ_RESTORE); }catch(Exception e){ msg("انتخاب فایل روی این گوشی ممکن نیست"); }
    }

    @Override protected void onActivityResult(int req,int res,Intent d){
        super.onActivityResult(req,res,d);
        if(res!=RESULT_OK||d==null||d.getData()==null) return;
        Uri u=d.getData();
        if(req==REQ_TREE){
            try{ getContentResolver().takePersistableUriPermission(u,Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_WRITE_URI_PERMISSION); }catch(Exception e){ msg("دسترسی دائمی به این پوشه داده نشد؛ پوشهٔ دیگری انتخاب کنید"); return; }
            prefs().edit().putString(BackupManager.PREF_TREE,u.toString()).putBoolean(BackupManager.PREF_ERR,false).apply();
            Uri existing=BackupManager.findBackup(this,u);
            if(existing!=null){
                JSONArray a=BackupManager.parse(BackupManager.read(this,existing));
                if(a!=null && a.length()>0){ askRestore(a,true); return; }
            }
            backupNow();
        } else if(req==REQ_RESTORE){
            JSONArray a=BackupManager.parse(BackupManager.read(this,u));
            if(a==null){ msg("این فایل، پشتیبان معتبر مأموریت‌ها نیست"); return; }
            askRestore(a,false);
        }
    }

    void applyRestore(JSONArray a,boolean replace){
        if(replace) data.clear();
        java.util.HashSet<String> have=new java.util.HashSet<>(); for(JSONObject m:data) have.add(m.toString());
        int added=0;
        int skipped=0;
        for(int i=0;i<a.length();i++){
            JSONObject m=a.optJSONObject(i); if(m==null) continue;
            if(!have.add(m.toString())) continue;
            if(formExists(m.optString("form",""),null)){ skipped++; continue; }
            data.add(m); added++;
        }
        save();
        msg("بازیابی انجام شد ("+added+" مأموریت)"+(skipped>0?"؛ "+skipped+" مأموریت با شمارهٔ فرم تکراری نادیده گرفته شد":""));
        home();
    }

    void askRestore(final JSONArray a,final boolean fromFolder){
        String head="در فایل پشتیبان "+a.length()+" مأموریت پیدا شد.";
        if(data.isEmpty()){
            String m=head+"\nبازیابی شود؟"+(fromFolder?"\n\nاگر بازیابی نکنید، با اولین ذخیرهٔ مأموریت، پشتیبان قدیمی در این پوشه بازنویسی می‌شود.":"");
            new AlertDialog.Builder(this).setTitle("بازیابی اطلاعات").setMessage(m)
                .setPositiveButton("بازیابی",(d,w)->applyRestore(a,true))
                .setNegativeButton(fromFolder?"نادیده گرفتن":"انصراف",(d,w)->{ if(fromFolder) backupNow(); })
                .show();
        } else {
            String m=head+"\nالان "+data.size()+" مأموریت هم داخل برنامه هست."+(fromFolder?"\n\nاگر «نادیده گرفتن» را بزنید، پشتیبان قدیمی بازنویسی می‌شود.":"");
            new AlertDialog.Builder(this).setTitle("بازیابی اطلاعات").setMessage(m)
                .setPositiveButton("افزودن به موارد فعلی",(d,w)->applyRestore(a,false))
                .setNeutralButton("جایگزین کردن همه",(d,w)->applyRestore(a,true))
                .setNegativeButton(fromFolder?"نادیده گرفتن":"انصراف",(d,w)->{ if(fromFolder) backupNow(); })
                .show();
        }
    }

    /** On a fresh install (no data yet) offer a one-time restore from a previous backup. */
    void maybeAskRestore(){
        SharedPreferences p=prefs();
        if(!data.isEmpty()||p.getBoolean(BackupManager.PREF_ASKED,false)) return;
        p.edit().putBoolean(BackupManager.PREF_ASKED,true).apply();
        new AlertDialog.Builder(this).setTitle("بازیابی اطلاعات قبلی؟")
            .setMessage("اگر قبلاً از مأموریت‌ها پشتیبان گرفته‌اید (مثلاً قبل از حذف برنامه)، می‌توانید فایل پشتیبان را انتخاب و بازیابی کنید.\n\nبعداً هم از «پشتیبان‌گیری و بازیابی اطلاعات» در صفحهٔ اصلی می‌شود.")
            .setPositiveButton("انتخاب فایل پشتیبان",(d,w)->pickRestoreFile())
            .setNegativeButton("شروع بدون اطلاعات",null).show();
    }

    void confirmDeleteAll(){
        final boolean hasTree=prefs().getString(BackupManager.PREF_TREE,null)!=null;
        AlertDialog.Builder b=new AlertDialog.Builder(this).setTitle("حذف همهٔ اطلاعات")
            .setMessage("همهٔ مأموریت‌های داخل برنامه پاک می‌شود."+(hasTree?"\n\n• «فقط اطلاعات برنامه»: فایل پشتیبان در پوشهٔ شما می‌ماند و پشتیبان‌گیری خودکار متوقف می‌شود.\n• «اطلاعات و پشتیبان»: فایل پشتیبان هم حذف می‌شود.":"\n\nاین کار قابل بازگشت نیست."))
            .setNegativeButton("لغو",null);
        if(hasTree){
            b.setPositiveButton("فقط اطلاعات برنامه",(d,w)->{ data.clear(); saveLocal(); prefs().edit().remove(BackupManager.PREF_TREE).remove(BackupManager.PREF_LAST).apply(); msg("اطلاعات برنامه حذف شد؛ فایل پشتیبان باقی ماند"); home(); });
            b.setNeutralButton("اطلاعات و پشتیبان",(d,w)->{
                final Uri tree=Uri.parse(prefs().getString(BackupManager.PREF_TREE,"")); final Context app=getApplicationContext();
                data.clear(); saveLocal(); prefs().edit().remove(BackupManager.PREF_TREE).remove(BackupManager.PREF_LAST).apply();
                BK.execute(()->BackupManager.deleteBackup(app,tree));
                msg("اطلاعات و فایل پشتیبان حذف شد"); home();
            });
        } else {
            b.setPositiveButton("حذف",(d,w)->{ data.clear(); saveLocal(); msg("اطلاعات حذف شد"); home(); });
        }
        b.show();
    }

    void backupScreen(){
        base("پشتیبان‌گیری و بازیابی"); backupBox=box;
        final SharedPreferences p=prefs();
        final boolean on=p.getString(BackupManager.PREF_TREE,null)!=null;
        String last=p.getString(BackupManager.PREF_LAST,"");
        LinearLayout c=section("وضعیت پشتیبان خودکار");
        String status;
        if(!on) status="غیرفعال — اطلاعات فقط داخل برنامه است";
        else if(p.getBoolean(BackupManager.PREF_ERR,false)) status="آخرین پشتیبان‌گیری ناموفق بود؛ پوشه را دوباره انتخاب کنید";
        else status="فعال"+(last.isEmpty()?"":"\nآخرین پشتیبان: "+last);
        TextView st=tv(status,16); st.setTypeface(Typeface.DEFAULT,Typeface.BOLD); st.setTextColor(on&&!p.getBoolean(BackupManager.PREF_ERR,false)?green:red); c.addView(st);
        TextView info=tv("اطلاعات مأموریت‌ها داخل حافظهٔ خصوصی برنامه ذخیره می‌شود و با حذف برنامه پاک می‌شود. با انتخاب یک پوشه (مثلاً پوشهٔ Documents یا یک پوشهٔ جدید)، بعد از هر ذخیرهٔ مأموریت یک فایل پشتیبان در آن پوشه نوشته می‌شود که با حذف برنامه از بین نمی‌رود.\n\nبعد از نصب دوبارهٔ برنامه، «بازیابی از فایل پشتیبان» را بزنید و همان فایل را انتخاب کنید، یا همان پوشه را دوباره انتخاب کنید.\n\nفایل پشتیبان متن ساده است؛ آن را در جای امنی نگه دارید. اگر گوشی اجازهٔ انتخاب پوشه‌ای را نداد، یک پوشهٔ جدید بسازید.",14);
        info.setTextColor(muted); info.setPadding(dp(4),dp(10),dp(4),dp(4)); c.addView(info);

        LinearLayout a=section("عملیات");
        Button main=bt(on?"پشتیبان‌گیری همین حالا":"انتخاب پوشهٔ پشتیبان"); main.setOnClickListener(v->{ if(on) backupNow(); else pickFolder(); }); addBtn(a,main);
        if(on){ Button ch=secondary("تغییر پوشهٔ پشتیبان"); ch.setOnClickListener(v->pickFolder()); addBtn(a,ch); }
        Button rs=secondary("بازیابی از فایل پشتیبان"); rs.setOnClickListener(v->pickRestoreFile()); addBtn(a,rs);
        Button del=secondary("حذف همهٔ اطلاعات…"); del.setTextColor(red); del.setOnClickListener(v->confirmDeleteAll()); addBtn(a,del);
        Button back=secondary("‹  بازگشت"); back.setOnClickListener(v->home()); box.addView(back);
    }

    void addBtn(LinearLayout parent,Button b){
        parent.addView(b,new LinearLayout.LayoutParams(-1,-2));
        Space sp=new Space(this); parent.addView(sp,new LinearLayout.LayoutParams(dp(1),dp(8)));
    }

    void toast(String s){Toast.makeText(this,"خطا: "+(s==null?"اطلاعات نامعتبر":s),Toast.LENGTH_LONG).show();}

    void attachDatePicker(final EditText target){
        target.setFocusable(false); target.setClickable(true); target.setCursorVisible(false);
        target.setOnClickListener(v->showJalaliDatePicker(target));
    }

    boolean jalaliLeap(int y){int r=y%33; return r==1||r==5||r==9||r==13||r==17||r==22||r==26||r==30;}
    void showJalaliDatePicker(final EditText target){
        int jy=1405,jm=1,jd=1;
        int[] curj=gregorianToJalali(Calendar.getInstance().get(Calendar.YEAR),Calendar.getInstance().get(Calendar.MONTH)+1,Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        if(curj!=null){jy=curj[0];jm=curj[1];jd=curj[2];}
        try{String[] p=target.getText().toString().replace("-","/").split("/");if(p.length==3){jy=Integer.parseInt(p[0]);jm=Integer.parseInt(p[1]);jd=Integer.parseInt(p[2]);}}catch(Exception ignored){}
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.HORIZONTAL);root.setGravity(Gravity.CENTER);root.setPadding(dp(12),dp(8),dp(12),dp(8));root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        Spinner ys=spinner(new String[]{"1403","1404","1405","1406","1407","1408","1409","1410","1411","1412","1413","1414","1415"});
        Spinner ms=spinner(new String[]{"فروردین","اردیبهشت","خرداد","تیر","مرداد","شهریور","مهر","آبان","آذر","دی","بهمن","اسفند"});
        Spinner ds=spinner(new String[]{"1","2","3","4","5","6","7","8","9","10","11","12","13","14","15","16","17","18","19","20","21","22","23","24","25","26","27","28","29","30","31"});
        ys.setSelection(Math.max(0,Math.min(12,jy-1403)));ms.setSelection(Math.max(0,jm-1));ds.setSelection(Math.max(0,jd-1));
        root.addView(ds,new LinearLayout.LayoutParams(0,dp(58),1));root.addView(ms,new LinearLayout.LayoutParams(0,dp(58),1));root.addView(ys,new LinearLayout.LayoutParams(0,dp(58),1));
        AlertDialog dlg=new AlertDialog.Builder(this).setTitle("انتخاب تاریخ شمسی").setView(root).setNegativeButton("انصراف",null).setPositiveButton("تأیید",null).create();
        dlg.setOnShowListener(x->dlg.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{int y=1403+ys.getSelectedItemPosition(),m=1+ms.getSelectedItemPosition(),d=1+ds.getSelectedItemPosition();int max=m<=6?31:(m<=11?30:(jalaliLeap(y)?30:29));if(d>max)d=max;target.setText(String.format(Locale.US,"%04d/%02d/%02d",y,m,d));dlg.dismiss();}));
        dlg.show();
    }

    int[] jalaliToGregorianSafe(String value){
        try{
            String[] p=value.trim().replace("-","/").split("/");
            if(p.length!=3)return null;
            int jy=Integer.parseInt(p[0]),jm=Integer.parseInt(p[1]),jd=Integer.parseInt(p[2]);
            if(jy<1200||jy>1600||jm<1||jm>12||jd<1||jd>31)return null;
            int jy2=jy-979;
            int days=365*jy2+(jy2/33)*8+((jy2%33)+3)/4;
            if(jm<=6) days+=(jm-1)*31; else days+=(jm-7)*30+186;
            days+=jd-1;
            int gdn=days+79;
            int gy=1600+400*(gdn/146097); gdn%=146097;
            boolean leap=true;
            if(gdn>=36525){gdn--;gy+=100*(gdn/36524);gdn%=36524;if(gdn>=365)gdn++;else leap=false;}
            gy+=4*(gdn/1461);gdn%=1461;
            if(gdn>=366){leap=false;gdn--;gy+=gdn/365;gdn%=365;}
            int[] md={31,(leap?29:28),31,30,31,30,31,31,30,31,30,31};
            int gm=1;while(gm<=12&&gdn>=md[gm-1]){gdn-=md[gm-1];gm++;}
            return new int[]{gy,gm,gdn+1};
        }catch(Exception e){return null;}
    }

    int[] gregorianToJalali(int gy,int gm,int gd){int[] gdm={0,31,28,31,30,31,30,31,31,30,31,30,31};int gy2=gy-1600,gm2=gm-1,gd2=gd-1;int gdn=365*gy2+(gy2+3)/4-(gy2+99)/100+(gy2+399)/400;for(int i=0;i<gm2;i++)gdn+=gdm[i];if(gm2>1&&((gy%4==0&&gy%100!=0)||gy%400==0))gdn++;gdn+=gd2;int jdn=gdn-79;int j_np=jdn/12053;jdn%=12053;int jy=979+33*j_np+4*(jdn/1461);jdn%=1461;if(jdn>=366){jy+=(jdn-1)/365;jdn=(jdn-1)%365;}int jm=jdn<186?1+jdn/31:7+(jdn-186)/30;int jd=1+(jdn%(jdn<186?31:30));return new int[]{jy,jm,jd};}
    int days360(int sy,int sm,int sd,int ey,int em,int ed){if(sd==31)sd=30;if(ed==31&&(sd==30||sd==31))ed=30;return 360*(ey-sy)+30*(em-sm)+(ed-sd);}
    void validate7(String end){if(end==null||end.trim().isEmpty())return;try{String[] p=end.trim().replace("-","/").split("/");if(p.length!=3)return;int y=Integer.parseInt(p[0]),mo=Integer.parseInt(p[1]),da=Integer.parseInt(p[2]);int[] now=gregorianToJalali(Calendar.getInstance().get(Calendar.YEAR),Calendar.getInstance().get(Calendar.MONTH)+1,Calendar.getInstance().get(Calendar.DAY_OF_MONTH));int d;if(y>=1200&&y<1700)d=days360(y,mo,da,now[0],now[1],now[2]);else{Calendar e=Calendar.getInstance();e.setLenient(false);e.set(y,mo-1,da,0,0,0);e.getTime();Calendar n=Calendar.getInstance();n.set(Calendar.HOUR_OF_DAY,0);n.set(Calendar.MINUTE,0);n.set(Calendar.SECOND,0);n.set(Calendar.MILLISECOND,0);d=(int)((n.getTimeInMillis()-e.getTimeInMillis())/86400000L);}if(d>7)throw new IllegalArgumentException("بیشتر از ۷ روز از مأموریت گذشته و هزینه‌ها طبق Excel قابل پرداخت نیست");}catch(NumberFormatException x){}catch(IllegalArgumentException x){if(x.getMessage()!=null&&x.getMessage().contains("بیشتر از"))throw x;}}



    void pdf(JSONObject m){
        PdfGenerator.generateAndShare(this, m);
    }
}
