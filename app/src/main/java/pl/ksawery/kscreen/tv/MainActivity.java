package pl.ksawery.kscreen.tv;
import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.provider.Settings;
import android.util.Base64;
import android.view.View;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
public class MainActivity extends Activity {
 private WebView web;
 @Override public void onCreate(Bundle state) {
  super.onCreate(state);
  getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
  getWindow().getDecorView().setSystemUiVisibility(5894|1024|512|256);
  web=new WebView(this);
  web.setBackgroundColor(Color.rgb(8,12,21));
  web.getSettings().setJavaScriptEnabled(true);
  web.getSettings().setDomStorageEnabled(true);
  web.getSettings().setDefaultTextEncodingName("UTF-8");
  web.addJavascriptInterface(new Bridge(),"KScreenNative");
  web.setWebViewClient(new WebViewClient());
  setContentView(web);
  web.loadUrl("file:///android_asset/index.html");
 }
 @Override public void onBackPressed() { if(web!=null)web.evaluateJavascript("window.ksBack&&window.ksBack()",null); }
 private final class Bridge {
  @JavascriptInterface public String apps() {
   JSONArray arr=new JSONArray();
   PackageManager pm=getPackageManager();
   List<ResolveInfo> found=new ArrayList<>();
   found.addAll(pm.queryIntentActivities(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER),0));
   found.addAll(pm.queryIntentActivities(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER),0));
   Set<String> seen=new HashSet<>();
   for(ResolveInfo ri:found) {
    ActivityInfo ai=ri.activityInfo;
    if(ai==null||ai.packageName==null||ai.packageName.equals(getPackageName())||!seen.add(ai.packageName)) continue;
    try {
     JSONObject o=new JSONObject();
     o.put("label",String.valueOf(ri.loadLabel(pm)));
     o.put("pkg",ai.packageName); o.put("activity",ai.name);
     Drawable ic=ri.loadIcon(pm);
     if(ic!=null) {
      Bitmap bm=Bitmap.createBitmap(90,90,Bitmap.Config.ARGB_8888);
      ic.setBounds(0,0,90,90); ic.draw(new Canvas(bm));
      ByteArrayOutputStream buf=new ByteArrayOutputStream();
      bm.compress(Bitmap.CompressFormat.PNG,95,buf);
      o.put("icon","data:image/png;base64,"+Base64.encodeToString(buf.toByteArray(),Base64.NO_WRAP));
      bm.recycle();
     }
     arr.put(o);
    } catch(Exception e) { }
   }
   return arr.toString();
  }
  @JavascriptInterface public void launch(String pkg,String activity) {
   runOnUiThread(()-> {
    try {
     Intent i;
     if(activity!=null&&!activity.isEmpty()){
      i=new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER);
      i.setComponent(new ComponentName(pkg,activity));
     } else {
      i=getPackageManager().getLeanbackLaunchIntentForPackage(pkg);
      if(i==null)i=getPackageManager().getLaunchIntentForPackage(pkg);
     }
     if(i!=null)startActivity(i);
    } catch(Exception ignored) { }
   });
  }
  @JavascriptInterface public void settings() {
   runOnUiThread(()->{ try {startActivity(new Intent(Settings.ACTION_SETTINGS));} catch(Exception ignored) {} });
  }
 }
 @Override public void onDestroy(){
  if(web!=null){web.removeJavascriptInterface("KScreenNative");web.destroy();web=null;}
  super.onDestroy();
 }
}
