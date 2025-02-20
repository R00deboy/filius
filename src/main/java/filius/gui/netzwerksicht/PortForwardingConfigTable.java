package filius.gui.netzwerksicht;

import javax.swing.event.ChangeEvent;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableModel;

import filius.gui.JExtendedTable;
import filius.rahmenprogramm.I18n;
import filius.software.firewall.Firewall;
import filius.software.firewall.FirewallRule;

@SuppressWarnings("serial")
public class PortForwardingConfigTable extends JExtendedTable implements I18n {

    public PortForwardingConfigTable(TableModel model, boolean editable) {
        super(model, editable);
    }

    public void editingStopped(ChangeEvent evt) {
        closeEditor();
    }

    void closeEditor() {
        TableCellEditor editor = getCellEditor();
        if (editor != null) {
            Firewall firewall = (Firewall) ((JFirewallDialog) parentGUI).getFirewall();
            FirewallRule rule = firewall.getRuleset().get(editingRow);
            rule.srcIP = getCurrentValueAt(editingRow, 1);
            rule.srcMask = getCurrentValueAt(editingRow, 2);
            rule.destIP = getCurrentValueAt(editingRow, 3);
            rule.destMask = getCurrentValueAt(editingRow, 4);
            String protocol = getCurrentValueAt(editingRow, 5);
            if (protocol.equals("TCP")) {
                rule.protocol = FirewallRule.TCP;
            } else if (protocol.equals("UDP")) {
                rule.protocol = FirewallRule.UDP;
            } else {
                rule.protocol = FirewallRule.ALL_PROTOCOLS;
            }
            String port = getCurrentValueAt(editingRow, 6);
            try {
                rule.port = Integer.valueOf(port);
            } catch (NumberFormatException e) {
                rule.port = FirewallRule.ALL_PORTS;
            }
            String action = getCurrentValueAt(editingRow, 7);
            if (messages.getString("jfirewalldialog_msg33").equals(action)) {
                rule.action = FirewallRule.ACCEPT;
            } else {
                rule.action = FirewallRule.DROP;
            }
            removeEditor();
            if (rule.validateExistingAttributes()) {
                firewall.updateRule(editingRow, rule);
                ((JFirewallDialog) parentGUI).updateRuleTable();
            }
        }
    }

    private String getCurrentValueAt(int row, int col) {
        if (row == editingRow && col == editingColumn) {
            return (String) getCellEditor().getCellEditorValue();
        } else {
            return (String) getValueAt(row, col);
        }
    }
}