/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.s4.example.twitter;

import java.io.File;
import java.io.FileInputStream;
import java.net.ServerSocket;
import java.util.Properties;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.s4.base.Event;
import org.apache.s4.core.adapter.AdapterApp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.name.Named;

public class TwitterInputAdapter extends AdapterApp {

    private static Logger logger = LoggerFactory.getLogger(TwitterInputAdapter.class);

    public TwitterInputAdapter() {
    }

    private LinkedBlockingQueue<Status> messageQueue = new LinkedBlockingQueue<Status>();

    protected ServerSocket serverSocket;

    private Thread t;

    @Named("twitter4j.user")
    @Inject
    String twitterUser;

    @Named("twitter4j.password")
    @Inject
    String twitterPassword;

    @Named("twitter4j.debug")
    @Inject
    String twitter4jDebug;

    @Override
    protected void onClose() {
    }

    @Override
    protected void onInit() {
        super.onInit();
        t = new Thread(new Dequeuer());
    }

    public void connectAndRead() throws Exception {

        ConfigurationBuilder cb = new ConfigurationBuilder();
        Properties twitterProperties = new Properties();
        File twitter4jPropsFile = new File(System.getProperty("user.home") + "/twitter4j.properties");
        if (!twitter4jPropsFile.exists()
                && (Strings.isNullOrEmpty(twitterUser) || Strings.isNullOrEmpty(twitterPassword))) {
            logger.info(
                    "Cannot find twitter4j.properties file in this location :[{}], nor through inline parameters. Make sure there is a configuration file available at this place and includes user/password credentials, or that you are passing the twitter4j.user and twitter4j.password parameters to the node",
                    twitter4jPropsFile.getAbsolutePath());
            return;
        }
        if (twitter4jPropsFile.exists()) {
            twitterProperties.load(new FileInputStream(twitter4jPropsFile));
        }

        cb.setDebugEnabled(
                !Strings.isNullOrEmpty(twitter4jDebug) ? Boolean.valueOf(twitter4jDebug) : Boolean
                        .valueOf(twitterProperties.getProperty("debug")))
                .setUser(!Strings.isNullOrEmpty(twitterUser) ? twitterUser : twitterProperties.getProperty("user"))
                .setPassword(
                        !Strings.isNullOrEmpty(twitterPassword) ? twitterPassword : twitterProperties
                                .getProperty("password"));
        TwitterStream twitterStream = new TwitterStreamFactory(cb.build()).getInstance();
        StatusListener statusListener = new StatusListener() {

            @Override
            public void onException(Exception ex) {
                logger.error("error", ex);
            }

            @Override
            public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
                logger.error("error");
            }

            @Override
            public void onStatus(Status status) {
                messageQueue.add(status);

            }

            @Override
            public void onScrubGeo(long userId, long upToStatusId) {
                logger.error("error");
            }

            @Override
            public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
                logger.error("error");
            }
        };
        twitterStream.addListener(statusListener);
        twitterStream.sample();

    }

    @Override
    protected void onStart() {
        try {
            t.start();
            connectAndRead();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    class Dequeuer implements Runnable {

        @Override
        public void run() {
            while (true) {
                try {
                    Status status = messageQueue.take();
                    Event event = new Event();
                    event.put("statusText", String.class, status.getText());
                    getRemoteStream().put(event);
                } catch (Exception e) {

                }
            }

        }
    }
}
