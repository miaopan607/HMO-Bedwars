package com.andrei1058.bedwars.listeners;

import com.andrei1058.bedwars.BedWars;
import com.andrei1058.bedwars.shop.InventorySyncAudit;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.logging.Level;

public class PacketInterceptor {

    private static Field FIELD_PLAYER_CONNECTION;
    private static Field FIELD_NETWORK_MANAGER;
    private static Field FIELD_CHANNEL;
    private static Field FIELD_CHAT_COMPONENT; // PacketPlayOutChat.a

    private static boolean initialized = false;
    private static boolean enabled = false;

    static {
        try {
            String packageName = BedWars.plugin.getServer().getClass().getPackage().getName();
            String nmsVersion = packageName.substring(packageName.lastIndexOf('.') + 1);

            Class<?> classEntityPlayer = Class.forName("net.minecraft.server." + nmsVersion + ".EntityPlayer");
            Class<?> classPlayerConnection = Class.forName("net.minecraft.server." + nmsVersion + ".PlayerConnection");
            Class<?> classNetworkManager = Class.forName("net.minecraft.server." + nmsVersion + ".NetworkManager");
            Class<?> classPacketPlayOutChat = Class.forName("net.minecraft.server." + nmsVersion + ".PacketPlayOutChat");
            
            FIELD_PLAYER_CONNECTION = classEntityPlayer.getField("playerConnection");
            
            try {
                FIELD_NETWORK_MANAGER = classPlayerConnection.getDeclaredField("networkManager");
            } catch (NoSuchFieldException e) {
                 // Fallback for some versions or obfuscations
                 // In 1.8.8 it is networkManager.
                 // In 1.18+ it might be 'b' or similar, but we target 1.8.8+ mostly for this legacy plugin structure
                 // If failed, we try to find by type
                 for (Field f : classPlayerConnection.getDeclaredFields()) {
                     if (f.getType().getSimpleName().equals("NetworkManager")) {
                         FIELD_NETWORK_MANAGER = f;
                         break;
                     }
                 }
                 if (FIELD_NETWORK_MANAGER == null) throw e;
            }
            
            FIELD_CHANNEL = classNetworkManager.getDeclaredField("channel");
            
            // PacketPlayOutChat fields
            // a = component
            try {
                FIELD_CHAT_COMPONENT = classPacketPlayOutChat.getDeclaredField("a");
            } catch (NoSuchFieldException e) {
                 // Try to find the component field by type (IChatBaseComponent)
                 for (Field f : classPacketPlayOutChat.getDeclaredFields()) {
                     if (f.getType().getSimpleName().equals("IChatBaseComponent")) {
                         FIELD_CHAT_COMPONENT = f;
                         break;
                     }
                 }
                 if (FIELD_CHAT_COMPONENT == null) throw e;
            }

            FIELD_PLAYER_CONNECTION.setAccessible(true);
            FIELD_NETWORK_MANAGER.setAccessible(true);
            FIELD_CHANNEL.setAccessible(true);
            FIELD_CHAT_COMPONENT.setAccessible(true);

            initialized = true;
            enabled = true;
        } catch (Exception e) {
            BedWars.plugin.getLogger().log(Level.WARNING, "Failed to initialize PacketInterceptor. Admin broadcast filtering might not work.", e);
            enabled = false;
        }
    }

    public static void inject(Player player) {
        if (!enabled || !initialized) return;

        try {
            Object craftPlayer = player.getClass().getMethod("getHandle").invoke(player);
            Object connection = FIELD_PLAYER_CONNECTION.get(craftPlayer);
            Object networkManager = FIELD_NETWORK_MANAGER.get(connection);
            Channel channel = (Channel) FIELD_CHANNEL.get(networkManager);

            if (channel.pipeline().get("BedWars_AdminFilter") != null) {
                InventorySyncAudit.recordPipeline(player, channel.pipeline().names());
                return; // Already injected
            }

            channel.pipeline().addBefore("packet_handler", "BedWars_AdminFilter", new ChannelDuplexHandler() {
                @Override
                public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                    InventorySyncAudit.onPacketInbound(player, msg);
                    super.channelRead(ctx, msg);
                }

                @Override
                public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                    if (msg.getClass().getSimpleName().equals("PacketPlayOutChat")) {
                        try {
                            Object component = FIELD_CHAT_COMPONENT.get(msg);
                            if (component != null) {
                                String content = component.toString();
                                if (content.contains("chat.type.admin")) {
                                    // Block system broadcast
                                    return; 
                                }
                            }
                        } catch (Exception e) {
                            // Ignore errors in filtering
                        }
                    } else if (BedWars.nms.isEquipmentPacket(msg)) {
                         try {
                             int entityId = BedWars.nms.getEquipmentEntityId(msg);
                             if (InvisibilityPotionListener.shouldHideArmor(entityId, player)) {
                                 msg = BedWars.nms.getEmptyEquipmentPacket(msg);
                             }
                         } catch (Exception e) {
                             // Ignore
                         }
                    }
                    InventorySyncAudit.onPacketOutbound(player, msg);
                    super.write(ctx, msg, promise);
                }
            });
            InventorySyncAudit.recordPipeline(player, channel.pipeline().names());

        } catch (Exception e) {
            BedWars.plugin.getLogger().log(Level.FINE, "Failed to inject packet filter for " + player.getName(), e);
        }
    }

    public static void uninject(Player player) {
        if (!enabled || !initialized) return;

        try {
            Object craftPlayer = player.getClass().getMethod("getHandle").invoke(player);
            Object connection = FIELD_PLAYER_CONNECTION.get(craftPlayer);
            Object networkManager = FIELD_NETWORK_MANAGER.get(connection);
            Channel channel = (Channel) FIELD_CHANNEL.get(networkManager);

            if (channel.pipeline().get("BedWars_AdminFilter") != null) {
                channel.pipeline().remove("BedWars_AdminFilter");
            }
        } catch (Exception e) {
            // Ignore
        }
    }
}
