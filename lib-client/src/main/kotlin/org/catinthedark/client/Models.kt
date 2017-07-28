package org.catinthedark.client

class OnConnected
class OnDisconnected
data class OnConnectionFailure(val e: Throwable)
data class UDPMessage(val payload: Any)
data class TCPMessage(val payload: Any)
data class OnSendingTCPMessageError(val e: Throwable, val message: TCPMessage)
data class OnSendingUDPMessageError(val e: Throwable, val message: UDPMessage)