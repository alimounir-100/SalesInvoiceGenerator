package controller;
/**
 * @author ali mounir
 */
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

import model.InvoiceLine;
import views.DesignHome;

public class InvoiceController implements ActionListener {
        
	// TODO: define object of view class
	DesignHome designhome;
	InvoiceData getData;
	InvoiceDetails invoiceDetails;
	InvoiceDetails tempInvoiceDetails;
	public ArrayList<InvoiceLine> tempInvoices = new ArrayList<>();
	public String invoiceDetailsPath = System.getProperty("user.dir") + "\\resources\\InvoiceLine.csv";

	// TODO: Constructor
	public InvoiceController(DesignHome designhome) {
		this.designhome = designhome;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// define any element on views by action command
		switch (e.getActionCommand().toString()) {
		case "btnCreateInvoice":
			readInvoiceData();
			break;
		case "deleteInvoice":
			try {
				deleteInvoice();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			break;
		case "loadfile":
			loadFile();
			break;
		case "savefile":
			saveFile();
			break;
		case "cancel":
			cancelUpdate();
			break;
		case "deleteitem":
			deleteitemUpdate();
			break;
		case "add":
			addNewItem();
			break;
		default:
			System.out.println("error");
		}
	}

	private void addNewItem() {
		// invoice details elements
		JTextField itemName = new JTextField();
		JTextField itemPrice = new JTextField();
		JTextField itemCount = new JTextField();

		Object[] invoiceDetailsMessage = { "Item Name:", itemName, "Item Price:", itemPrice, "Item Count", itemCount };
		int invoiceDetailsOption = JOptionPane.showConfirmDialog(null, invoiceDetailsMessage, "Invoice Details",
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (invoiceDetailsOption == JOptionPane.OK_OPTION) {
			// show new data in invoice details
			addNewInvoiceDetailsToTable(designhome.textField.getText(), itemName.getText().toString(),
					itemPrice.getText().toString(), itemCount.getText().toString(),
					Integer.parseInt(itemPrice.getText().toString())
							* Integer.parseInt(itemCount.getText().toString()));
			// add new invoice details to tempInvoice object
			InvoiceLine invoiceDetail = new InvoiceLine();
			invoiceDetail.setInvoiceNo(designhome.textField.getText());
			invoiceDetail.setItemName(itemName.getText().toString());
			invoiceDetail.setItemPrice(itemPrice.getText().toString());
			invoiceDetail.setItemCount(itemCount.getText().toString());
			tempInvoices.add(invoiceDetail);
			// update total in header table
			designhome.daDefaultTableModel.setValueAt(updateHeaderRowTotalPrice(designhome.textField.getText().toString()),
					designhome.table.getSelectedRow(), 3);
			// update text with new total
			designhome.textField_3
					.setText(Integer.toString(updateHeaderRowTotalPrice(designhome.textField.getText().toString())));
		}
	}

	private int updateHeaderRowTotalPrice(String invoiceNum) {
		int total = 0;
		for (int i = 0; i < tempInvoices.size(); i++) {
			if (tempInvoices.get(i).getInvoiceNo().equals(invoiceNum))
				total += (Integer.parseInt(tempInvoices.get(i).getItemPrice())
						* (Integer.parseInt(tempInvoices.get(i).getItemCount())));
		}
		return total;
	}

	private void deleteitemUpdate() {
		for (int i = 0; i < designhome.daDefaultTableModel.getRowCount(); i++)
			if (designhome.daDefaultTableModel.getValueAt(i, 0).equals(designhome.textField.getText())) {
				designhome.daDefaultTableModel.setValueAt(designhome.textField_1.getText().toString(), i, 1);
				designhome.daDefaultTableModel.setValueAt(designhome.textField_2.getText().toString(), i, 2);
			}
	}

	private void cancelUpdate() {
		designhome.textField.setText("");
		designhome.textField_1.setText("");
		designhome.textField_2.setText("");
		designhome.textField_3.setText("");
		designhome.daDefaultTableModel1.setRowCount(0);
	}

	private void saveFile() {
		// open file chooser and save header file and in line file
		JFileChooser headerfile = new JFileChooser(new File("C:\\"));
		JFileChooser inlinefile = new JFileChooser(new File("C:\\"));
		headerfile.addChoosableFileFilter(new FileFilter() {
			public String getDescription() {
				return "CSV Files (*.csv)";
			}

			public boolean accept(File f) {
				if (f.isDirectory()) {
					return true;
				} else {
					return f.getName().toLowerCase().endsWith(".csv");
				}
			}
		});
		inlinefile.addChoosableFileFilter(new FileFilter() {
			public String getDescription() {
				return "CSV Files (*.csv)";
			}

			public boolean accept(File f) {
				if (f.isDirectory()) {
					return true;
				} else {
					return f.getName().toLowerCase().endsWith(".csv");
				}
			}
		});
		headerfile.setDialogTitle("Save Invoice Header CSV File");
		inlinefile.setDialogTitle("Save Invoice Inline CSV File");
		int i = headerfile.showSaveDialog(null);
		if (i == JFileChooser.APPROVE_OPTION) {
			int j = inlinefile.showSaveDialog(null);
			if (j == JFileChooser.APPROVE_OPTION) {
				File header = headerfile.getSelectedFile();
				File inline = inlinefile.getSelectedFile();
				try {
					for (int k = 0; k < designhome.daDefaultTableModel.getRowCount(); k++) {
						FileWriter fw = new FileWriter(header.getPath(), true);
						FileWriter fw2 = new FileWriter(inline.getPath(), true);
						String content[] = new String[4];
						content[0] = designhome.daDefaultTableModel.getValueAt(k, 0).toString() + ",";
						content[1] = designhome.daDefaultTableModel.getValueAt(k, 1).toString() + ",";
						content[2] = designhome.daDefaultTableModel.getValueAt(k, 2).toString() + ",";
						content[3] = designhome.daDefaultTableModel.getValueAt(k, 3).toString();
						fw.write(content[0] + content[1] + content[2] + content[3]);
						fw.write("\n");
						fw.flush();
						fw.close();
						invoiceDetails = new InvoiceDetails(
								Integer.parseInt(designhome.daDefaultTableModel.getValueAt(k, 0).toString()),
								invoiceDetailsPath);
						for (int m = 0; m < invoiceDetails.invoices.size(); m++) {
							fw2.write(invoiceDetails.invoices.get(m).getInvoiceNo().toString() + ","
									+ invoiceDetails.invoices.get(m).getItemName().toString() + ","
									+ invoiceDetails.invoices.get(m).getItemPrice().toString() + ","
									+ invoiceDetails.invoices.get(m).getItemCount().toString());
							fw2.write("\n");
						}
						for (int m = 0; m < tempInvoices.size(); m++) {
							if (designhome.daDefaultTableModel.getValueAt(k, 0).toString() != tempInvoices.get(m)
									.getInvoiceNo())
								continue;
							else {
								fw2.write(tempInvoices.get(m).getInvoiceNo().toString() + ","
										+ tempInvoices.get(m).getItemName().toString() + ","
										+ tempInvoices.get(m).getItemPrice().toString() + ","
										+ tempInvoices.get(m).getItemCount().toString());
								fw2.write("\n");
							}
						}
						fw2.flush();
						fw2.close();
					}
				} catch (Exception e) {
					System.out.println(e.getMessage());
					JOptionPane.showMessageDialog(null, e.getMessage());
				}
			}
		}
	}

	private void loadFile() {
		// open file chooser and select header file
		JFileChooser invoiceHeaderFile = new JFileChooser(new File("C:\\"));
		invoiceHeaderFile.setDialogTitle("Choose Invoice Header File");
		invoiceHeaderFile.addChoosableFileFilter(new FileFilter() {
			public String getDescription() {
				return "CSV Files (*.csv)";
			}

			public boolean accept(File f) {
				if (f.isDirectory()) {
					return true;
				} else {
					return f.getName().toLowerCase().endsWith(".csv");
				}
			}
		});
		int i = invoiceHeaderFile.showOpenDialog(null);
		if (i == JFileChooser.APPROVE_OPTION) {
			// open file chooser and select line file
			JFileChooser invoiceLineFile = new JFileChooser(new File("C:\\"));
			invoiceLineFile.setDialogTitle("Choose Inline File");
			invoiceLineFile.addChoosableFileFilter(new FileFilter() {
				public String getDescription() {
					return "CSV Files (*.csv)";
				}

				public boolean accept(File f) {
					if (f.isDirectory()) {
						return true;
					} else {
						return f.getName().toLowerCase().endsWith(".csv");
					}
				}
			});
			int i2 = invoiceLineFile.showOpenDialog(null);
			if (i2 == JFileChooser.APPROVE_OPTION) {
				File f1 = invoiceHeaderFile.getSelectedFile();
				File f2 = invoiceLineFile.getSelectedFile();
				String headerfilepath = f1.getPath();
				invoiceDetailsPath = f2.getPath();
				// empty all table
				designhome.daDefaultTableModel.setRowCount(0);
				designhome.daDefaultTableModel1.setRowCount(0);
				showInvoiceData(headerfilepath);
			}
		}
	}

	private void deleteInvoice() throws IOException {
		// TODO Auto-generated method stub
		// check for selected row first
		if (designhome.table.getSelectedRow() != -1) {
			if (designhome.table_1.getSelectedRow() == -1) {
				// remove selected row from the model
				designhome.daDefaultTableModel.removeRow(designhome.table.getSelectedRow());
				designhome.textField.setText("");
				designhome.textField_1.setText("");
				designhome.textField_2.setText("");
				designhome.textField_3.setText("");
				designhome.daDefaultTableModel1.setRowCount(0);
				// disable add item until select header row
				designhome.btnNewButton.setEnabled(false);
			} else {
				// delete from in line file
				deleteSelectedLineFromCSVFile(designhome.daDefaultTableModel1
						.getValueAt(designhome.table_1.getSelectedRow(), 0).toString() + ","
						+ designhome.daDefaultTableModel1.getValueAt(designhome.table_1.getSelectedRow(), 1).toString()
						+ ","
						+ designhome.daDefaultTableModel1.getValueAt(designhome.table_1.getSelectedRow(), 2).toString()
						+ ","
						+ designhome.daDefaultTableModel1.getValueAt(designhome.table_1.getSelectedRow(), 3).toString());
//				invoiceDetails.invoices.remove(homePage.table_1.getSelectedRow());
				invoiceDetails = new InvoiceDetails(
						Integer.parseInt(
								designhome.daDefaultTableModel.getValueAt(designhome.table.getSelectedRow(), 0).toString()),
						invoiceDetailsPath);
				if (invoiceDetails.invoices.size() != 0) {
					// decrement total value in header table and view
					designhome.daDefaultTableModel.setValueAt(
							Integer.parseInt(designhome.daDefaultTableModel.getValueAt(designhome.table.getSelectedRow(), 3)
									.toString())
									- Integer.parseInt(designhome.daDefaultTableModel1
											.getValueAt(designhome.table_1.getSelectedRow(), 4).toString()),
							designhome.table.getSelectedRow(), 3);
					designhome.textField_3.setText(
							designhome.daDefaultTableModel.getValueAt(designhome.table.getSelectedRow(), 3).toString());
					// delete row from invoice line table
					designhome.daDefaultTableModel1.removeRow(designhome.table_1.getSelectedRow());
				} else {
					// decrement total value in header table and view
					designhome.daDefaultTableModel.setValueAt(
							Integer.parseInt(designhome.daDefaultTableModel.getValueAt(designhome.table.getSelectedRow(), 3)
									.toString())
									- Integer.parseInt(designhome.daDefaultTableModel1
											.getValueAt(designhome.table_1.getSelectedRow(), 4).toString()),
							designhome.table.getSelectedRow(), 3);
					designhome.textField_3.setText(
							designhome.daDefaultTableModel.getValueAt(designhome.table.getSelectedRow(), 3).toString());
					// delete invoice item from object
					tempInvoices.remove(designhome.table_1.getSelectedRow());
					// delete row from invoice line table
					designhome.daDefaultTableModel1.removeRow(designhome.table_1.getSelectedRow());
				}

			}
		}
	}

	private void deleteSelectedLineFromCSVFile(String lineData) throws IOException {
		File inputFile = new File(invoiceDetailsPath);
		File tempFile = new File(System.getProperty("user.dir") + "\\resources\\" + "tempInvoiceDetails.csv");

		try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
				BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {

			String lineToRemove = lineData;
			String currentLine;

			while ((currentLine = reader.readLine()) != null) {
				// trim newline when comparing with lineToRemove
				String trimmedLine = currentLine.trim();
				if (trimmedLine.equals(lineToRemove))
					continue;
				writer.write(currentLine + System.getProperty("line.separator"));
			}
		}

		// rename file to file2 name
		boolean success = new File(invoiceDetailsPath).delete();
		if (success && tempFile.renameTo(inputFile)) {
			System.out.println(inputFile.getName() + " is renamed and deleted!");
		} else {
			System.out.println("operation is failed.");
		}
	}

	public void addTableMouseListener() {
		designhome.table.addMouseListener(new MouseListener() {
			@Override
			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseExited(MouseEvent e) {
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseClicked(MouseEvent e) {
				// enable add item button on row select
				designhome.btnNewButton.setEnabled(true);
				if (e.getClickCount() == 1) {
					JTable target = (JTable) e.getSource();
					int rowIndex = target.getSelectedRow();
					@SuppressWarnings("unused")
					int columnIndex = target.getSelectedColumn();
					designhome.textField.setText(target.getModel().getValueAt(rowIndex, 0).toString());
					designhome.textField_1.setText(target.getModel().getValueAt(rowIndex, 1).toString());
					designhome.textField_2.setText(target.getModel().getValueAt(rowIndex, 2).toString());
					designhome.textField_3.setText(target.getModel().getValueAt(rowIndex, 3).toString());
					showInvoiceDetails(Integer.parseInt(target.getModel().getValueAt(rowIndex, 0).toString()));
				}
			}

			private void showInvoiceDetails(int rowIndex) {
				// TODO Auto-generated method stub
				designhome.daDefaultTableModel1.setRowCount(0);
				invoiceDetails = new InvoiceDetails(rowIndex, invoiceDetailsPath);
				for (int i = 0; i < invoiceDetails.invoices.size(); i++)
					designhome.daDefaultTableModel1.addRow(new Object[] { invoiceDetails.invoices.get(i).getInvoiceNo(),
							invoiceDetails.invoices.get(i).getItemName(), invoiceDetails.invoices.get(i).getItemPrice(),
							invoiceDetails.invoices.get(i).getItemCount(),
							showItemTotal(invoiceDetails.invoices.get(i).getItemPrice(),
									invoiceDetails.invoices.get(i).getItemCount()) });
				for (int i = 0; i < tempInvoices.size(); i++) {
					if (rowIndex != Integer.parseInt(tempInvoices.get(i).getInvoiceNo()))
						continue;
					else
						designhome.daDefaultTableModel1.addRow(new Object[] { tempInvoices.get(i).getInvoiceNo(),
								tempInvoices.get(i).getItemName(), tempInvoices.get(i).getItemPrice(),
								tempInvoices.get(i).getItemCount(), showItemTotal(tempInvoices.get(i).getItemPrice(),
										tempInvoices.get(i).getItemCount()) });
				}
			}
		});
	}

	private Object showItemTotal(String itemPrice, String itemCount) {
		// TODO Auto-generated method stub
		return (Integer.parseInt(itemPrice)) * (Integer.parseInt(itemCount));
	}

	public void showInvoiceData(String path) {
		getData = new InvoiceData(path);
		for (int i = 0; i < getData.invoices.size(); i++) {
			designhome.daDefaultTableModel.addRow(new Object[] { getData.invoices.get(i).getInvoiceNo(),
					getData.invoices.get(i).getInvoiceDate(), getData.invoices.get(i).getCustomerName(),
					calculateRowTotal(Integer.parseInt(getData.invoices.get(i).getInvoiceNo())) });
		}
	}

	private int calculateRowTotal(int invoiceNo) {
		// TODO Auto-generated method stub
		int totalprice = 0;
		InvoiceDetails invoiceDetails = new InvoiceDetails(invoiceNo, invoiceDetailsPath);
		if (invoiceDetails.invoices.size() == 0 && tempInvoices.size() == 0)
			return 0;
		else if (invoiceDetails.invoices.size() != 0) {
			for (int j = 0; j < invoiceDetails.invoices.size(); j++) {
				totalprice += (Integer.parseInt(invoiceDetails.invoices.get(j).getItemPrice())
						* Integer.parseInt(invoiceDetails.invoices.get(j).getItemCount()));
			}
		} else if (tempInvoices.size() != 0 && invoiceDetails.invoices.size() == 0) {
			for (int j = 0; j < tempInvoices.size(); j++) {
				if (Integer.parseInt(tempInvoices.get(j).getInvoiceNo()) != invoiceNo)
					continue;
				totalprice += (Integer.parseInt(tempInvoices.get(j).getItemPrice())
						* Integer.parseInt(tempInvoices.get(j).getItemCount()));
			}
		}
		return totalprice;
	}

	private void readInvoiceData() {
		// invoice elements
		JTextField invoiceNo = new JTextField();
		JTextField invoiceDate = new JTextField();
		JTextField customerName = new JTextField();
//		// invoice details elements
//		JTextField itemName = new JTextField();
//		JTextField itemPrice = new JTextField();
//		JTextField itemCount = new JTextField();

		Object[] message = { "Invoice No:", invoiceNo, "Invoice Date:", invoiceDate, "Customer Name", customerName };
//		Object[] invoiceDetailsMessage = { "Item Name:", itemName, "Item Price:", itemPrice, "Item Count", itemCount };

		int option = JOptionPane.showConfirmDialog(null, message, "New Invoice", JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE);
		if (option == JOptionPane.OK_OPTION) {

			designhome.daDefaultTableModel1.setRowCount(0);
			addNewInvoiceToTable(invoiceNo.getText().toString(), invoiceDate.getText().toString(),
					customerName.getText().toString(),
					calculateRowTotal(Integer.parseInt(invoiceNo.getText().toString())));
//			}
			// disable add item until select header row
			designhome.btnNewButton.setEnabled(false);
		}
	}

	private void addNewInvoiceToTable(String invoiceNo, String invoiceDate, String customerName, int total) {
		// TODO Auto-generated method stub
		designhome.daDefaultTableModel.addRow(new Object[] { invoiceNo, invoiceDate, customerName, total });
	}

	private void addNewInvoiceDetailsToTable(String invoiceNo, String itemName, String itemPrice, String itemCount,
			int total) {
		// TODO Auto-generated method stub
		designhome.daDefaultTableModel1.addRow(new Object[] { invoiceNo, itemName, itemPrice, itemCount, total });
	}
}
