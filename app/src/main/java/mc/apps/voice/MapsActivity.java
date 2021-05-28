package mc.apps.voice;

import androidx.fragment.app.FragmentActivity;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private String adress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Intent intent = getIntent();
        adress = intent.getStringExtra("address");

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    public LatLng getLocationFromAddress(Context context, String strAddress)
    {
        Geocoder coder= new Geocoder(context);
        List<Address> address;
        LatLng p1 = null;

        try
        {
            address = coder.getFromLocationName(strAddress, 5);
            if(address==null)
            {
                return null;
            }
            Address location = address.get(0);
            location.getLatitude();
            location.getLongitude();

            p1 = new LatLng(location.getLatitude(), location.getLongitude());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return p1;

    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng address = getLocationFromAddress(this, adress);
        if(address==null)
            return;

        mMap.addMarker(new MarkerOptions().position(address).title("My Marker"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(address));
        mMap.animateCamera( CameraUpdateFactory.zoomTo( 10.0f ) );

        new Handler().postDelayed(() -> mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(address, 16)), 800);
    }
}