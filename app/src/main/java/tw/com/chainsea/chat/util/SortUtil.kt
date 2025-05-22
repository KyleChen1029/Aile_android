package tw.com.chainsea.chat.util

import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity
import tw.com.chainsea.ce.sdk.bean.servicenumber.type.GroupPrivilegeEnum
import tw.com.chainsea.ce.sdk.bean.servicenumber.type.ServiceNumberPrivilege
import tw.com.chainsea.ce.sdk.http.ce.model.Member

object SortUtil {

    private val privilegeComparator = Comparator<Member> { a, b ->
        if ((a.privilege == ServiceNumberPrivilege.OWNER) xor (b.privilege == ServiceNumberPrivilege.OWNER)) {
            if (a.privilege == ServiceNumberPrivilege.OWNER) -1 else 1
        } else if ((a.privilege == ServiceNumberPrivilege.MANAGER) xor (b.privilege == ServiceNumberPrivilege.MANAGER)) {
            if (a.privilege == ServiceNumberPrivilege.MANAGER) -1 else 1
        } else {
            0
        }
    }

    private val serviceNumberPrivilegeComparator = Comparator<UserProfileEntity> { a, b ->
        if ((a.privilege == ServiceNumberPrivilege.OWNER) xor (b.privilege == ServiceNumberPrivilege.OWNER)) {
            if (a.privilege == ServiceNumberPrivilege.OWNER) -1 else 1
        } else if ((a.privilege == ServiceNumberPrivilege.MANAGER) xor (b.privilege == ServiceNumberPrivilege.MANAGER)) {
            if (a.privilege == ServiceNumberPrivilege.MANAGER) -1 else 1
        } else {
            0
        }
    }

    private val isOwnerComparator = Comparator<UserProfileEntity> { a, b ->
        if ((a.isOwner) xor (b.isOwner)) {
            if (a.isOwner) -1 else 1
        } else if ((a.privilege == ServiceNumberPrivilege.MANAGER) xor (b.privilege == ServiceNumberPrivilege.MANAGER)) {
            if (a.privilege == ServiceNumberPrivilege.MANAGER) -1 else 1
        } else {
            0
        }
    }

    private val groupPrivilegeComparator = Comparator<UserProfileEntity> { a, b ->
        if ((a.groupPrivilege == GroupPrivilegeEnum.Owner) xor (b.groupPrivilege == GroupPrivilegeEnum.Owner)) {
            if (a.groupPrivilege == GroupPrivilegeEnum.Owner) -1 else 1
        } else if ((a.groupPrivilege == GroupPrivilegeEnum.Manager) xor (b.groupPrivilege == GroupPrivilegeEnum.Manager)) {
            if (a.groupPrivilege == GroupPrivilegeEnum.Manager) -1 else 1
        } else {
            0
        }
    }

    //按照 擁有者 -> 管理者 -> 一般成員排序
    fun sortGroupOwnerManagerByPrivilege(userProfileList: List<UserProfileEntity>): MutableList<UserProfileEntity> {
        return userProfileList.sortedWith(groupPrivilegeComparator).toMutableList()
    }

    //按照 擁有者 -> 管理者 -> 一般成員排序
    fun sortOwnerManagerByPrivilege(userProfileList: List<Member>): List<Member> {
        return userProfileList.sortedWith(privilegeComparator)
    }

    //按照 擁有者 -> 管理者 -> 一般成員排序
    fun sortServiceNumberOwnerManagerByPrivilege(userProfileList: List<UserProfileEntity>): List<UserProfileEntity> {
        return userProfileList.sortedWith(serviceNumberPrivilegeComparator)
    }

    fun sortOwnerManagerByBoolean(userProfileList: List<UserProfileEntity>): List<UserProfileEntity> {
        return userProfileList.sortedWith(isOwnerComparator)
    }
}