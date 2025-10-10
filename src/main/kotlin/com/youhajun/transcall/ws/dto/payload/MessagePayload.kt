package com.youhajun.transcall.ws.dto.payload

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

sealed interface MessagePayload

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "action"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = JoinRoomPublisher::class, name = JoinRoomPublisher.ACTION),
    JsonSubTypes.Type(value = PublisherOffer::class, name = PublisherOffer.ACTION),
    JsonSubTypes.Type(value = JoinRoomSubscriber::class, name = JoinRoomSubscriber.ACTION),
    JsonSubTypes.Type(value = SubscriberAnswer::class, name = SubscriberAnswer.ACTION),
    JsonSubTypes.Type(value = SubscriberUpdate::class, name = SubscriberUpdate.ACTION),
    JsonSubTypes.Type(value = SignalingIceCandidate::class, name = SignalingIceCandidate.ACTION),
    JsonSubTypes.Type(value = CompleteIceCandidate::class, name = CompleteIceCandidate.ACTION),
    JsonSubTypes.Type(value = CameraEnableChanged::class, name = CameraEnableChanged.ACTION),
    JsonSubTypes.Type(value = MicEnableChanged::class, name = MicEnableChanged.ACTION),
)
sealed interface RequestPayload : MessagePayload

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "action"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = NewPublishers::class, name = NewPublishers.ACTION),
    JsonSubTypes.Type(value = JoinedRoomPublisher::class, name = JoinedRoomPublisher.ACTION),
    JsonSubTypes.Type(value = PublisherAnswer::class, name = PublisherAnswer.ACTION),
    JsonSubTypes.Type(value = SubscriberOffer::class, name = SubscriberOffer.ACTION),
    JsonSubTypes.Type(value = OnIceCandidate::class, name = OnIceCandidate.ACTION),
    JsonSubTypes.Type(value = ConnectedRoom::class, name = ConnectedRoom.ACTION),
    JsonSubTypes.Type(value = ChangedRoom::class, name = ChangedRoom.ACTION),
    JsonSubTypes.Type(value = NewPublishers::class, name = NewPublishers.ACTION),
    JsonSubTypes.Type(value = SttStart::class, name = SttStart.ACTION),
    JsonSubTypes.Type(value = TranslationMessage::class, name = TranslationMessage.ACTION),
    JsonSubTypes.Type(value = MediaStateChanged::class, name = MediaStateChanged.ACTION),
    JsonSubTypes.Type(value = MediaStateInit::class, name = MediaStateInit.ACTION),
)
sealed interface ResponsePayload : MessagePayload
