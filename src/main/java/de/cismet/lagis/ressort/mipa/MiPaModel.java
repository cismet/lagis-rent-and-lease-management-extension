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

import java.util.*;
import java.util.Date;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.text.BadLocationException;

import de.cismet.cids.custom.beans.lagis.MipaCustomBean;
import de.cismet.cids.custom.beans.lagis.MipaKategorieAuspraegungCustomBean;
import de.cismet.cids.custom.beans.lagis.MipaKategorieCustomBean;
import de.cismet.cids.custom.beans.lagis.MipaNutzungCustomBean;

import de.cismet.cismap.commons.features.Feature;

import de.cismet.lagis.broker.LagisBroker;

import de.cismet.lagis.models.CidsBeanTableModel_Lagis;
import de.cismet.lagis.models.documents.SimpleDocumentModel;

import de.cismet.lagisEE.entity.extension.vermietung.MiPa;
import de.cismet.lagisEE.entity.extension.vermietung.MiPaKategorie;
import de.cismet.lagisEE.entity.extension.vermietung.MiPaKategorieAuspraegung;

/**
 * DOCUMENT ME!
 *
 * @author   Sebastian Puhl
 * @version  $Revision$, $Date$
 */
public class MiPaModel extends CidsBeanTableModel_Lagis {

    //~ Static fields/initializers ---------------------------------------------

    private static final String[] COLUMN_HEADER = {
            "Lage",
            "Aktenzeichen",
            "Fläche m²",
            "Nutzung",
            "Ausprägung",
            "Nutzer",
            "Vertragsbeginn",
            "Vertragsende",
        };

    private static final Class[] COLUMN_CLASSES = {
            String.class,
            String.class,
            Integer.class,
            MiPaKategorie.class,
            Object.class,
            String.class,
            Date.class,
            Date.class
        };

    public static final int LAGE_COLUMN = 0;
    public static final int AKTENZEICHEN_COLUMN = 1;
    public static final int FLAECHE_COLUMN = 2;
    public static final int NUTZUNG_COLUMN = 3;
    public static final int AUSPRAEGUNG_COLUMN = 4;
    public static final int NUTZER_COLUMN = 5;
    public static final int VERTRAGS_BEGINN_COLUMN = 6;
    public static final int VERTRAGS_ENDE_COLUMN = 7;

    //~ Instance fields --------------------------------------------------------

    private final Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private SimpleDocumentModel bemerkungDocumentModel;
    private MiPa currentSelectedMiPa = null;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new MiPaModel object.
     */
    public MiPaModel() {
        super(COLUMN_HEADER, COLUMN_CLASSES, MipaCustomBean.class);
        initDocumentModels();
    }

    /**
     * Creates a new MiPaModel object.
     *
     * @param  miPas  DOCUMENT ME!
     */
    public MiPaModel(final Collection<MipaCustomBean> miPas) {
        super(COLUMN_HEADER, COLUMN_CLASSES, miPas);
        initDocumentModels();
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Object getValueAt(final int rowIndex, final int columnIndex) {
        try {
            if (rowIndex >= getRowCount()) {
                log.warn("Cannot access row " + rowIndex + ". There are just " + getRowCount() + " rows");
                return null;
            }

            final MiPa value = getCidsBeanAtRow(rowIndex);

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
     * @param  rowIndex  DOCUMENT ME!
     */
    @Override
    public void removeCidsBean(final int rowIndex) {
        final MiPa miPa = getCidsBeanAtRow(rowIndex);
        if ((miPa != null) && (miPa.getGeometry() != null)) {
            LagisBroker.getInstance().getMappingComponent().getFeatureCollection().removeFeature(miPa);
        }
        super.removeCidsBean(rowIndex);
    }

    @Override
    public boolean isCellEditable(final int rowIndex, final int columnIndex) {
        if ((COLUMN_HEADER.length > columnIndex) && (getRowCount() > rowIndex) && isInEditMode()) {
            if (columnIndex == AUSPRAEGUNG_COLUMN) {
                final MiPa currentMiPa = getCidsBeanAtRow(rowIndex);
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
     * @return  DOCUMENT ME!
     */
    public Vector<Feature> getAllMiPaFeatures() {
        final Vector<Feature> tmp = new Vector<Feature>();
        if (getCidsBeans() != null) {
            final Iterator<MipaCustomBean> it = (Iterator<MipaCustomBean>)getCidsBeans().iterator();
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

    @Override
    public void setValueAt(final Object aValue, final int rowIndex, final int columnIndex) {
        try {
            final MiPa value = getCidsBeanAtRow(rowIndex);
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
