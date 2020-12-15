package duberchat.gui.frames;

import java.awt.Dimension;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;

import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;

/**
 * The {@code ServerFrame} is a frame to which server behaviour is written.
 * <p>
 * Any significant action taken by the server--e.g. accepting client connections
 * is logged onto this frame, with a certain amount of information regarding the
 * exact situation so as to provide context.
 * <p>
 * 2020-12-11
 * 
 * @since 1.00
 * @version 1.00
 * @author Paula Yuan
 */
@SuppressWarnings("serial")
public class ServerFrame extends JFrame {

  /** The text area on which to display server activity. */
  private JTextArea textArea;
  /** The scroll pane containing the frame's text area. */
  private JScrollPane scrollPane;
  /** The main and only display panel for this frame. */
  private JPanel mainPanel;

  /**
   * A constructor for the {@code ServerFrame}.
   */
  public ServerFrame() {
    this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    this.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
            setExtendedState(JFrame.ICONIFIED);
        }
    });
    
    this.mainPanel = new JPanel();
    this.textArea = new JTextArea();
    this.scrollPane = new JScrollPane(textArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                                      JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    this.textArea.setEditable(false); 
    this.mainPanel.add(scrollPane);
    this.add(mainPanel);
    this.setVisible(true);
    this.setSize(new Dimension(600, 600));
    this.mainPanel.setSize(this.getSize());
    this.scrollPane.setSize(this.getSize());
    this.mainPanel.setPreferredSize(this.getSize());
    this.scrollPane.setPreferredSize(this.getSize());
    this.setIconImage(new ImageIcon("data/system/logo.png").getImage());
  }

  /**
   * Retrieves the text area associated with this frame.
   * 
   * @return a {@code JTextArea}, the associated text area.
   */
  public JTextArea getTextArea() {
    return this.textArea;
  }
  
}
