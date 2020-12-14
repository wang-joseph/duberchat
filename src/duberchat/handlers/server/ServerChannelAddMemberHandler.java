package duberchat.handlers.server;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;

import duberchat.chatutil.*;
import duberchat.events.ChannelAddMemberEvent;
import duberchat.events.FileWriteEvent;
import duberchat.events.RequestFailedEvent;
import duberchat.events.SerializableEvent;
import duberchat.handlers.Handleable;
import duberchat.server.ChatServer;

public class ServerChannelAddMemberHandler implements Handleable {
  private ChatServer server;

  public ServerChannelAddMemberHandler(ChatServer server) {
    this.server = server;
  }
   
  public void handleEvent(SerializableEvent newEvent) {
    ChannelAddMemberEvent event = (ChannelAddMemberEvent) newEvent;
    Channel toAddTo = event.getChannel();
    Channel serverToAddTo = server.getChannels().get(toAddTo.getChannelId());
    String newUserUsername = event.getNewUserUsername();
    User toAdd = server.getAllUsers().get(newUserUsername);
    User source = server.getAllUsers().get(((User) event.getSource()).getUsername());
    int id = toAddTo.getChannelId();
    
    try {
      // If the user doesn't exist, send back a request failed event
      if (toAdd == null) {
        ObjectOutputStream output = server.getCurUsers().get((User) event.getSource()).getOutputStream();
        output.writeObject(new RequestFailedEvent(new User(source)));
        output.flush();
        server.getServerFrame().getTextArea()
            .append(source.getUsername() + " tried to add an nonexistent user to a channel\n");
        return;
      }
      
      toAdd.getChannels().add(id);
      serverToAddTo.addUser(toAdd);
      toAddTo.addUser(toAdd);   

      // Update this channel's file to include the new user.
      String channelFilePath = "data/channels/" + id;
      server.getFileWriteQueue().add(new FileWriteEvent(serverToAddTo, channelFilePath));

      // Update the new member's user file to reflect their addition to the channel
      String userFilePath = "data/users/" + toAdd.getUsername();
      server.getFileWriteQueue().add(new FileWriteEvent(toAdd, userFilePath));

      // Send back a add member event to every online user in the channel
      Iterator<User> itr = serverToAddTo.getUsers().values().iterator();
      while (itr.hasNext()) {
        User member = itr.next();
        System.out.println(member.getUsername());
        // skip offline users
        if (!server.getCurUsers().containsKey(member)) {
          continue;
        }
        ObjectOutputStream output = server.getCurUsers().get(member).getOutputStream();
        output.writeObject(new ChannelAddMemberEvent(new User(source), new Channel(toAddTo), 
                                                     newUserUsername));
        output.flush();
      }
      server.getServerFrame().getTextArea().append(
          newUserUsername + " added to channel " + id + " by " + source.getUsername() + " and events sent to users\n");
    } catch (IOException e) {
      e.printStackTrace();
    }

  }
}
