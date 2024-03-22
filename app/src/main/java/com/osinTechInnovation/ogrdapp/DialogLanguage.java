package com.osinTechInnovation.ogrdapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import java.util.Locale;

public class DialogLanguage extends AppCompatDialogFragment {
    private Spinner spinner;
    private Activity activity;
    private Context context;

    public DialogLanguage(Context context)
    {
        this.context = context;
        activity= (UserMainActivity)context;

    }
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.layout_dialog, null);

        builder.setView(view)
                .setTitle(getString(R.string.select_language));


        spinner = view.findViewById(R.id.spinner_language2);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),R.array.language, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setPrompt(getString(R.string.select_language));
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedLanguage = parent.getItemAtPosition(position).toString();
                /*
                *   <item>polski</item>
                    <item>українська</item>
                    <item>english</item>
        * */
                Intent intent = new Intent(getContext(), UserMainActivity.class);

                if(selectedLanguage.equals("polski"))
                {
                    setLocal(activity,"pl");
                    activity.finish();
                    activity.startActivity(activity.getIntent());

                }
                else if(selectedLanguage.equals("українська"))
                {
                    setLocal(activity,"uk");
                    activity.finish();
                    activity.startActivity(activity.getIntent());


                } else if (selectedLanguage.equals("english")) {
                    setLocal(activity,"en");
                    activity.finish();
                    activity.startActivity(activity.getIntent());

                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        return builder.create();
    }

    //! ! ! ! ! ! ! ! ! ! ! !-FOR NEXT IMPROVEMNET LEAV IT ! ! ! ! ! ! ! ! ! ! ! !
    private void setLocal(Activity activity, String langCode) {
        Locale locale = new Locale(langCode);
        locale.setDefault(locale);
        Resources resources = activity.getResources();
        Configuration configuration = resources.getConfiguration();
        configuration.setLocale(locale);
        resources.updateConfiguration(configuration,resources.getDisplayMetrics());
        activity.recreate();
    }
}
