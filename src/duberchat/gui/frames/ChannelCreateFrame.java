package duberchat.gui.frames;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Color;
import java.awt.Insets;

import java.util.HashSet;
import java.util.Arrays;

import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;

import duberchat.events.ChannelCreateEvent;
import duberchat.gui.filters.TextLengthFilter;
import duberchat.gui.util.ComponentFactory;
import duberchat.client.ChatClient;
import duberchat.chatutil.Channel;
import duberchat.chatutil.User;

@SuppressWarnings("serial")
public class ChannelCreateFrame extends DynamicGridbagFrame {
    public static final Dimension DEFAULT_SIZE = new Dimension(400, 500);

    private ChatClient client;

    JPanel mainPanel;
    JTextField nameField;
    JTextField usersField;
    JButton submitButton;

    GridBagLayout layout;
    GridBagConstraints constraints;

    private boolean alreadySentRequest = false;

    public ChannelCreateFrame(ChatClient client) {
        super("Add a new channel");

        this.client = client;
        
        this.setResizable(false);
        this.setSize(ChannelCreateFrame.DEFAULT_SIZE);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setIconImage(new ImageIcon("data/system/logo.png").getImage());

        mainPanel = new JPanel();
        mainPanel.setBackground(MainFrame.MAIN_COLOR);

        layout = new GridBagLayout();
        constraints = new GridBagConstraints();

        mainPanel.setLayout(layout);
        mainPanel.setSize(this.getSize());

        nameField = ComponentFactory.createTextBox(20, MainFrame.BRIGHT_TEXT_COLOR, MainFrame.TEXTBOX_COLOR,
                new TextLengthFilter(50), BorderFactory.createEmptyBorder(5, 5, 5, 5));
        nameField.setMinimumSize(nameField.getPreferredSize());
        usersField = ComponentFactory.createTextBox(30, MainFrame.BRIGHT_TEXT_COLOR, MainFrame.TEXTBOX_COLOR,
                new TextLengthFilter(2000), BorderFactory.createEmptyBorder(5, 5, 5, 5));
        usersField.setMinimumSize(usersField.getPreferredSize());

        JLabel channelNameLabel = ComponentFactory.createLabel("Channel Name:");
        JLabel userLabel = ComponentFactory.createLabel("Invite users.");
        JLabel userDescriptionLabel = ComponentFactory.createLabel("Only existing users will be invited.",
                MainFrame.TEXT_COLOR);
        JLabel userSyntaxLabel = ComponentFactory.createLabel("Seperate users using a comma, and put @ before name.",
                MainFrame.TEXT_COLOR);
        JLabel userExampleLabel = ComponentFactory.createLabel("(eg. @EmeraldPhony, @PolyEntities)",
                MainFrame.TEXT_COLOR);

        submitButton = ComponentFactory.createButton("Start DuberChatting!", MainFrame.MAIN_COLOR, MainFrame.TEXT_COLOR,
                new CreateChannelActionListener());

        addConstrainedComponent(channelNameLabel, mainPanel, layout, constraints, 0, 0, 1, 1,
                GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER, new Insets(0, 0, 0, 0));
        addConstrainedComponent(nameField, mainPanel, layout, constraints, 0, 1, 1, 1, GridBagConstraints.HORIZONTAL,
                GridBagConstraints.CENTER, new Insets(8, 0, 8, 0));
        addConstrainedComponent(userLabel, mainPanel, layout, constraints, 0, 2, 1, 1, GridBagConstraints.HORIZONTAL,
                GridBagConstraints.CENTER, new Insets(30, 0, 0, 0));
        addConstrainedComponent(userDescriptionLabel, mainPanel, layout, constraints, 0, 3, 1, 1,
                GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER, new Insets(4, 0, 4, 0));
        addConstrainedComponent(userSyntaxLabel, mainPanel, layout, constraints, 0, 4, 1, 1,
                GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER, new Insets(4, 0, 4, 0));
        addConstrainedComponent(userExampleLabel, mainPanel, layout, constraints, 0, 5, 1, 1,
                GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER, new Insets(4, 0, 8, 0));
        addConstrainedComponent(usersField, mainPanel, layout, constraints, 0, 6, 1, 3, GridBagConstraints.BOTH,
                GridBagConstraints.CENTER, new Insets(8, 0, 8, 0));
        addConstrainedComponent(submitButton, mainPanel, layout, constraints, 0, 9, 1, 1, GridBagConstraints.REMAINDER,
                GridBagConstraints.CENTER, new Insets(8, 0, 8, 0));

        this.add(mainPanel);
    }

    /**
     * Requests a reload for this frame.
     * <p>
     * This method should only be called if channel creation failed.
     */
    public void reload() {
        this.handleFailedEvent();
    }

    private void handleFailedEvent() {
        JLabel failedText = ComponentFactory.createLabel("Failed. Add at least one existing user!", Color.RED);

        addConstrainedComponent(failedText, mainPanel, layout, constraints, 0, 10, 1, 1, GridBagConstraints.REMAINDER,
                GridBagConstraints.CENTER, new Insets(8, 0, 8, 0));

        mainPanel.revalidate();
        mainPanel.repaint();

        alreadySentRequest = false;
    }

    private class CreateChannelActionListener implements ActionListener {
        public void actionPerformed(ActionEvent evt) {
            if (alreadySentRequest) {
                return;
            }

            String channelName = nameField.getText();
            String[] users = usersField.getText().replace("@", "").split(", *");

            System.out.println(Arrays.toString(users));
            HashSet<String> usernames = new HashSet<>();

            for (String user : users) {
                usernames.add(user);
            }

            Channel newChannel = new Channel(channelName);
            User clientUser = new User(client.getUser());
            client.offerEvent(new ChannelCreateEvent(clientUser, newChannel, usernames));

            alreadySentRequest = true;
            System.out.println("SYSTEM: Created new channel event.");
        }
    }
}
