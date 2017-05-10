package net.henriqueof.stb.services;

import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Url;

/**
 * Created by Carlos Henrique on 21/12/2016.
 */

public class StbService {
    private final static String TAG = "StbService";

    public enum State { DISCONNECTED, CONNECTING, CONNECTED, LOADED, ERROR }

    private StateListener stateListener;
    private State currentState;

    private StalkerService.Profile profile; // Useless?

    private String server;      // Server provided in settings
    private String baseUrl;
    private String portalUrl;   // Resolved server URL
    private String endpoint;
    private String mac;
    private String serialNumber;
    private String timezone;
    private String token;

    private String lastError;

    private StalkerService stalkerService;
    private Connector connector;
    private LinkedHashSet<String> cookies;

    private List<StalkerService.Channel> channelList;

    private StbService(Builder builder) {
        this.server = builder.server;
        this.mac = builder.mac;
        this.serialNumber = builder.serial;
        this.timezone = builder.timezone;
        this.stateListener = builder.stateListener;

        connector = new Connector();

        currentState = State.DISCONNECTED;
    }

    public void connect() {
        Log.d(TAG, "Connect");

        Log.d(TAG, "onStateChange: CONNECTING");
        currentState = State.CONNECTING;

        if (stateListener != null)
            stateListener.onStateChange(currentState);

        if (connector.getStatus() == AsyncTask.Status.RUNNING)
            connector.cancel(true);

        connector.execute(server);
    }

    public void watchdog() {
        Log.d(TAG, "watchdog");
        if (stalkerService != null) {
            stalkerService.watchdog(endpoint, "Bearer " + token, portalUrl, "tv").enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {

                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {

                }
            });
        }
    }

    public List<StalkerService.Channel> getChannelList() {
        return channelList;
    }

    private void loadProfile() {
        Log.d(TAG, "loadProfile");

        String ver = "ImageDescription: 218; ImageDate: Fri Jan 15 15:20:44 EET 2016; PORTAL version: 5.0.3; API Version: JS API version: 328; STB API version: 134; Player Engine version: 0x566";

        stalkerService.getProfile(endpoint, "Bearer " + token, portalUrl, "MAG250", "3442be39a85f21e4", ver, "0.2.18-r14-pub-250", "1.7-BD-00").enqueue(new Callback<StalkerService.Response<StalkerService.Profile>>() {
            @Override
            public void onResponse(Call<StalkerService.Response<StalkerService.Profile>> call, Response<StalkerService.Response<StalkerService.Profile>> response) {

                Log.d(TAG, "onStateChange: CONNECTED");

                profile = response.body().js;

                currentState = State.CONNECTED;
                if (stateListener != null)
                    stateListener.onStateChange(currentState);

                // Load all channels
                loadChannels();
            }

            @Override
            public void onFailure(Call<StalkerService.Response<StalkerService.Profile>> call, Throwable t) {
                Log.e(TAG, "onStateChange: ERROR");
                t.printStackTrace();

                currentState = State.ERROR;
                if (stateListener != null)
                    stateListener.onStateChange(currentState);
            }
        });
    }

    private void loadChannels() {
        Log.d(TAG, "loadChannels");

        stalkerService.getAllChannels(endpoint, "Bearer " + token, portalUrl).enqueue(new Callback<StalkerService.Response<StalkerService.Channels>>() {
            @Override
            public void onResponse(Call<StalkerService.Response<StalkerService.Channels>> call, Response<StalkerService.Response<StalkerService.Channels>> response) {

                Log.d(TAG, "onStateChange: LOADED");
                channelList = Arrays.asList(response.body().js.data);

                currentState = State.LOADED;
                if (stateListener != null)
                    stateListener.onStateChange(currentState);
            }

            @Override
            public void onFailure(Call<StalkerService.Response<StalkerService.Channels>> call, Throwable t) {
                Log.e(TAG, "onStateChange: ERROR");
                t.printStackTrace();

                currentState = State.ERROR;
                if (stateListener != null)
                    stateListener.onStateChange(currentState);
            }
        });
    }

    public StalkerService.Profile getProfile() {
        return profile;
    }

    /**
     * Live TV methods
     */
    public void getAllChannels(final Request<StalkerService.Channel[]> request) {
        Log.d(TAG, "getAllChannels");

        stalkerService.getAllChannels(endpoint, "Bearer " + token, portalUrl).enqueue(new Callback<StalkerService.Response<StalkerService.Channels>>() {
            @Override
            public void onResponse(Call<StalkerService.Response<StalkerService.Channels>> call, Response<StalkerService.Response<StalkerService.Channels>> response) {
                Log.d(TAG, "getAllChannels: Loaded " + response.body().js.total_items + " channels");
                request.done(response.body().js.data); // TODO

                currentState = State.LOADED;
                if (stateListener != null)
                    stateListener.onStateChange(currentState);
            }

            @Override
            public void onFailure(Call<StalkerService.Response<StalkerService.Channels>> call, Throwable t) {
                Log.e(TAG, "getAllChannels: Error loading channels");
                request.error();
                t.printStackTrace();
            }
        });
    }

    public void getAllFavChannels(final Request<StalkerService.Channel[]> request) {
        Log.d(TAG, "getAllFavChannels");

        stalkerService.getAllFavChannels(endpoint, "Bearer " + token, portalUrl).enqueue(new Callback<StalkerService.Response<StalkerService.Channels>>() {
            @Override
            public void onResponse(Call<StalkerService.Response<StalkerService.Channels>> call, Response<StalkerService.Response<StalkerService.Channels>> response) {
                Log.d(TAG, "itvGetGenres: Loaded " + response.body().js.total_items + " genres");
                request.done(response.body().js.data);
            }

            @Override
            public void onFailure(Call<StalkerService.Response<StalkerService.Channels>> call, Throwable t) {
                Log.e(TAG, "getAllFavChannels: Error loading channels");
                request.error();
                t.printStackTrace();
            }
        });
    }

    public void getFavIds(final Request<String[]> request) {
        stalkerService.getFavIds(endpoint, "Bearer " + token, portalUrl).enqueue(new Callback<StalkerService.Response<String[]>>() {
            @Override
            public void onResponse(Call<StalkerService.Response<String[]>> call, Response<StalkerService.Response<String[]>> response) {
                request.done(response.body().js);
            }

            @Override
            public void onFailure(Call<StalkerService.Response<String[]>> call, Throwable t) {
                request.error();
            }
        });
    }

    public void itvGetGenres(final Request<List<StalkerService.Genre>> request) {
        Log.d(TAG, "getAllChannels");

        stalkerService.getGenres(endpoint, "Bearer " + token, portalUrl).enqueue(new Callback<StalkerService.Response<List<StalkerService.Genre>>>() {
            @Override
            public void onResponse(Call<StalkerService.Response<List<StalkerService.Genre>>> call, Response<StalkerService.Response<List<StalkerService.Genre>>> response) {
                Log.d(TAG, "itvGetGenres: Loaded " + response.body().js.size() + " genres");
                request.done(response.body().js);
            }

            @Override
            public void onFailure(Call<StalkerService.Response<List<StalkerService.Genre>>> call, Throwable t) {
                Log.e(TAG, "itvGetGenres: Error loading genres");
                request.error();
                t.printStackTrace();
            }
        });
    }

    public void itvSetLastId(final int channelId) {
        Log.d(TAG, "setLastId");

        stalkerService.itvSetLastId(endpoint, "Bearer " + token, portalUrl, channelId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                Log.d(TAG, "setLastId: Last channel id: " + channelId);
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "itvSetLastId: Error");
            }
        });
    }

    public void itvSetFav(String channels) {
        Log.d(TAG, "itvSetFav");

        stalkerService.itvSetFav(endpoint, "Bearer " + token, portalUrl, channels, "1-xml").enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {

            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {

            }
        });
    }

    public void itvGetOrderedList(final String category, final int currentPage, final Request<StalkerService.Channel[]> request) {
        Log.d(TAG, "vodGetOrderedList");

        stalkerService.itvGetOrderedList(endpoint, "Bearer " + token, portalUrl, category, currentPage, null).enqueue(new Callback<StalkerService.Response<StalkerService.Channels>>() {
            @Override
            public void onResponse(Call<StalkerService.Response<StalkerService.Channels>> call, Response<StalkerService.Response<StalkerService.Channels>> response) {
                request.done(response.body().js.data);
            }

            @Override
            public void onFailure(Call<StalkerService.Response<StalkerService.Channels>> call, Throwable t) {
                request.error();
            }
        });
    }

    public void itvCreateLink(String cmd, final Request<StalkerService.Stream> request) {
        Log.d(TAG, "itvCreateLink");

        stalkerService.itvCreateLink(endpoint, "Bearer " + token, portalUrl, cmd).enqueue(new Callback<StalkerService.Response<StalkerService.Stream>>() {
            @Override
            public void onResponse(Call<StalkerService.Response<StalkerService.Stream>> call, Response<StalkerService.Response<StalkerService.Stream>> response) {
                request.done(response.body().js);
            }

            @Override
            public void onFailure(Call<StalkerService.Response<StalkerService.Stream>> call, Throwable t) {
                request.error();
                t.printStackTrace();
            }
        });
    }

    public void itvGetEpgInfo(final Request<Map<String, StalkerService.EpgItem[]>> request) {
        Log.d(TAG, "itvGetEpgInfo");

        stalkerService.getEpgInfo(endpoint, "Bearer " + token, portalUrl).enqueue(new Callback<StalkerService.Response<StalkerService.Epg>>() {
            @Override
            public void onResponse(Call<StalkerService.Response<StalkerService.Epg>> call, Response<StalkerService.Response<StalkerService.Epg>> response) {
                request.done(response.body().js.data);
            }

            @Override
            public void onFailure(Call<StalkerService.Response<StalkerService.Epg>> call, Throwable t) {
                request.error();
                t.printStackTrace();
            }
        });
    }

    public void  itvGetShortEpg(int channelId, final Request<StalkerService.EpgItem[]> request) {
        stalkerService.getShortEpg(endpoint, "Bearer " + token, portalUrl, channelId).enqueue(new Callback<StalkerService.Response<StalkerService.EpgItem[]>>() {
            @Override
            public void onResponse(Call<StalkerService.Response<StalkerService.EpgItem[]>> call, Response<StalkerService.Response<StalkerService.EpgItem[]>> response) {
                request.done(response.body().js);
            }

            @Override
            public void onFailure(Call<StalkerService.Response<StalkerService.EpgItem[]>> call, Throwable t) {
                t.printStackTrace();
                request.error();
            }
        });
    }

    /**
     * VOD methods
     */
    public void vodGetCategories(final Request<StalkerService.Category[]> request) {
        Log.d(TAG, "vodGetCategories");

        stalkerService.vodGetCategories(endpoint, "Bearer " + token, portalUrl).enqueue(new Callback<StalkerService.Response<StalkerService.Category[]>>() {
            @Override
            public void onResponse(Call<StalkerService.Response<StalkerService.Category[]>> call, Response<StalkerService.Response<StalkerService.Category[]>> response) {
                request.done(response.body().js);
            }

            @Override
            public void onFailure(Call<StalkerService.Response<StalkerService.Category[]>> call, Throwable t) {
                request.error();
            }
        });
    }

    public void vodGetOrderedList(String category, int currentPage, String sortBy, final Request<StalkerService.Vod[]> request) {
        Log.d(TAG, "vodGetOrderedList");

        stalkerService.vodGetOrderedList(endpoint, "Bearer " + token, portalUrl, category, currentPage, sortBy).enqueue(new Callback<StalkerService.Response<StalkerService.VodList>>() {
            @Override
            public void onResponse(Call<StalkerService.Response<StalkerService.VodList>> call, Response<StalkerService.Response<StalkerService.VodList>> response) {
                request.done(response.body().js.data);
            }

            @Override
            public void onFailure(Call<StalkerService.Response<StalkerService.VodList>> call, Throwable t) {
                request.error();
            }
        });
    }

    public void vodCreateLink(String cmd, final Request<StalkerService.Stream> request) {
        Log.d(TAG, "vodCreateLink");

        stalkerService.vodCreateLink(endpoint, "Bearer " + token, portalUrl, cmd).enqueue(new Callback<StalkerService.Response<StalkerService.Stream>>() {
            @Override
            public void onResponse(Call<StalkerService.Response<StalkerService.Stream>> call, Response<StalkerService.Response<StalkerService.Stream>> response) {
                request.done(response.body().js);
            }

            @Override
            public void onFailure(Call<StalkerService.Response<StalkerService.Stream>> call, Throwable t) {
                request.error();
                t.printStackTrace();
            }
        });
    }

    public interface Request<T> {
        void done(T response);
        void error();
    }

    public String getServer() {
        return baseUrl;
    }

    public interface StateListener {
        void onStateChange(State newState);
    }

    /**
     * Other methods
     */
    public static class Builder {
        private final String server;
        private final String mac;
        private String username;
        private String password;
        private String serial;
        private String timezone;
        private StateListener stateListener;

        public Builder(String server, String mac, StateListener stateListener) {
            this.server = server;
            this.mac = mac;
            this.stateListener = stateListener;
        }

        public Builder user(String username, String password) {
            this.username = username;
            this.password = password;

            return this;
        }

        public Builder serial(String serial) {
            this.serial = serial;

            return this;
        }

        public Builder timezone(String timezone) {
            this.timezone = timezone;

            return this;
        }

        public StbService build() {
            return new StbService(this);
        }
    }

    public String getLastError() {
        return lastError;
    }

    private class CookieInterceptor implements Interceptor {

        LinkedHashSet<String> cookies;

        CookieInterceptor(@NonNull LinkedHashSet<String> cookies) {
            this.cookies = cookies;
        }

        @Override
        public okhttp3.Response intercept(Chain chain) throws IOException {

            okhttp3.Request request = chain.request();
            okhttp3.Request.Builder builder = request.newBuilder();

            String cookieList = "";

            Iterator iterator = cookies.iterator();
            while (iterator.hasNext()) {
                if (cookieList.isEmpty())
                    cookieList = iterator.next().toString();
                else
                    cookieList = iterator.next() +"; "+ cookieList;
            }

            builder.addHeader("Cookie", cookieList);

            okhttp3.Response networkResponse = chain.proceed(builder.build());

            // Intercept Set-Cookie headers
            if (!networkResponse.headers("Set-Cookie").isEmpty()) {

                for (String header : networkResponse.headers("Set-Cookie")) {
                    String cookie = header.substring(0, header.indexOf(";"));
                    cookies.add(cookie);
                }
            }

            return networkResponse;
        }
    }

    private class Connector extends AsyncTask<String, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            if (params.length < 1)
                return false;

            // Check for valid URL and set referrer and endpoint addresses
            String urlString = params[0];

            // Set connection protocol
            if (!urlString.startsWith("http://"))
                urlString = "http://" + urlString;

            portalUrl = getRedirect(urlString);

            try {
                URL url = new URL(portalUrl);
                baseUrl = "http://" + url.getHost() + ((url.getPort() != -1) ? ":" + String.valueOf(url.getPort()) : "") + "/";
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            endpoint = "stalker_portal/server/load.php";

            if (!checkUrl(baseUrl + endpoint)) {
                endpoint = "stalker_portal/portal.php";

                if (!checkUrl(baseUrl + endpoint)) {
                    endpoint = "portal.php";

                    if (!checkUrl(baseUrl + endpoint)) {
                        Log.e(TAG, "Cant't find stalker API endpoint");
                        return false;
                    }
                }
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);

            if (!aBoolean) {
                Log.e(TAG, "onStateChange: ERROR");

                currentState = State.ERROR;

                lastError = "Invalid URL supplied";
                Log.e(TAG, lastError);

                if (stateListener != null)
                    stateListener.onStateChange(currentState);
                return;
            }

            Log.d(TAG, "Portal URL: " + portalUrl);
            Log.d(TAG, "Endpoint: " + endpoint);

            // ...
            cookies = new LinkedHashSet<>();
            cookies.add("timezone=" + timezone);
            cookies.add("stb_lang=en");
            cookies.add("mac=" + mac);

            // Create server API interface
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .client(getClient())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            stalkerService = retrofit.create(StalkerService.class);

            // Do authentication and retrieve token
            stalkerService.Authenticate(endpoint, null).enqueue(new Callback<StalkerService.Response<StalkerService.Token>>() {
                @Override
                public void onResponse(Call<StalkerService.Response<StalkerService.Token>> call, Response<StalkerService.Response<StalkerService.Token>> response) {
                    if (response.code() == 200) {

                        token = response.body().js.token;
                        Log.d(TAG, "Authenticate: " + token);

                        // After auth get STB profile
                        loadProfile();
                    }
                }

                @Override
                public void onFailure(Call<StalkerService.Response<StalkerService.Token>> call, Throwable t) {
                    t.printStackTrace();
                }
            });
        }
    }

    private boolean checkUrl(String Url) {

        Log.d(TAG, "Testing URL: " + Url);

        try {
            HttpURLConnection apiEndpoint = (HttpURLConnection) new URL(Url).openConnection();
            apiEndpoint.setRequestMethod("GET");
            apiEndpoint.setConnectTimeout(5000);
            apiEndpoint.setInstanceFollowRedirects(false);
            apiEndpoint.connect();

            if (apiEndpoint.getResponseCode() == HttpURLConnection.HTTP_OK) {
                apiEndpoint.disconnect();
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    private String getRedirect(String baseUrl) {
        Log.d(TAG, "getRedirect: " + baseUrl);
        String location = baseUrl;

        try {
            Document doc = Jsoup.connect(baseUrl).get();
            location = doc.location();

            Element head = doc.head();
            Elements meta = head.getElementsByAttributeValue("http-equiv", "refresh");

            if (!meta.isEmpty()) {

                String[] content = meta.get(0).attr("content").split(";");

                if (content.length > 1)
                {
                    String url = content[1];
                    location = url.substring(url.indexOf('=') + 1);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "Portal URL: " + location);
        return location;
    }

    private OkHttpClient getClient() {

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();

        // set your desired log level
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

        // add your other interceptors …
        // add logging as last interceptor
        httpClient.addInterceptor(new CookieInterceptor(cookies));
        httpClient.addInterceptor(logging);

        return httpClient.build();
    }
}
