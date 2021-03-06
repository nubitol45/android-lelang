package com.example.lelangonline.ui.login;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.lelangonline.ui.MainActivity;
import com.example.lelangonline.R;
import com.example.lelangonline.ViewModelProviderFactory;
import com.example.lelangonline.data.model.LoggedInUser;
import com.example.lelangonline.data.model.ResponseMember;
import com.example.lelangonline.databinding.ActivityLoginBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;

import javax.inject.Inject;

import dagger.android.support.DaggerAppCompatActivity;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends DaggerAppCompatActivity {

    private LoginViewModel loginViewModel;
    private ActivityLoginBinding binding;

    @Inject
    LoginRepository loginRepository;
    @Inject
    ViewModelProviderFactory providerFactory;
    private Context mContext;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        setUp();

    }

    private void setUp() {
        binding.button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (binding.button3.getText() != "masuk"){
                    binding.button3.setText("masuk");
                    binding.label.setText("DAFTAR");
                    binding.txtUsername.setVisibility(View.VISIBLE);
                }else {
                    binding.button3.setText("daftar");
                    binding.label.setText("MASUK");
                    binding.txtUsername.setVisibility(View.GONE);
                }
            }
        });
        loginViewModel.getUser().observe(this, new Observer<LoggedInUser>() {
            @Override
            public void onChanged(@Nullable LoggedInUser loginUser) {

                if (TextUtils.isEmpty(Objects.requireNonNull(loginUser).getStrEmailAddress())) {
                    binding.txtEmail.setError("Masukkan alamat email");
                    Log.d("TAG","email :"+loginUser.getStrEmailAddress()+" password :"+loginUser.getStrPassword());
//                    binding.txtEmail.requestFocus();
                }
                else if (!loginUser.isEmailValid()) {
                    binding.txtEmail.setError("Masukkan Alamat EMail yang valid");
//                    binding.txtEmail.requestFocus();
                }
                else if (TextUtils.isEmpty(Objects.requireNonNull(loginUser).getStrPassword())) {
                    binding.txtPassword.setError("Masukkan password");
//                    binding.txtPassword.requestFocus();
                }
                else if (!loginUser.isPasswordLengthGreaterThan5()) {
                    binding.txtPassword.setError("Masukkan minimal 6 Digit kata sandi");
//                    binding.txtPassword.requestFocus();
                }
                else {
                    responLogin(loginUser.getStrEmailAddress(), loginUser.getStrPassword());
//                    binding.lblEmailAnswer.setText(loginUser.getStrEmailAddress());
//                    binding.lblPasswordAnswer.setText(loginUser.getStrPassword());
                }
            }
        });

    }

    private void responLogin(String strEmailAddress, String strPassword) {
        Call<ResponseBody> call = loginRepository.requestLogin(strEmailAddress, strPassword);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.isSuccessful()){
                    try {
                        JSONObject jsonRESULTS = new JSONObject(response.body().string());
                        if (jsonRESULTS.getString("success").equals("true")){
                            String id = jsonRESULTS.getJSONObject("data").getString("id");
                            String email = jsonRESULTS.getJSONObject("data").getString("email");
                            String username = jsonRESULTS.getJSONObject("data").getString("username");
                            String memberid = jsonRESULTS.getJSONObject("data").getString("memberId");
                            Toast.makeText(getApplicationContext(), "BERHASIL LOGIN", Toast.LENGTH_SHORT).show();
                            loginViewModel.setPrefUser(id, memberid, email, username);
                            loginRepository.requestMember(Integer.parseInt(memberid));
//                            .enqueue(new Callback<ResponseMember>() {
//                                @Override
//                                public void onResponse(Call<ResponseMember> call, Response<ResponseMember> response) {
//                                    if(response.isSuccessful()){
//                                        try {
//                                            Log.d("TAG","respon: "+response.body().getDataMember());
//                                            Toast.makeText(getApplicationContext(), "respon"+response.body().getDataMember(), Toast.LENGTH_SHORT).show();
//                                            String name = response.body().getDataMember().getName();
//                                            String phone = response.body().getDataMember().getPhoneNumber();
//                                            String status = response.body().getDataMember().getStatus();
//                                            int saldo = response.body().getDataMember().getSaldo().getBalance();
//                                            String avatar = response.body().getDataMember().getAvatar();
//                                            loginRepository.setPrefMember(name, phone, status, saldo, avatar);
//                                        }catch (Exception e){
//                                            e.printStackTrace();
//                                        }
//
//                                    }else {
//                                        Toast.makeText(getApplicationContext(), "error_message", Toast.LENGTH_SHORT).show();
//                                    }
//                                }
//
//                                @Override
//                                public void onFailure(Call<ResponseMember> call, Throwable t) {
//
//                                }
//                            });
                            startActivity(new Intent(getApplication(), MainActivity.class)
                                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
                            finish();
                        } else {
                            String error_message = jsonRESULTS.getString("status");
                            Toast.makeText(getApplicationContext(), error_message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }else {
                    binding.txtEmail.setError("Email mungkin salah");
                    binding.txtPassword.setError("Password mungkin salah");
//                    binding.txtEmail.requestFocus();
//                    binding.txtPassword.requestFocus();
                    try {
                        Toast.makeText(getApplicationContext(),response.errorBody().string(),Toast.LENGTH_SHORT).show(); // this will tell you why your api doesnt work most of time
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(getApplicationContext(),t.toString(),Toast.LENGTH_SHORT).show(); // ALL NETWORK ERROR HERE

            }
        });
    }

    private void initView() {
        loginViewModel = new ViewModelProvider(this, providerFactory).get(LoginViewModel.class);

        binding = DataBindingUtil.setContentView(LoginActivity.this, R.layout.activity_login);

        binding.setLifecycleOwner(this);

        binding.setLoginViewModel(loginViewModel);
    }

}
