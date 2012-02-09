package com.force.data;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;

import com.force.data.forceapi.ForceMetadataAPI;
import com.force.data.mysql.MySQLAccess;
import com.force.data.vo.FieldDefinition;
import com.force.data.vo.TableSchema;
import com.sforce.soap.enterprise.EnterpriseConnection;
import com.sforce.soap.enterprise.LoginResult;
import com.sforce.soap.enterprise.sobject.Account;
import com.sforce.soap.metadata.AsyncResult;
import com.sforce.soap.metadata.CustomField;
import com.sforce.soap.metadata.CustomObject;
import com.sforce.soap.metadata.DeploymentStatus;
import com.sforce.soap.metadata.FieldType;
import com.sforce.soap.metadata.Metadata;
import com.sforce.soap.metadata.MetadataConnection;
import com.sforce.soap.metadata.SharingModel;
import com.sforce.ws.ConnectorConfig;

public class ForceSchemaBuilder extends JFrame
{
	//private EnterpriseConnection connection;
	private JTextField sfUser;
	private JPasswordField sfpass;
	private JPasswordField mySqlPass;
	private JTextField mySQLUser;
	private JTextField mySQLUrl;
	private JComboBox environment;
	private JTextArea statusMessages;
	private JTextField mySQLdb;
    private JTree tree;
    private JTree cloudTree;
    private DefaultMutableTreeNode top;
    private DefaultMutableTreeNode cloudTop;
	private DefaultMutableTreeNode dbNode;
	private DefaultMutableTreeNode clouddbNode;
	private ArrayList<TableSchema> localTableSchema;
	private ArrayList<TableSchema> cloudTableSchema;
	private JScrollPane treeView;
	private JScrollPane cloudTreeView;
	private ForceMetadataAPI forceMetaAPI;
	private final int LABEL_LENGTH = 150;
	private final int FIELD_LENGTH = 250;
	private final int X1_ALIGN = 10;
	private final int X1A_ALIGN = 165;
	private final int X2_ALIGN = 425;
	private final int X2A_ALIGN = 580;
	private final int STANDARD_HEIGHT = 25;
	private final int X_ROW_INCREMENTS = 30;
	private final int Y_ROW_1 = 60;
	private final int Y_ROW_2 = 90;
	private final int Y_ROW_3 = 120;
	private final int Y_ROW_4 = 150;
	private final int Y_ROW_5 = 180;
	private final int Y_ROW_6 = 210;
	private final int Y_ROW_7 = 240;
	private Icon displayPhoto;
	
    public ForceSchemaBuilder() 
    {
    	forceMetaAPI = new ForceMetadataAPI();
    	initUI();
    }

    public final void initUI() 
    {    	
    	localTableSchema = new ArrayList<TableSchema>();
        setTitle("MySQL Exporter for Force.com");
        setSize(850, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        JPanel panel = new JPanel();
        getContentPane().add(panel);

        panel.setLayout(null);

    	java.net.URL imageURL =  ForceSchemaBuilder.class.getResource("CloudIcon.png");
    	if (imageURL != null) 
    	{
    	    ImageIcon icon = new ImageIcon(imageURL);
    	    this.setIconImage(icon.getImage());
        	JLabel appIcon = new JLabel();
        	appIcon.setText("<html><body><p>MySQL Exporter for Force.com<br>Cory Cowgill 2012<br>Source Available on Github: <a href=\"http://www.github.com/corycowgill\">http://www.github.com/corycowgill</a></p></body></html>");
        	appIcon.setBounds(5, 5,500, 50);
        	appIcon.setIcon(icon);
        	panel.add(appIcon);
        	
    	}        
        
        JLabel mySQLUserLabel = new JLabel();
        mySQLUserLabel.setBounds(X1_ALIGN,Y_ROW_1,LABEL_LENGTH,STANDARD_HEIGHT);
        mySQLUserLabel.setText("MySQL Username:");
        panel.add(mySQLUserLabel);        
 
        mySQLUser = new JTextField();
        mySQLUser.setBounds(X1A_ALIGN,Y_ROW_1,FIELD_LENGTH,STANDARD_HEIGHT);
        panel.add(mySQLUser);        
        
        JLabel mySQLPassLabel = new JLabel();
        mySQLPassLabel.setBounds(X1_ALIGN,Y_ROW_2,LABEL_LENGTH,STANDARD_HEIGHT);
        mySQLPassLabel.setText("MySQL Password:");
        panel.add(mySQLPassLabel);       
 
        mySqlPass = new JPasswordField();
        mySqlPass.setBounds(X1A_ALIGN,Y_ROW_2,FIELD_LENGTH,STANDARD_HEIGHT);
        panel.add(mySqlPass);

        JLabel mySQLUrlLabel = new JLabel();
        mySQLUrlLabel.setText("MySQL Server:");
        mySQLUrlLabel.setBounds(X1_ALIGN,Y_ROW_3,LABEL_LENGTH,STANDARD_HEIGHT);
        panel.add(mySQLUrlLabel);
        
        mySQLUrl = new JTextField();
        mySQLUrl.setBounds(X1A_ALIGN,Y_ROW_3,FIELD_LENGTH,STANDARD_HEIGHT);
        panel.add(mySQLUrl); 

        JLabel mySQLdbLabel = new JLabel();
        mySQLdbLabel.setText("MySQL Database:");
        mySQLdbLabel.setBounds(X1_ALIGN,Y_ROW_4,LABEL_LENGTH,STANDARD_HEIGHT);
        panel.add(mySQLdbLabel);
        
        mySQLdb = new JTextField();
        mySQLdb.setBounds(X1A_ALIGN,Y_ROW_4,FIELD_LENGTH,STANDARD_HEIGHT);
        panel.add(mySQLdb);       
        
        JLabel sfUserLabel = new JLabel();
        sfUserLabel.setBounds(X2_ALIGN,Y_ROW_1,LABEL_LENGTH,STANDARD_HEIGHT);
        sfUserLabel.setText("SF Username:");
        panel.add(sfUserLabel);        
        
        sfUser = new JTextField();
        sfUser.setBounds(X2A_ALIGN,Y_ROW_1,FIELD_LENGTH,STANDARD_HEIGHT);
        panel.add(sfUser);
        
        JLabel sfPassLabel = new JLabel();
        sfPassLabel.setBounds(X2_ALIGN,Y_ROW_2,LABEL_LENGTH,STANDARD_HEIGHT);
        sfPassLabel.setText("SF Password:");
        panel.add(sfPassLabel);
 
        sfpass = new JPasswordField();
        sfpass.setBounds(X2A_ALIGN,Y_ROW_2,FIELD_LENGTH,STANDARD_HEIGHT);
        panel.add(sfpass);        
        
        JLabel sfEnviroLabel = new JLabel();
        sfEnviroLabel.setText("SF Environment: ");
        sfEnviroLabel.setBounds(X2_ALIGN, Y_ROW_3, LABEL_LENGTH, STANDARD_HEIGHT);
        panel.add(sfEnviroLabel);
        
        environment = new JComboBox(new String[]{"Production/Developer","Sandbox"});
        environment.setBounds(X2A_ALIGN, Y_ROW_3, FIELD_LENGTH, STANDARD_HEIGHT);
        panel.add(environment);
        
        top = new DefaultMutableTreeNode("Need to read Local DB...");

        tree = new JTree(top);
        tree.getSelectionModel().setSelectionMode
                    (TreeSelectionModel.SINGLE_TREE_SELECTION);        
     
        treeView = new JScrollPane(tree);
        treeView.setBounds(X1_ALIGN,Y_ROW_7,FIELD_LENGTH + LABEL_LENGTH,250);
        Dimension minimumSize = new Dimension(100, 50);
        panel.add(treeView);
            
        cloudTop = new DefaultMutableTreeNode("Need to read Cloud Schema...");

        cloudTree = new JTree(cloudTop);
        cloudTree.getSelectionModel().setSelectionMode
                    (TreeSelectionModel.SINGLE_TREE_SELECTION);        
     
        cloudTreeView = new JScrollPane(cloudTree);
        cloudTreeView.setBounds(X2_ALIGN,Y_ROW_7,FIELD_LENGTH + LABEL_LENGTH,250);

        panel.add(cloudTreeView);
        
        
        JLabel sfMessageLabel = new JLabel();
        sfMessageLabel.setText("System Messages");
        sfMessageLabel.setBounds(X1_ALIGN,525,200,STANDARD_HEIGHT);
        panel.add(sfMessageLabel);
        
        statusMessages = new JTextArea();
        statusMessages.setBounds(X1_ALIGN,550,800,100);
        panel.add(statusMessages);
        
        
        JButton readSchemaButton = new JButton("Read Local Schema");
        readSchemaButton.setBounds(X1_ALIGN, Y_ROW_5, 400, 30);
        readSchemaButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
            	try
            	{
            		statusMessages.setText("Starting Schema Read.....");
            		readLocalSchema();
            		statusMessages.setText(statusMessages.getText() +'\n'+ "Finished Schema Read.....");
            	}catch(Exception e)
            	{
            		statusMessages.setText(statusMessages.getText() +'\n' + "ERROR: " + e.getMessage());
            	}
           }
        });

        panel.add(readSchemaButton);

        JButton moveSchemaButton = new JButton("Upload Local Schema to Force.com");
        moveSchemaButton.setBounds(X2_ALIGN, 510, 400, 30);
        moveSchemaButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
            	try
            	{
            		statusMessages.setText("Starting Schema Upload.....");
            		localTableSchema = forceMetaAPI.uploadSchema(sfUser.getText(), sfpass.getText(),localTableSchema);
            		statusMessages.setText(statusMessages.getText() +'\n'+ "Finished Schema Upload.");
            	}catch(Exception e)
            	{
            		statusMessages.setText(statusMessages.getText() +'\n' + "ERROR: " + e.getMessage());
            	}
           }
        });

        panel.add(moveSchemaButton);        

        JButton getSFSchemaButton = new JButton("Download Force.com Schema");
        getSFSchemaButton.setBounds(X2_ALIGN, Y_ROW_5, 400, 30);
        getSFSchemaButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
            	try
            	{
            		statusMessages.setText("Starting Schema Download. This could take up to a few minutes........");
            		cloudTableSchema = forceMetaAPI.downloadSchema(sfUser.getText(), sfpass.getText());
            		createCloudNodes();
            		statusMessages.setText(statusMessages.getText() +'\n'+ "Finished Schema Download.");
            	}catch(Exception e)
            	{
            		statusMessages.setText(statusMessages.getText() +'\n' + "ERROR: " + e.getMessage());
            		e.printStackTrace();
            	}
           }
        });

        panel.add(getSFSchemaButton);        
        
        
        
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        

        
     }
	
	/**
	 * @param args 
	 */
	public static void main(String[] args) throws Exception
	{		
		SwingUtilities.invokeLater(new Runnable() 
		{
            public void run() 
            {
            	ForceSchemaBuilder ex = new ForceSchemaBuilder();
                ex.setVisible(true);
            }
        });
	}
	
	

	
	public void readLocalSchema() throws Exception
	{
		System.out.println("Starting Force Schema Builder");
		MySQLAccess dao = new MySQLAccess();
		localTableSchema = dao.readDataBase(mySQLUser.getText(),mySQLUser.getText(),mySQLUrl.getText(), mySQLdb.getText());
		createNodes();
		this.repaint();
	}
	
	private void createCloudNodes()
	{
		cloudTop.setUserObject("Force.com Schema");
		cloudTop.removeAllChildren();

		for(TableSchema cloudTable : cloudTableSchema)
		{
			DefaultMutableTreeNode tableNode = new DefaultMutableTreeNode(cloudTable.getTableName());
			cloudTop.add(tableNode);
			for(FieldDefinition fd : cloudTable.getFields())
			{
				DefaultMutableTreeNode fieldNode = new DefaultMutableTreeNode(fd.getName() + " (Type: " + fd.getType() + ")");
				tableNode.add(fieldNode);
			}
		}
		this.repaint();
	}
	
	private void createNodes() 
	{
		top.setUserObject(mySQLdb.getText());
		top.removeAllChildren();

		for(TableSchema localTable : localTableSchema)
		{
			DefaultMutableTreeNode tableNode = new DefaultMutableTreeNode(localTable.getTableName());
			top.add(tableNode);
			for(FieldDefinition fd : localTable.getFields())
			{
				DefaultMutableTreeNode fieldNode = new DefaultMutableTreeNode(fd.getName() + " (Type: " + fd.getType() + ")");
				tableNode.add(fieldNode);
			}
		}
	}
}
