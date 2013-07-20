package org.demo.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.demo.model.Song;
import org.demo.util.DemoHelper;

@SuppressWarnings("serial")
public class DemoUI extends JFrame {

    JPanel startPanel = new JPanel();
    JPanel queryPanel = new JPanel();
    JPanel tablePanel = new JPanel();
    Log LOG = LogFactory.getLog(DemoUI.class);
    public DemoUI() {
        setTitle("Demo - Hibernate Search");        
        initPanels();
        JTabbedPane tabbedPane = initTabbedPane();
        getContentPane().add(tabbedPane);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 400);
    }

    private void initPanels() {
        JTable dataTable = new JTable();
        initStartPanel(dataTable);
        initQueryPanel();
        initTablePanel(dataTable);
    }

    private void initQueryPanel() {
        JTextField queryField = new JTextField();
        JLabel infoLabel = new JLabel();
        JTable queryResultTable = new JTable();
        JComboBox queryTypeComboBox = new JComboBox(DemoHelper.QUERY_TYPES);
        JComboBox fieldComboBox = new JComboBox(new String[] {"title","artist","album","notes"});
        fieldComboBox.setEnabled(false);
        infoLabel.setText("<html>"+DemoHelper.QUERY_TYPES_DESC[0]);
        queryTypeComboBox.addItemListener(new QueryTypeChangeListener(infoLabel, fieldComboBox));
        Box querySettingBox = new Box(BoxLayout.X_AXIS);

        querySettingBox.add(new JLabel(" Query Type : "));
        querySettingBox.add(queryTypeComboBox);
        querySettingBox.add(new JLabel(" Field : "));
        querySettingBox.add(fieldComboBox);
        
        querySettingBox.add(new JLabel(" Criteria : "));
        querySettingBox.add(queryField);
        JButton runButton = new JButton("Run");
        querySettingBox.add(runButton);
        runButton.addActionListener(new RunActionListener(queryTypeComboBox,queryField,fieldComboBox,queryResultTable));

        Box holder = new Box(BoxLayout.Y_AXIS);
        holder.add(querySettingBox);
        holder.add(infoLabel);
        
        
        queryPanel.setLayout(new BorderLayout());
        queryPanel.add(holder, BorderLayout.PAGE_START);
        queryPanel.add(new JScrollPane(queryResultTable), BorderLayout.CENTER);
        queryPanel.setEnabled(false);
    }

    private void initStartPanel(JTable dataTable) {
        JButton populateButton = new JButton("Populate Database Table");
        JProgressBar progressBar = new JProgressBar();
        populateButton.addActionListener(new PopulateAction(dataTable, progressBar));
        startPanel.setLayout(new BorderLayout());
        JLabel msgLabel = new JLabel("<html><ul>" +
                "<li>Start by populating the test database table by clicking button below</li>" +
                "<li>Use Search tab to try out Hibernate-Search queries</li>" +
                "<li>Use Database Table tab to review data in database</li></ul>");
        Box buttonHolder = new Box(BoxLayout.X_AXIS);
        buttonHolder.add(populateButton);
        buttonHolder.add(new JLabel(" Load Status: "));
        buttonHolder.add(progressBar);
        msgLabel.setBorder(BorderFactory.createLineBorder(Color.lightGray));
        startPanel.add(msgLabel, BorderLayout.PAGE_START);
        startPanel.add(buttonHolder, BorderLayout.PAGE_END);


    }

    private JTabbedPane initTabbedPane() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Start", startPanel);
        tabbedPane.addTab("Query", queryPanel);
        tabbedPane.addTab("Table Data", tablePanel);
        return tabbedPane;
    }

    private void initTablePanel(JTable dataTable) {
        tablePanel.setLayout(new BorderLayout());
        tablePanel.add(new JScrollPane(dataTable), BorderLayout.CENTER);
    }

    class SongTableModel extends AbstractTableModel {

        List<Song> songs;
        final String[] COLS = new String[]{"Title", "Artist", "Album", "Notes"};

        public SongTableModel(List<Song> songs) {
            this.songs = songs;
        }

        public int getColumnCount() {
            return 4;
        }

        public int getRowCount() {
            return songs == null ? 0 : songs.size();
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            if (songs != null && !songs.isEmpty()) {
                Song s = songs.get(rowIndex);
                switch (columnIndex) {
                    case 0:
                        return s.getTitle();
                    case 1:
                        return s.getArtist();
                    case 2:
                        return s.getAlbum();
                    case 3:
                        return s.getNotes();
                    default:
                        return null;
                }
            } else {
                return null;
            }
        }

        @Override
        public String getColumnName(int column) {
            return COLS[column];
        }

        public void setData(List<Song> songs) {
            this.songs = songs;
        }
    }
    
    //button click handler for populate. Observes DemoHelper to get
    //status of load %
    class PopulateAction implements ActionListener, Observer {

        JTable tableToRefresh;
        JProgressBar progressBar;
        JButton sourceButton;
        DemoHelper helper;
        public PopulateAction(JTable tableToRefresh, JProgressBar progressBarToUpdate) {
            this.tableToRefresh = tableToRefresh;
            this.progressBar = progressBarToUpdate;
        }

        public void actionPerformed(ActionEvent e) {
            helper = DemoHelper.getDemoHelper();
            helper.addObserver(this);
            sourceButton = ((JButton) e.getSource());
            sourceButton.setEnabled(false);
            helper.insertData();
        }

        public void update(Observable o, Object arg) {
            progressBar.setValue(helper.getPercentage());
            if(helper.getPercentage() == 100) {
                SongTableModel model = new SongTableModel(helper.getAllRecords());
                tableToRefresh.setModel(model);
                ((SongTableModel) tableToRefresh.getModel()).fireTableDataChanged();                
                sourceButton.setText("Data Loaded");
            }
        }
    }
    
    
    class QueryTypeChangeListener implements ItemListener{
        JLabel infoLabel;
        JComboBox fieldCombo;
        public QueryTypeChangeListener(JLabel l,JComboBox fieldCombo){
            this.infoLabel = l;
            this.fieldCombo = fieldCombo;
        }
        public void itemStateChanged(ItemEvent e) {
            JComboBox combo = (JComboBox)e.getSource();
            fieldCombo.setEnabled(!(combo.getSelectedIndex() == 0 || combo.getSelectedIndex()==5));
            infoLabel.setText("<html>" + DemoHelper.QUERY_TYPES_DESC[combo.getSelectedIndex()]);
        }
        
    }
    
    //Action responsible for running query.Delegates to DemoHelper
    class RunActionListener implements ActionListener{
        JComboBox queryTypeComboBox;
        JTextField criteriaTextField;
        JComboBox fieldComboBox;
        JTable table;

        private RunActionListener(JComboBox queryTypeComboBox, JTextField queryField, JComboBox fieldComboBox,JTable table) {
            this.queryTypeComboBox = queryTypeComboBox;
            this.criteriaTextField = queryField;
            this.fieldComboBox = fieldComboBox;
            this.table = table;
        }

        public void actionPerformed(ActionEvent e) {
            try{
                if(null == criteriaTextField.getText() || criteriaTextField.getText().trim().equals("")){
                  return;
                }
                LOG.info("Run clicked, calling executeQuery with :");
                LOG.info("Search field " + criteriaTextField.getText());
                LOG.info("Field field " + fieldComboBox.getSelectedItem());
                LOG.info("Query type index " + queryTypeComboBox.getSelectedIndex());
                
                List<Song> songs = DemoHelper.getDemoHelper().executeQuery(criteriaTextField.getText().toLowerCase(), (String)fieldComboBox.getSelectedItem(), queryTypeComboBox.getSelectedIndex());
                SongTableModel model = new SongTableModel(songs);
                table.setModel(model);
                ((SongTableModel) table.getModel()).fireTableDataChanged();                
                
            }catch(Exception ex) {
                JOptionPane.showMessageDialog(fieldComboBox, ex);
            }
        }
        
    }
}
