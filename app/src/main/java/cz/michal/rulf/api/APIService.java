package cz.michal.rulf.api;

import java.util.List;

import cz.michal.rulf.model.Server;
import cz.michal.rulf.model.Token;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface APIService {
    @POST("v1/tokens")
    Call<Token> getToken();

    @GET("v2/servers")
    Call<List<Server>> getServers(@Header("x-test-token") String token);
}