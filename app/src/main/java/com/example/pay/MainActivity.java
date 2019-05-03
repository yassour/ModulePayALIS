package com.example.pay;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.braintreepayments.api.dropin.DropInActivity;
import com.braintreepayments.api.dropin.DropInRequest;
import com.braintreepayments.api.dropin.DropInResult;
import com.braintreepayments.api.interfaces.HttpResponseCallback;
import com.braintreepayments.api.internal.HttpClient;
import com.braintreepayments.api.models.PaymentMethodNonce;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 1234;
    final String API_Get_Token ="http://10.0.2.2/braintree/main.php";
    final String API_Chek_out ="http://10.0.2.2/braintree/checkout.php";

    String token,amount;
    HashMap<String,String> paramsHash;
    Button Btn_pay;
    EditText edt_amount;
    LinearLayout group_waiting,group_payment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        group_payment= (LinearLayout)findViewById(R.id.payment_group);
        group_waiting= (LinearLayout)findViewById(R.id.waiting_group);
        edt_amount= (EditText) findViewById(R.id.edt_amount);
        Btn_pay= (Button)findViewById(R.id.btn_pay);




        new getToken().execute();

        Btn_pay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitPayment();

            }
        });
    }

    private void submitPayment() {


        DropInRequest dropInRequest = new DropInRequest().clientToken(token);
        startActivityForResult(dropInRequest.getIntent(this),REQUEST_CODE);


    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode== REQUEST_CODE)
        {

            if(resultCode == RESULT_OK)
            {
                DropInResult result =data.getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT);
                PaymentMethodNonce nonce=result.getPaymentMethodNonce();
                String Strnonce =nonce.getNonce();

                if (!edt_amount.getText().toString().isEmpty())
                {
                    amount =edt_amount.getText().toString();
                    paramsHash =new HashMap<>();
                    paramsHash.put("amount",amount);
                    paramsHash.put("nonce",Strnonce);

                    sendPayments();

                }
                else
                {
                    Toast.makeText(this,"Merci d'entrer un montant valid",Toast.LENGTH_SHORT).show();


                }
            }
            else if (resultCode==RESULT_CANCELED)
                Toast.makeText(this,"Annuler l'utilisatur",Toast.LENGTH_SHORT).show();
            else
            {
                Exception error = (Exception) data.getSerializableExtra(DropInActivity.EXTRA_ERROR);
                Log.d("ALIS_Error", error.toString());
            }

        }




    }

    private void sendPayments() {

        RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, API_Chek_out,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        if(response.toString().contains(""))
                            Toast.makeText(MainActivity.this,"Transaction réussie",Toast.LENGTH_SHORT).show();
                        else
                        {
                            Toast.makeText(MainActivity.this,"Transaction échouée",Toast.LENGTH_SHORT).show();
                        }
                        Log.d("ALIS_Error",response.toString());



                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Log.d("ALIS_Error",error.toString());

            }
        })

        {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                if(paramsHash==null)
                    return null;
                Map<String,String> params =new HashMap<>();
                for (String Key:paramsHash.keySet())
                {
                    params.put(Key,paramsHash.get(Key));

                }
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {

                Map<String,String> params= new HashMap<>();
                params.put("ContentType","application/x-www-form-urlencoded");

                return params;
            }
        };

        queue.add(stringRequest);

    }
    private class getToken extends AsyncTask {

        ProgressDialog mDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDialog = new ProgressDialog(MainActivity.this, android.R.style.Theme_DeviceDefault_Dialog);
            mDialog.setCancelable(false);
            mDialog.setMessage("Veuillez patienter !");
            mDialog.show();

        }


        @Override
        protected Object doInBackground(Object[] objects) {

            HttpClient client = new HttpClient();

            client.get(API_Get_Token, new HttpResponseCallback() {
                @Override
                public void success(final String responseBody) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //hide group waiting
                            group_waiting.setVisibility(View.GONE);
                            //show group payment
                            group_payment.setVisibility(View.VISIBLE);

                            //Set token
                            token=responseBody;



                        }
                    });
                }

                @Override
                public void failure(Exception exception) {


                    Log.d("ALIS Error", exception.toString());



                }
            });
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);

            mDialog.dismiss();

        }
    }
}
