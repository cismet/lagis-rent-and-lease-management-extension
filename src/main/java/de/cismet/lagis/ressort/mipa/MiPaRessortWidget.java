/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * MiPaRessortWidget.java
 *
 * Created on 23. April 2008, 11:26
 */
package de.cismet.lagis.ressort.mipa;

import com.vividsolutions.jts.geom.Geometry;

import edu.umd.cs.piccolo.PCanvas;

import org.apache.log4j.Logger;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.SortOrder;

import java.awt.Component;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;

import de.cismet.cids.custom.beans.lagis.*;

import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.FeatureCollection;
import de.cismet.cismap.commons.features.FeatureCollectionEvent;
import de.cismet.cismap.commons.features.FeatureCollectionListener;
import de.cismet.cismap.commons.features.StyledFeature;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.StyledFeatureGroupWrapper;

import de.cismet.lagis.broker.CidsBroker;
import de.cismet.lagis.broker.LagisBroker;

import de.cismet.lagis.editor.DateEditor;

import de.cismet.lagis.gui.checkbox.JCheckBoxList;
import de.cismet.lagis.gui.copypaste.Copyable;
import de.cismet.lagis.gui.copypaste.Pasteable;

import de.cismet.lagis.interfaces.FeatureSelectionChangedListener;
import de.cismet.lagis.interfaces.FlurstueckChangeListener;
import de.cismet.lagis.interfaces.FlurstueckSaver;
import de.cismet.lagis.interfaces.GeometrySlotProvider;

import de.cismet.lagis.models.DefaultUniqueListModel;

import de.cismet.lagis.renderer.DateRenderer;
import de.cismet.lagis.renderer.FlurstueckSchluesselRenderer;

import de.cismet.lagis.thread.BackgroundUpdateThread;

import de.cismet.lagis.utillity.GeometrySlotInformation;

import de.cismet.lagis.validation.Validatable;
import de.cismet.lagis.validation.Validator;

import de.cismet.lagis.widget.AbstractWidget;

import de.cismet.lagisEE.entity.basic.BasicEntity;
import de.cismet.lagisEE.entity.core.Flurstueck;
import de.cismet.lagisEE.entity.core.FlurstueckSchluessel;
import de.cismet.lagisEE.entity.core.hardwired.FlurstueckArt;
import de.cismet.lagisEE.entity.extension.vermietung.MiPa;
import de.cismet.lagisEE.entity.extension.vermietung.MiPaKategorie;
import de.cismet.lagisEE.entity.extension.vermietung.MiPaKategorieAuspraegung;
import de.cismet.lagisEE.entity.extension.vermietung.MiPaMerkmal;
import de.cismet.lagisEE.entity.extension.vermietung.MiPaNutzung;

import de.cismet.tools.CurrentStackTrace;

import de.cismet.tools.gui.StaticSwingTools;

/**
 * DOCUMENT ME!
 *
 * @author   Sebastian Puhl
 * @version  $Revision$, $Date$
 */
public class MiPaRessortWidget extends AbstractWidget implements FlurstueckChangeListener,
    FlurstueckSaver,
    MouseListener,
    ListSelectionListener,
    ItemListener,
    GeometrySlotProvider,
    FeatureSelectionChangedListener,
    FeatureCollectionListener,
    TableModelListener,
    Copyable,
    Pasteable {

    //~ Static fields/initializers ---------------------------------------------

    private static final String PROVIDER_NAME = "MiPa";

    private static final String WIDGET_ICON = "/de/cismet/lagis/ressort/mipa/icons/mipa.png";

    //~ Instance fields --------------------------------------------------------

    private final Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private boolean isFlurstueckEditable = true;
    private boolean isInEditMode = false;
    private MiPaModel miPaModel = new MiPaModel();
    private BackgroundUpdateThread<Flurstueck> updateThread;
    private Flurstueck currentFlurstueck;
    private Validator valTxtBemerkung;
    private ImageIcon icoExistingContract = new javax.swing.ImageIcon(getClass().getResource(
                "/de/cismet/lagis/ressource/icons/toolbar/contract.png"));
    private JComboBox cbxAuspraegung = new JComboBox();
    private boolean ignoreFeatureSelectionEvent = false;
    private final Icon copyDisplayIcon;

    private final ActionListener cboAuspraegungsActionListener = new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent event) {
//                log.debug("Action Performed CboAusprägung");
//                if ((event.getModifiers() & InputEvent.BUTTON1_MASK) != 0 && event.getActionCommand().equals("comboBoxChanged") && (cbxAuspraegung.getSelectedItem() != null && !(cbxAuspraegung.getSelectedItem() instanceof String) && (cbxAuspraegung.getSelectedItem() instanceof MiPaKategorieAuspraegung))) {
//                    log.debug("maus/comboChanged/noString");
//                    if (tblMipa.getCellEditor() != null) {
//                        log.debug("CellEditor wird geschlossen");
//                        tblMipa.getCellEditor().stopCellEditing();
//                    }
//                }
            }
        };

    private boolean listenerEnabled = true;

    // funktioniert nicht wann wird es ausgelöst ?
// public void tableChanged(TableModelEvent e) {
// if (e.getColumn() == 5) {
// log.debug("Eine Kategorie hat sich Verändert");
// int selectedRow = tblMipa.getSelectedRow();
// if (selectedRow != -1) {
// int modelIndex = ((JXTable) tblMipa).getFilters().convertRowIndexToModel(selectedRow);
// MiPa mp = mipaModel.getMiPaAtRow(modelIndex);
// if (mp != null) {
// updateCbxAuspraegung(mp);
// }
// }
// }
// ;
// }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddExitingMiPa;
    private javax.swing.JButton btnAddMiPa;
    private javax.swing.JButton btnRemoveMiPa;
    private javax.swing.JScrollPane cpMiPa;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTabbedPane jTabbedPane2;
    private javax.swing.JList lstCrossRefs;
    private javax.swing.JList lstMerkmale;
    private javax.swing.JPanel panBackground;
    private javax.swing.JPanel panBemerkung;
    private javax.swing.JPanel panBemerkungTitled;
    private javax.swing.JPanel panMerkmale;
    private javax.swing.JPanel panMerkmaleTitled;
    private javax.swing.JPanel panMiPaBordered;
    private javax.swing.JPanel panQuerverweise;
    private javax.swing.JPanel panQuerverweiseTitled;
    private javax.swing.JScrollPane spBemerkung;
    private javax.swing.JScrollPane spMerkmale;
    private javax.swing.JTextArea taBemerkung;
    private javax.swing.JTable tblMipa;
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new MiPaRessortWidget object.
     *
     * @param  widgetName  DOCUMENT ME!
     */
    public MiPaRessortWidget(final String widgetName) {
        this(widgetName, WIDGET_ICON);
    }

    /**
     * Creates a new MiPaRessortWidget object.
     *
     * @param  widgetName  DOCUMENT ME!
     * @param  iconPath    DOCUMENT ME!
     */
    public MiPaRessortWidget(final String widgetName, final String iconPath) {
        initComponents();
        setWidgetName(widgetName);
        setWidgetIcon(iconPath);
        configureComponents();
        configBackgroundThread();
        setOpaqueRecursive(panBackground.getComponents());

        this.copyDisplayIcon = new ImageIcon(this.getClass().getResource(WIDGET_ICON));
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  components  DOCUMENT ME!
     */
    private void setOpaqueRecursive(final Component[] components) {
        for (final Component currentComp : components) {
            if (currentComp instanceof Container) {
                setOpaqueRecursive(((Container)currentComp).getComponents());
            }
            if (currentComp instanceof JComponent) {
                ((JComponent)currentComp).setOpaque(false);
            }
        }
    }

    /**
     * DOCUMENT ME!
     */
    private void configureComponents() {
        tblMipa.setModel(miPaModel);
        tblMipa.setDefaultEditor(Date.class, new DateEditor());
        tblMipa.setDefaultRenderer(Date.class, new DateRenderer());
        tblMipa.addMouseListener(this);

        final HighlightPredicate noGeometryPredicate = new HighlightPredicate() {

                @Override
                public boolean isHighlighted(final Component renderer, final ComponentAdapter componentAdapter) {
                    final int displayedIndex = componentAdapter.row;
                    final int modelIndex = ((JXTable)tblMipa).getFilters().convertRowIndexToModel(displayedIndex);
                    final MiPa mp = miPaModel.getMiPaAtRow(modelIndex);
                    return (mp != null) && (mp.getGeometry() == null);
                }
            };

        final Highlighter noGeometryHighlighter = new ColorHighlighter(noGeometryPredicate, LagisBroker.grey, null);

        final HighlightPredicate contractExpiredPredicate = new HighlightPredicate() {

                @Override
                public boolean isHighlighted(final Component renderer, final ComponentAdapter componentAdapter) {
                    final int displayedIndex = componentAdapter.row;
                    final int modelIndex = ((JXTable)tblMipa).getFilters().convertRowIndexToModel(displayedIndex);
                    final MiPa mp = miPaModel.getMiPaAtRow(modelIndex);
                    return (mp != null) && (mp.getVertragsende() != null) && (mp.getVertragsbeginn() != null)
                                && (mp.getVertragsende().getTime() < System.currentTimeMillis());
                }
            };

        final Highlighter contractExpiredHighlighter = new ColorHighlighter(
                contractExpiredPredicate,
                LagisBroker.SUCCESSFUL_COLOR,
                null);
        // HighlighterPipeline hPipline = new HighlighterPipeline(new
        // Highlighter[]{LagisBroker.ALTERNATE_ROW_HIGHLIGHTER,contractExpiredHighlighter,noGeometryHighlighter});
        ((JXTable)tblMipa).setHighlighters(
            LagisBroker.ALTERNATE_ROW_HIGHLIGHTER,
            contractExpiredHighlighter,
            noGeometryHighlighter);

        final Comparator dateComparator = new Comparator() {

                @Override
                public int compare(final Object o1, final Object o2) {
                    if ((o1 == null) && (o2 == null)) {
                        return 0;
                    } else if (o1 == null) {
                        return 1;
                    } else if (o2 == null) {
                        return -1;
                    } else {
                        return -1 * ((Date)o1).compareTo((Date)o2);
                    }
                }
            };
        ((JXTable)tblMipa).getColumnExt(miPaModel.VERTRAGS_ENDE_COLUMN).setComparator(dateComparator);
        ((JXTable)tblMipa).setSortOrder(miPaModel.VERTRAGS_ENDE_COLUMN, SortOrder.ASCENDING);
        tblMipa.getSelectionModel().addListSelectionListener(this);
        ((JXTable)tblMipa).setColumnControlVisible(true);
        ((JXTable)tblMipa).setHorizontalScrollEnabled(true);
//        TableColumnExt id = ((JXTable) tblMipa).getColumnExt(0);
//        id.setVisible(false);
        TableColumn tc = tblMipa.getColumnModel().getColumn(miPaModel.NUTZUNG_COLUMN);
        // Kategorien EditorCombobox
        final JComboBox combo = new JComboBox();
        combo.setBorder(new javax.swing.border.EmptyBorder(0, 0, 0, 0));
        combo.setEditable(true);

        final Collection<MipaKategorieCustomBean> alleKategorien = CidsBroker.getInstance().getAllMiPaKategorien();
        for (final MiPaKategorie currentKategorie : alleKategorien) {
            combo.addItem(currentKategorie);
        }
        org.jdesktop.swingx.autocomplete.AutoCompleteDecorator.decorate(combo);
        combo.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(final ActionEvent event) {
//                log.debug("action Performed Kategorie CBX command:" + event.getActionCommand() + " modifier:" + event.getModifiers() + " currentSelected: " + combo.getSelectedItem() +" instanceOf MiPaKategorie: "+(combo.getSelectedItem() != null && combo.getSelectedItem() instanceof MiPaKategorie)+"instanceof String"+(combo.getSelectedItem() != null && combo.getSelectedItem() instanceof String),new CurrentStackTrace());
////                if ((event.getModifiers() & InputEvent.BUTTON1_MASK) != 0 && (event.getActionCommand().equals("comboBoxChanged")) && (combo.getSelectedItem() != null && (combo.getSelectedItem() instanceof MiPaKategorie))) {
////                    if (tblMipa.getCellEditor() != null) {
////                        tblMipa.getCellEditor().stopCellEditing();
////                    }
////                }
                }
            });
        tc.setCellEditor(new org.jdesktop.swingx.autocomplete.ComboBoxCellEditor(combo));

        tc = tblMipa.getColumnModel().getColumn(miPaModel.AUSPRAEGUNG_COLUMN);
        cbxAuspraegung.setBorder(new javax.swing.border.EmptyBorder(0, 0, 0, 0));
        cbxAuspraegung.setEditable(true);
        org.jdesktop.swingx.autocomplete.AutoCompleteDecorator.decorate(cbxAuspraegung);
        cbxAuspraegung.addActionListener(cboAuspraegungsActionListener);
        tc.setCellEditor(new org.jdesktop.swingx.autocomplete.ComboBoxCellEditor(cbxAuspraegung));

        ((JXTable)tblMipa).packAll();

        taBemerkung.setDocument(miPaModel.getBemerkungDocumentModel());

        enableSlaveComponents(false);
        // lstMerkmale.setm
        final Collection<MipaMerkmalCustomBean> miPaMerkmale = CidsBroker.getInstance().getAllMiPaMerkmale();
        final Vector<MerkmalCheckBox> merkmalCheckBoxes = new Vector<MerkmalCheckBox>();
        if ((miPaMerkmale != null) && (miPaMerkmale.size() > 0)) {
            for (final MipaMerkmalCustomBean currentMerkmal : miPaMerkmale) {
                if ((currentMerkmal != null) && (currentMerkmal.getBezeichnung() != null)) {
                    final MerkmalCheckBox newMerkmalCheckBox = new MerkmalCheckBox(currentMerkmal);
                    setOpaqueRecursive(newMerkmalCheckBox.getComponents());
                    newMerkmalCheckBox.setOpaque(false);
                    newMerkmalCheckBox.addItemListener(this);
                    merkmalCheckBoxes.add(newMerkmalCheckBox);
                }
            }
        }
        lstMerkmale.setListData(merkmalCheckBoxes);

        lstCrossRefs.setCellRenderer(new FlurstueckSchluesselRenderer());
        lstCrossRefs.setModel(new DefaultUniqueListModel());
        lstCrossRefs.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lstCrossRefs.addMouseListener(this);
        lstCrossRefs.setCellRenderer(new DefaultListCellRenderer() {

                @Override
                public Component getListCellRendererComponent(final JList list,
                        final Object value,
                        final int index,
                        final boolean isSelected,
                        final boolean cellHasFocus) {
                    String s = null;
                    if (value instanceof FlurstueckSchluessel) {
                        s = ((FlurstueckSchluessel)value).getKeyString();
                    } else {
                        s = value.toString();
                    }

                    setText(s);
                    setOpaque(false);

                    setEnabled(list.isEnabled());
                    setFont(list.getFont());
                    return this;
                }
            });

        final PCanvas pc = LagisBroker.getInstance().getMappingComponent().getSelectedObjectPresenter();
        pc.setBackground(this.getBackground());
        // ((SimpleBackgroundedJPanel) this.panBackground).setPCanvas(pc);
        // ((SimpleBackgroundedJPanel) this.panBackground).setTranslucency(0.5f);
        // ((SimpleBackgroundedJPanel) this.panBackground).setBackgroundEnabled(true);
        LagisBroker.getInstance().getMappingComponent().getFeatureCollection().addFeatureCollectionListener(this);

//      disabled because of the background transparency
//        taBemerkung.setDocument(miPaModel.getBemerkungDocumentModel());
//        valTxtBemerkung = new Validator(taBemerkung);
//        valTxtBemerkung.reSetValidator((Validatable) miPaModel.getBemerkungDocumentModel());
        miPaModel.addTableModelListener(this);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  mp  DOCUMENT ME!
     */
    private void updateCbxAuspraegung(final MiPa mp) {
        if (log.isDebugEnabled()) {
            log.debug("Update der Ausprägungen");
        }
        cbxAuspraegung.removeActionListener(cboAuspraegungsActionListener);
        cbxAuspraegung.removeAllItems();
        final int maxNumericEntries = 100;
        if ((mp != null) && (mp.getMiPaNutzung() != null)
                    && (mp.getMiPaNutzung().getMiPaKategorie() != null)
                    && mp.getMiPaNutzung().getMiPaKategorie().getHatNummerAlsAuspraegung()) {
            if (cbxAuspraegung.getItemCount() != maxNumericEntries) {
                for (int i = 1; i <= maxNumericEntries; i++) {
                    cbxAuspraegung.addItem(i);
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Kein Update nötig Zahlen sind schon in der Combobox");
                }
            }
        } else if ((mp != null) && (mp.getMiPaNutzung() != null) && (mp.getMiPaNutzung().getMiPaKategorie() != null)
                    && (mp.getMiPaNutzung().getMiPaKategorie().getKategorieAuspraegungen() != null)) {
            if (log.isDebugEnabled()) {
                log.debug("Ausprägungen sind vorhanden");
            }
            final Collection<MipaKategorieAuspraegungCustomBean> auspraegungen = mp.getMiPaNutzung()
                        .getMiPaKategorie()
                        .getKategorieAuspraegungen();
            for (final MiPaKategorieAuspraegung currentAuspraegung : auspraegungen) {
                cbxAuspraegung.addItem(currentAuspraegung);
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Keine Ausprägungen vorhanden");
            }
        }
        cbxAuspraegung.validate();
        cbxAuspraegung.repaint();
        cbxAuspraegung.updateUI();
        cbxAuspraegung.addActionListener(cboAuspraegungsActionListener);
    }

    /**
     * DOCUMENT ME!
     */
    private void configBackgroundThread() {
        updateThread = new BackgroundUpdateThread<Flurstueck>() {

                @Override
                protected void update() {
                    try {
                        if (isUpdateAvailable()) {
                            cleanup();
                            return;
                        }
                        clearComponent();
                        if (isUpdateAvailable()) {
                            cleanup();
                            return;
                        }
                        final FlurstueckArt flurstueckArt = getCurrentObject().getFlurstueckSchluessel()
                                    .getFlurstueckArt();
                        if ((flurstueckArt != null)
                                    && flurstueckArt.getBezeichnung().equals(
                                        FlurstueckArt.FLURSTUECK_ART_BEZEICHNUNG_STAEDTISCH)) {
                            if (log.isDebugEnabled()) {
                                log.debug("Flurstück ist städtisch und kann editiert werden");
                            }
                            isFlurstueckEditable = true;
                        } else {
                            if (log.isDebugEnabled()) {
                                log.debug("Flurstück ist nicht städtisch und kann nicht editiert werden");
                            }
                            isFlurstueckEditable = false;
                        }

                        miPaModel.refreshTableModel(getCurrentObject().getMiPas());
                        if (isUpdateAvailable()) {
                            cleanup();
                            return;
                        }
                        final Collection<FlurstueckSchluesselCustomBean> crossRefs = getCurrentObject()
                                    .getMiPasQuerverweise();
                        if ((crossRefs != null) && (crossRefs.size() > 0)) {
                            lstCrossRefs.setModel(new DefaultUniqueListModel(crossRefs));
                        }
                        if (isUpdateAvailable()) {
                            cleanup();
                            return;
                        }
                        EventQueue.invokeLater(new Runnable() {

                                @Override
                                public void run() {
                                    final Vector<Feature> features = miPaModel.getAllMiPaFeatures();
                                    final MappingComponent mappingComp = LagisBroker.getInstance()
                                                .getMappingComponent();
                                    final FeatureCollection featureCollection = mappingComp.getFeatureCollection();
                                    if (features != null) {
                                        for (Feature currentFeature : features) {
                                            if (currentFeature != null) {
                                                if (isWidgetReadOnly()) {
                                                    ((MiPa)currentFeature).setModifiable(false);
                                                }

                                                currentFeature = new StyledFeatureGroupWrapper(
                                                        (StyledFeature)currentFeature,
                                                        PROVIDER_NAME,
                                                        PROVIDER_NAME);

                                                featureCollection.addFeature(currentFeature);
                                            }
                                        }
                                    }
                                    ((JXTable)tblMipa).packAll();
                                }
                            });
                        if (isUpdateAvailable()) {
                            cleanup();
                            return;
                        }

                        LagisBroker.getInstance().flurstueckChangeFinished(MiPaRessortWidget.this);
                    } catch (Exception ex) {
                        log.error("Fehler im refresh thread: ", ex);
                        LagisBroker.getInstance().flurstueckChangeFinished(MiPaRessortWidget.this);
                    }
                }

                @Override
                protected void cleanup() {
                }
            };
        updateThread.setPriority(Thread.NORM_PRIORITY);
        updateThread.start();
    }

    @Override
    public void clearComponent() {
        if (log.isDebugEnabled()) {
            log.debug("clearComponent", new CurrentStackTrace());
        }
        miPaModel.clearSlaveComponents();
        deselectAllListEntries();
        miPaModel.refreshTableModel(new HashSet<MipaCustomBean>());
        lstCrossRefs.setModel(new DefaultUniqueListModel());
        if (EventQueue.isDispatchThread()) {
            lstCrossRefs.updateUI();
            lstCrossRefs.repaint();
        } else {
            EventQueue.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        lstCrossRefs.updateUI();
                        lstCrossRefs.repaint();
                    }
                });
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void deselectAllListEntries() {
        if (log.isDebugEnabled()) {
            log.debug("deselect all entries", new CurrentStackTrace());
        }
        for (int i = 0; i < lstMerkmale.getModel().getSize(); i++) {
            final MerkmalCheckBox currentCheckBox = (MerkmalCheckBox)lstMerkmale.getModel().getElementAt(i);
            currentCheckBox.removeItemListener(this);
            currentCheckBox.setSelected(false);
            currentCheckBox.addItemListener(this);
        }
    }

    @Override
    public void refresh(final Object arg0) {
    }

    @Override
    public void setComponentEditable(final boolean isEditable) {
        if (isFlurstueckEditable) {
            if (log.isDebugEnabled()) {
                log.debug("MiPARessortWidget --> setComponentEditable");
            }
            isInEditMode = isEditable;
            miPaModel.setIsInEditMode(isEditable);
            final TableCellEditor currentEditor = tblMipa.getCellEditor();
            if (currentEditor != null) {
                currentEditor.cancelCellEditing();
            }

            if (isEditable && (tblMipa.getSelectedRow() != -1)) {
                if (log.isDebugEnabled()) {
                    log.debug("Editable und TabellenEintrag ist gewählt");
                }
                btnRemoveMiPa.setEnabled(true);
                enableSlaveComponents(isEditable);
            } else if (!isEditable) {
                deselectAllListEntries();
                enableSlaveComponents(isEditable);
                btnRemoveMiPa.setEnabled(isEditable);
            }

            btnAddExitingMiPa.setEnabled(isEditable);
            btnAddMiPa.setEnabled(isEditable);
            miPaModel.setIsInEditMode(isEditable);
            if (log.isDebugEnabled()) {
                log.debug("MiPARessortWidget --> setComponentEditable finished");
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Flurstück ist nicht städtisch Vermietung & Verpachtungen können nicht editiert werden");
            }
        }
    }

    @Override
    public void flurstueckChanged(final FlurstueckCustomBean newFlurstueck) {
        try {
            log.info("FlurstueckChanged");
            currentFlurstueck = newFlurstueck;
            updateThread.notifyThread(currentFlurstueck);
        } catch (Exception ex) {
            log.error("Fehler beim Flurstückswechsel: ", ex);
            LagisBroker.getInstance().flurstueckChangeFinished(MiPaRessortWidget.this);
        }
    }

    @Override
    public void updateFlurstueckForSaving(final FlurstueckCustomBean flurstueck) {
        final Collection<MipaCustomBean> miPas = flurstueck.getMiPas();
        if (miPas != null) {
            miPas.clear();
            miPas.addAll(miPaModel.getAllMiPas());
        } else {
            final HashSet newSet = new HashSet();
            newSet.addAll(miPaModel.getAllMiPas());
            flurstueck.setMiPas(newSet);
        }
    }

    @Override
    public void mouseClicked(final MouseEvent e) {
        final Object source = e.getSource();
        if (source instanceof JXTable) {
            if (log.isDebugEnabled()) {
                log.debug("Mit maus auf MiPaTabelle geklickt");
            }
            final int selecetdRow = tblMipa.getSelectedRow();
            if (selecetdRow != -1) {
                if (isInEditMode) {
                    enableSlaveComponents(true);
                    btnRemoveMiPa.setEnabled(true);
                } else {
                    enableSlaveComponents(false);
                    if (log.isDebugEnabled()) {
                        log.debug("Liste ausgeschaltet");
                    }
                    if (selecetdRow == -1) {
                        deselectAllListEntries();
                    }
                }
            } else {
                // currentSelectedRebe = null;
                btnRemoveMiPa.setEnabled(false);
            }
        } else if (source instanceof JList) {
            if (e.getClickCount() > 1) {
                final FlurstueckSchluesselCustomBean key = (FlurstueckSchluesselCustomBean)
                    lstCrossRefs.getSelectedValue();
                if (key != null) {
                    LagisBroker.getInstance().loadFlurstueck(key);
                }
            }
        }
    }

    @Override
    public void mouseEntered(final MouseEvent e) {
    }

    @Override
    public void mouseExited(final MouseEvent e) {
    }

    @Override
    public void mousePressed(final MouseEvent e) {
        mouseClicked(e);
    }

    @Override
    public void mouseReleased(final MouseEvent e) {
    }

    /**
     * DOCUMENT ME!
     *
     * @param  isEnabled  DOCUMENT ME!
     */
    private void enableSlaveComponents(final boolean isEnabled) {
        taBemerkung.setEditable(isEnabled);
        lstMerkmale.setEnabled(isEnabled);
    }

    @Override
    public void valueChanged(final ListSelectionEvent e) {
        if (log.isDebugEnabled()) {
            log.debug("SelectionChanged MiPa");
        }
        final MappingComponent mappingComp = LagisBroker.getInstance().getMappingComponent();
        final int viewIndex = tblMipa.getSelectedRow();
        if (viewIndex != -1) {
            if (isInEditMode) {
                btnRemoveMiPa.setEnabled(true);
            } else {
                btnRemoveMiPa.setEnabled(false);
            }

            final int index = ((JXTable)tblMipa).getFilters().convertRowIndexToModel(viewIndex);
            if ((index != -1) && (tblMipa.getSelectedRowCount() <= 1)) {
                final MiPa selectedMiPa = miPaModel.getMiPaAtRow(index);
                miPaModel.setCurrentSelectedMipa(selectedMiPa);
                if (selectedMiPa != null) {
                    updateCbxAuspraegung(selectedMiPa);
                    if (selectedMiPa.getGeometry() == null) {
                        if (log.isDebugEnabled()) {
                            log.debug("SetBackgroundEnabled abgeschaltet: ", new CurrentStackTrace());
                        }
                        // ((SimpleBackgroundedJPanel) this.panBackground).setBackgroundEnabled(false);
                    } else {
                        // if (!((SimpleBackgroundedJPanel) this.panBackground).isBackgroundEnabled()) {
// ((SimpleBackgroundedJPanel) this.panBackground).setBackgroundEnabled(true);
                        // }
                    }
                }
                final Collection<MipaMerkmalCustomBean> merkmale = selectedMiPa.getMiPaMerkmal();
                if (merkmale != null) {
                    for (int i = 0; i < lstMerkmale.getModel().getSize(); i++) {
                        final MerkmalCheckBox currentCheckBox = (MerkmalCheckBox)lstMerkmale.getModel().getElementAt(i);
                        if ((currentCheckBox != null) && (currentCheckBox.getMiPaMerkmal() != null)
                                    && merkmale.contains(currentCheckBox.getMiPaMerkmal())) {
                            if (log.isDebugEnabled()) {
                                log.debug("Merkmal ist in MiPa vorhanden");
                            }
                            currentCheckBox.removeItemListener(this);
                            currentCheckBox.setSelected(true);
                            currentCheckBox.addItemListener(this);
                            lstMerkmale.repaint();
                        } else {
                            if (log.isDebugEnabled()) {
                                log.debug("Merkmal ist nicht in MiPa vorhanden");
                            }
                            currentCheckBox.removeItemListener(this);
                            currentCheckBox.setSelected(false);
                            currentCheckBox.addItemListener(this);
                            lstMerkmale.repaint();
                        }
                    }
                }
                if (isInEditMode) {
                    enableSlaveComponents(isInEditMode);
                } else {
                    enableSlaveComponents(isInEditMode);
                }
                if ((selectedMiPa.getGeometry() != null)
                            && !mappingComp.getFeatureCollection().isSelected(selectedMiPa)) {
                    if (log.isDebugEnabled()) {
                        log.debug("SelectedMipa hat eine Geometry und ist nicht selektiert --> wird selektiert");
                    }
                    ignoreFeatureSelectionEvent = true;
                    mappingComp.getFeatureCollection().select(selectedMiPa);
                    ignoreFeatureSelectionEvent = false;
                } else if (selectedMiPa.getGeometry() == null) {
                    if (log.isDebugEnabled()) {
                        log.debug(
                            "Keine Mipa Geometrie vorhanden die selektiert werden kann, prüfe ob eine MiPa Geometrie selektiert ist");
                    }
                    final Collection selectedFeatures = mappingComp.getFeatureCollection().getSelectedFeatures();
                    if (selectedFeatures != null) {
                        for (final Object currentObject : selectedFeatures) {
                            if ((currentObject != null) && (currentObject instanceof MiPa)) {
                                if (log.isDebugEnabled()) {
                                    log.debug("Eine MiPa Geometrie ist selektiert --> deselekt");
                                }
                                ignoreFeatureSelectionEvent = true;
                                mappingComp.getFeatureCollection().unselect((MiPa)currentObject);
                                ignoreFeatureSelectionEvent = false;
                            }
                        }
                    } else {
                        if (log.isDebugEnabled()) {
                            log.debug("selected FeatureCollection ist leer");
                        }
                    }
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("Die Geometrie des selektierten MiPas kann nicht seleketiert werden ");
                        log.debug("alreadySelected: " + (mappingComp.getFeatureCollection().isSelected(selectedMiPa))
                                    + " hasGeometry: " + (selectedMiPa.getGeometry() != null));
                    }
                    if (log.isDebugEnabled()) {
                        log.debug("get Selected Feature: " + mappingComp.getFeatureCollection().getSelectedFeatures());
                    }
                }
            }
        } else {
            btnRemoveMiPa.setEnabled(false);
            deselectAllListEntries();
            miPaModel.clearSlaveComponents();
            enableSlaveComponents(false);
            return;
        }
        ((JXTable)tblMipa).packAll();
    }

    @Override
    public int getStatus() {
        if (tblMipa.getCellEditor() != null) {
            validationMessage = "Bitte vollenden Sie alle Änderungen bei den Vermietungen und Verpachtungen.";
            return Validatable.ERROR;
        }
        final Vector<MipaCustomBean> miPas = miPaModel.getAllMiPas();
        if ((miPas != null) || (miPas.size() > 0)) {
            for (final MiPa currentMiPa : miPas) {
                if ((currentMiPa != null)
                            && ((currentMiPa.getMiPaNutzung() == null)
                                || (currentMiPa.getMiPaNutzung().getMiPaKategorie() == null))) {
                    validationMessage = "Alle Vermietungen und Verpachtungen müssen eine Nutzung (Kategorie) enthalten";
                    return Validatable.ERROR;
                }
                if ((currentMiPa != null)
                            && ((currentMiPa.getNutzer() == null) || currentMiPa.getNutzer().equals(""))) {
                    validationMessage = "Alle Vermietungen und Verpachtungen müssen einen Nutzer besitzen.";
                    return Validatable.ERROR;
                }
                if ((currentMiPa != null) && (currentMiPa.getVertragsbeginn() == null)) {
                    validationMessage = "Alle Vermietungen und Verpachtungen müssen ein Vertragsbeginn besitzen.";
                    return Validatable.ERROR;
                }
                if ((currentMiPa != null) && (currentMiPa.getLage() == null)) {
                    validationMessage = "Alle Vermietungen und Verpachtungen müssen eine Lage besitzen.";
                    return Validatable.ERROR;
                }
                if ((currentMiPa != null) && (currentMiPa.getAktenzeichen() == null)) {
                    validationMessage = "Alle Vermietungen und Verpachtungen müssen ein Aktenzeichen besitzen.";
                    return Validatable.ERROR;
                }
                if ((currentMiPa != null) && (currentMiPa.getVertragsbeginn() != null)
                            && (currentMiPa.getVertragsende() != null)
                            && (currentMiPa.getVertragsbeginn().compareTo(currentMiPa.getVertragsende()) > 0)) {
                    validationMessage = "Das Datum des Vertragsbeginns muss vor dem Datum des Vertragsende liegen.";
                    return Validatable.ERROR;
                }
            }
        }
        return Validatable.VALID;
    }

    @Override
    public void itemStateChanged(final ItemEvent e) {
        if (log.isDebugEnabled()) {
            log.debug("Item State of MiPAMerkmal Changed " + e);
        }
        // TODO use Constants from Java
        final MerkmalCheckBox checkBox = (MerkmalCheckBox)e.getSource();
        if (tblMipa.getSelectedRow() != -1) {
            final MiPa miPa = miPaModel.getMiPaAtRow(((JXTable)tblMipa).getFilters().convertRowIndexToModel(
                        tblMipa.getSelectedRow()));
            if (miPa != null) {
                Collection<MipaMerkmalCustomBean> merkmale = miPa.getMiPaMerkmal();
                if (merkmale == null) {
                    log.info("neues Hibernateset für Merkmale angelegt");
                    merkmale = new HashSet<MipaMerkmalCustomBean>();
                }

                if (e.getStateChange() == 1) {
                    if (log.isDebugEnabled()) {
                        log.debug("Checkbox wurde selektiert --> füge es zum Set hinzu");
                    }
                    merkmale.add(checkBox.getMiPaMerkmal());
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("Checkbox wurde deselektiert --> lösche es aus Set");
                    }
                    merkmale.remove(checkBox.getMiPaMerkmal());
                }
            } else {
                log.warn(
                    "Kann merkmalsänderung nicht speichern da kein Eintrag unter diesem Index im Modell vorhanden ist");
            }
        } else {
            log.warn("Kann merkmalsänderung nicht speichern da kein Eintrag selektiert ist");
        }
    }

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   mipa  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String getIdentifierString(final MiPa mipa) {
        final String idValue1 = mipa.getLage();
        final MiPaNutzung idValue2 = mipa.getMiPaNutzung();

        final StringBuffer identifier = new StringBuffer();

        if (idValue1 != null) {
            identifier.append(idValue1);
        } else {
            identifier.append("keine Lage");
        }

        if ((idValue2 != null) && (idValue2.getMiPaKategorie() != null)) {
            identifier.append(GeometrySlotInformation.SLOT_IDENTIFIER_SEPARATOR + idValue2.getMiPaKategorie());
        } else {
            identifier.append(GeometrySlotInformation.SLOT_IDENTIFIER_SEPARATOR + "keine Nutzung");
        }

        if ((idValue2 != null)
                    && ((idValue2.getAusgewaehlteNummer() != null) || (idValue2.getAusgewaehlteAuspraegung() != null))) {
            if (idValue2.getAusgewaehlteNummer() != null) {
                identifier.append(GeometrySlotInformation.SLOT_IDENTIFIER_SEPARATOR + "Nr. "
                            + idValue2.getAusgewaehlteNummer());
            } else {
                identifier.append(GeometrySlotInformation.SLOT_IDENTIFIER_SEPARATOR
                            + idValue2.getAusgewaehlteAuspraegung());
            }
        } else {
            identifier.append(GeometrySlotInformation.SLOT_IDENTIFIER_SEPARATOR + "keine Ausprägung");
        }

        return identifier.toString();
    }

    @Override
    public Vector<GeometrySlotInformation> getSlotInformation() {
        // VerwaltungsTableModel tmp = (VerwaltungsTableModel) tNutzung.getModel();
        final Vector<GeometrySlotInformation> result = new Vector<GeometrySlotInformation>();
        if (isWidgetReadOnly()) {
            return result;
        } else {
            final int rowCount = miPaModel.getRowCount();
            for (int i = 0; i < rowCount; i++) {
                final MiPa currentMiPa = miPaModel.getMiPaAtRow(i);
                // Geom geom;
                if (currentMiPa.getGeometry() == null) {
                    result.add(new GeometrySlotInformation(
                            getProviderName(),
                            this.getIdentifierString(currentMiPa),
                            currentMiPa,
                            this));
                }
            }
            return result;
        }
    }

    // TODO multiple Selection
    // HINT If there are problems try to remove/add Listselectionlistener at start/end of Method
    @Override
    public void featureSelectionChanged(final Collection<Feature> features) {
        if (log.isDebugEnabled()) {
            log.debug("FeatureSelectionChanged", new CurrentStackTrace());
        }

        if (features.isEmpty()) {
            return;
        }
        tblMipa.getSelectionModel().removeListSelectionListener(this);
        Feature wrappedFeature;
        for (final Feature feature : features) {
            if (feature instanceof StyledFeatureGroupWrapper) {
                wrappedFeature = ((StyledFeatureGroupWrapper)feature).getFeature();
            } else {
                wrappedFeature = feature;
            }
            if (wrappedFeature instanceof MipaCustomBean) {
                // TODO Refactor Name
                final int index = miPaModel.getIndexOfMiPa((MipaCustomBean)wrappedFeature);
                final int displayedIndex = ((JXTable)tblMipa).getFilters().convertRowIndexToView(index);
                if ((index != -1)
                            && LagisBroker.getInstance().getMappingComponent().getFeatureCollection().isSelected(
                                feature)) {
                    tblMipa.getSelectionModel().addSelectionInterval(displayedIndex, displayedIndex);
                    final Rectangle tmp = tblMipa.getCellRect(displayedIndex, 0, true);
                    if (tmp != null) {
                        tblMipa.scrollRectToVisible(tmp);
                    }
                } else {
                    tblMipa.getSelectionModel().removeSelectionInterval(displayedIndex, displayedIndex);
                }
            }
        }
        tblMipa.getSelectionModel().addListSelectionListener(this);
    }

    @Override
    public void stateChanged(final ChangeEvent e) {
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        jPanel2 = new javax.swing.JPanel();
        jTabbedPane2 = new javax.swing.JTabbedPane();
        panMiPaBordered = new javax.swing.JPanel();
        cpMiPa = new javax.swing.JScrollPane();
        tblMipa = new JXTable();
        btnAddMiPa = new javax.swing.JButton();
        btnRemoveMiPa = new javax.swing.JButton();
        btnAddExitingMiPa = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        panBackground = new javax.swing.JPanel();
        panQuerverweise = new javax.swing.JPanel();
        panQuerverweiseTitled = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        lstCrossRefs = new javax.swing.JList();
        jLabel2 = new javax.swing.JLabel();
        panMerkmale = new javax.swing.JPanel();
        panMerkmaleTitled = new javax.swing.JPanel();
        spMerkmale = new javax.swing.JScrollPane();
        lstMerkmale = new JCheckBoxList();
        jLabel1 = new javax.swing.JLabel();
        panBemerkung = new javax.swing.JPanel();
        panBemerkungTitled = new javax.swing.JPanel();
        spBemerkung = new javax.swing.JScrollPane();
        taBemerkung = new javax.swing.JTextArea();
        jLabel3 = new javax.swing.JLabel();

        final javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                jPanel2Layout.createSequentialGroup().addContainerGap().addComponent(
                    jTabbedPane2,
                    javax.swing.GroupLayout.DEFAULT_SIZE,
                    148,
                    Short.MAX_VALUE).addContainerGap()));
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                jPanel2Layout.createSequentialGroup().addContainerGap().addComponent(
                    jTabbedPane2,
                    javax.swing.GroupLayout.DEFAULT_SIZE,
                    126,
                    Short.MAX_VALUE).addContainerGap()));

        panMiPaBordered.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        cpMiPa.setBorder(null);

        tblMipa.setModel(new javax.swing.table.DefaultTableModel(
                new Object[][] {
                    { null, null, null, null, null, null, null },
                    { null, null, null, null, null, null, null },
                    { null, null, null, null, null, null, null },
                    { null, null, null, null, null, null, null },
                    { null, null, null, null, null, null, null },
                    { null, null, null, null, null, null, null },
                    { null, null, null, null, null, null, null },
                    { null, null, null, null, null, null, null },
                    { null, null, null, null, null, null, null },
                    { null, null, null, null, null, null, null },
                    { null, null, null, null, null, null, null },
                    { null, null, null, null, null, null, null }
                },
                new String[] { "Nummer", "Lage", "Fläche m²", "Nutzung", "Nutzer", "Vertragsbeginn", "Vertragsende" }));
        cpMiPa.setViewportView(tblMipa);

        btnAddMiPa.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/lagis/ressource/icons/buttons/add.png"))); // NOI18N
        btnAddMiPa.setBorder(null);
        btnAddMiPa.setOpaque(false);
        btnAddMiPa.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    btnAddMiPaActionPerformed(evt);
                }
            });

        btnRemoveMiPa.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/lagis/ressource/icons/buttons/remove.png"))); // NOI18N
        btnRemoveMiPa.setBorder(null);
        btnRemoveMiPa.setOpaque(false);
        btnRemoveMiPa.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    btnRemoveMiPaActionPerformed(evt);
                }
            });

        btnAddExitingMiPa.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/lagis/ressource/icons/toolbar/contract.png"))); // NOI18N
        btnAddExitingMiPa.setBorder(null);
        btnAddExitingMiPa.setOpaque(false);
        btnAddExitingMiPa.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    btnAddExitingMiPaActionPerformed(evt);
                }
            });

        final javax.swing.GroupLayout panMiPaBorderedLayout = new javax.swing.GroupLayout(panMiPaBordered);
        panMiPaBordered.setLayout(panMiPaBorderedLayout);
        panMiPaBorderedLayout.setHorizontalGroup(
            panMiPaBorderedLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                panMiPaBorderedLayout.createSequentialGroup().addContainerGap().addGroup(
                    panMiPaBorderedLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                        javax.swing.GroupLayout.Alignment.TRAILING,
                        panMiPaBorderedLayout.createSequentialGroup().addComponent(
                            btnAddExitingMiPa,
                            javax.swing.GroupLayout.PREFERRED_SIZE,
                            29,
                            javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(
                            javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(
                            btnAddMiPa,
                            javax.swing.GroupLayout.PREFERRED_SIZE,
                            31,
                            javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(
                            javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(
                            btnRemoveMiPa,
                            javax.swing.GroupLayout.PREFERRED_SIZE,
                            15,
                            javax.swing.GroupLayout.PREFERRED_SIZE)).addComponent(
                        cpMiPa,
                        javax.swing.GroupLayout.DEFAULT_SIZE,
                        696,
                        Short.MAX_VALUE)).addContainerGap()));

        panMiPaBorderedLayout.linkSize(
            javax.swing.SwingConstants.HORIZONTAL,
            new java.awt.Component[] { btnAddExitingMiPa, btnAddMiPa, btnRemoveMiPa });

        panMiPaBorderedLayout.setVerticalGroup(
            panMiPaBorderedLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                panMiPaBorderedLayout.createSequentialGroup().addContainerGap().addGroup(
                    panMiPaBorderedLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(
                        btnRemoveMiPa,
                        javax.swing.GroupLayout.PREFERRED_SIZE,
                        23,
                        javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(
                        btnAddMiPa,
                        javax.swing.GroupLayout.PREFERRED_SIZE,
                        28,
                        javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(
                        btnAddExitingMiPa,
                        javax.swing.GroupLayout.PREFERRED_SIZE,
                        0,
                        Short.MAX_VALUE)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(cpMiPa, javax.swing.GroupLayout.DEFAULT_SIZE, 299, Short.MAX_VALUE)
                            .addContainerGap()));

        panMiPaBorderedLayout.linkSize(
            javax.swing.SwingConstants.VERTICAL,
            new java.awt.Component[] { btnAddExitingMiPa, btnAddMiPa, btnRemoveMiPa });

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        panQuerverweise.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        panQuerverweise.setOpaque(false);

        panQuerverweiseTitled.setOpaque(false);

        jScrollPane1.setBorder(null);
        jScrollPane1.setOpaque(false);

        lstCrossRefs.setBackground(javax.swing.UIManager.getDefaults().getColor("Label.background"));
        lstCrossRefs.setOpaque(false);
        jScrollPane1.setViewportView(lstCrossRefs);

        final javax.swing.GroupLayout panQuerverweiseTitledLayout = new javax.swing.GroupLayout(panQuerverweiseTitled);
        panQuerverweiseTitled.setLayout(panQuerverweiseTitledLayout);
        panQuerverweiseTitledLayout.setHorizontalGroup(
            panQuerverweiseTitledLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(
                jScrollPane1,
                javax.swing.GroupLayout.DEFAULT_SIZE,
                187,
                Short.MAX_VALUE));
        panQuerverweiseTitledLayout.setVerticalGroup(
            panQuerverweiseTitledLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(
                jScrollPane1,
                javax.swing.GroupLayout.DEFAULT_SIZE,
                164,
                Short.MAX_VALUE));

        jLabel2.setText("Querverweise:");

        final javax.swing.GroupLayout panQuerverweiseLayout = new javax.swing.GroupLayout(panQuerverweise);
        panQuerverweise.setLayout(panQuerverweiseLayout);
        panQuerverweiseLayout.setHorizontalGroup(
            panQuerverweiseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                panQuerverweiseLayout.createSequentialGroup().addContainerGap().addGroup(
                    panQuerverweiseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(
                        jLabel2).addComponent(
                        panQuerverweiseTitled,
                        javax.swing.GroupLayout.DEFAULT_SIZE,
                        javax.swing.GroupLayout.DEFAULT_SIZE,
                        Short.MAX_VALUE)).addContainerGap()));
        panQuerverweiseLayout.setVerticalGroup(
            panQuerverweiseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                panQuerverweiseLayout.createSequentialGroup().addContainerGap().addComponent(jLabel2).addPreferredGap(
                    javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(
                    panQuerverweiseTitled,
                    javax.swing.GroupLayout.DEFAULT_SIZE,
                    javax.swing.GroupLayout.DEFAULT_SIZE,
                    Short.MAX_VALUE).addContainerGap()));

        panMerkmale.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        panMerkmale.setOpaque(false);

        panMerkmaleTitled.setOpaque(false);

        spMerkmale.setBorder(null);
        spMerkmale.setOpaque(false);

        lstMerkmale.setBackground(javax.swing.UIManager.getDefaults().getColor("Label.background"));
        lstMerkmale.setOpaque(false);
        spMerkmale.setViewportView(lstMerkmale);

        final javax.swing.GroupLayout panMerkmaleTitledLayout = new javax.swing.GroupLayout(panMerkmaleTitled);
        panMerkmaleTitled.setLayout(panMerkmaleTitledLayout);
        panMerkmaleTitledLayout.setHorizontalGroup(
            panMerkmaleTitledLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(
                spMerkmale,
                javax.swing.GroupLayout.DEFAULT_SIZE,
                171,
                Short.MAX_VALUE));
        panMerkmaleTitledLayout.setVerticalGroup(
            panMerkmaleTitledLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(
                spMerkmale,
                javax.swing.GroupLayout.DEFAULT_SIZE,
                164,
                Short.MAX_VALUE));

        jLabel1.setText("Merkmale:");

        final javax.swing.GroupLayout panMerkmaleLayout = new javax.swing.GroupLayout(panMerkmale);
        panMerkmale.setLayout(panMerkmaleLayout);
        panMerkmaleLayout.setHorizontalGroup(
            panMerkmaleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                panMerkmaleLayout.createSequentialGroup().addContainerGap().addGroup(
                    panMerkmaleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(
                        panMerkmaleTitled,
                        javax.swing.GroupLayout.DEFAULT_SIZE,
                        javax.swing.GroupLayout.DEFAULT_SIZE,
                        Short.MAX_VALUE).addComponent(jLabel1)).addContainerGap()));
        panMerkmaleLayout.setVerticalGroup(
            panMerkmaleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                panMerkmaleLayout.createSequentialGroup().addContainerGap().addComponent(jLabel1).addPreferredGap(
                    javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(
                    panMerkmaleTitled,
                    javax.swing.GroupLayout.DEFAULT_SIZE,
                    javax.swing.GroupLayout.DEFAULT_SIZE,
                    Short.MAX_VALUE).addContainerGap()));

        panBemerkung.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        panBemerkung.setOpaque(false);

        panBemerkungTitled.setOpaque(false);

        spBemerkung.setBorder(null);
        spBemerkung.setOpaque(false);

        taBemerkung.setColumns(20);
        taBemerkung.setLineWrap(true);
        taBemerkung.setRows(5);
        taBemerkung.setWrapStyleWord(true);
        taBemerkung.setOpaque(false);
        spBemerkung.setViewportView(taBemerkung);

        final javax.swing.GroupLayout panBemerkungTitledLayout = new javax.swing.GroupLayout(panBemerkungTitled);
        panBemerkungTitled.setLayout(panBemerkungTitledLayout);
        panBemerkungTitledLayout.setHorizontalGroup(
            panBemerkungTitledLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(
                spBemerkung,
                javax.swing.GroupLayout.DEFAULT_SIZE,
                254,
                Short.MAX_VALUE));
        panBemerkungTitledLayout.setVerticalGroup(
            panBemerkungTitledLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(
                spBemerkung,
                javax.swing.GroupLayout.DEFAULT_SIZE,
                164,
                Short.MAX_VALUE));

        jLabel3.setText("Bemerkung");

        final javax.swing.GroupLayout panBemerkungLayout = new javax.swing.GroupLayout(panBemerkung);
        panBemerkung.setLayout(panBemerkungLayout);
        panBemerkungLayout.setHorizontalGroup(
            panBemerkungLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                panBemerkungLayout.createSequentialGroup().addContainerGap().addGroup(
                    panBemerkungLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(
                        panBemerkungTitled,
                        javax.swing.GroupLayout.DEFAULT_SIZE,
                        javax.swing.GroupLayout.DEFAULT_SIZE,
                        Short.MAX_VALUE).addComponent(jLabel3)).addContainerGap()));
        panBemerkungLayout.setVerticalGroup(
            panBemerkungLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                panBemerkungLayout.createSequentialGroup().addContainerGap().addComponent(jLabel3).addPreferredGap(
                    javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(
                    panBemerkungTitled,
                    javax.swing.GroupLayout.DEFAULT_SIZE,
                    javax.swing.GroupLayout.DEFAULT_SIZE,
                    Short.MAX_VALUE).addContainerGap()));

        final javax.swing.GroupLayout panBackgroundLayout = new javax.swing.GroupLayout(panBackground);
        panBackground.setLayout(panBackgroundLayout);
        panBackgroundLayout.setHorizontalGroup(
            panBackgroundLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                panBackgroundLayout.createSequentialGroup().addComponent(
                    panMerkmale,
                    javax.swing.GroupLayout.PREFERRED_SIZE,
                    javax.swing.GroupLayout.DEFAULT_SIZE,
                    javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(
                    javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(
                    panQuerverweise,
                    javax.swing.GroupLayout.PREFERRED_SIZE,
                    javax.swing.GroupLayout.DEFAULT_SIZE,
                    javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(
                    javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(
                    panBemerkung,
                    javax.swing.GroupLayout.DEFAULT_SIZE,
                    javax.swing.GroupLayout.DEFAULT_SIZE,
                    Short.MAX_VALUE)));
        panBackgroundLayout.setVerticalGroup(
            panBackgroundLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(
                panBemerkung,
                javax.swing.GroupLayout.DEFAULT_SIZE,
                javax.swing.GroupLayout.DEFAULT_SIZE,
                Short.MAX_VALUE).addComponent(
                panMerkmale,
                javax.swing.GroupLayout.DEFAULT_SIZE,
                javax.swing.GroupLayout.DEFAULT_SIZE,
                Short.MAX_VALUE).addComponent(
                panQuerverweise,
                javax.swing.GroupLayout.DEFAULT_SIZE,
                javax.swing.GroupLayout.DEFAULT_SIZE,
                Short.MAX_VALUE));

        final javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                jPanel1Layout.createSequentialGroup().addContainerGap().addComponent(
                    panBackground,
                    javax.swing.GroupLayout.DEFAULT_SIZE,
                    javax.swing.GroupLayout.DEFAULT_SIZE,
                    Short.MAX_VALUE).addContainerGap()));
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                jPanel1Layout.createSequentialGroup().addContainerGap().addComponent(
                    panBackground,
                    javax.swing.GroupLayout.DEFAULT_SIZE,
                    javax.swing.GroupLayout.DEFAULT_SIZE,
                    Short.MAX_VALUE).addContainerGap()));

        final javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                javax.swing.GroupLayout.Alignment.TRAILING,
                layout.createSequentialGroup().addContainerGap().addGroup(
                    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING).addComponent(
                        panMiPaBordered,
                        javax.swing.GroupLayout.Alignment.LEADING,
                        javax.swing.GroupLayout.DEFAULT_SIZE,
                        javax.swing.GroupLayout.DEFAULT_SIZE,
                        Short.MAX_VALUE).addComponent(
                        jPanel1,
                        javax.swing.GroupLayout.Alignment.LEADING,
                        javax.swing.GroupLayout.DEFAULT_SIZE,
                        javax.swing.GroupLayout.DEFAULT_SIZE,
                        Short.MAX_VALUE)).addContainerGap()));
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                javax.swing.GroupLayout.Alignment.TRAILING,
                layout.createSequentialGroup().addContainerGap().addComponent(
                    panMiPaBordered,
                    javax.swing.GroupLayout.DEFAULT_SIZE,
                    javax.swing.GroupLayout.DEFAULT_SIZE,
                    Short.MAX_VALUE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(
                    jPanel1,
                    javax.swing.GroupLayout.DEFAULT_SIZE,
                    javax.swing.GroupLayout.DEFAULT_SIZE,
                    Short.MAX_VALUE).addContainerGap()));
    } // </editor-fold>//GEN-END:initComponents
    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void btnAddMiPaActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddMiPaActionPerformed
        final MipaCustomBean tmpMiPa = MipaCustomBean.createNew();
        miPaModel.addMiPa(tmpMiPa);
        miPaModel.fireTableDataChanged();
    }//GEN-LAST:event_btnAddMiPaActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void btnRemoveMiPaActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveMiPaActionPerformed
        final int currentRow = tblMipa.getSelectedRow();
        if (currentRow != -1) {
            // VerwaltungsTableModel currentModel = (VerwaltungsTableModel)tNutzung.getModel();
            miPaModel.removeMiPa(((JXTable)tblMipa).getFilters().convertRowIndexToModel(currentRow));
            miPaModel.fireTableDataChanged();
            updateCrossRefs();
            enableSlaveComponents(false);
            deselectAllListEntries();
            if (log.isDebugEnabled()) {
                log.debug("liste ausgeschaltet");
            }
        }
    }//GEN-LAST:event_btnRemoveMiPaActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void btnAddExitingMiPaActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddExitingMiPaActionPerformed
        final JDialog dialog = new JDialog(LagisBroker.getInstance().getParentComponent(), "", true);
        dialog.add(new AddExistingMiPaPanel(currentFlurstueck, miPaModel, lstCrossRefs.getModel()));
        dialog.pack();
        dialog.setIconImage(icoExistingContract.getImage());
        dialog.setTitle("Vorhandener Vertrag hinzufügen...");
        StaticSwingTools.showDialog(dialog);
    }//GEN-LAST:event_btnAddExitingMiPaActionPerformed

    /**
     * DOCUMENT ME!
     */
    private void updateCrossRefs() {
        if (log.isDebugEnabled()) {
            log.debug("Update der Querverweise");
        }
        final Collection<FlurstueckSchluesselCustomBean> crossRefs = CidsBroker.getInstance()
                    .getCrossreferencesForMiPas(new HashSet(miPaModel.getAllMiPas()));
        final DefaultUniqueListModel newModel = new DefaultUniqueListModel();
        if (crossRefs != null) {
            if (log.isDebugEnabled()) {
                log.debug("Es sind Querverweise auf MiPas vorhanden");
            }
            currentFlurstueck.setVertraegeQuerverweise(crossRefs);
            final Iterator<FlurstueckSchluesselCustomBean> it = crossRefs.iterator();
            while (it.hasNext()) {
                if (log.isDebugEnabled()) {
                    log.debug("Ein Querverweis hinzugefügt");
                }
                newModel.addElement(it.next());
            }
            newModel.removeElement(currentFlurstueck.getFlurstueckSchluessel());
        }
        lstCrossRefs.setModel(newModel);
    }

    /**
     * DOCUMENT ME!
     */
    private void updateWidgetUi() {
        tblMipa.repaint();
        lstCrossRefs.repaint();
        lstMerkmale.repaint();
    }

    @Override
    public void allFeaturesRemoved(final FeatureCollectionEvent fce) {
        updateWidgetUi();
    }

    @Override
    public void featureCollectionChanged() {
        updateWidgetUi();
    }

    @Override
    public void featureReconsiderationRequested(final FeatureCollectionEvent fce) {
        updateWidgetUi();
    }

    @Override
    public void featureSelectionChanged(final FeatureCollectionEvent fce) {
        updateWidgetUi();
    }

    @Override
    public void featuresAdded(final FeatureCollectionEvent fce) {
        updateWidgetUi();
    }

    @Override
    public void featuresChanged(final FeatureCollectionEvent fce) {
        updateWidgetUi();
    }

    @Override
    public void featuresRemoved(final FeatureCollectionEvent fce) {
        updateWidgetUi();
    }

    @Override
    public void tableChanged(final TableModelEvent e) {
        ((JXTable)tblMipa).packAll();
    }

    @Override
    public List<BasicEntity> getCopyData() {
        final Vector<MipaCustomBean> allMiPas = this.miPaModel.getAllMiPas();
        final ArrayList<BasicEntity> result = new ArrayList<BasicEntity>(allMiPas.size());

        MipaCustomBean tmp;
        for (final MipaCustomBean mipa : allMiPas) {
            tmp = MipaCustomBean.createNew();

            tmp.setAktenzeichen(mipa.getAktenzeichen());
            tmp.setBemerkung(mipa.getBemerkung());
            tmp.setCanBeSelected(mipa.canBeSelected());
            tmp.setEditable(mipa.isEditable());
            tmp.setFillingPaint(mipa.getFillingPaint());
            tmp.setFlaeche(mipa.getFlaeche());

            final Geometry geom = mipa.getGeometry();
            if (geom != null) {
                tmp.setGeometry((Geometry)geom.clone());
            }

            tmp.setHighlightingEnabled(mipa.isHighlightingEnabled());
            tmp.setLage(mipa.getLage());
            tmp.setLinePaint(mipa.getLinePaint());
            tmp.setLineWidth(mipa.getLineWidth());
            tmp.setMiPaMerkmal(mipa.getMiPaMerkmal());
            tmp.setMiPaNutzung(mipa.getMiPaNutzung());
            tmp.setModifiable(mipa.isModifiable());
            tmp.setNutzer(mipa.getNutzer());
            tmp.setNutzung(mipa.getNutzung());
            tmp.setPointAnnotationSymbol(mipa.getPointAnnotationSymbol());
            tmp.setTransparency(mipa.getTransparency());
            tmp.setVertragsbeginn(mipa.getVertragsbeginn());
            tmp.setVertragsende(mipa.getVertragsende());
            tmp.hide(mipa.isHidden());

            result.add(tmp);
        }

        return result;
    }

    @Override
    public void paste(final BasicEntity item) {
        if (item == null) {
            throw new NullPointerException("Given data item must not be null");
        }

        if (item instanceof MiPa) {
            final Collection<MipaCustomBean> residentMiPas = this.miPaModel.getAllMiPas();

            if (residentMiPas.contains(item)) {
                log.warn("MiPa " + item + " does already exist in Flurstück " + this.currentFlurstueck);
            } else {
                this.miPaModel.addMiPa((MipaCustomBean)item);

                final MappingComponent mc = LagisBroker.getInstance().getMappingComponent();
                final Feature f = new StyledFeatureGroupWrapper((StyledFeature)item, PROVIDER_NAME, PROVIDER_NAME);
                mc.getFeatureCollection().addFeature(f);
                mc.setGroupLayerVisibility(PROVIDER_NAME, true);

                this.miPaModel.fireTableDataChanged();
            }
        }
    }

    @Override
    public void pasteAll(final List<BasicEntity> dataList) {
        if (dataList == null) {
            throw new NullPointerException("Given list of MiPa items must not be null");
        }

        if (dataList.isEmpty()) {
            return;
        }

        final Vector<MipaCustomBean> residentMiPas = this.miPaModel.getAllMiPas();
        final int rowCountBefore = this.miPaModel.getRowCount();

        Feature f;
        final MappingComponent mc = LagisBroker.getInstance().getMappingComponent();
        final FeatureCollection featCollection = mc.getFeatureCollection();

        for (final BasicEntity entity : dataList) {
            if (entity instanceof MiPa) {
                if (residentMiPas.contains(entity)) {
                    log.warn("Verwaltungsbereich " + entity + " does already exist in Flurstück "
                                + this.currentFlurstueck);
                } else {
                    this.miPaModel.addMiPa((MipaCustomBean)entity);
                    f = new StyledFeatureGroupWrapper((StyledFeature)entity, PROVIDER_NAME, PROVIDER_NAME);
                    featCollection.addFeature(f);
                }
            }
        }

        if (rowCountBefore == this.miPaModel.getRowCount()) {
            log.warn("No MiPa items were added from input list " + dataList);
        } else {
            this.miPaModel.fireTableDataChanged();
            mc.setGroupLayerVisibility(PROVIDER_NAME, true);
        }
    }

    @Override
    public String getDisplayName(final BasicEntity entity) {
        if (entity instanceof MiPa) {
            final MiPa mipa = (MiPa)entity;

            return this.getProviderName()
                        + GeometrySlotInformation.SLOT_IDENTIFIER_SEPARATOR
                        + this.getIdentifierString(mipa);
        }

        return Copyable.UNKNOWN_ENTITY;
    }

    @Override
    public Icon getDisplayIcon() {
        return this.copyDisplayIcon;
    }

    @Override
    public boolean knowsDisplayName(final BasicEntity entity) {
        return entity instanceof MiPa;
    }
    @Override
    public boolean isFeatureSelectionChangedEnabled() {
        return listenerEnabled;
    }

    @Override
    public void setFeatureSelectionChangedEnabled(final boolean listenerEnabled) {
        this.listenerEnabled = listenerEnabled;
    }
}
