package Thread;

import config.Constants;
import entity.LoginBody;
import entity.LoginParameter;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import utils.Md5Utils;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.http.client.config.RequestConfig.custom;

public class NVR_API {
    private CloseableHttpClient client;
    private HttpPost httpPost;
    private HttpGet httpGet;
    private CloseableHttpResponse httpResponse;
    private static NVR_API mInstance = null;
    private static Cookie cookie;
    private HttpClientContext context;

    public static NVR_API getInstance() {
        if (mInstance == null) {
            mInstance = new NVR_API();
        }
        return mInstance;
    }

    public final LoginParameter getLoginParameter() throws IOException {
        LoginParameter loginParameter = null;
        String event_url = Constants.NVR_LOGIN_URI;
        httpGet = new HttpGet(event_url);
        CookieStore cookieStore = new BasicCookieStore();
        HttpClientBuilder custom = HttpClients.custom();
        custom.setDefaultRequestConfig(custom().setCookieSpec(CookieSpecs.STANDARD).build());
        custom.setDefaultCookieStore(cookieStore);
        client = custom.build();
        context = HttpClientContext.create();

        try {
            httpResponse = client.execute(httpGet, context);
            List<Cookie> cookies = context.getCookieStore().getCookies();
            cookie = cookies.get(0);
            String responseString = EntityUtils.toString(httpResponse.getEntity());
            JSONObject responseJson = new JSONObject(responseString);

            String qop = responseJson.get("qop").toString();
            String opaque = responseJson.get("opaque").toString();
            String realm = responseJson.get("realm").toString();
            String change_pwd = responseJson.get("change_pwd").toString();
            String nonce = responseJson.get("nonce").toString();

            loginParameter = new LoginParameter(qop, opaque, realm, change_pwd, nonce);

        } finally {
            httpResponse.close();
        }

        return loginParameter;
    }

    private final LoginBody createLoginBody() throws IOException {
        LoginParameter loginParameter = getLoginParameter();
        Date date = new Date();
        DateFormat dateFormat;
        dateFormat = new SimpleDateFormat("yyyy-MM-dd.hh:mm:ss");

        String cnonce = Md5Utils.hash(dateFormat.format(date));

        String response = calculateResponse(loginParameter, cnonce);
        LoginBody loginBody = new LoginBody(Constants.EVERFOCUS_USERNAME, Constants.EVERFOCUS_REALM, loginParameter.getNonce(), loginParameter.getQop(), loginParameter.getOpaque(), Constants.EVERFOCUS_NONCE_COUNT, cnonce, response);
        return loginBody;
    }

    private final String calculateResponse(LoginParameter loginParameter, String cnonce) {
        String username = Constants.EVERFOCUS_USERNAME;
        String password = Constants.EVERFOCUS_PASSWORD;
        String realm = Constants.EVERFOCUS_REALM;
        String method = Constants.METHOD;
        String digestURI = Constants.DIGEST_URI;

        String Hash1 = Md5Utils.hash(username + ":" + realm + ":" + password);
        String Hash2 = Md5Utils.hash(method + ":" + digestURI);



        assert loginParameter != null;
        String nonce = loginParameter.getNonce();
        String nonceCount = Constants.EVERFOCUS_NONCE_COUNT;
        String qop = loginParameter.getQop();

        String response = Md5Utils.hash(Hash1 + ":" + nonce + ":" + nonceCount + ":" + cnonce + ":" + qop + ":" + Hash2);


        return response;
    }

    public final void Login() throws IOException {
        LoginBody loginBody = createLoginBody();
        String event_url = Constants.NVR_LOGIN_URI;
        httpPost = new HttpPost(event_url);
        CookieStore cookieStore = new BasicCookieStore();
        cookieStore.addCookie(cookie);

        String username = loginBody.getUsername();
        String nc = loginBody.getNc();
        String cnonce = loginBody.getCnonce();
        String response = loginBody.getResponse();

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("username", username));
        urlParameters.add(new BasicNameValuePair("nc", nc));
        urlParameters.add(new BasicNameValuePair("cnonce", cnonce));
        urlParameters.add(new BasicNameValuePair("response", response));
        httpPost.setEntity(new UrlEncodedFormEntity(urlParameters));

        try {
            HttpClientBuilder custom = HttpClients.custom();
            custom.setDefaultRequestConfig(custom().setCookieSpec(CookieSpecs.STANDARD).build());
            custom.setDefaultCookieStore(cookieStore);
            client = custom.build();
            httpResponse = client.execute(httpPost);
        } finally {
            httpResponse.close();
        }
    }

    public final List<JSONObject> getCameraList() throws IOException {
        String event_url = Constants.NVR_CAMERALIST_URI;
        httpGet = new HttpGet(event_url);
        CookieStore cookieStore = new BasicCookieStore();
        cookieStore.addCookie(cookie);

        try {
            HttpClientBuilder custom = HttpClients.custom();
            custom.setDefaultRequestConfig(custom().setCookieSpec(CookieSpecs.STANDARD).build());
            custom.setDefaultCookieStore(cookieStore);
            client = custom.build();
            context = HttpClientContext.create();
            httpResponse = client.execute(httpGet, context);
            HttpEntity entity = httpResponse.getEntity();

            if (entity != null) {
                String retSrc = EntityUtils.toString(entity);
                String newSrc = retSrc.substring(1,retSrc.length()-1);
                String[] SrcSplit = newSrc.split("},");

                List<JSONObject> jsonObjectList = Arrays.asList(SrcSplit).stream().map(string ->{
                    return new JSONObject(string+"}");
                }).collect(Collectors.toList());
//                result = new JSONObject(retSrc);

                return jsonObjectList;
            }
            return null;
        } finally {
            client.close();

        }
    }

    public final List<JSONObject> getCameraSTATUS() throws IOException {
        String event_url = Constants.NVR_CAMERASTATUS_URI;
        httpGet = new HttpGet(event_url);
        CookieStore cookieStore = new BasicCookieStore();
        cookieStore.addCookie(cookie);

        try {
            HttpClientBuilder custom = HttpClients.custom();
            custom.setDefaultRequestConfig(custom().setCookieSpec(CookieSpecs.STANDARD).build());
            custom.setDefaultCookieStore(cookieStore);
            client = custom.build();
            context = HttpClientContext.create();
            httpResponse = client.execute(httpGet, context);
            HttpEntity entity = httpResponse.getEntity();

            if (entity != null) {
                String retSrc = EntityUtils.toString(entity);
                String newSrc = retSrc.substring(1,retSrc.length()-1);
                String[] SrcSplit = newSrc.split("},");

                List<JSONObject> jsonObjectList = Arrays.asList(SrcSplit).stream().map(string ->{
                    return new JSONObject(string+"}");
                }).collect(Collectors.toList());
//                result = new JSONObject(retSrc);

                return jsonObjectList;
            }
            return null;
        } finally {
            client.close();

        }
    }

    public final JSONObject getCameraDetail(int cameraCH) throws IOException {
        String event_url = String.format(Constants.NVR_CAMERADETAIL_URI, cameraCH);
        httpGet = new HttpGet(event_url);
        CookieStore cookieStore = new BasicCookieStore();
        cookieStore.addCookie(cookie);
        JSONObject result = null;
        try {
            HttpClientBuilder custom = HttpClients.custom();
            custom.setDefaultRequestConfig(custom().setCookieSpec(CookieSpecs.STANDARD).build());
            custom.setDefaultCookieStore(cookieStore);
            client = custom.build();
            context = HttpClientContext.create();
            httpResponse = client.execute(httpGet, context);
            HttpEntity entity = httpResponse.getEntity();

            if (entity != null) {
                String retSrc = EntityUtils.toString(entity);
                result = new JSONObject(retSrc);
                return result;
            }
            return null;
        } finally {
            client.close();

        }
    }
}

