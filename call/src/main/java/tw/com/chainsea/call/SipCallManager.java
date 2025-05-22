package tw.com.chainsea.call;

import android.content.Context;
import android.media.AudioManager;
import android.util.Log;

import org.linphone.core.ErrorInfo;
import org.linphone.core.LinphoneAddress;
import org.linphone.core.LinphoneAuthInfo;
import org.linphone.core.LinphoneCall;
import org.linphone.core.LinphoneCallParams;
import org.linphone.core.LinphoneCore;
import org.linphone.core.LinphoneCoreException;
import org.linphone.core.LinphoneCoreFactory;
import org.linphone.core.LinphoneProxyConfig;

import tw.com.chainsea.android.common.log.CELog;
import tw.com.chainsea.call.base.LinphoneManager;
import tw.com.chainsea.call.base.OnLinphoneListener;

/**
 * PublicChat
 * tw.com.chainsea.call
 * Created by Andy on 2016/7/19.
 */
public class SipCallManager implements OnLinphoneListener {
    private LinphoneCore mLc = null;
    private String mUserId;
    private String mPasswd;
    private String mCallTo;
    private Listener mListener;
    private String mSipxUrl;
    private boolean isMute;
    private AudioManager mAudioManager;

    private static SipCallManager sInstance;

    public static SipCallManager getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new SipCallManager(context);
        }
        return sInstance;
    }

    public SipCallManager(Context context) {
        LinphoneManager.createAndStart(context, this);
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mLc = LinphoneManager.getLc();

        LinphoneCore.Transports transportPorts = mLc.getSignalingTransportPorts();//端口
        transportPorts.udp = 5046;
        transportPorts.tcp = 5046;
        transportPorts.tls = -1;
        mLc.setSignalingTransportPorts(transportPorts);
    }

    public void setUserInfo(String sipUrl, String userId, String pw, Listener listener) {
        mSipxUrl = sipUrl;
        mUserId = userId;
        mPasswd = pw;
        mListener = listener;
        CELog.d("name = " + mUserId + ", pw = " + mPasswd + ", host = " + mSipxUrl);
        Log.e("UserInfo", "name = " + mUserId + ", pw = " + mPasswd + ", host = " + mSipxUrl);
    }

    public void startCall(String to) {
        mCallTo = to;
        mLc.setNetworkReachable(true);
        try {
            registerUserAuth(mUserId, mPasswd, mSipxUrl);
        } catch (LinphoneCoreException e) {
            CELog.e("linphone register failed", e);
        }
    }

    private void registerUserAuth(String name, String password, String host) throws LinphoneCoreException {
        CELog.d("name = " + name + ", pw = " + password + ", host = " + host);
        String identity = "sip:" + name + "@" + host;
        String proxy = "sip:" + host;

        LinphoneAddress proxyAddr = LinphoneCoreFactory.instance().createLinphoneAddress(proxy);
        LinphoneAddress identityAddr = LinphoneCoreFactory.instance().createLinphoneAddress(identity);

        LinphoneAuthInfo authInfo = LinphoneCoreFactory.instance().createAuthInfo(name, null, password, null, null, host);
        LinphoneProxyConfig prxCfg = mLc.createProxyConfig(identityAddr.asString(), proxyAddr.asStringUriOnly(), proxyAddr.asStringUriOnly(), true);
        CELog.d("proxyAddr" + prxCfg.getContactParameters() + " " + prxCfg.getContactUriParameters() + " " + prxCfg.getDialPrefix() + " " + prxCfg.getDomain() + " " + prxCfg.getIdentity() + " " + prxCfg.getProxy() + " " + prxCfg.getQualityReportingCollector() + " " + prxCfg.getRealm() + " " + prxCfg.getRoute());
        prxCfg.enableAvpf(false);
        prxCfg.setAvpfRRInterval(0);
        prxCfg.enableQualityReporting(false);
        prxCfg.setQualityReportingCollector(null);
        prxCfg.setQualityReportingInterval(0);

        prxCfg.enableRegister(true);

        mLc.clearAuthInfos();
        mLc.clearProxyConfigs();

        mLc.addProxyConfig(prxCfg);
        mLc.addAuthInfo(authInfo);

        mLc.setDefaultProxyConfig(prxCfg);
    }

    @Override
    public void onRegState(LinphoneCore.RegistrationState registrationState, String reason) {
        CELog.d("registrationState = " + registrationState.toString() + ", reason = " + reason);
        if (registrationState == LinphoneCore.RegistrationState.RegistrationOk) {
            if (mCallTo != null) {
                setCallingTo(mCallTo, mSipxUrl);
            }
        } else if (registrationState == LinphoneCore.RegistrationState.RegistrationFailed) {
            CELog.e("register failed");
            mLc.setNetworkReachable(false);
            if (mListener != null) {
                mListener.onCallEnd();
            }
        }
    }

    @Override
    public void onCallState(LinphoneCall linphoneCall, LinphoneCall.State state, String reason) {
        CELog.d("registrationState = " + state.toString() + ", reason = " + reason);
        CELog.d("callState = " + state.toString() + ", reason = " + reason);
        if (state.toString() == LinphoneCall.State.CallEnd.toString()) {
            if (mListener != null) {
                mListener.onCallEnd();
            }
        } else if (state.toString() == LinphoneCall.State.Connected.toString()) {
            if (mListener != null) {
                mListener.onConnect();
            }
        } else if (state.toString() == LinphoneCall.State.StreamsRunning.toString()) {
            if (mListener != null) {
                mListener.onCalling();
            }
        } else if (state.toString() == LinphoneCall.State.Error.toString()) {
            ErrorInfo ei = linphoneCall.getErrorInfo();
            CELog.d(String.format("onCallState:Error = [\n\t{Details:%s},\n\t{Phrase:%s},\n\t{Protocol:%s},\n\t{ProtocolCode:%s}]"
                , ei.getDetails(), ei.getPhrase(), ei.getProtocol(), ei.getProtocolCode()));
            if (mListener != null) {
                mListener.onError(ei.getProtocolCode());
            }
        }
    }

    @Override
    public void onPaused() {
        hangup();
    }

    private void setCallingTo(String callto, String host) {
        CELog.d("registrationState = " + callto + "@" + host);
        LinphoneAddress lAddress;
        try {
            lAddress = mLc.interpretUrl(callto + "@" + host);
        } catch (LinphoneCoreException e) {
            CELog.d("registrationState = " + e);
            e.printStackTrace();
            return;
        }

        LinphoneCallParams params = mLc.createCallParams(mLc.getCurrentCall());

        //LinphoneCallParams params = mLc.createDefaultCallParameters();
        params.setVideoEnabled(true);
        try {
            mLc.inviteAddressWithParams(lAddress, params);
        } catch (LinphoneCoreException ignored) {
        }
    }

    public void enableSpeaker(boolean isSpeaker) {
        mLc.enableSpeaker(isSpeaker);
    }

    public void enableMute(boolean isMute) {
        this.isMute = isMute;
        mLc.muteMic(isMute);
//        mAudioManager.setStreamMute(AudioManager.STREAM_DTMF, isMute);
//        mAudioManager.setStreamMute(AudioManager.STREAM_VOICE_CALL, isMute);
    }

    public void release() {
        LinphoneManager.destroy();
    }

    public void sendDtmf(char c) {
        mLc.sendDtmf(c);
    }

    public boolean isMute() {
        return isMute;
    }

    public void hangup() {
        LinphoneCall currentCall = mLc.getCurrentCall();
        if (currentCall != null) {
            mLc.terminateCall(currentCall);
        } else if (mLc.isInConference()) {
            mLc.terminateConference();
        } else {
            mLc.terminateAllCalls();
        }
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    public interface Listener {
        void onCallEnd();

        void onConnect();

        void onCalling();

        void onError(int protocolCode);
    }
}
