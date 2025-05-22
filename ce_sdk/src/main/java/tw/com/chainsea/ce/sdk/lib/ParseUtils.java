package tw.com.chainsea.ce.sdk.lib;

import com.google.common.collect.Lists;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.ce.sdk.bean.CrowdEntity;
import tw.com.chainsea.ce.sdk.bean.Recommend;
import tw.com.chainsea.ce.sdk.bean.ServiceNum;
import tw.com.chainsea.ce.sdk.bean.account.AccountType;
import tw.com.chainsea.ce.sdk.bean.account.Gender;
import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity;
import tw.com.chainsea.ce.sdk.bean.account.UserType;
import tw.com.chainsea.ce.sdk.bean.business.BusinessItems;
import tw.com.chainsea.ce.sdk.bean.label.Label;
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomType;
import tw.com.chainsea.ce.sdk.bean.servicenumber.type.ServiceNumberPrivilege;

/**
 * Created by sunhui on 2017/7/14.
 */

public class ParseUtils {
    private static String constructContent(String content, long callTime) {
        String str = null;
        try {
            if ("single_cancel".equals(content)) {
                str = "通話已取消";
            }
            if ("single_end".equals(content)) {
                String date;
                if (callTime >= 3600000) {
                    date = new SimpleDateFormat("HH:mm:ss", Locale.TAIWAN).format(callTime);
                } else {
                    date = new SimpleDateFormat("mm:ss", Locale.TAIWAN).format(callTime);
                }
                str = "通話時長: " + date;
            }
            if ("single_refuse".equals(content)) {
                str = "已拒接";
            }
            if ("group_call".equals(content)) {
                str = "發起了社團電話";
            }
        } catch (Exception ignored) {
        }
        return str;
    }

    public static Recommend parseRecommend(JSONObject json) throws JSONException {

        Recommend recommend = new Recommend();
        if (json.has("name")) {
            recommend.setName(json.getString("nickName"));
        }
        if (json.has("id")) {
            recommend.setId(json.getString("id"));
        }
        if (json.has("avatarId")) {
            recommend.setAvatarUrl(json.getString("avatarId"));
        }
        return recommend;
    }

    public static UserProfileEntity parseAccount(JSONObject json) throws JSONException {
        UserProfileEntity accountCE = new UserProfileEntity();
        if (json.has("id")) {
            accountCE.setId(json.getString("id"));
        }
        if (json.has("isBlock")) {
            accountCE.setBlock(json.getBoolean("isBlock"));
        }
        accountCE.setType(AccountType.SELF);
        if (json.has("isAddressBook")) {
            boolean isAddressBook = json.getBoolean("isAddressBook");
            accountCE.setType(isAddressBook ? AccountType.FRIEND : AccountType.UNDEF);
        }
        if (json.has("avatarId")) {
            accountCE.setAvatarId(json.getString("avatarId"));
        }
        if (json.has("nickName")) {
            accountCE.setNickName(json.getString("nickName"));
        }
        if (json.has("roomId")) {
            accountCE.setRoomId(json.getString("roomId"));
        }
        if (json.has("alias")) {
            accountCE.setAlias(json.getString("alias"));
        }
        if (json.has("mood")) {
            accountCE.setMood(json.getString("mood"));
        }
        if (json.has("department")) {
            accountCE.setDepartment(json.getString("department"));
        }
        if (json.has("duty")) {
            accountCE.setDuty(json.getString("duty"));
        }
        if (json.has("userType")) {
            accountCE.setUserType(UserType.of(json.getString("userType")));
        } else {
            accountCE.setUserType(UserType.CONTACT);
        }

        if (json.has("labelNames")) {
            JSONArray labelNames = json.getJSONArray("labelNames");
            if (labelNames.length() > 0) {
                List<Label> labels = Lists.newArrayList();
                for (int i = 0; i < labelNames.length(); i++) {
                    String labelName = labelNames.getJSONObject(i).getString("labelName");
                    String labelId = labelNames.getJSONObject(i).getString("labelId");
                    if (labelName.equals("我的收藏")) {
                        accountCE.setCollection(true);
                    }
                    Label label = new Label();
                    label.setId(labelId);
                    label.setName(labelName);
                    labels.add(label);
                }
                accountCE.setLabels(labels);
            }
        }

//        CELog.i(String.format("parseAccount::: %s",(System.currentTimeMillis() - useTime) / 1000.0d) +"/s");
        return accountCE;
    }

    public static UserProfileEntity parseMember(JSONObject json) throws JSONException {
        UserProfileEntity accountCE = new UserProfileEntity();
        if (json.has("id")) {
            accountCE.setId(json.getString("id"));
        }
        if (json.has("isBlock")) {
            accountCE.setBlock(json.getBoolean("isBlock"));
        }
        if (json.has("avatarId")) {
            accountCE.setAvatarId(json.getString("avatarId"));
        }
        if (json.has("name")) {
            accountCE.setName(json.getString("name"));
        }
        if (json.has("nickName")) {
            accountCE.setNickName(json.getString("nickName"));
        }
        if (json.has("customerName")) {
            accountCE.setCustomerName(json.getString("customerName"));
        }
        if (json.has("customerDescription")) {
            accountCE.setCustomerDescription(json.getString("customerDescription"));
        }
        if (json.has("customerBusinessCardUrl")) {
            accountCE.setCustomerDescription(json.getString("customerBusinessCardUrl"));
        }
        if (json.has("roomId")) {
            accountCE.setRoomId(json.getString("roomId"));
        }
        if (json.has("alias")) {
            accountCE.setAlias(json.getString("alias"));
        }
        if (json.has("mood")) {
            accountCE.setMood(json.getString("mood"));
        }
        if (json.has("userType")) {
            accountCE.setUserType(UserType.of(json.getString("userType")));
        }

        if (json.has("isOwner")) {
            accountCE.setOwner(json.getBoolean("isOwner"));
        }

        if (json.has("privilege")) {
            accountCE.setPrivilege(ServiceNumberPrivilege.of(json.getString("privilege")));
        }

        return accountCE;
    }

    public static UserProfileEntity parseUser(JSONObject json) throws JSONException {
        UserProfileEntity accountCE = new UserProfileEntity();
        if (json.has("id")) {
            accountCE.setId(json.getString("id"));
        }

        if (json.has("isBlock")) {
            accountCE.setBlock(json.getBoolean("isBlock"));
        }
        if (json.has("isCollection")) {
            accountCE.setCollection(json.getBoolean("isCollection"));
        }
        if (json.has("isAddressBook")) {
            boolean isAddressBook = json.getBoolean("isAddressBook");
            accountCE.setType(isAddressBook ? AccountType.FRIEND : AccountType.UNDEF);
        }

        if (json.has("avatarId")) {
            accountCE.setAvatarId(json.getString("avatarId"));
        }
        if (json.has("gender")) {
            accountCE.setGender(Gender.ofValue(json.getString("gender").trim()));
        }
        if (json.has("name")) {
            accountCE.setName(json.getString("name"));
        }
        if (json.has("nickName")) {
            accountCE.setNickName(json.getString("nickName"));
        }
        if (json.has("customerName")) {
            accountCE.setCustomerName(json.getString("customerName"));
        }
        if (json.has("customerDescription")) {
            accountCE.setCustomerDescription(json.getString("customerDescription"));
        }
        if (json.has("customerBusinessCardUrl")) {
            accountCE.setCustomerBusinessCardUrl(json.getString("customerBusinessCardUrl"));
        }
        if (json.has("otherPhone")) {
            accountCE.setOtherPhone(json.getString("otherPhone"));
        }
        if (json.has("userType")) {
            accountCE.setUserType(UserType.of(json.getString("userType")));
        }
        if (json.has("department")) {
            accountCE.setDepartment(json.getString("department"));
        }
        if (json.has("duty")) {
            accountCE.setDuty(json.getString("duty"));
        }
        if (json.has("email")) {
            accountCE.setEmail(json.getString("email"));
        }
        if (json.has("loginName")) {
            accountCE.setLoginName(json.getString("loginName"));
        }
        if (json.has("roomId")) {
            accountCE.setRoomId(json.getString("roomId"));
        }
        if (json.has("alias")) {
            accountCE.setAlias(json.getString("alias"));
        }
        if (json.has("birthday")) {
            accountCE.setBirthday(json.getString("birthday"));
        }
        if (json.has("google")) {
            accountCE.setGoogleId(json.getString("google"));
        }
        if (json.has("facebook")) {
            accountCE.setFbId(json.getString("facebook"));
        }
        if (json.has("line")) {
            accountCE.setLineId(json.getString("line"));
        }
        if (json.has("mood")) {
            accountCE.setMood(json.getString("mood"));
        }
        if (json.has("mobile")) {
            accountCE.setMobile(json.getLong("mobile"));
        }

        if (json.has("openId")) {
            accountCE.setOpenId(json.getString("openId"));
        }

        if (json.has("hasBindEmployee")) {
            accountCE.setHasBindEmployee(json.getBoolean("hasBindEmployee"));
        }

        if (json.has("personRoomId")) {
            accountCE.setPersonRoomId(json.getString("personRoomId"));
        }

        if (json.has("homePagePics")) {
            JSONArray array = json.getJSONArray("homePagePics");
            List<UserProfileEntity.HomePagePic> pics = Lists.newArrayList();
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                pics.add(UserProfileEntity.HomePagePic.Build()
                    .id(obj.getString("id"))
                    .picUrl(obj.getString("picUrl"))
                    .sequence(obj.getInt("sequence"))
                    .build());
            }
            accountCE.setHomePagePics(pics);
        }

        return accountCE;
    }

    public static CrowdEntity parseGroup(JSONObject json) throws JSONException {
        CrowdEntity crowdEntity = new CrowdEntity();
        if (json.has("avatarId")) {
            crowdEntity.setAvatarUrl(json.getString("avatarId"));
        }
        if (json.has("id")) {
            crowdEntity.setId(json.getString("id"));
        }
        if (json.has("isCustomName")) {
            crowdEntity.setCustomName(json.getBoolean("isCustomName"));
        }
        if (json.has("kind")) {
            crowdEntity.setKind(json.getString("kind"));
        }
        if (json.has("name")) {
            crowdEntity.setName(json.getString("name"));
        }
        if (json.has("ownerId")) {
            crowdEntity.setOwnerId(json.getString("ownerId"));
        }
        if (json.has("isOwner")) {
            crowdEntity.setOwnerId(json.getString("isOwner"));
        }

        if (json.has("type")) {
            String type = json.getString("type");
//            crowdEntity.setType(type);
            if ("crowdEntity".equals(type)) {
                crowdEntity.setType(ChatRoomType.group);
            } else if ("discuss".equals(type)) {
                crowdEntity.setType(ChatRoomType.discuss);
            }
        }
        if (json.has("members")) {
            JSONArray memberArray = json.getJSONArray("members");
            List<UserProfileEntity> users = new ArrayList<>();
            for (int i = 0; i < memberArray.length(); i++) {
                JSONObject jsonObject = memberArray.getJSONObject(i);
                UserProfileEntity account = parseMember(jsonObject);
                users.add(account);
            }
            crowdEntity.setMemberArray(users);
        }
        if (json.has("memberArray")) {
            JSONArray memberArray = json.getJSONArray("memberArray");
            List<UserProfileEntity> users = Lists.newArrayList();
            for (int i = 0; i < memberArray.length(); i++) {
                JSONObject jsonObject = memberArray.getJSONObject(i);
                UserProfileEntity account = parseMember(jsonObject);
                users.add(account);
            }
            crowdEntity.setMemberArray(users);
        }
        return crowdEntity;

    }

    public static ServiceNum parseServiceNum(JSONObject json) {
        ServiceNum serviceNum = new ServiceNum();
        try {
            if (json.has("description")) {
                serviceNum.description = json.getString("description");
            }
            if (json.has("isSubscribe")) {
                serviceNum.isSubscribe = json.getBoolean("isSubscribe");
            }
            if (json.has("name")) {
                serviceNum.name = json.getString("name");
            }
            if (json.has("roomId")) {
                serviceNum.roomId = json.getString("roomId");
            }
            if (json.has("serviceNumberAvatarId")) {
                serviceNum.serviceNumberAvatarId = json.getString("serviceNumberAvatarId");
            }
            if (json.has("serviceNumberId")) {
                serviceNum.serviceNumberId = json.getString("serviceNumberId");
            }

        } catch (JSONException ignored) {
        }
        return serviceNum;
    }

    public static BusinessItems parseServiceNumBusinessItems(JSONObject json) {
        return JsonHelper.getInstance().from(json.toString(), BusinessItems.class);
    }

    public static Label parseLabel(JSONObject json) {
        Label label = new Label();
        try {
            if (json.has("createTime")) {
                long createTime = json.getLong("createTime");
                label.setCreateTime(createTime);
            }
            if (json.has("id")) {
                String id = json.getString("id");
                label.setId(id);
            }
            if (json.has("name")) {
                String name = json.getString("name");
                label.setName(name);
            }
            if (json.has("ownerId")) {
                String ownerId = json.getString("ownerId");
                label.setOwnerId(ownerId);
            }
            if (json.has("readOnly")) {
                boolean readOnly = json.getBoolean("readOnly");
                label.setReadOnly(readOnly);
            }
            if (json.has("users")) {
                List<UserProfileEntity> users = new ArrayList<>();
                JSONArray jsonArray = json.getJSONArray("users");
                for (int i = 0; i < jsonArray.length(); i++) {
                    users.add(parseAccount(jsonArray.getJSONObject(i)));
                }
                label.setUsers(users);
            }

        } catch (JSONException ignored) {
        }
        return label;
    }
}
