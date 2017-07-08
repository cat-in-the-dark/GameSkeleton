package org.catinthedark.client

class OnConnected
class OnDisconnected
class OnConnectionFailure(val e: Throwable)
class Message(val payload: Any)
class OnSendingMessageError(val e: Throwable, val message: Message)