package com.github.ddth.springsocialhelper;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.social.facebook.api.Account;
import org.springframework.social.facebook.api.Facebook;
import org.springframework.social.facebook.api.FacebookLink;
import org.springframework.social.facebook.api.FacebookProfile;
import org.springframework.social.facebook.api.Page;
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
    public static FacebookProfile getUserProfile(final String accessToken) {
        Facebook facebook = getFacebook(accessToken);
        return facebook.userOperations().getUserProfile();
    }

    /*----------------------------------------------------------------------*/

    /**
     * Gets a Facebook page information.
     * 
     * @param accessToken
     * @param pageId
     * @return
     */
    public static Page getPageInfo(final String accessToken, String pageId) {
        Facebook facebook = getFacebook(accessToken);
        return facebook.pageOperations().getPage(pageId);
    }

    /**
     * Gets all Facebook pages that the user associated with the access token is
     * an administrator.
     * 
     * @param accessToken
     * @return
     */
    public static List<Page> getPages(final String accessToken) {
        Facebook facebook = getFacebook(accessToken);
        List<Account> accounts = facebook.pageOperations().getAccounts();
        List<Page> result = new ArrayList<Page>();
        if (accounts != null) {
            for (Account account : accounts) {
                Page page = getPageInfo(accessToken, account.getId());
                if (page != null) {
                    result.add(page);
                }
            }
        }
        return result;
    }

    /**
     * Checks whether the user associated with the access token is an admin of
     * the page with the given page id.
     * 
     * @param accessToken
     * @param pageId
     * @return
     */
    public static boolean isPageAdmin(final String accessToken, String pageId) {
        Facebook facebook = getFacebook(accessToken);
        return facebook.pageOperations().isPageAdmin(pageId);
    }

    /**
     * Posts a text status to a page specified by the page id as a page
     * administrator.
     * 
     * @param accessToken
     * @param pageId
     * @param text
     * @return id of the created feed
     */
    public static String postTextToPage(final String accessToken, String pageId, String text) {
        Facebook facebook = getFacebook(accessToken);
        return facebook.pageOperations().post(pageId, text.trim());
    }

    /**
     * Posts a link status to a page specified by the page id as a page
     * administrator.
     * 
     * @param accessToken
     * @param pageId
     * @param url
     * @return id of the created feed
     */
    public static String postLinkToPage(final String accessToken, String pageId, String url) {
        return postLinkToPage(accessToken, pageId, url, null, null);
    }

    /**
     * Posts a link status to a page specified by the page id as a page
     * administrator.
     * 
     * @param accessToken
     * @param pageId
     * @param url
     * @param caption
     * @param message
     * @return id of the created feed
     */
    public static String postLinkToPage(final String accessToken, String pageId, String url,
            String caption, String message) {
        FacebookLink facebookLink = new FacebookLink(url.trim(), null,
                caption != null ? caption.trim() : null, null);
        return postLinkToPage(accessToken, pageId, message, facebookLink);
    }

    /**
     * Posts a link status to a page specified by the page id as a page
     * administrator.
     * 
     * @param accessToken
     * @param pageId
     * @param message
     * @param facebookLink
     * @return id of the created feed
     */
    public static String postLinkToPage(final String accessToken, String pageId, String message,
            FacebookLink facebookLink) {
        Facebook facebook = getFacebook(accessToken);
        return facebook.pageOperations().post(pageId, message != null ? message.trim() : null,
                facebookLink);
    }

    /**
     * Posts a photo status to a page specified by the page id as a page
     * administrator.
     * 
     * @param accessToken
     * @param pageId
     * @param message
     * @param photo
     * @return id of the created photo
     */
    public static String postPhotoToPage(final String accessToken, String pageId, String albumId,
            String message, File photo) {
        return postPhotoToPage(accessToken, pageId, albumId, message, new FileSystemResource(photo));
    }

    /**
     * Posts a photo status to a page specified by the page id as a page
     * administrator.
     * 
     * @param accessToken
     * @param pageId
     * @param message
     * @param photo
     * @return id of the created photo
     */
    public static String postPhotoToPage(final String accessToken, String pageId, String albumId,
            String message, byte[] photo) {
        return postPhotoToPage(accessToken, pageId, albumId, message, new ByteArrayResource(photo));
    }

    /**
     * Posts a photo status to a page specified by the page id as a page
     * administrator.
     * 
     * @param accessToken
     * @param pageId
     * @param message
     * @param photo
     * @return id of the created photo
     */
    public static String postPhotoToPage(final String accessToken, String pageId, String albumId,
            String message, InputStream photo) {
        return postPhotoToPage(accessToken, pageId, albumId, message,
                new InputStreamResource(photo));
    }

    /**
     * Posts a photo status to a page specified by the page id as a page
     * administrator.
     * 
     * @param accessToken
     * @param pageId
     * @param message
     * @param photo
     * @return id of the created photo
     */
    public static String postPhotoToPage(final String accessToken, String pageId, String albumId,
            String message, URL photo) {
        return postPhotoToPage(accessToken, pageId, albumId, message, new UrlResource(photo));
    }

    private static String postPhotoToPage(final String accessToken, String pageId, String albumId,
            String message, Resource resource) {
        Facebook facebook = getFacebook(accessToken);
        if (message == null) {
            return facebook.pageOperations().postPhoto(pageId, albumId != null ? albumId : pageId,
                    resource);
        } else {
            return facebook.pageOperations().postPhoto(pageId, albumId != null ? albumId : pageId,
                    resource, message.trim());
        }
    }
    /*----------------------------------------------------------------------*/
}
