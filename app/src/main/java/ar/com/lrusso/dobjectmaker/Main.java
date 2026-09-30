package ar.com.lrusso.dobjectmaker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Html;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Calendar;

/* loaded from: /storage/emulated/0/Android/data/com.apktools.app.decompile/files/decompile_temp/jadx/classes.dex */
public class Main extends Activity {
    private static final int FILECHOOSER_RESULTCODE = 1;
    private static ValueCallback mUploadMessage;
    private static ValueCallback mUploadMessage5;
    private Activity myActivity;
    private Context myContext;
    private WebView webView;

    static /* synthetic */ ValueCallback access$002(ValueCallback valueCallback) {
        mUploadMessage = valueCallback;
        return valueCallback;
    }

    static /* synthetic */ ValueCallback access$102(ValueCallback valueCallback) {
        mUploadMessage5 = valueCallback;
        return valueCallback;
    }

    static /* synthetic */ WebView access$200(Main main) {
        return main.webView;
    }

    static /* synthetic */ void access$300(Main main) {
        main.clickInPrivacy();
    }

    static /* synthetic */ void access$400(Main main) {
        main.clickInAbout();
    }

    static /* synthetic */ Activity access$500(Main main) {
        return main.myActivity;
    }

    static /* synthetic */ void access$600(Main main) {
        main.setImportantNoteShowed();
    }

    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(2130903040);
        this.myContext = this;
        this.myActivity = this;
        showLowResDeviceMessage();
        WebView findViewById = findViewById(2130837507);
        this.webView = findViewById;
        findViewById.getSettings().setJavaScriptEnabled(true);
        this.webView.getSettings().setAllowFileAccess(true);
        this.webView.getSettings().setLoadWithOverviewMode(true);
        this.webView.getSettings().setUseWideViewPort(true);
        this.webView.loadDataWithBaseURL("file:///android_asset/", loadAssetTextAsString("index.html"), "text/html", "utf-8", null);
        this.webView.setWebViewClient(new myWebClient());
        this.webView.setWebChromeClient(new CustomWebChromeClient());
        if (Build.VERSION.SDK_INT >= 23) {
            try {
                iniciarVerificacionMarshmallow();
            } catch (Exception unused) {
            }
        }
    }

    class CustomWebChromeClient extends WebChromeClient {
        CustomWebChromeClient() {
        }

        public void openFileChooser(ValueCallback valueCallback) {
            Main.access$002(valueCallback);
            Intent intent = new Intent("android.intent.action.GET_CONTENT");
            intent.addCategory("android.intent.category.OPENABLE");
            intent.setType("*/*");
            Main.this.startActivityForResult(Intent.createChooser(intent, "File Chooser"), 1);
        }

        public void openFileChooser(ValueCallback valueCallback, String str) {
            Main.access$002(valueCallback);
            Intent intent = new Intent("android.intent.action.GET_CONTENT");
            intent.addCategory("android.intent.category.OPENABLE");
            intent.setType("*/*");
            Main.this.startActivityForResult(Intent.createChooser(intent, "File Browser"), 1);
        }

        public void openFileChooser(ValueCallback valueCallback, String str, String str2) {
            Main.access$002(valueCallback);
            Intent intent = new Intent("android.intent.action.GET_CONTENT");
            intent.addCategory("android.intent.category.OPENABLE");
            intent.setType("*/*");
            Main.this.startActivityForResult(Intent.createChooser(intent, "File Chooser"), 1);
        }

        public boolean onShowFileChooser(WebView webView, ValueCallback valueCallback, WebChromeClient.FileChooserParams fileChooserParams) {
            Main.access$102(valueCallback);
            Intent intent = new Intent("android.intent.action.GET_CONTENT");
            intent.addCategory("android.intent.category.OPENABLE");
            intent.setType("*/*");
            Main.this.startActivityForResult(Intent.createChooser(intent, "File Chooser"), 1);
            return true;
        }

        public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
            String message = consoleMessage.message();
            if (!message.startsWith("STLFILE---") && !message.startsWith("SCENEFILE---")) {
                return true;
            }
            if (Build.VERSION.SDK_INT >= 29) {
                Main.this.writeFileNewLogic(message);
                return true;
            }
            Main.this.writeFileLegacy(message);
            return true;
        }
    }

    protected void onActivityResult(int i, int i2, Intent intent) {
        try {
            if (i2 == -1) {
                if (i == 1) {
                    mUploadMessage5.onReceiveValue(new Uri[]{(intent == null || i2 != -1) ? null : intent.getData()});
                    mUploadMessage5 = null;
                    return;
                } else {
                    if (mUploadMessage == null) {
                        return;
                    }
                    mUploadMessage.onReceiveValue((intent == null || i2 != -1) ? null : intent.getData());
                    mUploadMessage = null;
                    return;
                }
            }
            ValueCallback valueCallback = mUploadMessage5;
            if (valueCallback != null) {
                valueCallback.onReceiveValue((Object) null);
                mUploadMessage5 = null;
            }
            ValueCallback valueCallback2 = mUploadMessage;
            if (valueCallback2 != null) {
                valueCallback2.onReceiveValue((Object) null);
                mUploadMessage = null;
            }
        } catch (Exception unused) {
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(2130968576, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == 2130837505) {
            PopupMenu popupMenu = new PopupMenu(this, findViewById(2130837505));
            popupMenu.inflate(2130968577);
            popupMenu.setOnMenuItemClickListener(new CustomMenuItemClickListener());
            popupMenu.show();
            return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    class CustomMenuItemClickListener implements PopupMenu.OnMenuItemClickListener {
        CustomMenuItemClickListener() {
        }

        public boolean onMenuItemClick(MenuItem menuItem) {
            if (menuItem.getTitle().toString().contains(Main.this.getResources().getString(2131034125))) {
                try {
                    Main.access$200(Main.this).getSettings().setUseWideViewPort(false);
                    Main.access$200(Main.this).getSettings().setUseWideViewPort(true);
                    Main.access$200(Main.this).setInitialScale(1);
                } catch (Exception unused) {
                }
            } else if (menuItem.getTitle().toString().contains(Main.this.getResources().getString(2131034122))) {
                Main.access$300(Main.this);
            } else if (menuItem.getTitle().toString().contains(Main.this.getResources().getString(2131034115))) {
                Main.access$400(Main.this);
            }
            return true;
        }
    }

    public boolean onKeyUp(int i, KeyEvent keyEvent) {
        if (i == 4) {
            try {
                if (keyEvent.getRepeatCount() == 0) {
                    clickInExit();
                    return false;
                }
            } catch (NullPointerException unused) {
            }
        }
        return super.onKeyUp(i, keyEvent);
    }

    class ExitClickListener implements DialogInterface.OnClickListener {
        ExitClickListener() {
        }

        public void onClick(DialogInterface dialogInterface, int i) {
            if (i != -1) {
                return;
            }
            Main.access$500(Main.this).finish();
            System.exit(0);
        }
    }

    private void clickInExit() {
        ExitClickListener r0 = new ExitClickListener();
        new AlertDialog.Builder(this).setMessage(2131034117).setPositiveButton(2131034124, r0).setNegativeButton(2131034120, r0).show();
    }

    private void clickInPrivacy() {
        View inflate = LayoutInflater.from(this).inflate(2130903041, (ViewGroup) null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(2131034122));
        builder.setView(inflate);
        builder.setPositiveButton(getResources().getString(2131034121), new PrivacyClickListener());
        builder.show();
    }

    class PrivacyClickListener implements DialogInterface.OnClickListener {
        public void onClick(DialogInterface dialogInterface, int i) {
        }

        PrivacyClickListener() {
        }
    }

    private void clickInAbout() {
        String replace = getResources().getString(2131034116).replace("APPNAME", getResources().getString(2131034113));
        TextView textView = new TextView(this);
        textView.setText(Html.fromHtml(replace));
        textView.setPadding(10, 20, 10, 25);
        textView.setGravity(17);
        textView.setTextSize(new EditText(this).getTextSize() / getResources().getDisplayMetrics().scaledDensity);
        new AlertDialog.Builder(this).setTitle(getResources().getString(2131034115)).setView(textView).setIcon(2130771969).setPositiveButton(getResources().getString(2131034121), new AboutClickListener()).show();
    }

    class AboutClickListener implements DialogInterface.OnClickListener {
        public void onClick(DialogInterface dialogInterface, int i) {
        }

        AboutClickListener() {
        }
    }

    private String loadAssetTextAsString(String str) {
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(getAssets().open(str)))) {
            StringBuilder sb = new StringBuilder();
            String readLine;
            boolean first = true;
            while ((readLine = bufferedReader.readLine()) != null) {
                if (first) {
                    first = false;
                } else {
                    sb.append("\n");
                }
                sb.append(readLine);
            }
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }

    public void iniciarVerificacionMarshmallow() {
        if (this.checkSelfPermission("android.permission.READ_EXTERNAL_STORAGE") != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            this.requestPermissions(new String[]{"android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE"}, 123);
        }
    }

    public void onRequestPermissionsResult(int i, String[] strArr, int[] iArr) {
        super.onRequestPermissionsResult(i, strArr, iArr);
        if (i != 123 || iArr.length <= 0) {
            return;
        }
        int i2 = iArr[0];
    }

    private String getImportantNoteShowed() {
        String str = "";
        DataInputStream dataInputStream = null;
        try {
            DataInputStream dataInputStream2 = new DataInputStream(openFileInput("note.cfg"));
            while (true) {
                try {
                    str = str + dataInputStream2.readUTF();
                } catch (Exception unused) {
                    dataInputStream = dataInputStream2;
                    try {
                        dataInputStream.close();
                    } catch (Exception unused2) {
                    }
                    return str;
                }
            }
        } catch (Exception unused3) {
        }
        return str;
    }

    private void setImportantNoteShowed() {
        try {
            DataOutputStream dataOutputStream = new DataOutputStream(openFileOutput("note.cfg", 0));
            dataOutputStream.writeUTF("123");
            dataOutputStream.close();
        } catch (Exception unused) {
        }
    }

    public void showLowResDeviceMessage() {
        if (getImportantNoteShowed() == "") {
            new AlertDialog.Builder(new ContextThemeWrapper(this, 16973941)).setCancelable(false).setTitle(getResources().getString(2131034127)).setMessage(getResources().getString(2131034126)).setPositiveButton(getResources().getString(2131034121), new LowResClickListener()).show();
        }
    }

    class LowResClickListener implements DialogInterface.OnClickListener {
        LowResClickListener() {
        }

        public void onClick(DialogInterface dialogInterface, int i) {
            Main.access$600(Main.this);
        }
    }

    public void writeFileLegacy(String str) {
        String str2;
        String str3;
        File file = new File(String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)));
        if (!file.exists()) {
            file.mkdirs();
        }
        if (str.startsWith("STLFILE---")) {
            str2 = str.substring(10, str.length());
            str3 = ".stl";
        } else if (!str.startsWith("SCENEFILE---")) {
            str2 = "";
            str3 = "";
        } else {
            str2 = str.substring(12, str.length());
            str3 = ".scene";
        }
        String string = getResources().getString(2131034118);
        boolean z = false;
        int i = 0;
        while (!z) {
            if (i == 0) {
                if (new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), string + str3).exists()) {
                    i++;
                } else {
                    string = string + str3;
                    z = true;
                }
            } else {
                if (new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), string + "(" + i + ")" + str3).exists()) {
                    i++;
                } else {
                    string = string + "(" + i + ")" + str3;
                    z = true;
                }
            }
        }
        try {
            File file2 = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), string);
            file2.createNewFile();
            FileOutputStream fileOutputStream = new FileOutputStream(file2);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
            outputStreamWriter.append(str2);
            outputStreamWriter.close();
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (Exception unused) {
        }
        Context context = this.myContext;
        Toast.makeText(context, context.getResources().getString(2131034119), 0).show();
    }

    public void writeFileNewLogic(String str) {
        String str2;
        String str3 = "3D_Project_" + Calendar.getInstance().get(1) + "_" + Calendar.getInstance().get(2) + "_" + Calendar.getInstance().get(5) + "_" + Calendar.getInstance().get(11) + "_" + Calendar.getInstance().get(12) + "_" + Calendar.getInstance().get(13);
        if (str.startsWith("STLFILE---")) {
            str3 = str3 + ".stl";
            str2 = str.substring(10, str.length());
        } else if (!str.startsWith("SCENEFILE---")) {
            str2 = "";
        } else {
            str3 = str3 + ".scene";
            str2 = str.substring(12, str.length());
        }
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put("_display_name", str3);
            contentValues.put("mime_type", "application/octet-stream");
            contentValues.put("relative_path", Environment.DIRECTORY_DOWNLOADS);
            OutputStream openOutputStream = this.myContext.getContentResolver().openOutputStream(this.myContext.getContentResolver().insert(MediaStore.Files.getContentUri("external"), contentValues));
            openOutputStream.write(str2.getBytes());
            openOutputStream.close();
        } catch (Exception unused) {
        }
        Context context = this.myContext;
        Toast.makeText(context, context.getResources().getString(2131034119), 0).show();
    }
}
