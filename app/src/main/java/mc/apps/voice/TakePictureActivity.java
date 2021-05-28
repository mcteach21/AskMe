package mc.apps.voice;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import mc.apps.voice.camera.CameraFragment;


public class TakePictureActivity extends AppCompatActivity {
    private static final String TAG = "samples";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().hide();

        setContentView(R.layout.camera_activity);
        Fragment fragment = CameraFragment.newInstance();
        fragment.setArguments(getIntent().getExtras());

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }

}