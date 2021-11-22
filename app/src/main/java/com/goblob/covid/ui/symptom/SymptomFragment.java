package com.goblob.covid.ui.symptom;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.goblob.covid.R;
import com.goblob.covid.dagger.Injectable;
import com.goblob.covid.data.dao.factory.DAOFactory;
import com.goblob.covid.ui.notification.NotificationViewModel;
import com.goblob.covid.ui.notification.adapter.NotificationsRecyclerViewAdapter;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.yarolegovich.mp.MaterialPreferenceCategory;

import java.util.List;

public class SymptomFragment extends Fragment implements View.OnClickListener {

    private DAOFactory daoFactory;

    private View root;
    private Button fiverButton, congestionButton, cansancioButton, corizaButton, dificuttadRespirarButton, dolorCabezaButton, doloresCuerpoButton, malestarBurtton, tosButton, diarreaButton;
    private Button si, no, noSe, confirmButton;
    private Boolean fiver = false, congestion = false, cansancio = false, coriza = false, dificultadRespirar = false, dolorCabeza = false, doloresCuerpo = false, malestar = false, tos = false, diarrea = false;
    private Boolean siBool = false, noBool = false, noSeBool = false;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        ((AppCompatActivity)getActivity()).getSupportActionBar().show();

        root = inflater.inflate(R.layout.fragment_symptom, container, false);
        /*final TextView textView = root.findViewById(R.id.text_slideshow);
        notificationViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });*/

        // initRecentRecyclerView();
        // loadNotifications();
        fiverButton = root.findViewById(R.id.fiver_button);
        fiverButton.setOnClickListener(this);
        congestionButton = root.findViewById(R.id.congestion_nasal_button);
        congestionButton.setOnClickListener(this);
        cansancioButton = root.findViewById(R.id.cansancio_button);
        cansancioButton.setOnClickListener(this);
        corizaButton = root.findViewById(R.id.coriza_nasal_button);
        corizaButton.setOnClickListener(this);
        dificuttadRespirarButton = root.findViewById(R.id.dificultad_respirar_button);
        dificuttadRespirarButton.setOnClickListener(this);
        dolorCabezaButton = root.findViewById(R.id.dolor_cabeza_button);
        dolorCabezaButton.setOnClickListener(this);
        doloresCuerpoButton = root.findViewById(R.id.dolor_cuerpo_button);
        doloresCuerpoButton.setOnClickListener(this);
        malestarBurtton = root.findViewById(R.id.malestar_genaral_button);
        malestarBurtton.setOnClickListener(this);
        tosButton = root.findViewById(R.id.tos_button);
        tosButton.setOnClickListener(this);
        diarreaButton = root.findViewById(R.id.diarrea_button);
        diarreaButton.setOnClickListener(this);

        si = root.findViewById(R.id.si);
        si.setOnClickListener(this);
        no = root.findViewById(R.id.no);
        no.setOnClickListener(this);
        noSe = root.findViewById(R.id.no_se);
        noSe.setOnClickListener(this);

        confirmButton = root.findViewById(R.id.confirm_button);
        confirmButton.setOnClickListener(this);


        daoFactory = Injectable.get().getDaoFactory();
        return root;
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.fiver_button) {
            fiver = (fiver) ? false : true;
            changeButtonStatus(fiverButton, fiver);
        } else if (id == R.id.congestion_nasal_button) {
            congestion = (congestion) ? false : true;
            changeButtonStatus(congestionButton, congestion);
        } else if (id == R.id.cansancio_button) {
            cansancio = (cansancio) ? false : true;
            changeButtonStatus(cansancioButton, cansancio);
        } else if (id == R.id.coriza_nasal_button) {
            coriza = (coriza) ? false : true;
            changeButtonStatus(corizaButton, coriza);
        } else if (id == R.id.dificultad_respirar_button) {
            dificultadRespirar = (dificultadRespirar) ? false : true;
            changeButtonStatus(dificuttadRespirarButton, dificultadRespirar);
        } else if (id == R.id.dolor_cabeza_button) {
            dolorCabeza = (dolorCabeza) ? false : true;
            changeButtonStatus(dolorCabezaButton, dolorCabeza);
        } else if (id == R.id.dolor_cuerpo_button) {
            doloresCuerpo = (doloresCuerpo) ? false : true;
            changeButtonStatus(doloresCuerpoButton, doloresCuerpo);
        } else if (id == R.id.malestar_genaral_button) {
            malestar = (malestar) ? false : true;
            changeButtonStatus(malestarBurtton, malestar);
        } else if (id == R.id.tos_button) {
            tos = (tos) ? false : true;
            changeButtonStatus(tosButton, tos);
        } else if (id == R.id.diarrea_button) {
            diarrea = (diarrea) ? false : true;
            changeButtonStatus(diarreaButton, diarrea);
        } else if (id == R.id.si) {
            siBool = (siBool) ? false : true;
            changeButtonStatus(si, siBool);
        } else if (id == R.id.no) {
            noBool = (noBool) ? false : true;
            changeButtonStatus(no, noBool);
        } else if (id == R.id.no_se) {
            noSeBool = (noSeBool) ? false : true;
            changeButtonStatus(noSe, noSeBool);
        } else if (id == R.id.confirm_button) {
            saveSymptom();
        }
    }

    private void saveSymptom() {
        final ParseObject symptom = new ParseObject("Symptom");
        symptom.put("fiver", fiver);
        symptom.put("congestion_nasal", congestion);
        symptom.put("cansancio", cansancio);
        symptom.put("coriza", coriza);
        symptom.put("dificultad_respirar", dificultadRespirar);
        symptom.put("dolor_cabeza", dolorCabeza);
        symptom.put("dolores_cuerpo", doloresCuerpo);
        symptom.put("malestar", malestar);
        symptom.put("tos", tos);
        symptom.put("diarrea", diarrea);
        if (siBool) {
            symptom.put("contacto_sospechoso", "si");
        } else if (noBool) {
            symptom.put("contacto_sospechoso", "no");
        } else {
            symptom.put("contacto_sospechoso", "no_se");
        }

        ParseUser user = ParseUser.getCurrentUser();
        symptom.put("user", user);

        symptom.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    symptom.saveEventually(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {

                        }
                    });
                } else {

                }
            }
        });
        NavHostFragment.findNavController(this).navigate(R.id.nav_map);
    }

    private void changeButtonStatus(Button button, Boolean status) {
        if (status) {
            button.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        } else {
            button.setBackgroundColor(getResources().getColor(R.color.light_gray_inactive_icon));
            button.setWidth(0);
        }
    }
}
