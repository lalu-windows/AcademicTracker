import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MainWindow {

	// the row builder
	public static JPanel subrow(String subject, int attended, int total,Runnable onDelete){
		int percentage = (int)(((double) attended / total) * 100);

		JPanel subPanel = new JPanel(new BorderLayout(10,10));
		subPanel.setPreferredSize(new Dimension(600,25));
		subPanel.setMaximumSize(new Dimension(600,25));
		subPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

		JLabel subjectName = new JLabel(subject);
		subjectName.setPreferredSize(new Dimension(130,25));
		subjectName.setFont(new Font("Arial", Font.BOLD, 15));

		JProgressBar bar = new JProgressBar(0,100);
		bar.setValue(percentage);
		bar.setStringPainted(true);
		bar.setString(percentage + "%");

		JLabel ratio = new JLabel(attended + "/" + total);
		ratio.setPreferredSize(new Dimension(60,25));

		subPanel.add(subjectName, BorderLayout.WEST);
		subPanel.add(bar, BorderLayout.CENTER);
	JButton delbtn = new JButton("X");
delbtn.setMargin(new Insets(0,6,0,6));
delbtn.addActionListener(e -> onDelete.run());

JPanel east = new JPanel(new BorderLayout(5,0));
east.add(ratio, BorderLayout.CENTER);
east.add(delbtn, BorderLayout.EAST);

subPanel.add(east, BorderLayout.EAST);	
		return subPanel;
	}

	// ---------- Attendance page ----------
	static class AttenGUI extends JPanel {

		// data and the panel that holds the rows are fields so refresh() can reach them
		private final List<String> subjects = new ArrayList<>();
private final List<Integer> attended = new ArrayList<>();
private final List<Integer> tot = new ArrayList<>();
private final JComboBox<String> subjectdrop = new JComboBox<>();
private final JPanel subjectPanel = new JPanel();
				public AttenGUI(){
			setLayout(new BorderLayout(10,10));
			setBorder(BorderFactory.createEmptyBorder(15,15,15,15));

			JLabel titlelabel = new JLabel("Attendance tracker", JLabel.CENTER);
			titlelabel.setFont(new Font("Inter", Font.BOLD, 25));
			add(titlelabel, BorderLayout.NORTH);

			subjectPanel.setLayout(new BoxLayout(subjectPanel, BoxLayout.Y_AXIS));
			add(subjectPanel, BorderLayout.CENTER);

			add(buildUpdatePanel(), BorderLayout.SOUTH);
			for(Object[] r : DBManager.loadAll()){
    				subjects.add((String) r[0]);
			        attended.add((Integer) r[1]);
			        tot.add((Integer) r[2]);
			        subjectdrop.addItem((String) r[0]);
			}

			JPanel bottom = new JPanel(new GridLayout(2,1));
			bottom.add(buildAddPanel());
			bottom.add(buildUpdatePanel());
			add(bottom, BorderLayout.SOUTH);
			
			
			
			
			refresh(); // first draw
		}

		// redraws subjectpanel
		public void refresh(){
			subjectPanel.removeAll();

			subjectPanel.add(Box.createVerticalGlue());
			for(int i = 0; i < subjects.size(); i++){
    final int idx = i; // lambdas need a final copy of i
    subjectPanel.add(subrow(subjects.get(i), attended.get(i), tot.get(i), () -> deleteSubject(idx)));
    subjectPanel.add(Box.createRigidArea(new Dimension(0,8)));
}
		/*	for(int i = 0; i < subjects.size(); i++){
			
				subjectPanel.add(subrow(subjects.get(i), attended.get(i), tot.get(i),));
				subjectPanel.add(Box.createRigidArea(new Dimension(0,8)));
			}*/
			subjectPanel.add(Box.createVerticalGlue());
			subjectPanel.revalidate(); // re-run the layout for the new components
    subjectPanel.repaint();

		}
		private void deleteSubject(int index){
    String name = subjects.get(index);
    int ok = JOptionPane.showConfirmDialog(this, "Delete " + name + "?", "Confirm", JOptionPane.YES_NO_OPTION);
    if(ok != JOptionPane.YES_OPTION) return;

    DBManager.delete(name);      // remove from the database first
    subjects.remove(index);      // then from the lists (index is an int, so this removes by position)
    attended.remove(index);
    tot.remove(index);
    subjectdrop.removeItemAt(index);
    refresh();
}
		//for adding subjects:
		private JPanel buildAddPanel(){
    JPanel addpan = new JPanel(new FlowLayout(FlowLayout.LEFT,10,10));
    addpan.setBorder(BorderFactory.createTitledBorder("Add subject"));

    JTextField namefield = new JTextField(10);
    JTextField attfield = new JTextField(4);
    JTextField totalfield = new JTextField(4);
    JButton addbtn = new JButton("Add");

    addbtn.addActionListener(e -> {
        String name = namefield.getText().trim();
        try{
            int a = Integer.parseInt(attfield.getText().trim());
            int t = Integer.parseInt(totalfield.getText().trim());

            if(name.isEmpty() || a < 0 || t <= 0 || a > t){
                JOptionPane.showMessageDialog(this, "Enter a name and valid numbers", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if(subjects.contains(name)){
                JOptionPane.showMessageDialog(this, "Subject already exists", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            DBManager.insert(name, a, t);
            subjects.add(name);
            attended.add(a);
            tot.add(t);
            subjectdrop.addItem(name);
            refresh();

            namefield.setText(""); attfield.setText(""); totalfield.setText("");
        }catch(NumberFormatException ex){
            JOptionPane.showMessageDialog(this, "Please enter valid numbers!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    });

    addpan.add(new JLabel("Name: "));     addpan.add(namefield);
    addpan.add(new JLabel("Attended: ")); addpan.add(attfield);
    addpan.add(new JLabel("Total: "));    addpan.add(totalfield);
    addpan.add(addbtn);
    return addpan;
}
		// Update attendance section
		private JPanel buildUpdatePanel(){
			JPanel updatepan = new JPanel(new FlowLayout(FlowLayout.LEFT,10,10));
			updatepan.setBorder(BorderFactory.createTitledBorder("Update attendance"));

			JTextField attenfield = new JTextField(4);
			JTextField totfield = new JTextField(4);
			JButton updatebtn = new JButton("Update");

			// autofill the text fields when a subject is selected
			subjectdrop.addActionListener(e -> {
				int index = subjectdrop.getSelectedIndex();
				if(index<0) return;
				attenfield.setText(String.valueOf(attended.get(index)));
				totfield.setText(String.valueOf(tot.get(index)));

			});
			if(subjectdrop.getItemCount() > 0) subjectdrop.setSelectedIndex(0);

			updatebtn.addActionListener(e -> {
				int index = subjectdrop.getSelectedIndex();
				try{
					int atten = Integer.parseInt(attenfield.getText().trim());
					int newtot = Integer.parseInt(totfield.getText().trim());

					if(atten < 0 || newtot <= 0){
						JOptionPane.showMessageDialog(this, "Values must be positive", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					if(atten > newtot){
						JOptionPane.showMessageDialog(this, "Attended cannot be greater than total", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					attended.set(index,atten);
				tot.set(index,newtot);
				DBManager.update(subjects.get(index),atten,newtot);
	


					refresh(); // redraw with the new numbers
				}
				catch(NumberFormatException ex){
					JOptionPane.showMessageDialog(this, "Please enter valid numbers!", "Error", JOptionPane.ERROR_MESSAGE);
				}
			});

			updatepan.add(new JLabel("Subject: "));
			updatepan.add(subjectdrop);
			updatepan.add(new JLabel("Attended: "));
			updatepan.add(attenfield);
			updatepan.add(new JLabel("Total: "));
			updatepan.add(totfield);
			updatepan.add(updatebtn);

			return updatepan;
		}
	}

	// grade gui
	/*static class GradeGUI extends JPanel {
		public GradeGUI(){
			setLayout(new BorderLayout());
			add(new JLabel("Grades coming soon", JLabel.CENTER), BorderLayout.CENTER);
		}
	}*/

	// ---------- Main frame ----------
	static class MainFrame extends JFrame {
		public MainFrame(){
			setTitle("Academic Tracker");
			setSize(1280,720);
			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			setLocationRelativeTo(null);

			CardLayout cardlay = new CardLayout();
			JPanel cardpan = new JPanel(cardlay);

			cardpan.add(new AttenGUI(), "ATTENDANCE");
//			cardpan.add(new GradeGUI(), "GRADE");

			JPanel btnpan = new JPanel();
			JButton attenbtn = new JButton("Attendance");
			JButton gradebtn = new JButton("Grade");
			attenbtn.addActionListener(e -> cardlay.show(cardpan, "ATTENDANCE"));
			gradebtn.addActionListener(e -> cardlay.show(cardpan, "GRADE"));
			btnpan.add(attenbtn);
			btnpan.add(gradebtn);

			add(btnpan, BorderLayout.NORTH);
			add(cardpan, BorderLayout.CENTER);
		}
	}


	public static void main(String[] args){
    DBManager.createTable();

    // seed default subjects only on the very first run
    if(DBManager.loadAll().isEmpty()){
        DBManager.insert("TOC", 35, 40);
        DBManager.insert("OOP", 20, 38);
        DBManager.insert("MATHS", 32, 40);
        DBManager.insert("DSA", 25, 35);
        DBManager.insert("DE&LD", 28, 34);
        DBManager.insert("ETHICS", 25, 35);
    }

    SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
}
}
