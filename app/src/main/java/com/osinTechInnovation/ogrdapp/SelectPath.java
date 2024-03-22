package com.osinTechInnovation.ogrdapp;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

public class SelectPath {
    private Context context;
    public Button viewById;
    public Button settleForWork;
    private AlertDialog.Builder builder;

    public SelectPath(Context context) {
        this.context = context;
    }

    public void buildAlertDialog()
    {
        builder = new AlertDialog.Builder(context);
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View inflate = layoutInflater.inflate(R.layout.chose_pathway, null);
        viewById = inflate.findViewById(R.id.button_edit);
        settleForWork = inflate.findViewById(R.id.button_settling);
        builder.setView(inflate);
        builder.setTitle("Co chcesz zrobić ?");
        builderShow();
    }

    private AlertDialog builderShow()
    {
        return  builder.show();
    }

    public void dismissBuildAlertDialog()
    {
     builderShow().cancel();
    }


    public Button getViewById() {
        return viewById;
    }

    public Button getSettleForWork() {
        return settleForWork;
    }
}
