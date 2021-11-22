package com.goblob.covid.ui.geofence;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.goblob.covid.R;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.sql.Ref;
import java.util.List;


public class GeofenceDialogFragment extends DialogFragment implements View.OnClickListener {

    private static final String PHONE_TAG = "phoneDialog";
    private EditText geofenceName, geofenceAddress;
    private Spinner geofenceType;
    private Button cancel, add;
    private static GeofenceDialogFragment dialogFragment;
    final private ParseObject geofence = null;

    public static GeofenceDialogFragment newInstance(){
        dialogFragment = new GeofenceDialogFragment();
        return dialogFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceBundle){
        super.onCreate(savedInstanceBundle);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        if (id == R.id.cancelButton) {
            dismiss();
        } else if (id == R.id.addButton) {
            ParseQuery<ParseObject> query = ParseQuery.getQuery("Geofence");
            query.fromPin("temp_geofence");
            query.getFirstInBackground(new GetCallback<ParseObject>() {
                @Override
                public void done(final ParseObject geofence, ParseException e) {
                    geofence.put("type", geofenceType.getSelectedItemPosition());
                    geofence.put("user", ParseUser.getCurrentUser());
                    geofence.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                geofence.pinInBackground("geofence");
                            }
                        }
                    });
                    geofence.unpinInBackground("temp_geofence");
                }
            });
            RefreshAdapter listener = (RefreshAdapter) getActivity();
            listener.refreshAdapter();
            dismiss();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View layout = inflater.inflate(R.layout.geofence_details, container, false);
        geofenceName = layout.findViewById(R.id.geofence_name);
        geofenceName.requestFocus();
        geofenceAddress = layout.findViewById(R.id.geofence_address);
        geofenceAddress.requestFocus();

        geofenceType = layout.findViewById(R.id.geofence_type);
        cancel = layout.findViewById(R.id.cancelButton);
        cancel.setOnClickListener(this);
        add = layout.findViewById(R.id.addButton);
        add.setOnClickListener(this);

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Geofence");
        query.fromPin("temp_geofence");
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject geofence, ParseException e) {
                geofenceName.setText((CharSequence) geofence.get("name"));
                geofenceAddress.setText((CharSequence) geofence.get("address"));
            }
        });

        dialogFragment.getDialog().setTitle("New Safe Place");

        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        return layout;
    }

    public void open(FragmentManager fragmentManager) {
        if (fragmentManager.findFragmentByTag(PHONE_TAG) == null){
            show(fragmentManager, PHONE_TAG);
        }
    }

    public interface RefreshAdapter {
        void refreshAdapter();
        // void addPhone(IPhoneNumber phoneNumber, boolean addToProfile);
    }

    public interface ChangePhonePrivacy {
        void changePhonePrivacy(boolean privacy);
    }
}
