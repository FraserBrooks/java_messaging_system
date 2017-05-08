import java.awt.BorderLayout;		
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.basic.BasicBorders;

import networkObjects.*;

public class ClientGUI extends JFrame{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    
    // Arbitrary negative number to specify default GridBagConstraint value
    public static final int DF = -424242;
    
    private ClientEventHandler mainHandler;
    
    
    private JLabel errorLabel = new JLabel();
    private JLabel connectingLabel = null;
    private int animationCounter = 3;
    private int notificationCount = 0;
    private Timer timer;
    
    // Buttons
    private JButton loginButton;
    private JButton registerButton;
    private JButton addFriendButton;
    private JButton removeFriendButton;
    private JButton createGroupButton;
    private JButton deleteGroupButton;
    private JButton addUserToGroupButton;
    private JButton removeUserFromGroupButton;
    private JButton setProfilePictureButton;
    private JButton sendButton;
    
    // Panels
    private JPanel leftPane;
    private JPanel centralPane;
    private JPanel rightPane;
    private JPanel popUpPane;
    private JPanel inputPane;
    private JPanel rightPaneButtonsPanel;
    private JPanel leftPaneButtonsPanel;
    private JPanel notificationWindow;
    private JPanel buttonActionWindow;
    private JSplitPane nestedRightSplitPane;
    private JSplitPane outerSplitPane;
    
    // Text Area for writing message
    private JTextArea inputArea;
    
    private DefaultListModel<Friend> friendListModel;
    private DefaultListModel<Group> groupListModel;
    private JScrollPane friendScrollPane;
    private JScrollPane groupScrollPane;
    private JScrollPane messageViewScrollPane;
    private JList<Friend> friendList;
    private JList<Group> groupList;
    
    // Text Fields and Check Box
    private JTextField buttonWindowTextField;
    private JCheckBox newMemberIsAdmin;
    public String username = null;
    
    // Dimensions
    private static Dimension minCol = new Dimension(40, 250);
    private static Dimension maxCol = new Dimension(400, 10000);
    
    // Colours
    public static final Color appOrange = new Color(239, 71, 36);
    public static final Color appBlue = new Color(28, 116, 150);
    public static final Color errorRed = new Color(241, 17, 0);
    public static final Color greyedOut = new Color(190,190,190);
    public static final Color successGreen = new Color(106,179,10);
    
    
    // Fonts
    public static Font merriweatherLarge = new Font("Merriweather", Font.PLAIN, 18);
    public static Font merriweather = new Font("Merriweather", Font.PLAIN, 16);
    public static Font merriweatherSmall = new Font("Merriweather", Font.PLAIN, 12);
    
    // Icons
    public static final ImageIcon defaultIcon = new ImageIcon("images/defaultIcon40x40.gif");
    public static final ImageIcon addIcon = new ImageIcon("images/addIcon40x40.gif");
    public static final ImageIcon removeIcon = new ImageIcon("images/removeIcon40x40.gif");
    public static final ImageIcon addUserIcon = new ImageIcon("images/addUserIcon40x40.gif");
    public static final ImageIcon removeUserIcon = new ImageIcon("images/removeUserIcon40x40.gif");
    public static final ImageIcon newProfilePicIcon = new ImageIcon("images/newProfilePicIcon40x40.gif");
    public static final ImageIcon highlightIcon = new ImageIcon("images/highlightIcon40x40.gif");
    public static final ImageIcon closeButtonIcon = new ImageIcon("images/closeIcon20x20.gif");
    public static final ImageIcon closeButtonHighlightIcon = new ImageIcon("images/closeIconHighlight20x20.gif");
    public static final ImageIcon sendIcon = new ImageIcon("images/sendIcon50x50.gif");
    public static final ImageIcon sendIconHighlight = new ImageIcon("images/sendIconHighlight50x50.gif");
    
    private int messageBoxSize = 60;
    private MessageWindow currentMessageWindow = null;
    
    // Used by Client Sender (MessageQueue is thread safe)
    public static final MessageQueue queueToServer = new MessageQueue();
    private ConcurrentMap<String, MessageWindow> activeConversations = new ConcurrentHashMap<String, MessageWindow>();
    private String currentlySelected = "none!";
    private boolean currentlySelectedIsGroup = true;
    
    @SuppressWarnings("serial")
    private class friendAndGroupJListCellRenderer extends JLabel implements ListCellRenderer<Object> {

        public Component getListCellRendererComponent(JList<?> list, // the list
                Object value, // value to display
                int index, // cell index
                boolean isSelected, // is the cell selected
                boolean cellHasFocus) // does the cell have focus
        {
            String s = "error";
            setFont(list.getFont());
            if (value.toString().substring(0, 6).equals("Friend")) {
                Friend f = (Friend) value;
                s = f.username;
                setFont(ClientGUI.merriweather);
                setHorizontalAlignment(SwingConstants.CENTER);
                setHorizontalTextPosition(SwingConstants.LEADING);
                setIconTextGap(20);
                if (f.icon != null) {
                    setIcon(f.icon);
                } else {
                    setIcon(ClientGUI.defaultIcon);
                }
            } else if (value.toString().substring(0, 6).equals("Group ")) {
                Group g = (Group) value;
                s = g.groupName;
                setFont(ClientGUI.merriweatherLarge);
                setHorizontalAlignment(SwingConstants.CENTER);
            }
            if (isSelected) {
                setBackground(ClientGUI.appOrange);
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            setText(s);
            setEnabled(list.isEnabled());
            setOpaque(true);
            return this;
        }
    }
    
    @SuppressWarnings("serial")
    private class NotificationButton extends JButton{
        private JPanel parentPanel;
        private String link;
        
        public void setParent(JPanel p){
            parentPanel = p;
        }
        
        public JPanel getParent(){
            return parentPanel;
        }
        
        public void setLink(String groupOrFriendName){
            link = groupOrFriendName;
        }
        
        public String getLink(){
            return link;
        }
    }
    
    private class groupSelectionListener implements ListSelectionListener{
        
        @Override
        public void valueChanged(ListSelectionEvent e){
            if(groupList.isSelectionEmpty()){
                return;
            }
            String selectedGroup = groupListModel.elementAt(groupList.getSelectedIndex()).groupName;
            MessageWindow mw = activeConversations.get(selectedGroup);
            if (mw == null) {
                mw = new MessageWindow();
                activeConversations.put(selectedGroup, mw);
                queueToServer.offer(new StartConversation(username, selectedGroup, true));
            }
            currentMessageWindow = mw;
            messageViewScrollPane.setViewportView(mw);
            currentlySelected = selectedGroup;
            friendList.clearSelection();
            currentlySelectedIsGroup = true;
            centralPane.add(messageViewScrollPane, BorderLayout.CENTER);
            centralPane.validate();
            if(mw.equals(currentMessageWindow)){
                messageViewScrollPane.getVerticalScrollBar().setValue(
                        messageViewScrollPane.getVerticalScrollBar().getMaximum());
            }
        }
    }

    private class friendSelectionListener implements ListSelectionListener {

        @Override
        public void valueChanged(ListSelectionEvent e) {
            if(friendList.isSelectionEmpty()){
                return;
            }
            String selectedFriend = friendListModel.elementAt(friendList.getSelectedIndex()).username;
            MessageWindow mw = activeConversations.get(selectedFriend);
            if (mw == null) {
                mw = new MessageWindow();
                activeConversations.put(selectedFriend, mw);
                queueToServer.offer(new StartConversation(username, selectedFriend, false));
            }
            currentMessageWindow = mw;
            messageViewScrollPane.setViewportView(mw);
            currentlySelected = selectedFriend;
            groupList.clearSelection();
            currentlySelectedIsGroup = false;
            centralPane.add(messageViewScrollPane, BorderLayout.CENTER);
            centralPane.validate();
            if(mw.equals(currentMessageWindow)){
                messageViewScrollPane.getVerticalScrollBar().setValue(
                        messageViewScrollPane.getVerticalScrollBar().getMaximum());
            }
        }
    }
    
    
    public ClientGUI(String serverAddress){
        
        Container c = getContentPane();
        c.setLayout(new GridBagLayout());
        
        JPanel topPanel = new JPanel(new GridBagLayout());
        JPanel midPanel = new JPanel(new GridBagLayout());
        JPanel bottomPanel = new JPanel(new GridBagLayout());
        JPanel footerPanel = new JPanel(new BorderLayout());
        
        
        JLabel welcomeLabel = new JLabel("Welcome to the messaging system.");
        welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        welcomeLabel.setFont(merriweatherLarge);
        topPanel.add(welcomeLabel, 
                createGBC(
                        // x/y
                        0,0,
                        // width/height
                        GridBagConstraints.REMAINDER, DF,
                        // fill/anchor
                        DF, DF,
                        // ipadx/y
                        DF, DF,
                        // weightX/Y
                        DF, 0.5,
                        // inset
                        new Insets(4, 50, 5, 50 )));
        
        errorLabel.setHorizontalAlignment(SwingConstants.CENTER);
        errorLabel.setFont(merriweatherSmall);
        errorLabel.setForeground(errorRed);
        topPanel.add(errorLabel, 
                createGBC(
                        // x/y
                        0,1,
                        // width/height
                        GridBagConstraints.REMAINDER, DF,
                        // fill/anchor
                        DF, DF,
                        // ipadx/y
                        DF, DF,
                        // weightX/Y
                        DF, 0.5,
                        // inset
                        new Insets(4, 50, 5, 50 )));
        
        
        connectingLabel = new JLabel("Connecting to server" + " " + " " + " ");
        connectingLabel.setVerticalAlignment(SwingConstants.BOTTOM);
        connectingLabel.setFont(merriweather);
        connectingLabel.setForeground(appOrange);
        footerPanel.add(connectingLabel, BorderLayout.EAST);
        
        JLabel uLabel = new JLabel("Username: ", SwingConstants.RIGHT);
        midPanel.add(uLabel, 
                createGBC(
                        // x/y
                        0,0,
                        // width/height
                        DF, DF,
                        // fill/anchor
                        DF, GridBagConstraints.LINE_END,
                        // ipadx/y
                        DF, DF,
                        // weightX/Y
                        DF, DF,
                        // inset
                        new Insets(10, 0, 10, 0 )));
        
        
        JTextField usernameField = new JTextField(20);
        usernameField.setActionCommand("usernameField");
        usernameField.setMinimumSize(new Dimension(140,0));
        midPanel.add(usernameField, 
                createGBC(
                        // x/y
                        1,0,
                        // width/height
                        DF, DF,
                        // fill/anchor
                        GridBagConstraints.BOTH, GridBagConstraints.LINE_START,
                        // ipadx/y
                        DF, DF,
                        // weightX/Y
                        DF, DF,
                        // inset
                        new Insets(10, 10, 10, 0 )));
        
        
        JLabel pLabel = new JLabel("Password: ", SwingConstants.RIGHT);
        midPanel.add(pLabel, 
                createGBC(
                        // x/y
                        0,1,
                        // width/height
                        DF, DF,
                        // fill/anchor
                        DF, GridBagConstraints.LINE_END,
                        // ipadx/y
                        DF, DF,
                        // weightX/Y
                        DF, DF,
                        // inset
                        new Insets(10, 0, 10, 0 )));
        
        
        JPasswordField passwordField = new JPasswordField(20);
        passwordField.setMinimumSize(new Dimension(140,0));
        passwordField.setActionCommand("passwordField");
        midPanel.add(passwordField, 
                createGBC(
                        // x/y
                        1,1,
                        // width/height
                        DF , DF,
                        // fill/anchor
                        GridBagConstraints.BOTH, GridBagConstraints.LINE_START,
                        // ipadx/y
                        DF, DF,
                        // weightX/Y
                        DF, DF,
                        // inset
                        new Insets(10, 10, 10, 0 )));
        
        
        loginButton = new JButton("Log In");
        loginButton.setActionCommand("loginButton");
        loginButton.setEnabled(false);
        bottomPanel.add(loginButton, 
                createGBC(
                        // x/y
                        0,0,
                        // width/height
                        DF, DF,
                        // fill/anchor
                        DF, DF,
                        // ipadx/y
                        DF, DF,
                        // weightX/Y
                        0.5, 0.2,
                        // inset
                        null));
        
        
        registerButton = new JButton("Register");
        registerButton.setActionCommand("registerButton");
        registerButton.setEnabled(false);
        bottomPanel.add(registerButton, 
                createGBC(
                        // x/y
                        1,0,
                        // width/height
                        DF, DF,
                        // fill/anchor
                        DF, DF,
                        // ipadx/y
                        DF, DF,
                        // weightX/Y
                        0.5, 0.2,
                        // inset
                        null));
        
        (new Thread(new ConnectWorker(this, serverAddress))).start();
        
        
        c.add(topPanel, 
                createGBC(
                        // x/y
                        0,0,
                        // width/height
                        DF, DF,
                        // fill/anchor
                        GridBagConstraints.BOTH, DF,
                        // ipadx/y
                        DF, DF,
                        // weightX/Y
                        DF, 0.3,
                        // inset
                        null));
        
        c.add(midPanel, 
                createGBC(
                        // x/y
                        0,1,
                        // width/height
                        DF, DF,
                        // fill/anchor
                        GridBagConstraints.BOTH, DF,
                        // ipadx/y
                        DF, DF,
                        // weightX/Y
                        DF, 0.5,
                        // inset
                        new Insets(0, 0, 20, 0 )));
        
        c.add(bottomPanel, 
                createGBC(
                        // x/y
                        0,2,
                        // width/height
                        DF, DF,
                        // fill/anchor
                        GridBagConstraints.BOTH, DF,
                        // ipadx/y
                        DF, DF,
                        // weightX/Y
                        DF, 0.2,
                        // inset
                        null));
        
        c.add(footerPanel, 
                createGBC(
                        // x/y
                        0,3,
                        // width/height
                        DF, DF,
                        // fill/anchor
                        GridBagConstraints.BOTH, DF,
                        // ipadx/y
                        DF, 20,
                        // weightX/Y
                        DF, DF,
                        // inset
                        new Insets(0, 0, 0, 20 )));
        
        ClientAuthEventHandler cAEV = new ClientAuthEventHandler(this, usernameField, passwordField, registerButton, midPanel, errorLabel);
        
        usernameField.addActionListener(cAEV);
        passwordField.addActionListener(cAEV);
        loginButton.addActionListener(cAEV);
        registerButton.addActionListener(cAEV);
        
        
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Messaging System"); 
        setSize(400, 250);
        setMinimumSize(new Dimension(400, 250));
        setMaximumSize(new Dimension(400, 250));
        setResizable(false);
        setVisible(true);
        
        
        class tListener implements ActionListener {
            private ClientGUI cGUI;
            
            tListener(ClientGUI c){
                cGUI = c;
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                cGUI.updateLabel();
            }
        };
        
        
        timer = new Timer(500, new tListener(this));
        timer.start();
        
    }
    
    public void updateLabel() {
        String s = "Connecting to server";
        for(int i = animationCounter%4; i > 0  ;i--){
            s += ".";
        }
        for(int i = 3 - animationCounter%4; i > 0  ;i--){
            s += " ";
        }
        connectingLabel.setText(s);
        animationCounter++;
    }

    public static GridBagConstraints createGBC(int gridx, int gridy, int width, int height, int fill, int anchor, int ipadx, int ipady, double weightx, double weighty, Insets inset){
        GridBagConstraints gbc = new GridBagConstraints();
        if (gridx != DF) {
            gbc.gridx = gridx;
        }
        if (gridy != DF) {
            gbc.gridy = gridy;
        }
        if (width != DF) {
            gbc.gridwidth = width;
        }
        if (height != DF) {
            gbc.gridheight = height;
        }
        if (fill != DF) {
            gbc.fill = fill;
        }
        if (anchor != DF) {
            gbc.anchor = anchor;
        }
        if (ipadx != DF) {
            gbc.ipadx = ipadx;
        }
        if (ipady != DF) {
            gbc.ipady = ipady;
        }
        if (weightx != DF) {
            gbc.weightx = weightx;
        }
        if (weighty != DF) {
            gbc.weighty = weighty;
        }
        if (inset != null) {
            gbc.insets = inset;
        }
        return gbc;
    }

    public void connectedToServer() {
        timer.stop();
        connectingLabel.setText("CONNECTED ");
        connectingLabel.setForeground(ClientGUI.successGreen);
        loginButton.setEnabled(true);
        registerButton.setEnabled(true);
    }
    
    public void initialiseMainUi(ClientInfoObject cio){
        Report.behaviour("Logged In, Loading Main UI");
        setVisible(false);
        
        username = cio.clientUsername;
        mainHandler = new ClientEventHandler(this);
        
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                ClientGUI gui = (ClientGUI) e.getSource();
                gui.windowResized();
            }
        });
        
        Container c = getContentPane();
        c.removeAll();
        c.setLayout(new BorderLayout());
        c.setFont(merriweather);
        
        
        leftPane = new JPanel(new BorderLayout());
        rightPane = new JPanel(new BorderLayout());
        centralPane = new JPanel(new BorderLayout());
        
        popUpPane = new JPanel(new BorderLayout());
        inputPane = new JPanel(new GridBagLayout());
        //messageView = new JPanel(new BorderLayout());
        messageViewScrollPane = new JScrollPane();
        
        popUpPane.setOpaque(true);
        popUpPane.setBackground(greyedOut);
        inputPane.setBackground(Color.WHITE);
        inputPane.setMaximumSize(new Dimension(Short.MAX_VALUE, 200));
        
        centralPane.add(popUpPane, BorderLayout.NORTH);
        centralPane.add(inputPane, BorderLayout.SOUTH);
        
        
        inputArea = new JTextArea(2, messageBoxSize);
        inputArea.setEditable(true);
        inputArea.setBackground(greyedOut);
        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);
        //inputArea.setMinimumSize(new Dimension(300, 30));
        //inputArea.setMaximumSize(new Dimension(Short.MAX_VALUE, 200));
        //inputPane.setMaximumSize(new Dimension(Short.MAX_VALUE, 300));
        JScrollPane inputScroll = new JScrollPane(inputArea);
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(inputScroll, BorderLayout.CENTER);
        //inputScroll.setPreferredSize(new Dimension(800,50));
        inputPane.add(inputPanel, createGBC(0, 0, DF, DF, DF, DF, DF, DF, 0.8, 0.5, new Insets(10, 20, 10, 20)));
        sendButton = new JButton(sendIcon);
        //sendButton.setPreferredSize(new Dimension(60,60));
        setMainUIButtonDefaults(sendButton);
        sendButton.setRolloverIcon(sendIconHighlight);
        sendButton.setActionCommand("sendMessage");
        inputPane.add(sendButton, createGBC(1, 0, DF, DF, DF, DF, DF, DF, 0.2, 0.5, new Insets(10, 10, 10, 10)));
        
        nestedRightSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, centralPane, rightPane);
        outerSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPane, nestedRightSplitPane);
        c.add(outerSplitPane, BorderLayout.CENTER);
        
        leftPane.setMinimumSize(minCol);
        rightPane.setMinimumSize(minCol);
        leftPane.setMaximumSize(maxCol);
        rightPane.setMaximumSize(maxCol);
        centralPane.setMinimumSize(new Dimension(500, 250));
        nestedRightSplitPane.setMinimumSize(new Dimension(600, 250));
        outerSplitPane.setMinimumSize(new Dimension(600, 250));
        
        centralPane.setPreferredSize(new Dimension(600, 800));
        leftPane.setPreferredSize(new Dimension(400, 800));
        rightPane.setPreferredSize(new Dimension(600, 800));
        
        nestedRightSplitPane.setResizeWeight(0.5);
        outerSplitPane.setResizeWeight(0.33);
        outerSplitPane.resetToPreferredSizes();;
        
        // Initialise friend/group list scroll panes
        friendListModel = new DefaultListModel<Friend>();
        for(Friend f : cio.friendList){
            friendListModel.addElement(f);
        }
        
        groupListModel = new DefaultListModel<Group>();
        for(Group g : cio.groupList){
            groupListModel.addElement(g);
        }
        
        friendList = new JList<Friend>(friendListModel);
        friendList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        friendList.setCellRenderer(new friendAndGroupJListCellRenderer());
        friendList.setFixedCellHeight(50);
        friendList.addListSelectionListener(new friendSelectionListener());
        friendList.setSelectedIndex(-1);
        
        groupList = new JList<Group>(groupListModel);
        groupList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        groupList.setCellRenderer(new friendAndGroupJListCellRenderer());
        groupList.setFixedCellHeight(40);
        groupList.addListSelectionListener(new groupSelectionListener());
        groupList.setSelectedIndex(0);
        
        friendScrollPane = new JScrollPane(friendList);
        groupScrollPane = new JScrollPane(groupList);
        
        
        rightPane.add(friendScrollPane, BorderLayout.CENTER);
        leftPane.add(groupScrollPane, BorderLayout.CENTER);
        
        rightPaneButtonsPanel = new JPanel(new GridBagLayout());
        leftPaneButtonsPanel = new JPanel(new GridBagLayout());
        

        addFriendButton = new JButton(addIcon);
        addFriendButton.setActionCommand("addFriendButton");
        setMainUIButtonDefaults(addFriendButton);
        
        removeFriendButton = new JButton(removeIcon);
        removeFriendButton.setActionCommand("removeFriendButton");
        setMainUIButtonDefaults(removeFriendButton);
        
        createGroupButton = new JButton(addIcon);
        createGroupButton.setActionCommand("createGroupButton");
        setMainUIButtonDefaults(createGroupButton);
        
        deleteGroupButton = new JButton(removeIcon);
        deleteGroupButton.setActionCommand("deleteGroupButton");
        setMainUIButtonDefaults(deleteGroupButton);
        
        addUserToGroupButton = new JButton(addUserIcon);
        addUserToGroupButton.setActionCommand("addUserToGroupButton");
        setMainUIButtonDefaults(addUserToGroupButton);

        
        removeUserFromGroupButton = new JButton(removeUserIcon);
        removeUserFromGroupButton.setActionCommand("removeUserFromGroupButton");
        setMainUIButtonDefaults(removeUserFromGroupButton);

        
        setProfilePictureButton = new JButton(newProfilePicIcon);
        setProfilePictureButton.setActionCommand("setProfilePictureButton");
        setMainUIButtonDefaults(setProfilePictureButton);

        
        rightPaneButtonsPanel.add(setProfilePictureButton, createGBC(0, 0, DF, DF, DF, DF, DF, DF, 0.5, 0.5, new Insets(20, 0, 20, 0)));
        rightPaneButtonsPanel.add(addFriendButton, createGBC(1, 0, DF, DF, DF, DF, DF, DF, 0.5, 0.5, null));
        rightPaneButtonsPanel.add(removeFriendButton, createGBC(2, 0, DF, DF, DF, DF, DF, DF, 0.5, 0.5, null));
        rightPaneButtonsPanel.setBackground(greyedOut);
        
        leftPaneButtonsPanel.add(createGroupButton, createGBC(0, 0, DF, DF, DF, DF, DF, DF, 0.5, 0.5, new Insets(20, 0, 20, 0)));
        leftPaneButtonsPanel.add(deleteGroupButton, createGBC(1, 0, DF, DF, DF, DF, DF, DF, 0.5, 0.5, null));
        leftPaneButtonsPanel.add(addUserToGroupButton, createGBC(2, 0, DF, DF, DF, DF, DF, DF, 0.5, 0.5, null));
        leftPaneButtonsPanel.add(removeUserFromGroupButton, createGBC(3, 0, DF, DF, DF, DF, DF, DF, 0.5, 0.5, null));
        leftPaneButtonsPanel.setBackground(greyedOut);
        
        rightPane.add(rightPaneButtonsPanel, BorderLayout.NORTH);
        leftPane.add(leftPaneButtonsPanel, BorderLayout.NORTH);
        
        buttonActionWindow = new JPanel(new GridBagLayout());
        notificationWindow = new JPanel(new GridBagLayout());
        buttonActionWindow.setBackground(greyedOut);
        buttonActionWindow.setBorder(new BasicBorders.MenuBarBorder(Color.BLACK, Color.BLACK));
        notificationWindow.setBackground(greyedOut);
        notificationWindow.setBorder(new BasicBorders.MenuBarBorder(Color.BLACK, Color.BLACK));
 
        
        for(Notification n: cio.notificationList){
            newNotification(n);
        }
        
        popUpPane.add(buttonActionWindow, BorderLayout.SOUTH);
        popUpPane.add(notificationWindow, BorderLayout.NORTH);
        
        setSize(1200, 800);
        setMinimumSize(new Dimension(800, 250));
        setMaximumSize(null);
        setResizable(true);
        setVisible(true);
        validate();
    }
    
    protected void windowResized() {
        //Report.behaviour("windowResizeEvent: call to windowResized was made.");
        int width = centralPane.getWidth();
        while (true) {
            if (width > 1200) {
                inputArea.setColumns(90);
                break;
            }
            if (width > 800) {
                inputArea.setColumns(80);
                break;
            }
            if (width > 700) {
                inputArea.setColumns(70);

                break;
            }
            if (width > 600) {
                inputArea.setColumns(55);
                inputPane.validate();
                break;
            }
            inputArea.setColumns(45);
            break;
        }
        
        //currentMessageWindow.setMaxMessageSize(width);
        
        inputPane.validate();
        
        
    }
    

    
    private void setMainUIButtonDefaults(JButton b){
        b.addActionListener(mainHandler);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setBorder(null);
        b.setMargin(new Insets(0, 0, 0, 0));
        b.setContentAreaFilled(false);
        b.setRolloverIcon(highlightIcon);
    }
    
    

    public void openButtonWindow(String buttonPressed) {
        
        buttonActionWindow.removeAll();
        centralPane.validate();
        
        Friend selectedFriend;
        Group selectedGroup;
        
        JLabel headerLabel = new JLabel();
        headerLabel.setFont(merriweatherLarge);
        JLabel label = new JLabel();
        label.setFont(merriweather);
        
        buttonWindowTextField = new JTextField(30);
        buttonWindowTextField.addActionListener(mainHandler);
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(mainHandler);
        cancelButton.setActionCommand("cancel");
        JButton actionButton = new JButton();
        actionButton.addActionListener(mainHandler);
        
        newMemberIsAdmin = new JCheckBox("Make this user an admin: ");
        newMemberIsAdmin.setSelected(false);
        newMemberIsAdmin.setVisible(false);
        newMemberIsAdmin.setOpaque(false);
        
        
        switch (buttonPressed) {
        
        case "addFriend":
            headerLabel.setText("Add New Friend");
            label.setText("Enter username of friend and a friend request will be sent: ");
            actionButton.setText("Send Request");
            actionButton.setActionCommand("sendFriendRequest");
            break;
            
        case "removeFriend":
            selectedFriend = friendListModel.get(friendList.getSelectedIndex());
            headerLabel.setText("Remove " + selectedFriend.username  + " from your friend list? ");
            label.setVisible(false);
            buttonWindowTextField.setVisible(false);
            actionButton.setText(" Yes ");
            actionButton.setActionCommand("removeSelectedFriend");
            break;
            
        case "createGroup":
            headerLabel.setText("Create New Group");
            label.setText("Enter the name for your new group:" );
            actionButton.setText("Create Group");
            actionButton.setActionCommand("newGroupRequest");
            break;
            
        case "deleteGroup":
            selectedGroup = groupListModel.get(groupList.getSelectedIndex());
            buttonWindowTextField.setVisible(false);
            if(selectedGroup.clientIsOwner){
                label.setText("As you're the owner, this will delete the group for everyone!" );
                label.setForeground(errorRed);
            }else{
                label.setVisible(false);
            } 
            headerLabel.setText("Remove yourself from the " + selectedGroup.groupName + " group?");
            actionButton.setText(" Yes ");
            actionButton.setActionCommand("deleteSelectedGroup");
            break;
            
        case "addToGroup":
            selectedGroup = groupListModel.get(groupList.getSelectedIndex());
            if(selectedGroup.clientIsAdmin){
                headerLabel.setText("Add User To " + selectedGroup.groupName + " Group");
                label.setText("Enter the username of user to add: " );
                actionButton.setText("Add User");
                newMemberIsAdmin.setVisible(true);
                actionButton.setActionCommand("addUserToGroup");
            }else{
                newNotification(new Notification(new Message("You must be an admin to add users to a group."), selectedGroup.groupName));
                cancelButton.doClick();
                return;
            }
            
            break;
            
        case "removeFromGroup":
            selectedGroup = groupListModel.get(groupList.getSelectedIndex());
            if(selectedGroup.clientIsAdmin){
                headerLabel.setText("Remove User From " + selectedGroup.groupName + " Group");
                label.setText("Enter the username of user to remove: " );
                actionButton.setText("Remove User");
                actionButton.setActionCommand("removeUserFromGroup");
            }else{
                newNotification(new Notification(new Message("You must be an admin to remove other users from a group."), selectedGroup.groupName));
                cancelButton.doClick();
                return;
            }
            
            break;
            
        case "setProfilePic":
            headerLabel.setText("Set Profile Pic");
            label.setText("##########" );
            actionButton.setText("Update Picture");
            break;
            
        }
        
        buttonActionWindow.add(headerLabel, createGBC(0, 0, 2, DF, DF, DF, 10, 10, 0.5, 0.5, null));
        buttonActionWindow.add(label, createGBC(0, 1, 2, DF, DF, DF, 10, 10, 0.5, 0.5, null));
        buttonActionWindow.add(buttonWindowTextField, createGBC(0, 2, 2, DF, DF, DF, 10, 10, 0.5, 0.5, new Insets(10, 0, 20, 0)));
        buttonActionWindow.add(newMemberIsAdmin, createGBC(0, 3, 2, DF, DF, DF, DF, DF, 0.5, 0.5, new Insets(0, 0, 20, 0)));
        buttonActionWindow.add(actionButton, createGBC(0, 4, DF, DF, DF, GridBagConstraints.LINE_END, 10, 10, 0.5, 0.5, new Insets(0, 0, 20, 30)));
        buttonActionWindow.add(cancelButton, createGBC(1, 4, DF, DF, DF, GridBagConstraints.LINE_START, 10, 10, 0.5, 0.5, new Insets(0, 0, 20, 30)));
        buttonActionWindow.add(errorLabel, createGBC(0, 5, 2, DF, DF, DF, DF, DF, 0.5, 0.2, new Insets(0, 0, 10, 0)));
        
        centralPane.validate();
        
    }
    
    
    public void closeButtonWindow() {
        buttonActionWindow.removeAll();
        centralPane.validate();
    }
    
    

    public void sendFriendRequest() {
        String friend = buttonWindowTextField.getText();
        queueToServer.offer(new FriendRequest(username, friend));
        closeButtonWindow();
    }

    public void removeSelectedFriend() {
        Friend selectedFriend = friendListModel.get(friendList.getSelectedIndex());
        queueToServer.offer(new RemoveFriend(username, selectedFriend.username));
        friendListModel.removeElement(selectedFriend);
        closeButtonWindow();
    }

    public void newGroupRequest() {
        String groupName = buttonWindowTextField.getText();
        queueToServer.offer(new CreateGroup(username, groupName));
        closeButtonWindow();
    }

    public void removeSelectedGroup() {
        Group selectedGroup = groupListModel.get(groupList.getSelectedIndex());
        // Delete the entire group and every user's assocciated membership if clientIsOwner
        // otherwise just remove clients membership. Either way remove it locally from groupListModel
        queueToServer.offer(new RemoveGroup(username, selectedGroup.groupName, selectedGroup.clientIsOwner));
        groupListModel.removeElement(selectedGroup);
        closeButtonWindow();
    }

    public void addUserToGroup() {
        Group selectedGroup = groupListModel.get(groupList.getSelectedIndex());
        String newMember = buttonWindowTextField.getText();
        queueToServer.offer(new AddUserToGroup(username, newMember, selectedGroup.groupName, newMemberIsAdmin.isSelected()));
        closeButtonWindow();
    }

    public void removeUserFromGroup() {
        String userToRemove = buttonWindowTextField.getText();
        Group selectedGroup = groupListModel.get(groupList.getSelectedIndex());
        queueToServer.offer(new RemoveUserFromGroup(username, userToRemove, selectedGroup.groupName));
        closeButtonWindow();
        
    }

    public void newServerMessage(ServerMessage sm, boolean loggedOn) {
        Report.behaviour("Received ServerMessage object from stream.");
        if(!loggedOn){
            errorLabel.setText(sm.message.content);
            return;
        }

        Report.behaviour("Received ServerMessage: \n " + sm.toString());
        if(sm.newFriend){
            friendListModel.addElement(sm.friend);
        }
        if(sm.newGroup){
            groupListModel.addElement(sm.group);
        }
        
    }

    public void newMessage(NetworkMessage nm) {
        MessageWindow mw;
        if((mw = activeConversations.get(nm.recipient)) != null){
            mw.addMessage(nm.message, nm.sender.equals(username), nm.sender);
        }else if((mw = activeConversations.get(nm.sender)) != null){
            mw = activeConversations.get(nm.sender);
            mw.addMessage(nm.message, nm.sender.equals(username), nm.sender);
        }else{
            // This should not happen
            Report.behaviour("Error: Received Network Message for un-initialised conversation. ");
        }
        centralPane.validate();
        if(mw.equals(currentMessageWindow)){
            messageViewScrollPane.getVerticalScrollBar().setValue(
                    messageViewScrollPane.getVerticalScrollBar().getMaximum());
        }
    }

    public void newNotification(Notification n) {
        JPanel notificationPanel = new JPanel(new GridBagLayout());
        JLabel notificationDate = new JLabel(n.notificationText.time.getTime().toString());
        NotificationButton notificationContent = new NotificationButton();
        NotificationButton notificationCloser = new NotificationButton();
        
        setMainUIButtonDefaults(notificationContent);
        setMainUIButtonDefaults(notificationCloser);
        
        notificationContent.setText(n.notificationText.content);
        notificationContent.setLink(n.sender);
        
        notificationCloser.setIcon(closeButtonIcon);
        notificationCloser.setRolloverIcon(closeButtonHighlightIcon);
        
        notificationDate.setFont(merriweatherSmall);
        notificationContent.setFont(merriweather);
        
        notificationContent.setParent(notificationPanel);
        notificationCloser.setParent(notificationPanel);
        
        notificationContent.setActionCommand("notificationClick");
        notificationCloser.setActionCommand("notificationClose");
        
        notificationPanel.setBorder(new BasicBorders.MenuBarBorder(Color.BLACK, Color.BLACK));
        notificationPanel.add(notificationContent, createGBC(0, 0, DF, DF, DF, DF, DF, DF, 0.8, 0.8, new Insets(5, 0, 2, 15)));
        notificationPanel.add(notificationDate, createGBC(0, 1, DF, DF, DF, DF, DF, DF, 0.8, 0.2, new Insets(0, 0, 5, 15)));
        notificationPanel.add(notificationCloser, createGBC(1, 0, DF, 2, DF, DF, DF, DF, 0.2, 0.5, new Insets(5, 0, 5, 15)));
        notificationWindow.add(notificationPanel, createGBC(0, notificationCount++, DF, DF, GridBagConstraints.HORIZONTAL, DF, DF, DF, 0.5, 0.5, new Insets(0, 0, 0, 0)));
        centralPane.validate();
    }

    public void removeNotification(Object source) {
        NotificationButton nb = (NotificationButton) source;
        notificationWindow.remove(nb.getParent());
        centralPane.validate();
    }
    
    public void notificationClick(Object source){
        NotificationButton nb = (NotificationButton) source;
        notificationWindow.remove(nb.getParent());
        centralPane.validate();
        String groupOrFriend = nb.getLink();
        for(int i = 0; i < friendListModel.size(); i++ ){
            Friend f = friendListModel.getElementAt(i);
            if(f.username.equals(groupOrFriend)){
                friendList.setSelectedIndex(i);
                return;
            }
        }
        for(int i = 0; i < groupListModel.size(); i++ ){
            Group g = groupListModel.getElementAt(i);
            if(g.groupName.equals(groupOrFriend)){
                groupList.setSelectedIndex(i);
                return;
            }
        }
        
        // If notification doesn't point to a local group
        // or friend then it could be a friend request
        // If so the user has accepted it so send response to server
        String label = nb.getText();
        if(label.substring(label.length() - 16).equals("Click to accept.")){
            queueToServer.offer(new FriendRequest(username, groupOrFriend));
        }
        
        
    }

    public void sendMessage() {
        String message = inputArea.getText();
        inputArea.setText("");
        queueToServer.offer(new NetworkMessage(username, currentlySelected, new Message(message), currentlySelectedIsGroup));
        
    }
   
}
