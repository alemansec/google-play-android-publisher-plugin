package org.jenkinsci.plugins.googleplayandroidpublisher.internal;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.androidpublisher.AndroidPublisher;
import com.google.jenkins.plugins.credentials.oauth.GoogleRobotCredentials;
import hudson.FilePath;
import hudson.remoting.VirtualChannel;
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import jenkins.MasterToSlaveFileCallable;
import jenkins.model.Jenkins;
import net.dongliu.apk.parser.ApkParsers;
import net.dongliu.apk.parser.bean.ApkMeta;
import org.jenkinsci.plugins.googleplayandroidpublisher.AndroidPublisherScopeRequirement;
import org.jenkinsci.plugins.googleplayandroidpublisher.Util;

public class UtilsImpl implements JenkinsUtil, AndroidUtil {
    private static UtilsImpl sInstance;

    public static UtilsImpl getInstance() {
        if (sInstance == null) sInstance = new UtilsImpl();
        return sInstance;
    }

    // region JenkinsUtil

    @Override
    public String getPluginVersion() {
        final String version = Jenkins.getInstance().getPluginManager().whichPlugin(Util.class).getVersion();
        int index = version.indexOf(' ');
        return (index == -1) ? version : version.substring(0, index);
    }

    @Override
    public <R> R actOnPath(FilePath file, CheckedFunction<File, R> function) throws IOException, InterruptedException {
        return file.act(new MasterToSlaveFileCallable<R>() {
            @Override
            public R invoke(File f, VirtualChannel channel) throws IOException {
                return function.apply(f);
            }
        });
    }

    @Override
    public AndroidPublisher createPublisherClient(GoogleRobotCredentials credentials, String pluginVersion)
            throws GeneralSecurityException {
        final Credential credential = credentials.getGoogleCredential(new AndroidPublisherScopeRequirement());
        return new AndroidPublisher.Builder(credential.getTransport(), credential.getJsonFactory(), credential)
                .setApplicationName(String.format("Jenkins-GooglePlayAndroidPublisher/%s", pluginVersion))
                .build();
    }

    // endregion

    // region AndroidUtil

    @Override
    public ApkMeta getApkMetadata(File apk) throws IOException {
        return ApkParsers.getMetaInfo(apk);
    }

    // endregion
}
