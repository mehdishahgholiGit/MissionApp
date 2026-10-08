package com.missionapp;

import android.os.*;
import android.graphics.*;
import android.view.*;
import android.widget.*;
import org.json.*;
import java.util.*;

public class WizardActivity extends MainActivity {
    EditText form,service,subject,dest,start,end,startTime,endTime,days,holiday;
    EditText hotel,go,back,cancelGo,cancelBack,goNo,backNo;
    Spinner ps,ds,hp,gp,bp,cgp,cbp;
    CheckBox suburb;
    LinearLayout contentBox;
    int step=1;

    TextView stepTitle, stepHint;
    Button nextBtn, backBtn;
    LinearLayout expenseList;
    TextView expenseSum;

    public void edit(){
        step=1;
        buildWizard();
    }

    void buildWizard(){
        base(cur.length()==0?"مأموریت جدید":"ویرایش مأموریت");
        TextView progress=tv("مرحله "+step+" از ۴",14);
        progress.setTextColor(primary);
        progress.setGravity(Gravity.CENTER);
        box.addView(progress,new LinearLayout.LayoutParams(-1,40));

        stepTitle=tv("",20); stepTitle.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        stepTitle.setTextColor(primaryDark); stepTitle.setGravity(Gravity.CENTER);
        box.addView(stepTitle,new LinearLayout.LayoutParams(-1,52));

        stepHint=tv("",13); stepHint.setTextColor(muted); stepHint.setGravity(Gravity.CENTER);
        box.addView(stepHint,new LinearLayout.LayoutParams(-1,44));

        contentBox=new LinearLayout(this); contentBox.setOrientation(LinearLayout.VERTICAL);
        contentBox.setPadding(4,4,4,8); box.addView(contentBox,new LinearLayout.LayoutParams(-1,-2));

        LinearLayout nav=new LinearLayout(this); nav.setOrientation(LinearLayout.HORIZONTAL); nav.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        backBtn=secondary("← قبلی"); backBtn.setOnClickListener(v->{if(step>1){step--;renderStep();}});
        nextBtn=bt("ادامه →"); nextBtn.setOnClickListener(v->{if(validateStep()){if(step<4){step++;renderStep();}else{finishMission();}}});
        nav.addView(backBtn,new LinearLayout.LayoutParams(0,56,1));
        Space sp=new Space(this); nav.addView(sp,new LinearLayout.LayoutParams(8,1));
        nav.addView(nextBtn,new LinearLayout.LayoutParams(0,56,1));
        box.addView(nav);
        renderStep();
    }

    void makeFields(){
        form=ed("شماره فرم"); service=ed("شماره سرویس"); subject=ed("موضوع"); dest=ed("مقصد");
        start=ed("تاریخ شروع"); end=ed("تاریخ پایان"); startTime=ed("زمان شروع"); endTime=ed("زمان پایان");
        days=ed("تعداد روز"); holiday=ed("تعداد روز تعطیل");
        hotel=ed("مبلغ هتل"); go=ed("مبلغ بلیط رفت"); back=ed("مبلغ بلیط برگشت");
        cancelGo=ed("مبلغ کنسلی رفت"); cancelBack=ed("مبلغ کنسلی برگشت");
        goNo=ed("شماره بلیط رفت"); backNo=ed("شماره بلیط برگشت");
        ps=spinner(people); ds=spinner(devices); hp=spinner(payers); gp=spinner(payers); bp=spinner(payers); cgp=spinner(payers); cbp=spinner(payers);

        form.setText(cur.optString("form")); service.setText(cur.optString("service")); subject.setText(cur.optString("subject"));
        dest.setText(cur.optString("destination")); start.setText(cur.optString("start")); end.setText(cur.optString("end"));
        startTime.setText(cur.optString("startTime")); endTime.setText(cur.optString("endTime"));
        days.setText(String.valueOf(cur.optDouble("days",1))); holiday.setText(String.valueOf(cur.optDouble("holiday",0)));
        hotel.setText(String.valueOf(cur.optDouble("hotel",0))); go.setText(String.valueOf(cur.optDouble("ticketGo",0)));
        back.setText(String.valueOf(cur.optDouble("ticketBack",0))); cancelGo.setText(String.valueOf(cur.optDouble("cancelGo",0)));
        cancelBack.setText(String.valueOf(cur.optDouble("cancelBack",0))); goNo.setText(cur.optString("goNo")); backNo.setText(cur.optString("backNo"));
        ps.setSelection(index(people,cur.optString("person"))); ds.setSelection(index(devices,cur.optString("device")));
        hp.setSelection(index(payers,payer(cur,"hotelPayer"))); gp.setSelection(index(payers,payer(cur,"goPayer")));
        bp.setSelection(index(payers,payer(cur,"backPayer"))); cgp.setSelection(index(payers,payer(cur,"cancelGoPayer")));
        cbp.setSelection(index(payers,payer(cur,"cancelBackPayer")));
        attachDatePicker(start); attachDatePicker(end);
        suburb=new CheckBox(this); suburb.setText("مأموریت حومه تهران"); suburb.setTextColor(text); suburb.setTextSize(15); suburb.setChecked(cur.optBoolean("suburb",false));
    }

    void renderStep(){
        contentBox.removeAllViews();
        stepTitle.setText(new String[]{"","۱. اطلاعات مأموریت","۲. زمان و نوع مأموریت","۳. هزینه‌ها","۴. بررسی نهایی"}[step]);
        stepHint.setText(new String[]{"","فقط اطلاعات پایه را وارد کنید","تاریخ و مدت مأموریت را مشخص کنید","فقط هزینه‌هایی را که واقعاً پرداخت کرده‌اید وارد کنید","قبل از ثبت، همه چیز را یک‌جا بررسی کنید"}[step]);
        if(form==null) makeFields();

        if(step==1){
            LinearLayout c=sectionLocal("اطلاعات ضروری");
            c.addView(field("نام کارشناس",ps)); c.addView(field("مقصد",dest)); c.addView(field("موضوع",subject));
            LinearLayout r=row(); add2(r,"شماره فرم",form,"شماره سرویس",service); c.addView(r);
            contentBox.addView(c);
        } else if(step==2){
            LinearLayout c=sectionLocal("زمان مأموریت");
            LinearLayout r=row(); add2(r,"تاریخ شروع",start,"تاریخ پایان",end); c.addView(r);
            r=row(); add2(r,"زمان شروع",startTime,"زمان پایان",endTime); c.addView(r);
            r=row(); add2(r,"تعداد روز مأموریت",days,"تعداد روز تعطیل",holiday); c.addView(r);
            c.addView(suburb);
            TextView rule=tv("حق مأموریت به‌صورت خودکار محاسبه می‌شود.",13);rule.setTextColor(muted);c.addView(rule);
            contentBox.addView(c);
        } else if(step==3){
            LinearLayout c=sectionLocal("هتل و بلیط");
            LinearLayout r=row(); add2(r,"هزینه هتل",hotel,"پرداخت هتل توسط",hp);c.addView(r);
            r=row();add2(r,"بلیط رفت",go,"پرداخت بلیط رفت توسط",gp);c.addView(r);
            r=row();add2(r,"شماره بلیط رفت",goNo,"بلیط برگشت",back);c.addView(r);
            r=row();add2(r,"پرداخت بلیط برگشت توسط",bp,"شماره بلیط برگشت",backNo);c.addView(r);
            r=row();add2(r,"کنسلی رفت",cancelGo,"پرداخت کنسلی رفت توسط",cgp);c.addView(r);
            r=row();add2(r,"کنسلی برگشت",cancelBack,"پرداخت کنسلی برگشت توسط",cbp);c.addView(r);
            contentBox.addView(c);

            LinearLayout e=sectionLocal("ریز هزینه‌ها");
            expenseList=new LinearLayout(this);expenseList.setOrientation(LinearLayout.VERTICAL);e.addView(expenseList);
            expenseSum=tv("جمع هزینه‌ها: "+money(expenseSum(cur,null))+" ریال",16);expenseSum.setTextColor(green);expenseSum.setGravity(Gravity.CENTER);
            e.addView(expenseSum);
            Button add=bt("＋ افزودن هزینه");add.setOnClickListener(v->expenseDialog(expenseList,expenseSum));e.addView(add);
            render(expenseList,expenseSum);
            contentBox.addView(e);
        } else {
            LinearLayout c=sectionLocal("خلاصه مأموریت");
            summaryLine(c,"کارشناس",ps.getSelectedItem().toString());
            summaryLine(c,"مقصد",dest.getText().toString());
            summaryLine(c,"موضوع",subject.getText().toString());
            summaryLine(c,"تاریخ",start.getText().toString()+" تا "+end.getText().toString());
            summaryLine(c,"مدت",days.getText().toString()+" روز / "+holiday.getText().toString()+" روز تعطیل");
            summaryLine(c,"حق مأموریت",money(missionPay(cur))+" ریال");
            summaryLine(c,"سایر هزینه‌ها",money(expenseSum(cur,null))+" ریال");
            TextView total=tv("💰 جمع کل قابل پرداخت\n"+money(total(cur))+" ریال",21);total.setTextColor(green);total.setGravity(Gravity.CENTER);total.setTypeface(Typeface.DEFAULT,Typeface.BOLD);total.setPadding(8,22,8,22);c.addView(total);
            contentBox.addView(c);
            TextView note=tv("با زدن «ثبت نهایی»، مأموریت ذخیره می‌شود. سپس می‌توانید PDF آن را بسازید.",13);note.setTextColor(muted);note.setGravity(Gravity.CENTER);contentBox.addView(note);
        }
        backBtn.setVisibility(step==1?View.INVISIBLE:View.VISIBLE);
        nextBtn.setText(step==4?"✓ ثبت نهایی":"ادامه →");
        if(step==4) nextBtn.setBackground(shape(green,14,Color.TRANSPARENT));
        else nextBtn.setBackground(shape(primary,14,Color.TRANSPARENT));
        stepTitle.invalidate();
    }

    LinearLayout sectionLocal(String title){
        LinearLayout c=new LinearLayout(this);c.setOrientation(LinearLayout.VERTICAL);c.setPadding(16,14,16,14);
        c.setBackground(shape(card,18,border));c.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        TextView h=tv(title,17);h.setTextColor(primaryDark);h.setTypeface(Typeface.DEFAULT,Typeface.BOLD);h.setPadding(0,0,0,10);c.addView(h);
        return c;
    }

    void summaryLine(LinearLayout c,String a,String b){
        LinearLayout r=new LinearLayout(this);r.setPadding(10,8,10,8);r.setGravity(Gravity.CENTER_VERTICAL);r.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        TextView l=tv(a,13);l.setTextColor(muted);TextView v=tv(b.isEmpty()?"—":b,15);v.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        r.addView(l,new LinearLayout.LayoutParams(0,42,1));r.addView(v,new LinearLayout.LayoutParams(0,42,2));c.addView(r);
    }

    boolean validateStep(){
        try{
            if(step==1){
                if(ps.getSelectedItemPosition()==0 && people.length>1){} 
                if(dest.getText().toString().trim().isEmpty()){toast("مقصد را وارد کنید");return false;}
                if(subject.getText().toString().trim().isEmpty()){toast("موضوع مأموریت را وارد کنید");return false;}
            }
            if(step==2){
                if(start.getText().toString().trim().isEmpty()||end.getText().toString().trim().isEmpty()){toast("تاریخ شروع و پایان را انتخاب کنید");return false;}
                if(num(days)<=0){toast("تعداد روز باید بیشتر از صفر باشد");return false;}
                validate7(end.getText().toString());
            }
            sync();
            return true;
        }catch(Exception e){toast(e.getMessage());return false;}
    }

    void sync(){
        try{
            cur.put("form",form.getText().toString());cur.put("service",service.getText().toString());
            cur.put("person",ps.getSelectedItem().toString());cur.put("device",ds.getSelectedItem().toString());
            cur.put("subject",subject.getText().toString());cur.put("destination",dest.getText().toString());
            cur.put("start",start.getText().toString());cur.put("end",end.getText().toString());
            cur.put("startTime",startTime.getText().toString());cur.put("endTime",endTime.getText().toString());
            cur.put("days",num(days));cur.put("holiday",num(holiday));cur.put("suburb",suburb.isChecked());
            cur.put("hotel",num(hotel));cur.put("ticketGo",num(go));cur.put("ticketBack",num(back));
            cur.put("cancelGo",num(cancelGo));cur.put("cancelBack",num(cancelBack));
            cur.put("goNo",goNo.getText().toString());cur.put("backNo",backNo.getText().toString());
            cur.put("hotelPayer",hp.getSelectedItem().toString());cur.put("goPayer",gp.getSelectedItem().toString());
            cur.put("backPayer",bp.getSelectedItem().toString());cur.put("cancelGoPayer",cgp.getSelectedItem().toString());
            cur.put("cancelBackPayer",cbp.getSelectedItem().toString());
        }catch(Exception e){}
    }

    void finishMission(){
        try{
            sync();
            validate7(end.getText().toString());
            if(!data.contains(cur))data.add(cur);
            save();
            home();
        }catch(Exception e){toast(e.getMessage());}
    }
}
