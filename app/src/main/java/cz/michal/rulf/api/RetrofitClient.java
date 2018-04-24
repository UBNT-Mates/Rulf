package cz.michal.rulf.api;

import android.util.Log;

import cz.michal.rulf.Constants;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    public EndpointService endpoint;
    public APIService api;

    public RetrofitClient(OkHttpClient client) {
        final Retrofit.Builder retrofitBuilder = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .client(client);
        api = retrofitBuilder.baseUrl(Constants.API_URL).build().create(APIService.class);
    }

    public RetrofitClient(OkHttpClient client, String endpointUrl) {
        final Retrofit.Builder retrofitBuilder = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .client(client);
        api = retrofitBuilder.baseUrl(Constants.API_URL).build().create(APIService.class);
        endpoint = retrofitBuilder.baseUrl(endpointUrl).build().create(EndpointService.class);
    }
}