package local.venus.kiosk;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final String PREFS = "venus_kiosk";
    private static final String KEY_URL = "url";
    private static final String KEY_FIT = "fit_console";
    private static final String DEFAULT_URL = "http://192.168.1.107/gui-v1/";

    private WebView webView;
    private View adminHotspot;
    private long hotspotDownAt = 0L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        hideSystemUi();

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.BLACK);

        webView = new WebView(this);
        configureWebView(webView);
        root.addView(webView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));

        // Invisible admin hotspot, top-left. Hold for ~3 seconds.
        adminHotspot = new View(this);
        adminHotspot.setBackgroundColor(Color.TRANSPARENT);
        FrameLayout.LayoutParams hp = new FrameLayout.LayoutParams(72, 72, Gravity.TOP | Gravity.LEFT);
        root.addView(adminHotspot, hp);
        adminHotspot.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    hotspotDownAt = System.currentTimeMillis();
                    return true;
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    long held = System.currentTimeMillis() - hotspotDownAt;
                    if (held >= 2500) showAdminDialog();
                    return true;
                }
                return true;
            }
        });

        setContentView(root);
        loadConfiguredUrl();
    }

    private void configureWebView(WebView w) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        WebSettings s = w.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setUseWideViewPort(true);
        s.setLoadWithOverviewMode(true);
        s.setBuiltInZoomControls(false);
        s.setDisplayZoomControls(false);
        s.setSupportZoom(false);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setCacheMode(WebSettings.LOAD_DEFAULT);
        s.setUserAgentString(s.getUserAgentString() + " VenusKiosk/0.2");

        w.setBackgroundColor(Color.BLACK);
        w.setWebChromeClient(new WebChromeClient());
        w.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageFinished(final WebView view, String url) {
                super.onPageFinished(view, url);
                hideSystemUi();
                if (isFitEnabled()) {
                    // Venus/noVNC may finish laying itself out after onPageFinished.
                    applyConsoleFit(view);
                    view.postDelayed(new Runnable() {
                        @Override public void run() { applyConsoleFit(view); }
                    }, 1000);
                    view.postDelayed(new Runnable() {
                        @Override public void run() { applyConsoleFit(view); }
                    }, 3000);
                }
            }
        });

    }



    private boolean isFitEnabled() {
        return getSharedPreferences(PREFS, MODE_PRIVATE).getBoolean(KEY_FIT, true);
    }

    private void applyConsoleFit(WebView view) {
        // Keep the original hotkeys working, but move them into a compact sidebar.
        String js =
                "(function(){try{" +
                "var d=document,html=d.documentElement,b=d.body;if(!b)return;" +
                "html.style.background='#000';b.style.background='#000';" +
                "html.style.margin='0';b.style.margin='0';" +
                "html.style.overflow='hidden';b.style.overflow='hidden';" +
                "var shells=d.querySelectorAll('.remote-console-popup-container,.remote-console-logged-in,.remote-console-display-container,.remote-console-display,.remote-console-display-inner');" +
                "for(var h=0;h<shells.length;h++){" +
                " shells[h].style.background='transparent';shells[h].style.border='0';shells[h].style.boxShadow='none';" +
                "}" +
                "var sidebarWidth=136;" +
                "var panel=d.getElementById('venus-kiosk-hotkeys')||d.querySelector('.remote-console-logged-in .remote-console-controls-container');" +
                "if(panel){" +
                " panel.id='venus-kiosk-hotkeys';" +
                " b.appendChild(panel);" +
                " panel.style.setProperty('display','block','important');" +
                " panel.style.position='fixed';panel.style.right='4px';panel.style.top='50%';" +
                " panel.style.left='auto';panel.style.width=(sidebarWidth-8)+'px';panel.style.height='auto';" +
                " panel.style.margin='0';panel.style.padding='8px';panel.style.boxSizing='border-box';" +
                " panel.style.background='#171a1f';panel.style.borderRadius='10px';panel.style.overflow='hidden';" +
                " panel.style.transform='translateY(-50%)';panel.style.zIndex='2147483647';" +
                " var title=panel.querySelector('.remote-console-controls-text');" +
                " if(title){title.style.display='none';}" +
                " var controlsArea=panel.querySelector('.remote-console-controls');" +
                " if(controlsArea){" +
                "  controlsArea.style.display='flex';controlsArea.style.flexDirection='column';" +
                "  controlsArea.style.alignItems='stretch';controlsArea.style.width='100%';" +
                "  controlsArea.style.height='auto';controlsArea.style.margin='0';controlsArea.style.padding='0';" +
                "  controlsArea.style.position='static';controlsArea.style.background='transparent';" +
                " }" +
                " var rows=panel.querySelectorAll('.remote-console-controls-row');" +
                " for(var m=0;m<rows.length;m++){" +
                "  rows[m].style.display='none';" +
                " }" +
                " var sequence=['left-button','right-button','up','down','left','right','center'];" +
                " if(controlsArea){" +
                "  for(var z=0;z<sequence.length;z++){" +
                "   var item=panel.querySelector('[data-button=\"'+sequence[z]+'\"]');" +
                "   if(item){controlsArea.appendChild(item);}" +
                "  }" +
                " }" +
                " var order={\"left-button\":1,\"right-button\":2,up:3,down:4,left:5,right:6,center:7};" +
                " var controls=panel.querySelectorAll('.remote-console-control-button');" +
                " for(var n=0;n<controls.length;n++){" +
                "  controls[n].style.display='flex';controls[n].style.alignItems='center';controls[n].style.justifyContent='center';" +
                "  controls[n].style.order=order[controls[n].getAttribute('data-button')]||99;" +
                "  controls[n].style.position='static';controls[n].style.width='100%';controls[n].style.height='62px';" +
                "  controls[n].style.margin='4px 0';controls[n].style.padding='0';controls[n].style.boxSizing='border-box';" +
                "  controls[n].style.background='#f4f5f6';controls[n].style.border='1px solid #3b4650';" +
                "  controls[n].style.borderRadius='8px';controls[n].style.color='#4698cc';controls[n].style.fontSize='22px';" +
                "  controls[n].style.touchAction='manipulation';controls[n].style.float='none';" +
                " }" +
                " var escSpacer=panel.querySelector('[data-button=\"left-button\"] .remote-console-control-icon:not(.text)');" +
                " if(escSpacer){escSpacer.style.display='none';}" +
                " var escText=panel.querySelector('[data-button=\"left-button\"] .text');" +
                " if(escText){" +
                "  escText.style.position='static';escText.style.width='auto';escText.style.height='auto';" +
                "  escText.style.lineHeight='normal';escText.style.margin='0';escText.style.fontWeight='700';" +
                " }" +
                " var icons=panel.querySelectorAll('.remote-console-control-icon');" +
                " for(var q=0;q<icons.length;q++){" +
                "  icons[q].style.maxWidth='28px';icons[q].style.maxHeight='28px';icons[q].style.transform='scale(1.15)';" +
                " }" +
                " var shapes=panel.querySelectorAll('svg path,svg rect');" +
                " for(var u=0;u<shapes.length;u++){shapes[u].style.stroke='#4698cc';shapes[u].style.strokeWidth='0.65';}" +
                "}" +
                "var cs=d.getElementsByTagName('canvas'),c=null,area=0;" +
                "for(var k=0;k<cs.length;k++){var rr=cs[k].getBoundingClientRect(),a=rr.width*rr.height;if(a>area){area=a;c=cs[k];}}" +
                "if(!c||area<10000)return;" +
                "c.style.position='fixed';c.style.margin='0';" +
                "c.style.transform='none';" +
                "var availableWidth=window.innerWidth-(panel?sidebarWidth:0);" +
                "c.style.left=(availableWidth/2)+'px';c.style.top='50%';" +
                "c.style.transformOrigin='50% 50%';" +
                "c.style.zIndex='2147483646';" +
                "var r=c.getBoundingClientRect();" +
                "var baseW=r.width||c.width,baseH=r.height||c.height;" +
                "var scale=Math.min((availableWidth*0.985)/baseW,(window.innerHeight*0.985)/baseH);" +
                "if(!isFinite(scale)||scale<=0)scale=1;" +
                "c.style.transform='translate(-50%,-50%) scale('+scale+')';" +
                "}catch(e){console.log('VenusKiosk fit:',e);}})();";
        view.loadUrl("javascript:" + js);
    }

    private void loadConfiguredUrl() {
        String url = getSharedPreferences(PREFS, MODE_PRIVATE).getString(KEY_URL, DEFAULT_URL);
        webView.loadUrl(url);
    }

    private void showAdminDialog() {
        final SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);

        final EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setText(prefs.getString(KEY_URL, DEFAULT_URL));
        input.setSelectAllOnFocus(true);

        final CheckBox fit = new CheckBox(this);
        fit.setText("Adatta console con sidebar Hotkeys");
        fit.setChecked(prefs.getBoolean(KEY_FIT, true));

        Button silkButton = new Button(this);
        silkButton.setText("Apri Silk");
        silkButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { openSilk(); }
        });

        Button settingsButton = new Button(this);
        settingsButton.setText("Impostazioni Android");
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { openAndroidSettings(); }
        });

        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        form.setPadding(pad, pad, pad, 0);
        form.addView(input, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        form.addView(fit, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        form.addView(silkButton, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        form.addView(settingsButton, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        new AlertDialog.Builder(this)
                .setTitle("Venus Kiosk")
                .setMessage("Dashboard")
                .setView(form)
                .setPositiveButton("Salva e ricarica", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String value = input.getText().toString().trim();
                        if (!value.startsWith("http://") && !value.startsWith("https://")) {
                            value = "http://" + value;
                        }
                        prefs.edit().putString(KEY_URL, value).putBoolean(KEY_FIT, fit.isChecked()).apply();
                        webView.loadUrl(value);
                    }
                })
                .setNeutralButton("Ricarica", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) { webView.reload(); }
                })
                .setNegativeButton("Chiudi", null)
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override public void onDismiss(DialogInterface dialog) { hideSystemUi(); }
                })
                .show();
    }

    /** Opens the Amazon Silk browser when it is installed on the device. */
    private void openSilk() {
        Intent intent = getPackageManager().getLaunchIntentForPackage("com.amazon.cloud9");
        if (intent == null) {
            Toast.makeText(this, "Silk non disponibile", Toast.LENGTH_SHORT).show();
            return;
        }
        startActivity(intent);
    }

    /** Opens the Android system settings screen. */
    private void openAndroidSettings() {
        startActivity(new Intent(Settings.ACTION_SETTINGS));
    }

    private void hideSystemUi() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) hideSystemUi();
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUi();
    }

    @Override
    public void onBackPressed() {
        hideSystemUi();
    }

    @Override
    protected void onDestroy() {
        if (webView != null) webView.destroy();
        super.onDestroy();
    }
}
