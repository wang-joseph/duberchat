package duberchat.handlers.server;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import duberchat.chatutil.Channel;
import duberchat.chatutil.User;
import duberchat.events.ChannelCreateEvent;
import duberchat.events.FileWriteEvent;
import duberchat.events.RequestFailedEvent;
import duberchat.events.SerializableEvent;
import duberchat.handlers.Handleable;
import duberchat.server.ChatServer;

/**
 * A {@code ServerChannelCreateHandler} is a handler that processes channel creation.
 * <p>
 * The handler receives a {@code ChannelCreateEvent} from the server, which itself came from the 
 * client. The handler adds the channel to the file writing queue and propagates the event with an  
 * updated {@code Channel} object to all existant users who should be in the channel.
 * 
 * <p>
 * Since <b>2020-12-05</b>.
 * 
 * @since 1.0.0
 * @version 1.0.0
 * @author Paula Yuan
 */
public class ServerChannelCreateHandler implements Handleable {
  private ChatServer server;

  /**
   * Constructs a new {@code ServerChannelCreateHandler}.
   * 
   * @param server The associated server with this handler.
   */
  public ServerChannelCreateHandler(ChatServer server) {
    this.server = server;
  }

  /**
   * Handles the {@code ChannelCreateEvent}.
   * 
   * @param newEvent The {@code ChanenelCreateEvent} to handle..
   */
  public void handleEvent(SerializableEvent newEvent) {
    ChannelCreateEvent event = (ChannelCreateEvent) newEvent;
    ObjectOutputStream output = server.getCurUsers().get((User) event.getSource()).getOutputStream();
    HashSet<String> usersFound = new HashSet<>();
    ArrayList<User> channelUsers = new ArrayList<>();
    String channelName = event.getChannel().getChannelName();
    User creator = ((User) event.getSource());
    boolean foundAUser = false;
    Iterator<String> itr = event.getUsernames().iterator();
    while (itr.hasNext()) {
      String username = itr.next();
      // The channel creator is always automatically added, prevent them from being added twice
      if (username.equals(creator.getUsername())) continue;
      User user = server.getAllUsers().get(username);
      if (user != null) {
        foundAUser = true;
        usersFound.add(username);
        channelUsers.add(user);
      }
    }
    if (!foundAUser) {
      try {
        output.writeObject(new RequestFailedEvent(event));
        output.flush();
      } catch (IOException e) {
        e.printStackTrace();
      }
      return;
    }

    // If this channel is a dm, check if the dm already exists.
    // If this channel is an already existing dm, return that dm instead.
    usersFound.add(creator.getUsername());
    channelUsers.add(creator);
    if (channelUsers.size() == 2) {
      for (int channelId : creator.getChannels()) {
        Channel channel = server.getChannels().get(channelId);
        User user1 = channel.getUsers().get(0);
        User user2 = channel.getUsers().get(1);
        if ((channelUsers.get(0).equals(user1) && creator.equals(user2))
            || (channelUsers.get(0).equals(user2) && creator.equals(user1))) {
          try {
            output.writeObject(new ChannelCreateEvent((User) event.getSource(), channel, usersFound));
            output.flush();
          } catch (IOException e) {
            e.printStackTrace();
          }
          return;
        }
      }
    }

    HashSet<User> admins = new HashSet<>();
    admins.add(creator);
    // both parties in a dm are admins of the dm; otherwise, only the creator starts off as admin
    if (channelUsers.size() == 2) {
      admins.add(channelUsers.get(0));
    }
    int id = server.getNumChannelsCreated() + 1;
    server.setNumChannelsCreated(id);
    Channel newChannel = new Channel(channelName, id, channelUsers, admins, 0);
    server.getChannels().put(id, newChannel);

    try {
      // Make a new file and write the channel object to it.
      server.getFileWriteQueue().add(new FileWriteEvent(newChannel, "data/channels/" + id + ".txt"));
      
      // update all the users (and their files) with the new channel
      for (User user : channelUsers) {
        user.getChannels().add(id);
        String filePath = "data/users/" + user.getUsername() + ".txt";
        server.getFileWriteQueue().add(new FileWriteEvent(user, filePath));
      }

      // Output a corresponding event to the client who made the channel
      output.writeObject(new ChannelCreateEvent((User) event.getSource(), newChannel, usersFound));
      output.flush();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
