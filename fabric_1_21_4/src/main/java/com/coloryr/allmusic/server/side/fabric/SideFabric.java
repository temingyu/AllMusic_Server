package com.coloryr.allmusic.server.side.fabric;

import com.coloryr.allmusic.server.AllMusicFabric;
import com.coloryr.allmusic.server.PackPayload;
import com.coloryr.allmusic.server.TaskItem;
import com.coloryr.allmusic.server.Tasks;
import com.coloryr.allmusic.server.core.AllMusic;
import com.coloryr.allmusic.server.core.objs.config.SaveObj;
import com.coloryr.allmusic.server.core.objs.enums.ComType;
import com.coloryr.allmusic.server.core.objs.enums.HudType;
import com.coloryr.allmusic.server.core.objs.music.MusicObj;
import com.coloryr.allmusic.server.core.objs.music.SongInfoObj;
import com.coloryr.allmusic.server.core.side.BaseSide;
import com.coloryr.allmusic.server.core.utils.HudUtils;
import com.coloryr.allmusic.server.side.fabric.event.MusicAddEvent;
import com.coloryr.allmusic.server.side.fabric.event.MusicPlayEvent;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SideFabric extends BaseSide {

    @Override
    public void reload() {
        String path = "allmusic/";
        new AllMusic().init(new File(path));
    }

    @Override
    public int getPlayerSize() {
        return AllMusicFabric.server.getCurrentPlayerCount();
    }

    @Override
    public void runTask(Runnable run) {
        AllMusicFabric.server.execute(run);
    }

    @Override
    public void runTask(Runnable run1, int delay) {
        Tasks.add(new TaskItem() {{
            tick = delay;
            run = run1;
        }});
    }

    @Override
    public boolean checkPermission(Object player) {
        ServerCommandSource source = (ServerCommandSource) player;
        return source.hasPermissionLevel(2);
    }

    @Override
    public boolean isPlayer(Object player) {
        ServerCommandSource source = (ServerCommandSource) player;
        return source.isExecutedByPlayer();
    }

    @Override
    public boolean checkPermission(Object player, String permission) {
        return checkPermission(player);
    }

    @Override
    public boolean needPlay() {
        for (var player : AllMusicFabric.server.getPlayerManager().getPlayerList()) {
            if (!AllMusic.isSkip(player.getName().getString(), null, false)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void sideSendStop() {
        try {
            for (var player : AllMusicFabric.server.getPlayerManager().getPlayerList()) {
                send(player, new PackPayload(ComType.STOP, null, 0));
            }
        } catch (Exception e) {
            AllMusic.log.warning("§d[AllMusic]§c停止指令发送出错");
            e.printStackTrace();
        }
    }

    @Override
    protected void sideSendStop(String name) {
        try {
            var player = AllMusicFabric.server.getPlayerManager().getPlayer(name);
            if (player == null)
                return;
            send(player, new PackPayload(ComType.STOP, null, 0));
        } catch (Exception e) {
            AllMusic.log.warning("§d[AllMusic]§c停止指令发送出错");
            e.printStackTrace();
        }
    }

    @Override
    public void sendMusic(String data) {
        try {
            for (var player : AllMusicFabric.server.getPlayerManager().getPlayerList()) {
                if (AllMusic.isSkip(player.getName().getString(), null, false))
                    continue;
                send(player, new PackPayload(ComType.PLAY, data, 0));
                AllMusic.addNowPlayPlayer(player.getName().getString());
            }
        } catch (Exception e) {
            AllMusic.log.warning("§d[AllMusic]§c歌曲指令发送出错");
            e.printStackTrace();
        }
    }

    @Override
    protected void sideSendMusic(String player, String data) {
        try {
            var player1 = AllMusicFabric.server.getPlayerManager().getPlayer(player);
            if (player1 == null)
                return;
            if (AllMusic.isSkip(player, null, false))
                return;
            send(player1, new PackPayload(ComType.PLAY, data, 0));
        } catch (Exception e) {
            AllMusic.log.warning("§d[AllMusic]§c歌曲指令发送出错");
            e.printStackTrace();
        }
    }

    @Override
    public void sendPic(String data) {
        try {
            for (var player : AllMusicFabric.server.getPlayerManager().getPlayerList()) {
                if (AllMusic.isSkip(player.getName().getString(), null, true))
                    continue;
                String name = player.getName().getString();
                SaveObj obj = HudUtils.get(name);
                if (!obj.pic.enable)
                    continue;
                send(player, new PackPayload(ComType.IMG, data, 0));
            }
        } catch (Exception e) {
            AllMusic.log.warning("§d[AllMusic]§c图片指令发送出错");
            e.printStackTrace();
        }
    }

    @Override
    public void sendPic(String player, String data) {
        try {
            var player1 = AllMusicFabric.server.getPlayerManager().getPlayer(player);
            if (player1 == null)
                return;
            if (AllMusic.isSkip(player1.getName().getString(), null, true))
                return;
            send(player1, new PackPayload(ComType.IMG, data, 0));
        } catch (Exception e) {
            AllMusic.log.warning("§d[AllMusic]§c图片指令发送出错");
            e.printStackTrace();
        }
    }

    @Override
    public void sendPos(String player, int pos) {
        try {
            var player1 = AllMusicFabric.server.getPlayerManager().getPlayer(player);
            if (player1 == null)
                return;
            if (AllMusic.isSkip(player1.getName().getString(), null, true))
                return;
            send(player1, new PackPayload(ComType.POS, null, pos));
        } catch (Exception e) {
            AllMusic.log.warning("§d[AllMusic]§c清空Hud发生出错");
            e.printStackTrace();
        }
    }

    @Override
    public void sendHudLyric(String data) {
        try {
            for (var player : AllMusicFabric.server.getPlayerManager().getPlayerList()) {
                if (AllMusic.isSkip(player.getName().getString(), null, true))
                    continue;
                String name = player.getName().getString();
                SaveObj obj = HudUtils.get(name);
                if (!obj.lyric.enable)
                    continue;
                send(player, new PackPayload(ComType.LYRIC, data, 0));
            }
        } catch (Exception e) {
            AllMusic.log.warning("§d[AllMusic]§c歌词发送出错");
            e.printStackTrace();
        }
    }

    @Override
    public void sendHudInfo(String data) {
        try {
            for (var player : AllMusicFabric.server.getPlayerManager().getPlayerList()) {
                if (AllMusic.isSkip(player.getName().getString(), null, true))
                    continue;
                String name = player.getName().getString();
                SaveObj obj = HudUtils.get(name);
                if (!obj.info.enable)
                    continue;
                send(player, new PackPayload(ComType.INFO, data, 0));
            }
        } catch (Exception e) {
            AllMusic.log.warning("§d[AllMusic]§c歌词信息发送出错");
            e.printStackTrace();
        }
    }

    @Override
    public void sendHudPos(String name) {
        try {
            var player = AllMusicFabric.server.getPlayerManager().getPlayer(name);
            if (player == null)
                return;
            SaveObj obj = HudUtils.get(name);
            String data = AllMusic.gson.toJson(obj);
            send(player, new PackPayload(ComType.HUD, data, 0));
        } catch (Exception e) {
            AllMusic.log.warning("§d[AllMusic]§c界面位置发送出错");
            e.printStackTrace();
        }
    }

    @Override
    public void sendHud(String name, HudType pos, String data) {
        try {
            if (pos == HudType.PIC) {
                return;
            }
            var player = AllMusicFabric.server.getPlayerManager().getPlayer(name);
            if (player == null)
                return;

            if (AllMusic.isSkip(name, null, true))
                return;

            switch (pos) {
                case INFO:
                    send(player, new PackPayload(ComType.INFO, data, 0));
                    break;
                case LIST:
                    send(player, new PackPayload(ComType.LIST, data, 0));
                    break;
                case LYRIC:
                    send(player, new PackPayload(ComType.LYRIC, data, 0));
                    break;
            }
        } catch (Exception e) {
            AllMusic.log.warning("§d[AllMusic]§c停止指令发送出错");
            e.printStackTrace();
        }
    }

    @Override
    public void sendHudList(String data) {
        try {
            for (var player : AllMusicFabric.server.getPlayerManager().getPlayerList()) {
                if (AllMusic.isSkip(player.getName().getString(), null, true))
                    continue;
                String name = player.getName().getString();
                SaveObj obj = HudUtils.get(name);
                if (!obj.list.enable)
                    continue;
                send(player, new PackPayload(ComType.LIST, data, 0));
            }
        } catch (Exception e) {
            AllMusic.log.warning("§d[AllMusic]§c歌曲列表发送出错");
            e.printStackTrace();
        }
    }

    @Override
    public void sendHudUtilsAll() {
        for (var player : AllMusicFabric.server.getPlayerManager().getPlayerList()) {
            String Name = player.getName().getString();
            try {
                SaveObj obj = HudUtils.get(Name);
                String data = AllMusic.gson.toJson(obj);
                send(player, new PackPayload(ComType.HUD, data, 0));
            } catch (Exception e1) {
                AllMusic.log.warning("§d[AllMusic]§c数据发送发生错误");
                e1.printStackTrace();
            }
        }
    }

    @Override
    public void sendBar(String data) {
        for (var player : AllMusicFabric.server.getPlayerManager().getPlayerList()) {
            try {
                if (AllMusic.isSkip(player.getName().getString(), null, true))
                    continue;
                FabricApi.sendBar(player, data);
            } catch (Exception e1) {
                AllMusic.log.warning("§d[AllMusic]§c数据发送发生错误");
                e1.printStackTrace();
            }
        }
    }

    @Override
    public void clearHud(String name) {
        try {
            var player = AllMusicFabric.server.getPlayerManager().getPlayer(name);
            if (player == null)
                return;
            send(player, new PackPayload(ComType.CLEAR, null, 0));
        } catch (Exception e) {
            AllMusic.log.warning("§d[AllMusic]§c清空Hud发生出错");
            e.printStackTrace();
        }
    }

    @Override
    public void clearHud() {
        try {
            for (var player : AllMusicFabric.server.getPlayerManager().getPlayerList()) {
                send(player, new PackPayload(ComType.CLEAR, null, 0));
            }
        } catch (Exception e) {
            AllMusic.log.warning("§d[AllMusic]§c清空Hud发生出错");
            e.printStackTrace();
        }
    }

    @Override
    public void broadcast(String data) {
        for (var player : AllMusicFabric.server.getPlayerManager().getPlayerList()) {
            if (!AllMusic.isSkip(player.getName().getString(), null, false)) {
                player.sendMessage(Text.of(data), false);
            }
        }
    }

    @Override
    public void broadcastWithRun(String message, String end, String command) {
        FabricApi.sendMessageBqRun(message, end, command);
    }

    @Override
    public void sendMessage(Object obj, String message) {
        ServerCommandSource source = (ServerCommandSource) obj;
        source.sendMessage(Text.of(message));
    }

    @Override
    public void sendMessageRun(Object obj, String message, String end, String command) {
        FabricApi.sendMessageRun(obj, message, end, command);
    }

    @Override
    public void sendMessageSuggest(Object obj, String message, String end, String command) {
        FabricApi.sendMessageSuggest(obj, message, end, command);
    }

    @Override
    public boolean onMusicPlay(SongInfoObj obj) {
        return MusicPlayEvent.EVENT.invoker().interact(obj) != ActionResult.PASS;
    }

    @Override
    public boolean onMusicAdd(Object obj, MusicObj music) {
        ServerCommandSource source = (ServerCommandSource) obj;
        return MusicAddEvent.EVENT.invoker().interact(source.getPlayer(), music) != ActionResult.PASS;
    }

    @Override
    public List<String> getPlayerList() {
        var list = new ArrayList<String>();
        for (var item : AllMusicFabric.server.getPlayerManager().getPlayerList()) {
            list.add(item.getGameProfile().getName());
        }
        return list;
    }

    private void send(ServerPlayerEntity players, PackPayload data) {
        if (players == null)
            return;
        try {
            runTask(() -> ServerPlayNetworking.send(players, data));
        } catch (Exception e) {
            AllMusic.log.warning("§c数据发送发生错误");
            e.printStackTrace();
        }
    }
}
