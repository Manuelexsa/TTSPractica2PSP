package com.example.kronos.ttspractica2psp;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

public class Principal extends Activity implements TextToSpeech.OnInitListener {

    ChatterBotFactory factory;
    ChatterBot bot1;
    ChatterBotSession bot1Session;
    private static final int CH = 1, CH1 = 2;
    Button btTalk;
    private TextToSpeech tts;
    String frase = "";
    float tono=1, velocidad=1;
    private TextView et;
    private boolean reproductor = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actividad_principal);
        Intent i = new Intent();
        et = (TextView) findViewById(R.id.tvCombersacion);
        i.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(i, CH);
        factory = new ChatterBotFactory();
        btTalk = (Button) findViewById(R.id.btnHablar);
        try {
            bot1 = factory.create(ChatterBotType.CLEVERBOT);
        } catch (Exception e) {
            e.printStackTrace();
        }
        bot1Session = bot1.createSession();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.principal, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.es) {
            tts.setLanguage(new Locale("es", "ES"));
        } else if (id == R.id.fr) {
            tts.setLanguage(Locale.FRENCH);
        } else if (id == R.id.uk) {
            tts.setLanguage(Locale.UK);
        }
        return super.onOptionsItemSelected(item);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CH) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                tts = new TextToSpeech(this,this);
            } else {
                Intent intent = new Intent();
                intent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(intent);
            }
        } else if (requestCode == CH1) {
            if (resultCode == RESULT_OK) {
                ArrayList<String> textos = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                frase = textos.get(0);
                btTalk.setClickable(false);
                Hebra hf = new Hebra();
                hf.execute();
            }
        }
    }

    @Override
    public void onInit(int i) {
        if(i == TextToSpeech.SUCCESS){
            //Se puede reproducir
            reproductor = true;
            tts.setLanguage(new Locale("es", "ES"));
            tts.setPitch(tono);
            tts.setSpeechRate(velocidad);
        } else {
            //No se puede reproducir
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            tts = null;
        }
    }
    public void hablar(View v) {
        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "es-ES");
        i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Habla ahora");
        i.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 3000);
        startActivityForResult(i, CH1);
    }
    class Hebra extends AsyncTask<Object, Integer, String> {

        Hebra(String... p) {
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Object[] params) {
            String respuesta = "";
            try {
                respuesta = bot1Session.think((frase));
            } catch (Exception ex) {
                Log.v("excepciondoinback: ", ex.toString());
            }
            return respuesta;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if(reproductor){
                tts.speak(s, TextToSpeech.QUEUE_ADD, null);
                et.setText(s);
                btTalk.setClickable(true);
            } else {
            }
            frase = "";
        }
    }
}
