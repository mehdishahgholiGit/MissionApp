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
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import org.json.*;
import java.io.*;
import java.util.*;

public class MainActivity extends Activity {
    LinearLayout box;
    JSONObject cur;
    ArrayList<JSONObject> data = new ArrayList<>();

    final int DAILY=39000000, MULTI=35000000, SUBURB=15000000, HOLIDAY=16000000;

    String[] people={"احسان صنعتی","الهه ناجی","امیرمحمد گلک","امین ترابی نیا","بنفشه رئیسی دهکردی","بهمن لاله زارزاده","ستوده ملکی","سعید سوهانی","سوگند حداد","سهیل قاسم پور مطلق","سیدعلی سعادتمندی","علی رزاقی","فرزاد میرآبادی دستگردی","گویک کارپیانس","مریم فریدون فاراب","معصومه غلامی","مهدی جوادی مجره","مهدی شاهقلی دستگردی","مهدی فراهانی","مهدی مردانپورجرتوده","نیما فیروزان بلسی"};
    String[] devices={"ADVIA 2120","ADVIA 2120i","ADVIA 360","ADVIA 560","ADVIA Centaur CP","ADVIA Centaur XP","ADVIA Centaur XPT","ADVIA Chemistry 1800","Atellica CH 930 Analyzer","Atellica IM 1300 Analyzer","Atellica IM 1600 Analyzer","Atellica MagLine","Atellica Solution","Atellica UAS 800","BEP2000","BFT II","CLINITEK ADVANTUS","CLINITEK Novus","CLINITEK STATUS","epoc Host","epoc Reader","IMMULITE 2000","IMMULITE 2000 XPi","PCR QS 5","PFA-200","Prisca","RAPIDCHEM 744","RAPIDCHEM 754","RAPIDPoint 500","RL348EX","VersaCell X3"};
    String[] payers={"شخص","شرکت"};
    String[] cats={"هزینه شهر تهران","هزینه تردد بین شهری","هزینه تردد درون شهری","سایر هزینه ها"};
    String[] tehranItems={"بازار (خرید لوازم و ابزار کار)","ترمینال یا فرودگاه به منزل","سایت مشتری به سایت مشتری","سایت مشتری به شرکت","سایت مشتری به منزل","شرکت به ترمینال یا فرودگاه","شرکت به سایت مشتری","شرکت به منزل","فرودگاه یا ترمینال به شرکت","منزل به ترمینال یا فرودگاه","منزل به سایت مشتری","منزل به شرکت"};

    int bg=Color.rgb(246,248,251), card=Color.WHITE, primary=Color.rgb(24,82,140), primaryDark=Color.rgb(17,62,106), border=Color.rgb(215,222,230), text=Color.rgb(35,43,52), muted=Color.rgb(92,104,117), green=Color.rgb(31,116,72), red=Color.rgb(176,48,48);
    boolean darkMode=false;

    void applyColors(){
        darkMode=getPreferences(0).getBoolean("darkMode",false);
        if(darkMode){
            bg=Color.rgb(18,20,24); card=Color.rgb(30,34,40); primary=Color.rgb(66,133,210); primaryDark=Color.rgb(190,210,235);
            border=Color.rgb(70,77,88); text=Color.rgb(238,241,245); muted=Color.rgb(178,187,198); green=Color.rgb(94,201,137); red=Color.rgb(244,112,112);
        } else {
            bg=Color.rgb(246,248,251); card=Color.WHITE; primary=Color.rgb(24,82,140); primaryDark=Color.rgb(17,62,106);
            border=Color.rgb(215,222,230); text=Color.rgb(35,43,52); muted=Color.rgb(92,104,117); green=Color.rgb(31,116,72); red=Color.rgb(176,48,48);
        }
    }

    void toggleDarkMode(){
        getPreferences(0).edit().putBoolean("darkMode",!darkMode).apply();
        recreate();
    }

    public void onCreate(Bundle b){ super.onCreate(b); applyColors(); load(); home(); }

    TextView tv(String s,float z){
        TextView v=new TextView(this); v.setText(s); v.setTextSize(z); v.setTextColor(text);
        v.setPadding(4,4,4,4); v.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);
        v.setLayoutDirection(View.LAYOUT_DIRECTION_RTL); return v;
    }

    TextView label(String s){
        TextView v=tv(s,13); v.setTextColor(muted); v.setPadding(2,0,2,5);
        return v;
    }

    GradientDrawable shape(int color,float radius,int strokeColor){
        GradientDrawable g=new GradientDrawable(); g.setColor(color); g.setCornerRadius(radius);
        if(strokeColor!=Color.TRANSPARENT) g.setStroke(1,strokeColor); return g;
    }

    EditText ed(String hint){
        EditText v=new EditText(this); v.setHint(hint); v.setHintTextColor(Color.rgb(145,153,162));
        v.setTextSize(15); v.setTextColor(text); v.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);
        v.setSingleLine(true); v.setPadding(14,0,14,0); v.setBackground(shape(card,14,border));
        v.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        if(hint.contains("هزینه")||hint.contains("مبلغ")||hint.contains("روز")||hint.contains("شماره"))
            v.setInputType(android.text.InputType.TYPE_CLASS_NUMBER|android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        return v;
    }

    Button bt(String s){
        Button v=new Button(this); v.setText(s); v.setTextSize(14); v.setAllCaps(false); v.setTextColor(Color.WHITE);
        v.setPadding(12,4,12,4); v.setMinHeight(60); v.setGravity(Gravity.CENTER);
        v.setBackground(shape(primary,14,Color.TRANSPARENT)); return v;
    }

    Button secondary(String s){
        Button v=bt(s); v.setTextColor(primary); v.setBackground(shape(card,14,primary)); v.setMinHeight(60); v.setTextSize(15); return v;
    }

    Spinner spinner(String[] a){
        Spinner s=new Spinner(this); s.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,a));
        s.setBackground(shape(Color.WHITE,14,border)); s.setPadding(10,0,10,0); s.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        return s;
    }

    LinearLayout field(String name,View v){
        LinearLayout l=new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL); l.setPadding(0,7,0,7);
        TextView lab=label(name);
        lab.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        lab.setTextColor(primaryDark);
        lab.setPadding(10,5,10,5);
        lab.setBackground(shape(darkMode?Color.rgb(48,55,65):Color.rgb(235,240,246),8,Color.TRANSPARENT));
        l.addView(lab,new LinearLayout.LayoutParams(-1,32));
        LinearLayout.LayoutParams vp=new LinearLayout.LayoutParams(-1,52); vp.topMargin=4;
        l.addView(v,vp);
        return l;
    }

    LinearLayout section(String title){
        LinearLayout c=new LinearLayout(this); c.setOrientation(LinearLayout.VERTICAL); c.setPadding(16,14,16,14);
        c.setBackground(shape(card,18,border)); c.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        TextView h=tv(title,18); h.setTextColor(primaryDark); h.setTypeface(Typeface.DEFAULT,Typeface.BOLD); h.setPadding(0,0,0,10);
        c.addView(h); box.addView(c,new LinearLayout.LayoutParams(-1,-2));
        Space sp=new Space(this); box.addView(sp,new LinearLayout.LayoutParams(1,10)); return c;
    }

    LinearLayout row(){
        LinearLayout r=new LinearLayout(this); r.setOrientation(LinearLayout.HORIZONTAL); r.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        return r;
    }

    void add2(LinearLayout r,String n1,View v1,String n2,View v2){
        LinearLayout a=field(n1,v1),b=field(n2,v2);
        r.addView(a,new LinearLayout.LayoutParams(0,-2,1)); r.addView(b,new LinearLayout.LayoutParams(0,-2,1));
    }

    void base(String title){
        box=new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL); box.setPadding(12,12,12,24);
        box.setBackgroundColor(bg); box.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        ScrollView root=new ScrollView(this); root.setFillViewport(true); root.setBackgroundColor(bg); root.addView(box);
        setContentView(root);
        LinearLayout bar=new LinearLayout(this); bar.setPadding(14,10,14,10); bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setBackground(shape(primaryDark,16,Color.TRANSPARENT));
        TextView h=tv(title,21); h.setTextColor(Color.WHITE); h.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        bar.addView(h,new LinearLayout.LayoutParams(0,60,1)); box.addView(bar);
        Space s=new Space(this); box.addView(s,new LinearLayout.LayoutParams(1,12));
    }

    String money(double x){ return String.format(Locale.US,"%,.2f",x); }
    String moneyInt(double x){ return String.format(Locale.US,"%,.0f",x); }

    String words(long n){
        String[] u={"صفر","یک","دو","سه","چهار","پنج","شش","هفت","هشت","نه","ده","یازده","دوازده","سیزده","چهارده","پانزده","شانزده","هفده","هجده","نوزده","بیست","بیست و یک","بیست و دو","بیست و سه","بیست و چهار","بیست و پنج","بیست و شش","بیست و هفت","بیست و هشت","بیست و نه"};
        String[] t={"","","بیست","سی","چهل","پنجاه","شصت","هفتاد","هشتاد","نود"};
        String[] h={"","", "صد","دویست","سیصد","چهارصد","پانصد","ششصد","هفتصد","هشتصد","نهصد"};
        if(n<30)return u[(int)n]; if(n<100)return t[(int)n/10]+(n%10>0?" و "+u[(int)n%10]:"");
        if(n<1000)return h[(int)n/100]+(n%100>0?" و "+words(n%100):"");
        if(n<1000000)return words(n/1000)+" هزار"+(n%1000>0?" و "+words(n%1000):"");
        if(n<1000000000)return words(n/1000000)+" میلیون"+(n%1000000>0?" و "+words(n%1000000):"");
        return words(n/1000000000)+" میلیارد"+(n%1000000000>0?" و "+words(n%1000000000):"");
    }

    String amountWords(double v){ return words(Math.round(v))+" ریال"; }
    double num(EditText e){ try{return Double.parseDouble(e.getText().toString().replace(",",""));}catch(Exception x){return 0;} }

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
        TextView intro=tv("فرم مأموریت، محاسبات و روکش سند حسابداری",14); intro.setTextColor(muted); intro.setPadding(4,0,4,12); box.addView(intro);

        Button mode=secondary(darkMode?"☀  حالت روشن":"☾  حالت تاریک"); mode.setOnClickListener(v->toggleDarkMode()); box.addView(mode);
        Space modeSp=new Space(this); box.addView(modeSp,new LinearLayout.LayoutParams(1,8));

        Button n=bt("＋  ثبت مأموریت جدید"); n.setOnClickListener(v->{cur=new JSONObject();try{cur.put("expenses",new JSONArray());}catch(Exception e){}edit();}); box.addView(n);
        Space sp=new Space(this); box.addView(sp,new LinearLayout.LayoutParams(1,12));

        TextView count=tv("مأموریت‌های ثبت‌شده  ("+data.size()+")",17); count.setTypeface(Typeface.DEFAULT,Typeface.BOLD); box.addView(count);
        Space sp2=new Space(this); box.addView(sp2,new LinearLayout.LayoutParams(1,6));

        if(data.isEmpty()){
            LinearLayout empty=new LinearLayout(this); empty.setGravity(Gravity.CENTER); empty.setPadding(20,30,20,30); empty.setBackground(shape(card,18,border));
            TextView e=tv("هنوز مأموریتی ثبت نشده است\nبرای شروع، «ثبت مأموریت جدید» را بزنید.",15); e.setGravity(Gravity.CENTER); e.setTextColor(muted); empty.addView(e); box.addView(empty);
        } else {
            for(JSONObject m:data){
                LinearLayout c=new LinearLayout(this); c.setOrientation(LinearLayout.VERTICAL); c.setPadding(16,12,16,12); c.setBackground(shape(card,16,border));
                TextView a=tv(m.optString("subject","بدون موضوع"),16); a.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
                TextView b=tv("مقصد: "+m.optString("destination","")+"    |    کارشناس: "+m.optString("person",""),13); b.setTextColor(muted);
                TextView z=tv("جمع قابل پرداخت: "+money(total(m))+" ریال",14); z.setTextColor(green); z.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
                c.addView(a); c.addView(b); c.addView(z);
                c.setOnClickListener(v->{cur=m;edit();}); box.addView(c);
                Space q=new Space(this); box.addView(q,new LinearLayout.LayoutParams(1,8));
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
        TextView sum=tv("جمع قابل پرداخت: "+money(total(cur))+" ریال",18);sum.setTextColor(green);sum.setTypeface(Typeface.DEFAULT,Typeface.BOLD);sum.setGravity(Gravity.CENTER);sum.setPadding(5,14,5,14);s4.addView(sum);
        Button ex=bt("＋  افزودن ردیف هزینه");ex.setOnClickListener(v->expenseDialog(exList,sum));s4.addView(ex);render(exList,sum);

        LinearLayout actions=section("عملیات");
        Button save=bt("✓  ذخیره مأموریت"); save.setOnClickListener(v->{
            try{
                validate7(end.getText().toString());
                cur.put("form",form.getText().toString());cur.put("service",service.getText().toString());cur.put("person",ps.getSelectedItem().toString());cur.put("device",ds.getSelectedItem().toString());cur.put("subject",subject.getText().toString());cur.put("destination",dest.getText().toString());cur.put("start",start.getText().toString());cur.put("end",end.getText().toString());cur.put("startTime",startTime.getText().toString());cur.put("endTime",endTime.getText().toString());cur.put("days",num(days));cur.put("holiday",num(holiday));cur.put("suburb",suburb.isChecked());cur.put("hotel",num(hotel));cur.put("ticketGo",num(go));cur.put("ticketBack",num(back));cur.put("cancelGo",num(cancelGo));cur.put("cancelBack",num(cancelBack));cur.put("goNo",goNo.getText().toString());cur.put("backNo",backNo.getText().toString());cur.put("hotelPayer",hp.getSelectedItem().toString());cur.put("goPayer",gp.getSelectedItem().toString());cur.put("backPayer",bp.getSelectedItem().toString());cur.put("cancelGoPayer",cgp.getSelectedItem().toString());cur.put("cancelBackPayer",cbp.getSelectedItem().toString());
                if(!data.contains(cur))data.add(cur); save(); Toast.makeText(this,"مأموریت ذخیره شد",Toast.LENGTH_SHORT).show(); home();
            }catch(Exception e){toast(e.getMessage());}
        });
        Button pdf=secondary("▣  تولید PDF با فرم رسمی"); pdf.setOnClickListener(v->pdf(cur));
        Button backBtn=secondary("‹  بازگشت"); backBtn.setOnClickListener(v->home());
        actions.addView(save); Space x=new Space(this);actions.addView(x,new LinearLayout.LayoutParams(1,6));actions.addView(pdf);Space y=new Space(this);actions.addView(y,new LinearLayout.LayoutParams(1,6));actions.addView(backBtn);
    }

    void expenseDialog(LinearLayout list,TextView sum){
        LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);l.setPadding(10,4,10,4);
        Spinner cat=spinner(cats),ti=spinner(tehranItems); l.addView(field("نوع هزینه",cat));l.addView(field("شرح/مورد تهران",ti));
        EditText date=ed("تاریخ"),desc=ed("شرح"),invoice=ed("شماره فاکتور"),amount=ed("مبلغ");
        attachDatePicker(date); l.addView(field("تاریخ",date));l.addView(field("شرح",desc));l.addView(field("شماره فاکتور",invoice));l.addView(field("مبلغ",amount));
        cat.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){public void onItemSelected(android.widget.AdapterView<?> p,View v,int pos,long id){ti.setVisibility(pos==0?View.VISIBLE:View.GONE);}public void onNothingSelected(android.widget.AdapterView<?> p){}});
        new AlertDialog.Builder(this).setTitle("افزودن هزینه").setView(l).setNegativeButton("انصراف",null).setPositiveButton("ثبت هزینه",(d,w)->{
            try{JSONArray a=cur.getJSONArray("expenses");JSONObject o=new JSONObject();o.put("category",cat.getSelectedItem().toString());o.put("tehranItem",ti.getSelectedItem().toString());o.put("date",date.getText().toString());o.put("desc",desc.getText().toString());o.put("invoice",invoice.getText().toString());o.put("amount",num(amount));a.put(o);render(list,sum);}catch(Exception e){toast(e.getMessage());}
        }).show();
    }

    void render(LinearLayout list,TextView sum){
        list.removeAllViews(); JSONArray a=cur.optJSONArray("expenses");
        if(a!=null) for(int i=0;i<a.length();i++){
            final int k=i; JSONObject o=a.optJSONObject(i);
            LinearLayout r=new LinearLayout(this);r.setOrientation(LinearLayout.HORIZONTAL);r.setGravity(Gravity.CENTER_VERTICAL);r.setPadding(8,6,8,6);r.setBackground(shape(Color.rgb(249,250,252),12,border));
            TextView t=tv((i+1)+"  "+o.optString("category")+"\n"+o.optString("desc")+"   |   "+money(o.optDouble("amount"))+" ریال",13);t.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            Button del=secondary("حذف");del.setTextColor(red);del.setMinHeight(44);del.setOnClickListener(v->{a.remove(k);render(list,sum);});
            r.addView(t,new LinearLayout.LayoutParams(0,-2,1));r.addView(del,new LinearLayout.LayoutParams(80,48));list.addView(r);Space s=new Space(this);list.addView(s,new LinearLayout.LayoutParams(1,6));
        }
        sum.setText("جمع قابل پرداخت: "+money(total(cur))+" ریال");
    }

    void save(){try{JSONArray a=new JSONArray();for(JSONObject m:data)a.put(m);getPreferences(0).edit().putString("data",a.toString()).apply();}catch(Exception e){}}
    void load(){try{JSONArray a=new JSONArray(getPreferences(0).getString("data","[]"));for(int i=0;i<a.length();i++)data.add(a.getJSONObject(i));}catch(Exception e){}}
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
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.HORIZONTAL);root.setGravity(Gravity.CENTER);root.setPadding(12,8,12,8);root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        Spinner ys=spinner(new String[]{"1403","1404","1405","1406","1407","1408","1409","1410","1411","1412","1413","1414","1415"});
        Spinner ms=spinner(new String[]{"فروردین","اردیبهشت","خرداد","تیر","مرداد","شهریور","مهر","آبان","آذر","دی","بهمن","اسفند"});
        Spinner ds=spinner(new String[]{"1","2","3","4","5","6","7","8","9","10","11","12","13","14","15","16","17","18","19","20","21","22","23","24","25","26","27","28","29","30","31"});
        ys.setSelection(Math.max(0,Math.min(12,jy-1403)));ms.setSelection(Math.max(0,jm-1));ds.setSelection(Math.max(0,jd-1));
        root.addView(ds,new LinearLayout.LayoutParams(0,58,1));root.addView(ms,new LinearLayout.LayoutParams(0,58,1));root.addView(ys,new LinearLayout.LayoutParams(0,58,1));
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

    Paint pdfPaint(float size,boolean bold){
        Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(Color.BLACK); p.setTextSize(size);
        if(bold)p.setTypeface(Typeface.DEFAULT_BOLD);
        return p;
    }

    void pdfText(Canvas c,String s,float x,float y,float width,float height,float size,boolean bold,boolean center){
        Paint p=pdfPaint(size,bold);
        android.text.TextPaint tp=new android.text.TextPaint(p);
        tp.setTextAlign(center?Paint.Align.CENTER:Paint.Align.RIGHT);
        android.text.StaticLayout sl=new android.text.StaticLayout(
                s==null?"":s,tp,Math.max(20,(int)width),
                center?android.text.Layout.Alignment.ALIGN_CENTER:android.text.Layout.Alignment.ALIGN_OPPOSITE,
                1.0f,0,false);
        c.save();
        c.translate(center?x-width/2:x-5,y+4);
        sl.draw(c);
        c.restore();
    }

    void excelBox(Canvas c,String s,float l,float t,float r,float b,boolean fill,boolean bold){
        Paint q=pdfPaint(8,false);
        q.setStyle(Paint.Style.FILL);
        q.setColor(fill?Color.rgb(224,224,224):Color.WHITE);
        c.drawRect(l,t,r,b,q);
        q.setStyle(Paint.Style.STROKE); q.setStrokeWidth(1.2f); q.setColor(Color.BLACK);
        c.drawRect(l,t,r,b,q);
        pdfText(c,s,r,t,r-l-8,b-t,9,bold,false);
    }

    void excelLabel(Canvas c,String label,String value,float l,float t,float r,float b){
        Paint q=pdfPaint(8,false); q.setStyle(Paint.Style.FILL); q.setColor(Color.rgb(224,224,224)); c.drawRect(l,t,r,b,q);
        q.setStyle(Paint.Style.STROKE); q.setStrokeWidth(1.2f); q.setColor(Color.BLACK); c.drawRect(l,t,r,b,q);
        float split=r-92;
        c.drawLine(split,t,split,b,q);
        pdfText(c,label,r-5,t,82,b-t,8,true,false);
        pdfText(c,value,split-5,t,split-l-8,b-t,9,false,false);
    }

    void excelSection(Canvas c,String title,float l,float y,float r){
        Paint q=pdfPaint(9,true); q.setTextAlign(Paint.Align.RIGHT); c.drawText(title,r,y,q);
        q.setStyle(Paint.Style.STROKE); q.setStrokeWidth(1.2f); q.setColor(Color.BLACK);
        c.drawLine(l,y+5,r,y+5,q);
    }

    void excelCheckbox(Canvas c,String text,float x,float y,boolean checked){
        Paint q=pdfPaint(9,false);q.setStyle(Paint.Style.STROKE);q.setColor(Color.BLACK);q.setStrokeWidth(1);
        c.drawRect(x,y-10,x+10,y,q);
        if(checked){q.setStrokeWidth(1.8f);c.drawLine(x+2,y-5,x+5,y-2,q);c.drawLine(x+5,y-2,x+9,y-8,q);}
        q.setStyle(Paint.Style.FILL);q.setTextAlign(Paint.Align.RIGHT);c.drawText(text,x-6,y,q);
    }

    void excelFooter(Canvas c,int W){
        Paint p=pdfPaint(7,false);p.setTextAlign(Paint.Align.CENTER);
        c.drawText("A01AB098-2011-48B1-8600-AE558395FF54",W/2,815,p);
        p.setTextAlign(Paint.Align.RIGHT);c.drawText("1405/07/15",W-42,815,p);
    }

    void drawPaymentsPage(Canvas c,JSONObject m,int W,int H){
        int L=44,R=W-44;
        Paint p=pdfPaint(9,true);p.setTextAlign(Paint.Align.CENTER);
        c.drawText("فرم هزینه ماموریت",W/2,30,p);
        Paint q=pdfPaint(8,false);q.setTextAlign(Paint.Align.RIGHT);c.drawText("75F0203-B",R,24,q);
        Paint title=pdfPaint(26,false);title.setTextAlign(Paint.Align.CENTER);c.drawText("*M*",W/2,62,title);
        Paint company=pdfPaint(12,true);company.setTextAlign(Paint.Align.RIGHT);c.drawText("فن آوری آزمایشگاهی",R,55,company);
        excelBox(c,"به فیلدهایی که قرمز میشوند توجه کنید.",L,72,230,103,true,true);
        excelLabel(c,"شماره فرم",m.optString("form"),R-255,72,R,103);
        excelLabel(c,"شماره سرویس",m.optString("service"),L,108,235,130);
        excelLabel(c,"بخش","خدمات پس از فروش، زیمنس آزمایشگاهی",235,108,R,130);
        excelLabel(c,"تاریخ",m.optString("start"),L,131,235,153);
        excelLabel(c,"الی",m.optString("end"),235,131,R,153);
        excelLabel(c,"نام کارشناس",m.optString("person"),L,154,235,176);
        excelLabel(c,"کمپانی","SIEMENS",235,154,R,176);
        excelLabel(c,"موضوع",m.optString("subject"),L,177,235,199);
        excelLabel(c,"دستگاه",m.optString("device"),235,177,R,199);
        excelLabel(c,"مقصد",m.optString("destination"),235,200,R,222);
        excelSection(c,"وضعیت سفر",L,236,R);
        excelCheckbox(c,"زمینی",R-75,257,m.optBoolean("ground",false));
        excelCheckbox(c,"هوایی",R-75,279,m.optBoolean("air",false));
        excelCheckbox(c,"کنسلی",R-75,301,m.optBoolean("cancel",false));
        excelLabel(c,"هزینه تردد بین شهری",money(expenseSum(m,"هزینه تردد بین شهری"))+" ریال",L,246,355,268);
        excelLabel(c,"هزینه بلیط رفت",money(m.optDouble("ticketGo"))+" ریال",L,269,355,291);
        excelLabel(c,"هزینه بلیط برگشت",money(m.optDouble("ticketBack"))+" ریال",L,292,355,314);
        excelLabel(c,"هزینه کنسلی رفت",money(m.optDouble("cancelGo"))+" ریال",L,315,355,337);
        excelLabel(c,"هزینه کنسلی برگشت",money(m.optDouble("cancelBack"))+" ریال",L,338,355,360);
        excelLabel(c,"شماره بلیط رفت",m.optString("goNo"),355,269,R,291);
        excelLabel(c,"شماره بلیط برگشت",m.optString("backNo"),355,292,R,314);
        excelSection(c,"زمان بندی سفر",L,374,R);
        excelLabel(c,"تاریخ شروع",m.optString("start"),L,382,300,404);
        excelLabel(c,"تاریخ برگشت",m.optString("end"),300,382,R,404);
        excelLabel(c,"زمان شروع",m.optString("startTime"),L,405,300,427);
        excelLabel(c,"زمان اتمام",m.optString("endTime"),300,405,R,427);
        excelSection(c,"هزینه سفر",L,442,R);
        excelLabel(c,"هتل",money(m.optDouble("hotel"))+" ریال / "+payer(m,"hotelPayer"),L,449,300,471);
        excelLabel(c,"تردد",money(expenseSum(m,"هزینه تردد درون شهری")+expenseSum(m,"هزینه تردد بین شهری"))+" ریال",300,449,R,471);
        excelLabel(c,"حقوق روزانه",money(missionPay(m))+" ریال",L,472,300,494);
        excelLabel(c,"متفرقه",money(expenseSum(m,"سایر هزینه ها"))+" ریال",300,472,R,494);
        excelLabel(c,"تعداد روزهای ماموریت",String.valueOf(m.optDouble("days",1)),L,495,300,517);
        excelLabel(c,"تعداد روزهای تعطیل",String.valueOf(m.optDouble("holiday",0)),300,495,R,517);
        excelLabel(c,"جمع کل",money(total(m))+" ریال",L,518,R,542);
        excelSection(c,"امضاء کارشناس / تایید مدیر گروه",L,555,R);
        excelBox(c,"پرداخت هزینه فوق بلا مانع است",L,561,R,594,false,false);
        excelLabel(c,"امضاء","",L,595,R,617);
        excelSection(c,"پرداخت",L,632,R);
        excelBox(c,"مبلغ "+money(total(m))+" ریال بابت حق علی الحساب ماموریت فوق به اینجانب پرداخت گردید.",L,640,R,683,false,false);
        excelLabel(c,"نام و نام خانوادگی",m.optString("person"),L,684,300,706);
        excelLabel(c,"تاریخ",m.optString("end"),300,684,R,706);
        excelBox(c,"مبلغ "+money(total(m))+" ریال بابت تسویه ماموریت فوق به اینجانب پرداخت گردید.",L,707,R,750,false,false);
        excelLabel(c,"نام و نام خانوادگی",m.optString("person"),L,751,300,773);
        excelLabel(c,"تاریخ",m.optString("end"),300,751,R,773);
        excelFooter(c,W);
    }

    void drawTripsPage(Canvas c,JSONObject m,int W,int H){
        int L=44,R=W-44; Paint p=pdfPaint(14,true);p.setTextAlign(Paint.Align.CENTER);
        c.drawText("صورت هزینه های تنخواه گردان",W/2,34,p);
        Paint q=pdfPaint(9,false);q.setTextAlign(Paint.Align.RIGHT);c.drawText("تاریخ: "+m.optString("end"),R,58,q);
        String[] cats2={"هزینه های شهر تهران","هزینه های تردد بین شهری","هزینه های تردد درون شهری به غیر از تهران","سایر هزینه ها"};
        String[] keys={"هزینه شهر تهران","هزینه تردد بین شهری","هزینه تردد درون شهری","سایر هزینه ها"};
        int y=72;
        JSONArray ar=m.optJSONArray("expenses");
        for(int g=0;g<4;g++){
            Paint h=pdfPaint(10,true);h.setTextAlign(Paint.Align.RIGHT);c.drawText(cats2[g],R,y,h);y+=7;
            int[] x={L,115,340,410,R};
            String[] heads={"تاریخ","شرح","فاکتور","مبلغ"};
            for(int i=0;i<4;i++)excelBox(c,heads[i],x[i],y,x[i+1],y+22,true,true);
            y+=22;
            int count=0;
            if(ar!=null)for(int i=0;i<ar.length()&&count<3;i++){
                JSONObject o=ar.optJSONObject(i);
                if(!keys[g].equals(o.optString("category")))continue;
                excelBox(c,o.optString("date"),x[0],y,x[1],y+22,false,false);
                String detail=o.optString("desc");if(detail.isEmpty())detail=o.optString("tehranItem");
                excelBox(c,detail,x[1],y,x[2],y+22,false,false);
                excelBox(c,o.optString("invoice"),x[2],y,x[3],y+22,false,false);
                excelBox(c,money(o.optDouble("amount")),x[3],y,x[4],y+22,false,false);
                y+=22;count++;
            }
            while(count<2){for(int i=0;i<4;i++)excelBox(c,"",x[i],y,x[i+1],y+22,false,false);y+=22;count++;}
            double sum=expenseSum(m,keys[g]);
            excelBox(c,"جمع "+cats2[g],L,y,340,y+23,true,true);
            excelBox(c,money(sum)+" ریال",340,y,R,y+23,true,true);y+=35;
        }
        excelBox(c,"جمع کل:",L,y,340,y+25,true,true);
        excelBox(c,money(expenseSum(m,null))+" ریال",340,y,R,y+25,true,true);y+=45;
        excelLabel(c,"نام و امضا کارشناس",m.optString("person"),L,y,330,y+28);
        excelLabel(c,"تایید مدیر","",330,y,R,y+28);
        excelFooter(c,W);
    }

    void drawCoverPage(Canvas c,JSONObject m,int W,int H){
        int L=36,R=W-36;Paint p=pdfPaint(14,true);p.setTextAlign(Paint.Align.CENTER);
        c.drawText("شرکت فن آوری آزمایشگاهی (با مسئولیت محدود)",W/2,28,p);
        Paint q=pdfPaint(10,true);q.setTextAlign(Paint.Align.CENTER);
        c.drawText("درخواست وجه                         تسویه وجه دریافتی",W/2,54,q);
        excelLabel(c,"از واحد","خدمات پس از فروش",L,70,290,94);
        excelLabel(c,"به واحد","مالی",290,70,R,94);
        excelLabel(c,"شماره",m.optString("form"),L,95,290,119);
        excelLabel(c,"تاریخ",m.optString("end"),290,95,R,119);
        excelBox(c,"بدینوسیله خواهشمند است نسبت به پرداخت یا تسویه مبلغ  "+money(total(m))+" ریال",L,124,R,155,false,false);
        excelBox(c,"مبلغ به حروف: "+amountWords(total(m)),L,156,R,188,false,false);
        excelLabel(c,"در وجه",m.optString("person"),L,189,300,214);
        excelLabel(c,"بابت ماموریت",m.optString("destination"),300,189,R,214);
        excelSection(c,"مربوط به",L,232,R);
        excelLabel(c,"کمپانی","زیمنس آزمایشگاهی",L,238,300,264);
        excelLabel(c,"نسبت درصد هر کمپانی","100%",300,238,R,264);
        excelLabel(c,"شماره سفارش / پروفرم","",L,265,300,291);
        excelLabel(c,"پیش فاکتور / فاکتور","",300,265,R,291);
        excelBox(c,"توضیحات: مأموریت "+m.optString("subject")+" — مقصد: "+m.optString("destination"),L,292,R,346,false,false);
        excelLabel(c,"درخواست کننده",m.optString("person"),L,360,300,388);
        excelLabel(c,"مدیر واحد","",300,360,R,388);
        excelSection(c,"حسابداری",L,407,R);
        excelBox(c,"لطفاً نسبت به پرداخت مبلغ ...........................................................( "+money(total(m))+" ) ریال/ارز اقدام نمایید.",L,414,R,449,false,false);
        excelLabel(c,"بابت","ماموریت",L,450,220,477);
        excelLabel(c,"به سررسید",m.optString("end"),220,450,390,477);
        excelLabel(c,"از محل موجودی بانک","",390,450,R,477);
        excelLabel(c,"به شماره حساب","",L,478,300,505);
        excelLabel(c,"اقدام نمایید","",300,478,R,505);
        excelSection(c,"رسیدگی کننده / مدیر امور مالی",L,524,R);
        excelBox(c,"رسیدگی کننده: ____________________                         مدیر امور مالی: ____________________",L,531,R,568,false,false);
        excelBox(c,"□ الزامات بودجه رعایت نشده است.",L,582,R,606,false,false);
        excelBox(c,"□ سقف بودجه رعایت نشده است.",L,607,R,631,false,false);
        excelFooter(c,W);
    }

    void pdf(JSONObject m){
        try{
            PdfDocument d=new PdfDocument();int W=595,H=842,page=1;
            PdfDocument.Page p=d.startPage(new PdfDocument.PageInfo.Builder(W,H,page++).create());
            drawPaymentsPage(p.getCanvas(),m,W,H); d.finishPage(p);

            p=d.startPage(new PdfDocument.PageInfo.Builder(W,H,page++).create());
            drawTripsPage(p.getCanvas(),m,W,H); d.finishPage(p);

            p=d.startPage(new PdfDocument.PageInfo.Builder(W,H,page++).create());
            drawCoverPage(p.getCanvas(),m,W,H); d.finishPage(p);

            String fn="Mission_"+System.currentTimeMillis()+".pdf"; Uri u;
            if(Build.VERSION.SDK_INT>=29){
                ContentValues v=new ContentValues();
                v.put(MediaStore.Downloads.DISPLAY_NAME,fn);
                v.put(MediaStore.Downloads.MIME_TYPE,"application/pdf");
                v.put(MediaStore.Downloads.IS_PENDING,1);
                u=getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI,v);
                OutputStream o=getContentResolver().openOutputStream(u);d.writeTo(o);o.close();
                v.clear();v.put(MediaStore.Downloads.IS_PENDING,0);getContentResolver().update(u,v,null,null);
            }else{
                File f=new File(getExternalFilesDir(null),fn);OutputStream o=new FileOutputStream(f);d.writeTo(o);o.close();u=Uri.fromFile(f);
            }
            d.close();share(u);
        }catch(Exception e){toast("خطا در ساخت PDF: "+e.getMessage());}
    }

    void share(Uri u){Intent i=new Intent(Intent.ACTION_SEND);i.setType("application/pdf");i.putExtra(Intent.EXTRA_STREAM,u);i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);startActivity(Intent.createChooser(i,"ارسال / ذخیره PDF"));}
}
