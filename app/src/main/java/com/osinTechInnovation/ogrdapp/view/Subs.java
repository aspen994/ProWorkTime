package com.osinTechInnovation.ogrdapp.view;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.ProductDetailsResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchasesParams;
import com.google.common.collect.ImmutableList;
import com.osinTechInnovation.ogrdapp.R;
import com.osinTechInnovation.ogrdapp.UserMainActivity;
import com.osinTechInnovation.ogrdapp.utility.ConnectionClass;
import com.osinTechInnovation.ogrdapp.utility.Security;
import com.osinTechInnovation.ogrdapp.viewmodel.AuthViewModel;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Subs extends AppCompatActivity {

    private BillingClient billingClient;
    String subsName, phases,des,dur;

    TextView subsStatus,titleMonthly,titleNoAds,tvGetPrice;
    Button btnGetPrice,btnSubscribe,btn_quit;

    public static final String PRODUCT_ID = "my_sub_ogrodapp";

    boolean isSuccess = false;
    private AuthViewModel authViewModel;
    boolean flag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subs);
        btnGetPrice = findViewById(R.id.btn_getPrice);
        btnSubscribe = findViewById(R.id.btn_subscribe);
        btn_quit = findViewById(R.id.btn_quit);
        subsStatus = findViewById(R.id.subs_status);
        titleNoAds = findViewById(R.id.title_no_ads);
        tvGetPrice = findViewById(R.id.tv_get_price);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        //Toast.makeText(this, "On create Subs", Toast.LENGTH_SHORT).show();

        billingClient = BillingClient.newBuilder(this)
                .enablePendingPurchases()
                .setListener(purchasesUpdatedListener)
                .build();

        getPrice();
        query_purchase();




        if(ConnectionClass.premium){
            subsStatus.setText(getString(R.string.status_already_subscribed));
            btnSubscribe.setVisibility(View.GONE);
        }
        else {
            subsStatus.setText(getString(R.string.status_not_subscribed));
        }

        quitClick();

        btnSubscribe.setOnClickListener(v->{
            billingClient.startConnection(new BillingClientStateListener() {
                @Override
                public void onBillingServiceDisconnected() {

                }

                @Override
                public void onBillingSetupFinished(@NonNull BillingResult billingResult) {

                    QueryProductDetailsParams queryProductDetailsParams =
                            QueryProductDetailsParams.newBuilder()
                                    .setProductList(
                                            ImmutableList.of(
                                                    QueryProductDetailsParams.Product.newBuilder()
                                                            .setProductId(PRODUCT_ID)
                                                            .setProductType(BillingClient.ProductType.SUBS)
                                                            .build()))
                                    .build();

                    billingClient.queryProductDetailsAsync(
                            queryProductDetailsParams,
                            new ProductDetailsResponseListener() {
                                public void onProductDetailsResponse(BillingResult billingResult,
                                                                     List<ProductDetails> productDetailsList) {
                                    for(ProductDetails productDetails: productDetailsList){

                                        String offerToken = productDetails.getSubscriptionOfferDetails()
                                                .get(0).getOfferToken();
                                        ImmutableList productDetailsParamsList =
                                                ImmutableList.of(
                                                        BillingFlowParams.ProductDetailsParams.newBuilder()
                                                                .setProductDetails(productDetails)
                                                                .setOfferToken(offerToken)
                                                                .build()
                                                );

                                        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                                                .setProductDetailsParamsList(productDetailsParamsList)
                                                .build();


                                        billingClient.launchBillingFlow(Subs.this, billingFlowParams);

                                        Log.i("Invoked only once ?","Invoked only once ?");

                                    }
                                }
                            }
                    );



                }
            });
        });

    }

    private void query_purchase() {
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                Log.i("enter BSF",billingResult+"");
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    ExecutorService executorService = Executors.newSingleThreadExecutor();
                    executorService.execute(() -> {
                        try {

                            billingClient.queryPurchasesAsync(
                                    QueryPurchasesParams.newBuilder()
                                            .setProductType(BillingClient.ProductType.SUBS)
                                            .build(),
                                    (billingResult1, purchaseList) -> {

                                        for (Purchase purchase : purchaseList) {
                                            if (purchase != null && purchase.isAcknowledged()) {
                                                ConnectionClass.premium = true;
                                                    subsStatus.setText(getString(R.string.status_already_subscribed));
                                                    btnSubscribe.setVisibility(View.GONE);
                                                    Log.i("Access Granted in BSF","Acces Granted in BSF");
                                                    flag=true;
                                            }
                                            else{
                                                Log.i("Access Denied in BSF 78","Acces Denied in BSF 78");
                                            }
                                        }

                                    }

                            );
                            Log.i("try in block","enter BillingSetupFinisehd");
                            if(!flag){
                                ConnectionClass.premium = false;
                                Log.i("Access Denied in BSF 79","Acces Denied in BSF 79");
                            }


                        } catch (Exception e) {
                            // TODO 24.06.24
                            ConnectionClass.premium = false;
                            subsStatus.setText(getString(R.string.status_not_subscribed));
                            btnSubscribe.setVisibility(View.VISIBLE);
                            Log.i("catch in block","enter BillingSetupFinisehd");
                            Log.i("Access Denied in BSF","Acces Denied in BSF");
                        }

                        runOnUiThread(() -> {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }


                        });
                    });
                }
            }

            @Override
            public void onBillingServiceDisconnected() {

            }
        });

    }

    private void getPrice() {

        btnGetPrice.setOnClickListener(v->{
            Log.i("btnGetPrice","btnGetPriceClicked");
            billingClient.startConnection(new BillingClientStateListener() {
                @Override
                public void onBillingSetupFinished(BillingResult billingResult) {
                    if (billingResult.getResponseCode() ==  BillingClient.BillingResponseCode.OK) {
                        ExecutorService executorService = Executors.newSingleThreadExecutor();
                        executorService.execute(()->{

                            QueryProductDetailsParams queryProductDetailsParams =
                                    QueryProductDetailsParams.newBuilder()
                                            .setProductList(
                                                    ImmutableList.of(
                                                            QueryProductDetailsParams.Product.newBuilder()
                                                                    .setProductId(PRODUCT_ID)
                                                                    .setProductType(BillingClient.ProductType.SUBS)
                                                                    .build()))
                                            .build();

                            billingClient.queryProductDetailsAsync(
                                    queryProductDetailsParams,
                                    new ProductDetailsResponseListener() {
                                        public void onProductDetailsResponse(BillingResult billingResult,
                                                                             List<ProductDetails> productDetailsList) {
                                            for(ProductDetails productDetails: productDetailsList){
                                                String offerToken = productDetails
                                                        .getSubscriptionOfferDetails()
                                                        .get(0)
                                                        .getOfferToken();

                                                ImmutableList<BillingFlowParams.ProductDetailsParams> productDetailsParamsList =
                                                        ImmutableList.of(
                                                                BillingFlowParams.ProductDetailsParams.newBuilder()
                                                                        // retrieve a value for "productDetails" by calling queryProductDetailsAsync()
                                                                        .setProductDetails(productDetails)
                                                                        // For one-time products, "setOfferToken" method shouldn't be called.
                                                                        // For subscriptions, to get an offer token, call
                                                                        // ProductDetails.subscriptionOfferDetails() for a list of offers
                                                                        // that are available to the user.
                                                                        .setOfferToken(offerToken)
                                                                        .build()
                                                        );

                                                subsName = productDetails.getName();
                                                des = productDetails.getDescription();
                                                String formattedPrice = productDetails.getSubscriptionOfferDetails().get(0).getPricingPhases()
                                                        .getPricingPhaseList().get(0).getFormattedPrice();
                                                String billingPeriod = productDetails.getSubscriptionOfferDetails().get(0).getPricingPhases()
                                                        .getPricingPhaseList().get(0).getBillingPeriod();
                                                int recurrenceMode = productDetails.getSubscriptionOfferDetails().get(0).getPricingPhases()
                                                        .getPricingPhaseList().get(0).getRecurrenceMode();

                                                String n,duration,bp;
                                                bp = billingPeriod;
                                                n = billingPeriod.substring(1,2);
                                                duration = billingPeriod.substring(2,3);
                                                if(recurrenceMode ==2){
                                                    if(duration.equals("M")){
                                                        dur = getString(R.string.for_word) + n + getString(R.string.month);

                                                    }
                                                    else if(duration.equals("Y")){
                                                        dur = getString(R.string.for_word) + n + getString(R.string.year);

                                                    }
                                                    else if(duration.equals(" WEEK ")){
                                                        dur = getString(R.string.for_word) + n + getString(R.string.week);

                                                    } else if (duration.equals("D")) {
                                                        dur = getString(R.string.for_word) + n + getString(R.string.days);
                                                    }

                                                } else{
                                                    if(bp.equals("P1M")){
                                                        dur = getString(R.string.monthly);
                                                    }
                                                    else if(bp.equals("P6M")){
                                                        dur = getString(R.string.every_6_month);
                                                        dur = getString(R.string.every_6_month);
                                                    }else if(bp.equals("P1Y")){
                                                        dur = getString(R.string.yearly);
                                                        dur = getString(R.string.yearly);
                                                    }else if(bp.equals("P1W")){
                                                        dur = getString(R.string.weekly);
                                                        dur = getString(R.string.weekly);
                                                    } else if (bp.equals("P3W")) {
                                                        dur = getString(R.string.every_3_week);
                                                        dur = getString(R.string.every_3_week);
                                                    }
                                                }
                                                phases = formattedPrice + " " + dur;

                                                for(int i = 0; i <= (productDetails.getSubscriptionOfferDetails().get(0).getPricingPhases().getPricingPhaseList().size());i++){
                                                    if(i>0) {
                                                        String period = productDetails.getSubscriptionOfferDetails().get(0)
                                                                .getPricingPhases().getPricingPhaseList().get(0).getBillingPeriod();
                                                        String price = productDetails.getSubscriptionOfferDetails().get(0)
                                                                .getPricingPhases().getPricingPhaseList().get(0).getFormattedPrice();

                                                        if(period.equals("P1M")){
                                                            dur = getString(R.string.monthly);

                                                        }else if(period.equals("P6M")){
                                                            dur = getString(R.string.every_6_month);
                                                        } else if (period.equals("P1Y")) {
                                                            dur = getString(R.string.yearly);
                                                        } else if (period.equals("P1W")) {
                                                            dur = getString(R.string.weekly);
                                                        } else if (period.equals("P1W")) {
                                                            dur = getString(R.string.weekly);
                                                        } else if (period.equals("P3W")) {
                                                            dur = getString(R.string.every_3_week);
                                                        }

                                                        phases += "\n" + price+dur;

                                                    }
                                                }

                                            }
                                        }
                                    }
                            );


                        });

                        runOnUiThread(()->{
                            try{
                                Thread.sleep(1000);
                            }
                            catch (InterruptedException e){
                                e.printStackTrace();
                            }

                            //subsStatus.setText(subsName);
                            tvGetPrice.setText(getString(R.string.price) + phases);
                            titleNoAds.setText(des);

                        });

                    }
                }
                @Override
                public void onBillingServiceDisconnected() {

                }
            });
        });
    }



    private PurchasesUpdatedListener purchasesUpdatedListener = new PurchasesUpdatedListener() {
        @Override
        public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> purchases) {

            Log.i("Pre checkpoint",billingResult.getResponseCode()+"");

             if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases !=null){
                 for(Purchase purchase: purchases){
                     handlePurchase(purchase);

                     Log.i("First checkpoint","First checkpoint");
                    /* authViewModel.isSubscribtionAlreadyExist(purchase.getOrderId()).observe(Subs.this, new Observer<String>() {
                         @Override
                         public void onChanged(String email) {
                             //Log.i("Powinno hulać","Powinno hulać"+ aBoolean);
                             if(aBoolean==false){

                                 subsStatus.setText("Already Subcribed");
                                 isSuccess = true;
                                 ConnectionClass.premium = true;
                                 ConnectionClass.locked = true;
                                 btnSubscribe.setVisibility(View.GONE);
                             }
                         }
                     });
*/

                 }
             }
             else if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED){
                 Log.i("SUBS CLASS FUN","ITEM_ALREADY_OWNED");

                 subsStatus.setText(R.string.already_subscribed);
                 isSuccess = true;
                 ConnectionClass.premium = true;
                 ConnectionClass.locked = true;
                 btnSubscribe.setVisibility(View.GONE);
                 Log.i("Status of subs1",ConnectionClass.premium+"");

                 for(Purchase purchase: purchases){
                     handlePurchase(purchase);
                     Log.i("What is there, Subs", purchase.getOriginalJson());


                     Log.i("Second checkpoint","Second checkpoint");

                  /*   authViewModel.isSubscribtionAlreadyExist(purchase.getOrderId()).observe(Subs.this, new Observer<String>() {
                         @Override
                         public void onChanged(String email) {
                          //   Log.i("Powinno hulać","Powinno hulać"+ aBoolean);
                            if(aBoolean==false){

                                subsStatus.setText("Already Subcribed");
                                isSuccess = true;
                                ConnectionClass.premium = true;
                                ConnectionClass.locked = true;
                                btnSubscribe.setVisibility(View.GONE);
                            }
                         }
                     });*/
                 }
             }
             else if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED){
                 subsStatus.setText(getString(R.string.feature_not_supported));
             }
             else if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.BILLING_UNAVAILABLE){
                 subsStatus.setText(getString(R.string.billing_unavailable));
             }else if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED){
                 subsStatus.setText(getString(R.string.user_canceled));
             }else if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.DEVELOPER_ERROR){
                 subsStatus.setText(getString(R.string.developer_error));
             }else if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.ITEM_UNAVAILABLE){
                 subsStatus.setText(getString(R.string.item_unavailable));
             }else if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.NETWORK_ERROR){
                 subsStatus.setText(getString(R.string.network_error));
             }else if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.SERVICE_DISCONNECTED){
                 subsStatus.setText(getString(R.string.service_disconnected));
             }else if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.ITEM_NOT_OWNED){
                 ConnectionClass.premium = false;
                 Log.i("Item not owned","Item not owned");
             }
             else {
                 Toast.makeText(getApplicationContext(), getString(R.string.error) + billingResult.getDebugMessage(), Toast.LENGTH_SHORT).show();
             }
        }

    };

    public void handlePurchase(final Purchase purchase){

            ConsumeParams consumeParams =
                    ConsumeParams.newBuilder()
                            .setPurchaseToken(purchase.getPurchaseToken())
                            .build();

            ConsumeResponseListener listener =(billingResult, s)->{
                if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK){

                }
            };

            billingClient.consumeAsync(consumeParams, listener);

            if(purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED){
                if(!verifyValidSignature(purchase.getOriginalJson(),purchase.getSignature())){
                    Toast.makeText(getApplicationContext(), getString(R.string.error_invalid_purchase), Toast.LENGTH_SHORT).show();
                    return;
                }
                if(!purchase.isAcknowledged()){
                    AcknowledgePurchaseParams acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                            .setPurchaseToken(purchase.getPurchaseToken())
                            .build();
                    billingClient.acknowledgePurchase(acknowledgePurchaseParams,acknowledgePurchaseResponseListener);
                    subsStatus.setText(getString(R.string.subscribed));
                    isSuccess = true;
                }else {
                    subsStatus.setText(getString(R.string.already_subscribed));
                }

             /*   ConnectionClass.premium = true;
                ConnectionClass.locked = false;
                btnSubscribe.setVisibility(View.GONE);

                */

                Log.i("Status of subs2",ConnectionClass.premium+"");

                Log.i("Third checkpoint","Third checkpoint");
               /* authViewModel.isSubscribtionAlreadyExist(purchase.getOrderId()).observe(Subs.this, new Observer<String>() {
                    @Override
                    public void onChanged(String email) {
                       *//* Log.i("Powinno hulać","Powinno hulać" + aBoolean);
                        if(aBoolean){

                            ConnectionClass.premium = true;
                            ConnectionClass.locked = false;
                            btnSubscribe.setVisibility(View.GONE);
                        }*//*
                    }
                });*/

            } else if (purchase.getPurchaseState() == Purchase.PurchaseState.PENDING) {
                subsStatus.setText(getString(R.string.subscription_pending));
            } else if (purchase.getPurchaseState() == Purchase.PurchaseState.UNSPECIFIED_STATE) {
                subsStatus.setText(getString(R.string.unspecified_state));
            }



//billingClient.startConnection();
    }

    AcknowledgePurchaseResponseListener acknowledgePurchaseResponseListener = new AcknowledgePurchaseResponseListener() {
        @Override
        public void onAcknowledgePurchaseResponse(@NonNull BillingResult billingResult) {
                subsStatus.setText(getString(R.string.subscribed));
                isSuccess = true;
                Log.i("getResponseCode",billingResult.getResponseCode()+"");
                ConnectionClass.premium = true;
                ConnectionClass.locked =false;

        }
    };

    private boolean verifyValidSignature(String signedData, String signature){
        try{
            String base64KEy = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAiqv34z7ISQfy2cvNToL/SDNhiEGl8jh4DGPHNxMl0IbIH4+O8mOYwkJEu2gkAj6sa/Orbm4yRGWHCMy+XmebAMM5GX2xWlCuS/Xl3tg42LM6J4LOo86sUZ8bGyNK01kgRKMCZLzn8aCT0SKB/sfk8YrgazIwEXT6MI8t1dGR/SAlrLlr4awKYZgVfCiF7B0Z5BuNcu20ujXtX2+Oc5OhFTejxEpIBQG68MNJ0cUAygOVlIQt32N/KfPz4EV5zvKB5e5hKwQwUfdEk0quL+XmvqojCS22u3Ef2etqgXdb0QeX4WBhdoydbY1fB/sm01z+UMwU9ZMTIIgzkmYXdjxuAwIDAQAB";
            return Security.verifyPurchase(base64KEy,signedData,signature);
        }catch(IOException e){
            return false;
        }
    }

    public void quitClick(){
        btn_quit.setOnClickListener(v->{
        finish();
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(billingClient!=null){
            billingClient.endConnection();
        }
    }

    @Override
    protected void onResume() {
        query_purchase();
        //Toast.makeText(this, "On Resume", Toast.LENGTH_SHORT).show();
        super.onResume();
    }
}