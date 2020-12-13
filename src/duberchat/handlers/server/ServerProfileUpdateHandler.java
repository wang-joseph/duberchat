package duberchat.handlers.server;

import duberchat.chatutil.User;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Iterator;

import duberchat.chatutil.Channel;
import duberchat.events.ClientProfileUpdateEvent;
import duberchat.events.FileWriteEvent;
import duberchat.events.SerializableEvent;
import duberchat.handlers.Handleable;
import duberchat.server.ChatServer;

public class ServerProfileUpdateHandler implements Handleable {
  private ChatServer server;

  public ServerProfileUpdateHandler(ChatServer server) {
    this.server = server;
  }

  public void handleEvent(SerializableEvent newEvent) {
    ClientProfileUpdateEvent event = (ClientProfileUpdateEvent) newEvent;
    User user = (User) event.getSource();
    User serverUser = server.getAllUsers().get(user.getUsername());
    
    if (serverUser.getStatus() != user.getStatus()) {
      serverUser.setStatus(user.getStatus());
    }

    if (!serverUser.pfpEquals(user.getPfp())) {
      serverUser.setPfp(user.getPfp());
      String filePath = "data/images/" + serverUser.getUsername();
      server.getFileWriteQueue().add(new FileWriteEvent(user.getPfp(), filePath));
    }

    // Update the user file
    String userFilePath = "data/users/" + user.getUsername();
    server.getFileWriteQueue().add(new FileWriteEvent(serverUser, userFilePath));

    // Send a status update event to every other user in every channel this user is in
    // Also, update every channel's file because the user information has changed. :/
    Iterator<Integer> setItr = serverUser.getChannels().iterator();
    while (setItr.hasNext()) {
      int channelId = setItr.next();
      Channel channel = server.getChannels().get(channelId);
      String channelFilePath = "data/channels/" + channelId;
      server.getFileWriteQueue().add(new FileWriteEvent(channel, channelFilePath));
      Iterator<User> itr = channel.getUsers().values().iterator();
      while (itr.hasNext()) {
        User member = itr.next();
        if (member.equals(serverUser) || !server.getCurUsers().containsKey(member)) {
          continue;
        }
        ObjectOutputStream output = server.getCurUsers().get(member).getOutputStream();
        try {
          output.writeObject(new ClientProfileUpdateEvent(new User(serverUser)));
          output.flush();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }

    // close down the appropriate client thread if the user logs off
    if (serverUser.getStatus() == 0) {
      server.getCurUsers().get(user).setRunning(false);
      server.getCurUsers().remove(user);
    }
  }
}