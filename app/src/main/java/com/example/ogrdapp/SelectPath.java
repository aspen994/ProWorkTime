package com.example.ogrdapp;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

public class SelectPath {
    private Context context;
    public Button viewById;
    public Button settleForWork;

    public SelectPath(Context context) {
        this.context = context;
    }

    public void buildAlertDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View inflate = layoutInflater.inflate(R.layout.chose_pathway, null);
        viewById = inflate.findViewById(R.id.button_edit);
        settleForWork = inflate.findViewById(R.id.button_settling);
        builder.setView(inflate);
        builder.setTitle("Co chcesz zrobić ?");
        builder.show();
    }


    public Button getViewById() {
        return viewById;
    }

    public Button getSettleForWork() {
        return settleForWork;
    }
}
