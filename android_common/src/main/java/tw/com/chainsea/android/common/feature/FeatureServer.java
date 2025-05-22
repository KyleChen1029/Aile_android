package tw.com.chainsea.android.common.feature;

/**
 * current by evan on 2019-12-31
 */
//public class FeatureServer {
//    private static final String TAG = FeatureServer.class.getSimpleName();
//    private Context ctx;
//    private SplitInstallManager manager;
//    int CONFIRMATION_REQUEST_CODE = 1;
//
//
//    SplitInstallStateUpdatedListener listener = state -> {
//        boolean multiInstall = state.moduleNames().size() > 1;
//        boolean langsInstall = !state.languages().isEmpty();
//
//        if (langsInstall) {
//            String name = state.languages().get(0);
//        } else {
////                    state.moduleNames().joinToString(" - ");
//        }
//
//        switch (state.status()) {
//            case SplitInstallSessionStatus.DOWNLOADING:
//                Log.i(TAG, "DOWNLOADING");
//                break;
//            case SplitInstallSessionStatus.REQUIRES_USER_CONFIRMATION:
////                manager.startConfirmationDialogForResult(state, this, CONFIRMATION_REQUEST_CODE);
//                break;
//            case SplitInstallSessionStatus.INSTALLED:
//                Log.i(TAG, "INSTALLED");
//                if (langsInstall) {
////                    onSuccessfulLanguageLoad(names);
//                } else {
//                    onSuccessfulLoad("", true, null);
//                }
//                break;
//            case SplitInstallSessionStatus.INSTALLING:
//                Log.i(TAG, "INSTALLING");
//                break;
//            case SplitInstallSessionStatus.FAILED:
//                Log.i(TAG, "FAILED");
//                break;
//        }
//    };
//
//    private FeatureServer(Context ctx, boolean register) {
//        this.ctx = ctx;
//        this.manager = SplitInstallManagerFactory.create(ctx);
//        if (register) {
//            this.manager.registerListener(listener);
//        }
//    }
//
//    public static FeatureServer init(Context ctx, boolean register) {
//        return new FeatureServer(ctx, register);
//    }
//
//
//    public void register() {
//        this.manager.registerListener(listener);
//    }
//
//    public void unregister() {
//        this.manager.unregisterListener(listener);
//    }
//
//
//    private void launchActivity(String className, LaunchCallBack launchCallBack) {
//        if (launchCallBack != null) {
//            launchCallBack.onLaunch(className);
//        }
//    }
//
//    public void loadAndLaunchModule(String name, String packageName, LaunchCallBack launchCallBack) {
//        if (this.manager.getInstalledModules().contains(name)) {
//            onSuccessfulLoad(packageName, true, launchCallBack);
//            return;
//        }
//
//        // Create request to install a feature module by name.
//        SplitInstallRequest request = SplitInstallRequest.newBuilder()
//                .addModule(name)
//                .build();
//
//        // Load and install the requested feature module.
//        this.manager.startInstall(request);
//    }
//
//    private void onSuccessfulLoad(String moduleName, boolean launch, LaunchCallBack launchCallBack) {
//        if (launch) {
//            launchActivity(moduleName, launchCallBack);
//        }
//    }
//
//
//    public interface LaunchCallBack {
//        void onLaunch(String className);
//    }
//}
