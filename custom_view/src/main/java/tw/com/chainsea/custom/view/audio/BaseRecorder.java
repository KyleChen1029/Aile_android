package tw.com.chainsea.custom.view.audio;

/**
 * current by evan on 2020-08-26
 *
 * @author Evan Wang
 * @date 2020-08-26
 */
public abstract class BaseRecorder {

    protected int mVolume;

    public abstract int getRealVolume();

    /**
     * 此计算方法来自samsung开发范例
     *
     * @param buffer   buffer
     * @param readSize readSize
     */
    protected void calculateRealVolume(short[] buffer, int readSize) {
        double sum = 0;
        for (int i = 0; i < readSize; i++) {
            // 这里没有做运算的优化，为了更加清晰的展示代码
            sum += buffer[i] * buffer[i];
        }
        if (readSize > 0) {
            double amplitude = sum / readSize;
            mVolume = (int) Math.sqrt(amplitude);
        }
    }

}