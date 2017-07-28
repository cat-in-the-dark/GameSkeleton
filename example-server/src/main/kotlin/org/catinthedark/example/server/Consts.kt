package org.catinthedark.example.server

import org.catinthedark.shared.invokers.DeferrableInvoker
import org.catinthedark.shared.invokers.SimpleInvoker
import org.catinthedark.shared.invokers.TickInvoker
import java.net.SocketAddress

typealias ClientID = String

val invoker: DeferrableInvoker = SimpleInvoker()
val gameInvoker: DeferrableInvoker = TickInvoker()

val clients: MutableMap<ClientID, SocketAddress> = mutableMapOf()
val clientsRooms: MutableMap<ClientID, Room> = mutableMapOf()
val lobby: Room = Room("lobby")