/*
 * MIT License
 *
 * Copyright (c) 2020 Jannis Weis
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.github.weisj.darklaf.ui.filechooser;

import sun.awt.shell.ShellFolder;
import sun.swing.FilePane;
import sun.swing.SwingUtilities2;

import javax.accessibility.AccessibleContext;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileSystemView;
import javax.swing.plaf.ActionMapUIResource;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicDirectoryModel;
import javax.swing.plaf.basic.BasicFileChooserUI;
import javax.swing.plaf.metal.MetalFileChooserUI;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.Vector;


/**
 * Metal L&amp;F implementation of a FileChooser.
 *
 * @author Jeff Dinkins
 */
public class DarkFileChooserUIBridge extends BasicFileChooserUI {

    // Much of the Metal UI for JFilechooser is just a copy of
    // the windows implementation, but using Metal themed buttons, lists,
    // icons, etc. We are planning a complete rewrite, and hence we've
    // made most things in this class protected.

    protected static final Dimension hstrut5 = new Dimension(5, 1);
    protected static final Dimension hstrut11 = new Dimension(11, 1);
    protected static final Dimension vstrut5 = new Dimension(1, 5);
    protected static final Insets shrinkwrap = new Insets(0, 0, 0, 0);
    static final int space = 10;
    // Preferred and Minimum sizes for the dialog box
    protected static int PREF_WIDTH = 500;
    protected static int PREF_HEIGHT = 326;
    protected static Dimension PREF_SIZE = new Dimension(PREF_WIDTH, PREF_HEIGHT);
    protected static int MIN_WIDTH = 500;
    protected static int MIN_HEIGHT = 326;
    protected static int LIST_PREF_WIDTH = 405;
    protected static int LIST_PREF_HEIGHT = 135;
    protected static Dimension LIST_PREF_SIZE = new Dimension(LIST_PREF_WIDTH, LIST_PREF_HEIGHT);
    protected JLabel lookInLabel;
    protected JComboBox<Object> directoryComboBox;
    protected DirectoryComboBoxModel directoryComboBoxModel;
    protected Action directoryComboBoxAction = new DirectoryComboBoxAction();
    protected FilterComboBoxModel filterComboBoxModel;
    protected JTextField fileNameTextField;
    protected DarkFilePaneUIBridge filePane;
    protected JToggleButton listViewButton;
    protected JToggleButton detailsViewButton;
    protected JButton approveButton;
    protected JButton cancelButton;
    protected JPanel buttonPanel;
    protected JPanel bottomPanel;
    protected JComboBox<?> filterComboBox;
    // Labels, mnemonics, and tooltips (oh my!)
    protected int lookInLabelMnemonic = 0;
    protected String lookInLabelText = null;
    protected String saveInLabelText = null;
    protected int fileNameLabelMnemonic = 0;
    protected String fileNameLabelText = null;
    protected int folderNameLabelMnemonic = 0;
    protected String folderNameLabelText = null;
    protected int filesOfTypeLabelMnemonic = 0;
    protected String filesOfTypeLabelText = null;
    protected String upFolderToolTipText = null;
    protected String upFolderAccessibleName = null;
    protected String homeFolderToolTipText = null;
    protected String homeFolderAccessibleName = null;
    protected String newFolderToolTipText = null;
    protected String newFolderAccessibleName = null;
    protected String listViewButtonToolTipText = null;
    protected String listViewButtonAccessibleName = null;
    protected String detailsViewButtonToolTipText = null;
    protected String detailsViewButtonAccessibleName = null;
    protected AlignedLabel fileNameLabel;

    /**
     * Constructs a new instance of {@code MetalFileChooserUI}.
     *
     * @param filechooser a {@code JFileChooser}
     */
    public DarkFileChooserUIBridge(final JFileChooser filechooser) {
        super(filechooser);
    }

    /**
     * Constructs a new instance of {@code MetalFileChooserUI}.
     *
     * @param c a component
     * @return a new instance of {@code MetalFileChooserUI}
     */
    public static ComponentUI createUI(final JComponent c) {
        return new MetalFileChooserUI((JFileChooser) c);
    }

    public void installUI(final JComponent c) {
        super.installUI(c);
    }

    public void uninstallUI(final JComponent c) {
        // Remove listeners
        c.removePropertyChangeListener(filterComboBoxModel);
        c.removePropertyChangeListener(filePane);
        cancelButton.removeActionListener(getCancelSelectionAction());
        approveButton.removeActionListener(getApproveSelectionAction());
        fileNameTextField.removeActionListener(getApproveSelectionAction());

        if (filePane != null) {
            filePane.uninstallUI();
            filePane = null;
        }

        super.uninstallUI(c);
    }

    public void installComponents(final JFileChooser fc) {
        FileSystemView fsv = fc.getFileSystemView();

        fc.setBorder(new EmptyBorder(12, 12, 11, 11));
        fc.setLayout(new BorderLayout(0, 11));

        filePane = new DarkFilePane(new MetalFileChooserUIAccessor());
        fc.addPropertyChangeListener(filePane);

        // ********************************* //
        // **** Construct the top panel **** //
        // ********************************* //

        // Directory manipulation buttons
        JPanel topPanel = new JPanel(new BorderLayout(11, 0));
        JPanel topButtonPanel = new JPanel();
        topButtonPanel.setLayout(new BoxLayout(topButtonPanel, BoxLayout.LINE_AXIS));
        topPanel.add(topButtonPanel, BorderLayout.AFTER_LINE_ENDS);

        // Add the top panel to the fileChooser
        fc.add(topPanel, BorderLayout.NORTH);

        // ComboBox Label
        lookInLabel = new JLabel(lookInLabelText);
        lookInLabel.setDisplayedMnemonic(lookInLabelMnemonic);
        topPanel.add(lookInLabel, BorderLayout.BEFORE_LINE_BEGINS);

        // CurrentDir ComboBox
        @SuppressWarnings("serial") // anonymous class
                JComboBox<Object> tmp1 = new JComboBox<Object>() {
            public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                // Must be small enough to not affect total width.
                d.width = 150;
                return d;
            }
        };
        directoryComboBox = tmp1;
        directoryComboBox.putClientProperty(AccessibleContext.ACCESSIBLE_DESCRIPTION_PROPERTY,
                                            lookInLabelText);
        directoryComboBox.putClientProperty("JComboBox.isTableCellEditor", Boolean.TRUE);
        lookInLabel.setLabelFor(directoryComboBox);
        directoryComboBoxModel = createDirectoryComboBoxModel(fc);
        directoryComboBox.setModel(directoryComboBoxModel);
        directoryComboBox.addActionListener(directoryComboBoxAction);
        directoryComboBox.setRenderer(createDirectoryComboBoxRenderer(fc));
        directoryComboBox.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        directoryComboBox.setAlignmentY(JComponent.TOP_ALIGNMENT);
        directoryComboBox.setMaximumRowCount(8);

        topPanel.add(directoryComboBox, BorderLayout.CENTER);

        // Up Button
        JButton upFolderButton = new JButton(getChangeToParentDirectoryAction());
        upFolderButton.setText(null);
        upFolderButton.setIcon(upFolderIcon);
        upFolderButton.setToolTipText(upFolderToolTipText);
        upFolderButton.putClientProperty(AccessibleContext.ACCESSIBLE_NAME_PROPERTY,
                                         upFolderAccessibleName);
        upFolderButton.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        upFolderButton.setAlignmentY(JComponent.CENTER_ALIGNMENT);
        upFolderButton.setMargin(shrinkwrap);

        topButtonPanel.add(upFolderButton);
        topButtonPanel.add(Box.createRigidArea(hstrut5));

        // Home Button
        File homeDir = fsv.getHomeDirectory();
        String toolTipText = homeFolderToolTipText;


        JButton b = new JButton(homeFolderIcon);
        b.setToolTipText(toolTipText);
        b.putClientProperty(AccessibleContext.ACCESSIBLE_NAME_PROPERTY,
                            homeFolderAccessibleName);
        b.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        b.setAlignmentY(JComponent.CENTER_ALIGNMENT);
        b.setMargin(shrinkwrap);

        b.addActionListener(getGoHomeAction());
        topButtonPanel.add(b);
        topButtonPanel.add(Box.createRigidArea(hstrut5));

        // New Directory Button
        if (!UIManager.getBoolean("FileChooser.readOnly")) {
            b = new JButton(filePane.getNewFolderAction());
            b.setText(null);
            b.setIcon(newFolderIcon);
            b.setToolTipText(newFolderToolTipText);
            b.putClientProperty(AccessibleContext.ACCESSIBLE_NAME_PROPERTY,
                                newFolderAccessibleName);
            b.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            b.setAlignmentY(JComponent.CENTER_ALIGNMENT);
            b.setMargin(shrinkwrap);
        }
        topButtonPanel.add(b);
        topButtonPanel.add(Box.createRigidArea(hstrut5));

        // View button group
        ButtonGroup viewButtonGroup = new ButtonGroup();

        // List Button
        listViewButton = new JToggleButton(listViewIcon);
        listViewButton.setToolTipText(listViewButtonToolTipText);
        listViewButton.putClientProperty(AccessibleContext.ACCESSIBLE_NAME_PROPERTY,
                                         listViewButtonAccessibleName);
        listViewButton.setSelected(true);
        listViewButton.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        listViewButton.setAlignmentY(JComponent.CENTER_ALIGNMENT);
        listViewButton.setMargin(shrinkwrap);
        listViewButton.addActionListener(filePane.getViewTypeAction(FilePane.VIEWTYPE_LIST));
        topButtonPanel.add(listViewButton);
        viewButtonGroup.add(listViewButton);

        // Details Button
        detailsViewButton = new JToggleButton(detailsViewIcon);
        detailsViewButton.setToolTipText(detailsViewButtonToolTipText);
        detailsViewButton.putClientProperty(AccessibleContext.ACCESSIBLE_NAME_PROPERTY,
                                            detailsViewButtonAccessibleName);
        detailsViewButton.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        detailsViewButton.setAlignmentY(JComponent.CENTER_ALIGNMENT);
        detailsViewButton.setMargin(shrinkwrap);
        detailsViewButton.addActionListener(filePane.getViewTypeAction(FilePane.VIEWTYPE_DETAILS));
        topButtonPanel.add(detailsViewButton);
        viewButtonGroup.add(detailsViewButton);

        filePane.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(final PropertyChangeEvent e) {
                if ("viewType".equals(e.getPropertyName())) {
                    int viewType = filePane.getViewType();
                    switch (viewType) {
                        case FilePane.VIEWTYPE_LIST:
                            listViewButton.setSelected(true);
                            break;

                        case FilePane.VIEWTYPE_DETAILS:
                            detailsViewButton.setSelected(true);
                            break;
                    }
                }
            }
        });

        // ************************************** //
        // ******* Add the directory pane ******* //
        // ************************************** //
        fc.add(getAccessoryPanel(), BorderLayout.AFTER_LINE_ENDS);
        JComponent accessory = fc.getAccessory();
        if (accessory != null) {
            getAccessoryPanel().add(accessory);
        }
        filePane.setPreferredSize(LIST_PREF_SIZE);
        fc.add(filePane, BorderLayout.CENTER);

        // ********************************** //
        // **** Construct the bottom panel ** //
        // ********************************** //
        JPanel bottomPanel = getBottomPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        fc.add(bottomPanel, BorderLayout.SOUTH);

        // FileName label and textfield
        JPanel fileNamePanel = new JPanel();
        fileNamePanel.setLayout(new BoxLayout(fileNamePanel, BoxLayout.LINE_AXIS));
        bottomPanel.add(fileNamePanel);
        bottomPanel.add(Box.createRigidArea(vstrut5));

        fileNameLabel = new AlignedLabel();
        populateFileNameLabel();
        fileNamePanel.add(fileNameLabel);

        @SuppressWarnings("serial") // anonymous class
                JTextField tmp2 = new JTextField(35) {
            public Dimension getMaximumSize() {
                return new Dimension(Short.MAX_VALUE, super.getPreferredSize().height);
            }
        };
        fileNameTextField = tmp2;
        fileNamePanel.add(fileNameTextField);
        fileNameLabel.setLabelFor(fileNameTextField);
        fileNameTextField.addFocusListener(
                new FocusAdapter() {
                    public void focusGained(final FocusEvent e) {
                        if (!getFileChooser().isMultiSelectionEnabled()) {
                            filePane.clearSelection();
                        }
                    }
                }
        );
        if (fc.isMultiSelectionEnabled()) {
            setFileName(fileNameString(fc.getSelectedFiles()));
        } else {
            setFileName(fileNameString(fc.getSelectedFile()));
        }


        // Filetype label and combobox
        JPanel filesOfTypePanel = new JPanel();
        filesOfTypePanel.setLayout(new BoxLayout(filesOfTypePanel, BoxLayout.LINE_AXIS));
        bottomPanel.add(filesOfTypePanel);

        AlignedLabel filesOfTypeLabel = new AlignedLabel(filesOfTypeLabelText);
        filesOfTypeLabel.setDisplayedMnemonic(filesOfTypeLabelMnemonic);
        filesOfTypePanel.add(filesOfTypeLabel);

        filterComboBoxModel = createFilterComboBoxModel();
        fc.addPropertyChangeListener(filterComboBoxModel);
        filterComboBox = new JComboBox<>(filterComboBoxModel);
        filterComboBox.putClientProperty(AccessibleContext.ACCESSIBLE_DESCRIPTION_PROPERTY,
                                         filesOfTypeLabelText);
        filesOfTypeLabel.setLabelFor(filterComboBox);
        filterComboBox.setRenderer(createFilterComboBoxRenderer());
        filesOfTypePanel.add(filterComboBox);

        // buttons
        getButtonPanel().setLayout(new ButtonAreaLayout());

        approveButton = new JButton(getApproveButtonText(fc));
        // Note: Metal does not use mnemonics for approve and cancel
        approveButton.addActionListener(getApproveSelectionAction());
        approveButton.setToolTipText(getApproveButtonToolTipText(fc));
        getButtonPanel().add(approveButton);

        cancelButton = new JButton(cancelButtonText);
        cancelButton.setToolTipText(cancelButtonToolTipText);
        cancelButton.addActionListener(getCancelSelectionAction());
        getButtonPanel().add(cancelButton);

        if (fc.getControlButtonsAreShown()) {
            addControlButtons();
        }

        groupLabels(new AlignedLabel[]{fileNameLabel, filesOfTypeLabel});
    }

    public void uninstallComponents(final JFileChooser fc) {
        fc.removeAll();
        bottomPanel = null;
        buttonPanel = null;
    }

    protected void installListeners(final JFileChooser fc) {
        super.installListeners(fc);
        ActionMap actionMap = getActionMap();
        SwingUtilities.replaceUIActionMap(fc, actionMap);
    }

    protected void installStrings(final JFileChooser fc) {
        super.installStrings(fc);

        Locale l = fc.getLocale();

        lookInLabelMnemonic = getMnemonic("FileChooser.lookInLabelMnemonic", l);
        lookInLabelText = UIManager.getString("FileChooser.lookInLabelText", l);
        saveInLabelText = UIManager.getString("FileChooser.saveInLabelText", l);

        fileNameLabelMnemonic = getMnemonic("FileChooser.fileNameLabelMnemonic", l);
        fileNameLabelText = UIManager.getString("FileChooser.fileNameLabelText", l);
        folderNameLabelMnemonic = getMnemonic("FileChooser.folderNameLabelMnemonic", l);
        folderNameLabelText = UIManager.getString("FileChooser.folderNameLabelText", l);

        filesOfTypeLabelMnemonic = getMnemonic("FileChooser.filesOfTypeLabelMnemonic", l);
        filesOfTypeLabelText = UIManager.getString("FileChooser.filesOfTypeLabelText", l);

        upFolderToolTipText = UIManager.getString("FileChooser.upFolderToolTipText", l);
        upFolderAccessibleName = UIManager.getString("FileChooser.upFolderAccessibleName", l);

        homeFolderToolTipText = UIManager.getString("FileChooser.homeFolderToolTipText", l);
        homeFolderAccessibleName = UIManager.getString("FileChooser.homeFolderAccessibleName", l);

        newFolderToolTipText = UIManager.getString("FileChooser.newFolderToolTipText", l);
        newFolderAccessibleName = UIManager.getString("FileChooser.newFolderAccessibleName", l);

        listViewButtonToolTipText = UIManager.getString("FileChooser.listViewButtonToolTipText", l);
        listViewButtonAccessibleName = UIManager.getString("FileChooser.listViewButtonAccessibleName", l);

        detailsViewButtonToolTipText = UIManager.getString("FileChooser.detailsViewButtonToolTipText", l);
        detailsViewButtonAccessibleName = UIManager.getString("FileChooser.detailsViewButtonAccessibleName", l);
    }

    protected Integer getMnemonic(final String key, final Locale l) {
        return SwingUtilities2.getUIDefaultsInt(key, l);
    }

    /*
     * Listen for filechooser property changes, such as
     * the selected file changing, or the type of the dialog changing.
     */
    public PropertyChangeListener createPropertyChangeListener(final JFileChooser fc) {
        return new PropertyChangeListener() {
            public void propertyChange(final PropertyChangeEvent e) {
                String s = e.getPropertyName();
                if (s.equals(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY)) {
                    doSelectedFileChanged(e);
                } else if (s.equals(JFileChooser.SELECTED_FILES_CHANGED_PROPERTY)) {
                    doSelectedFilesChanged(e);
                } else if (s.equals(JFileChooser.DIRECTORY_CHANGED_PROPERTY)) {
                    doDirectoryChanged(e);
                } else if (s.equals(JFileChooser.FILE_FILTER_CHANGED_PROPERTY)) {
                    doFilterChanged(e);
                } else if (s.equals(JFileChooser.FILE_SELECTION_MODE_CHANGED_PROPERTY)) {
                    doFileSelectionModeChanged(e);
                } else if (s.equals(JFileChooser.ACCESSORY_CHANGED_PROPERTY)) {
                    doAccessoryChanged(e);
                } else if (s.equals(JFileChooser.APPROVE_BUTTON_TEXT_CHANGED_PROPERTY) ||
                        s.equals(JFileChooser.APPROVE_BUTTON_TOOL_TIP_TEXT_CHANGED_PROPERTY)) {
                    doApproveButtonTextChanged(e);
                } else if (s.equals(JFileChooser.DIALOG_TYPE_CHANGED_PROPERTY)) {
                    doDialogTypeChanged(e);
                } else if (s.equals(JFileChooser.APPROVE_BUTTON_MNEMONIC_CHANGED_PROPERTY)) {
                    doApproveButtonMnemonicChanged(e);
                } else if (s.equals(JFileChooser.CONTROL_BUTTONS_ARE_SHOWN_CHANGED_PROPERTY)) {
                    doControlButtonsChanged(e);
                } else if (s.equals("componentOrientation")) {
                    ComponentOrientation o = (ComponentOrientation) e.getNewValue();
                    JFileChooser cc = (JFileChooser) e.getSource();
                    if (o != e.getOldValue()) {
                        cc.applyComponentOrientation(o);
                    }
                } else if (Objects.equals(s, "FileChooser.useShellFolder")) {
                    doDirectoryChanged(e);
                } else if (s.equals("ancestor")) {
                    if (e.getOldValue() == null && e.getNewValue() != null) {
                        // Ancestor was added, set initial focus
                        fileNameTextField.selectAll();
                        fileNameTextField.requestFocus();
                    }
                }
            }
        };
    }

    protected void doSelectedFileChanged(final PropertyChangeEvent e) {
        File f = (File) e.getNewValue();
        JFileChooser fc = getFileChooser();
        if (f != null
                && ((fc.isFileSelectionEnabled() && !f.isDirectory())
                || (f.isDirectory() && fc.isDirectorySelectionEnabled()))) {

            setFileName(fileNameString(f));
        }
    }

    protected void doSelectedFilesChanged(final PropertyChangeEvent e) {
        File[] files = (File[]) e.getNewValue();
        JFileChooser fc = getFileChooser();
        if (files != null
                && files.length > 0
                && (files.length > 1 || fc.isDirectorySelectionEnabled() || !files[0].isDirectory())) {
            setFileName(fileNameString(files));
        }
    }

    protected void doDirectoryChanged(final PropertyChangeEvent e) {
        JFileChooser fc = getFileChooser();
        FileSystemView fsv = fc.getFileSystemView();

        clearIconCache();
        File currentDirectory = fc.getCurrentDirectory();
        if (currentDirectory != null) {
            directoryComboBoxModel.addItem(currentDirectory);

            if (fc.isDirectorySelectionEnabled() && !fc.isFileSelectionEnabled()) {
                if (fsv.isFileSystem(currentDirectory)) {
                    setFileName(currentDirectory.getPath());
                } else {
                    setFileName(null);
                }
            }
        }
    }

    protected void doFilterChanged(final PropertyChangeEvent e) {
        clearIconCache();
    }

    protected void doFileSelectionModeChanged(final PropertyChangeEvent e) {
        if (fileNameLabel != null) {
            populateFileNameLabel();
        }
        clearIconCache();

        JFileChooser fc = getFileChooser();
        File currentDirectory = fc.getCurrentDirectory();
        if (currentDirectory != null
                && fc.isDirectorySelectionEnabled()
                && !fc.isFileSelectionEnabled()
                && fc.getFileSystemView().isFileSystem(currentDirectory)) {

            setFileName(currentDirectory.getPath());
        } else {
            setFileName(null);
        }
    }

    protected void doAccessoryChanged(final PropertyChangeEvent e) {
        if (getAccessoryPanel() != null) {
            if (e.getOldValue() != null) {
                getAccessoryPanel().remove((JComponent) e.getOldValue());
            }
            JComponent accessory = (JComponent) e.getNewValue();
            if (accessory != null) {
                getAccessoryPanel().add(accessory, BorderLayout.CENTER);
            }
        }
    }

    protected void doApproveButtonTextChanged(final PropertyChangeEvent e) {
        JFileChooser chooser = getFileChooser();
        approveButton.setText(getApproveButtonText(chooser));
        approveButton.setToolTipText(getApproveButtonToolTipText(chooser));
    }

    protected void doDialogTypeChanged(final PropertyChangeEvent e) {
        JFileChooser chooser = getFileChooser();
        approveButton.setText(getApproveButtonText(chooser));
        approveButton.setToolTipText(getApproveButtonToolTipText(chooser));
        if (chooser.getDialogType() == JFileChooser.SAVE_DIALOG) {
            lookInLabel.setText(saveInLabelText);
        } else {
            lookInLabel.setText(lookInLabelText);
        }
    }

    protected void doApproveButtonMnemonicChanged(final PropertyChangeEvent e) {
        // Note: Metal does not use mnemonics for approve and cancel
    }

    protected void doControlButtonsChanged(final PropertyChangeEvent e) {
        if (getFileChooser().getControlButtonsAreShown()) {
            addControlButtons();
        } else {
            removeControlButtons();
        }
    }

    /**
     * Removes control buttons from bottom panel.
     */
    protected void removeControlButtons() {
        getBottomPanel().remove(getButtonPanel());
    }

    public String getFileName() {
        if (fileNameTextField != null) {
            return fileNameTextField.getText();
        } else {
            return null;
        }
    }

    public void setFileName(final String filename) {
        if (fileNameTextField != null) {
            fileNameTextField.setText(filename);
        }
    }

    /**
     * Returns the directory name.
     *
     * @return the directory name
     */
    public String getDirectoryName() {
        // PENDING(jeff) - get the name from the directory combobox
        return null;
    }

    /* The following methods are used by the PropertyChange Listener */

    /**
     * Sets the directory name.
     *
     * @param dirname the directory name
     */
    public void setDirectoryName(final String dirname) {
        // PENDING(jeff) - set the name in the directory combobox
    }

    public void rescanCurrentDirectory(final JFileChooser fc) {
        filePane.rescanCurrentDirectory();
    }

    public void ensureFileIsVisible(final JFileChooser fc, final File f) {
        filePane.ensureFileIsVisible(fc, f);
    }

    protected JButton getApproveButton(final JFileChooser fc) {
        return approveButton;
    }

    /**
     * Creates a selection listener for the list of files and directories.
     *
     * @param fc a <code>JFileChooser</code>
     * @return a <code>ListSelectionListener</code>
     */
    public ListSelectionListener createListSelectionListener(final JFileChooser fc) {
        return super.createListSelectionListener(fc);
    }

    /**
     * Property to remember whether a directory is currently selected in the UI. This is normally called by the UI on a
     * selection event.
     *
     * @param directorySelected if a directory is currently selected.
     * @since 1.4
     */
    protected void setDirectorySelected(final boolean directorySelected) {
        super.setDirectorySelected(directorySelected);
        JFileChooser chooser = getFileChooser();
        if (directorySelected) {
            if (approveButton != null) {
                approveButton.setText(directoryOpenButtonText);
                approveButton.setToolTipText(directoryOpenButtonToolTipText);
            }
        } else {
            if (approveButton != null) {
                approveButton.setText(getApproveButtonText(chooser));
                approveButton.setToolTipText(getApproveButtonToolTipText(chooser));
            }
        }
    }

    /**
     * Returns an instance of {@code ActionMap}.
     *
     * @return an instance of {@code ActionMap}
     */
    protected ActionMap getActionMap() {
        return createActionMap();
    }

    /**
     * Constructs an instance of {@code ActionMap}.
     *
     * @return an instance of {@code ActionMap}
     */
    protected ActionMap createActionMap() {
        ActionMap map = new ActionMapUIResource();
        FilePane.addActionsToMap(map, filePane.getActions());
        return map;
    }

    /**
     * Constructs a new instance of {@code DataModel} for {@code DirectoryComboBox}.
     *
     * @param fc a {@code JFileChooser}
     * @return a new instance of {@code DataModel} for {@code DirectoryComboBox}
     */
    protected DirectoryComboBoxModel createDirectoryComboBoxModel(final JFileChooser fc) {
        return new DirectoryComboBoxModel();
    }

    /**
     * Constructs a new instance of {@code DirectoryComboBoxRenderer}.
     *
     * @param fc a {@code JFileChooser}
     * @return a new instance of {@code DirectoryComboBoxRenderer}
     */
    protected DefaultListCellRenderer createDirectoryComboBoxRenderer(final JFileChooser fc) {
        return new DirectoryComboBoxRenderer();
    }

    /**
     * Returns the bottom panel.
     *
     * @return the bottom panel
     */
    protected JPanel getBottomPanel() {
        if (bottomPanel == null) {
            bottomPanel = new JPanel();
        }
        return bottomPanel;
    }

    protected void populateFileNameLabel() {
        if (getFileChooser().getFileSelectionMode() == JFileChooser.DIRECTORIES_ONLY) {
            fileNameLabel.setText(folderNameLabelText);
            fileNameLabel.setDisplayedMnemonic(folderNameLabelMnemonic);
        } else {
            fileNameLabel.setText(fileNameLabelText);
            fileNameLabel.setDisplayedMnemonic(fileNameLabelMnemonic);
        }
    }

    protected String fileNameString(final File[] files) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; files != null && i < files.length; i++) {
            if (i > 0) {
                sb.append(" ");
            }
            if (files.length > 1) {
                sb.append("\"");
            }
            sb.append(fileNameString(files[i]));
            if (files.length > 1) {
                sb.append("\"");
            }
        }
        return sb.toString();
    }

    protected String fileNameString(final File file) {
        if (file == null) {
            return null;
        } else {
            JFileChooser fc = getFileChooser();
            if ((fc.isDirectorySelectionEnabled() && !fc.isFileSelectionEnabled()) ||
                    (fc.isDirectorySelectionEnabled() && fc.isFileSelectionEnabled()
                            && fc.getFileSystemView().isFileSystemRoot(file))) {
                return file.getPath();
            } else {
                return file.getName();
            }
        }
    }

    /**
     * Constructs a {@code DataModel} for types {@code ComboBox}.
     *
     * @return a {@code DataModel} for types {@code ComboBox}
     */
    protected FilterComboBoxModel createFilterComboBoxModel() {
        return new FilterComboBoxModel();
    }

    /**
     * Constructs a {@code Renderer} for types {@code ComboBox}.
     *
     * @return a {@code Renderer} for types {@code ComboBox}
     */
    protected FilterComboBoxRenderer createFilterComboBoxRenderer() {
        return new FilterComboBoxRenderer();
    }

    /**
     * Returns the button panel.
     *
     * @return the button panel
     */
    protected JPanel getButtonPanel() {
        if (buttonPanel == null) {
            buttonPanel = new JPanel();
        }
        return buttonPanel;
    }

    /**
     * Adds control buttons to bottom panel.
     */
    protected void addControlButtons() {
        getBottomPanel().add(getButtonPanel());
    }

    protected static void groupLabels(final AlignedLabel[] group) {
        for (int i = 0; i < group.length; i++) {
            group[i].group = group;
        }
    }

    /**
     * Constructs a details view.
     *
     * @param fc a {@code JFileChooser}
     * @return the list
     */
    protected JPanel createList(final JFileChooser fc) {
        return filePane.createList();
    }

    /**
     * Constructs a details view.
     *
     * @param fc a {@code JFileChooser}
     * @return the details view
     */
    protected JPanel createDetailsView(final JFileChooser fc) {
        return filePane.createDetailsView();
    }

    /**
     * Returns the preferred size of the specified
     * <code>JFileChooser</code>.
     * The preferred size is at least as large, in both height and width, as the preferred size recommended by the file
     * chooser's layout manager.
     *
     * @param c a <code>JFileChooser</code>
     * @return a <code>Dimension</code> specifying the preferred width and height of the file chooser
     */
    @Override
    public Dimension getPreferredSize(final JComponent c) {
        int prefWidth = PREF_SIZE.width;
        Dimension d = c.getLayout().preferredLayoutSize(c);
        if (d != null) {
            return new Dimension(d.width < prefWidth ? prefWidth : d.width,
                                 d.height < PREF_SIZE.height ? PREF_SIZE.height : d.height);
        } else {
            return new Dimension(prefWidth, PREF_SIZE.height);
        }
    }

    /**
     * Returns the minimum size of the <code>JFileChooser</code>.
     *
     * @param c a <code>JFileChooser</code>
     * @return a <code>Dimension</code> specifying the minimum width and height of the file chooser
     */
    @Override
    public Dimension getMinimumSize(final JComponent c) {
        return new Dimension(MIN_WIDTH, MIN_HEIGHT);
    }

    /**
     * Returns the maximum size of the <code>JFileChooser</code>.
     *
     * @param c a <code>JFileChooser</code>
     * @return a <code>Dimension</code> specifying the maximum width and height of the file chooser
     */
    @Override
    public Dimension getMaximumSize(final JComponent c) {
        return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    /**
     * Invokes when {@code ListSelectionEvent} occurs.
     *
     * @param e an instance of {@code ListSelectionEvent}
     */
    public void valueChanged(final ListSelectionEvent e) {
        JFileChooser fc = getFileChooser();
        File f = fc.getSelectedFile();
        if (!e.getValueIsAdjusting() && f != null && !getFileChooser().isTraversable(f)) {
            setFileName(fileNameString(f));
        }
    }

    /**
     * Render different type sizes and styles.
     */
    @SuppressWarnings("serial") // Superclass is not serializable across versions
    public static class FilterComboBoxRenderer extends DefaultListCellRenderer {
        public Component getListCellRendererComponent(final JList<?> list,
                                                      final Object value, final int index, final boolean isSelected,
                                                      final boolean cellHasFocus) {

            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (value instanceof FileFilter) {
                setText(((FileFilter) value).getDescription());
            }
            setOpaque(false);

            return this;
        }
    }

    /**
     * <code>ButtonAreaLayout</code> behaves in a similar manner to
     * <code>FlowLayout</code>. It lays out all components from left to
     * right, flushed right. The widths of all components will be set to the largest preferred size width.
     */
    protected static class ButtonAreaLayout implements LayoutManager {
        protected int hGap = 5;
        protected int topMargin = 17;

        public void addLayoutComponent(final String string, final Component comp) {
        }

        public void removeLayoutComponent(final Component c) {
        }

        public Dimension preferredLayoutSize(final Container c) {
            return minimumLayoutSize(c);
        }

        public Dimension minimumLayoutSize(final Container c) {
            if (c != null) {
                Component[] children = c.getComponents();

                if (children != null && children.length > 0) {
                    int numChildren = children.length;
                    int height = 0;
                    Insets cInsets = c.getInsets();
                    int extraHeight = topMargin + cInsets.top + cInsets.bottom;
                    int extraWidth = cInsets.left + cInsets.right;
                    int maxWidth = 0;

                    for (int counter = 0; counter < numChildren; counter++) {
                        Dimension aSize = children[counter].getPreferredSize();
                        height = Math.max(height, aSize.height);
                        maxWidth = Math.max(maxWidth, aSize.width);
                    }
                    return new Dimension(extraWidth + numChildren * maxWidth +
                                                 (numChildren - 1) * hGap,
                                         extraHeight + height);
                }
            }
            return new Dimension(0, 0);
        }

        public void layoutContainer(final Container container) {
            Component[] children = container.getComponents();

            if (children != null && children.length > 0) {
                int numChildren = children.length;
                Dimension[] sizes = new Dimension[numChildren];
                Insets insets = container.getInsets();
                int yLocation = insets.top + topMargin;
                int maxWidth = 0;

                for (int counter = 0; counter < numChildren; counter++) {
                    sizes[counter] = children[counter].getPreferredSize();
                    maxWidth = Math.max(maxWidth, sizes[counter].width);
                }
                int xLocation, xOffset;
                if (container.getComponentOrientation().isLeftToRight()) {
                    xLocation = container.getSize().width - insets.left - maxWidth;
                    xOffset = hGap + maxWidth;
                } else {
                    xLocation = insets.left;
                    xOffset = -(hGap + maxWidth);
                }
                for (int counter = numChildren - 1; counter >= 0; counter--) {
                    children[counter].setBounds(xLocation, yLocation,
                                                maxWidth, sizes[counter].height);
                    xLocation -= xOffset;
                }
            }
        }
    }

    protected class MetalFileChooserUIAccessor implements DarkFilePaneUIBridge.FileChooserUIAccessor {
        public JFileChooser getFileChooser() {
            return DarkFileChooserUIBridge.this.getFileChooser();
        }

        public BasicDirectoryModel getModel() {
            return DarkFileChooserUIBridge.this.getModel();
        }

        public JPanel createList() {
            return DarkFileChooserUIBridge.this.createList(getFileChooser());
        }

        public JPanel createDetailsView() {
            return DarkFileChooserUIBridge.this.createDetailsView(getFileChooser());
        }

        public boolean isDirectorySelected() {
            return DarkFileChooserUIBridge.this.isDirectorySelected();
        }

        public File getDirectory() {
            return DarkFileChooserUIBridge.this.getDirectory();
        }

        public Action getChangeToParentDirectoryAction() {
            return DarkFileChooserUIBridge.this.getChangeToParentDirectoryAction();
        }

        public Action getApproveSelectionAction() {
            return DarkFileChooserUIBridge.this.getApproveSelectionAction();
        }

        public Action getNewFolderAction() {
            return DarkFileChooserUIBridge.this.getNewFolderAction();
        }

        public MouseListener createDoubleClickListener(final JList<?> list) {
            return DarkFileChooserUIBridge.this.createDoubleClickListener(getFileChooser(),
                                                                          list);
        }

        public ListSelectionListener createListSelectionListener() {
            return DarkFileChooserUIBridge.this.createListSelectionListener(getFileChooser());
        }
    }

    /**
     * Obsolete class, not used in this version.
     *
     * @deprecated As of JDK version 9. Obsolete class.
     */
    protected class SingleClickListener extends MouseAdapter {
        /**
         * Constructs an instance of {@code SingleClickListener}.
         *
         * @param list an instance of {@code JList}
         */
        public SingleClickListener(final JList<?> list) {
        }
    }

    /**
     * Obsolete class, not used in this version.
     *
     * @deprecated As of JDK version 9. Obsolete class.
     */
    @SuppressWarnings("serial") // Superclass is not serializable across versions
    protected class FileRenderer extends DefaultListCellRenderer {
    }

    //
    // Renderer for DirectoryComboBox
    //
    @SuppressWarnings("serial")
            // Superclass is not serializable across versions
    class DirectoryComboBoxRenderer extends DefaultListCellRenderer {
        IndentIcon ii = new IndentIcon();

        public Component getListCellRendererComponent(final JList<?> list, final Object value,
                                                      final int index, final boolean isSelected,
                                                      final boolean cellHasFocus) {

            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (value == null) {
                setText("");
                return this;
            }
            File directory = (File) value;
            setText(getFileChooser().getName(directory));
            Icon icon = getFileChooser().getIcon(directory);
            ii.icon = icon;
            ii.depth = directoryComboBoxModel.getDepth(index);
            setIcon(ii);

            return this;
        }
    }

    class IndentIcon implements Icon {

        Icon icon = null;
        int depth = 0;

        public void paintIcon(final Component c, final Graphics g, final int x, final int y) {
            if (c.getComponentOrientation().isLeftToRight()) {
                icon.paintIcon(c, g, x + depth * space, y);
            } else {
                icon.paintIcon(c, g, x, y);
            }
        }

        public int getIconWidth() {
            return icon.getIconWidth() + depth * space;
        }

        public int getIconHeight() {
            return icon.getIconHeight();
        }

    }

    /**
     * Data model for a type-face selection combo-box.
     */
    @SuppressWarnings("serial") // Superclass is not serializable across versions
    protected class DirectoryComboBoxModel extends AbstractListModel<Object> implements ComboBoxModel<Object> {
        Vector<File> directories = new Vector<File>();
        int[] depths = null;
        File selectedDirectory = null;
        JFileChooser chooser = getFileChooser();
        FileSystemView fsv = chooser.getFileSystemView();

        /**
         * Constructs an instance of {@code DirectoryComboBoxModel}.
         */
        public DirectoryComboBoxModel() {
            // Add the current directory to the model, and make it the
            // selectedDirectory
            File dir = getFileChooser().getCurrentDirectory();
            if (dir != null) {
                addItem(dir);
            }
        }

        /**
         * Adds the directory to the model and sets it to be selected, additionally clears out the previous selected
         * directory and the paths leading up to it, if any.
         *
         * @param directory the directory
         */
        protected void addItem(final File directory) {

            if (directory == null) {
                return;
            }

            boolean useShellFolder = FilePane.usesShellFolder(chooser);

            directories.clear();

            File[] baseFolders = (useShellFolder)
                                 ? (File[]) ShellFolder.get("fileChooserComboBoxFolders")
                                 : fsv.getRoots();
            directories.addAll(Arrays.asList(baseFolders));

            // Get the canonical (full) path. This has the side
            // benefit of removing extraneous chars from the path,
            // for example /foo/bar/ becomes /foo/bar
            File canonical;
            try {
                canonical = ShellFolder.getNormalizedFile(directory);
            } catch (IOException e) {
                // Maybe drive is not ready. Can't abort here.
                canonical = directory;
            }

            // create File instances of each directory leading up to the top
            try {
                File sf = useShellFolder ? ShellFolder.getShellFolder(canonical)
                                         : canonical;
                File f = sf;
                Vector<File> path = new Vector<File>(10);
                do {
                    path.addElement(f);
                } while ((f = f.getParentFile()) != null);

                int pathCount = path.size();
                // Insert chain at appropriate place in vector
                for (int i = 0; i < pathCount; i++) {
                    f = path.get(i);
                    if (directories.contains(f)) {
                        int topIndex = directories.indexOf(f);
                        for (int j = i - 1; j >= 0; j--) {
                            directories.insertElementAt(path.get(j), topIndex + i - j);
                        }
                        break;
                    }
                }
                calculateDepths();
                setSelectedItem(sf);
            } catch (FileNotFoundException ex) {
                calculateDepths();
            }
        }

        protected void calculateDepths() {
            depths = new int[directories.size()];
            for (int i = 0; i < depths.length; i++) {
                File dir = directories.get(i);
                File parent = dir.getParentFile();
                depths[i] = 0;
                if (parent != null) {
                    for (int j = i - 1; j >= 0; j--) {
                        if (parent.equals(directories.get(j))) {
                            depths[i] = depths[j] + 1;
                            break;
                        }
                    }
                }
            }
        }

        /**
         * Returns the depth of {@code i}-th file.
         *
         * @param i an index
         * @return the depth of {@code i}-th file
         */
        public int getDepth(final int i) {
            return (depths != null && i >= 0 && i < depths.length) ? depths[i] : 0;
        }

        public int getSize() {
            return directories.size();
        }

        public Object getElementAt(final int index) {
            return directories.elementAt(index);
        }

        public void setSelectedItem(final Object selectedDirectory) {
            this.selectedDirectory = (File) selectedDirectory;
            fireContentsChanged(this, -1, -1);
        }


        public Object getSelectedItem() {
            return selectedDirectory;
        }


    }

    /**
     * Data model for a type-face selection combo-box.
     */
    @SuppressWarnings("serial") // Same-version serialization only
    protected class FilterComboBoxModel extends AbstractListModel<Object> implements ComboBoxModel<Object>, PropertyChangeListener {

        /**
         * An array of file filters.
         */
        protected FileFilter[] filters;

        /**
         * Constructs an instance of {@code FilterComboBoxModel}.
         */
        protected FilterComboBoxModel() {
            super();
            filters = getFileChooser().getChoosableFileFilters();
        }

        public void propertyChange(final PropertyChangeEvent e) {
            String prop = e.getPropertyName();
            if (Objects.equals(prop, JFileChooser.CHOOSABLE_FILE_FILTER_CHANGED_PROPERTY)) {
                filters = (FileFilter[]) e.getNewValue();
                fireContentsChanged(this, -1, -1);
            } else if (Objects.equals(prop, JFileChooser.FILE_FILTER_CHANGED_PROPERTY)) {
                fireContentsChanged(this, -1, -1);
            }
        }

        public void setSelectedItem(final Object filter) {
            if (filter != null) {
                getFileChooser().setFileFilter((FileFilter) filter);
                fireContentsChanged(this, -1, -1);
            }
        }

        public Object getSelectedItem() {
            // Ensure that the current filter is in the list.
            // NOTE: we shouldnt' have to do this, since JFileChooser adds
            // the filter to the choosable filters list when the filter
            // is set. Lets be paranoid just in case someone overrides
            // setFileFilter in JFileChooser.
            FileFilter currentFilter = getFileChooser().getFileFilter();
            boolean found = false;
            if (currentFilter != null) {
                for (FileFilter filter : filters) {
                    if (filter == currentFilter) {
                        found = true;
                    }
                }
                if (!found) {
                    getFileChooser().addChoosableFileFilter(currentFilter);
                }
            }
            return getFileChooser().getFileFilter();
        }

        public int getSize() {
            if (filters != null) {
                return filters.length;
            } else {
                return 0;
            }
        }

        public Object getElementAt(final int index) {
            if (index > getSize() - 1) {
                // This shouldn't happen. Try to recover gracefully.
                return getFileChooser().getFileFilter();
            }
            if (filters != null) {
                return filters[index];
            } else {
                return null;
            }
        }
    }

    /**
     * Acts when DirectoryComboBox has changed the selected item.
     */
    @SuppressWarnings("serial") // Superclass is not serializable across versions
    protected class DirectoryComboBoxAction extends AbstractAction {

        /**
         * Constructs a new instance of {@code DirectoryComboBoxAction}.
         */
        protected DirectoryComboBoxAction() {
            super("DirectoryComboBoxAction");
        }

        public void actionPerformed(final ActionEvent e) {
            directoryComboBox.hidePopup();
            File f = (File) directoryComboBox.getSelectedItem();
            if (!getFileChooser().getCurrentDirectory().equals(f)) {
                getFileChooser().setCurrentDirectory(f);
            }
        }
    }

    @SuppressWarnings("serial") // Superclass is not serializable across versions
    protected class AlignedLabel extends JLabel {
        protected AlignedLabel[] group;
        protected int maxWidth = 0;

        AlignedLabel() {
            super();
            setAlignmentX(JComponent.LEFT_ALIGNMENT);
        }


        AlignedLabel(final String text) {
            super(text);
            setAlignmentX(JComponent.LEFT_ALIGNMENT);
        }

        public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            // Align the width with all other labels in group.
            return new Dimension(getMaxWidth() + 11, d.height);
        }

        protected int getMaxWidth() {
            if (maxWidth == 0 && group != null) {
                int max = 0;
                for (int i = 0; i < group.length; i++) {
                    max = Math.max(group[i].getSuperPreferredWidth(), max);
                }
                for (int i = 0; i < group.length; i++) {
                    group[i].maxWidth = max;
                }
            }
            return maxWidth;
        }

        protected int getSuperPreferredWidth() {
            return super.getPreferredSize().width;
        }
    }
}
