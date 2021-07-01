package mc.apps.voice;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import javax.security.auth.login.LoginException;

import mc.apps.voice.util.Query;
import mc.apps.voice.util.Util;


public class VoiceActivity extends AppCompatActivity {
    //TODO : add recorded commands (start auto.)!

    private static final String TAG = "samples";
    private static final int PERMISSIONS_REQUEST_ALL_PERMISSIONS = 1;

    private View view;
    private EditText editText;
    private ImageView micButton;
    private SpeechRecognizer mSpeechRecognizer;
    private Intent mSpeechRecognizerIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.voice_activity);

        if(needPermissions(this)){
            requestPermissions();
            finish();
        }

        getSupportActionBar().hide();
        setVoiceSearch();
        handleList();
    }
    private void setVoiceSearch() {
        editText = findViewById(R.id.text);
        micButton = findViewById(R.id.mic);

        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        RecognitionListener recognitionListener = new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {
            }
            @Override
            public void onBeginningOfSpeech() {
                editText.setText("");
                editText.setHint("Listening...");
            }
            @Override
            public void onRmsChanged(float v) {
            }
            @Override
            public void onBufferReceived(byte[] bytes) {
            }
            @Override
            public void onEndOfSpeech() {
                editText.setHint("Analysing speech...");
            }
            @Override
            public void onError(int error) {
                String message;
                switch (error) {
                    case SpeechRecognizer.ERROR_AUDIO:
                        message = "Audio recording error";
                        break;
                    case SpeechRecognizer.ERROR_CLIENT:
                        message = "?";
                        break;
                    case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                        message = "Insufficient permissions";
                        break;
                    case SpeechRecognizer.ERROR_NETWORK:
                        message = "Network error";
                        break;
                    case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                        message = "Network timeout";
                        break;
                    case SpeechRecognizer.ERROR_NO_MATCH:
                        message = "No match";

                        mSpeechRecognizer.stopListening();
                        micButton.setImageResource(R.drawable.ic_action_micro);
                        break;
                    case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                        message = "RecognitionService busy";
                        break;
                    case SpeechRecognizer.ERROR_SERVER:
                        message = "error from server";
                        break;
                    case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                        message = "No speech input";
                        break;
                    default:
                        message = "Didn't understand, please try again.";
                        break;
                }
                editText.setText(message);


            }
            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null)
                    editText.setText(matches.get(0));
                micButton.setImageResource(R.drawable.ic_action_micro);
            }
            @Override
            public void onPartialResults(Bundle bundle) {
            }
            @Override
            public void onEvent(int i, Bundle bundle) {
            }
        };
        mSpeechRecognizer.setRecognitionListener(recognitionListener);
        mSpeechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        micButton.setOnTouchListener((view, motionEvent) -> {
            //Toast.makeText(this, "setOnTouchListener..", Toast.LENGTH_SHORT).show();

            if (motionEvent.getAction() == MotionEvent.ACTION_UP){
                mSpeechRecognizer.stopListening();
                micButton.setImageResource(R.drawable.ic_action_micro);
            }
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                micButton.setImageResource(R.drawable.ic_action_micro_red);
                mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
            }

            return false;
        });

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void afterTextChanged(Editable editable) {
                analyzeCommand(editable.toString());
            }
        });

        micButton.setImageResource(R.drawable.ic_action_micro_red);
        mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
    }


    private void createCommands(List<String> keywords, List<Query.Contact> contacts, List<String> words) {
        List<String> commands = new ArrayList<>();
        String cmd="", options="";

        Log.i(TAG, "*****************************************");
        Log.i(TAG, "Create Commands - Keywords: ");
        keywords.forEach(kw -> Log.i(TAG, kw));
        Log.i(TAG, "*****************************************");

        Log.i(TAG, "Create Commands - Words: ");
        if(words!=null)
            words.forEach(o -> Log.i(TAG, o));
        Log.i(TAG, "*****************************************");

        Log.i(TAG, "Create Commands - contacts: ");
        if(contacts!=null)
            contacts.forEach(o -> Log.i(TAG, o.name));
        Log.i(TAG, "=========================================");

        for (String keyword : keywords) {
            cmd = keyword;
            if (contacts != null)
                for (Query.Contact contact : contacts) {
                    options = contact.name + "\n" + contact.phoneNumber + (contact.email == null ? "" : "\n" + contact.email);
                    commands.add(cmd + "\n" + options);
                }
            if (words != null) {
                options = words.stream().map(Object::toString).collect(Collectors.joining("+"));

                if(!options.equals("?") && !options.isEmpty())
                    commands.add(cmd + "\n" + options);

                Log.i(TAG, "Create Commands - Cmd+Options = "+cmd + "+" + options);
            }
            if (contacts == null && words == null) {
                commands.add(cmd);
            }
        }


        updateList(commands);
    }

    RecyclerView list;
    private void handleList() {
        list = findViewById(R.id.list);
        StaggeredGridLayoutManager gridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        list.setLayoutManager(gridLayoutManager);

        List<String> commands = new ArrayList<>();
        CommandsAdapter adapter = new CommandsAdapter(commands);
        list.setAdapter(adapter);
    }
    private void updateList(List<String> commands) {
        CommandsAdapter adapter = (CommandsAdapter) list.getAdapter();
        adapter.updateData(commands);
    }

    List<String> others;
    private void analyzeCommand(String command){
            String[] words = command.split(" ");
            List<String> keywords = new ArrayList<>();
            others = new ArrayList<>();
            for (String word : words) {
                if (!word.trim().isEmpty()) {
                    Log.i(TAG, "analyze Command/Word : " + word);

                    if (Util.isKeyWord(word))
                        keywords.add(Util.formatKeyWord(word));
                    else if (Util.isKeyWordSynonym(word)) {
                        Log.i(TAG, "analyzeCommand - Synonym : "+word);
                        keywords.add(word + " (" + Util.keyWordSynonym(word) + ")");
                    }else
                        others.add(word);
                }
            }

            updateList(new ArrayList<>());

            if(keywords.isEmpty()) {
                //Log.i(TAG, "analyzeCommand: keywords empty!!!!");
                keywords.addAll(Arrays.asList("Google","Youtube")); //default
                //keywords.add("internet"); //default
            }

            if(!others.isEmpty()) {
                boolean forYoutube = keywords.stream().filter(kw->kw.contains("Youtube")).count()>0;
                boolean forMaps = keywords.stream().filter(kw->kw.contains("Maps")).count()>0;
                boolean forGoogle = keywords.stream().filter(kw->kw.contains("Google")).count()>0;

                if(!forYoutube && !forMaps && !forGoogle)
                    for (String search : others)
                        Query.searchContact(this, search, data -> {
                            List<Query.Contact> contacts = (List<Query.Contact>) data;
                            createCommands(keywords, contacts, null);
                        });
                else{
                    List<String> clean_words = others.stream().filter(x->!Util.isPreposition(x)).collect(Collectors.toList());
                    clean_words = Util.cleanSearch(clean_words); //exclude words..

                    createCommands(keywords, null, clean_words);
                }
            }else {
                createCommands(keywords, null, null);
            }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSpeechRecognizer.destroy();
    }


    /**
     * Permissions
     */
    static public boolean needPermissions(Activity activity) {
        Log.d(TAG, "needPermissions..");
        return activity.checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED
                || activity.checkSelfPermission(Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED
                || activity.checkSelfPermission(Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED
                || activity.checkSelfPermission(Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED
                || activity.checkSelfPermission(Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED
                || activity.checkSelfPermission(Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED
                || activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
                ;
    }
    private void requestPermissions() {
        Log.d(TAG, "requestPermissions..");
        String[] permissions = new String[] {
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CAMERA,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.CALL_PHONE,
                Manifest.permission.SEND_SMS,
                Manifest.permission.INTERNET,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        requestPermissions(permissions, PERMISSIONS_REQUEST_ALL_PERMISSIONS);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "onRequestPermissionsResult..");
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ALL_PERMISSIONS:
                boolean hasAllPermissions = true;
                for (int i = 0; i < grantResults.length; ++i) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        hasAllPermissions = false;
                        Log.e(TAG, "Unable to get permission " + permissions[i]);
                    }
                }
                if (hasAllPermissions) {
                    Toast.makeText(this, "All required permissions granted!", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(this, "Unable to get all required permissions", Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }
                break;
            default:
                Log.e(TAG, "Unexpected request code");
        }
    }

    /**
     * List Adapter
     */
    class CommandsAdapter extends RecyclerView.Adapter<CommandsAdapter.CommandViewHolder>{
        private static final String TAG = "samples" ;
        private static final long FADE_DURATION = 500 ;
        List<String> data;
        public CommandsAdapter(List<String> data){
            this.data = data;
        }
        public void updateData(List<String> items){
            this.data.clear();
            this.data.addAll(items);

            new Handler(Looper.getMainLooper()).post(new Runnable() {
                public void run() {
                    notifyDataSetChanged();
                }
            });
        }

        @NonNull
        @Override
        public CommandViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout, parent, false);
            return new CommandViewHolder(itemView);
        }
        @Override
        public void onBindViewHolder(@NonNull CommandViewHolder holder, int position) {
            holder.item_title.setText(data.get(position));
            setFadeAnimation(holder.itemView);
        }
        private void setFadeAnimation(View view) {
            AlphaAnimation anim = new AlphaAnimation(0.0f, 1.0f);
            anim.setDuration(FADE_DURATION);
            view.startAnimation(anim);
        }
        @Override
        public int getItemCount() {
            return data.size();
        }
        class CommandViewHolder extends RecyclerView.ViewHolder {
            TextView item_title;
            public CommandViewHolder(@NonNull View itemView) {
                super(itemView);
                item_title = itemView.findViewById(R.id.item_title);
                itemView.setOnClickListener(view -> {
                    String text = data.get(getAdapterPosition());
                    item_title.setTextColor(getResources().getColor(R.color.colorAccent, null));
                    commandToAction(text);
                });
                //itemView.setOnLongClickListener(view -> {animate(itemView); return false;});
            }
        }
    }

    private void commandToAction(String command_text) {
        String[] parts = command_text.split("\n");

        String cmd = parts[0];
        String contact = parts.length>1?parts[1]:"";
        String phone = parts.length>2?parts[2]:"";
        String email = parts.length>3?parts[3]:"";

        if(cmd.contains("SMS"))
            Util.sendSMS(VoiceActivity.this, phone,"Hello from Jarvis!");
        else if(cmd.contains("Appel"))
            Util.makeCall(VoiceActivity.this, phone);
        else if(cmd.contains("Mail"))
            Util.sendEmail(VoiceActivity.this, email,"Hello from Jarvis!");
        else if(cmd.contains("Viber"))
            callViberChat(phone);
        else if(cmd.contains("Youtube")) {
            /*Log.i(TAG, "==================================");*/
            String query="";
            for (String word:others ) {
                if (!Util.isPreposition(word))
                    query +=("".equals(query)?"":"+")+word;
            }
            /*Log.i(TAG, "commandToAction: "+query);
            Log.i(TAG, "==================================");*/
            Util.gotoYoutube(VoiceActivity.this, query);
        }else if(cmd.contains("Maps")){
            Intent intent = new Intent(VoiceActivity.this, MapsActivity.class);

            String address="";
            for (String word:others)
                address +=("".equals(address)?"":"+")+word;
            intent.putExtra("address",address);
            startActivity(intent);
        } else if(cmd.contains("Google")) {
            String query="";
            for (String word:others )
                    query +=("".equals(query)?"":"+")+word;
            Util.gotoGoogle(VoiceActivity.this, query);
        }
        else if(cmd.contains("Photo")) {
            Log.i(TAG, "commandToAction: call TakePictureActivity!!");
            startActivity(new Intent(VoiceActivity.this, TakePictureActivity.class));
        }else
            Toast.makeText(this, "other command : "+cmd, Toast.LENGTH_LONG).show();

    }

    private void callViberChat(String phone) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("viber://add?number="+phone));
        intent.setPackage("com.viber.voip");
        startActivity(intent);
    }


}