package readme;

import com.coditory.quark.uri.UriBuilder;
import com.coditory.quark.uri.UriComponents;

public class ReadmeSamples {
    public static void main(String[] args) {
        String result = UriBuilder.fromUri("https://coditory.com?w=W&a=A")
                .addPathSegment("about")
                .addQueryParam("a", "X")
                .addQueryParam("a", "X")
                .addQueryParam("a", "Y")
                .addQueryParam("b", "Y")
                .addQueryParam("e", "")
                .buildUriString();
        System.out.println(result);

        UriComponents uriComponents = UriBuilder.fromUri("https://coditory.com?w=W&a=A")
                .addPathSegment("about")
                .addQueryParam("a", "X")
                .addQueryParam("a", "X")
                .addQueryParam("a", "Y")
                .addQueryParam("b", "Y")
                .addQueryParam("e", "")
                .buildUriComponents();
        System.out.println(uriComponents);

        result = UriBuilder.fromUri("https://coditory.com?w=W&a=A")
                .addPathSegment("a b")
                .addQueryParam("x y", " X Y ")
                .buildUriString();
        System.out.println(result);

        result = UriBuilder.fromUri("https://coditory.com/a+bc/d%20ef/")
                .addPathSegment("x y ")
                .setFragment("frag ment")
                .addQueryParam("f oo", "b ar")
                .addQueryParam("x", "y+z")
                .buildUriString();
        System.out.println(result);

        uriComponents = UriBuilder.fromUri("/abc?a+b=A+B").buildUriComponents();
        System.out.println(uriComponents);
        uriComponents = UriBuilder.fromUri("/abc?a%20b=A%20B").buildUriComponents();
        System.out.println(uriComponents);
    }
}
