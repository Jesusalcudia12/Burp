package com.burpmobile;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class MainActivity extends Activity {
    private final int BG = Color.rgb(7, 16, 25);
    private final int PANEL = Color.rgb(15, 30, 43);
    private final int ORANGE = Color.rgb(255, 102, 0);
    private final int TEXT = Color.rgb(235, 243, 250);
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    
    private LinearLayout root, content;
    private EditText urlInput, bodyInput, decoderInput;
    private Spinner methodSpinner;
    private TextView resultText, statusText;

    @Override 
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showHome();
    }

    private TextView label(String text, int size) {
        TextView t = new TextView(this);
        t.setText(text);
        t.setTextColor(TEXT);
        t.setTextSize(size);
        t.setPadding(0, 10, 0, 10);
        return t;
    }

    private Button button(String text) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextColor(Color.WHITE);
        b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(ORANGE));
        return b;
    }

    private void begin(String title) {
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);
        root.setPadding(18, 18, 18, 18);
        setContentView(root);
        
        TextView heading = label("⚡ BurpMobile  /  " + title, 22);
        heading.setTypeface(null, Typeface.BOLD);
        root.addView(heading);
        
        Button home = button("← Inicio");
        home.setOnClickListener(v -> showHome());
        root.addView(home);
        
        ScrollView scroll = new ScrollView(this);
        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(0, 8, 0, 20);
        scroll.addView(content);
        
        LinearLayout.LayoutParams scrollParams = new LinearLayout.LayoutParams(-1, 0, 1);
        root.addView(scroll, scrollParams);
    }

    private EditText input(String hint, int minLines) {
        EditText e = new EditText(this);
        e.setHint(hint);
        e.setHintTextColor(Color.LTGRAY);
        e.setTextColor(TEXT);
        e.setTextSize(14);
        e.setPadding(12, 8, 12, 8);
        e.setBackgroundTintList(android.content.res.ColorStateList.valueOf(ORANGE));
        e.setMinLines(minLines);
        content.addView(e);
        return e;
    }

    private void showHome() {
        begin("Panel Principal");
        TextView intro = label("Herramientas de pruebas web para sistemas propios o con autorización.", 15);
        content.addView(intro);
        
        addCard("HTTP Tester", "Envía solicitudes HTTP/HTTPS y muestra la respuesta.", this::showHttp);
        addCard("Repeater", "Edita y vuelve a enviar una solicitud de prueba.", this::showHttp);
        addCard("Decoder", "Codificación y decodificación Base64.", this::showDecoder);
        addCard("Scanner (básico)", "Comprueba encabezados de seguridad de una URL autorizada.", this::showScanner);
        addCard("Proxy / Historial / Site Map", "Estas funciones avanzadas aún no están implementadas.", () -> {
            begin("En Desarrollo");
            content.addView(label("El proxy real, la captura de tráfico de otras aplicaciones y el Site Map requieren módulos adicionales. Esta versión inicial no intercepta tráfico de otras aplicaciones.", 16));
        });
        
        content.addView(label("Versión 1.0 • BurpMobile", 12));
    }

    private void addCard(String title, String desc, Runnable action) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(14, 8, 14, 8);
        card.setBackgroundColor(PANEL);
        
        TextView t = label(title, 18);
        t.setTypeface(null, Typeface.BOLD);
        TextView d = label(desc, 13);
        card.addView(t);
        card.addView(d);
        
        Button b = button("Abrir");
        b.setOnClickListener(v -> action.run());
        card.addView(b);
        
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(0, 8, 0, 8);
        content.addView(card, lp);
    }

    private void showHttp() {
        begin("HTTP Tester / Repeater");
        urlInput = input("URL (ej. https://example.com)", 1);
        urlInput.setText("https://example.com");
        
        methodSpinner = new Spinner(this);
        String[] methods = {"GET", "POST", "HEAD", "PUT", "DELETE"};
        methodSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, methods));
        content.addView(methodSpinner);
        
        bodyInput = input("Cuerpo opcional para POST/PUT", 3);
        Button send = button("Enviar Solicitud");
        content.addView(send);
        
        statusText = label("Estado: esperando solicitud", 14);
        content.addView(statusText);
        resultText = label("", 13);
        content.addView(resultText);
        
        send.setOnClickListener(v -> sendRequest());
    }

    private void sendRequest() {
        final String address = urlInput.getText().toString().trim();
        final String method = methodSpinner.getSelectedItem().toString();
        final String body = bodyInput.getText().toString();
        
        if (!(address.startsWith("http://") || address.startsWith("https://"))) {
            statusText.setText("❌ Introduce una URL HTTP o HTTPS válida.");
            return;
        }
        
        statusText.setText("⏳ Enviando solicitud…");
        resultText.setText("");
        
        new Thread(() -> {
            HttpURLConnection conn = null;
            String output;
            int code = -1;
            try {
                URL url = new URL(address);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod(method);
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);
                conn.setInstanceFollowRedirects(false);
                
                if ("POST".equals(method) || "PUT".equals(method)) {
                    conn.setDoOutput(true);
                    conn.setRequestProperty("Content-Type", "text/plain; charset=UTF-8");
                    conn.getOutputStream().write(body.getBytes(StandardCharsets.UTF_8));
                }
                
                code = conn.getResponseCode();
                InputStream stream = code >= 400 ? conn.getErrorStream() : conn.getInputStream();
                StringBuilder sb = new StringBuilder();
                
                if (stream != null) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
                    String line;
                    int count = 0;
                    while ((line = reader.readLine()) != null && count++ < 400) {
                        sb.append(line).append('\n');
                    }
                    reader.close();
                }
                
                StringBuilder headers = new StringBuilder();
                Map<String, List<String>> headerMap = conn.getHeaderFields();
                for (Map.Entry<String, List<String>> entry : headerMap.entrySet()) {
                    headers.append(entry.getKey()).append(": ").append(entry.getValue()).append('\n');
                }
                
                output = "📌 URL: " + address + 
                        "\n📌 Método: " + method + 
                        "\n\n📋 Encabezados:\n" + headers.toString() +
                        "\n📄 Respuesta:\n" + sb;
                        
            } catch (Exception ex) {
                output = "❌ Error: " + ex.getClass().getSimpleName() + ": " + ex.getMessage();
            } finally {
                if (conn != null) conn.disconnect();
            }
            
            final int status = code;
            final String finalOutput = output;
            mainHandler.post(() -> {
                if (status > 0) {
                    statusText.setText("✅ Código HTTP: " + status);
                } else {
                    statusText.setText("⚠️ Solicitud finalizada con error");
                }
                resultText.setText(finalOutput);
            });
        }).start();
    }

    private void showDecoder() {
        begin("Base64 Decoder");
        decoderInput = input("Texto o cadena Base64", 4);
        Button enc = button("Codificar → Base64");
        Button dec = button("Decodificar ← Base64");
        content.addView(enc);
        content.addView(dec);
        
        TextView out = label("", 14);
        content.addView(out);
        
        enc.setOnClickListener(v -> {
            String value = decoderInput.getText().toString();
            out.setText(Base64.encodeToString(value.getBytes(StandardCharsets.UTF_8), Base64.NO_WRAP));
        });
        
        dec.setOnClickListener(v -> {
            try {
                byte[] decoded = Base64.decode(decoderInput.getText().toString().trim(), Base64.DEFAULT);
                out.setText(new String(decoded, StandardCharsets.UTF_8));
            } catch (Exception e) {
                out.setText("❌ Base64 no válido.");
            }
        });
    }

    private void showScanner() {
        begin("Scanner de Seguridad");
        urlInput = input("URL del sitio propio/autorizado", 1);
        urlInput.setText("https://example.com");
        Button scan = button("🔍 Revisar Encabezados");
        content.addView(scan);
        resultText = label("", 14);
        content.addView(resultText);
        
        scan.setOnClickListener(v -> {
            String address = urlInput.getText().toString().trim();
            resultText.setText("⏳ Consultando encabezados…");
            
            new Thread(() -> {
                HttpURLConnection c = null;
                String report;
                try {
                    if (!(address.startsWith("http://") || address.startsWith("https://"))) {
                        throw new Exception("URL inválida");
                    }
                    c = (HttpURLConnection) new URL(address).openConnection();
                    c.setConnectTimeout(8000);
                    c.setReadTimeout(8000);
                    c.setRequestMethod("HEAD");
                    c.getResponseCode();
                    
                    String[] names = {
                        "Strict-Transport-Security",
                        "Content-Security-Policy",
                        "X-Content-Type-Options",
                        "X-Frame-Options"
                    };
                    
                    StringBuilder sb = new StringBuilder("📋 Reporte de Seguridad\n");
                    sb.append("═══════════════════════════\n");
                    sb.append("URL: ").append(address).append("\n\n");
                    
                    for (String n : names) {
                        String value = c.getHeaderField(n);
                        if (value != null) {
                            sb.append("✅ ").append(n).append(":\n   PRESENTE\n   Valor: ").append(value).append("\n\n");
                        } else {
                            sb.append("⚠️ ").append(n).append(":\n   NO DETECTADO\n\n");
                        }
                    }
                    
                    sb.append("───────────────────────────\n");
                    sb.append("⚠️ Nota: La ausencia de un encabezado no confirma por sí sola una vulnerabilidad.\n");
                    sb.append("⚠️ Úsalo únicamente en sitios de tu propiedad o con autorización escrita.");
                    report = sb.toString();
                    
                } catch (Exception e) {
                    report = "❌ Error: " + e.getMessage();
                } finally {
                    if (c != null) c.disconnect();
                }
                
                final String r = report;
                mainHandler.post(() -> resultText.setText(r));
            }).start();
        });
    }
}
