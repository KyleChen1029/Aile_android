package tw.com.chainsea.chat.keyboard.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.Toast;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.collect.Range;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.android.common.log.CELog;
import tw.com.chainsea.ce.sdk.bean.InputLogBean;
import tw.com.chainsea.ce.sdk.bean.InputLogType;
import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity;
import tw.com.chainsea.ce.sdk.bean.account.UserType;
import tw.com.chainsea.ce.sdk.bean.msg.MessageType;
import tw.com.chainsea.ce.sdk.bean.msg.content.TextContent;
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity;
import tw.com.chainsea.ce.sdk.cache.ChatMemberCacheService;

@SuppressLint("AppCompatCustomView")
public class HadEditText extends EditText implements TextWatcher {
    private static final String TAG = HadEditText.class.getSimpleName();
    private Context ctx;
    private OnTextChangedInterface onTextChangedInterface;

    private ChatRoomEntity entity;

    private final LinkedList<SelectData> selectData = Lists.newLinkedList();
    private static final String TRIGGER_WORD = "@";

    public HadEditText(Context context) {
        this(context, null);
        this.ctx = context;
    }

    public HadEditText(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        this.ctx = context;
    }

    public HadEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.ctx = context;
        initView();
    }

    private void initView() {
        addTextChangedListener(this);
        LinkedList<UserProfileEntity> members = null;
        setFilters(new InputFilter[]{(source, start, end, dest, dstart, dend) -> source});
    }

    public void setChatRoomEntity(ChatRoomEntity entity, boolean isMentionMode, boolean isAddAll) {
        this.entity = entity;
        this.MENTION_MODE_OPEN = isMentionMode;
        this.IS_ADD_ALL = isAddAll;
        if (!isMentionMode) {
            return;
        }
        // 解決剛進入聊天室且有標註未編輯完成內容導致標註模式被開啟問題
//        postDelayed(() -> IS_READY = true, 2000l);
        IS_READY = true;
        if (entity != null) makeUpMemberList(false, this.entity.getId());
    }

    public void refreshMemberList() {
        if (this.onTextChangedInterface != null) {
            this.onTextChangedInterface.onHideMention(this, "");
        }
        if (this.entity != null) {
            String roomId = this.entity.getId();
            ChatMemberCacheService.refresh(roomId);
            List<UserProfileEntity> members = ChatMemberCacheService.getChatMember(roomId);
            this.entity.setMembers(members);
        }
        makeUpMemberList(true, this.entity.getId());
    }

    public HadEditText setReady(boolean isReady) {
        this.IS_READY = isReady;
        return this;
    }

    public HadEditText setUsingAtInterFace(boolean isUsingAtInterFace) {
        IS_USING_AT_INTERFACE = isUsingAtInterFace;
        return this;
    }


    // 是否增加標註全部
    boolean IS_ADD_ALL = true;
    // 記錄改變之前文字長度，用於判斷是否為一次複製貼上
    int BEFORE_COUNT = 0;
    // 是否正在退格
    boolean IS_LOWER = false;
    // 是否正在增加文字
    boolean IS_PLUS = false;
    // 是否進入選單模式
    boolean MENTION_MODE_OPEN = false;
    // 是否正在編輯模式
    boolean FILTER_MODE_OPEN = false;
    // 準備刪除模式
    boolean DELETE_MODE_OPEN = false;
    // 是否進入多選模式
    boolean IS_MULTI_SELECT = false;
    // 是否正在開啟At介面
    boolean IS_USING_AT_INTERFACE = false;
    // 當前游標位置
    int currentCursorPosition = -1;
//    int currentLength = -1;

    int deleteStart = -1;
    int filterStart = -1;
    int filterEnd = -1;
    String filterTmp = "";
    boolean isAdd = false;
    // 是否準備完成，因為線程關係導致有未編輯完成內標註會先跳出選單
    boolean IS_READY = false;


    /**
     * 若已經進入標註選單及過濾模式，當遇到游標移動位置則取消該模式狀態
     */
    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        super.onSelectionChanged(selStart, selEnd);
        // EVAN_FLAG 2020-06-19 (1.11.0) 當游標到達AT物件
//        for (SelectData d : selectData) {
//            Range<Integer> range = d.getRange();
//            int lower = range.lowerEndpoint();
//            int upper = range.upperEndpoint();
//            if (selEnd > lower && selEnd < upper && selEnd != upper) {
//                try {
//                    setSelection(range.upperEndpoint());
//                } catch (Exception e) {
//
//                }
//                return;
//            }
//        }

        if (MENTION_MODE_OPEN && FILTER_MODE_OPEN && !this.IS_MULTI_SELECT && !IS_USING_AT_INTERFACE) {
            if (filterEnd + 1 != selStart) {
                filterStart = -1;
                filterEnd = -1;
                FILTER_MODE_OPEN = false;

                if (this.onTextChangedInterface != null) {
                    this.onTextChangedInterface.onHideMention(HadEditText.this, "");
                }
            }
        }

        if (this.IS_MULTI_SELECT && currentCursorPosition != selStart) {
            currentCursorPosition = getSelectionEnd();
            filterStart = -1;
            filterEnd = -1;
            FILTER_MODE_OPEN = false;

            if (this.onTextChangedInterface != null) {
                this.onTextChangedInterface.onHideMention(HadEditText.this, "");
            }
        }

        if (this.onTextChangedInterface != null) {
            this.onTextChangedInterface.onSelectionChanged(selStart, selEnd);
        }
    }

    @Override
    public void onEditorAction(int actionCode) {
        super.onEditorAction(actionCode);
    }


    public void clearMode() {
        if (MENTION_MODE_OPEN) {
            FILTER_MODE_OPEN = false;
            filterEnd = -1;
            filterStart = -1;
            deleteStart = -1;
        }
    }


//    private SpannableStringBuilder builder = new SpannableStringBuilder();

    /**
     * 改變之前
     */
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//        Log.w(TAG, String.format("start: %s\n count: %s\n after: %s", start, count, after));
//        builder = new SpannableStringBuilder(s);
        BEFORE_COUNT = s.length();
        IS_LOWER = after < count;
        IS_PLUS = after > count;

        if (!FILTER_MODE_OPEN && this.onTextChangedInterface != null) {
            this.onTextChangedInterface.onHideMention(this, "");
            IS_MULTI_SELECT = false;
        }

        if (!FILTER_MODE_OPEN) {
            filterEnd = -1;
            filterStart = -1;
        }

        if (MENTION_MODE_OPEN && FILTER_MODE_OPEN) {
            if (IS_PLUS) {
                filterEnd++;
            }
            if (IS_LOWER) {
                filterEnd--;
            }
        }

        if (MENTION_MODE_OPEN && IS_LOWER && !FILTER_MODE_OPEN) {
            DELETE_MODE_OPEN = true;
            deleteStart = start;
        }

        if (this.onTextChangedInterface != null) this.onTextChangedInterface.onBeforeTextChanged(s, start, count, after);
    }


    boolean isDelete(int index) {
        for (SelectData data : this.selectData) {
            if (data.getRange().upperEndpoint() - 1 == index) {
                return true;
            }
        }
        return false;
    }

    /**
     * 改變中
     */
    @Override
    public void onTextChanged(CharSequence arg0, int start, int lengthBefore, int after) {
//        builder = new SpannableStringBuilder(arg0);

        if (arg0.length() - BEFORE_COUNT > 1) {
            FILTER_MODE_OPEN = false;
            filterStart = -1;
            filterEnd = -1;
            if (this.onTextChangedInterface != null && !IS_MULTI_SELECT) {
                this.onTextChangedInterface.onHideMention(HadEditText.this, "");
            }
        }

        // 如果輸入換行直接取消模式
        if (arg0.length() > 0) {
            try {
                CharSequence trim = arg0.subSequence(arg0.length() - 1, arg0.length());
                if ("\n".equals(trim.toString())) {
                    FILTER_MODE_OPEN = false;
                    filterStart = -1;
                    filterEnd = -1;
                    if (this.onTextChangedInterface != null) {
                        this.onTextChangedInterface.onHideMention(HadEditText.this, "");
                    }
                }
            } catch (Exception ignored) {

            }
        }

        //被取消過濾模式
        if (MENTION_MODE_OPEN && (filterEnd < filterStart || filterEnd > arg0.length())) {
//            if (MENTION_MODE_OPEN && (filterEnd < filterStart || filterEnd < 0 || filterEnd > arg0.length())) {
            FILTER_MODE_OPEN = false;
            filterStart = -1;
            filterEnd = -1;
            if (this.onTextChangedInterface != null) {
                this.onTextChangedInterface.onHideMention(HadEditText.this, "");
                IS_USING_AT_INTERFACE = false;
            }
        }

        //過濾中資料
        if (MENTION_MODE_OPEN && FILTER_MODE_OPEN) {
            try {
                String filter = arg0.subSequence(filterStart + 1, filterEnd + 1).toString();
                if (this.onTextChangedInterface != null && IS_READY) {
                    if ((IS_USING_AT_INTERFACE && !Strings.isNullOrEmpty(filter)) || (IS_LOWER && Strings.isNullOrEmpty(filter))) {
                        this.onTextChangedInterface.onShowMention(entity.getMembersLinkedList(), HadEditText.this, IS_MULTI_SELECT, filter);
                    }

                    IS_USING_AT_INTERFACE = true;
                }
            } catch (Exception ignored) {

            }
        }

        if (MENTION_MODE_OPEN && IS_PLUS && lengthBefore == 0 && start + 1 <= arg0.length()) {
//            if (MENTION_MODE_OPEN && IS_PLUS && lengthBefore == 0 && !FILTER_MODE_OPEN && start + 1 <= arg0.length()) {
            String at = arg0.subSequence(start, start + 1).toString();
            if (TRIGGER_WORD.equals(at)) {
                FILTER_MODE_OPEN = true;
                filterStart = start;
                filterEnd = start;
                if (this.onTextChangedInterface != null && IS_READY && !IS_MULTI_SELECT) {
                    this.onTextChangedInterface.onHideMention(HadEditText.this, "");
                    this.onTextChangedInterface.onShowMention(entity.getMembersLinkedList(), HadEditText.this, IS_MULTI_SELECT, "");
                    IS_USING_AT_INTERFACE = true;
                }
            }
        }

        if (this.onTextChangedInterface != null) {
            if (arg0.length() > 10000) {
                Toast.makeText(this.ctx, "您輸入的字數超過傳送限制。(" + arg0.length() + ">10000)", Toast.LENGTH_SHORT).show();
                this.onTextChangedInterface.onTextChanged(arg0, false);
            } else {
                this.onTextChangedInterface.onTextChanged(arg0, true);
            }
        }
    }


    /**
     * 改變結束
     */
    @Override
    public void afterTextChanged(Editable s) {
        if (DELETE_MODE_OPEN && !FILTER_MODE_OPEN) {
            DELETE_MODE_OPEN = false;
            try {
                removeMentionSelect(s, deleteStart);
            } catch (RuntimeException e) {
                CELog.e(TAG, e.getMessage());
            }
            deleteStart = -1;
        }

        if (MENTION_MODE_OPEN) {
            try {
                processingUUIDByUserNameAndDecorationText(0xFF4A90E2, s, TRIGGER_WORD, entity == null ? Lists.newLinkedList() : entity.getMembersLinkedList());
//                processingUserNameByUUIDs(builder, TRIGGER_KEY, entity.getMembersTable());
//                if (IS_USING_AT_INTERFACE) {
//                    processingUUIDByUserNameAndDecorationText(0xFF4A90E2, s, TRIGGER_KEY, entity == null ? Lists.newLinkedList() : entity.getMembersLinkedList());
//                }
            } catch (RuntimeException e) {
                CELog.e(TAG, e.getMessage());
            }
        }
    }

    /**
     * 刪除輸入框中被標註的對象
     */
    private void removeMentionSelect(Editable s, int start) throws RuntimeException {
        try {
            for (SelectData data : this.selectData) {
                Range<Integer> range = data.getRange();
                if (range.contains(start + 1)) {
                    removeTextChangedListener(this);
                    s.replace(range.lowerEndpoint(), range.upperEndpoint() - 1, "");
                    filterStart = -1;
                    filterEnd = -1;
                    FILTER_MODE_OPEN = false;
                    post(() -> setSelection(range.lowerEndpoint()));
                    addTextChangedListener(this);
                    break;
                }
            }
        } catch (Exception e) {
            addTextChangedListener(this);
            throw new RuntimeException(e);
        }
    }

    //    停止震動回饋
    @Override
    public void setSelection(int index) {
        currentCursorPosition = index;
        // EVAN_FLAG 2020-06-19 (1.11.0) 當游標到達AT物件
//        for (SelectData d : selectData) {
//            Range<Integer> range = d.getRange();
//            int lower = range.lowerEndpoint();
//            int upper = range.upperEndpoint();
//            if (index > lower && index < upper && index != upper) {
//                Editable editable = getText();
//                try {
//                    char last = editable.charAt(upper + 1);
//                    if (!" ".equals(String.valueOf(last))) {
//                        editable.insert(upper, " ");
//                        processingUUIDByUserNameAndDecorationText(0xFF4A90E2, editable, TRIGGER_KEY, entity == null ? Lists.newLinkedList() : entity.getMembersLinkedList());
//                    }
//                } catch (Exception e) {
//
//                }
//
//                try {
//                    setSelection(upper);
//                }catch (Exception e) {
//
//                }
//                return;
//            }
//        }
        try {
            super.setSelection(index);
        } catch (Exception ignored) {
        }
    }

    @Override
    public void setOnEditorActionListener(OnEditorActionListener l) {
        super.setOnEditorActionListener(l);
    }


    /**
     * 該聊天室成員資料，且收到用戶資料更新重新取得
     */
    public void makeUpMemberList(boolean isClear, String roomId) {
        if (MENTION_MODE_OPEN && isClear && this.entity != null && this.entity.getMembers() != null) {
            FILTER_MODE_OPEN = false;
            filterStart = -1;
            filterEnd = -1;
            deleteStart = -1;
            if (this.onTextChangedInterface != null) {
                this.onTextChangedInterface.onHideMention(this, "");
                IS_USING_AT_INTERFACE = false;
            }

            this.entity.getMembers().clear();
            List<UserProfileEntity> members = ChatMemberCacheService.getChatMember(roomId);
            this.entity.getMembers().addAll(members);
            makeUpMemberListToRemote();
        } else if (MENTION_MODE_OPEN && this.entity != null) {
            makeUpMemberListToRemote();
        }
    }

    /**
     * 從Server取得該聊天室成員資料，並增加 ALL
     */
    private void makeUpMemberListToRemote() {
        if (this.IS_ADD_ALL) {
            addAllData(this.entity.getMembers());
        }

        List<UserProfileEntity> members = ChatMemberCacheService.getChatMember(this.entity.getId());

        for (UserProfileEntity profile : members) {
            if (!entity.getMembers().contains(profile)) {
                entity.getMembers().add(profile);
            }
        }
        if (onTextChangedInterface != null) {
            onTextChangedInterface.onNotifyMentionDataChanged(entity.getMembersLinkedList());
        }

    }


    /**
     * 增加標註全部(ALL)項目資料
     */
    public void addAllData(List<UserProfileEntity> userProfiles) {
        UserProfileEntity allProfile = UserProfileEntity.Build()
            .id("00000000-0000-0000-0000-000000000000")
            .avatarId("ALL")
            .alias("ALL")
            .userType(UserType.EMPLOYEE)
//                .userType("employee")
            .nickName("ALL")
            .build();
        if (!userProfiles.contains(allProfile)) {
            userProfiles.add(0, allProfile);
        }
    }

    /**
     * EVAN_FLAG 2019-12-23 (1.8.1) 增加選擇名稱直接幫忙加入標註
     */
    public void appendMentionSelectById(String id) {
        if (MENTION_MODE_OPEN) {
            Iterator<UserProfileEntity> iterator = entity.getMembers().iterator();
            while (iterator.hasNext()) {
                UserProfileEntity profile = iterator.next();
                if (id.equals(profile.getId())) {
                    FILTER_MODE_OPEN = true;
                    filterStart = getSelectionStart();
                    filterEnd = getSelectionEnd() - 1;
                    appendMentionSelect(profile, false, false);
                    break;
                }
            }
        }
    }


    /**
     * 增加被選取對象的UUID到輸入框中
     */
    public void appendMentionSelect(UserProfileEntity profile, boolean isMultiSelect, boolean needCalculatePosition) {
        this.IS_MULTI_SELECT = isMultiSelect;

        Editable editable = getText();
        // 確保文本對象不為空
        if (editable == null) {
            return;
        }

        // 獲取當前文本長度
        int textLength = editable.length();

        if (needCalculatePosition) {
            FILTER_MODE_OPEN = true;
            filterTmp = "";
            // 安全設置選擇起始位置
            filterStart = Math.min(Math.max(0, getSelectionStart()), textLength);
            // 安全設置選擇結束位置
            int selectionEnd = getSelectionEnd();
            filterEnd = (selectionEnd > 0) ? Math.min(selectionEnd - 1, textLength - 1) : -1;
        } else {
            int select = getSelectionEnd() - 1;
            // 檢查選擇位置是否有效
            if (select >= 0 && select < textLength) {
                char t = editable.charAt(select);
                if (t == '@') {
                    removeTextChangedListener(this);
                    editable.delete(select, select + 1);
                    addTextChangedListener(this);
                    FILTER_MODE_OPEN = true;
                    filterTmp = "";
                    filterStart = Math.min(getSelectionStart(), textLength);
                    filterEnd = Math.min(Math.max(getSelectionEnd() - 1, -1), textLength - 1);
                }
            }
        }

        if (this.onTextChangedInterface != null && !isMultiSelect) {
            this.onTextChangedInterface.onHideMention(HadEditText.this, "");
            IS_USING_AT_INTERFACE = false;
        }

        String name = !Strings.isNullOrEmpty(profile.getAlias()) ? profile.getAlias() : profile.getNickName();

        if (FILTER_MODE_OPEN) {
            removeTextChangedListener(this);
            filterEnd = filterEnd == -1 ? filterStart - 1 : filterEnd;

            // 關鍵優化：確保替換的範圍在有效範圍內
            int start = Math.max(0, Math.min(filterStart, textLength));
            int end = Math.max(start, Math.min(filterEnd + 1, textLength));

            isAdd = true;
            addTextChangedListener(this);
            String replace = "@" + name + " ";

            // 只有當開始和結束位置有效且開始位置小於結束位置時才執行替換
            if (start < end) {
                editable.replace(start, end, replace);
                currentCursorPosition += this.IS_MULTI_SELECT ? replace.length() : 0;
            } else {
                // 如果範圍無效，直接在當前位置插入
                editable.insert(start, replace);
                currentCursorPosition += this.IS_MULTI_SELECT ? replace.length() : 0;
            }

            filterTmp = "";
        }
        FILTER_MODE_OPEN = false;
        filterEnd = -1;
        if (this.onTextChangedInterface != null && this.entity != null && !isMultiSelect) {
            this.onTextChangedInterface.onHideMention(HadEditText.this, "");
            IS_USING_AT_INTERFACE = false;
        }
    }

//    /**
//     * 處理輸入框中的UUID，轉換成 @+名稱
//     *
//     * @param text
//     * @param prefix
//     * @param membersTable
//     */
//    private void processingUserNameByUUIDs(Editable text, String prefix, Map<String, String> membersTable) throws RuntimeException {
//        try {
//            removeTextChangedListener(this);
//            String result = text.toString();
//            Matcher m = UUID_PATTERN.matcher(result);
//            while (m.find()) {
//                String id = m.group();
//                String name = membersTable.get(id);
//                if (Strings.isNullOrEmpty(name)) {
//                    name = "未知";
//                }
//                result = result.replace(id, prefix + name + " ");
//            }
//
//            text.replace(0, text.length(), result);
//            addTextChangedListener(this);
//

    /// /            while (m.find()) {
    /// /                String id = m.group();
    /// /                int start = m.start();
    /// /                int end = m.end();
    /// /
    /// /                String name = membersTable.get(id);
    /// /                if (Strings.isNullOrEmpty(name)) {
    /// /                    name = "未知";
    /// /                }
    /// /                text.replace(start, end, prefix + name + " ");
    /// /            }
    /// /            addTextChangedListener(this);
//        } catch (Exception e) {
//            Log.e(TAG, e.getMessage());
//            throw new RuntimeException(e);
//        }
//    }
    private static void sort(LinkedList<UserProfileEntity> users) {
        try {
//            Ordering
//            Collections.sort(users, (o1, o2) -> {
//                String o1Name = !Strings.isNullOrEmpty(o1.getAlias()) ? o1.getAlias() : o1.getNickName();
//                String o2Name = !Strings.isNullOrEmpty(o2.getAlias()) ? o2.getAlias() : o2.getNickName();
//                CELog.i("userL:: " + o1Name + " , userR:: " + o2Name);
//                return ComparisonChain.start()
//                        .compare(o2Name.length() * 1.0d, o1Name.length() * 1.11d)
////                        .compare(o2Name.length(), o1Name.length())
//                        .result();
//            });


            Collections.sort(users,
                Ordering.natural().reverse().nullsFirst()
                    .onResultOf((Function<UserProfileEntity, Double>) p -> {
                        String o1Name = !Strings.isNullOrEmpty(p.getAlias()) ? p.getAlias() : p.getNickName();
                        return o1Name.length() * 1.0d;
                    })
            );

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 處理輸入框中有被標註的對象成高亮文字及所在位置範圍(index range)
     */
    public void processingUUIDByUserNameAndDecorationText(int color, Editable text, String prefix, LinkedList<UserProfileEntity> users) throws RuntimeException {
//        long time = System.currentTimeMillis();
        // 判斷 users 裡面 有沒有 "ALL" 的 user，如果沒有 加一個進去以便判斷是否有 @ALL
        addAllData(users);
        int count = 0;
        int currentCount = this.selectData.size();
        try {
//            removeTextChangedListener(this);
            text.setSpan(new ForegroundColorSpan(0xFF000000), 0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            this.selectData.clear();
            sort(users);
            for (UserProfileEntity profile : users) {
                String name = (!Strings.isNullOrEmpty(profile.getAlias()) ? profile.getAlias() : profile.getNickName());
                String quote = Pattern.quote(prefix + name + " ");
                String wordReg = "(?i)" + quote;//用(?i)来忽略大小写
                Matcher m = Pattern.compile(wordReg).matcher(text);

                while (m.find()) {
                    int start = m.start();
                    int end = m.end();
                    this.selectData.add(
                        new SelectData(profile.getId(), name, end > start ? Range.closed(start, end) : Range.closed(-2, -1)));

                    removeTextChangedListener(this);
                    text.replace(start + 1, end, name + " ");
                    addTextChangedListener(this);

                    text.setSpan(new ForegroundColorSpan(color), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    count++;
                }
            }
//            addTextChangedListener(this);
        } catch (Exception ignored) {
        }
//        CELog.w(String.format("auto select using ::: %s", ((System.currentTimeMillis() - time) / 1000.0)) + "/s");
        if (currentCount != count) {
            FILTER_MODE_OPEN = false;
            filterStart = -1;
            filterEnd = -1;
            deleteStart = -1;
//            CELog.w(String.format("auto select use time ::: %s", ((System.currentTimeMillis() - time) / 1000.0)) + "/s");
        }
    }

    /**
     * 依照被標註範圍排序位置
     */
    private static void sort(List<SelectData> selectData) {
        Collections.sort(selectData, (o1, o2) -> ComparisonChain.start()
            .compare(o1.getRange().lowerEndpoint(), o2.getRange().lowerEndpoint())
            .result());
    }

    /**
     * 排除重疊位置範圍資料
     */
    private void filter(List<SelectData> selectData) {
        Map<Integer, SelectData> result = Maps.newHashMap();
        for (SelectData data : selectData) {
            SelectData dataTmp = result.get(data.getRange().lowerEndpoint());
            if (dataTmp == null) {
                result.put(data.getRange().lowerEndpoint(), data);
            } else {
                int tmpEnd = dataTmp.getRange().upperEndpoint();
                int end = data.getRange().upperEndpoint();
                if (tmpEnd > end) {
                    result.put(data.getRange().lowerEndpoint(), dataTmp);
                }
            }
        }

        this.selectData.clear();
        for (Map.Entry<Integer, SelectData> entry : result.entrySet()) {
            this.selectData.add(entry.getValue());
        }

    }

    /**
     * 取出輸入框中Content 成物件格式
     */
    public SendData getTextData() {
        Editable editable = getText();
        filter(this.selectData);
        sort(this.selectData);
        String result = editable.toString();
        if (!this.selectData.isEmpty()) {
            try {
                String data = composeAtData(result, this.selectData);
                return new SendData(MessageType.AT, data);
            } catch (Exception e) {
                return new SendData(MessageType.TEXT, result);
            }
        } else {
            return new SendData(MessageType.TEXT, result);
        }
    }

    /**
     * 組裝 送出@AT資料格式
     */
    private static String composeAtData(String result, LinkedList<SelectData> selectData) throws Exception {
        List<AtMentionComponent> list = Lists.newArrayList();
        AtMentionComponent tmp = AtMentionComponent.newInstance();
        List<String> ids;
        int logIndex = selectData.size() - 1;
        int length = result.length();

        for (int i = selectData.size() - 1; i >= 0; i--) {
            SelectData data = selectData.get(i);
            Range<Integer> range = data.getRange();
            if (logIndex == i) {
                ids = findContinuousIds(i, data, Lists.newArrayList(), selectData);
                logIndex = logIndex - ids.size(); // EVAN_FLAG 2019-12-06 (1.8.0) 目前處理完成的位置紀錄

                if (ids.size() == 1 && "00000000-0000-0000-0000-000000000000".equals(ids.get(0))) {
                    tmp.setObjectType("All");
                    ids = Lists.newArrayList();
                } else {
                    tmp.setObjectType("User");
                }

                String content = getContent(result, range.upperEndpoint(), length);
                tmp.setUserIds(ids);
                tmp.setContent(new TextContent(content));
                list.add(0, tmp);
                tmp = AtMentionComponent.newInstance();
            }

            length = range.lowerEndpoint();

            if (i == 0) { // EVAN_FLAG 2019-12-06 (1.8.0) 判對第一筆AT之前還有沒有文字
                if (range.lowerEndpoint() != 0 && result.length() >= range.upperEndpoint()) {
                    String content = result.substring(0, range.lowerEndpoint());
                    if (!Strings.isNullOrEmpty(content)) {
                        tmp.setObjectType("");
                        tmp.setUserIds(Lists.newArrayList());
                        tmp.setContent(new TextContent(content));
                        list.add(0, tmp);
                        tmp = AtMentionComponent.newInstance();
                    }
                }
            }
        }
        return JsonHelper.getInstance().toJson(list);
    }

    /**
     * 找 ＠AT 下的 content
     */
    private static String getContent(String str, int start, int end) throws Exception {
        return str.substring(start, end);
    }

    /**
     * 找關連連續的 @AT ids
     */
    private static List<String> findContinuousIds(int index, SelectData data, List<String> ids, List<SelectData> selectData) throws Exception {
        String id = data.getId();
        Range<Integer> range = data.getRange();
        if ("00000000-0000-0000-0000-000000000000".equals(data.getId())) {
            return Lists.newArrayList(id);
        }
        ids.add(0, id);
        if (index != 0) {
            index--;
            SelectData lastData = selectData.get(index);
            Range<Integer> lastRange = lastData.getRange();
            if (range.lowerEndpoint() - lastRange.upperEndpoint() > 0) {
                return ids;
            } else {
                findContinuousIds(index, lastData, ids, selectData);
            }
        }
        return ids;
    }


    /**
     * // EVAN_FLAG 2020-05-21 (1.11.0) 取得未完成編輯的內容(物件化)
     */
    public InputLogBean getUnfinishedEditBean() {
        String text = getText().toString();
        InputLogBean.InputLogBeanBuilder builder = InputLogBean.Build().text(text);
        if (this.selectData != null && !this.selectData.isEmpty()) {
            return builder.type(InputLogType.AT).build();
        } else {
            return builder.type(InputLogType.TEXT).build();
        }
//        if (Strings.isNullOrEmpty(text) || this.selectData.isEmpty()) {
//            return builder.type(InputLogType.TEXT).build();
//        } else {
//            return builder.type(InputLogType.AT).build();
//        }
    }
//
//    /**
//     * 取得未完成編輯的內容
//     *
//     * @return
//     */
//    public String getUnfinishedEditContent() {
//        String text = getText().toString();
//        if (Strings.isNullOrEmpty(text) || !MENTION_MODE_OPEN) {
//            return text;
//        }
//
//        // EVAN_FLAG 2019-12-16 (1.8.0) 暫時不支持未編輯完成物件化資料內容

    /// /        String result = text;
    /// /        for (UserProfileEntity profile : entity.getMembers()) {
    /// /            String name = !Strings.isNullOrEmpty(profile.getNickName()) ? profile.getNickName() : profile.getLoginName();
    /// /            String quote = Pattern.quote(TRIGGER_KEY + name + "");
    /// /
    /// /            result = result.replace(TRIGGER_KEY + name + "", profile.getId());
    /// /
    /// /
    /// /            Pattern p = Pattern.compile(quote);
    /// /            Matcher m = p.matcher(text);
    /// /
    /// /            level:
    /// /            while (m.find()) {
    /// /
    /// /            }
    /// /        }
//
//        return text;
//    }

    public static class AtMentionComponent {
        private String type;
        private String objectType;
        private List<String> userIds;
        private TextContent content;

        public AtMentionComponent(String type, String objectType, List<String> userIds, TextContent content) {
            this.type = type;
            this.objectType = objectType;
            this.userIds = userIds;
            this.content = content;
        }

        public static AtMentionComponent newInstance() {
            return new AtMentionComponent(MessageType.TEXT.getType(), "User", Lists.newArrayList(), new TextContent(""));
        }

        public String getType() {
            return type;
        }

        public String getObjectType() {
            return objectType;
        }

        public List<String> getUserIds() {
            return userIds;
        }

        public TextContent getContent() {
            return content;
        }

        public void setType(String type) {
            this.type = type;
        }

        public void setObjectType(String objectType) {
            this.objectType = objectType;
        }

        public void setUserIds(List<String> userIds) {
            this.userIds = userIds;
        }

        public void setContent(TextContent content) {
            this.content = content;
        }
    }

    public static class AtTextComponent {
        private String text;

        public String toJson() {
            return JsonHelper.getInstance().toJson(this);
        }

        public String getText() {
            return text;
        }
    }

    public static class SendData {
        private MessageType type;
        private String content;

        public SendData(MessageType type, String content) {
            this.type = type;
            this.content = content;
        }

        public MessageType getType() {
            return type;
        }

        public String getContent() {
            return content;
        }
    }

    public static class SelectData {
        private final String id;
        private final String name;
        private final Range<Integer> range;

        public SelectData(String id, String name, Range<Integer> range) {
            this.id = id;
            this.name = name;
            this.range = range;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public Range<Integer> getRange() {
            return range;
        }
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        super.setText(text, type);
    }

    public HadEditText setOnTextChangedInterface(OnTextChangedInterface onTextChangedInterface) {
        this.onTextChangedInterface = onTextChangedInterface;
        return this;
    }

    public interface OnTextChangedInterface {
        void onTextChanged(CharSequence argo, boolean enableSend);

        void onBeforeTextChanged(CharSequence s, int start, int count, int after);

        void onShowMention(LinkedList<UserProfileEntity> users, HadEditText editText, boolean isMultiSelect, String keyword);

        void onHideMention(HadEditText editText, String keyword);

        void onNotifyMentionDataChanged(LinkedList<UserProfileEntity> users);

        void onSelectionChanged(int selStart, int selEnd);
    }
}
