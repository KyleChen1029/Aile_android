package tw.com.chainsea.android.common.client.helper;

import java.util.Map;

import okhttp3.HttpUrl;

public class ParamsHelper {

    /**
     * Map<String, String> params to Json String
     *
     * @param params
     * @return Json String
     * @author Evan Wang
     * @version 0.0.1
     * @since 0.0.1
     */
    public static String buildParamsToJsonString(Map<String, String> params) {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        for (Map.Entry<String, String> param : params.entrySet()) {
            builder.append("\"");
            builder.append(param.getKey());
            builder.append("\":\"");
            builder.append(param.getValue());
            builder.append("\",");
        }
        builder.deleteCharAt(builder.lastIndexOf(","));
        builder.append("}");
        return builder.toString();
    }

    /**
     * Map<String, String> params to HttpUrl.Builder
     *
     * @param params
     * @return HttpUrl.Builder
     * @author Evan Wang
     * @version 0.0.1
     * @since 0.0.1
     */
    public static HttpUrl.Builder buildParams(String url, Map<String, String> params) {
        HttpUrl.Builder httpBuilder = HttpUrl.parse(url).newBuilder();
        if (params != null) {
            for (Map.Entry<String, String> param : params.entrySet()) {
                httpBuilder.addQueryParameter(param.getKey(), param.getValue());
            }
        }
        return httpBuilder;
    }
}
