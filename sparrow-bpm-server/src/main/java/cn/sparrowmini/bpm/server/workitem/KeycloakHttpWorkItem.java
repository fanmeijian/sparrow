//package cn.sparrowmini.bpm.server.workitem;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.http.Consts;
//import org.apache.http.HttpEntity;
//import org.apache.http.NameValuePair;
//import org.apache.http.StatusLine;
//import org.apache.http.client.entity.UrlEncodedFormEntity;
//import org.apache.http.client.methods.*;
//import org.apache.http.conn.ssl.NoopHostnameVerifier;
//import org.apache.http.entity.StringEntity;
//import org.apache.http.impl.client.CloseableHttpClient;
//import org.apache.http.impl.client.HttpClients;
//import org.apache.http.message.BasicNameValuePair;
//import org.apache.http.util.EntityUtils;
//import org.kie.api.runtime.process.WorkItem;
//import org.kie.api.runtime.process.WorkItemHandler;
//import org.kie.api.runtime.process.WorkItemManager;
//import org.springframework.stereotype.Component;
//import org.springframework.web.reactive.function.client.WebClient;
//
//import javax.net.ssl.SSLContext;
//import javax.net.ssl.TrustManager;
//import javax.net.ssl.X509TrustManager;
//import java.security.cert.X509Certificate;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import static org.jbpm.process.workitem.rest.RESTWorkItemHandler.*;
//
//@Slf4j
//@Component
//public class KeycloakHttpWorkItem implements WorkItemHandler {
//    @Override
//    public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
//        try {
//            WebClient webClient;
//            JsonNode token = null;
//            CloseableHttpResponse response;
//            try (CloseableHttpClient httpClient = createUnsafeClient()) {
//                Map<String, Object> results = new HashMap<String, Object>();
//                List<NameValuePair> form = new ArrayList<>();
//                form.add(new BasicNameValuePair("client_id", this.clientId));
//                form.add(new BasicNameValuePair("client_secret", this.credential));
//                form.add(new BasicNameValuePair("grant_type", "client_credentials"));
//                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(form, Consts.UTF_8);
//
//                HttpPost httpPost = new HttpPost(this.authUrl);
//                httpPost.setEntity(entity);
//                response = httpClient.execute(httpPost);
//
//                StatusLine statusLine = response.getStatusLine();
//                int responseCode = statusLine.getStatusCode();
//
//                HttpEntity respEntity = response.getEntity();
//                results.put(PARAM_STATUS,responseCode);
//                if (responseCode >= 200 && responseCode < 300) {
//                    String responseBody = EntityUtils.toString(respEntity);
//                    token = new ObjectMapper().readTree(responseBody);
//
//                } else {
//                    String responseBody = EntityUtils.toString(respEntity);
//                    logger.warn("Unsuccessful response from REST server (status: {}, endpoint: {}, response: {}",
//                            responseCode, this.authUrl, responseBody);
//                    results.put(PARAM_STATUS_MSG,
//                            "endpoint " + this.authUrl + " could not be reached: " + responseBody);
//                    throw new RuntimeException(results.get(PARAM_STATUS_MSG).toString());
//                }
////				results.put(PARAM_STATUS, responseCode);
//                logger.info("调用获取token结果{}", results);
//            }
//
//            try (CloseableHttpClient httpClient = createUnsafeClient()) {
//                Map<String, Object> results = new HashMap<String, Object>();
//                Map<String, Object> params = workItem.getParameters();
//                String urlStr = (String) workItem.getParameter("Url");
//                String method = (String) workItem.getParameter("Method");
//                if (method == null) {
//                    method = "GET";
//                }
//                String resultClass = (String) workItem.getParameter("ResultClass");
//                String contentType = "application/json";
//
//                try {
//                    Object content = params.get(PARAM_CONTENT_DATA) != null ? params.get(PARAM_CONTENT_DATA)
//                            : params.get(PARAM_CONTENT);
//                    if (!(content instanceof String)) {
//                        content = transformRequest(content, contentType);
//                    }
//
//                    logger.info("请求url:{} \n 请求内容：{}", urlStr, content);
//
//                    HttpUriRequest request = null;
//
//                    switch (method) {
//                        case "POST":
//                            HttpPost httpPost1 = new HttpPost(urlStr);
//                            httpPost1.setEntity(new StringEntity((String) content, ContentType.parse(contentType)));
//                            request = httpPost1;
//                            break;
//                        case "DELETE":
//                            request = new HttpDelete(urlStr);
//                            break;
//                        case "PATCH":
//                            HttpPatch httpPatch = new HttpPatch(urlStr);
//                            httpPatch.setEntity(new StringEntity((String) content, ContentType.parse(contentType)));
//                            request = httpPatch;
//                            break;
//                        case "PUT":
//                            HttpPut httpPut = new HttpPut(urlStr);
//                            httpPut.setEntity(new StringEntity((String) content, ContentType.parse(contentType)));
//                            request = httpPut;
//                            break;
//                        default:
//                            request = new HttpGet(urlStr);
//                            break;
//                    }
//
//                    request.setHeader("Authorization", "Bearer " + token.get("access_token").asText());
//
//                    response = httpClient.execute(request);
//
//                    StatusLine statusLine = response.getStatusLine();
//                    int responseCode = statusLine.getStatusCode();
//                    results.put(PARAM_STATUS, responseCode);
//                    HttpEntity respEntity = response.getEntity();
//                    String responseBody = EntityUtils.toString(respEntity);
//
//                    if (responseCode >= 200 && responseCode < 300) {
//                        postProcessResult(responseBody, resultClass, contentType, results);
//                        results.put(PARAM_STATUS_MSG, "request to endpoint " + urlStr + " successfully completed "
//                                + statusLine.getReasonPhrase());
//                    } else {
//                        logger.warn("Unsuccessful response from REST server (status: {}, endpoint: {}, response: {}",
//                                responseCode, urlStr, responseBody);
//                        results.put(PARAM_STATUS_MSG, "endpoint " + urlStr + " could not be reached: " + responseBody);
//                    }
//                    logger.info("调用业务服务结果{}", results);
//
//                    manager.completeWorkItem(workItem.getId(), results);
//                } catch (Exception e) {
//                    handleException(e);
//                }
//            }
//
//        } catch (Throwable cause) {
//            handleException(cause);
//        }
//    }
//
//    @Override
//    public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
//
//    }
//
//    public static CloseableHttpClient createUnsafeClient() throws Exception {
//        // Trust all certificates
//        TrustManager[] trustAllCertificates = new TrustManager[]{
//                new X509TrustManager() {
//                    public X509Certificate[] getAcceptedIssuers() { return null; }
//                    public void checkClientTrusted(X509Certificate[] certs, String authType) {}
//                    public void checkServerTrusted(X509Certificate[] certs, String authType) {}
//                }
//        };
//
//        // Create an SSL context that ignores invalid certificates
//        SSLContext sslContext = SSLContext.getInstance("TLS");
//        sslContext.init(null, trustAllCertificates, new java.security.SecureRandom());
//
//        // Build the HttpClient with the custom SSLContext
//        return HttpClients.custom()
//                .setSSLContext(sslContext)
//                .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
//                .build();
//    }
//}
