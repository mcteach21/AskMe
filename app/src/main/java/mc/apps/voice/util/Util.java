package mc.apps.voice.util;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Util {

    static class KeyWord{
        private String word;
        private List<String> synonyms;

        public KeyWord(String word) {
            this.word = word;
            this.synonyms = new ArrayList<>();
        }
        public void addSynonyms(List<String> synonyms) {
            this.synonyms.addAll(synonyms);
        }
        public String getWord() {
            return this.word;
        }
        public List<String> getSynonyms() {
            return this.synonyms;
        }
    }

    static List<KeyWord> keywords = new ArrayList<>();
    static {
        KeyWord sms = new KeyWord("SMS");
        KeyWord mail = new KeyWord("Mail");
        KeyWord call =  new KeyWord("Appel");
        KeyWord photo = new KeyWord("Photo");
        KeyWord galerie = new KeyWord("Galerie");

        KeyWord youtube = new KeyWord("Youtube");
        //KeyWord viber = new KeyWord("Viber");

        sms.addSynonyms( Arrays.asList("sms","message","texto"));
        mail.addSynonyms( Arrays.asList("email","message"));
        call.addSynonyms( Arrays.asList("appel","appeler","téléphoner", "composer", "call", "allo"));
        photo.addSynonyms( Arrays.asList("photos","cheese","sourire", "selfie", "wistiti"));
        galerie.addSynonyms( Arrays.asList("galerie","maelys"));
        youtube.addSynonyms( Arrays.asList("vidéo","visionner"));

        keywords.addAll(Arrays.asList(sms,mail,call,photo,youtube));
    }
    public static String formatKeyWord(String word){
        return word.substring(0,1).toUpperCase()+word.substring(1).toLowerCase();
    }
    public static boolean isKeyWord(String word){
        for (KeyWord kw: keywords)
            if(kw.getWord().equalsIgnoreCase(word))
                return true;
        return false;
    }
    public static boolean isKeyWordSynonym(String word){
        for (KeyWord kw: keywords)
            if(kw.getSynonyms().contains(word.toLowerCase()))
                return true;
        return false;
    }
    public static String keyWordSynonym(String word){
        for (KeyWord kw: keywords)
            if(kw.getSynonyms().contains(word))
                return kw.getWord();
        return null;
    }

    /**
     * Phone, SMS, Email, youtube,...
     */
    public static void sendSMS(Activity context, String phoneNumber, String message) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("smsto:"+phoneNumber));
        intent.putExtra("sms_body", message);

        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        }

/*      SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage("PhoneNumber-example:+989147375410", null, "SMS Message Body", null, null);*/
    }
    public static void sendEmail(Activity context, String email, String message) {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto",email, null));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Hello!");
        emailIntent.putExtra(Intent.EXTRA_TEXT, message);
        context.startActivity(Intent.createChooser(emailIntent, "Jarvis"));
    }
    public static void makeCall(Activity context, String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumber));
        context.startActivity(intent);
    }
    public static void gotoYoutube(Activity context, String query) {
        Toast.makeText(context, "show video on youtube..", Toast.LENGTH_SHORT).show();
    }

    /**
     * Permissions
     */
}