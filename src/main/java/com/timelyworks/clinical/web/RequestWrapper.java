package com.timelyworks.clinical.web;

import com.google.gson.*;
import org.json.JSONObject;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.List;

public class RequestWrapper {

    private static Gson gson = new GsonBuilder().registerTypeAdapter(LocalDate.class, new JsonDeserializer<LocalDate>() {
        @Override
        public LocalDate deserialize(JsonElement json, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            String d = json.getAsJsonPrimitive().getAsString();
            return (d != null) ? LocalDate.parse(d) : null;
        }
    }).create();

    String body;
    HttpHeaders headers;
    UriInfo info;

    private RequestWrapper(HttpHeaders headers, UriInfo info) {
        this.headers = headers;
        this.info = info;
    }

    public static RequestWrapper of(HttpHeaders headers, UriInfo info) {
        return new RequestWrapper(headers, info);
    }

    public static RequestWrapper of(HttpHeaders headers, UriInfo info, String body) {
        return new RequestWrapper(headers, info).wrap(body);
    }

    public RequestWrapper wrap(String body) {
        this.body = body;
        return this;
    }

    private void append(JSONObject o, MultivaluedMap<String, String> params) {
        for (String str : params.keySet()) {
            List<String> values = params.get(str);
            if (values.size() == 1) {
                o.put(str, values.get(0));
            } else {
                o.put(str, values);
            }
        }
    }

    public JSONObject toJSON() {
        JSONObject json = (body != null) ? new JSONObject(body) : new JSONObject();
        headers.getCookies().entrySet().stream().forEach(e -> {
            json.put(e.getKey(), e.getValue().getValue());
        });
        append(json, headers.getRequestHeaders());
        append(json, info.getPathParameters());
        append(json, info.getQueryParameters());
        return json;
    }

    public <T> T toClass(Class<T> type) {
        try {
            return gson.fromJson(toJSON().toString(), type);
        } catch (Exception e) {
            throw new RuntimeException("Malformed request");
        }
    }


}
