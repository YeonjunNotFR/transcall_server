package com.youhajun.transcall

import com.fasterxml.uuid.Generators
import com.youhajun.transcall.call.conversation.domain.CallConversation
import com.youhajun.transcall.call.conversation.domain.CallConversationTrans
import com.youhajun.transcall.call.conversation.repository.CallConversationRepository
import com.youhajun.transcall.call.conversation.repository.CallConversationTransRepository
import com.youhajun.transcall.call.history.domain.CallHistory
import com.youhajun.transcall.call.history.repository.CallHistoryRepository
import com.youhajun.transcall.call.participant.domain.CallParticipant
import com.youhajun.transcall.call.participant.repository.CallParticipantRepository
import com.youhajun.transcall.call.room.domain.CallRoom
import com.youhajun.transcall.call.room.domain.RoomJoinType
import com.youhajun.transcall.call.room.domain.RoomStatus
import com.youhajun.transcall.call.room.domain.RoomVisibility
import com.youhajun.transcall.call.room.repository.CallRoomRepository
import com.youhajun.transcall.user.domain.*
import com.youhajun.transcall.user.repository.UserRepository
import kotlinx.coroutines.runBlocking
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

@Component
class DummyDataInitializer(
    private val userRepository: UserRepository,
    private val roomRepository: CallRoomRepository,
    private val participantRepository: CallParticipantRepository,
    private val conversationRepository: CallConversationRepository,
    private val conversationTransRepository: CallConversationTransRepository,
    private val historyRepository: CallHistoryRepository,
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        runBlocking {
//            val myUserId = UUID.fromString("019c02aa-dfdb-7951-89e9-68165900f81c")
//            insertDummyData(myUserId = myUserId, roomCount = 100)
        }
    }

    suspend fun insertDummyData(myUserId: UUID, roomCount: Int) {
        val userIds = listOf(myUserId) + List(10) {
            UUID.randomUUID().also { ensureUserExists(it) }
        }
        val rooms = createDummyRooms(userIds, roomCount)
        rooms.forEach { roomRepository.save(it) }

        rooms.forEach { room ->
            val histories = createDummyHistory(room, myUserId, 3)
            histories.forEach { historyRepository.save(it) }

            val participants = createDummyParticipants(room, userIds, histories, 100)
            participants.forEach { participantRepository.save(it) }

            val conversations = createDummyConversations(room, participants, 100)
            conversations.forEach { conversationRepository.save(it) }

            val translations = createDummyTransConversations(conversations, myUserId)
            translations.forEach { conversationTransRepository.save(it) }
        }
    }

    private fun createDummyRooms(userIds: List<UUID>, count: Int): List<CallRoom> =
        (1..count).map { index ->
            val roomCreatedAt = randomRoomCreatedAt()
            CallRoom(
                uuid = Generators.timeBasedEpochGenerator().construct(roomCreatedAt.toEpochSecond(ZoneOffset.UTC)),
                roomCode = "ROOM-${1000 + index}",
                hostId = userIds.random(),
                title = "Room $index",
                maxParticipants = (2..6).random(),
                currentParticipantsCount = (0..3).random(),
                janusRoomId = index.toLong(),
                visibility = if (index % 2 == 0) RoomVisibility.PUBLIC else RoomVisibility.PRIVATE,
                joinType = if (index % 2 == 0) RoomJoinType.CODE_JOIN else RoomJoinType.DIRECT_CALL,
                tags = setOf("tag$index", "chat"),
                status = if (index % 2 == 0) RoomStatus.WAITING else RoomStatus.IN_PROGRESS
            ).apply {
                createdAt = roomCreatedAt.toInstant(ZoneOffset.UTC)
            }
        }

    private fun createDummyParticipants(
        room: CallRoom,
        userIds: List<UUID>,
        histories: List<CallHistory>,
        count: Int,
    ): List<CallParticipant> =
        (1..((1..count).random())).map { idx ->
            val history = histories.random()

            val start = LocalDateTime.ofInstant(history.createdAt, ZoneOffset.UTC)
            val end = history.leftAt?.let { LocalDateTime.ofInstant(it, ZoneOffset.UTC) } ?: LocalDateTime.now()

            val createdAt = randomDateTimeBetween(start, end.minusMinutes(5))
            val leftAt = if ((0..10).random() == 1) null else randomDateTimeBetween(createdAt, end)

            val userId = if (idx == 1) userIds.first() else userIds.random()
            val language = if (idx % 2 == 0) LanguageType.ENGLISH else LanguageType.KOREAN
            val country = if (language == LanguageType.ENGLISH) CountryType.UNITED_STATES else CountryType.SOUTH_KOREA

            CallParticipant(
                uuid = Generators.timeBasedEpochGenerator()
                    .construct(createdAt.toEpochSecond(ZoneOffset.UTC)),
                roomId = room.uuid,
                userId = userId,
                language = language,
                country = country,
                displayName = "User$idx",
                profileImageUrl = "https://placehold.co/64x64?text=U$idx",
                leftAt = leftAt?.toInstant(ZoneOffset.UTC),
            ).apply {
                this.createdAt = createdAt.toInstant(ZoneOffset.UTC)
            }
        }

    private fun createDummyConversations(
        room: CallRoom,
        participants: List<CallParticipant>,
        count: Int
    ): List<CallConversation> {
        val now = LocalDateTime.now()
        val roomId = room.uuid

        return (0..count).map { idx ->
            val participant = participants.random()
            val start = LocalDateTime.ofInstant(participant.createdAt, ZoneOffset.UTC)
            val end = participant.leftAt?.let { LocalDateTime.ofInstant(participant.createdAt, ZoneOffset.UTC) } ?: now
            val conversationCreatedAt = randomDateTimeBetween(start, end)

            CallConversation(
                uuid = Generators.timeBasedEpochGenerator().construct(conversationCreatedAt.toEpochSecond(ZoneOffset.UTC)),
                roomId = roomId,
                senderId = participant.userId,
                participantId = participant.id,
                originText = "Original message $idx from ${room.title}",
                originLanguage = if (idx % 2 == 0) LanguageType.ENGLISH else LanguageType.KOREAN
            ).apply {
                createdAt = conversationCreatedAt.toInstant(ZoneOffset.UTC)
            }
        }
    }

    private fun createDummyTransConversations(
        conversations: List<CallConversation>,
        myUserId: UUID
    ): List<CallConversationTrans> {
        return conversations.flatMap { conv ->
            val targetLanguages = listOf(LanguageType.KOREAN, LanguageType.ENGLISH)
                .filter { it != conv.originLanguage }

            targetLanguages.map { lang ->
                CallConversationTrans(
                    roomId = conv.roomId,
                    conversationId = conv.uuid,
                    translatedText = "[${lang.name}] ${conv.originText}",
                    translatedLanguage = lang
                ).apply {
                    createdAt = conv.createdAt
                }
            }
        }
    }

    private fun createDummyHistory(room: CallRoom, myUserId: UUID, count: Int): List<CallHistory> =
        (1..count).map { index ->
            val createdAtInstant = LocalDateTime.ofInstant(room.createdAt, ZoneOffset.UTC)
            val leftAt = randomHistoryLeftAt(createdAtInstant)
            val historyCreatedAt = randomDateTimeBetween(createdAtInstant, leftAt ?: LocalDateTime.now())
            CallHistory(
                uuid = Generators.timeBasedEpochGenerator().construct(historyCreatedAt.toEpochSecond(ZoneOffset.UTC)),
                roomId = room.uuid,
                userId = myUserId,
                title = "History of Room ${room.title}",
                summary = "Summary text $index",
                memo = "Memo for ${room.roomCode}",
                liked = (0..1).random() == 1,
                deleted = false,
                leftAt = leftAt?.toInstant(ZoneOffset.UTC),
            ).apply {
                createdAt = historyCreatedAt.toInstant(ZoneOffset.UTC)
            }
        }

    private suspend fun ensureUserExists(userId: UUID) {
        val exists = userRepository.findById(userId) != null
        if (!exists) {
            val dummyUser = User(
                uuid = userId,
                email = "dummy_${userId.toString().take(8)}@example.com",
                socialType = SocialType.GOOGLE,
                nickname = "DummyUser_${userId.toString().takeLast(4)}",
                language = LanguageType.entries.random(),
                country = CountryType.entries.random(),
                membershipPlan = MembershipPlan.Free,
                profileImageUrl = "https://placehold.co/64x64?text=U",
                isActive = true
            )

            userRepository.save(dummyUser)
        }
    }

    private fun randomRoomCreatedAt(): LocalDateTime {
        val now = LocalDateTime.now()

        return when ((1..10).random()) {
            1 -> now.minusYears(2).plusDays((0..365).random().toLong())    // 2년 전 ~ 1년 전
            2 -> now.minusYears(1).plusDays((0..365).random().toLong())    // 1년 전 ~ 현재
            3, 4 -> now.minusMonths((1..6).random().toLong())              // 1~6개월 전
            5, 6 -> now.minusWeeks((1..4).random().toLong())               // 1~4주 전
            7, 8 -> now.minusDays((1..14).random().toLong())               // 1~2주 전
            9 -> now.minusHours((1..72).random().toLong())                 // 1~3일 전
            else -> now.minusMinutes((5..300).random().toLong())           // 오늘~몇 시간 전
        }
    }

    private fun randomHistoryLeftAt(roomCreatedAt: LocalDateTime): LocalDateTime? {
        if ((1..50).random() == 1) return null

        // room 생성 후 5분~14일 사이 랜덤 종료
        val offsetMinutes = listOf(
            (5..120).random(),               // 몇 시간 안에 종료
            (121..1440).random(),            // 하루 안에 종료
            (1441..10080).random()           // 최대 1~14일 사이
        ).random()

        return roomCreatedAt.plusMinutes(offsetMinutes.toLong())
    }

    private fun randomDateTimeBetween(start: LocalDateTime, end: LocalDateTime): LocalDateTime {
        if (end.isBefore(start)) return start
        val duration = Duration.between(start, end).toMinutes().coerceAtLeast(1)
        val offset = (0 until duration).random().toLong()
        return start.plusMinutes(offset)
    }
}