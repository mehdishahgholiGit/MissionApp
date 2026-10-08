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
            bg=Color.rgb(18,20,24); card=Color.rgb(30,34,40); primary=Color.rgb(66,133,210); primaryDark=Color.rgb(35,53,76);
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
        v.setSingleLine(true); v.setPadding(14,0,14,0); v.setBackground(shape(Color.WHITE,14,border));
        v.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        if(hint.contains("هزینه")||hint.contains("مبلغ")||hint.contains("روز")||hint.contains("شماره"))
            v.setInputType(android.text.InputType.TYPE_CLASS_NUMBER|android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        return v;
    }

    Button bt(String s){
        Button v=new Button(this); v.setText(s); v.setTextSize(14); v.setAllCaps(false); v.setTextColor(Color.WHITE);
        v.setPadding(12,4,12,4); v.setMinHeight(52); v.setGravity(Gravity.CENTER);
        v.setBackground(shape(primary,14,Color.TRANSPARENT)); return v;
    }

    Button secondary(String s){
        Button v=bt(s); v.setTextColor(primary); v.setBackground(shape(Color.WHITE,14,primary)); return v;
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
        lab.setBackground(shape(darkMode?Color.rgb(42,47,55):Color.rgb(235,240,246),8,Color.TRANSPARENT));
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
        target.setFocusable(false);
        target.setClickable(true);
        target.setOnClickListener(v->{
            Calendar initial=Calendar.getInstance();
            int[] g=jalaliToGregorianSafe(target.getText().toString());
            if(g!=null){ initial.set(g[0],g[1]-1,g[2]); }
            DatePickerDialog dlg=new DatePickerDialog(this,(view,year,month,day)->{
                int[] j=gregorianToJalali(year,month+1,day);
                target.setText(String.format(Locale.US,"%04d/%02d/%02d",j[0],j[1],j[2]));
            },initial.get(Calendar.YEAR),initial.get(Calendar.MONTH),initial.get(Calendar.DAY_OF_MONTH));
            dlg.show();
        });
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

    Paint pdfPaint(float size,boolean bold){Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);p.setColor(Color.BLACK);p.setTextSize(size);if(bold)p.setTypeface(Typeface.DEFAULT_BOLD);return p;}
    void drawCell(Canvas c,String s,float l,float t,float r,float b,boolean bold){
        Paint borderP=pdfPaint(10,false);borderP.setStyle(Paint.Style.STROKE);c.drawRect(l,t,r,b,borderP);
        Paint p=pdfPaint(bold?10:9,bold);android.text.TextPaint tp=new android.text.TextPaint(p);tp.setTextAlign(Paint.Align.RIGHT);
        android.text.StaticLayout sl=new android.text.StaticLayout(s==null?"":s,tp,Math.max(20,(int)(r-l-10)),android.text.Layout.Alignment.ALIGN_OPPOSITE,1.05f,0,false);
        c.save();c.translate(r-5,t+5);sl.draw(c);c.restore();
    }

    void drawHeader(Canvas c,String title,String sub,int W){
        Paint p=pdfPaint(18,true);p.setTextAlign(Paint.Align.CENTER);c.drawText(title,W/2,30,p);
        Paint q=pdfPaint(9,false);q.setTextAlign(Paint.Align.CENTER);c.drawText(sub,W/2,46,q);
    }

    void drawSummaryGrid(Canvas c,JSONObject m,int y,int W){
        int L=28,R=W-28,mid=W/2;int h=38;
        drawCell(c,"شماره فرم\n"+m.optString("form"),L,y,mid,y+h,false);
        drawCell(c,"شماره سرویس\n"+m.optString("service"),mid,y,R,y+h,false);y+=h;
        drawCell(c,"نام کارشناس\n"+m.optString("person"),L,y,mid,y+h,false);
        drawCell(c,"دستگاه\n"+m.optString("device"),mid,y,R,y+h,false);y+=h;
        drawCell(c,"موضوع\n"+m.optString("subject"),L,y,mid,y+h,false);
        drawCell(c,"مقصد\n"+m.optString("destination"),mid,y,R,y+h,false);y+=h;
        drawCell(c,"تاریخ شروع\n"+m.optString("start"),L,y,mid,y+h,false);
        drawCell(c,"تاریخ پایان\n"+m.optString("end"),mid,y,R,y+h,false);y+=h;
        drawCell(c,"زمان شروع\n"+m.optString("startTime"),L,y,mid,y+h,false);
        drawCell(c,"زمان پایان\n"+m.optString("endTime"),mid,y,R,y+h,false);y+=h;
        drawCell(c,"تعداد روز\n"+m.optDouble("days",1),L,y,mid,y+h,false);
        drawCell(c,"تعطیل\n"+m.optDouble("holiday",0),mid,y,R,y+h,false);y+=h;
        drawCell(c,"حومه تهران\n"+(m.optBoolean("suburb")?"بله":"خیر"),L,y,mid,y+h,false);
        drawCell(c,"حق مأموریت\n"+money(missionPay(m))+" ریال",mid,y,R,y+h,true);
    }

    int drawExpenseRows(Canvas c,JSONObject m,int startY,int pageNo,int W){
        int y=startY,L=24,R=W-24;
        Paint p=pdfPaint(12,true);p.setTextAlign(Paint.Align.RIGHT);c.drawText("ریز هزینه‌ها",R,y,p);y+=10;
        int[] x={L,150,305,395,475,R};String[] h={"دسته هزینه","شرح","تاریخ","فاکتور","مبلغ"};
        for(int i=0;i<5;i++)drawCell(c,h[i],x[i],y,x[i+1],y+30,true);y+=30;
        JSONArray ar=m.optJSONArray("expenses");int idx=0;
        if(ar!=null)for(;idx<ar.length();idx++){
            if(y>790){finishPage(c);return idx;}
            JSONObject o=ar.optJSONObject(idx);
            String detail=o.optString("desc"); if(detail.isEmpty()) detail=o.optString("tehranItem");
            String[] rr={o.optString("category"),detail,o.optString("date"),o.optString("invoice"),money(o.optDouble("amount"))};
            for(int z=0;z<5;z++)drawCell(c,rr[z],x[z],y,x[z+1],y+34,false);y+=34;
        }
        double[] sums={0,0,0,0};String[] cn={"هزینه شهر تهران","هزینه تردد بین شهری","هزینه تردد درون شهری","سایر هزینه ها"};
        if(ar!=null)for(int i=0;i<ar.length();i++){JSONObject o=ar.optJSONObject(i);for(int z=0;z<4;z++)if(cn[z].equals(o.optString("category")))sums[z]+=o.optDouble("amount");}
        y+=8;for(int i=0;i<4;i++){drawCell(c,cn[i],L,y,300,y+28,false);drawCell(c,money(sums[i])+" ریال",300,y,R,y+28,false);y+=28;}
        drawCell(c,"جمع کل قابل پرداخت",L,y,300,y+38,true);drawCell(c,money(total(m))+" ریال",300,y,R,y+38,true);
        return -1;
    }

    void finishPage(Canvas c){ }

    void pdf(JSONObject m){
        try{
            PdfDocument d=new PdfDocument();int W=595,H=842;int page=1;

            PdfDocument.Page p=d.startPage(new PdfDocument.PageInfo.Builder(W,H,page++).create());Canvas c=p.getCanvas();
            drawHeader(c,"فرم مأموریت و هزینه‌ها","نسخه چاپی ساختاریافته — تمام مقادیر ثبت‌شده در فرم",W);
            drawSummaryGrid(c,m,60,W);
            int y=60+7*38+18;
            Paint hp=pdfPaint(12,true);hp.setTextAlign(Paint.Align.RIGHT);c.drawText("هزینه‌های مستقیم",W-28,y,hp);y+=12;
            int L=28,R=W-28,mid=W/2,h=38;
            drawCell(c,"هتل\n"+money(m.optDouble("hotel"))+" ریال\nپرداخت: "+payer(m,"hotelPayer"),L,y,mid,y+h,false);
            drawCell(c,"بلیط رفت\n"+money(m.optDouble("ticketGo"))+" ریال\nپرداخت: "+payer(m,"goPayer"),mid,y,R,y+h,false);y+=h;
            drawCell(c,"بلیط برگشت\n"+money(m.optDouble("ticketBack"))+" ریال\nپرداخت: "+payer(m,"backPayer"),L,y,mid,y+h,false);
            drawCell(c,"کنسلی رفت\n"+money(m.optDouble("cancelGo"))+" ریال\nپرداخت: "+payer(m,"cancelGoPayer"),mid,y,R,y+h,false);y+=h;
            drawCell(c,"کنسلی برگشت\n"+money(m.optDouble("cancelBack"))+" ریال\nپرداخت: "+payer(m,"cancelBackPayer"),L,y,mid,y+h,false);
            drawCell(c,"جمع کل\n"+money(total(m))+" ریال",mid,y,R,y+h,true);
            d.finishPage(p);

            JSONArray ar=m.optJSONArray("expenses");int count=ar==null?0:ar.length();
            int from=0;
            if(count==0){
                p=d.startPage(new PdfDocument.PageInfo.Builder(W,H,page++).create());c=p.getCanvas();drawHeader(c,"ریز هزینه‌ها","در این مأموریت ردیف هزینه‌ای ثبت نشده است",W);d.finishPage(p);
            } else {
                while(from<count){
                    p=d.startPage(new PdfDocument.PageInfo.Builder(W,H,page++).create());c=p.getCanvas();
                    drawHeader(c,"ریز هزینه‌ها","صفحه "+(page-1),W);
                    int y0=65,L2=24,R2=W-24;Paint pp=pdfPaint(12,true);pp.setTextAlign(Paint.Align.RIGHT);c.drawText("جزئیات هزینه‌های ثبت‌شده",R2,y0,pp);y0+=12;
                    int[] x={L2,150,305,395,475,R2};String[] hh={"دسته هزینه","شرح","تاریخ","فاکتور","مبلغ"};
                    for(int i=0;i<5;i++)drawCell(c,hh[i],x[i],y0,x[i+1],y0+30,true);y0+=30;
                    while(from<count && y0<=790){
                        JSONObject o=ar.optJSONObject(from);String detail=o.optString("desc"); if(detail.isEmpty()) detail=o.optString("tehranItem");
                        String[] rr={o.optString("category"),detail,o.optString("date"),o.optString("invoice"),money(o.optDouble("amount"))};
                        for(int z=0;z<5;z++)drawCell(c,rr[z],x[z],y0,x[z+1],y0+34,false);y0+=34;from++;
                    }
                    d.finishPage(p);
                }
            }

            p=d.startPage(new PdfDocument.PageInfo.Builder(W,H,page++).create());c=p.getCanvas();
            drawHeader(c,"روکش سند حسابداری","فرم قابل ارائه به واحد مالی",W);
            int L3=28,R3=W-28,coverY=65;String[][] cover={
                {"واحد / دستگاه",m.optString("device")},{"نام کارشناس",m.optString("person")},{"مقصد",m.optString("destination")},
                {"موضوع مأموریت",m.optString("subject")},{"شماره فرم",m.optString("form")},{"شماره سرویس",m.optString("service")},
                {"بازه مأموریت",m.optString("start")+" تا "+m.optString("end")},{"مبلغ سند",money(total(m))+" ریال"},{"مبلغ به حروف",amountWords(total(m))}
            };
            for(String[] r:cover){int hh=r[0].equals("مبلغ به حروف")?58:42;drawCell(c,r[0],L3,coverY,205,coverY+hh,true);drawCell(c,r[1],205,coverY,R3,coverY+hh,false);coverY+=hh;}
            coverY+=12;Paint pp=pdfPaint(12,true);pp.setTextAlign(Paint.Align.RIGHT);c.drawText("وضعیت پرداخت هزینه‌های مستقیم",R3,coverY,pp);coverY+=10;
            String[] payRows={"هتل: "+payer(m,"hotelPayer")+"    |    بلیط رفت: "+payer(m,"goPayer"),"بلیط برگشت: "+payer(m,"backPayer")+"    |    کنسلی رفت: "+payer(m,"cancelGoPayer"),"کنسلی برگشت: "+payer(m,"cancelBackPayer")};
            for(String s:payRows){drawCell(c,s,L3,coverY,R3,coverY+34,false);coverY+=34;}
            coverY+=12;c.drawText("تأییدها و امضا",R3,coverY,pp);coverY+=10;
            String[] sig={"درخواست کننده","مدیر / مسئول","حسابداری","تأیید نهایی"};
            for(String s:sig){drawCell(c,s,L3,coverY,205,coverY+48,true);drawCell(c,"نام و امضا: ______________________________",205,coverY,R3,coverY+48,false);coverY+=52;}
            d.finishPage(p);

            String fn="Mission_"+System.currentTimeMillis()+".pdf";
            Uri u;
            if(Build.VERSION.SDK_INT>=29){
                ContentValues v=new ContentValues();v.put(MediaStore.Downloads.DISPLAY_NAME,fn);v.put(MediaStore.Downloads.MIME_TYPE,"application/pdf");v.put(MediaStore.Downloads.IS_PENDING,1);
                u=getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI,v);OutputStream o=getContentResolver().openOutputStream(u);d.writeTo(o);o.close();v.clear();v.put(MediaStore.Downloads.IS_PENDING,0);getContentResolver().update(u,v,null,null);
            } else {
                File f=new File(getExternalFilesDir(null),fn);OutputStream o=new FileOutputStream(f);d.writeTo(o);o.close();u=Uri.fromFile(f);
            }
            d.close();share(u);
        }catch(Exception e){toast("خطا در ساخت PDF: "+e.getMessage());}
    }

    void share(Uri u){Intent i=new Intent(Intent.ACTION_SEND);i.setType("application/pdf");i.putExtra(Intent.EXTRA_STREAM,u);i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);startActivity(Intent.createChooser(i,"ارسال / ذخیره PDF"));}
}
