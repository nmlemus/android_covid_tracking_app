package com.goblob.covid.ui.notification;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.goblob.covid.R;
import com.goblob.covid.dagger.Injectable;
import com.goblob.covid.data.dao.factory.DAOFactory;
import com.goblob.covid.utils.ClickToSelectEditText;
import com.goblob.covid.utils.Countries;
import com.goblob.covid.utils.Country;
import com.goblob.covid.utils.SexItems;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParsePush;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class CreateNotificationFragment extends Fragment implements View.OnClickListener {

    private View root;
    private DAOFactory daoFactory;
    private ClickToSelectEditText channel;
    private String channelName;
    private String messageTitle, messageImageURL;
    private EditText messaggeText, messageURL;
    private Button sendPush;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        ((AppCompatActivity)getActivity()).getSupportActionBar().show();
        // getActivity().setTheme(R.style.AppTheme_NoDisplay);

        root = inflater.inflate(R.layout.fragment_create_notification, container, false);

        channel = root.findViewById(R.id.channel_text_input);
        messaggeText = root.findViewById(R.id.message_text);
        messageURL = root.findViewById(R.id.message_url);
        sendPush = root.findViewById(R.id.send_push);
        sendPush.setOnClickListener(this);


        List<SexItems> sexItems = new ArrayList<SexItems>();
        sexItems.add(new SexItems("MINSA"));
        sexItems.add(new SexItems("Consulado de Francia"));
        sexItems.add(new SexItems("GOV"));
        sexItems.add(new SexItems("Dev"));
        channel.setItems(sexItems);
        channel.setOnItemSelectedListener(new ClickToSelectEditText.OnItemSelectedListener<SexItems>() {
            @Override
            public void onItemSelectedListener(SexItems item, int selectedIndex) {
                channelName = item.getLabel();
            }
        });

        daoFactory = Injectable.get().getDaoFactory();
        return root;


    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.send_push) {
            sendPush();
        }
    }

    private void sendPush() {
        final HashMap<String, String> params = new HashMap<>();
        // Calling the cloud code function
        params.put("channel", channelName);
        params.put("alert", messaggeText.getText().toString());
        params.put("message", messaggeText.getText().toString());
        params.put("messageType", "MINSA");
        params.put("title", channelName);
        params.put("url", messageURL.getText().toString());
        params.put("image", "image");
        params.put("sentAt", new Date().toString());


        ParseCloud.callFunctionInBackground("sendPush", params, new FunctionCallback<Object>() {
            @Override
            public void done(Object response, ParseException exc) {
                if(exc == null) {
                    // The function was executed, but it's interesting to check its response
                    alertDisplayer("Successful Push","Check on your phone the notifications to confirm!");
                }
                else {
                    // Something went wrong
                    Toast.makeText(getActivity(), exc.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void alertDisplayer(String title,String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        AlertDialog ok = builder.create();
        ok.show();
    }
}
