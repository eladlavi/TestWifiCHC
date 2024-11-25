package il.co.expertigo.testwifiapplication;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * The easy way to sen http requests
 */
public class Http {
    /**
     * The easy way to sen http request
     *
     * @param url http://google.com
     * @param contentType text/html, image/png, ...
     * @param requestMethod GET, POST, ...
     * @param work when you got a open connection you decide what to do, you can get response code, inputStream, ...
     * @throws IOException when something gos wrong
     */
    public static void sendHttpRequest(@NonNull String url, @NonNull String contentType, @NonNull String requestMethod, @NonNull HttpWork work) throws IOException {
        sendHttpRequest(new URL(url), contentType, requestMethod, work);
    }
    /**
     * The easy way to sen http request
     *
     * @param url new URL("http://google.com")
     * @param contentType text/html, image/png, ...
     * @param requestMethod GET, POST, ...
     * @param work when you got a open connection you decide what to do, you can get response code, inputStream, ...
     * @throws IOException when something gos wrong
     */
    public static void sendHttpRequest(@NonNull URL url, @NonNull String contentType, @NonNull String requestMethod, @NonNull HttpWork work) throws IOException {
        sendHttpRequest((HttpURLConnection) url.openConnection(), contentType, requestMethod, work);
    }
    /**
     * The easy way to sen http request
     *
     * @param connection an open connection
     * @param contentType text/html, image/png, ...
     * @param requestMethod GET, POST, ...
     * @param work when you got a open connection you decide what to do, you can get response code, inputStream, ...
     * @throws IOException when something gos wrong
     */
    public static void sendHttpRequest(@NonNull HttpURLConnection connection, @NonNull String contentType, @NonNull String requestMethod, @NonNull HttpWork work) throws IOException {
        try {
            connection.setRequestMethod(requestMethod);
            boolean needOutputStream = requestMethod.equals("POST");
            connection.setDoInput(true);
            connection.setDoOutput(needOutputStream);
            connection.setRequestProperty( "Content-Type", contentType);
            connection.setUseCaches(false);
            connection.connect();
            work.work(connection);
        } finally {
            connection.disconnect();
        }
    }
    public interface HttpWork{
        void work(HttpURLConnection connection) throws IOException;
    }
    public static void writeBody(@NonNull OutputStream outputStream, @NonNull String body) throws IOException {
        outputStream.write(body.getBytes());
    }

    /**
     * If you don't have the strength to read all the bytes in a while loop, here we do it for you.
     * Note, the inputStream will not close in this function.
     * @param inputStream connection's input stream
     * @return response of http connection in string
     * @throws IOException on inputStream error
     */
    public static @NonNull String readResponse(@NonNull InputStream inputStream) throws IOException{
        byte[] buffer = new byte[1024];
        int read;
        StringBuilder stringBuffer = new StringBuilder();
        while((read = inputStream.read(buffer)) != -1)
            stringBuffer.append(new String(buffer,0, read));
        return stringBuffer.toString();
    }
}
