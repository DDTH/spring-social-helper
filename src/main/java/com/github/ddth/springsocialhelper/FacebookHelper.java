package com.github.ddth.springsocialhelper;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.springframework.social.facebook.api.Facebook;
import org.springframework.social.facebook.api.FacebookProfile;
import org.springframework.social.facebook.api.impl.FacebookTemplate;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * Helper class for Spring-Social-Facebook.
 * 
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1
 */
public class FacebookHelper {
    private final static Cache<String, Facebook> cachedFacebooks;
    static {
        int numProcessors = Runtime.getRuntime().availableProcessors();
        cachedFacebooks = CacheBuilder.newBuilder().concurrencyLevel(numProcessors)
                .maximumSize(1000).expireAfterWrite(3600, TimeUnit.SECONDS).build();
    }

    /**
     * Gets a {@link Facebook} instance associated with an access token.
     * 
     * @param accessToken
     * @return
     */
    public static Facebook getFacebook(final String accessToken) {
        try {
            Facebook facebook = cachedFacebooks.get(accessToken, new Callable<Facebook>() {
                @Override
                public Facebook call() throws Exception {
                    return new FacebookTemplate(accessToken);
                }
            });
            return facebook;
        } catch (ExecutionException e) {
            return null;
        }
    }

    /**
     * Gets a feed's information.
     * 
     * @param accessToken
     * @param feedId
     * @return
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> getFeedInfo(final String accessToken, String feedId) {
        Facebook facebook = getFacebook(accessToken);
        Map<String, Object> obj = facebook.fetchObject(feedId, Map.class);
        return obj;
    }

    /**
     * Gets a Facebook user profile.
     * 
     * @param accessToken
     * @return
     */
    public static FacebookProfile getUserProfile(String accessToken) {
        Facebook facebook = getFacebook(accessToken);
        return facebook.userOperations().getUserProfile();
    }
}
