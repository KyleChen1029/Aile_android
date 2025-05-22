package tw.com.chainsea.android.common.video;

import java.io.File;

/**
 * current by evan on 2020-03-05
 */
public interface IVideoSize {

    int width();

    int height();

    long size();

    String name();

    File file();

    String path();

    long duration();

}