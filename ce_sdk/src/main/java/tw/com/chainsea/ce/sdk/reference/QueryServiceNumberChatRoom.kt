package tw.com.chainsea.ce.sdk.reference

import android.annotation.SuppressLint
import tw.com.chainsea.ce.sdk.bean.room.ServiceNumberStatus
import tw.com.chainsea.ce.sdk.bean.room.ServiceNumberType
import tw.com.chainsea.ce.sdk.database.DBContract.ChatRoomEntry

object QueryServiceNumberChatRoom {
    fun buildQuery(selfUserId: String): String {
        // AI Service
        val aiServiceConditions = buildAiServiceConditions()
        // UnService conditions
        val unServiceConditions = buildUnServiceConditions(selfUserId)
        // My service conditions
        val myServiceConditions = buildMyServiceConditions(selfUserId)
        // Serviced conditions
        val servicedConditions = buildServicedConditions(selfUserId)

        return "SELECT * FROM " + ChatRoomEntry.TABLE_NAME +
            " WHERE (" +
            java.lang.String.join(
                " OR ",
                listOf(
                    aiServiceConditions,
                    unServiceConditions,
                    myServiceConditions,
                    servicedConditions
                )
            ) +
            ")"
    }

    fun buildFormalQuery(selfUserId: String): String {
        // AI Service
        val aiServiceConditions = buildAiServiceConditions()
        // UnService conditions
        val unServiceConditions = buildUnServiceConditions(selfUserId)
        // My service conditions
        val myServiceConditions = buildMyServiceConditions(selfUserId)
        // Serviced conditions
        val servicedConditions = buildServicedConditions(selfUserId)

        return "(" +
            java.lang.String.join(
                " OR ",
                listOf(
                    aiServiceConditions,
                    unServiceConditions,
                    myServiceConditions,
                    servicedConditions
                )
            ) +
            ")"
    }

    private fun buildAiServiceConditions(): String =
        String.format(
            "(%s OR %s)", // isAiServiced
            String.format(
                "(%s = '%s' AND %s = 'N' AND %s = 'N' AND %s = '' AND %s = 'SERVICE')",
                ChatRoomEntry.COLUMN_SERVICE_NUMBER_STATUS,
                ServiceNumberStatus.ROBOT_SERVICE.status,
                ChatRoomEntry.COLUMN_CHAT_ROOM_DELETED,
                ChatRoomEntry.COLUMN_AI_SERVICE_WARNED,
                ChatRoomEntry.COLUMN_SERVICE_NUMBER_AGENT_ID,
                ChatRoomEntry.COLUMN_LIST_CLASSIFY
            ), // isMonitorAi
            String.format(
                "(%s = '%s' AND %s = 'N' AND %s = 'Y' AND %s = '')",
                ChatRoomEntry.COLUMN_SERVICE_NUMBER_STATUS,
                ServiceNumberStatus.ROBOT_SERVICE.status,
                ChatRoomEntry.COLUMN_CHAT_ROOM_DELETED,
                ChatRoomEntry.COLUMN_AI_SERVICE_WARNED,
                ChatRoomEntry.COLUMN_SERVICE_NUMBER_AGENT_ID
            )
        )

    private fun buildUnServiceConditions(selfUserId: String): String =
        String.format(
            "(%s OR %s)", // BOSS type
            String.format(
                "(%s = '%s' AND %s != '' AND %s = '' AND %s != '%s' AND (%s) AND %s = 'SERVICE')",
                ChatRoomEntry.COLUMN_SERVICE_NUMBER_TYPE,
                ServiceNumberType.BOSS.type,
                ChatRoomEntry.COLUMN_SERVICE_NUMBER_OWNER_ID,
                ChatRoomEntry.COLUMN_SERVICE_NUMBER_AGENT_ID,
                ChatRoomEntry.COLUMN_SERVICE_NUMBER_OWNER_ID,
                selfUserId,
                buildStatusAndUnreadConditions(),
                ChatRoomEntry.COLUMN_LIST_CLASSIFY
            ), // Non-BOSS type
            String.format(
                "(%s != '%s' AND %s = '' AND (%s))",
                ChatRoomEntry.COLUMN_SERVICE_NUMBER_TYPE,
                ServiceNumberType.BOSS.type,
                ChatRoomEntry.COLUMN_SERVICE_NUMBER_AGENT_ID,
                buildStatusAndUnreadConditions()
            )
        )

    private fun buildMyServiceConditions(selfUserId: String): String =
        String.format(
            "(%s OR %s OR %s OR %s)", // Offline conditions
            String.format(
                "(%s = -1 AND %s IN ('%s', '%s')) AND %s = 'SERVICE'",
                ChatRoomEntry.COLUMN_UNREAD_NUMBER,
                ChatRoomEntry.COLUMN_SERVICE_NUMBER_STATUS,
                ServiceNumberStatus.OFF_LINE.status,
                ServiceNumberStatus.TIME_OUT.status,
                ChatRoomEntry.COLUMN_LIST_CLASSIFY
            ), // Professional online
            buildProfessionalCondition(
                selfUserId,
                ServiceNumberStatus.ON_LINE.status,
                ">=",
                0
            ), // Professional timeout
            buildProfessionalCondition(
                selfUserId,
                ServiceNumberStatus.TIME_OUT.status,
                ">",
                0
            ), // Professional offline
            buildProfessionalCondition(selfUserId, ServiceNumberStatus.OFF_LINE.status, ">=", 0)
        )

    private fun buildServicedConditions(selfUserId: String): String =
        String.format(
            "(%s OR %s OR %s OR %s OR %s) AND %s = 'SERVICE'", // Non-professional online
            buildNonProfessionalCondition(
                selfUserId,
                ServiceNumberStatus.ON_LINE.status,
                ">="
            ), // Non-professional timeout
            buildNonProfessionalCondition(
                selfUserId,
                ServiceNumberStatus.TIME_OUT.status,
                ">"
            ), // Any agent online
            buildAnyAgentCondition(
                ServiceNumberStatus.ON_LINE.status,
                ">=",
                0
            ), // Any agent timeout
            buildAnyAgentCondition(
                ServiceNumberStatus.TIME_OUT.status,
                ">",
                0
            ), // Boss type specific
            buildBossTypeCondition(selfUserId),
            ChatRoomEntry.COLUMN_LIST_CLASSIFY
        )

    private fun buildStatusAndUnreadConditions(): String =
        String.format(
            "(%s = '%s' AND %s >= 0) OR (%s = '%s' AND %s > 0)",
            ChatRoomEntry.COLUMN_SERVICE_NUMBER_STATUS,
            ServiceNumberStatus.ON_LINE.status,
            ChatRoomEntry.COLUMN_UNREAD_NUMBER,
            ChatRoomEntry.COLUMN_SERVICE_NUMBER_STATUS,
            ServiceNumberStatus.TIME_OUT.status,
            ChatRoomEntry.COLUMN_UNREAD_NUMBER
        )

    @SuppressLint("DefaultLocale")
    private fun buildProfessionalCondition(
        selfUserId: String,
        status: String,
        operator: String,
        value: Int
    ): String =
        String.format(
            "(%s = '%s' AND %s = '%s' AND %s = '%s' AND %s %s %d)",
            ChatRoomEntry.COLUMN_SERVICE_NUMBER_AGENT_ID,
            selfUserId,
            ChatRoomEntry.COLUMN_SERVICE_NUMBER_TYPE,
            ServiceNumberType.PROFESSIONAL.type,
            ChatRoomEntry.COLUMN_SERVICE_NUMBER_STATUS,
            status,
            ChatRoomEntry.COLUMN_UNREAD_NUMBER,
            operator,
            value
        )

    @SuppressLint("DefaultLocale")
    private fun buildNonProfessionalCondition(
        selfUserId: String,
        status: String,
        operator: String
    ): String =
        String.format(
            "(%s = '%s' AND %s != '%s' AND %s = '%s' AND %s %s %d)",
            ChatRoomEntry.COLUMN_SERVICE_NUMBER_AGENT_ID,
            selfUserId,
            ChatRoomEntry.COLUMN_SERVICE_NUMBER_TYPE,
            ServiceNumberType.PROFESSIONAL.type,
            ChatRoomEntry.COLUMN_SERVICE_NUMBER_STATUS,
            status,
            ChatRoomEntry.COLUMN_UNREAD_NUMBER,
            operator,
            0
        )

    @SuppressLint("DefaultLocale")
    private fun buildAnyAgentCondition(
        status: String,
        operator: String,
        value: Int
    ): String =
        String.format(
            "(%s != '' AND %s = '%s' AND %s %s %d)",
            ChatRoomEntry.COLUMN_SERVICE_NUMBER_AGENT_ID,
            ChatRoomEntry.COLUMN_SERVICE_NUMBER_STATUS,
            status,
            ChatRoomEntry.COLUMN_UNREAD_NUMBER,
            operator,
            value
        )

    private fun buildBossTypeCondition(selfUserId: String): String =
        String.format(
            "(%s != '' AND %s != '%s' AND %s >= 0 AND %s = '%s' AND %s = '%s')",
            ChatRoomEntry.COLUMN_SERVICE_NUMBER_AGENT_ID,
            ChatRoomEntry.COLUMN_SERVICE_NUMBER_AGENT_ID,
            selfUserId,
            ChatRoomEntry.COLUMN_UNREAD_NUMBER,
            ChatRoomEntry.COLUMN_SERVICE_NUMBER_STATUS,
            ServiceNumberStatus.ON_LINE.status,
            ChatRoomEntry.COLUMN_SERVICE_NUMBER_TYPE,
            ServiceNumberType.BOSS.type
        )
}
