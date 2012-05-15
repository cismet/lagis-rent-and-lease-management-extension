/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.lagis.ressort.mipa;

import org.apache.log4j.Logger;

import org.jdesktop.swingx.JXTable;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import java.util.*;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.text.BadLocationException;

import de.cismet.cids.custom.beans.lagis.MipaCustomBean;
import de.cismet.cids.custom.beans.lagis.MipaKategorieAuspraegungCustomBean;
import de.cismet.cids.custom.beans.lagis.MipaKategorieCustomBean;
import de.cismet.cids.custom.beans.lagis.MipaNutzungCustomBean;

import de.cismet.cismap.commons.features.Feature;

import de.cismet.lagis.broker.LagisBroker;

import de.cismet.lagis.models.documents.SimpleDocumentModel;

import de.cismet.lagisEE.entity.extension.vermietung.MiPa;
import de.cismet.lagisEE.entity.extension.vermietung.MiPaKategorie;
import de.cismet.lagisEE.entity.extension.vermietung.MiPaKategorieAuspraegung;
import de.cismet.lagisEE.entity.extension.vermietung.MiPaNutzung;

/**
 * DOCUMENT ME!
 *
 * @author   Sebastian Puhl
 * @version  $Revision$, $Date$
 */
public class MiPaModel extends AbstractTableModel {

    //~ Static fields/initializers ---------------------------------------------

    private static final String[] COLUMN_HEADER = {
            "Lage",
            "Aktenzeichen",
            "Fläche m²",
            "Alte Nutzung",
            "Nutzung",
            "Ausprägung",
            "Nutzer",
            "Vertragsbeginn",
            "Vertragsende",
        };
    public static final int LAGE_COLUMN = 0;
    public static final int AKTENZEICHEN_COLUMN = 1;
    public static final int FLAECHE_COLUMN = 2;
    public static final int ALTE_NUTZUNG_COLUMN = 3;
    public static final int NUTZUNG_COLUMN = 4;
    public static final int AUSPRAEGUNG_COLUMN = 5;
    public static final int NUTZER_COLUMN = 6;
    public static final int VERTRAGS_BEGINN_COLUMN = 7;
    public static final int VERTRAGS_ENDE_COLUMN = 8;

    //~ Instance fields --------------------------------------------------------

    Vector<MipaCustomBean> miPas;

    private final Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private boolean isInEditMode = false;
    private SimpleDocumentModel bemerkungDocumentModel;
    private MiPa currentSelectedMiPa = null;
    private DefaultListModel miPaMerkmalsModel;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new MiPaModel object.
     */
    public MiPaModel() {
        miPas = new Vector<MipaCustomBean>();
        initDocumentModels();
        miPaMerkmalsModel = new DefaultListModel();
    }

    /**
     * Creates a new MiPaModel object.
     *
     * @param  miPas  DOCUMENT ME!
     */
    public MiPaModel(final Collection<MipaCustomBean> miPas) {
        try {
            this.miPas = new Vector<MipaCustomBean>(miPas);
        } catch (Exception ex) {
            log.error("Fehler beim anlegen des Models", ex);
            this.miPas = new Vector<MipaCustomBean>();
        }
        initDocumentModels();
        miPaMerkmalsModel = new DefaultListModel();
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public int getColumnCount() {
        return COLUMN_HEADER.length;
    }

    @Override
    public int getRowCount() {
        return miPas.size();
    }

    @Override
    public String getColumnName(final int column) {
        return COLUMN_HEADER[column];
    }

    @Override
    public Object getValueAt(final int rowIndex, final int columnIndex) {
        try {
            if (rowIndex >= miPas.size()) {
                log.warn("Cannot access row " + rowIndex + ". There are just " + miPas.size() + " rows");
                return null;
            }

            final MiPa value = miPas.get(rowIndex);

            switch (columnIndex) {
                case LAGE_COLUMN: {
                    return value.getLage();
                }
                case AKTENZEICHEN_COLUMN: {
                    return value.getAktenzeichen();
                }
                case FLAECHE_COLUMN: {
                    if (value.getFlaeche() != null) {
                        return value.getFlaeche().intValue();
                    } else {
                        return null;
                    }
                }
                case ALTE_NUTZUNG_COLUMN: {
                    return value.getNutzung();
                }
                case NUTZUNG_COLUMN: {
                    if (value.getMiPaNutzung() != null) {
                        return value.getMiPaNutzung().getMiPaKategorie();
                    } else {
                        return null;
                    }
                }
                case AUSPRAEGUNG_COLUMN: {
                    if ((value.getMiPaNutzung() != null) && (value.getMiPaNutzung().getMiPaKategorie() != null)
                                && value.getMiPaNutzung().getMiPaKategorie().getHatNummerAlsAuspraegung()) {
                        if (value.getMiPaNutzung().getAusgewaehlteNummer() != null) {
                            return "Nr. " + value.getMiPaNutzung().getAusgewaehlteNummer();
                        } else {
                            return "";
                        }
                    } else if ((value.getMiPaNutzung() != null)
                                && (value.getMiPaNutzung().getMiPaKategorie() != null)) {
                        return value.getMiPaNutzung().getAusgewaehlteAuspraegung();
                    } else {
                        return null;
                    }
                }
                case NUTZER_COLUMN: {
                    return value.getNutzer();
                }
                case VERTRAGS_BEGINN_COLUMN: {
                    return value.getVertragsbeginn();
                }
                case VERTRAGS_ENDE_COLUMN: {
                    return value.getVertragsende();
                }
                default: {
                    return "Spalte ist nicht definiert";
                }
            }
        } catch (Exception ex) {
            log.error("Fehler beim abrufen von Daten aus dem Modell: Zeile: " + rowIndex + " Spalte" + columnIndex, ex);
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  miPa  DOCUMENT ME!
     */
    public void addMiPa(final MipaCustomBean miPa) {
        miPas.add(miPa);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   rowIndex  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public MipaCustomBean getMiPaAtRow(final int rowIndex) {
        return miPas.get(rowIndex);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  rowIndex  DOCUMENT ME!
     */
    public void removeMiPa(final int rowIndex) {
        final MiPa miPa = miPas.get(rowIndex);
        if ((miPa != null) && (miPa.getGeometry() != null)) {
            LagisBroker.getInstance().getMappingComponent().getFeatureCollection().removeFeature(miPa);
        }
        miPas.remove(rowIndex);
    }

    @Override
    public boolean isCellEditable(final int rowIndex, final int columnIndex) {
        if ((COLUMN_HEADER.length > columnIndex) && (miPas.size() > rowIndex) && isInEditMode
                    && (columnIndex != ALTE_NUTZUNG_COLUMN)) {
            if (columnIndex == AUSPRAEGUNG_COLUMN) {
                final MiPa currentMiPa = getMiPaAtRow(rowIndex);
                if ((currentMiPa != null) && (currentMiPa.getMiPaNutzung() != null)
                            && (currentMiPa.getMiPaNutzung().getMiPaKategorie() != null)
                            && ((currentMiPa.getMiPaNutzung().getMiPaKategorie().getHatNummerAlsAuspraegung())
                                || ((currentMiPa.getMiPaNutzung().getMiPaKategorie().getKategorieAuspraegungen()
                                        != null)
                                    && (currentMiPa.getMiPaNutzung().getMiPaKategorie().getKategorieAuspraegungen()
                                        .size() > 0)))) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  isEditable  DOCUMENT ME!
     */
    public void setIsInEditMode(final boolean isEditable) {
        isInEditMode = isEditable;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Vector<Feature> getAllMiPaFeatures() {
        final Vector<Feature> tmp = new Vector<Feature>();
        if (miPas != null) {
            final Iterator<MipaCustomBean> it = miPas.iterator();
            while (it.hasNext()) {
                final MiPa curMiPa = it.next();
                if (curMiPa.getGeometry() != null) {
                    tmp.add(curMiPa);
                }
            }
            return tmp;
        } else {
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  miPas  DOCUMENT ME!
     */
    public void refreshTableModel(final Collection<MipaCustomBean> miPas) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Refresh des MiPaTableModell");
            }
            this.miPas = new Vector<MipaCustomBean>(miPas);
        } catch (Exception ex) {
            log.error("Fehler beim refreshen des Models", ex);
            this.miPas = new Vector<MipaCustomBean>();
        }
        fireTableDataChanged();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Vector<MipaCustomBean> getAllMiPas() {
        return miPas;
    }

    @Override
    public void setValueAt(final Object aValue, final int rowIndex, final int columnIndex) {
        try {
            final MiPa value = miPas.get(rowIndex);
            switch (columnIndex) {
                case LAGE_COLUMN: {
                    value.setLage((String)aValue);
                    break;
                }
                case AKTENZEICHEN_COLUMN: {
                    value.setAktenzeichen((String)aValue);
                    break;
                }
                case FLAECHE_COLUMN: {
                    if (aValue != null) {
                        value.setFlaeche(((Integer)aValue).doubleValue());
                    } else {
                        value.setFlaeche(null);
                    }
                    break;
                }
                case ALTE_NUTZUNG_COLUMN: {
                    value.setNutzung((String)aValue);
                    break;
                }
                case NUTZUNG_COLUMN: {
                    if (value.getMiPaNutzung() == null) {
                        value.setMiPaNutzung(MipaNutzungCustomBean.createNew());
                        value.getMiPaNutzung().setMiPaKategorie((MipaKategorieCustomBean)aValue);
                        if ((aValue != null) && (((MiPaKategorie)aValue).getKategorieAuspraegungen() != null)
                                    && (((MiPaKategorie)aValue).getKategorieAuspraegungen().size() == 1)) {
                            for (final MipaKategorieAuspraegungCustomBean currentAuspraegung
                                        : ((MipaKategorieCustomBean)aValue).getKategorieAuspraegungen()) {
                                value.getMiPaNutzung().setAusgewaehlteAuspraegung(currentAuspraegung);
                            }
                        }
                    } else {
                        MiPaKategorie oldKategory = null;
                        if (((oldKategory = value.getMiPaNutzung().getMiPaKategorie()) != null) && (aValue != null)) {
                            if (!oldKategory.equals(aValue)) {
                                if (log.isDebugEnabled()) {
                                    log.debug("Kategorie hat sich geändert --> Ausprägung ist nicht mehr gültig");
                                }
                                value.getMiPaNutzung().setAusgewaehlteAuspraegung(null);
                                value.getMiPaNutzung().setAusgewaehlteNummer(null);
                            }
                        }
                        value.getMiPaNutzung().setMiPaKategorie((MipaKategorieCustomBean)aValue);
                        if ((aValue != null) && (((MiPaKategorie)aValue).getKategorieAuspraegungen() != null)
                                    && (((MiPaKategorie)aValue).getKategorieAuspraegungen().size() == 1)) {
                            for (final MipaKategorieAuspraegungCustomBean currentAuspraegung
                                        : ((MiPaKategorie)aValue).getKategorieAuspraegungen()) {
                                value.getMiPaNutzung().setAusgewaehlteAuspraegung(currentAuspraegung);
                            }
                        }
                    }
                    break;
                }
                case AUSPRAEGUNG_COLUMN: {
                    if (value.getMiPaNutzung() == null) {
                        value.setMiPaNutzung(MipaNutzungCustomBean.createNew());
                    } else if ((aValue != null) && (aValue instanceof MiPaKategorieAuspraegung)) {
                        value.getMiPaNutzung().setAusgewaehlteAuspraegung((MipaKategorieAuspraegungCustomBean)aValue);
                        value.getMiPaNutzung().setAusgewaehlteNummer(null);
                    } else if ((aValue != null) && (aValue instanceof Integer)) {
                        value.getMiPaNutzung().setAusgewaehlteAuspraegung(null);
                        value.getMiPaNutzung().setAusgewaehlteNummer((Integer)aValue);
                    } else {
                        value.getMiPaNutzung().setAusgewaehlteAuspraegung(null);
                        value.getMiPaNutzung().setAusgewaehlteNummer(null);
                    }
                    break;
                }
                case NUTZER_COLUMN: {
                    value.setNutzer((String)aValue);
                    break;
                }
                case VERTRAGS_BEGINN_COLUMN: {
                    if ((aValue instanceof Date) || (aValue == null)) {
                        value.setVertragsbeginn((Date)aValue);
                    } // else if(aValue == null){
//                        value.setVertragsbeginn(null);
//                    }
                    break;
                }
                case VERTRAGS_ENDE_COLUMN: {
                    if ((aValue instanceof Date) || (aValue == null)) {
                        value.setVertragsende((Date)aValue);
                    }
                    break;
                }
                default: {
                    log.warn("Keine Spalte für angegebenen Index vorhanden: " + columnIndex);
                    return;
                }
            }
            fireTableDataChanged();
        } catch (Exception ex) {
            log.error("Fehler beim setzen von Daten in dem Modell: Zeile: " + rowIndex + " Spalte" + columnIndex, ex);
        }
    }

    @Override
    public Class<?> getColumnClass(final int columnIndex) {
        switch (columnIndex) {
            case LAGE_COLUMN: {
                return String.class;
            }
            case AKTENZEICHEN_COLUMN: {
                return String.class;
            }
            case FLAECHE_COLUMN: {
                return Integer.class;
            }
            case ALTE_NUTZUNG_COLUMN: {
                return String.class;
            }
            case NUTZUNG_COLUMN: {
                return MiPaKategorie.class;
            }
            case AUSPRAEGUNG_COLUMN: {
                return Object.class;
            }
            case NUTZER_COLUMN: {
                return String.class;
            }
            case VERTRAGS_BEGINN_COLUMN: {
                return Date.class;
            }
            case VERTRAGS_ENDE_COLUMN: {
                return Date.class;
            }
            default: {
                log.warn("Die gewünschte Spalte exitiert nicht, es kann keine Klasse zurück geliefert werden");
                return null;
            }
        }
    }

    /**
     * DOCUMENT ME!
     */
    private void initDocumentModels() {
        bemerkungDocumentModel = new SimpleDocumentModel() {

                @Override
                public void assignValue(final String newValue) {
                    if (log.isDebugEnabled()) {
                        log.debug("Bemerkung assigned");
                        log.debug("new Value: " + newValue);
                    }
                    valueToCheck = newValue;
                    fireValidationStateChanged(this);
                    if ((currentSelectedMiPa != null) && (getStatus() == VALID)) {
                        currentSelectedMiPa.setBemerkung(newValue);
                    }
                }
            };
    }

    /**
     * DOCUMENT ME!
     *
     * @param   miPa  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getIndexOfMiPa(final MiPa miPa) {
        return miPas.indexOf(miPa);
    }

    /**
     * DOCUMENT ME!
     */
    public void clearSlaveComponents() {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Clear Slave Components");
            }
            bemerkungDocumentModel.clear(0, bemerkungDocumentModel.getLength());
        } catch (Exception ex) {
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public SimpleDocumentModel getBemerkungDocumentModel() {
        return bemerkungDocumentModel;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  newMiPa  DOCUMENT ME!
     */
    public void setCurrentSelectedMipa(final MiPa newMiPa) {
        currentSelectedMiPa = newMiPa;
        if (currentSelectedMiPa != null) {
            try {
                bemerkungDocumentModel.clear(0, bemerkungDocumentModel.getLength());
                bemerkungDocumentModel.insertString(0, currentSelectedMiPa.getBemerkung(), null);
            } catch (BadLocationException ex) {
                // TODO Böse
                log.error("Fehler beim setzen des BemerkungsModells: ", ex);
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("nichts selektiert lösche Felder");
            }
            clearSlaveComponents();
        }
    }
}
