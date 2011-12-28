package com.raibaz.lupus;

import java.util.List;

import com.google.appengine.api.channel.ChannelMessage;
import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;
import com.raibaz.lupus.game.AbsJsonable;
import com.raibaz.lupus.game.Game;
import com.raibaz.lupus.game.Player;
import com.raibaz.lupus.game.PlayerRole;

public class LupusMessage extends AbsJsonable {
	
	public enum MessageType {
		JOIN,
		CHAT,
		GAMESTATE,
		VOTE,
		NIGHTVOTE,
		NIGHTINFO,
		ROLEINFO
	}		
	
	private MessageType type;
	private String msg;
	private Player player;
	private Player target;
	private Player next;
	private String gameId;
	private List<Player> nominated;
	
	public LupusMessage(MessageType type, Player p) {
		this.type = type;
		this.player = p;
	}
	
	public MessageType getType() {
		return type;
	}
	public void setType(MessageType type) {
		this.type = type;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public Player getPlayer() {
		return player;
	}
	public void setPlayer(Player player) {
		this.player = player;
	}
	public Player getTarget() {
		return target;
	}
	public void setTarget(Player target) {
		this.target = target;
	}

	public String getGameId() {
		return gameId;
	}

	public void setGameId(String gameId) {
		this.gameId = gameId;
	}

	public Player getNext() {
		return next;
	}

	public void setNext(Player next) {
		this.next = next;
	}
	
	public List<Player> getNominated() {
		return nominated;
	}

	public void setNominated(List<Player> nominated) {
		this.nominated = nominated;
	}

	public void broadcastToPlayingPlayers(Game g) {
		ChannelService chanServ = ChannelServiceFactory.getChannelService();
		for(Player p : g.getPlayers()) {
			chanServ.sendMessage(new ChannelMessage(p.getFbId() + "-playing", this.toJSONString()));			
		}
	}
	
	public void broadcastToPlayersByRole(Game g, PlayerRole role) {
		ChannelService chanServ = ChannelServiceFactory.getChannelService();
		for(Player p : g.getPlayers()) {
			if(p.getRole() == role) {
				chanServ.sendMessage(new ChannelMessage(p.getFbId() + "-playing", this.toJSONString()));
			}
		}
	}
}
