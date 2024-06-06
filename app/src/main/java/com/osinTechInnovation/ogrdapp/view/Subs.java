package com.osinTechInnovation.ogrdapp.view;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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
import com.google.common.collect.ImmutableList;
import com.osinTechInnovation.ogrdapp.R;
import com.osinTechInnovation.ogrdapp.utility.ConnectionClass;
import com.osinTechInnovation.ogrdapp.utility.Security;

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


        billingClient = BillingClient.newBuilder(this)
                .enablePendingPurchases()
                .setListener(purchasesUpdatedListener)
                .build();

        getPrice();


        if(ConnectionClass.premium){
            subsStatus.setText("Status: Already Subscribed");
            btnSubscribe.setVisibility(View.GONE);
        }
        else {
            subsStatus.setText("Status: Not Subscribed");
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


                                    }
                                }
                            }
                    );



                }
            });
        });

    }

    private void getPrice() {
        btnGetPrice.setOnClickListener(v->{
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
                                                        dur = " For " + n + " Month ";
                                                    }
                                                    else if(duration.equals("Y")){
                                                        dur = " For " + n + " Year ";
                                                    }
                                                    else if(duration.equals(" WEEK ")){
                                                        dur = " FOR " + n + " Week ";
                                                    } else if (duration.equals("D")) {
                                                        dur = " FOR " + n + " Days ";
                                                    }

                                                } else{
                                                    if(bp.equals("P1M")){
                                                        dur = "/Monthly";
                                                    }
                                                    else if(bp.equals("P6M")){
                                                        dur = "/Every 6 Month";
                                                    }else if(bp.equals("P1Y")){
                                                        dur = "/Yearly";
                                                    }else if(bp.equals("P1W")){
                                                        dur = "/Weekly";
                                                    } else if (bp.equals("P3W")) {
                                                        dur = "Every /3 Week";
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
                                                            dur = "/Monthly";
                                                        }else if(period.equals("P6M")){
                                                            dur = "/Every 6 Month";
                                                        } else if (period.equals("P1Y")) {
                                                            dur = "/Yearly";
                                                        } else if (period.equals("P1W")) {
                                                            dur = "/Weekly";
                                                        } else if (period.equals("P1W")) {
                                                            dur = "/Weekly";
                                                        } else if (period.equals("P3W")) {
                                                            dur = "Every /3 Week";
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

                            subsStatus.setText(subsName);
                            tvGetPrice.setText("Price: " + phases);
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
             if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases !=null){
                 for(Purchase purchase: purchases){
                     handlePurchase(purchase);
                 }
             }
             else if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED){
                 subsStatus.setText("Already Subcribed");
                 isSuccess = true;
                 ConnectionClass.premium = true;
                 ConnectionClass.locked = true;
                 btnSubscribe.setVisibility(View.GONE);
             }
             else if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED){
                 subsStatus.setText("FEATURE_NOT_SUPPORTED");
             }
             else if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.BILLING_UNAVAILABLE){
                 subsStatus.setText("BILLING_UNAVAILABLE");
             }else if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED){
                 subsStatus.setText("USER_CANCELED");
             }else if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.DEVELOPER_ERROR){
                 subsStatus.setText("DEVELOPER_ERROR");
             }else if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.ITEM_UNAVAILABLE){
                 subsStatus.setText("ITEM_UNAVAILABLE");
             }else if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.NETWORK_ERROR){
                 subsStatus.setText("NETWORK_ERROR");
             }else if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.SERVICE_DISCONNECTED){
                 subsStatus.setText("SERVICE_DISCONNECTED");
             }else {
                 Toast.makeText(getApplicationContext(), "Error " + billingResult.getDebugMessage(), Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(getApplicationContext(), "Error : invalid Purchase", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(!purchase.isAcknowledged()){
                    AcknowledgePurchaseParams acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                            .setPurchaseToken(purchase.getPurchaseToken())
                            .build();
                    billingClient.acknowledgePurchase(acknowledgePurchaseParams,acknowledgePurchaseResponseListener);
                    subsStatus.setText("Subscribed");
                    isSuccess = true;
                }else {
                    subsStatus.setText("Already_Subscribed");
                }
                ConnectionClass.premium = true;
                ConnectionClass.locked = false;
                btnSubscribe.setVisibility(View.GONE);
            } else if (purchase.getPurchaseState() == Purchase.PurchaseState.PENDING) {
                subsStatus.setText("Subscription PENDING");
            } else if (purchase.getPurchaseState() == Purchase.PurchaseState.UNSPECIFIED_STATE) {
                subsStatus.setText("UNSPECIFIED_STATE");
            }


    }

    AcknowledgePurchaseResponseListener acknowledgePurchaseResponseListener = new AcknowledgePurchaseResponseListener() {
        @Override
        public void onAcknowledgePurchaseResponse(@NonNull BillingResult billingResult) {
                subsStatus.setText("Subscribed");
                isSuccess = true;
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
}