package net.henriqueof.stb.services;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by Carlos Henrique on 21/12/2016.
 */

public interface StalkerService {
    /**
     *
     * @return Authorization token
     */
    @Headers({
            "User-Agent: Mozilla/5.0 (QtEmbedded; U; Linux; C) AppleWebKit/533.3 (KHTML, like Gecko) MAG200 stbapp ver: 4 rev: 1812 Mobile Safari/533.3",
            "X-User-Agent: Model: MAG250; Link: WiFi"
    })
    @GET("{endpoint}?type=stb&action=handshake&JsHttpRequest=1-xml")
    Call<Response<Token>> Authenticate(@Path(value = "endpoint", encoded = true) String endpoint, @Query("token") String token);

    /**
     *
     * @param authorization Authorization header
     * @param referrer Referrer
     * @param stbType STB model
     * @param sn STB serial number
     * @param ver STB firmware info
     * @param imageVersion STB firmware version
     * @param HardwareVersion STB hardware version
     * @return STB profile info
     */
    @Headers({
            "User-Agent: Mozilla/5.0 (QtEmbedded; U; Linux; C) AppleWebKit/533.3 (KHTML, like Gecko) MAG200 stbapp ver: 4 rev: 1812 Mobile Safari/533.3",
            "X-User-Agent: Model: MAG250; Link: WiFi"
    })
    @GET("{endpoint}?type=stb&action=get_profile&hd=1&num_banks=2&not_valid_token=0&auth_second_step=0&video_out=hdmi&client_type=STB&hw_version_2=e4dba31b32a3bb86d5c218a401fb9c36d89816fd&timestamp=1483547613&metrics={\"mac\":\"00:1A:79:CA:91:27\",\"sn\":\"3442be39a85f21e4\",\"type\":\"stb\",\"model\":\"MAG250\",\"uid\":\"\"}&JsHttpRequest=1-xml")
    Call<Response<Profile>> getProfile(@Path(value = "endpoint", encoded = true) String endpoint, @Header("Authorization") String authorization, @Header("Referrer") String referrer,
                                       @Query("stb_type") String stbType, @Query("sn") String sn, @Query("ver") String ver, @Query("image_version") String imageVersion, @Query("hw_version") String HardwareVersion);

    @Headers({
            "User-Agent: Mozilla/5.0 (QtEmbedded; U; Linux; C) AppleWebKit/533.3 (KHTML, like Gecko) MAG200 stbapp ver: 4 rev: 1812 Mobile Safari/533.3",
            "X-User-Agent: Model: MAG250; Link: WiFi"
    })
    @GET("{endpoint}?type=watchdog&action=get_events")
    Call<Void> watchdog(@Path(value = "endpoint", encoded = true) String endpoint, @Header("Authorization") String authorization, @Header("Referrer") String referrer,
                        @Query("cur_play_type") String currPlayType);

    /**
     *
     * @return Genre list
     */
    @Headers({
            "User-Agent: Mozilla/5.0 (QtEmbedded; U; Linux; C) AppleWebKit/533.3 (KHTML, like Gecko) MAG200 stbapp ver: 2 rev: 250 Safari/533.3",
            "X-User-Agent: Model: MAG250; Link: WiFi"
    })
    @GET("{endpoint}?type=itv&action=get_genres&JsHttpRequest=1%2Dxml&")
    Call<Response<List<Genre>>> getGenres(@Path(value = "endpoint", encoded = true) String endpoint, @Header("Authorization") String authorization, @Header("Referrer") String referrer);

    @Headers({
            "User-Agent: Mozilla/5.0 (QtEmbedded; U; Linux; C) AppleWebKit/533.3 (KHTML, like Gecko) MAG200 stbapp ver: 4 rev: 1812 Mobile Safari/533.3",
            "X-User-Agent: Model: MAG250; Link: WiFi"
    })
    @GET("{endpoint}?type=itv&action=set_last_id")
    Call<Void> itvSetLastId(@Path(value = "endpoint", encoded = true) String endpoint, @Header("Authorization") String authorization, @Header("Referrer") String referrer,
                         @Query("id") int channelId);

    @Headers({
            "User-Agent: Mozilla/5.0 (QtEmbedded; U; Linux; C) AppleWebKit/533.3 (KHTML, like Gecko) MAG200 stbapp ver: 4 rev: 1812 Mobile Safari/533.3",
            "X-User-Agent: Model: MAG250; Link: WiFi"
    })
    @GET("{endpoint}?type=itv&action=set_fav&JsHttpRequest=1-xml")
    Call<Void> itvSetFav(@Path(value = "endpoint", encoded = true) String endpoint, @Header("Authorization") String authorization, @Header("Referrer") String referrer,
                         @Query("fav_ch") String channels, @Query("JsHttpRequest") String request);

    @Headers({
            "User-Agent: Mozilla/5.0 (QtEmbedded; U; Linux; C) AppleWebKit/533.3 (KHTML, like Gecko) MAG200 stbapp ver: 4 rev: 1812 Mobile Safari/533.3",
            "X-User-Agent: Model: MAG250; Link: WiFi"
    })
    @GET("{endpoint}?type=itv&action=get_all_fav_channels&fav=1&JsHttpRequest=1-xml&")
    Call<Response<Channels>> getAllFavChannels(@Path(value = "endpoint", encoded = true) String endpoint, @Header("Authorization") String authorization, @Header("Referrer") String referrer);

    @Headers({
            "User-Agent: Mozilla/5.0 (QtEmbedded; U; Linux; C) AppleWebKit/533.3 (KHTML, like Gecko) MAG200 stbapp ver: 4 rev: 1812 Mobile Safari/533.3",
            "X-User-Agent: Model: MAG250; Link: WiFi"
    })
    @GET("{endpoint}?type=itv&action=get_fav_ids")
    Call<Response<String[]>> getFavIds(@Path(value = "endpoint", encoded = true) String endpoint, @Header("Authorization") String authorization, @Header("Referrer") String referrer);

    @Headers({
            "User-Agent: Mozilla/5.0 (QtEmbedded; U; Linux; C) AppleWebKit/533.3 (KHTML, like Gecko) MAG200 stbapp ver: 4 rev: 1812 Mobile Safari/533.3",
            "X-User-Agent: Model: MAG250; Link: WiFi"
    })
    @GET("{endpoint}?type=itv&action=get_ordered_list&JsHttpRequest=1-xml")
    Call<Response<Channels>> itvGetOrderedList(@Path(value = "endpoint", encoded = true) String endpoint, @Header("Authorization") String authorization, @Header("Referrer") String referrer,
                                              @Query("genre") String genre, @Query("p") int currentPage, @Query("sortby") String sortBy);

    /**
     *
     * @param authorization Authorization header
     * @param referrer Referrer
     * @return Channel list
     */
    @Headers({
            "User-Agent: Mozilla/5.0 (QtEmbedded; U; Linux; C) AppleWebKit/533.3 (KHTML, like Gecko) MAG200 stbapp ver: 4 rev: 1812 Mobile Safari/533.3",
            "X-User-Agent: Model: MAG250; Link: WiFi"
    })
    @GET("{endpoint}?type=itv&action=get_all_channels")
    Call<Response<Channels>> getAllChannels(@Path(value = "endpoint", encoded = true) String endpoint, @Header("Authorization") String authorization, @Header("Referrer") String referrer);

    /**
     *
     * @return EPG
     */
    @Headers({
            "User-Agent: Mozilla/5.0 (QtEmbedded; U; Linux; C) AppleWebKit/533.3 (KHTML, like Gecko) MAG200 stbapp ver: 4 rev: 1812 Mobile Safari/533.3",
            "X-User-Agent: Model: MAG250; Link: WiFi"
    })
    @GET("{endpoint}?type=itv&action=get_epg_info&period=5&JsHttpRequest=1-xml")
    Call<Response<Epg>> getEpgInfo(@Path(value = "endpoint", encoded = true) String endpoint, @Header("Authorization") String authorization, @Header("Referrer") String referrer);

    @Headers({
            "User-Agent: Mozilla/5.0 (QtEmbedded; U; Linux; C) AppleWebKit/533.3 (KHTML, like Gecko) MAG200 stbapp ver: 4 rev: 1812 Mobile Safari/533.3",
            "X-User-Agent: Model: MAG250; Link: WiFi"
    })
    @GET("{endpoint}?type=itv&action=get_short_epg&JsHttpRequest=1-xml")
    Call<Response<EpgItem[]>> getShortEpg(@Path(value = "endpoint", encoded = true) String endpoint, @Header("Authorization") String authorization, @Header("Referrer") String referrer,
                                    @Query("ch_id") int channelId);

    /**
     *
     * @return Channel stream link
     */
    @Headers({
            "User-Agent: Mozilla/5.0 (QtEmbedded; U; Linux; C) AppleWebKit/533.3 (KHTML, like Gecko) MAG200 stbapp ver: 4 rev: 1812 Mobile Safari/533.3",
            "X-User-Agent: Model: MAG250; Link: WiFi"
    })
    @GET("{endpoint}?type=itv&action=create_link")
    Call<Response<Stream>> itvCreateLink(@Path(value = "endpoint", encoded = true) String endpoint, @Header("Authorization") String authorization, @Header("Referrer") String referrer,
                                         @Query("cmd") String cmd);

    @Headers({
            "User-Agent: Mozilla/5.0 (QtEmbedded; U; Linux; C) AppleWebKit/533.3 (KHTML, like Gecko) MAG200 stbapp ver: 4 rev: 1812 Mobile Safari/533.3",
            "X-User-Agent: Model: MAG250; Link: WiFi"
    })
    @GET("{endpoint}?type=vod&action=get_categories")
    Call<Response<Category[]>> vodGetCategories(@Path(value = "endpoint", encoded = true) String endpoint, @Header("Authorization") String authorization, @Header("Referrer") String referrer);

    @Headers({
            "User-Agent: Mozilla/5.0 (QtEmbedded; U; Linux; C) AppleWebKit/533.3 (KHTML, like Gecko) MAG200 stbapp ver: 4 rev: 1812 Mobile Safari/533.3",
            "X-User-Agent: Model: MAG250; Link: WiFi"
    })
    @GET("{endpoint}?type=vod&action=get_ordered_list")
    Call<Response<VodList>> vodGetOrderedList(@Path(value = "endpoint", encoded = true) String endpoint, @Header("Authorization") String authorization, @Header("Referrer") String referrer,
                                              @Query("category") String category, @Query("p") int currentPage, @Query("sortby") String sortBy);

    @Headers({
            "User-Agent: Mozilla/5.0 (QtEmbedded; U; Linux; C) AppleWebKit/533.3 (KHTML, like Gecko) MAG200 stbapp ver: 4 rev: 1812 Mobile Safari/533.3",
            "X-User-Agent: Model: MAG250; Link: WiFi"
    })
    @GET("{endpoint}?type=vod&action=create_link")
    Call<Response<Stream>> vodCreateLink(@Path(value = "endpoint", encoded = true) String endpoint, @Header("Authorization") String authorization, @Header("Referrer") String referrer,
                                         @Query("cmd") String cmd);

    class Response<T> {
        T js;
    }

    class Token {
        public String token;
        public String not_valid;
    }

    class Profile {
        int id;
        public int last_itv_id;
        public int watchdog_timeout;

        public String parent_password;

        /*
        String name;
        String sname;
        String pass;

        int bright;
        int contrast;
        int saturation;
        int aspect;
        String video_out;
        int volume;
        int playback_buffer_bytes;
        int playback_buffer_size;
        int audio_out;

        String mac;
        String ip;
        String ls;
        String version;
        String lang;

        String locale;
        int city_id;

        int status;
        int hd;

        int main_notify;
        int fav_itv_on;

        String now_playing_start; // Datetime?
        int now_playing_type;
        String now_playing_content;
        */
    }

    class Genre {
        public String id;
        public String title;
        public String alias;
        public int censored;
    }

    class Channels {
        int total_items;
        int max_page_items;
        int selected_item;
        int cur_page;
        Channel[] data;
    }

    class Channel {
        public int id;
        public int number;
        public String name;
        public String cmd;
        public String tv_genre_id;
        public int use_http_tmp_link;
        public String logo;
        public int lock;
        public int fav;
        public String cur_playing;
    }

    class Stream {
        public int id;
        public String cmd;
        public int streamer_id;
        public int link_id;
        public int load;
        public String error;
    }

    class Epg {
        Map<String, EpgItem[]> data;
    }

    class EpgItem {
        public int id;
        public int ch_id;
        public String time;
        public String time_to;
        public int duration;
        public String name;
        public String descr;
        public String category;
        public String director;
        public String actor;
        public long start_timestamp; // long?
        public String stop_timestamp;
        public String t_time;               // Time string
        public String t_time_to;
    }

    class Category {
        public String id;
        public String title;
        public String alias;
    }

    class VodList {
        int total_items;
        int max_page_items;
        int selected_item;
        int cur_page;
        public Vod[] data;
    }

    class Vod {
        public String name;
        public String description;
        public String pic;

        String rtsp_url;

        String category_id;
        String genre_id;

        String director;
        String actors;
        String year;
        public String added;   // DateTime

        public String screenshot_uri;
        public String cmd;
    }
}
