import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;
import java.net.*;
import java.io.*;

/**
 * A GUI client for a fair and improving chat room system.
 *
 * @author Jim Glenn
 * @author Lizzy Austad
 * @version 0.3 2015-11-13 added this comment (and be careful today, too)
 * @version 0.2 2012-03-15 minor edits to comments and names (and beware! again)
 * @version 0.1 3/15/2006 Beware!
 */

public class GUIChatClient extends JFrame
{
    /**
     * Components that are used by name outside the constructor.
     */

    // you decide which ones go here, but don't put more here
    // than you need

    /**
     * The writer used to send messages to the server.  <CODE>null</CODE>
     * if no connection has been made or if an error has occurred during
     * communication.
     */
    private PrintWriter toServer;

    /**
     * The screen name used by this client.
     */
    private String name;
    
    private JTextArea messageArea;
    private JTextField input;
    private JTextField server;
    private JTextField port;
    private JTextField screenName;
    
    /**
     * Creates a new chat room client.
     */
    public GUIChatClient()
    {
        Container contents = getContentPane();

        JPanel connectionPanel = new JPanel(new BorderLayout());

        JPanel connectionLabelPanel = new JPanel(new GridLayout(3, 1));
        connectionLabelPanel.add(new JLabel("Server:"));
        connectionLabelPanel.add(new JLabel("Port:"));
        connectionLabelPanel.add(new JLabel("Screen Name:"));

        JPanel connectButtonPanel = new JPanel();

        JPanel connectionInputPanel = new JPanel(new GridLayout(3, 1));

        JPanel outgoingMessagePanel = new JPanel(new BorderLayout());

        // create the "Connect" button
        JButton connectButton = new JButton("Connect");
        connectButton.addActionListener(new ConnectListener());
        // create the three text fields that are associated with
        // the connect button
        server = new JTextField();
        port = new JTextField();
        screenName = new JTextField();
        // create the central message display (check the javax.swing
        // documentation to find an appropriate component, and check
        // what name I'm expecting it to be called in showMessage)
        messageArea = new JTextArea();
        // create the text field used to get input from the user
        input = new JTextField();
        // create the "Send" button
        JButton sendButton = new JButton("Send");
        sendButton.addActionListener(new SendListener());
        // add the components to the panels and the panels
        // to the other panels and to the content pane

        connectButtonPanel.add(connectButton);

        connectionInputPanel.add(server);
        connectionInputPanel.add(port);
        connectionInputPanel.add(screenName);

        connectionPanel.add(connectionLabelPanel, BorderLayout.WEST);
        connectionPanel.add(connectionInputPanel, BorderLayout.CENTER);
        connectionPanel.add(connectButtonPanel, BorderLayout.EAST);

        outgoingMessagePanel.add(new JLabel("Message:"), BorderLayout.WEST);
        outgoingMessagePanel.add(input, BorderLayout.CENTER);
        outgoingMessagePanel.add(sendButton, BorderLayout.EAST);

        contents.setLayout(new BorderLayout());
        contents.add(connectionPanel, BorderLayout.NORTH);
        contents.add(messageArea, BorderLayout.CENTER);
        contents.add(outgoingMessagePanel, BorderLayout.SOUTH);

        setDefaultCloseOperation(EXIT_ON_CLOSE);

        setSize(400, 600);
        setVisible(true);
    }

    /**
     * Adds the given message to the message area in the middle of this
     * client's interface.
     *
     * @param mess the message to display
     */
    private void showMessage(String mess)
    {
        // remove the first row if there is too much text displayed
        // (this isn't really the right way to check, but getRows() doesn't
        // seem to do what one would think it should

        while (messageArea.getLineCount()
               >= messageArea.getHeight() / (messageArea.getFont().getSize() + 2))
            {
                // get starting and ending positions of the first
                // line and then erase it

                try
                    {
                        int firstLineStart = messageArea.getLineStartOffset(0);
                        int firstLineEnd = messageArea.getLineEndOffset(0);

                        messageArea.replaceRange(null, firstLineStart, firstLineEnd);
                    }
                catch (BadLocationException javaIsAnnoyingSometimes)
                    {
                    }
            }

        messageArea.append(mess);
        messageArea.append("\n");
    }
 
    /**
     * Connects this client to the given server using the given port.
     *
     * @param serverName the domain name of the server to connect to
     * @param port the port on which to connect to the server 
     * @param n the screen name to be used over the new connection
     */
    private void connect(String serverName, int port, String n)
    {
        showMessage("Connecting to server...");

        name = n;

        try
            {
                // establish the connection
                Socket conn = new Socket(InetAddress.getByName(serverName),
                                         port);
        
                // send the sign on message
                toServer = new PrintWriter(conn.getOutputStream(), true);
                toServer.println(name + ":SIGN-ON");

                // start a thread that receives other clients' messages
                // from the server
                new MessageReader(new BufferedReader(new InputStreamReader(conn.getInputStream()))).start();

                showMessage("Connected");
            }
        catch (IOException e)
            {
                showMessage("Failed to connect to server.");
                toServer = null;
            }
    }

    /**
     * Write an inner class that can be instantiated to make objects
     * that listen for ActionEvents fired by the message input text field
     * and the "Send" button.
     *
     * The event handler method should read the input from
     * the text field and use <CODE>println</CODE> to send it
     * through the <CODE>toServer</CODE> <CODE>PrintWriter</CODE>. 
     */

    private class SendListener implements ActionListener
    {
    	String message;
    	
    	public void actionPerformed(ActionEvent e) {
    		message = input.getText();
    		toServer.println(name+": "+ message);
    	}
    }

    /**
     * Write another inner class that can be instantiated to make
     * objects that handle events from the "Connect" button.
     *
     * The event handler method should read the values from the
     * three text fields at the top of the window and then
     * pass those into the <CODE>connect</CODE> method (note
     * that <CODE>connect</CODE> expects the <CODE>port</CODE> argument
     * to be an <CODE>int</CODE>.
     */
    private class ConnectListener implements ActionListener
    {
    	String s;
    	int p;
    	String sn;
    	
    	public void actionPerformed(ActionEvent e) {
    		s = server.getText();
    		p = Integer.parseInt(port.getText());
    		sn = screenName.getText();
    		connect(s,p,sn);
    	}
    }

    /**
     * Reads messages sent from the server and displays them in the
     * message area of this client.
     */

    private class MessageReader extends Thread
    {
        /**
         * The reader used to get messages from the server.
         */
        BufferedReader fromServer;

        /**
         * Creates a thread that reads messages from the given reader.
         *
         * @param in a reader that wraps the input stream of a connection
         * to a chat room server
         */
        public MessageReader(BufferedReader in)
        {
            fromServer = in;
        }

        public void run()
        {
            try
                {
                    String incomingMessage;
                    while ((incomingMessage = fromServer.readLine()) != null)
                        {
                            showMessage(incomingMessage);
                        }

                }
            catch (IOException e)
                {
                    showMessage("Connection to server lost");
                    toServer = null;
                }
        }
    }

    public static void main(String[] args)
    {
        new GUIChatClient();
    }
}