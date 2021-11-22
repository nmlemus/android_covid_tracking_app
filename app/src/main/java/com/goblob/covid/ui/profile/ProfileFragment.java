package com.goblob.covid.ui.profile;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.goblob.covid.R;
import com.goblob.covid.dagger.Injectable;
import com.goblob.covid.data.dao.factory.DAOFactory;
import com.goblob.covid.utils.ClickToSelectEditText;
import com.goblob.covid.utils.Countries;
import com.goblob.covid.utils.SexItems;
import com.hbb20.CountryCodePicker;
import com.mukesh.countrypicker.Country;
import com.mukesh.countrypicker.CountryPicker;
import com.mukesh.countrypicker.OnCountryPickerListener;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment implements OnCountryPickerListener, View.OnClickListener {

    private View root;
    private EditText age;
    private ClickToSelectEditText gender;
    private Button save;
    private String genderName = "";
    private EditText displayName;
    private TextView description;
    private CountryCodePicker country;
    private CountryCodePicker nationality;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        ((AppCompatActivity)getActivity()).getSupportActionBar().show();
        // getActivity().setTheme(R.style.AppTheme_NoDisplay);

        root = inflater.inflate(R.layout.fragment_profile, container, false);
        /*final TextView textView = root.findViewById(R.id.text_slideshow);
        notificationViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });*/

        age = root.findViewById(R.id.age);
        gender = root.findViewById(R.id.gender_text_input);
        country = root.findViewById(R.id.country_text_input);

        country.setShowPhoneCode(false);
        country.setCcpDialogShowPhoneCode(false);
        country.setCcpDialogShowNameCode(false);
        country.showFullName(true);
        country.showNameCode(false);

        save = root.findViewById(R.id.save);
        displayName = root.findViewById(R.id.display_name);
        description = root.findViewById(R.id.description);

        description.setText(getResources().getText(R.string.description_profile));

        save.setOnClickListener(this);

        nationality = root.findViewById(R.id.natuinality_text_input);

        nationality.setShowPhoneCode(false);
        nationality.setCcpDialogShowPhoneCode(false);
        nationality.setCcpDialogShowNameCode(false);
        nationality.showFullName(true);
        nationality.showNameCode(false);

        List<SexItems> sexItems = new ArrayList<SexItems>();
        sexItems.add(new SexItems("Male"));
        sexItems.add(new SexItems("Female"));
        sexItems.add(new SexItems("Other"));
        gender.setItems(sexItems);
        gender.setOnItemSelectedListener(new ClickToSelectEditText.OnItemSelectedListener<SexItems>() {
            @Override
            public void onItemSelectedListener(SexItems item, int selectedIndex) {
                genderName = item.getLabel();
            }
        });

        loadInfo();

        return root;
    }

    private void loadInfo() {
        ParseUser user = ParseUser.getCurrentUser();
        if(user != null) {
            if (user.has("displayName")) {
                displayName.setText(user.getString("displayName"));
            }

            if (user.has("age") && user.get("age") != null && user.get("age") != "") {
                age.setText(user.get("age").toString());
            }
            if (user.has("gender") && user.get("gender") != null && user.get("gender") != "") {
                gender.setText(user.getString("gender"));
                genderName = user.getString("gender");
            }
            if (user.has("countrycode") && user.get("countrycode") != null && user.get("countrycode") != "") {
                country.setCountryForNameCode(user.getString("countrycode"));
            } else {
                country.setAutoDetectedCountry(true);
                //country.setCountryForNameCode("PA");
            }

            if (user.has("nationalitycode") && user.get("nationalitycode") != null && user.get("nationalitycode") != "") {
                nationality.setCountryForNameCode(user.getString("nationalitycode"));
            } else {
                nationality.setAutoDetectedCountry(true);
                //nationality.setCountryForNameCode("PA");
            }
        }
    }

    @Override
    public void onSelectCountry(Country country) {

    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.save) {
            ParseUser user = ParseUser.getCurrentUser();
            if (user.isAuthenticated()) {
                user.put("displayName", displayName.getText().toString());
                user.put("age", Integer.valueOf(age.getText().toString()));
                user.put("gender", genderName);
                user.put("country", country.getSelectedCountryName());
                user.put("countrycode", country.getSelectedCountryNameCode());
                user.put("nationality", nationality.getSelectedCountryName());
                user.put("nationalitycode", nationality.getSelectedCountryNameCode());
                user.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null) {
                            Log.d("Error in save profile: ", e.getMessage());
                            Toast.makeText(getContext(), getResources().getText(R.string.error_saving_user), Toast.LENGTH_LONG);
                            // user.saveEventually();
                        } else {
                            Toast.makeText(getContext(), "User saved", Toast.LENGTH_LONG);
                        }
                    }
                });
            } else {
                Log.d("User not authenticated: ", user.getUsername());
            }

            NavHostFragment.findNavController(this).navigate(R.id.nav_map);
        }
    }
}
