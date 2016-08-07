package houde.zoomimageview;

import android.app.Application;

import com.orhanobut.logger.LogLevel;
import com.orhanobut.logger.Logger;

/**
 * Created by Administrator on 2016/8/6.
 */
public class AppContext extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Logger.init("qiu")                 // default PRETTYLOGGER or use just init()
                .methodCount(0)                 // default 2
                .hideThreadInfo()               // default shown
                .logLevel(LogLevel.FULL)        // default LogLevel.FULL
                .methodOffset(2)                // default 0
        ; //default AndroidLogAdapter


    }
}
