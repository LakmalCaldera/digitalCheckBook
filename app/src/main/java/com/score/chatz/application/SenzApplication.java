package com.score.chatz.application;

import android.app.Application;

/**
 * Application class to hold shared attributes
 *
 * @author erangaeb@gmail.com (eranga herath)
 */
public class SenzApplication extends Application {

    private static boolean onChat = false;

    private static String userOnChat = null;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate() {
        super.onCreate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    public static boolean isOnChat() {
        return onChat;
    }

    public static void setOnChat(boolean onChat) {
        SenzApplication.onChat = onChat;
    }

    public static String getUserOnChat() {
        return userOnChat;
    }

    public static void setUserOnChat(String userOnChat) {
        SenzApplication.userOnChat = userOnChat;
    }
}
