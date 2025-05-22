package tw.com.chainsea.chat.messagekit.lib;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.primitives.Longs;

import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import tw.com.chainsea.chat.R;

/**
 * current by evan on 2020/5/6
 *
 * @author Evan Wang
 * date 2020/5/6
 */
public class MessageDomino {
    private String name;
    @ColorInt
    private int textColor;
    @DrawableRes
    int resId;
    @ColorInt
    private int color;

    public MessageDomino(String name, int textColor, int resId, int color) {
        this.name = name;
        this.textColor = textColor;
        this.resId = resId;
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public int getTextColor() {
        return textColor;
    }

    public int getColor() {
        return color;
    }

    public int getResId() {
        return resId;
    }

    public static Map<String, MessageDomino> getDominoData() {
        return dominoData;
    }

    public static TreeSet<Domino> getDominos() {
        return dominos;
    }

    public static Map<String, MessageDomino> dominoData = Maps.newHashMap();

    public static TreeSet<Domino> dominos = Sets.newTreeSet((o1, o2) -> Longs.compare(o1.index, o2.index));

    @Deprecated
    public static void init(int batchNumber) {
        dominos.clear();
        dominos.addAll(Lists.newArrayList(Domino.values()));

        if (batchNumber > 0) {
            for (Domino d : dominos) {
                d.setName(d.getName() + " " + batchNumber);
            }
        }
    }

    /**
     * // EVAN_FLAG 2020-09-05 (1.13.0) 遞增位移
     * 遞增位移
     */
    public static void init2(int batchNumber) {
        dominos.clear();
        List<Domino> list = Lists.newArrayList(Domino.values());
        for (int i = 0; i < Domino.values().length; i++) {
            Domino d = Domino.values()[i];
            int index = (i + batchNumber) % DominoName.values().length;
            if (batchNumber == 0) {
                d.setName(DominoName.values()[index].getName());
            } else {
                d.setName(DominoName.values()[index].getName() + " " + batchNumber);
            }
        }
        dominos.addAll(list);
    }

    public static TreeSet<Domino> getDomino(int batchNumber) {
        if (dominos.isEmpty()) {
            List<Domino> list = Lists.newArrayList(Domino.values());
            for (int i = 0; i < Domino.values().length; i++) {
                Domino d = Domino.values()[i];
                int index = (i + batchNumber) % DominoName.values().length;
                if (batchNumber == 0) {
                    d.setName(DominoName.values()[index].getName());
                } else {
                    d.setName(DominoName.values()[index].getName() + " " + batchNumber);
                }
            }
            dominos.addAll(list);
        }
        return dominos;
    }

    public static void clear() {
        dominoData.clear();
        dominos.clear();
    }

    public static int getBatchNumber() {
        return MessageDomino.dominoData.size() / MessageDomino.Domino.values().length;
    }

    public enum Domino {
        H_00(0, "一杯咖啡的時間", R.drawable.hide_00),
        H_01(1, "大太陽", R.drawable.hide_01),
        H_02(2, "大象", R.drawable.hide_02),
        H_03(3, "小雨傘", R.drawable.hide_03),
        H_04(4, "小熊", R.drawable.hide_04),
        H_05(5, "小禮物", R.drawable.hide_05),
        H_06(6, "公事包", R.drawable.hide_06),
        H_07(7, "包粽包中", R.drawable.hide_07),
        H_08(8, "可愛花朵", R.drawable.hide_08),
        H_09(9, "白雲飄飄", R.drawable.hide_09),
        H_10(10, "夾腳拖", R.drawable.hide_10),
        H_11(11, "我要上火星", R.drawable.hide_11),
        H_12(12, "來杯清涼", R.drawable.hide_12),
        H_13(13, "旺旺鳳梨", R.drawable.hide_13),
        H_14(14, "計時開始", R.drawable.hide_14),
        H_15(15, "海島", R.drawable.hide_15),
        H_16(16, "海豹", R.drawable.hide_16),
        H_17(17, "紙飛機", R.drawable.hide_17),
        H_18(18, "草莓", R.drawable.hide_18),
        H_19(19, "清涼西瓜", R.drawable.hide_19),
        H_20(20, "尋找", R.drawable.hide_20),
        H_21(21, "游泳圈", R.drawable.hide_21),
        H_22(22, "猴子", R.drawable.hide_22),
        H_23(23, "筆", R.drawable.hide_23),
        H_24(24, "新創意", R.drawable.hide_24),
        H_25(25, "熱汽球", R.drawable.hide_25),
        H_26(26, "機器人", R.drawable.hide_26),
        H_27(27, "蘋果", R.drawable.hide_27),
        H_28(28, "鑽石", R.drawable.hide_28),
        H_29(29, "Ok囉", R.drawable.hide_29);

        Domino(int index, String name, int resId) {
            this.index = index;
            this.name = name;
            this.resId = resId;
        }

        int index;
        String name;
        @DrawableRes int resId;

        public int getIndex() {
            return index;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getResId() {
            return resId;
        }
    }

    public enum DominoName {
        H_00("一杯咖啡的時間"),
        H_01("大太陽"),
        H_02("大象"),
        H_03("小雨傘"),
        H_04("小熊"),
        H_05("小禮物"),
        H_06("公事包"),
        H_07("包粽包中"),
        H_08("可愛花朵"),
        H_09("白雲飄飄"),
        H_10("夾腳拖"),
        H_11("我要上火星"),
        H_12("來杯清涼"),
        H_13("旺旺鳳梨"),
        H_14("計時開始"),
        H_15("海島"),
        H_16("海豹"),
        H_17("紙飛機"),
        H_18("草莓"),
        H_19("清涼西瓜"),
        H_20("尋找"),
        H_21("游泳圈"),
        H_22("猴子"),
        H_23("筆"),
        H_24("新創意"),
        H_25("熱汽球"),
        H_26("機器人"),
        H_27("蘋果"),
        H_28("鑽石"),
        H_29("Ok囉");

        String name;

        DominoName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
