package jp.co.nri.voice.demo;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private TextView textView;
    private TextToSpeech textToSpeech;
    private EditText editText;
    private EditText editText2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // -----------------
        // 音声認識
        textView = findViewById(R.id.textView);
        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startVoiceRecognitionActivity();
            }
        });


        // -----------------
        // テキストターンスピーチ
        textToSpeech = new TextToSpeech(this, this);
        editText = findViewById(R.id.editText);
        findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (textToSpeech != null && !textToSpeech.isSpeaking() && editText.getText().toString().length() > 0) {
                    // トーンを設定し、値が大きいほど、音 (女の子) がよりシャープになり、値が小さいほど男性の声になり、1.0 は一般
                    textToSpeech.setPitch(0.5f);
                    // 音声の速度を設定します, デフォルト1.0 通常の速度
                    textToSpeech.setSpeechRate(1.0f);
                    textToSpeech.speak(editText.getText().toString(), TextToSpeech.QUEUE_FLUSH, null, null);
                }
            }
        });

        // -----------------
        // 翻訳
        // 翻訳 SDK は一般に有料です。 ただし、翻訳には多くの web バージョンがあるため、クローラとして使用できます。
        editText2 = findViewById(R.id.editText2);
        findViewById(R.id.button3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TranslateUtil.TranslateCallback translateCallback = new TranslateUtil.TranslateCallback() {
                    @Override
                    public void onTranslateDone(String result) {
                        editText2.setText(result);
                    }
                };
                new TranslateUtil().translate(MainActivity.this, "auto", "ja", editText2.getText().toString(), translateCallback);
            }
        });

    }

    /**
     * Google 音声認識を呼び出す
     */
    private void startVoiceRecognitionActivity() {
        try {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "音声の開始");
            startActivityForResult(intent, 1001);
        } catch (Exception e) {
            e.printStackTrace();
            showDialog();
        }
    }

    private void showDialog() {
        new AlertDialog.Builder(this)
            .setMessage("音声認識デバイスが見つかりませんでしたが、ダウンロードされていますか?")
            .setTitle("ヒント")
            .setNegativeButton("Download",
                    new android.content.DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.voicesearch");
                            Intent it = new Intent(Intent.ACTION_VIEW, uri);
                            startActivity(it);
                        }
                    })
            .setPositiveButton("Cancel",
                    new android.content.DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            StringBuilder sb = new StringBuilder();
            if(results != null){
                for(String s : results){
                    sb.append(s);
                    sb.append("\n");
                }
            }
            textView.setText(String.format("結果の特定：\n%s", sb.toString()));
        }
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = textToSpeech.setLanguage(Locale.CHINA);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(this, "データ損失またはサポートされない", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        textToSpeech.stop();
        textToSpeech.shutdown();
    }

}
