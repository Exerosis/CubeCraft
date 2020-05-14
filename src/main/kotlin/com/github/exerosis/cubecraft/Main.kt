package com.github.exerosis.cubecraft

import io.netty.buffer.Unpooled
import io.netty.channel.*
import net.minecraft.server.v1_8_R3.IChatBaseComponent
import net.minecraft.server.v1_8_R3.PacketDataSerializer
import net.minecraft.server.v1_8_R3.PacketPlayInChat
import net.minecraft.server.v1_8_R3.PacketPlayOutChat
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import java.util.*

const val PACKET_HANDLER  = "packet_handler"
const val CUSTOM_HANDLER = "custom_handler"

val Player.channel get() = (this as CraftPlayer).handle.playerConnection.networkManager.channel

class Main : JavaPlugin(), Listener {
    private val handler = object : ChannelDuplexHandler(), Listener {
        override fun channelRead(context: ChannelHandlerContext, packet: Any) {
            super.channelRead(context, if (packet is PacketPlayInChat)
                PacketPlayInChat("${packet.a()} Arrived")
            else packet)
        }
        override fun write(context: ChannelHandlerContext, packet: Any, promise: ChannelPromise) {
            super.write(context, if (packet is PacketPlayOutChat) {
                val out = PacketDataSerializer(Unpooled.buffer())
                packet.b(out)
                val message = IChatBaseComponent.ChatSerializer.a(out.c(32767)).a(" Sent")
                out.release()
                PacketPlayOutChat(message)
            } else packet, promise)
        }
    }

    override fun onEnable() {
        server.onlinePlayers.forEach { it.channel.inject() }
        server.pluginManager.registerEvents(this, this)
    }
    override fun onDisable() {
        HandlerList.unregisterAll(this as Plugin)
        server.onlinePlayers.forEach { it.channel.pipeline().remove(handler) }
    }

    @EventHandler fun PlayerJoinEvent.onJoin() = player.channel.inject()

    private fun Channel.inject() { pipeline().addBefore(PACKET_HANDLER, CUSTOM_HANDLER, handler) }
}